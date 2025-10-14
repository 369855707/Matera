package com.maternity.repository;

import com.maternity.model.MotherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MotherProfileRepository extends JpaRepository<MotherProfile, Long> {
    Optional<MotherProfile> findByUserId(Long userId);

    @Query("SELECT m FROM MotherProfile m WHERE m.dueDate IS NOT NULL AND m.dueDate > :today AND m.babyBirthDate IS NULL")
    List<MotherProfile> findExpectingMothers(@Param("today") LocalDate today);

    @Query("SELECT m FROM MotherProfile m WHERE m.babyBirthDate IS NOT NULL")
    List<MotherProfile> findMothersWithBabies();
}
