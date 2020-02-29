package pw.byakuren.modbot.util

import net.dv8tion.jda.api.entities.{Guild, Message, Role, TextChannel}
import pw.byakuren.modbot.guild.{GuildData, GuildDataManager, GuildSettings}

object Utilities {
  implicit class MessageUtils(message: Message) {
    def reply(replyText: String): Unit = {
      message.getChannel.sendMessage(replyText).queue()
    }
  }
  implicit class StringUtils(string: String) {
    def asBoolean: Boolean = {
      string.matches("1|true|t|yes|y")
    }
  }
  implicit class IntUtils(i: Int) {
    def toPositionString: String = i + Seq("th", "st", "nd", "rd", "th")((i % 10) min 4)
  }
  implicit class GuildUtils(guild: Guild) {
    def getData(implicit guildDataManager: GuildDataManager): GuildData = guildDataManager(guild)
    def logChannel(implicit guildDataManager: GuildDataManager): Option[TextChannel] = getData.logChannel
    def modRole(implicit guildDataManager: GuildDataManager): Option[Role] = getData.moderatorRole
    def getSettings(implicit guildDataManager: GuildDataManager): GuildSettings = getData.guildSettings
  }
}
