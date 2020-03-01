package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.entities.{Member, User}
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.{CommandExecutor, CommandRegistry}
import pw.byakuren.modbot.Main.prefix
import pw.byakuren.modbot.commands.CommandPermission.CommandPermission
import pw.byakuren.modbot.commands.{CommandPermission, GuildCommand, PrivateCommand}
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class CommandExecutionHandler(owner: User, guildCommands: CommandRegistry[GuildCommand],
                              privateCommands: CommandRegistry[PrivateCommand])
                             (implicit guildDataManager: GuildDataManager,
                              conversationTracker: ConversationTracker) extends ListenerAdapter {
  private val commandExecutor = new CommandExecutor(owner.openPrivateChannel().complete())
  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    if (event.getAuthor.isBot) return
    if (event.getMessage.getContentRaw.startsWith(prefix)) {
      val args = event.getMessage.getContentRaw.substring(prefix.length).split(" ").toSeq
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
    if (event.getMessage.getContentRaw.startsWith(prefix)) {
      val args = event.getMessage.getContentRaw.substring(prefix.length).split(" ").toSeq
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
    if (member.isGuildModerator) CommandPermission.Admins
    CommandPermission.Everybody
  }
}
