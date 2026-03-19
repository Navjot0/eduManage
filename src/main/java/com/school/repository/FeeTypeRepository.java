package com.school.repository;

import com.school.entity.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeTypeRepository extends JpaRepository<FeeType, UUID> {
    List<FeeType> findByIsActive(Boolean isActive);
    boolean existsByName(String name);
}
