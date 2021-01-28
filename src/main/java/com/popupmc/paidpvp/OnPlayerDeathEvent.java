package com.popupmc.paidpvp;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Objective;

// When someone does
public class OnPlayerDeathEvent implements Listener {
    @EventHandler
    public void onKill(PlayerDeathEvent e)
    {
        // Get killer and killed
        Player killed = e.getEntity();

        // Make sure its by another player
        if(e.getEntity().getKiller() == null)
            return;

        Player killer = e.getEntity().getKiller();

        // Get killed score
        Objective playerKillsObj = killer.getScoreboard().getObjective("playerKills");
        if(playerKillsObj == null) {
            PaidPVP.plugin.getLogger().warning("Objective playerKills is null");
            return;
        }

        int killerKills = playerKillsObj.getScore(killer.getName()).getScore();

        // Stop here if not evenly divisible by killsPerEmerald
        if((killerKills % PaidPVP.killsPerEmerald) > 0)
            return;

        // By default killer gets 1 emerald and killed gets nothing
        int emeraldsKiller = 1;
        int emeraldsKilled = 0;

        // Double Pay Perm?
        boolean doublePay = killer.hasPermission("paidpvp.double");

        // Everybody wins with double pay
        if(doublePay) {
            emeraldsKiller++;
            emeraldsKilled++;
        }

        // Convert to money
        float moneyKiller = (float) Math.round(emeraldsKiller * PaidPVP.exchangeRate * 100) / 100;
        float moneyKilled = 0f;

        if(emeraldsKilled > 0)
            moneyKilled = (float) Math.round(emeraldsKilled * PaidPVP.exchangeRate * 100) / 100;

        // Pay killer
        EconomyResponse r = PaidPVP.econ.depositPlayer(killer, moneyKiller);
        if(!r.transactionSuccess()) {
            killer.sendMessage("Vault Error: Unable to pay you ❇" + moneyKiller);
            return;
        }

        // Pay killed (if double pay)
        if(emeraldsKilled > 0) {
            r = PaidPVP.econ.depositPlayer(killed, moneyKilled);
            if(!r.transactionSuccess()) {
                killed.sendMessage("Vault Error: Unable to pay you ❇" + moneyKilled);
            }
        }

        Objective paidPlayerKillsObj = killer.getScoreboard().getObjective("paidPlayerKills");
        if(paidPlayerKillsObj == null) {
            PaidPVP.plugin.getLogger().warning("Objective paidPlayerKills is null");
            return;
        }

        // Set kills paid up to
        paidPlayerKillsObj.getScore(killer.getName()).setScore(killerKills);

        // Announce pay
        if(!doublePay) {
            killer.sendMessage("You've been paid ❇" + moneyKiller + " for 5 kills, worth " + emeraldsKiller + " emerald(s)");
        }
        else {
            killer.sendMessage("You've been paid ❇" + moneyKiller + " for 5 kills, worth " + emeraldsKiller + " emerald(s) and you received double the normal amount.");
            killed.sendMessage("You've been paid ❇" + moneyKilled + " for being slain by a killer with doubleMoney perk, worth " + emeraldsKilled + " emerald(s)");
        }

        ///////////////////////
        // Killer and Killed are now paid normally
        ///////////////////////

        // Setup WG query
        // Apparently nobody knows hwo to do this but this is the best way I could fine online to test for arena flag
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(killer);
        Location loc = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        // Test for PVP Arena flag
        // Stop here if not in an arena
        if(!query.testState(loc, localPlayer, PaidPVP.pvpArenaFlag))
            return;

        // Pay killer
        r = PaidPVP.econ.depositPlayer(killer, moneyKiller);
        if(!r.transactionSuccess()) {
            killer.sendMessage("Vault Error: Unable to pay you ❇" + moneyKiller);
        }

        // Pay killed (if double pay)
        if(emeraldsKilled > 0) {
            r = PaidPVP.econ.depositPlayer(killed, moneyKilled);
            if(!r.transactionSuccess()) {
                killed.sendMessage("Vault Error: Unable to pay you ❇" + moneyKilled);
            }
        }

        // Announce pay
        if(!doublePay) {
            killer.sendMessage("You've been paid an extra ❇" + moneyKiller + " for an arena kill, worth " + emeraldsKiller + " emerald(s)");
        }
        else {
            killer.sendMessage("You've been paid extra ❇" + moneyKiller + " for an arena kill, worth " + emeraldsKiller + " emerald(s) and you received double the normal amount.");
            killed.sendMessage("You've been paid extra ❇" + moneyKilled + " for being slain by a killer with doubleMoney perk inside an arena, worth " + emeraldsKilled + " emerald(s)");
        }

        ///////////////////////
        // Killer and Killed have been arena paid
        ///////////////////////
    }
}
