package com.example.rpg.model;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class Player {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int level = 1;
    private int xp = 0;
    @ManyToOne private CharacterClass characterClass;

    public Player() {}
    public Player(String name, CharacterClass c) { this.name = name; this.characterClass = c; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public CharacterClass getCharacterClass() { return characterClass; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLevel(int level) { this.level = level; }
    public void setXp(int xp) { this.xp = xp; }
    public void setCharacterClass(CharacterClass characterClass) { this.characterClass = characterClass; }
}
