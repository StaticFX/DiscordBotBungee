package de.staticred.discordbot.event;

import de.staticred.discordbot.api.Event;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UserVerifiedEvent extends Event {

    private Member member;
    private ProxiedPlayer player;
    private TextChannel textChannel;
    private boolean canceled;

    public UserVerifiedEvent(Member member, ProxiedPlayer player, TextChannel textChannel) {
        this.member = member;
        this.player = player;
        this.textChannel = textChannel;
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

    public TextChannel getTextChannel() {
        return textChannel;
    }
}
