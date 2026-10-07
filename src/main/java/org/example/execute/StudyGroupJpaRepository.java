package org.example.execute;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface StudyGroupJpaRepository extends JpaRepository<StudyGroupEntity, Integer> {
    Optional<StudyGroupEntity> findFirstByOrderByIdAsc();
}
