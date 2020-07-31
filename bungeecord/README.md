This is my litte Discord Verify Bungee plugin.
It can be used with alle bungeecord server, (Version 1.8 - 1.14)

Setup:
Edit you SQL data.
Put you Discord token in the config.yml without '' or ""
Edit the ranknames so they fit your discord rank e.g. vip is called on your discord premium, set premium as value

Introduction:
If you set up everything correctly the discordbot will be online.
Then you need to type !verify [ingame] on the discord and if the account with this name is on the mc server it will get a message.
The player than has to accept the message to verify itself. His discordid will get linked to his mc uuid and his mc rank will get synced to the discordrank.
The ingame command is editable in the config. The arguments should be self explaining.

Discord - Commands: 
!verify [ingame] -> verify yourself
!update -> update your rank (player must be online)
!unlink -> unlink yourself
!help -> list of all commands

Minecraft permissions:
db.verified = verify rank
db.admin = admin rank
db.discordstaff = discord staff rank
db.staff = staff rank
db.friend = friend rank
db.youtuber = youtuber rank
db.vip = vip rank

