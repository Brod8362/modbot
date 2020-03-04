package pw.byakuren.modbot.pagination

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.{Message, MessageEmbed, User}
import pw.byakuren.modbot.handlers.PaginatedMessageHandler
import pw.byakuren.modbot.util.Utilities._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PaginatedMessage {
  /**
   * Create a new PaginatedMessage. This message will automatically be added to the registry.
   *
   * @param originalMessage         The message this PaginatedMessage originated from.
   * @param addExtraMessageElements A function that will set desired values of an EmbedBuilder.
   * @param paginatedStrings        The PaginatedStrings object holding the desired data to be displayed.
   * @param paginatedMessages       The registry of PaginatedMessages
   * @return a Future of the PaginatedMessage to be created.
   */
  def apply(originalMessage: Message, addExtraMessageElements: EmbedBuilder => Unit, paginatedStrings: PaginatedStrings)
           (implicit paginatedMessages: PaginatedMessageHandler): Future[PaginatedMessage] = {
    val embedBuilder = new EmbedBuilder()
    addExtraMessageElements(embedBuilder)
    embedBuilder.setDescription(paginatedStrings.content)
    val messageFuture = originalMessage.replyFuture(embedBuilder.build())
    val paginatedFuture = messageFuture.map(new PaginatedMessage(_, originalMessage.getAuthor, addExtraMessageElements, paginatedStrings))
    paginatedMessages(paginatedFuture)
    paginatedFuture
  }

  def apply(originalMessage: Message, addExtraMessageElements: EmbedBuilder => Unit, data: IndexedSeq[String],
            linesPerPage: Int, startingPage: Int)(implicit paginatedMessages: PaginatedMessageHandler): Future[PaginatedMessage] = {
    apply(originalMessage, addExtraMessageElements, new PaginatedStrings(data, linesPerPage, startingPage))
  }

  def apply(originalMessage: Message, addExtraMessageElements: EmbedBuilder => Unit, data: IndexedSeq[String],
            linesPerPage: Int)(implicit paginatedMessages: PaginatedMessageHandler): Future[PaginatedMessage] = {
    apply(originalMessage, addExtraMessageElements, data, linesPerPage, 0)
  }
}

class PaginatedMessage(val message: Message, val sourceUser: User, addExtraMessageElements: EmbedBuilder => Unit,
                       paginatedString: PaginatedStrings) {

  if (paginatedString.maxPages > 1) {
    message.addReaction("⬅").queue()
    message.addReaction("➡").queue()
  }
  else
    message.addReaction("\uD83D\uDED1").queue()
  var lastInteractionTime: Long = System.currentTimeMillis()

  def updateTime(): Unit = lastInteractionTime = System.currentTimeMillis()

  /**
   * Advance the content by one page, and update the discord message.
   * If the content is already at the last page, nothing with happen.
   */
  def next(): Unit = {
    paginatedString.next()
    updateTime()
    updateMessage()
  }

  /**
   * Rewind the content by one page, and update the discord message.
   * If the content is already at page 1, nothing will happen.
   */
  def prev(): Unit = {
    paginatedString.prev()
    updateTime()
    updateMessage()
  }

  /**
   * Set the content to the desired page and update the discord message.
   * If the desired page is outside of the available range, nothing will happen.
   *
   * @param page
   * @return
   */
  def setPage(page: Int): Boolean = {
    updateTime()
    if (paginatedString.setPage(page)) {
      updateMessage()
      return true
    }
    false
  }

  private def makeMessageContent(): MessageEmbed = {
    val t = new EmbedBuilder()
      .setDescription(paginatedString.content)
      .setFooter(s"Page: ${paginatedString.currentPage + 1}/${paginatedString.maxPages}")
    addExtraMessageElements(t)
    t.build()
  }

  /**
   * Edit the discord message to display the current page's content.
   */
  def updateMessage(): Unit = {
    message.editMessage(makeMessageContent()).queue()
  }
}
