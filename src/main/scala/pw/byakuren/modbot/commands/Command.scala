package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.commands.CommandPermission.CommandPermission

abstract class Command(names: Seq[String], val description: String, val permissionLevel: CommandPermission) {

  def name: String = aliases.head

  def aliases: Seq[String] = names

  def run(message: Message, args: Seq[String]): Unit

}
