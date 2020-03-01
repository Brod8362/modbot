package pw.byakuren.modbot.commands

import pw.byakuren.modbot.commands.CommandPermission.CommandPermission

abstract class GuildCommand(names: Seq[String], val desc: String, val synt: String, val permission: CommandPermission)
  extends Command(names, desc, synt) {

}
