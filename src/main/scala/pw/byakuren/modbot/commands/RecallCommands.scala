package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.commands.RecallCommands.mainLogic
import pw.byakuren.modbot.database.SQLConnection
import pw.byakuren.modbot.handlers.PaginatedMessageHandler
import pw.byakuren.modbot.pagination.PaginatedMessage
import pw.byakuren.modbot.util.Utilities._

object RecallCommands {

  class RecallConversationGuild(implicit val SQLConnection: SQLConnection, paginatedMessageHandler: PaginatedMessageHandler)
    extends GuildCommand(Seq("recall"), "", "", CommandPermission.Admins) {
    override def run(message: Message, args: Seq[String]): Unit = mainLogic(message, args, isGuild = true)
  }

  class RecallConversationPrivate(implicit SQLConnection: SQLConnection, paginatedMessages: PaginatedMessageHandler)
    extends PrivateCommand(Seq("recall"), "", "") {
    override def run(message: Message, args: Seq[String]): Unit = mainLogic(message, args, isGuild = false)
  }

  private def mainLogic(message: Message, args: Seq[String], isGuild: Boolean)
                       (implicit SQLConnection: SQLConnection, paginatedMessageHandler: PaginatedMessageHandler): Unit = {
    if (args.isEmpty) {
      message.reply("You need to provide a UUID!")
      return
    }
    val c = if (isGuild) {
      SQLConnection.getConversations(args.head, message.getGuild)
    } else {
      SQLConnection.getConversations(args.head, message.getAuthor)
    }
    c.size match {
      case 1 =>
        val ps = if (isGuild) c.head.pagedGuild else c.head.pagedPrivate
        PaginatedMessage(message, extraContent, ps)
      case 0 =>
        message.reply(s"No conversation with ID ${args.head} found.")
      case _ =>
        message.reply(s"More than one conversation matches that ID. Try being more specific.")
    }
  }
  private def extraContent(embedBuilder: EmbedBuilder): Unit = embedBuilder.setTitle(f"Conversation")
}