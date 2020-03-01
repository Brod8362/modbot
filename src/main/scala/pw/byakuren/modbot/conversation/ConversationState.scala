package pw.byakuren.modbot.conversation

case object ConversationState extends Enumeration {
  type ConversationState = Value
  val Init, ServerInit, Waiting, InProgress, Canceled, Closed = Value
}
