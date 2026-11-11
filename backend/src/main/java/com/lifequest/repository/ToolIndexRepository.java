package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.ToolIndexItem;

public interface ToolIndexRepository extends JpaRepository<ToolIndexItem, Long> {

	List<ToolIndexItem> findByCatalogIdIsNotNullOrderByIdAsc();

	Optional<ToolIndexItem> findByCatalogId(String catalogId);
}
