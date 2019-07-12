package me.leoko.advancedban.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;

import me.leoko.advancedban.Universal;
import net.kyori.text.TextComponent;

/**
 * Created by ironboundred on 08.07.2019.
 */
public class ConnectionListenerVelocity {
	@Subscribe
	public void playerConnect(PostLoginEvent event) {
		String result = Universal.get().callConnection(event.getPlayer().getUsername(), event.getPlayer().getRemoteAddress().getHostString());
        if (result != null) {
       	 event.getPlayer().disconnect(TextComponent.builder(result).build());
        }
	}
}
