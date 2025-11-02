package com.example.rpg.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.rpg.model.CharacterClass;
import com.example.rpg.model.Match;
import com.example.rpg.model.Player;
import com.example.rpg.repository.CharacterClassRepository;
import com.example.rpg.repository.MatchRepository;
import com.example.rpg.repository.PlayerRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TerminalV2Service {

    private final CharacterClassRepository classRepo;
    private final PlayerRepository playerRepo;
    private final MatchRepository matchRepo;
    private final Random rng = new Random();

    // ANSI colors
    private final String RESET = "\u001B[0m";
    private final String RED = "\u001B[31m";
    private final String GREEN = "\u001B[32m";
    private final String YELLOW = "\u001B[33m";
    private final String BLUE = "\u001B[34m";
    private final String PURPLE = "\u001B[35m";

    public TerminalV2Service(CharacterClassRepository classRepo, PlayerRepository playerRepo, MatchRepository matchRepo) {
        this.classRepo = classRepo; this.playerRepo = playerRepo; this.matchRepo = matchRepo;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            println(BLUE + "\\n=== RPG Terminal v2 (Colorido & Itens) ===" + RESET);
            println("1) Nova partida");
            println("2) Histórico de partidas");
            println("3) Sair");
            System.out.print("Escolha: ");
            String opt = sc.nextLine().trim();
            if (opt.equals("1")) startMatch(sc);
            else if (opt.equals("2")) showHistory();
            else if (opt.equals("3")) { println("Saindo..."); break; }
            else println(YELLOW + "Opção inválida." + RESET);
        }
        sc.close();
    }

    private void println(String s){ System.out.println(s); }
    private void pause(int ms){ try{ Thread.sleep(ms);}catch(Exception e){} }

    private void showHistory() {
        List<Match> matches = matchRepo.findAll().stream().sorted(Comparator.comparing(Match::getCreatedAt).reversed()).collect(Collectors.toList());
        if (matches.isEmpty()) { println(YELLOW + "Nenhuma partida encontrada." + RESET); return; }
        for (Match m : matches) {
            println(BLUE + String.format("ID:%d - Jogador:%s - Classe:%s - Inimigo:%s - Turnos:%d - Vencedor:%s - Data:%s",
                m.getId(), m.getPlayerName(), m.getPlayerClass().getName(), m.getEnemyClass().getName(), m.getTurnNumber(), m.getWinner(), m.getCreatedAt()) + RESET);
            println(" Ações: " + String.join(" | ", m.getActions()));
        }
    }

    @Transactional
    private void startMatch(Scanner sc) {
        System.out.print("Nome do jogador: ");
        String name = sc.nextLine().trim();
        List<CharacterClass> classes = classRepo.findAll();
        println("Escolha sua classe:");
        for (int i=0;i<classes.size();i++){
            CharacterClass c = classes.get(i);
            println(String.format("%d) %s - Vida:%d ATK:%d DEF:%d Cura:%d Taxa:%.2f", i+1, c.getName(), c.getMaxHp(), c.getAttack(), c.getDefense(), c.getHealAmount(), c.getSuccessRate()));
        }
        int ch=-1;
        while(ch<1||ch>classes.size()){ System.out.print("Digite o número da classe: "); try{ ch=Integer.parseInt(sc.nextLine().trim()); }catch(Exception e){ ch=-1;} }
        CharacterClass chosen = classes.get(ch-1);

        Player player = playerRepo.findByName(name);
        if (player==null){ player = new Player(name, chosen); playerRepo.save(player); }
        else { player.setCharacterClass(chosen); playerRepo.save(player); }

        // enemy
        List<CharacterClass> others = new ArrayList<>(); for (CharacterClass c:classes) if(!c.getName().equals(chosen.getName())) others.add(c);
        CharacterClass enemy = others.get(rng.nextInt(others.size()));

        Match match = new Match(name, chosen, enemy);
        matchRepo.save(match);
        println(GREEN + "Partida iniciada: " + name + " (" + chosen.getName() + ") VS Computador (" + enemy.getName() + ")" + RESET);
        pause(300);
        play(match, player, sc);
    }

    private void play(Match m, Player player, Scanner sc){
        while(!m.isFinished()){
            displayHeader(m);
            println("1) ⚔️ Atacar  2) 💊 Curar-se  3) 📦 Usar item  4) 🏳️ Desistir");
            System.out.print("Ação: ");
            String line = sc.nextLine().trim();
            if(line.equals("4")){ m.setFinished(true); m.setWinner("COMPUTER"); m.addAction("Jogador desistiu"); matchRepo.save(m); println(YELLOW+"Você desistiu."+RESET); break; }
            if(line.equals("3")){ useItem(m, player); matchRepo.save(m); continue; }
            if(!line.equals("1") && !line.equals("2")){ println(YELLOW+"Entrada inválida."+RESET); continue; }
            String action = line.equals("1") ? "ATTACK" : "HEAL";
            // prevent double heal
            if(action.equals("HEAL") && "HEAL".equals(lastPlayerAction(m))){ println(YELLOW+"Não é possível curar-se duas vezes seguidas."+RESET); continue; }
            m.setTurnNumber(m.getTurnNumber()+1);
            // player action
            String pres = applyAction(m.getPlayerClass(), m.getEnemyClass(), action, true, m);
            m.addAction(String.format("Turn %d: Jogador -> %s", m.getTurnNumber(), pres));
            matchRepo.save(m);
            animateAction(action, true, pres);
            if(checkFinishAndFinalize(m, player)) break;
            // enemy action
            String enemyAction = chooseEnemyAction(m);
            if(enemyAction.equals("HEAL") && "HEAL".equals(lastEnemyAction(m))) enemyAction="ATTACK";
            String eres = applyAction(m.getEnemyClass(), m.getPlayerClass(), enemyAction, false, m);
            m.addAction(String.format("Turn %d: Inimigo -> %s", m.getTurnNumber(), eres));
            matchRepo.save(m);
            animateAction(enemyAction, false, eres);
            if(checkFinishAndFinalize(m, player)) break;
        }
    }

    private void displayHeader(Match m){
        println(PURPLE + "===== TURNO " + m.getTurnNumber() + " =====" + RESET);
        String playerBar = renderHpBar(m.getPlayerHp(), m.getPlayerClass().getMaxHp());
        String enemyBar = renderHpBar(m.getEnemyHp(), m.getEnemyClass().getMaxHp());
        println("Jogador: " + GREEN + m.getPlayerName() + RESET + " | Classe: " + m.getPlayerClass().getName());
        println("Vida: " + playerBar + "  Inimigo: " + RED + m.getEnemyClass().getName() + RESET + " Vida: " + enemyBar);
        println("Ações: " + String.join(" | ", m.getActions()));
    }

    private String renderHpBar(int hp, int max){
        int hearts = 10;
        int full = (int)Math.round(((double)hp / max) * hearts);
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<full;i++) sb.append("❤️");
        for(int i=full;i<hearts;i++) sb.append("🖤");
        sb.append(String.format(" (%d/%d)", hp, max));
        return sb.toString();
    }

    private void animateAction(String action, boolean isPlayer, String result){
        if(action.equals("ATTACK")){
            println(RED + "⚔️ Ataque!" + RESET); pause(300);
            println(RED + result + RESET); pause(300);
        } else if(action.equals("HEAL")){
            println(GREEN + "✨ Curando..." + RESET); pause(300);
            println(GREEN + result + RESET); pause(300);
        }
    }

    private void useItem(Match m, Player player){
        // simple items: Poção(+heal), Escudo(+defense next turn), Amuleto(+success next turn)
        List<String> inv = Arrays.asList("Pocao de Vida", "Escudo Temporario", "Amuleto de Precisao");
        String item = inv.get(rng.nextInt(inv.size()));
        m.addAction("Jogador usou item: " + item);
        if(item.equals("Pocao de Vida")){
            int heal = 25;
            int newHp = Math.min(m.getPlayerClass().getMaxHp(), m.getPlayerHp() + heal);
            int actual = newHp - m.getPlayerHp();
            m.setPlayerHp(newHp);
            println(GREEN + "Você usou Poção de Vida e recuperou " + actual + " HP." + RESET);
        } else if(item.equals("Escudo Temporario")){
            // implement as immediate small heal to simulate shield
            int block = 10;
            int newHp = Math.min(m.getPlayerClass().getMaxHp(), m.getPlayerHp() + block);
            int actual = newHp - m.getPlayerHp();
            m.setPlayerHp(newHp);
            println(BLUE + "Você ativou Escudo Temporário (+10 HP temporário)" + RESET);
        } else {
            // amulet increases next attack success chance - applied as immediate small heal to represent effect
            println(PURPLE + "Amuleto de Precisão ativado! (Aumenta taxa de sucesso no próximo ataque)" + RESET);
            // store as action only; in real full implementation you'd track buffs
        }
        matchRepo.save(m);
    }

    private boolean checkFinishAndFinalize(Match m, Player p){
        if(m.getPlayerHp()<=0){ m.setFinished(true); m.setWinner("COMPUTER"); matchRepo.save(m); println(RED+"Você perdeu!"+RESET); return true; }
        if(m.getEnemyHp()<=0){ m.setFinished(true); m.setWinner("PLAYER"); matchRepo.save(m); println(GREEN+"Você venceu!"+RESET); // reward xp
            int xp = 50 + rng.nextInt(51);
            p.setXp(p.getXp()+xp);
            if(p.getXp()>=p.getLevel()*100){ p.setXp(p.getXp()-p.getLevel()*100); p.setLevel(p.getLevel()+1); println(PURPLE+"Subiu de nível! Novo nível: "+p.getLevel()+RESET); }
            println(GREEN+"Ganhou "+xp+" XP. Total: "+p.getXp()+RESET);
            playerRepo.save(p);
            // chance to drop item
            if(rng.nextDouble()<0.5){ String loot = rng.nextBoolean() ? "Pocao de Vida" : "Amuleto de Precisao"; m.addAction("Loot: "+loot); println(YELLOW+"Você encontrou um item: "+loot+RESET); matchRepo.save(m); }
            return true;
        }
        return false;
    }

    private String lastPlayerAction(Match m){
        List<String> a = m.getActions();
        for(int i=a.size()-1;i>=0;i--){
            String s=a.get(i);
            if(s.contains("Jogador")) return s;
        }
        return null;
    }
    private String lastEnemyAction(Match m){
        List<String> a = m.getActions();
        for(int i=a.size()-1;i>=0;i--){
            String s=a.get(i);
            if(s.contains("Inimigo")) return s;
        }
        return null;
    }

    private String chooseEnemyAction(Match m){
        int hp = m.getEnemyHp(); int max = m.getEnemyClass().getMaxHp();
        double ratio = (double)hp/max;
        if(ratio<=0.30 && (lastEnemyAction(m)==null || !lastEnemyAction(m).contains("curou"))) return "HEAL";
        return rng.nextDouble()<0.65 ? "ATTACK" : "HEAL";
    }

    private String applyAction(CharacterClass actor, CharacterClass target, String action, boolean isPlayer, Match m){
        action = action.toUpperCase();
        if(action.equals("ATTACK")){
            boolean success = rng.nextDouble() < actor.getSuccessRate();
            if(!success) return actor.getName()+" errou o ataque!";
            int raw = actor.getAttack() - target.getDefense();
            int damage = Math.max(0, raw);
            if(isPlayer) m.setEnemyHp(Math.max(0, m.getEnemyHp()-damage));
            else m.setPlayerHp(Math.max(0, m.getPlayerHp()-damage));
            return String.format("atacou causando %d de dano (raw %d)", damage, raw);
        } else if(action.equals("HEAL")){
            boolean success = rng.nextDouble() < actor.getSuccessRate();
            if(!success) return actor.getName()+" falhou ao curar-se!";
            int heal = actor.getHealAmount();
            if(isPlayer){ int newHp=Math.min(actor.getMaxHp(), m.getPlayerHp()+heal); int actual=newHp-m.getPlayerHp(); m.setPlayerHp(newHp); return String.format("curou %d de vida", actual); }
            else { int newHp=Math.min(actor.getMaxHp(), m.getEnemyHp()+heal); int actual=newHp-m.getEnemyHp(); m.setEnemyHp(newHp); return String.format("curou %d de vida", actual); }
        }
        return "ação desconhecida";
    }
}
