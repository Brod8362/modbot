package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class SetLogChannel(implicit data: GuildDataManager) extends
  Command(Seq("logchannel"), "Set the server's log channel.", CommandPermission.Admins) {
  override def run(message: Message, args: Seq[String]): Unit = {
    data(message.getGuild).logChannel = Some(message.getTextChannel)
    message.reply(f"Log Channel set to ${message.getTextChannel.getAsMention}")
  }
}
