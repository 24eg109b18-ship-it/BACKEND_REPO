package com.campus.lostfound.web;

import com.campus.lostfound.dto.*;
import com.campus.lostfound.model.*;
import com.campus.lostfound.repository.AppUserRepository;
import com.campus.lostfound.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;
    private final AppUserRepository users;

    @GetMapping("/items")
    public List<ItemDto> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) ItemType type,
                              @RequestParam(required = false) ItemStatus status) {
        return service.search(q, type, status).stream().map(ItemDto::from).toList();
    }

    @GetMapping("/items/{id}")
    public ItemDetailDto detail(@PathVariable Long id, Principal principal) {
        Item item = service.get(id);
        AppUser me = principal == null ? null : users.findByEmail(principal.getName()).orElse(null);
        boolean reporter = me != null && me.getId().equals(item.getReporter().getId());

        // Conversations are private: the reporter sees all of theirs, a responder sees only their own,
        // everyone else only gets the reply count (inside ItemDto).
        List<ThreadDto> threads = item.getResponses().stream()
                .filter(t -> reporter || (me != null && t.getResponder().getId().equals(me.getId())))
                .map(t -> ThreadDto.from(t, me))
                .toList();
        boolean canRespond = me != null && !reporter && item.getStatus() != ItemStatus.CLOSED && threads.isEmpty();
        return new ItemDetailDto(ItemDto.from(item), reporter, canRespond, threads);
    }

    @GetMapping("/items/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        ItemImage img = service.imageOf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(img.getData());
    }

    @PostMapping(value = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemDto> create(@Valid @ModelAttribute ItemForm form,
                                          @RequestParam(value = "image", required = false) MultipartFile image,
                                          Principal principal) throws IOException {
        Item item = service.create(form, image, currentUser(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemDto.from(item));
    }

    @PostMapping("/items/{id}/respond")
    public Map<String, String> respond(@PathVariable Long id, @RequestBody MessageRequest body, Principal principal) {
        service.respond(id, body.message(), currentUser(principal));
        return Map.of("status", "ok");
    }

    @GetMapping("/threads/{id}")
    public ThreadDto thread(@PathVariable Long id, Principal principal) {
        AppUser me = currentUser(principal);
        return ThreadDto.from(service.threadFor(id, me), me);
    }

    @PostMapping("/threads/{id}/messages")
    public Map<String, String> post(@PathVariable Long id, @RequestBody MessageRequest body, Principal principal) {
        service.postMessage(id, body.message(), currentUser(principal));
        return Map.of("status", "ok");
    }

    @PostMapping("/threads/{id}/confirm")
    public Map<String, String> confirm(@PathVariable Long id, Principal principal) {
        service.confirm(id, currentUser(principal));
        return Map.of("status", "ok");
    }

    @GetMapping("/me/items")
    public MyItemsDto mine(Principal principal) {
        AppUser me = currentUser(principal);
        List<ItemDto> reported = service.reportedBy(me).stream().map(ItemDto::from).toList();
        List<ItemDto> replied = service.responsesBy(me).stream()
                .map(ItemResponse::getItem).distinct().map(ItemDto::from).toList();
        return new MyItemsDto(reported, replied);
    }

    private AppUser currentUser(Principal principal) {
        return users.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again."));
    }
}
