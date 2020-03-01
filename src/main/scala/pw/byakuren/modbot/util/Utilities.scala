package pw.byakuren.modbot.util

import net.dv8tion.jda.api.{OnlineStatus, Permission}
import net.dv8tion.jda.api.entities.{Guild, Member, Message, Role, TextChannel, User}
import pw.byakuren.modbot.guild.{GuildData, GuildDataManager, GuildSettings}

import scala.jdk.CollectionConverters._

object Utilities {
  implicit class MessageUtils(message: Message) {
    def reply(replyText: String): Unit = {
      message.getChannel.sendMessage(replyText).queue()
    }
    def translateMentions(from: User, to: User): String = {
      message.getContentRaw.replace(from.getAsMention, to.getAsMention)
    }
  }
  implicit class StringUtils(string: String) {
    def asBoolean: Boolean = {
      string.matches("1|true|t|yes|y")
    }
  }
  implicit class IntUtils(i: Int) {
    def toPositionString: String = i + Seq("th", "st", "nd", "rd", "th")((i % 10) min 4)
    //this breaks for 11-19
  }
  implicit class GuildUtils(guild: Guild) {
    def getData(implicit guildDataManager: GuildDataManager): GuildData = guildDataManager(guild)
    def logChannel(implicit guildDataManager: GuildDataManager): Option[TextChannel] = getData.logChannel
    def modRole(implicit guildDataManager: GuildDataManager): Option[Role] = getData.moderatorRole
    def getSettings(implicit guildDataManager: GuildDataManager): GuildSettings = getData.guildSettings
    def getAdminMembers(implicit guildDataManager: GuildDataManager): Set[Member] = {
      val roles = for (role <- guild.modRole) yield {
        guild.getMembers.asScala.filter(_.getRoles.contains(role)).toSet
      }
      val perms = guild.getMembers.asScala.filter(_.hasPermission(Permission.ADMINISTRATOR)).toSet
      perms++roles.getOrElse(Nil)
    }
  }
  implicit class MemberUtils(member: Member) {
    def toMarkdownStatusString: String = {
      val name = member.getUser.getName+"#"+member.getUser.getDiscriminator
      member.getOnlineStatus match {
        case OnlineStatus.ONLINE => f"[$name]()"
        case OnlineStatus.IDLE => f"< $name >"
        case OnlineStatus.DO_NOT_DISTURB => f"/* $name *"
        case _ => f"> $name"
      }
    }
  }
}
