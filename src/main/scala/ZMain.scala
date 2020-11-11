import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import org.slf4j.LoggerFactory
import ru.otus.sc.zio.config.HttpConfig
import ru.otus.sc.zio.di.DI
import ru.otus.sc.zio.route.ZAppRouter
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.{Console, getStrLn, putStrLn}
import zio.internal.{Executor, Platform}
import zio.logging.slf4j.Slf4jLogger
import zio.{Exit, Runtime, ZIO, ZLayer, ZManaged}

object ZMain {
  private val log = LoggerFactory.getLogger("RuntimeReporter")

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("system")
    val bind = for {
      route   <- ZIO.service[ZAppRouter.Service].map(_.route)
      config  <- ZIO.service[HttpConfig]
      binding <- ZIO.fromFuture(_ => Http().newServerAt(config.host, config.port).bind(route)).orDie
    } yield binding

    val endpoint = ZManaged.make(bind)(binding => ZIO.fromFuture(_ => binding.unbind()).orDie)
    val action = endpoint.use { binding =>
      putStrLn("Binding on " + binding.localAddress) *>
        putStrLn("Press Enter to exit") *>
        getStrLn.unit
    }

    val runtime = Runtime.default
      .withExecutor(Executor.fromExecutionContext(Platform.defaultYieldOpCount)(system.dispatcher))
      .withReportFailure { cause => if (cause.died) { log.error(cause.prettyPrint) } }

    val logging = Slf4jLogger.makeWithAnnotationsAsMdc(Nil)

    val app = for {
      fiber <-
        action
          .provideLayer(DI.live ++ ZLayer.requires[Console])
          .provideSomeLayer[Blocking with Clock with Console](logging)
          .fork
      _ <- ZIO.effectTotal(
        CoordinatedShutdown(system)
          .addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate, "interrupt-effect") { () =>
            runtime.unsafeRunToFuture(fiber.interrupt.as(Done))
          }
      )
      _ <- fiber.join
      _ <- fiber.interrupt

    } yield ()

    runtime.unsafeRunAsync(app.orDie) { _: Exit[Nothing, Unit] => system.terminate() }
  }
}
