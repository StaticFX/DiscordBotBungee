package de.staticred.discordbot.event;

import de.staticred.discordbot.api.Event;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UserUnverifiedEvent extends Event {
    private Member member;
    private ProxiedPlayer player;
    private boolean canceled;

    public UserUnverifiedEvent(Member member, ProxiedPlayer player) {
        this.member = member;
        this.player = player;
        canceled = false;
    }

    @Override
    public Class<?> getEventSubClass() {
        return this.getClass();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public Member getMember() {
        return member;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }
}
