package pw.byakuren.modbot.commands
import net.dv8tion.jda.api.entities.{Activity, Message}

import pw.byakuren.modbot.util.Utilities._

class SetGame extends GuildCommand(Seq("setgame","sg"), "set the playing game", "", CommandPermission.Admins) {
  override def run(message: Message, args: Seq[String]): Unit = {
    message.getJDA.getPresence.setActivity(Activity.playing(args.mkString(" ")))
    message.reply("200")
  }
}
