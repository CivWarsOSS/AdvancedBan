package me.leoko.advancedban.Velocity.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import me.leoko.advancedban.Common.manager.CommandManager;
import me.leoko.advancedban.Velocity.VelocityMain;


/**
 * Created by ironboundred on 08.07.2019.
 */
public class CommandReceiverVelocity implements Command {
	private String name;
	
	public CommandReceiverVelocity(String name) {
		this.name = name;
	}

	@Override
	public void execute(CommandSource sender, String @NonNull [] args) {
        CommandManager.get().onCommand(sender, this.name, args);
	}
	
	@Override
	public List<String> suggest(@NonNull CommandSource source, String[] currentArgs) {
		List<String> names = new ArrayList<>();
		
		for(Player p: VelocityMain.get().getServer().getAllPlayers()) {
			names.add(p.getUsername());
		}
		
	    if (currentArgs.length == 0) {
	        return names;
	    } else if (currentArgs.length == 1) {
	        return names.stream()
	                .filter(name -> name.regionMatches(true, 0, currentArgs[0], 0, currentArgs[0].length()))
	                .collect(Collectors.toList());
	    } else {
	        return ImmutableList.of();
	    }
	}
}
