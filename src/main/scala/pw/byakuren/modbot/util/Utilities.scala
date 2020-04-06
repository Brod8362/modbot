package pw.byakuren.modbot.util

import net.dv8tion.jda.api.{OnlineStatus, Permission}
import net.dv8tion.jda.api.entities.{Guild, Member, Message, MessageEmbed, Role, TextChannel, User}
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.guild.GuildSetting.GuildSetting
import pw.byakuren.modbot.guild.{GuildData, GuildDataManager, GuildSetting, GuildSettings}

import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global

object Utilities {
  implicit class MessageUtils(message: Message) {
    def reply(replyText: String): Unit = {
      message.getChannel.sendMessage(replyText.substring(0, replyText.length min 2000)).queue()
    }
    def reply(messageEmbed: MessageEmbed): Unit = {
      message.getChannel.sendMessage(messageEmbed).queue()
    }
    def replyFuture(replyText: String): Future[Message] = {
      Future {
        message.getChannel.sendMessage(replyText.substring(0, replyText.length min 2000)).complete()
      }
    }
    def replyFuture(messageEmbed: MessageEmbed): Future[Message] = {
      Future {
        message.getChannel.sendMessage(messageEmbed).complete()
      }
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
    def customPrefix(implicit guildDataManager: GuildDataManager): Option[String] = getData.customPrefix
    def getSettings(implicit guildDataManager: GuildDataManager): GuildSettings = getData.guildSettings
    def getAdminMembers(implicit guildDataManager: GuildDataManager): Set[Member] = {
      val roles = for (role <- guild.modRole) yield {
        guild.getMembers.asScala.filter(_.getRoles.contains(role)).toSet
      }
      val perms = guild.getMembers.asScala.filter(_.hasPermission(Permission.ADMINISTRATOR)).toSet
      perms++roles.getOrElse(Nil)
    }
    def apply(setting: GuildSetting)(implicit guildDataManager: GuildDataManager): Boolean = {
      guild.getSettings.apply(setting)
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
    def isGuildModerator(implicit guildDataManager: GuildDataManager): Boolean = {
      member.getGuild.getData.moderatorRole.exists(member.getRoles.contains(_)) ||
        member.getPermissions.contains(Permission.ADMINISTRATOR)
    }
  }
  implicit class UserUtils(user: User) {
    def hasActiveConversation(implicit conversationTracker: ConversationTracker): Boolean = {
      conversationTracker(user.getIdLong).isDefined
    }
  }
}
