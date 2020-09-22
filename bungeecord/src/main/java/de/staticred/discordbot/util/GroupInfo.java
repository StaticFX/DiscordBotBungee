package de.staticred.discordbot.util;

public class GroupInfo {

    private boolean dynamic;
    private String name;
    private String permission;
    private String prefix;
    private String discordGroup;

    public GroupInfo(boolean dynamic, String name, String permission, String prefix, String discordGroup) {
        this.dynamic = dynamic;
        this.name = name;
        this.permission = permission;
        this.prefix = prefix;
        this.discordGroup = discordGroup;
    }

    public String getDiscordGroup() {
        return discordGroup;
    }

    public void setDiscordGroup(String discordGroup) {
        this.discordGroup = discordGroup;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
