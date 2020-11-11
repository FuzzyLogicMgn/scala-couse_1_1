package ru.otus.sc.zio.utils

import zio.ZIO
import zio.clock.Clock
import zio.logging._
import zio.duration._

object LoggingUtils {

  def logger[R <: Logging, E, A](name: String, names: String*)(
      zio: ZIO[R, E, A]
  ): ZIO[Logging with R, E, A] = {
    log.locally(LogAnnotation.Name(name :: names.toList))(zio)
  }

  def localTimed[R <: Logging with Clock, E, A](name: String, names: String*)(
      zio: ZIO[R, E, A]
  ): ZIO[Logging with R, E, A] = {
    logger(name, names: _*) {
      for {
        _         <- log.debug("Started")
        timeTuple <- zio.timed
        (duration, res) = timeTuple
        _ <- log.debug(s"Ended. Duration: ${duration.render}")
      } yield (res)
    }
  }

}
