package com.popupmc.paidpvp;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Objective;

// Sync player kills and death stats
public class OnPlayerJoinEvent implements Listener {
    @EventHandler
    public void onKill(PlayerJoinEvent e) {

        // Get Player
        Player p = e.getPlayer();

        // Get kill & death stats
        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);

        // Get Kills Objective
        Objective playerKillsObj = p.getScoreboard().getObjective("playerKills");
        if(playerKillsObj == null) {
            PaidPVP.plugin.getLogger().warning("Objective playerKills is null");
            return;
        }

        // Update to make sure its accurate
        playerKillsObj.getScore(p.getName()).setScore(kills);

        // Get deaths objective
        Objective deathsObj = p.getScoreboard().getObjective("deaths");
        if(deathsObj == null) {
            PaidPVP.plugin.getLogger().warning("Objective deaths is null");
            return;
        }

        // Update to make sure it's accurate
        deathsObj.getScore(p.getName()).setScore(deaths);

        ///////////////////////
        // Stats are now synced with score
        ///////////////////////

        // Get kills score obj
        int playerKills = playerKillsObj.getScore(p.getName()).getScore();

        // Stop here if no pvp kills
        if(playerKills <= 0)
            return;

        // Get amount paid
        Objective paidPlayerKillsObj = p.getScoreboard().getObjective("paidPlayerKills");
        if(paidPlayerKillsObj == null) {
            PaidPVP.plugin.getLogger().warning("Objective paidPlayerKills is null");
            return;
        }

        int paidPlayerKills = paidPlayerKillsObj.getScore(p.getName()).getScore();

        // Get amount unpaid
        int unpaidKills = playerKills - paidPlayerKills;

        // If paid up then stop here
        if(unpaidKills <= 0)
            return;

        // Convert unpaid to money
        int emeralds = unpaidKills / PaidPVP.killsPerEmerald;
        float money = (float) Math.round(emeralds * PaidPVP.exchangeRate * 100) / 100;

        boolean doublePay = p.hasPermission("paidpvp.double");

        // Double earnings if double permission
        if(doublePay) {
            emeralds *= 2;
            money = (float) Math.round(emeralds * PaidPVP.exchangeRate * 100) / 100;
        }

        // Stop here if no emeralds to pay
        if(emeralds <= 0)
            return;

        // Add money to players inventory
        EconomyResponse r = PaidPVP.econ.depositPlayer(p, money);

        // Stop here if vault error
        if(!r.transactionSuccess()) {
            p.sendMessage("Vault Error: Unable to pay you ❇" + money);
            return;
        }

        // Set kills paid up to
        paidPlayerKillsObj.getScore(p.getName()).setScore(playerKills);

        // Announce money deposit
        if(doublePay) {
            p.sendMessage("You've been paid ❇" + money + " for past unpaid pvp kills, worth " + emeralds + " emerald(s)");
        }
        else {
            p.sendMessage("You've been paid ❇" + money + " for past unpaid pvp kills, worth " + emeralds + " emerald(s) and you received double the normal amount.");
        }

        ///////////////////////
        // Player is now paid up amount owed
        ///////////////////////
    }
}
