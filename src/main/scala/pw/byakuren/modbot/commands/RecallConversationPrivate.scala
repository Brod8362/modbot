package pw.byakuren.modbot.commands
import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.database.SQLConnection
import pw.byakuren.modbot.util.Utilities._

class RecallConversationPrivate(implicit SQLConnection: SQLConnection) extends PrivateCommand(Seq("recall"), "","") {
  override def run(message: Message, args: Seq[String]): Unit = {
    if (args.isEmpty) {
      message.reply("You need to provide a UUID!")
      return
    }
    SQLConnection.getConversation(args.head, message.getAuthor) match {
      case Some(x) =>
        message.reply(x.privateRepresentation)
      case None =>
        message.reply(s"No conversation found with UUID ${args.head}")
    }
  }
}
