package de.staticred.discordbot.event;

import de.staticred.discordbot.api.Event;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UserUnverifiedEvent extends Event {
    private Member member;
    private ProxiedPlayer player;

    public UserUnverifiedEvent(Member member, ProxiedPlayer player) {
        this.member = member;
        this.player = player;
    }

    public Member getMember() {
        return member;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }
}
