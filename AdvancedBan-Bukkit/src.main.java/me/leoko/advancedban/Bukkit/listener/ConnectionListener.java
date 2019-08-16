package me.leoko.advancedban.Bukkit.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;


/**
 * Created by Leoko @ dev.skamps.eu on 16.07.2016.
 */
public class ConnectionListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        String result = Universal.get().callConnection(event.getName(), event.getAddress().getHostAddress());
        if (result != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, result);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        PunishmentManager.get().discard(event.getPlayer().getName());
    }
}