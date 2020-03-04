package pw.byakuren.modbot.util

import java.util.concurrent.{Callable, Executors, ScheduledFuture}

import scala.concurrent.duration._

class TaskScheduler {
  private val scheduler = Executors.newScheduledThreadPool(1)
  def schedule[T](delay: Duration)(f: => T): ScheduledFuture[T] =
    scheduler.schedule((() => f): Callable[T], delay.length, delay.unit)
}
