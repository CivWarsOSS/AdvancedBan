package me.leoko.advancedban.Velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;

import me.leoko.advancedban.Common.Universal;

/**
 * Created by ironboundred on 08.07.2019.
 */
public class ChatListenerVelocity {
	@Subscribe(order = PostOrder.FIRST)
	public void playerChat(PlayerChatEvent event) {
		 if (Universal.get().getMethods().callChat(event.getPlayer())) {
             event.setResult(ChatResult.denied());
         }
	}
}
