package me.leoko.advancedban.Velocity;

import java.nio.file.Path;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import me.leoko.advancedban.Common.Universal;
import me.leoko.advancedban.Velocity.listener.ChatListenerVelocity;
import me.leoko.advancedban.Velocity.listener.ConnectionListenerVelocity;



/**
 * Created by ironboundred on 08.07.2019.
 */
@Plugin(id = "advancedban", name = "AdvancedBan",
	description = "All-In-One Punishment-System", version = "3.0.9",
	authors = {"Leoko", "ironboundred"})
public class VelocityMain {
	private static VelocityMain instance;
	private final ProxyServer server;
	private final Logger logger;
	
	@Inject
	@DataDirectory
	private Path dir;
	
	@Inject
	public VelocityMain(ProxyServer server, Logger logger) {
		instance = this;
		this.server = server;
		this.logger = logger;
	}
	
	@Subscribe
	public void onEnable(ProxyInitializeEvent event) {
		Universal.get().setup(new VelocityMethods(true));
		
		server.getEventManager().register(instance, new ChatListenerVelocity());
		server.getEventManager().register(instance, new ConnectionListenerVelocity());
	}
	
	@Subscribe
	public void onShutDown(ProxyShutdownEvent event) {
		Universal.get().shutdown();
	}
	
	public static VelocityMain get() {
		return instance;
	}
	
	public ProxyServer getServer() {
		return server;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public Path getDataFolder() {
		return dir;
	}
}
