package de.staticred.discordbot.event;

import de.staticred.discordbot.api.Event;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UserClickedMessageEvent extends Event {

    private ProxiedPlayer player;
    private boolean accepted;
    private boolean canceled;

    public UserClickedMessageEvent(ProxiedPlayer player, boolean accepted) {
        this.accepted = accepted;
        this.player = player;
        canceled = false;
    }

    @Override
    public Class<?> getEventSubClass() {
        return this.getClass();
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

}
