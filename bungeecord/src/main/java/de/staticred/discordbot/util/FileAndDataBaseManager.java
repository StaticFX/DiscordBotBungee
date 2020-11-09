package de.staticred.discordbot.util;

import de.staticred.discordbot.files.*;

public class FileAndDataBaseManager {

    public static void loadAllFilesAndDatabases() {

        ConfigFileManager.INSTANCE.loadFile();

        VerifyFileManager.INSTANCE.loadFile();

        MessagesFileManager.INSTANCE.loadFile();

        DiscordFileManager.INSTANCE.loadFile();

        RewardsFileManager.INSTANCE.loadFile();

        BlockedServerFileManager.INSTANCE.loadFile();

        DiscordMessageFileManager.INSTANCE.loadFile();

        SettingsFileManager.INSTANCE.loadFile();

        DiscordFileManager.INSTANCE.update();

        AliasesFileManager.INSTANCE.loadFile();
    }

}
