package de.staticred.discordbot.event;

import de.staticred.discordbot.api.Event;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class UserUpdatedRolesEvent extends Event {

    private Member member;
    private ProxiedPlayer player;
    private List<String> newRanks;

    public UserUpdatedRolesEvent(Member member, ProxiedPlayer player, List<String> newRanks) {
        this.member = member;
        this.player = player;
        this.newRanks = newRanks;
    }

    @Override
    public Class<?> getEventSubClass() {
        return this.getClass();
    }

    public Member getMember() {
        return member;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public List<String> getNewRanks() {
        return newRanks;
    }
}
