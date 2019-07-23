package me.leoko.advancedban.Velocity.event;

import me.leoko.advancedban.Common.utils.Punishment;

public class PunishmentEvent {
    private final Punishment punishment;

    public PunishmentEvent(Punishment punishment) {
        this.punishment = punishment;
    }

    /**
     * Returns the punishment involved in this event
     *
     * @return Punishment
     */
    public Punishment getPunishment() {
        return punishment;
    }
}
