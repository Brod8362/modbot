package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.{Message, MessageEmbed}
import pw.byakuren.modbot.Main
import pw.byakuren.modbot.Main.paginatedMessageHandler
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.pagination.{PaginatedMessage, PaginatedStrings}
import pw.byakuren.modbot.util.Utilities._

class HelpCommands(implicit guildDataManager: GuildDataManager) {

  private val aliases = Seq("help", "h", "?")
  private val description = "Look up syntax and usage of commands"
  private val syntax = ""

  val Private: PrivateCommand = new PrivateCommand(aliases, description, syntax) {
    override def run(message: Message, args: Seq[String]): Unit = {
      logic(message, args, isGuild = false)
    }
  }

  val Guild: GuildCommand = new GuildCommand(aliases, description, syntax, CommandPermission.Everybody) {
    override def run(message: Message, args: Seq[String]): Unit = {
      logic(message, args, isGuild = true)
    }
  }

  def logic(message: Message, args: Seq[String], isGuild: Boolean): Unit = {
    val commands = if (isGuild) guildCommands
      .map(_.asInstanceOf[GuildCommand])
      .filter(message.getMember.permissionLevel>=_.permission)
    else privateCommands
    if (args.isEmpty) {
      val ps = new PaginatedStrings(commands.map(shortHelp).toIndexedSeq, 6)
      PaginatedMessage(message, addExtra, ps)
    } else {
      commands.find(_.aliases.contains(args.head)) match {
        case Some(cmd) =>
          message.reply(longHelp(cmd))
        case None =>
          message.reply(s"Command ${args.head} not found.")
      }
    }
  }

  private def privateCommands: Set[Command] = {
    Main.privateCommandRegistry.commands.asInstanceOf[Set[Command]]
  }

  private def guildCommands: Set[Command] = {
    Main.guildCommandRegistry.commands.asInstanceOf[Set[Command]]
  }

  private def shortHelp(cmd: Command): String = {
    s"`${cmd.name}`: ${cmd.description}"
  }

  private def longHelp(cmd: Command): MessageEmbed = {
    new EmbedBuilder()
      .setTitle(s"${cmd.name}")
      .setDescription(s"${cmd.description}\n\n`Syntax` ${Main.prefix}${cmd.name} ${cmd.syntax}")
      .setFooter(s"${cmd.aliases.mkString(",")}")
      .build()
  }

  private def addExtra(embedBuilder: EmbedBuilder): Unit = {
    embedBuilder.setTitle("Command help")
  }

}