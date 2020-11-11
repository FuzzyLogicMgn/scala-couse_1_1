package ru.otus.sc.zio.accounting.route

import akka.http.scaladsl.server.Route
import ru.otus.sc.accounting.json.AppTapir
import ru.otus.sc.accounting.json.AppTapir._
import ru.otus.sc.route.BaseRouter
import ru.otus.sc.zio.accounting.route.ZAccountRouter.ZAccountRouter
import ru.otus.sc.zio.accounting.route.ZClientRouter.ZClientRouter
import sttp.tapir.swagger.akkahttp.SwaggerAkka
import zio.{Has, URLayer, ZLayer}

object ZDocRouter {

  type ZDocRouter = Has[Service]

  trait Service extends BaseRouter {}

  val live: URLayer[ZClientRouter with ZAccountRouter, ZDocRouter] =
    ZLayer.fromServices[ZClientRouter.Service, ZAccountRouter.Service, Service] {
      (clientRouter, accountRouter) =>
        new Service {
          private val docsAsYaml: String = (clientRouter.getEndpoints ++ accountRouter.getEndpoints)
            .toOpenAPI("Account Service", "1.0")
            .toYaml
          override def route: Route = new SwaggerAkka(docsAsYaml).routes

          override def getEndpoints: Seq[AppTapir.Endpoint[_, _, _, _]] = Seq()
        }
    }
}
