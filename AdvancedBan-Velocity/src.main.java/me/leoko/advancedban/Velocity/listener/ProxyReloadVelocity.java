package me.leoko.advancedban.Velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.Velocity.VelocityMain;

public class ProxyReloadVelocity {
	@Subscribe
	public void proxyReload(ProxyReloadEvent event) {
		Universal.get().getMethods().loadFiles();
		VelocityMain.get().getLogger().info("AdvancedBan reloaded!");
	}
}
