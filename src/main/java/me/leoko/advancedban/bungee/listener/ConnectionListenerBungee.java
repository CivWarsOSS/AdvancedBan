package me.leoko.advancedban.bungee.listener;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.BungeeMain;
import me.leoko.advancedban.manager.PunishmentManager;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class ConnectionListenerBungee implements Listener {

    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW)
    public void onConnection(LoginEvent event) {
        event.registerIntent((BungeeMain)Universal.get().getMethods().getPlugin());
        Universal.get().getMethods().runAsync(() -> {
            String result = Universal.get().callConnection(event.getConnection().getName(), event.getConnection().getAddress().getAddress().getHostAddress());
            if (result != null) {
                event.setCancelled(true);
                event.setCancelReason(result);
            }
            if (Universal.get().useRedis()) {
                RedisBungee.getApi().sendChannelMessage("AdvancedBanConnection", event.getConnection().getName() + "," + event.getConnection().getAddress().getAddress().getHostAddress());
            }
            event.completeIntent((BungeeMain)Universal.get().getMethods().getPlugin());
        });
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        Universal.get().getMethods().runAsync(() -> {
            if (event.getPlayer() != null) {
                PunishmentManager.get().discard(event.getPlayer().getName());
            }
        });
    }
}