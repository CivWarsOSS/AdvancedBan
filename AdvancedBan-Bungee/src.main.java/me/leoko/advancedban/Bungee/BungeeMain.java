package me.leoko.advancedban.Bungee;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;

import me.leoko.advancedban.Bungee.listener.ChatListenerBungee;
import me.leoko.advancedban.Bungee.listener.ConnectionListenerBungee;
import me.leoko.advancedban.Bungee.listener.InternalListener;
import me.leoko.advancedban.Bungee.listener.PubSubMessageListener;
import me.leoko.advancedban.Common.Universal;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {
    private static BungeeMain instance;

    public static BungeeMain get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
                
        Universal.get().setup(new BungeeMethods(true));
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ConnectionListenerBungee());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListenerBungee());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new InternalListener());
        ProxyServer.getInstance().registerChannel("AdvancedBan");
        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null) {
            Universal.get().useRedis(true);
            ProxyServer.getInstance().getPluginManager().registerListener(this, new PubSubMessageListener());
            RedisBungee.getApi().registerPubSubChannels("AdvancedBan", "AdvancedBanConnection");
            Universal.get().log("RedisBungee detected, hooking into it!");
        }
    }

    @Override
    public void onDisable() {
        Universal.get().shutdown();
    }
}