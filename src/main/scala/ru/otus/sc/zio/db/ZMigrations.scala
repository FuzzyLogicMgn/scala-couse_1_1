package ru.otus.sc.zio.db

import org.flywaydb.core.Flyway
import ru.otus.sc.zio.utils.LoggingUtils._
import zio._
import zio.blocking._
import zio.clock.Clock
import zio.logging.Logging

object ZMigrations {
  type Migrations = Has[Service]

  type Env = Blocking with Clock with Logging
  trait Service {
    def applyMigrations(): URIO[Env, Unit]
  }

  sealed trait AfterMigrations
  type WithMigrations = Has[AfterMigrations]

  val live: URLayer[Has[DbConfig], Has[Service]] = ZLayer.fromService { config =>
    new Service {
      override def applyMigrations(): URIO[Env, Unit] =
        localTimed("Migrations", "applyMigrations") {
          effectBlocking {
            Flyway
              .configure()
              .dataSource(config.dbUrl, config.dbUserName, config.dbPassword)
              .load()
              .migrate()
          }.orDie.unit
        }
    }
  }

  val afterMigrations: URLayer[Env with Migrations, WithMigrations] =
    ZIO.service[Service].flatMap(_.applyMigrations()).as(new AfterMigrations {}).toLayer

}
