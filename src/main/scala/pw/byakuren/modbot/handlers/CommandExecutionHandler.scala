package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.entities.{Member, User}
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.commands.CommandPermission.CommandPermission
import pw.byakuren.modbot.commands.{CommandPermission, GuildCommand, PrivateCommand}
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.guild.{GuildDataManager, GuildSetting}
import pw.byakuren.modbot.util.Utilities._
import pw.byakuren.modbot.{CommandExecutor, CommandRegistry, Main}

class CommandExecutionHandler(owner: User, guildCommands: CommandRegistry[GuildCommand],
                              privateCommands: CommandRegistry[PrivateCommand])
                             (implicit guildDataManager: GuildDataManager,
                              conversationTracker: ConversationTracker) extends ListenerAdapter {
  private val commandExecutor = new CommandExecutor(owner.openPrivateChannel().complete())

  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    if (event.getAuthor.isBot) return
    val guild = event.getMessage.getGuild
    val rawContent = event.getMessage.getContentRaw
    if (!guild(GuildSetting.disableDefaultPrefix) && rawContent.startsWith(Main.prefix) || guild.customPrefix.isDefined
    && rawContent.startsWith(guild.customPrefix.get)) {
      val prefix = List(Some(Main.prefix), guild.customPrefix).flatMap(_.toSeq).find(rawContent.startsWith)
      val args = rawContent.substring(prefix.get.length).split(" ").toSeq
      val a = args.head
      guildCommands(args.head) match {
        case Some(command) =>
          if (permissionLevel(event.getMember) >= command.permission)
            commandExecutor.execute(command, event.getMessage, args drop 1)
          else
            event.getMessage.reply("Insufficient permission")
        case _ =>
          event.getMessage.reply("Command not found")
      }
    }
  }

  override def onPrivateMessageReceived(event: PrivateMessageReceivedEvent): Unit = {
    if (event.getAuthor.isBot) return
    if (event.getMessage.getContentRaw.startsWith(Main.prefix)) {
      val args = event.getMessage.getContentRaw.substring(Main.prefix.length).split(" ").toSeq
      if (event.getAuthor.hasActiveConversation) {
        event.getMessage.reply("`Commands are currently unavailable.`")
        return
      }
      privateCommands(args.head) match {
        case Some(command) =>
          commandExecutor.execute(command, event.getMessage, args drop 1)
        case _ =>
          event.getMessage.reply("Command not found")
      }
    }
  }

  def permissionLevel(member: Member): CommandPermission = {
    if (member.getIdLong == owner.getIdLong) return CommandPermission.Debug
    if (member.isGuildModerator) return CommandPermission.Admins
    CommandPermission.Everybody
  }
}
