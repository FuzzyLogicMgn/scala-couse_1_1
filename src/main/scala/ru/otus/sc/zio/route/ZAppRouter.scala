package ru.otus.sc.zio.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.otus.sc.route.BaseRouter
import ru.otus.sc.zio.accounting.route.ZAccountRouter.ZAccountRouter
import ru.otus.sc.zio.accounting.route.ZClientRouter.ZClientRouter
import ru.otus.sc.zio.accounting.route.ZDocRouter.ZDocRouter
import ru.otus.sc.zio.accounting.route.{ZAccountRouter, ZClientRouter, ZDocRouter}
import sttp.tapir.Endpoint
import zio.{Has, URLayer, ZLayer}

object ZAppRouter {
  type ZAppRouter = Has[Service]
  trait Service extends BaseRouter

  val live: URLayer[ZClientRouter with ZAccountRouter with ZDocRouter, ZAppRouter] = ZLayer.fromFunction { env =>
    val clientRouter = env.get[ZClientRouter.Service]
    val accRouter = env.get[ZAccountRouter.Service]
    val docRouter = env.get[ZDocRouter.Service]
    new Service {
      override def route: Route = concat(clientRouter.route, accRouter.route, docRouter.route)

      override def getEndpoints: Seq[Endpoint[_, _, _, _]] = Seq()
    }
  }
}
