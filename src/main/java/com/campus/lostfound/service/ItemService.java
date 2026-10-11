package com.campus.lostfound.service;

import com.campus.lostfound.dto.ItemForm;
import com.campus.lostfound.model.*;
import com.campus.lostfound.repository.ItemImageRepository;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.ItemResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository items;
    private final ItemResponseRepository responses;
    private final ItemImageRepository images;

    public List<Item> search(String q, ItemType type, ItemStatus status) {
        Specification<Item> spec = (root, cq, cb) -> cb.conjunction();
        if (type != null) spec = spec.and((r, c, cb) -> cb.equal(r.get("type"), type));
        if (status != null) spec = spec.and((r, c, cb) -> cb.equal(r.get("status"), status));
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((r, c, cb) -> cb.or(
                    cb.like(cb.lower(r.get("title")), like),
                    cb.like(cb.lower(r.get("description")), like),
                    cb.like(cb.lower(r.get("location")), like),
                    cb.like(cb.lower(r.get("category")), like)));
        }
        return items.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Item get(Long id) {
        return items.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    public List<Item> reportedBy(AppUser user) {
        return items.findByReporterOrderByCreatedAtDesc(user);
    }

    public List<ItemResponse> responsesBy(AppUser user) {
        return responses.findByResponderOrderByCreatedAtDesc(user);
    }

    public ItemImage imageOf(Long itemId) {
        return images.findByItemId(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No photo for this item"));
    }

    @Transactional
    public Item create(ItemForm f, MultipartFile image, AppUser reporter) throws IOException {
        byte[] data = null;
        String contentType = null;
        if (image != null && !image.isEmpty()) {
            contentType = image.getContentType();
            if (contentType == null || !contentType.matches("image/(png|jpeg|gif|webp)"))
                throw new IllegalArgumentException("Upload a PNG, JPG, GIF or WEBP image.");
            data = image.getBytes();
        }

        Item i = new Item();
        i.setType(f.getType());
        i.setTitle(f.getTitle().trim());
        i.setDescription(f.getDescription() == null ? null : f.getDescription().trim());
        i.setCategory(f.getCategory());
        i.setLocation(f.getLocation().trim());
        i.setEventDate(f.getEventDate());
        i.setReporter(reporter);
        i.setHasImage(data != null);
        Item saved = items.save(i);

        if (data != null) {
            ItemImage img = new ItemImage();
            img.setItemId(saved.getId());
            img.setContentType(contentType);
            img.setData(data);
            images.save(img);
        }
        return saved;
    }

    /** Start a conversation (or add to the one you already have): "I found it" / "This is mine". */
    @Transactional
    public void respond(Long itemId, String message, AppUser user) {
        Item item = get(itemId);
        if (item.getReporter().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot reply to your own report.");
        if (item.getStatus() == ItemStatus.CLOSED)
            throw new IllegalArgumentException("This case is already closed.");
        String text = cleanMessage(message);

        ItemResponse t = item.getResponses().stream()
                .filter(r -> r.getResponder().getId().equals(user.getId()))
                .findFirst().orElse(null);
        if (t == null) {
            t = new ItemResponse();
            t.setItem(item);
            t.setResponder(user);
            t.setMessage(text);
            item.getResponses().add(t);
        }
        addMessage(t, user, text);
        if (item.getStatus() == ItemStatus.OPEN) item.setStatus(ItemStatus.IN_PROGRESS);
        items.save(item);
    }

    public ItemResponse threadFor(Long threadId, AppUser user) {
        ItemResponse t = responses.findById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        if (!t.isParticipant(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This conversation is private.");
        return t;
    }

    /** Either person in the conversation can write, until the case is closed. */
    @Transactional
    public void postMessage(Long threadId, String message, AppUser user) {
        ItemResponse t = threadFor(threadId, user);
        if (t.getItem().getStatus() == ItemStatus.CLOSED)
            throw new IllegalArgumentException("This case is closed, so the conversation has ended.");
        addMessage(t, user, cleanMessage(message));
    }

    /**
     * Each side confirms once: the owner "I got my item back", the finder "I handed it over".
     * When both have confirmed, the case is closed and every conversation on it becomes read-only.
     */
    @Transactional
    public void confirm(Long threadId, AppUser user) {
        ItemResponse t = threadFor(threadId, user);
        Item item = t.getItem();
        if (item.getStatus() == ItemStatus.CLOSED)
            throw new IllegalArgumentException("This case is already closed.");
        if (t.isOwnerSide(user)) t.setOwnerConfirmed(true); else t.setFinderConfirmed(true);
        if (t.isOwnerConfirmed() && t.isFinderConfirmed()) {
            item.setStatus(ItemStatus.CLOSED);
            item.setClosedAt(LocalDateTime.now());
        }
    }

    private void addMessage(ItemResponse thread, AppUser sender, String text) {
        ChatMessage m = new ChatMessage();
        m.setThread(thread);
        m.setSender(sender);
        m.setBody(text);
        thread.getMessages().add(m);
    }

    private String cleanMessage(String message) {
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Write a message first.");
        String m = message.trim();
        if (m.length() > 500) throw new IllegalArgumentException("Keep each message under 500 characters.");
        return m;
    }

    /** The person who posted a report can remove it (for example if it was posted by mistake). */
    @Transactional
    public void delete(Long itemId, AppUser user) {
        Item item = get(itemId);
        if (!item.getReporter().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the person who posted this can delete it.");
        if (item.getStatus() == ItemStatus.CLOSED)
            throw new IllegalArgumentException("A returned item stays on the board as a record and cannot be deleted.");
        images.findByItemId(itemId).ifPresent(images::delete);
        items.delete(item);               // also removes its conversations and messages
    }
}
