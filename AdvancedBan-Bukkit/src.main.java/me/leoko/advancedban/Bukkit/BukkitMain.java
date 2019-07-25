package me.leoko.advancedban.Bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.leoko.advancedban.Bukkit.listener.ChatListener;
import me.leoko.advancedban.Bukkit.listener.CommandListener;
import me.leoko.advancedban.Bukkit.listener.ConnectionListener;
import me.leoko.advancedban.Bukkit.listener.InternalListener;
import me.leoko.advancedban.Common.Universal;


public class BukkitMain extends JavaPlugin {
    private static BukkitMain instance;

    public static BukkitMain get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Universal.get().setup(new BukkitMethods(false));

        ConnectionListener connListener = new ConnectionListener();
        this.getServer().getPluginManager().registerEvents(connListener, this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new CommandListener(), this);
        this.getServer().getPluginManager().registerEvents(new InternalListener(), this);

        for (Player op : Bukkit.getOnlinePlayers()) {
            AsyncPlayerPreLoginEvent apple = new AsyncPlayerPreLoginEvent(op.getName(), op.getAddress().getAddress(), op.getUniqueId());
            connListener.onConnect(apple);
            if (apple.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_BANNED) {
                op.kickPlayer(apple.getKickMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        Universal.get().shutdown();
    }
}