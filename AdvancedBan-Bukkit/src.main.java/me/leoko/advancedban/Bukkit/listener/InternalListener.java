package me.leoko.advancedban.Bukkit.listener;

import java.util.Date;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.leoko.advancedban.Bukkit.event.PunishmentEvent;
import me.leoko.advancedban.Bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.Common.utils.PunishmentType;

/**
 *
 * @author Beelzebu
 */
public class InternalListener implements Listener {
    
    @EventHandler
    public void onPunish(PunishmentEvent e) {
        BanList banlist;
        if (e.getPunishment().getType().equals(PunishmentType.BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.NAME);
            banlist.addBan(e.getPunishment().getName(), e.getPunishment().getReason(), new Date(e.getPunishment().getEnd()), e.getPunishment().getOperator());
        } else if (e.getPunishment().getType().equals(PunishmentType.IP_BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_IP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.IP);
            banlist.addBan(e.getPunishment().getName(), e.getPunishment().getReason(), new Date(e.getPunishment().getEnd()), e.getPunishment().getOperator());
        }
    }
    
    @EventHandler
    public void onRevokePunishment(RevokePunishmentEvent e) {
        BanList banlist;
        if (e.getPunishment().getType().equals(PunishmentType.BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.NAME);
            banlist.pardon(e.getPunishment().getName());
        } else if (e.getPunishment().getType().equals(PunishmentType.IP_BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_IP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.IP);
            banlist.pardon(e.getPunishment().getName());
        }
    }
}