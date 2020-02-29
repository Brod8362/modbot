package pw.byakuren.modbot.database

import java.io.{File, FileNotFoundException}
import java.sql.Date
import java.util.UUID

import net.dv8tion.jda.api.entities.{Guild, TextChannel, User}
import pw.byakuren.modbot.conversation.Conversation
import scalikejdbc._

class SQLConnection {
  Class.forName("org.sqlite.JDBC")
  ConnectionPool.singleton("jdbc:sqlite:modbot.db", null, null)

  implicit val session: AutoSession.type = AutoSession

  sql"CREATE TABLE IF NOT EXISTS log_channel (guild INTEGER PRIMARY KEY NOT NULL, channel INTEGER NOT NULL)".execute().apply()
  sql"""CREATE TABLE IF NOT EXISTS conversation_log (uuid STRING NOT NULL, guild INTEGER NOT NULL, user_id INTEGER NOT NULL,
    timestamp DATETIME NOT NULL, message_index INTEGER NOT NULL, message_author INTEGER NOT NULL, content STRING,
    PRIMARY KEY(uuid, message_index))""".execute().apply()

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
    val batchParams = messageLog.map(m => Seq(conversation.uuid, conversation.getGuild.get,conversation.user.getIdLong, conversation.time,
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
}
