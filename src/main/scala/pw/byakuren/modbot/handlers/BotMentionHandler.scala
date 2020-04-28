package pw.byakuren.modbot.handlers

import java.awt.Color
import java.time.LocalDateTime

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._
import scala.jdk.CollectionConverters._

class BotMentionHandler(implicit guildDataManager: GuildDataManager) extends ListenerAdapter {
  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    val msg = event.getMessage
    if (msg.getAuthor.isBot) return
    val botUser = event.getJDA.getSelfUser
    val modRole = msg.getGuild.getData.moderatorRole
    val logChannel = msg.getGuild.getData.logChannel
    if (msg.getMentionedMembers.asScala.map(_.getIdLong).contains(botUser.getIdLong)){
      modRole match {
        case Some(role) =>
          try {
            msg.delete().queue()
          } catch {
            case e: InsufficientPermissionException =>
              //failed to delete because insufficient perms
          }
          val eb = new EmbedBuilder
          eb.setTitle("Report")
          eb.setDescription(msg.getContentRaw)
          eb.setColor(Color.RED)
          msg.getChannel.sendMessage(s"${role.getAsMention}").embed(eb.build()).queue()
          logChannel match {
            case Some(channel) =>
              val eb = new EmbedBuilder()
              val pinger = msg.getAuthor
              eb.setTitle("User Report")
              eb.setFooter(f"${pinger.getName}#${pinger.getDiscriminator}", pinger.getAvatarUrl)
              eb.setDescription(msg.getContentRaw)
              eb.setAuthor(f"#${msg.getChannel.getName}")
              eb.setTimestamp(LocalDateTime.now())
              channel.sendMessage(eb.build()).queue()
          }
        case None =>
          msg.reply("Please send me a private message to report something.")
      }
    }
  }
}
