package de.staticred.discordbot.util;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.api.EventManager;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.event.UserUpdatedRolesEvent;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordFileManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemberManager {

    public static Member getMemberFromPlayer(UUID uuid) throws SQLException {
        User u;

        String id = VerifyDAO.INSTANCE.getDiscordID(uuid);

        u = DBVerifier.getInstance().jda.retrieveUserById(id).complete();
        Member m = null;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage(id);

        if (!DBVerifier.getInstance().jda.getGuilds().isEmpty()) {
            for (Guild guild : DBVerifier.getInstance().jda.getGuilds()) {
                if (u != null)
                    m = guild.retrieveMember(u).complete();
            }
        } else {
            throw new SQLException("There was an internal error! The member of the player can´t be found. Please contact the developer of this plugin.") ;
        }
        return m;
    }

    public static void updateRoles(Member m, ProxiedPlayer p) {
        List<Member> addedNonDynamicGroups = new ArrayList<>();
        List<String> roles = new ArrayList<>();


        Debugger.debugMessage("Updating groups for player: " + p.getName() + " Member: " + m.getEffectiveName());

        //when the admin sets a verify role the member will get the role
        Debugger.debugMessage("Checking if verify role is empty");

        if(ConfigFileManager.INSTANCE.hasVerifyRole()) {
            Debugger.debugMessage("Role is not empty");

            try {
                Debugger.debugMessage("Checking if tokens are used");

                if(ConfigFileManager.INSTANCE.useTokens()) {
                    Debugger.debugMessage("Tokens are used");
                    Debugger.debugMessage("Token found for group: verify - " + ConfigFileManager.INSTANCE.getVerifyRole());
                    Debugger.debugMessage("Trying to give role to the player");
                    Debugger.debugMessage("Is role null? " + (m.getGuild().getRoleById((ConfigFileManager.INSTANCE.getVerifyRole())) == null));

                    m.getGuild().addRoleToMember(m,m.getGuild().getRoleById((ConfigFileManager.INSTANCE.getVerifyRole()))).queue();
                }else{
                    Debugger.debugMessage("Tokens are not used");
                    Debugger.debugMessage("Name found for group: verify - " + ConfigFileManager.INSTANCE.getVerifyRole());
                    Debugger.debugMessage("Trying to give role to the player");
                    Debugger.debugMessage("Is role null? " + (m.getGuild().getRolesByName(ConfigFileManager.INSTANCE.getVerifyRole(),true).get(0) == null));

                    m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(ConfigFileManager.INSTANCE.getVerifyRole(),true).get(0)).queue();
                }

                if(DBVerifier.INSTANCE.syncNickname) {
                    Debugger.debugMessage("Renaming " + m.getNickname() + " to " + p.getName());
                    if(!m.isOwner())
                        m.getGuild().modifyNickname(m,p.getName()).queue();
                }

            }catch (NullPointerException | IndexOutOfBoundsException exception) {
                Debugger.debugMessage("The Bot can't find the given Role. The Role the problem has occured: verify");
                Debugger.debugMessage("UseIDs: " + ConfigFileManager.INSTANCE.useTokens());
            }

        }

        Debugger.debugMessage("Starting group loop");
        for(String group : DiscordFileManager.INSTANCE.getAllGroups()) {
            Debugger.debugMessage("Checking if group " + group + " is a dynamic group.");
            if(!DiscordFileManager.INSTANCE.isDynamicGroup(group)) {
                Debugger.debugMessage("Group is not dynamic.");
                Debugger.debugMessage("Checking if the player already has a dynamic group.");
                if(addedNonDynamicGroups.contains(m)) {
                    Debugger.debugMessage("Player already has a dynamic group. Skipping to next group.");
                    continue;
                }

                Debugger.debugMessage("Player does not have dynamic group.");
                Debugger.debugMessage("Checking if players has permission: " + DiscordFileManager.INSTANCE.getPermissionsForGroup(group));
                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    Debugger.debugMessage("Player has permission.");

                    try {
                        Debugger.debugMessage("Checking if tokens are used");
                        if(ConfigFileManager.INSTANCE.useTokens()) {
                            Debugger.debugMessage("Tokens are used");
                            Debugger.debugMessage("Token found for group: " + group + "-" +DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group));
                            Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group))).queue();
                        }else{
                            Debugger.debugMessage("Tokens are not used");
                            Debugger.debugMessage("Token found for group: " + group + "-" + DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group));
                            Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group),true).get(0)).queue();
                        }

                        if(DBVerifier.INSTANCE.syncNickname) {
                            Debugger.debugMessage("Renaming " + m.getEffectiveName() + " to " + DiscordFileManager.INSTANCE.getPrefix(group).replaceAll("%name%",p.getName()));
                            if(!m.isOwner()) {
                                m.getGuild().modifyNickname(m,DiscordFileManager.INSTANCE.getPrefix(group).replaceAll("%name%",p.getName())).queue();
                            }
                        }

                        Debugger.debugMessage("Adding nondynamic role to player");
                        roles.add(group);
                        addedNonDynamicGroups.add(m);
                    }catch (NullPointerException | IndexOutOfBoundsException exception) {
                        Debugger.debugMessage("The Bot can't find the given Role. The Role the problem has occured: " + group);
                        Debugger.debugMessage("UseIDs: " + ConfigFileManager.INSTANCE.useTokens());
                        return;
                    }

                } else {
                    Debugger.debugMessage("Player does not have permission.");
                }

            }else{
                Debugger.debugMessage("Group is dynamic");
                Debugger.debugMessage("Checking if players has permission: " + DiscordFileManager.INSTANCE.getPermissionsForGroup(group));

                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    Debugger.debugMessage("Player has permission.");
                    Debugger.debugMessage("Checking if tokens are used");

                    try {

                        if(ConfigFileManager.INSTANCE.useTokens()) {
                            Debugger.debugMessage("Tokens are used");
                            Debugger.debugMessage("Token found for group: " + group + "-" + DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group));
                            Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group))).queue();
                        }else{
                            Debugger.debugMessage("Tokens are not used");
                            Debugger.debugMessage("Token found for group: " + group + "-" + DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group));
                            Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group),true).get(0)).queue();
                        }
                        roles.add(group);
                    }catch (NullPointerException | IndexOutOfBoundsException exception) {
                        Debugger.debugMessage("The Bot can't find the given Role. The Role the problem has occured: " + group);
                        Debugger.debugMessage("UseIDs: " + ConfigFileManager.INSTANCE.useTokens());
                        return;
                    }
                }
            }
        }
        EventManager.instance.fireEvent(new UserUpdatedRolesEvent(m,p,roles));
    }

}
