package com.campus.lostfound.repository;

import com.campus.lostfound.model.AppUser;
import com.campus.lostfound.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByReporterOrderByCreatedAtDesc(AppUser reporter);
}
