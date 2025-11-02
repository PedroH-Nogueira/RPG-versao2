package com.example.rpg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.rpg.model.CharacterClass;
import java.util.Optional;

public interface CharacterClassRepository extends JpaRepository<CharacterClass, Long> {
    Optional<CharacterClass> findByName(String name);
}
