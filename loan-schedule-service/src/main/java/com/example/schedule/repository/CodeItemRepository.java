package com.example.schedule.repository;

import com.example.schedule.domain.CodeItem;
import com.example.schedule.domain.CodeItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeItemRepository extends JpaRepository<CodeItem, CodeItemId> {

    List<CodeItem> findByGroupCodeOrderBySortOrder(String groupCode);
}
