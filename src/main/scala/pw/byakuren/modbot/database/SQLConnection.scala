package pw.byakuren.modbot.database

import java.sql.Date
import java.util.UUID

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.{Guild, Role, TextChannel, User}
import pw.byakuren.modbot.conversation.{Conversation, PreviousConversation}
import pw.byakuren.modbot.guild.GuildSettings
import scalikejdbc._

class SQLConnection {
  Class.forName("org.sqlite.JDBC")
  ConnectionPool.singleton("jdbc:sqlite:modbot.db", null, null)

  implicit val session: AutoSession.type = AutoSession

  sql"CREATE TABLE IF NOT EXISTS log_channel (guild INTEGER PRIMARY KEY NOT NULL, channel INTEGER NOT NULL)".execute().apply()
  sql"""CREATE TABLE IF NOT EXISTS conversation_log (uuid STRING NOT NULL, guild INTEGER NOT NULL, user_id INTEGER NOT NULL,
    timestamp DATETIME NOT NULL, message_index INTEGER NOT NULL, message_author INTEGER NOT NULL, content STRING,
    PRIMARY KEY(uuid, message_index))""".execute().apply()
  sql"CREATE TABLE IF NOT EXISTS guild_settings (guild INTEGER PRIMARY KEY NOT NULL, bitfield NOT NULL)".execute().apply()
  sql"CREATE TABLE IF NOT EXISTS guild_prefix (guild INTEGER PRIMARY KEY NOT NULL, prefix STRING)".execute().apply()
  sql"CREATE TABLE IF NOT EXISTS guild_modrole (guild INTEGER PRIMARY KEY NOT NULL, role INTEGER NOT NULL)".execute().apply()

  def setGuildLogChannel(channel: TextChannel): Boolean = {
    sql"INSERT OR REPLACE INTO log_channel VALUES (${channel.getGuild.getIdLong}, ${channel.getIdLong})".execute().apply()
  }

  def getGuildLogChannel(guild: Guild): Option[TextChannel] = {
    sql"SELECT channel FROM log_channel WHERE guild=${guild.getIdLong}"
      .map(_.long("channel")).single().apply() match {
      case Some(id) =>
        Some(guild.getTextChannelById(id))
      case _ => None
    }
  }

  def writeConversation(conversation: Conversation): Int = {
    val messageLog = conversation.messageLog
    val batchParams = messageLog.map(m => Seq(conversation.uuid, conversation.getGuild.get.getIdLong, conversation.user.getIdLong, conversation.time,
      messageLog.indexOf(m), m.getAuthor.getIdLong, m.getContentRaw))
    DB localTx { implicit session =>
      sql"INSERT INTO conversation_log VALUES (?,?,?,?,?,?,?)".batch(batchParams: _*).apply()
    }
    messageLog.size
  }

  def getGuildPreviousConversations(guild: Guild): Set[(UUID, Date, User)] = {
    sql"SELECT uuid,timestamp,user_id FROM conversation_log WHERE guild=${guild.getIdLong}".
      map(rs => (rs.string("uuid"), rs.date("timestamp"), rs.long("user_id")))
      .list()
      .apply()
      .map(t => (UUID.fromString(t._1), t._2, guild.getJDA.getUserById(t._3)))
      .toSet
  }

  def writeGuildSettings(guildSettings: GuildSettings): Unit = {
    DB localTx { implicit session =>
      sql"INSERT OR REPLACE INTO guild_settings VALUES (${guildSettings.guild.getIdLong}, ${guildSettings.value})"
        .execute()
        .apply()
    }
  }

  def getGuildSettings(guild: Guild): Option[GuildSettings] = {
    sql"SELECT bitfield FROM guild_settings WHERE guild=${guild.getIdLong}"
      .map(_.int("bitfield")).single().apply() match {
      case Some(bf) => Some(new GuildSettings(guild, bf))
      case _ => None
    }
  }

  def getConversations(uuid: String, user: User): Seq[PreviousConversation] = {
    val list =
      sql"""
         SELECT uuid,guild,user_id,message_author,content FROM conversation_log WHERE uuid LIKE ${uuid + "%"} AND
         user_id=${user.getIdLong}
       """.map(rs => (rs.string("uuid"), rs.long("guild"), rs.long("user_id"), rs.long("message_author"),
        rs.string("content"))).list().apply()
    conversationFromResults(list, user.getJDA)
  }

  def getConversations(uuid: String, guild: Guild): Seq[PreviousConversation] = {
    val list =
      sql"""SELECT uuid,guild,user_id,message_author,content FROM conversation_log WHERE uuid LIKE ${uuid + "%"} AND guild=${guild.getIdLong}"""
        .map(rs => (rs.string("uuid"), rs.long("guild"), rs.long("user_id"), rs.long("message_author"),
          rs.string("content")))
        .list()
        .apply()
    conversationFromResults(list, guild.getJDA)
  }

  private def conversationFromResults(list: List[(String, Long, Long, Long, String)], jda: JDA): Seq[PreviousConversation] = {
    {
      for (uuid:String <- list.map(_._1).toSet) yield {
        val sublist = list.filter(_._1 == uuid)
        val guild = jda.getGuildById(sublist.head._2)
        val user = guild.getMemberById(sublist.head._3)
        new PreviousConversation(UUID.fromString(sublist.head._1), user.getUser, guild, sublist.map(t => (guild.getMemberById(t._4).getUser, t._5)))
      }
      }.toSeq
  }

  def writeGuildPrefix(guild: Guild, prefix: String): Unit = {
    DB localTx { implicit session =>
      sql"""INSERT OR REPLACE INTO guild_prefix VALUES (${guild.getIdLong},$prefix)"""
        .execute()
        .apply()
    }
  }

  def getGuildPrefix(guild: Guild): Option[String] = {
    sql"""SELECT prefix FROM guild_prefix WHERE guild=${guild.getIdLong}"""
      .map(rs => rs.string("prefix"))
      .single()
      .apply()
  }

  def writeModeratorRole(server: Guild, mRole: Role): Unit = {
    sql"""INSERT OR REPLACE INTO guild_modrole VALUES (${server.getIdLong}, ${mRole.getIdLong})"""
      .execute()
      .apply()
  }

  def getModeratorRole(guild: Guild): Option[Role] = {
    val opt = sql"""SELECT role FROM guild_modrole WHERE guild=${guild.getIdLong}"""
      .map(rs => rs.long("role"))
      .single()
      .apply()
    opt match {
      case Some(id) => Option(guild.getRoleById(id))
      case None => None
    }
  }
}
