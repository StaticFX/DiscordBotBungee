package de.staticred.discordbot.api;

import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.DiscordFileManager;

public class VerifyAPI {

    public static VerifyAPI instance;


    public static VerifyAPI getInstance() {
        return instance;
    }

    public void registerEvent(Listener listener) {
        EventManager.instance.registerEvent(listener);
    }

    public VerifyDAO getDataAccess() {
        return VerifyDAO.INSTANCE;
    }

    public DiscordFileManager getGroupManager() {
        return DiscordFileManager.INSTANCE;
    }

}
