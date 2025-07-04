package com.mit.project.services;

import com.mit.project.entities.CourtConfig;
import com.mit.project.repositories.CourtConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourtConfigService {

  private final CourtConfigRepository courtConfigRepository;

  public void saveCourtConfig(CourtConfig newCourtConfig) {
    Optional<CourtConfig> byId =
        courtConfigRepository.findById(newCourtConfig.getCourtName().toUpperCase());

    if (byId.isPresent()) {
      CourtConfig existingConfig = byId.get();
      existingConfig.setTotal(newCourtConfig.getTotal());
      existingConfig.setAvailable(newCourtConfig.isAvailable());
      existingConfig.setHourlyRate(newCourtConfig.getHourlyRate()   );
      courtConfigRepository.save(existingConfig);
    } else {
      newCourtConfig.setCourtName(newCourtConfig.getCourtName().toUpperCase());
      courtConfigRepository.save(newCourtConfig);
    }
  }

  public List<CourtConfig> getAllCourtConfigs() {
    return courtConfigRepository.findAll();
  }

  public List<String> getCourtNames() {
    return courtConfigRepository.findAll().stream().map(CourtConfig::getCourtName).toList();
  }

  public void deleteCourtConfig(CourtConfig config) {
    courtConfigRepository.delete(config);
  }

  public CourtConfig updateCourtRate(String sport, double newRate) {
    CourtConfig config =
        courtConfigRepository
            .findById(sport.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sport not found"));

    config.setHourlyRate(newRate);
    return courtConfigRepository.save(config);
  }
}
