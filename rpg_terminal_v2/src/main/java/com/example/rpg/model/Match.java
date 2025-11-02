package com.example.rpg.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String playerName;
    @ManyToOne private CharacterClass playerClass;
    @ManyToOne private CharacterClass enemyClass;
    private int playerHp;
    private int enemyHp;
    private int turnNumber;
    private boolean finished;
    private String winner;
    private LocalDateTime createdAt = LocalDateTime.now();
    @ElementCollection @CollectionTable(name="match_actions", joinColumns=@JoinColumn(name="match_id")) @Column(name="action")
    private List<String> actions = new ArrayList<>();
    public Match() {}
    public Match(String playerName, CharacterClass playerClass, CharacterClass enemyClass) {
        this.playerName = playerName; this.playerClass = playerClass; this.enemyClass = enemyClass;
        this.playerHp = playerClass.getMaxHp(); this.enemyHp = enemyClass.getMaxHp(); this.turnNumber = 0; this.finished = false;
    }
    public Long getId() { return id; } public String getPlayerName() { return playerName; } public CharacterClass getPlayerClass() { return playerClass; } public CharacterClass getEnemyClass() { return enemyClass; }
    public int getPlayerHp() { return playerHp; } public int getEnemyHp() { return enemyHp; } public int getTurnNumber() { return turnNumber; } public boolean isFinished() { return finished; } public String getWinner() { return winner; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; } public List<String> getActions() { return actions; }
    public void setPlayerHp(int v){ this.playerHp=v;} public void setEnemyHp(int v){ this.enemyHp=v;} public void setTurnNumber(int t){ this.turnNumber=t;} public void setFinished(boolean f){ this.finished=f;} public void setWinner(String w){ this.winner=w;} public void addAction(String a){ this.actions.add(a);} public void setActions(List<String> a){ this.actions=a; }
}
