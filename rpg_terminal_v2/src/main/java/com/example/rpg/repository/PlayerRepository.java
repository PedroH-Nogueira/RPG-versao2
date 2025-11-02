package com.example.rpg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.rpg.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByName(String name);
}
