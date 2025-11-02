package com.example.rpg;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.rpg.model.CharacterClass;
import com.example.rpg.repository.CharacterClassRepository;
import com.example.rpg.service.TerminalV2Service;

@SpringBootApplication
public class RpgTerminalV2Application {
    public static void main(String[] args) {
        SpringApplication.run(RpgTerminalV2Application.class, args);
    }

    @Bean
    CommandLineRunner init(CharacterClassRepository classRepo, TerminalV2Service gameService) {
        return args -> {
            if (classRepo.count() == 0) {
                classRepo.save(new CharacterClass("Guerreiro Cibernético", 150, 30, 20, 12, 0.88));
                classRepo.save(new CharacterClass("Mago das Sombras", 100, 40, 6, 25, 0.72));
                classRepo.save(new CharacterClass("Curandeiro das Chamas", 120, 22, 14, 35, 0.80));
            }
            gameService.run();
        };
    }
}
