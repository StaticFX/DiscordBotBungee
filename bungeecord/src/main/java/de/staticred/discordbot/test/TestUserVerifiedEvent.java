package de.staticred.discordbot.test;

import de.staticred.discordbot.api.BotEvent;
import de.staticred.discordbot.api.Listener;
import de.staticred.discordbot.api.event.UserVerifiedEvent;

public class TestUserVerifiedEvent implements Listener {

    @BotEvent
    public void onUserVerified(UserVerifiedEvent event) {
    }

}
