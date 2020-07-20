package de.staticred.discordbot.test;

import de.staticred.discordbot.api.BotEvent;
import de.staticred.discordbot.api.Listener;
import de.staticred.discordbot.event.UserVerifiedEvent;
import de.staticred.discordbot.util.Debugger;

public class TestUserVerifiedEvent implements Listener {

    @BotEvent
    public void onUserVerified(UserVerifiedEvent event) {
    }

}
