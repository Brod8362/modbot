package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.database.SQLConnection
import pw.byakuren.modbot.util.Utilities._

class RecallConversationGuild(implicit val SQLConnection: SQLConnection) extends GuildCommand(Seq("recall"), "", "", CommandPermission.Admins) {
  override def run(message: Message, args: Seq[String]): Unit = {
    if (args.isEmpty) {
      message.reply("You need to provide a UUID!")
      return
    }
    SQLConnection.getConversation(args.head, message.getGuild) match {
      case Some(x) =>
        message.reply(x.guildRepresentation)
      case None =>
        message.reply(s"No conversation found with UUID ${args.head}")
    }
  }
}
