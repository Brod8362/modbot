package pw.byakuren.modbot.persistence

import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.database.SQLConnection

class ConversationWriteThread(implicit conversationTracker: ConversationTracker, sqlConnection: SQLConnection) extends Thread {

  private var loop = true

  override def run(): Unit = {
    while (loop) {
      Thread.sleep(1000*180)
      conversationTracker.write(sqlConnection)
      println("wrote some stuff to sql")
    }
  }

  override def interrupt(): Unit = {
    loop=false
  }
}
