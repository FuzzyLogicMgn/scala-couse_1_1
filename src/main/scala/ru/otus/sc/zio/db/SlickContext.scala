package ru.otus.sc.zio.db

import ru.otus.sc.zio.utils.LoggingUtils
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.PostgresProfile.api._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.Logging
import zio.{Has, URIO, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

object SlickContext {

  type SlickContext = Has[Service]
  type Env          = Logging with Clock

  trait Service {
    def run[R](make: ExecutionContext => DBIOAction[R, NoStream, Nothing]): URIO[Env, R]
  }

  val fromDbConfig: ZLayer[Has[DbConfig], Nothing, Has[Database]] =
    ZLayer.fromService(config => Database.forURL(config.dbUrl, config.dbUserName, config.dbPassword))

  val fromDatabase: ZLayer[Has[Database], Nothing, SlickContext] = ZLayer.fromService(db =>
    new Service {
      override def run[R](
          make: ExecutionContext => DBIOAction[R, NoStream, Nothing]
      ): URIO[Env, R] =
        LoggingUtils.localTimed("SlickContext", "run")(
          ZIO.fromFuture(ec => db.run(make(ec))).orDie
        )
    }
  )

  val live: ZLayer[Has[DbConfig] with Blocking with Clock with Logging, Nothing, SlickContext] =
    (ZLayer.requires[Has[DbConfig] with Blocking with Clock with Logging] ++ ZMigrations.live) >+>
      ZMigrations.afterMigrations >+>
      fromDbConfig >>>
      fromDatabase

}
