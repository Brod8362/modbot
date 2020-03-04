package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.database.SQLConnection
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.handlers.PaginatedMessageHandler
import pw.byakuren.modbot.pagination.PaginatedMessage
import pw.byakuren.modbot.util.Utilities._

class ConversationList(implicit guildDataManager: GuildDataManager, SQLConnection: SQLConnection,
                       paginatedMessageHandler: PaginatedMessageHandler) extends
  GuildCommand(Seq("conv"), "See previous conversations.", "", CommandPermission.Admins) {
  override def run(message: Message, args: Seq[String]): Unit = {
    val previous = guildDataManager(message.getGuild).getPreviousConversations(SQLConnection)
    if (previous.nonEmpty) {
      val a = previous.map(t => f"`${t._1.toString.substring(0, 8)}`: ${t._3.getAsMention} (${t._2})").toIndexedSeq
      PaginatedMessage(message, addExtra, a, 8)
    } else
      message.reply("No previous reports to view.")
  }

  def addExtra(embedBuilder: EmbedBuilder): Unit = {
    embedBuilder.setTitle("Previous Conversations")
  }
}
