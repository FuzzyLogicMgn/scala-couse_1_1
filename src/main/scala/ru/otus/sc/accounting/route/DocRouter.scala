package ru.otus.sc.accounting.route

import akka.http.scaladsl.server.Route
import ru.otus.sc.accounting.json.AppTapir
import ru.otus.sc.route.BaseRouter
import ru.otus.sc.accounting.json.AppTapir._
import sttp.tapir.swagger.akkahttp.SwaggerAkka

class DocRouter(clientRouter: ClientRouter, accountRouter: AccountRouter) extends BaseRouter {
  private val docsAsYaml: String = (clientRouter.getEndpoints ++ accountRouter.getEndpoints)
    .toOpenAPI("Account Service", "1.0")
    .toYaml
  override def route: Route = new SwaggerAkka(docsAsYaml).routes

  override def getEndpoints: Seq[AppTapir.Endpoint[_, _, _, _]] = Seq()
}
