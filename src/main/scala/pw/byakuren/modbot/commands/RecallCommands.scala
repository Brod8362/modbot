package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.{Guild, Message, User}
import pw.byakuren.modbot.cache.ConversationCache
import pw.byakuren.modbot.commands.RecallCommands.mainLogic
import pw.byakuren.modbot.conversation.PreviousConversation
import pw.byakuren.modbot.database.SQLConnection
import pw.byakuren.modbot.handlers.PaginatedMessageHandler
import pw.byakuren.modbot.pagination.PaginatedMessage
import pw.byakuren.modbot.util.Utilities._

object RecallCommands {

  private val aliases = Seq("recall")
  private val desc = "Recall a previous conversation you were involved in"
  private val syntax = "[conversation ID]"

  class RecallConversationGuild(implicit val SQLConnection: SQLConnection,
                                paginatedMessageHandler: PaginatedMessageHandler, conversationCache: ConversationCache)
    extends GuildCommand(aliases, desc, syntax, CommandPermission.Admins) {
    override def run(message: Message, args: Seq[String]): Unit = mainLogic(message, args, isGuild = true)
  }

  class RecallConversationPrivate(implicit SQLConnection: SQLConnection, paginatedMessages: PaginatedMessageHandler, conversationCache: ConversationCache)
    extends PrivateCommand(aliases, desc, syntax) {
    override def run(message: Message, args: Seq[String]): Unit = mainLogic(message, args, isGuild = false)
  }

  private def mainLogic(message: Message, args: Seq[String], isGuild: Boolean)
                       (implicit SQLConnection: SQLConnection, paginatedMessageHandler: PaginatedMessageHandler, conversationCache: ConversationCache): Unit = {
    if (args.isEmpty) {
      message.reply("You need to provide a UUID!")
      return
    }
    val c = if (isGuild) {
      getConversations(args.head, message.getGuild)
    } else {
      getConversations(args.head, message.getAuthor)
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

  private def getConversations(uuid: String, guild: Guild)(implicit SQLConnection: SQLConnection,
                                                           conversationCache: ConversationCache): Set[PreviousConversation] = {
    (SQLConnection.getConversations(uuid,guild)++conversationCache.search(uuid).filter(_.guild==guild)).toSet
  }

  private def getConversations(uuid: String, user: User)(implicit SQLConnection: SQLConnection,
                                                           conversationCache: ConversationCache): Set[PreviousConversation] = {
    (SQLConnection.getConversations(uuid,user)++conversationCache.search(uuid).filter(_.user==user)).toSet
  }
}