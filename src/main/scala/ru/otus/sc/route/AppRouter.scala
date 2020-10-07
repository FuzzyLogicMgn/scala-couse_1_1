package ru.otus.sc.route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.otus.sc.accounting.route.{AccountRouter, ClientRouter, DocRouter}
import sttp.tapir.Endpoint

class AppRouter(clientRouter: ClientRouter, accountRouter: AccountRouter, docRouter: DocRouter)
    extends BaseRouter {
  override def route: Route = concat(clientRouter.route, accountRouter.route, docRouter.route)

  override def getEndpoints: Seq[Endpoint[_, _, _, _]] = Seq()
}
