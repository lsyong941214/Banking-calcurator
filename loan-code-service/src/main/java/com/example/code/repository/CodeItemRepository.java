package com.example.code.repository;

import com.example.code.domain.CodeItem;
import com.example.code.domain.CodeItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeItemRepository extends JpaRepository<CodeItem, CodeItemId> {

    List<CodeItem> findByGroupCodeOrderBySortOrder(String groupCode);
}
