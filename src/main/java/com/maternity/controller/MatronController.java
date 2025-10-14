package com.maternity.controller;

import com.maternity.dto.MatronProfileDTO;
import com.maternity.service.MatronService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matrons")
@Tag(name = "Matrons", description = "Maternity matron profile management")
@SecurityRequirement(name = "bearerAuth")
public class MatronController {

    private final MatronService matronService;

    public MatronController(MatronService matronService) {
        this.matronService = matronService;
    }

    @Operation(summary = "Get all matrons",
               description = "Retrieve a list of all matron profiles")
    @GetMapping
    public ResponseEntity<List<MatronProfileDTO>> getAllMatrons() {
        return ResponseEntity.ok(matronService.getAllMatrons());
    }

    @Operation(summary = "Get matron by ID",
               description = "Retrieve detailed profile of a specific matron")
    @GetMapping("/{id}")
    public ResponseEntity<MatronProfileDTO> getMatronById(@PathVariable Long id) {
        return ResponseEntity.ok(matronService.getMatronById(id));
    }

    @Operation(summary = "Get available matrons",
               description = "Retrieve list of matrons currently available for booking")
    @GetMapping("/available")
    public ResponseEntity<List<MatronProfileDTO>> getAvailableMatrons() {
        return ResponseEntity.ok(matronService.getAvailableMatrons());
    }

    @Operation(summary = "Search matrons by location",
               description = "Find matrons in a specific location")
    @GetMapping("/search")
    public ResponseEntity<List<MatronProfileDTO>> searchByLocation(@RequestParam String location) {
        return ResponseEntity.ok(matronService.searchByLocation(location));
    }

    @Operation(summary = "Filter matrons by price range",
               description = "Find matrons within a specific price range (per month)")
    @GetMapping("/filter/price")
    public ResponseEntity<List<MatronProfileDTO>> filterByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return ResponseEntity.ok(matronService.filterByPriceRange(minPrice, maxPrice));
    }
}
