package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.conversation.Conversation
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class ViewChatQueue(implicit guildDataManager: GuildDataManager) extends
  GuildCommand(Seq("chatqueue", "queue"), "See the chat queue.", "", CommandPermission.Admins) {

  override def run(message: Message, args: Seq[String]): Unit = {
    val queue = guildDataManager(message.getGuild).getQueue()
    if (queue.isEmpty) {
      message.reply("There's nobody in the queue right now.")
      return
    }
    val currentUser = f"${queue.head.user.getName}#${queue.head.user.getDiscriminator}"
    message.reply(f"```diff\nChat Queue\n+$currentUser\n${queueString(queue)}```")
  }

  private def queueString(queue: Seq[Conversation]) = {
    {for (i <- 1 until queue.size) yield {
      f"$i: ${queue(i).user.getName}#${queue(i).user.getDiscriminator}"
    }}.mkString("\n")
  }
}
