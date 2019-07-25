package me.leoko.advancedban.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import me.leoko.advancedban.Common.MethodInterface;
import me.leoko.advancedban.Common.Universal;
import me.leoko.advancedban.Common.manager.PunishmentManager;
import me.leoko.advancedban.Common.manager.UUIDManager;
import me.leoko.advancedban.Common.utils.Punishment;
import me.leoko.advancedban.Velocity.command.CommandReceiverVelocity;
import me.leoko.advancedban.Velocity.event.PunishmentEvent;
import me.leoko.advancedban.Velocity.event.RevokePunishmentEvent;
import net.kyori.text.TextComponent;

/**
 * Created by ironboundred on 08.07.2019.
 */
public class VelocityMethods implements MethodInterface {
	private final File messageFile = new File(VelocityMain.get().getDataFolder().toString(), "Messages.toml");
	private final File layoutFile  = new File(VelocityMain.get().getDataFolder().toString(), "Layouts.toml");
	private final File configFile = new File(VelocityMain.get().getDataFolder().toString(), "config.toml");
	private final File mysqlFile = new File(VelocityMain.get().getDataFolder().toString(), "mysql.toml");
	
	private Toml config;
	private Toml messages;
	private Toml layouts;
	private Toml mysql;
	
    private boolean isProxy;

    public VelocityMethods(boolean isProxy) {
    	this.isProxy = isProxy;
    }
    
	@Override
	public void loadFiles() {
		try {
			File folder = VelocityMain.get().getDataFolder().toFile();
			
			if(!folder.exists()) {
				folder.mkdir();
			}
			
			if(!configFile.exists()) {
	            try (InputStream input = getClass().getResourceAsStream("/" + configFile.getName())) {
	                if (input != null) {
	                    Files.copy(input, configFile.toPath());
	                } else {
	                	configFile.createNewFile();
	                }
	            } catch (IOException exception) {
	                exception.printStackTrace();
	            }
			}
			
			if(!layoutFile.exists()) {
	            try (InputStream input = getClass().getResourceAsStream("/" + layoutFile.getName())) {
	                if (input != null) {
	                    Files.copy(input, layoutFile.toPath());
	                } else {
	                	layoutFile.createNewFile();
	                }
	            } catch (IOException exception) {
	                exception.printStackTrace();
	            }
			}
			
			if(!messageFile.exists()) {
	            try (InputStream input = getClass().getResourceAsStream("/" + messageFile.getName())) {
	                if (input != null) {
	                    Files.copy(input, messageFile.toPath());
	                } else {
	                	messageFile.createNewFile();
	                }
	            } catch (IOException exception) {
	                exception.printStackTrace();
	            }
			}
			
			config = new Toml().read(configFile);
			messages = new Toml().read(messageFile);
			layouts = new Toml().read(layoutFile);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFromUrlJson(String url, String key) {
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonObject json = (JsonObject) jp.parse(new InputStreamReader(request.getInputStream()));

            String[] keys = key.split("\\|");
            for (int i = 0; i < keys.length - 1; i++) {
                json = json.getAsJsonObject(keys[i]);
            }

            return json.get(keys[keys.length - 1]).toString().replaceAll("\"", "");

        } catch (Exception exc) {
            return null;
        }
	}

	@Override
	public String getVersion() {
		return VelocityMain.get().getServer().getPluginManager().fromInstance(VelocityMain.get()).get().getDescription().getVersion().orElse("");
	}

	@Override
	public Object getConfig() {
		return config;
	}

	@Override
	public Object getMessages() {
		return messages;
	}

	@Override
	public Object getLayouts() {
		return layouts;
	}
	
	@Override
	public Object getPlugin() {
		return VelocityMain.get();
	}

	@Override
	public File getDataFolder() {
		return VelocityMain.get().getDataFolder().toFile();
	}

	@Override
	public void setCommandExecutor(String cmd) {
		VelocityMain.get().getServer().getCommandManager().register(new CommandReceiverVelocity(cmd), cmd);
		
	}

	@Override
	public void sendMessage(Object player, String msg) {
		((CommandSource)player).sendMessage(TextComponent.builder(msg).build());
	}

	@Override
	public String getName(Object player) {
		return player instanceof Player ? ((Player)player).getUsername() : "Console";
	}

	@Override
	public String getName(String uuid) {
		return VelocityMain.get().getServer().getPlayer(UUID.fromString(uuid)).get().getUsername();
	}

	@Override
	public String getIP(Object player) {
		return ((Player)player).getRemoteAddress().getHostName();
	}

	@Override
	public String getInternUUID(Object player) {
		return player instanceof Player ? ((Player)player).getUniqueId().toString().replaceAll("-", "") : "none";
	}

	@Override
	public String getInternUUID(String player) {
		if(VelocityMain.get().getServer().getPlayer(player).isPresent()) {
			Player p = VelocityMain.get().getServer().getPlayer(player).get();
			
			UUID uuid = p.getUniqueId();
			return uuid == null ? null : uuid.toString().replaceAll("-", "");
		}else {
			return null;
		}
	}

	@Override
	public boolean hasPerms(Object player, String perms) {
		return ((CommandSource)player).hasPermission(perms);
	}

	@Override
	public boolean isOnline(String name) {
		return VelocityMain.get().getServer().getPlayer(name).isPresent();
	}

	@Override
	public Object getPlayer(String name) {
		return VelocityMain.get().getServer().getPlayer(name).orElseGet(null);
	}

	@Override
	public void kickPlayer(String player, String reason) {
		VelocityMain.get().getServer().getPlayer(player).get().disconnect(TextComponent.builder(reason).build());
	}

	@Override
	public Object[] getOnlinePlayers() {
		return VelocityMain.get().getServer().getAllPlayers().toArray();
	}

	@Override
	public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
		VelocityMain.get().getServer().getScheduler().buildTask(
				VelocityMain.get(), rn).repeat(l1, TimeUnit.MILLISECONDS).delay(l2, TimeUnit.MILLISECONDS).schedule();
	}

	@Override
	public void scheduleAsync(Runnable rn, long l1) {
		VelocityMain.get().getServer().getScheduler().buildTask(
				VelocityMain.get(), rn).delay(l1, TimeUnit.MILLISECONDS).schedule();
	}

	@Override
	public void runAsync(Runnable rn) {
		VelocityMain.get().getServer().getScheduler().buildTask(
				VelocityMain.get(), rn).schedule();
	}

	@Override
	public void runSync(Runnable rn) {
		VelocityMain.get().getServer().getScheduler().buildTask(
				VelocityMain.get(), rn).schedule();
	}

	@Override
	public void executeCommand(String cmd) {
		VelocityMain.get().getServer().getCommandManager().execute(VelocityMain.get().getServer().getConsoleCommandSource(), cmd);
	}

	@Override
	public boolean callChat(Object player) {
        Punishment pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)));
        if (pnt != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
	}

	@Override
	public boolean callCMD(Object player, String cmd) {
        Punishment pnt;
        if (Universal.get().isMuteCommand(cmd.split(" ")[0].substring(1))
                && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))) != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
	}

	@Override
	public void loadMySQLFile(File f) {
		mysql = new Toml().read(f);
	}

	@Override
	public void createMySQLFile(File f) {
		if(!mysqlFile.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/" + mysqlFile.getName())) {
                if (input != null) {
                    Files.copy(input, mysqlFile.toPath());
                } else {
                	mysqlFile.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
		}
	}

	@Override
	public Object getMySQLFile() {
		return mysql;
	}

	@Override
	public String parseJSON(InputStreamReader json, String key) {
        JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonNull) {
            return null;
        }
        JsonElement obj = ((JsonObject) element).get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
	}

	@Override
	public String parseJSON(String json, String key) {
        JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonNull) {
            return null;
        }
        JsonElement obj = ((JsonObject) element).get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
	}

	@Override
	public Boolean getBoolean(Object file, String path) {
		return ((Toml)file).getBoolean(path);
	}

	@Override
	public String getString(Object file, String path) {
		return ((Toml)file).getString(path);
	}

	@Override
	public Long getLong(Object file, String path) {
		return ((Toml)file).getLong(path);
	}

	@Override
	public Integer getInteger(Object file, String path) {
		return Integer.parseInt(((Toml)file).getString(path));
	}

	@Override
	public List<String> getStringList(Object file, String path) {
		return ((Toml)file).getList(path);
	}

	@Override
	public boolean getBoolean(Object file, String path, boolean def) {
		return ((Toml)file).getBoolean(path, def);
	}

	@Override
	public String getString(Object file, String path, String def) {
		return ((Toml)file).getString(path, def);
	}

	@Override
	public long getLong(Object file, String path, long def) {
		return ((Toml)file).getLong(path, def);
	}

	@Override
	public int getInteger(Object file, String path, int def) {
		return Integer.parseInt(((Toml)file).getString(path, String.valueOf(def)));
	}

	@Override
	public boolean contains(Object file, String path) {
		return ((Toml)file).contains(path);
	}

	@Override
	public String getFileName(Object file) {
		return "[Only available on Bukkit-Version!]";
	}

	@Override
	public void callPunishmentEvent(Punishment punishment) {
		VelocityMain.get().getServer().getEventManager().fire(new PunishmentEvent(punishment));
	}

	@Override
	public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
		VelocityMain.get().getServer().getEventManager().fire(new RevokePunishmentEvent(punishment, massClear));
	}

	@Override
	public boolean isOnlineMode() {
		return VelocityMain.get().getServer().getConfiguration().isOnlineMode();
	}

	@Override
	public void notify(String perm, List<String> notification) {
		VelocityMain.get().getServer().getAllPlayers().stream().filter((pp) -> (Universal.get().hasPerms(pp, perm))).forEachOrdered((pp) -> {
			notification.forEach((str) -> {
				sendMessage(pp, str);
			});
		});
	}

	@Override
	public void log(String msg) {
		VelocityMain.get().getLogger().info(msg.replaceAll("&", "§"));
	}

	@Override
	public boolean isUnitTesting() {
		return false;
	}

	@Override
	public File getConfigFile() {
		return configFile;
	}

	@Override
	public File getMessagesFile() {
		return messageFile;
	}

	@Override
	public File getLayoutsFile() {
		return layoutFile;
	}
	
	
	@Override
	public boolean isProxy() {
		return isProxy;
	}

	@Override
	public String getServerType() {
		return VelocityMain.get().getServer().getVersion().getName();
	}
}
