package me.leoko.advancedban.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.leoko.advancedban.Bukkit.event.PunishmentEvent;
import me.leoko.advancedban.Bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.Bukkit.listener.CommandReceiver;
import me.leoko.advancedban.Common.MethodInterface;
import me.leoko.advancedban.Common.Universal;
import me.leoko.advancedban.Common.manager.PunishmentManager;
import me.leoko.advancedban.Common.manager.UUIDManager;
import me.leoko.advancedban.Common.utils.Punishment;


/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public class BukkitMethods implements MethodInterface {

    private final File messageFile = new File(getDataFolder(), "Messages.yml");
    private final File layoutFile = new File(getDataFolder(), "Layouts.yml");
    private File configFile = new File(getDataFolder(), "config.yml");
    private File mysqlFile = new File(getDataFolder(), "MySQL.yml");
    private YamlConfiguration config;
    private YamlConfiguration messages;
    private YamlConfiguration layouts;
    private YamlConfiguration mysql;
    
    private boolean isProxy;
    
    public BukkitMethods(boolean isProxy) {
    	this.isProxy = isProxy;
    }

    @Override
    public void loadFiles() {
        if (!configFile.exists()) {
            ((JavaPlugin) getPlugin()).saveResource("config.yml", true);
        }
        if (!messageFile.exists()) {
            ((JavaPlugin) getPlugin()).saveResource("Messages.yml", true);
        }
        if (!layoutFile.exists()) {
            ((JavaPlugin) getPlugin()).saveResource("Layouts.yml", true);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messageFile);
        layouts = YamlConfiguration.loadConfiguration(layoutFile);

        if (!config.contains("UUID-Fetcher")) {
            //noinspection ResultOfMethodCallIgnored
            configFile.renameTo(new File(getDataFolder(), "oldConfig.yml"));
            configFile = new File(getDataFolder(), "config.yml");
            ((JavaPlugin) getPlugin()).saveResource("config.yml", true);
            config = YamlConfiguration.loadConfiguration(configFile);
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
        return ((JavaPlugin) getPlugin()).getDescription().getVersion();
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
        return BukkitMain.get();
    }

    @Override
    public File getDataFolder() {
        return ((JavaPlugin) getPlugin()).getDataFolder();
    }

    @Override
    public void setCommandExecutor(String cmd) {
        PluginCommand command = Bukkit.getPluginCommand(cmd);
        if (command != null) {
            command.setExecutor(CommandReceiver.get());
        } else {
            System.out.println("AdvancedBan >> Failed to register command " + cmd);
        }
    }

    @Override
    public void sendMessage(Object player, String msg) {
        ((CommandSender) player).sendMessage(msg);
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        return ((CommandSender) player).hasPermission(perms);
    }

    @SuppressWarnings("deprecation")
	@Override
    public boolean isOnline(String name) {
        return Bukkit.getOfflinePlayer(name).isOnline();
    }

    @Override
    public Object getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }

    @Override
    public void kickPlayer(String player, String reason) {
        if (Bukkit.getPlayer(player) != null && Bukkit.getPlayer(player).isOnline()) {
            Bukkit.getPlayer(player).kickPlayer(reason);
        }
    }

    @Override
    public Object[] getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().toArray();
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        Bukkit.getScheduler().runTaskTimerAsynchronously((JavaPlugin) getPlugin(), rn, l1, l2);
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        Bukkit.getScheduler().runTaskLaterAsynchronously((JavaPlugin) getPlugin(), rn, l1);
    }

    @Override
    public void runAsync(Runnable rn) {
        Bukkit.getScheduler().runTaskAsynchronously((JavaPlugin) getPlugin(), rn);
    }

    @Override
    public void runSync(Runnable rn) {
        Bukkit.getScheduler().runTask((JavaPlugin) getPlugin(), rn);
    }

    @Override
    public void executeCommand(String cmd) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    @Override
    public String getName(Object player) {
        return ((CommandSender) player).getName();
    }

    @Override
    public String getName(String uuid) {
        return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
    }

    @Override
    public String getIP(Object player) {
        return ((Player) player).getAddress().getHostName();
    }

    @Override
    public String getInternUUID(Object player) {
        return player instanceof OfflinePlayer ? ((OfflinePlayer) player).getUniqueId().toString().replaceAll("-", "") : "none";
    }

    @SuppressWarnings("deprecation")
	@Override
    public String getInternUUID(String player) {
        return Bukkit.getOfflinePlayer(player).getUniqueId().toString().replaceAll("-", "");
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
        if (Universal.get().isMuteCommand(cmd.split(" ")[0].substring(1)) && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))) != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public void loadMySQLFile(File f) {
        mysql = YamlConfiguration.loadConfiguration(f);
    }

    @Override
    public void createMySQLFile(File f) {
    	mysqlFile = f;
    	
        mysql.set("MySQL.IP", "localhost");
        mysql.set("MySQL.DB-Name", "YourDatabase");
        mysql.set("MySQL.Username", "root");
        mysql.set("MySQL.Password", "pw123");
        mysql.set("MySQL.Port", 3306);
        try {
            mysql.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getMySQLFile() {
        return mysqlFile;
    }
    
    @Override
	public Object getMysql() {
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
        return ((YamlConfiguration) file).getBoolean(path);
    }

    @Override
    public String getString(Object file, String path) {
        return ((YamlConfiguration) file).getString(path);
    }

    @Override
    public Long getLong(Object file, String path) {
        return ((YamlConfiguration) file).getLong(path);
    }

    @Override
    public Integer getInteger(Object file, String path) {
        return ((YamlConfiguration) file).getInt(path);
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        return ((YamlConfiguration) file).getStringList(path);
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        return ((YamlConfiguration) file).getBoolean(path, def);
    }

    @Override
    public String getString(Object file, String path, String def) {
        return ((YamlConfiguration) file).getString(path, def);
    }

    @Override
    public long getLong(Object file, String path, long def) {
        return ((YamlConfiguration) file).getLong(path, def);
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        return ((YamlConfiguration) file).getInt(path, def);
    }

    @Override
    public boolean contains(Object file, String path) {
        return ((YamlConfiguration) file).contains(path);
    }

    @Override
    public String getFileName(Object file) {
        return ((YamlConfiguration) file).getName();
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        Bukkit.getPluginManager().callEvent(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        Bukkit.getPluginManager().callEvent(new RevokePunishmentEvent(punishment, massClear));
    }

    @Override
    public boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (hasPerms(p, perm)) {
                for (String str : notification) {
                    sendMessage(p, str);
                }
            }
        }
    }

    @Override
    public void log(String msg) {
        BukkitMain.get().getLogger().info(msg.replaceAll("&", "§"));
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
		return BukkitMain.get().getServer().getName();
	}
}