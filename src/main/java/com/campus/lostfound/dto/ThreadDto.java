package com.campus.lostfound.dto;

import com.campus.lostfound.model.AppUser;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemResponse;
import com.campus.lostfound.model.ItemStatus;

import java.time.LocalDateTime;
import java.util.List;

/** A conversation as seen by one viewer. Contact details go only to the reporter. */
public record ThreadDto(Long id, String otherName, String otherEmail, String otherPhone,
                        String myRole, boolean ownerConfirmed, boolean finderConfirmed,
                        boolean iConfirmed, boolean otherConfirmed, boolean closed, boolean completed,
                        LocalDateTime createdAt, List<MessageDto> messages) {

    public static ThreadDto from(ItemResponse t, AppUser viewer) {
        Item item = t.getItem();
        boolean viewerIsReporter = item.getReporter().getId().equals(viewer.getId());
        boolean viewerIsOwner = t.isOwnerSide(viewer);
        AppUser other = viewerIsReporter ? t.getResponder() : item.getReporter();

        List<MessageDto> msgs = t.getMessages().isEmpty()
                // older replies written before chats existed
                ? List.of(new MessageDto(0L, t.getResponder().getName(),
                        t.getResponder().getId().equals(viewer.getId()), t.getMessage(), t.getCreatedAt()))
                : t.getMessages().stream()
                        .map(m -> new MessageDto(m.getId(), m.getSender().getName(),
                                m.getSender().getId().equals(viewer.getId()), m.getBody(), m.getCreatedAt()))
                        .toList();

        return new ThreadDto(t.getId(), other.getName(),
                viewerIsReporter ? other.getEmail() : null,
                viewerIsReporter ? other.getPhone() : null,
                viewerIsOwner ? "OWNER" : "FINDER",
                t.isOwnerConfirmed(), t.isFinderConfirmed(),
                viewerIsOwner ? t.isOwnerConfirmed() : t.isFinderConfirmed(),
                viewerIsOwner ? t.isFinderConfirmed() : t.isOwnerConfirmed(),
                item.getStatus() == ItemStatus.CLOSED,
                t.isOwnerConfirmed() && t.isFinderConfirmed(),
                t.getCreatedAt(), msgs);
    }
}
