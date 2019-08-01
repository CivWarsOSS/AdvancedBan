package me.leoko.advancedban.Common.manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.leoko.advancedban.Common.MethodInterface;
import me.leoko.advancedban.Common.Universal;
import me.leoko.advancedban.Common.utils.InterimData;
import me.leoko.advancedban.Common.utils.Punishment;
import me.leoko.advancedban.Common.utils.PunishmentType;
import me.leoko.advancedban.Common.utils.SQLQuery;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public class PunishmentManager {

	private static PunishmentManager instance = null;
	private final Universal universal = Universal.get();
	private final Set<Punishment> punishments = Collections.synchronizedSet(new HashSet<>());
	private final Set<Punishment> history = Collections.synchronizedSet(new HashSet<>());
	private final Set<String> cached = Collections.synchronizedSet(new HashSet<>());

	public static PunishmentManager get() {
		return instance == null ? instance = new PunishmentManager() : instance;
	}

	public void setup() {
		MethodInterface mi = universal.getMethods();
		DatabaseManager.get().executeStatement(SQLQuery.DELETE_OLD_PUNISHMENTS, TimeManager.getTime());
		for (Object player : mi.getOnlinePlayers()) {
			String name = mi.getName(player).toLowerCase();
			load(name, UUIDManager.get().getUUID(name), mi.getIP(player));
		}
	}

	public InterimData load(String name, String uuid, String ip) {
		Set<Punishment> punishments = new HashSet<>(DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_WITH_IP, uuid,
				ip));
		Set<Punishment> history = new HashSet<>(DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP, uuid,
				ip));
		
		return new InterimData(uuid, name, ip, punishments, history);
	}

	public void discard(String name) {
		name = name.toLowerCase();
		String ip = Universal.get().getIps().get(name);
		String uuid = UUIDManager.get().getUUID(name);
		cached.remove(name);
		cached.remove(uuid);
		cached.remove(ip);

		Iterator<Punishment> iterator = punishments.iterator();
		while (iterator.hasNext()) {
			Punishment punishment = iterator.next();
			if (punishment.getUuid().equals(uuid) || punishment.getUuid().equals(ip)) {
				iterator.remove();
			}
		}

		iterator = history.iterator();
		while (iterator.hasNext()) {
			Punishment punishment = iterator.next();
			if (punishment.getUuid().equals(uuid) || punishment.getUuid().equals(ip)) {
				iterator.remove();
			}
		}
	}

	public List<Punishment> getPunishments(String uuid, PunishmentType put, boolean current) {
		List<Punishment> ptList = new ArrayList<>();

		if (isCached(uuid)) {
			for (Iterator<Punishment> iterator = (current ? punishments : history).iterator(); iterator.hasNext();) {
				Punishment pt = iterator.next();
				if ((put == null || put == pt.getType().getBasic()) && pt.getUuid().equals(uuid)) {
					if (!current || !pt.isExpired()) {
						ptList.add(pt);
					} else {
						pt.delete(null, false, false);
						iterator.remove();
					}
				}
			}
		} else {
			ptList = new ArrayList<>(DatabaseManager.get().executeResultStatement(
					current ? SQLQuery.SELECT_USER_PUNISHMENTS : SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY, uuid));
		}
		return ptList;
	}

	public List<Punishment> getPunishments(SQLQuery sqlQuery, Object... parameters) {
		return new ArrayList<>(DatabaseManager.get().executeResultStatement(sqlQuery, parameters));
	}

	public Punishment getPunishment(int id) {
		Set<Punishment> set = new HashSet<>(DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_PUNISHMENT_BY_ID, id));
		Punishment pt = null;
		if(!set.isEmpty()) {
			pt = set.iterator().next();
		}
		return pt == null || pt.isExpired() ? null : pt;
	}

	public Punishment getWarn(int id) {
		Punishment punishment = getPunishment(id);
		return punishment.getType().getBasic() == PunishmentType.WARNING ? punishment : null;
	}

	public List<Punishment> getWarns(String uuid) {
		return getPunishments(uuid, PunishmentType.WARNING, true);
	}

	public Punishment getBan(String uuid) {
		List<Punishment> punishments = getPunishments(uuid, PunishmentType.BAN, true);
		return punishments.isEmpty() ? null : punishments.get(0);
	}

	public Punishment getMute(String uuid) {
		List<Punishment> punishments = getPunishments(uuid, PunishmentType.MUTE, true);
		return punishments.isEmpty() ? null : punishments.get(0);
	}

	public boolean isBanned(String uuid) {
		return getBan(uuid) != null;
	}

	public boolean isMuted(String uuid) {
		return getMute(uuid) != null;
	}

	public boolean isCached(String name) {
		return cached.contains(name);
	}

	public void addCached(String name) {
		cached.add(name);
	}

	public int getCalculationLevel(String uuid, String layout) {
		if (isCached(uuid)) {
			return (int) history.stream()
					.filter(pt -> pt.getUuid().equals(uuid) && layout.equalsIgnoreCase(pt.getCalculation())).count();
		} else {
			return (int) new HashSet<>(DatabaseManager.get()
					.executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION, uuid, layout)).stream()
					.filter(pt -> pt.getUuid().equals(uuid) && layout.equalsIgnoreCase(pt.getCalculation())).count();
		}
	}

	public int getCurrentWarns(String uuid) {
		return getWarns(uuid).size();
	}

	public Set<Punishment> getLoadedPunishments(boolean checkExpired) {
		if (checkExpired) {
			List<Punishment> toDelete = new ArrayList<>();
			for (Punishment pu : punishments) {
				if (pu.isExpired()) {
					toDelete.add(pu);
				}
			}
			for (Punishment pu : toDelete) {
				pu.delete();
			}
		}
		return punishments;
	}

	public Set<Punishment> getLoadedHistory() {
		return history;
	}
}