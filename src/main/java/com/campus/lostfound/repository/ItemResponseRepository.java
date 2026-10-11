package com.campus.lostfound.repository;

import com.campus.lostfound.model.AppUser;
import com.campus.lostfound.model.ItemResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemResponseRepository extends JpaRepository<ItemResponse, Long> {
    List<ItemResponse> findByResponderOrderByCreatedAtDesc(AppUser responder);
}
