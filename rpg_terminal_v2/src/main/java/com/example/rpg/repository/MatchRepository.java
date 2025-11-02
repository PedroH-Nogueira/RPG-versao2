package com.example.rpg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.rpg.model.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {}
