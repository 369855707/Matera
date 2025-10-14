package com.maternity.service;

import com.maternity.dto.MatronProfileDTO;
import com.maternity.exception.ResourceNotFoundException;
import com.maternity.model.MatronProfile;
import com.maternity.repository.MatronProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatronService {

    private final MatronProfileRepository matronProfileRepository;

    public MatronService(MatronProfileRepository matronProfileRepository) {
        this.matronProfileRepository = matronProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<MatronProfileDTO> getAllMatrons() {
        return matronProfileRepository.findAll().stream()
                .map(MatronProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatronProfileDTO getMatronById(Long id) {
        MatronProfile matron = matronProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matron not found with id: " + id));
        return MatronProfileDTO.fromEntity(matron);
    }

    @Transactional(readOnly = true)
    public List<MatronProfileDTO> getAvailableMatrons() {
        return matronProfileRepository.findByIsAvailable(true).stream()
                .map(MatronProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatronProfileDTO> searchByLocation(String location) {
        return matronProfileRepository.findByLocationContaining(location).stream()
                .map(MatronProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatronProfileDTO> filterByPriceRange(Double minPrice, Double maxPrice) {
        return matronProfileRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(MatronProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatronProfile updateMatronProfile(MatronProfile matronProfile) {
        return matronProfileRepository.save(matronProfile);
    }
}
