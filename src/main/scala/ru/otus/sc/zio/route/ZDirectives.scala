package ru.otus.sc.zio.route

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.otus.sc.zio.utils.LoggingUtils.localTimed
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging._
import zio._

import scala.concurrent.Future

object ZDirectives {
  type ZDirectives = Has[Service]

  trait Service {
    def completeZio[R: ToResponseMarshaller](zio: URIO[Blocking with Logging with Clock, R]): Route
    def onSuccessZio[R](zio: URIO[Blocking with Logging with Clock, R])(f: R => Route): Route
    def onZio[E: CanFail, R](zio: ZIO[Blocking with Logging with Clock, E, R])(
        f: Either[E, R] => Route
    ): Route
    def runToFuture[R](
        zio: URIO[Blocking with Logging with Clock, R]
    ): Future[R]

  }

  val live: URLayer[Blocking with Logging with Clock, ZDirectives] =
    ZIO
      .runtime[Blocking with Logging with Clock]
      .map { runtime =>
        new Service {
          def completeZio[R: ToResponseMarshaller](
              zio: URIO[Blocking with Logging with Clock, R]
          ): Route =
            onSuccessZio(zio) { res =>
              complete(res)
            }

          def onZio[E: CanFail, R](
              zio: ZIO[Blocking with Logging with Clock, E, R]
          )(f: Either[E, R] => Route): Route =
            onSuccessZio(zio.either)(f)

          def onSuccessZio[R](
              zio: URIO[Blocking with Logging with Clock, R]
          )(f: R => Route): Route =
            onSuccess(runtime.unsafeRunToFuture {
              localTimed("HTTP", UUID.randomUUID().toString) {
                zio
              }
            })(f)

          override def runToFuture[R](
              zio: URIO[Blocking with Logging with Clock, R]
          ): Future[R] =
            runtime.unsafeRunToFuture {
              localTimed("HTTP", UUID.randomUUID().toString) {
                zio
              }
            }
        }
      }
      .toLayer
}
