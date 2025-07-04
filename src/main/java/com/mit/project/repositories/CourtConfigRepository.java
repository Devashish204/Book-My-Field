package com.mit.project.repositories;

import com.mit.project.entities.CourtConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtConfigRepository extends JpaRepository<CourtConfig, String> {}
