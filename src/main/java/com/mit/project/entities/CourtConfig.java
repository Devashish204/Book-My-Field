package com.mit.project.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "court_config")
@Data
public class CourtConfig {
  @Id private String courtName;
  private Integer total;
  private boolean available;
  private double hourlyRate;
}
