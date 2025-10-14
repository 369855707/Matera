package com.maternity.repository;

import com.maternity.model.MatronProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatronProfileRepository extends JpaRepository<MatronProfile, Long> {
    Optional<MatronProfile> findByUserId(Long userId);
    List<MatronProfile> findByIsAvailable(Boolean isAvailable);

    @Query("SELECT m FROM MatronProfile m WHERE m.location LIKE %:location%")
    List<MatronProfile> findByLocationContaining(@Param("location") String location);

    @Query("SELECT m FROM MatronProfile m WHERE m.pricePerMonth BETWEEN :minPrice AND :maxPrice")
    List<MatronProfile> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
}