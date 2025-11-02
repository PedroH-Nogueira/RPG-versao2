package com.example.rpg.model;

import jakarta.persistence.*;

@Entity
@Table(name = "character_classes")
public class CharacterClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private int maxHp;
    private int attack;
    private int defense;
    private int healAmount;
    private double successRate;

    public CharacterClass() {}
    public CharacterClass(String name, int maxHp, int attack, int defense, int healAmount, double successRate) {
        this.name = name; this.maxHp = maxHp; this.attack = attack; this.defense = defense; this.healAmount = healAmount; this.successRate = successRate;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getHealAmount() { return healAmount; }
    public double getSuccessRate() { return successRate; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setAttack(int attack) { this.attack = attack; }
    public void setDefense(int defense) { this.defense = defense; }
    public void setHealAmount(int healAmount) { this.healAmount = healAmount; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
}
