package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.react.{MessageReactionAddEvent, MessageReactionRemoveEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.pagination.PaginatedMessage
import pw.byakuren.modbot.util.TaskScheduler

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class PaginatedMessageHandler(implicit taskScheduler: TaskScheduler) extends ListenerAdapter {

  private val timeout = 4.minutes

  private val messages: mutable.Map[Long, Future[PaginatedMessage]] = new mutable.HashMap

  override def onMessageReactionAdd(event: MessageReactionAddEvent): Unit =
    handleReaction(event.getMessageIdLong, event.getUser, event.getReactionEmote)

  override def onMessageReactionRemove(event: MessageReactionRemoveEvent): Unit =
    handleReaction(event.getMessageIdLong, event.getUser, event.getReactionEmote)

  def handleReaction(messageId: Long, user: User, reactionEmote: ReactionEmote): Unit = {
    get(messageId) match {
      case Some(messageFuture) =>
        for (paginatedMessage <- messageFuture) {
          if (paginatedMessage.sourceUser == user) {
            reactionEmote.getEmoji match {
              case "➡" =>
                paginatedMessage.next()
              case "⬅" =>
                paginatedMessage.prev()
              case _ =>
            }
          }
        }
      case None => //do nothing, because this message isn't paginated
    }
  }

  def apply(paginatedMessageFuture: Future[PaginatedMessage]): Unit = {
    paginatedMessageFuture.map({
      m =>
        messages.put(m.message.getIdLong, paginatedMessageFuture)
        taskScheduler.schedule(timeout) {
          removeOldMessage(m)
          println("removed message?")
        }
    })
  }

  def get(id: Long): Option[Future[PaginatedMessage]] = messages.get(id)

  private def removeOldMessage(paginatedMessage: PaginatedMessage): Unit = {
    messages.remove(paginatedMessage.message.getIdLong)
    paginatedMessage.message.clearReactions().queue()
  }
}

