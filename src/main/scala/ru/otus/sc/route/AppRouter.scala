package ru.otus.sc.route
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import ru.otus.sc.accounting.route.{AccountRouter, ClientRouter}

class AppRouter(clientRouter: ClientRouter, accountRouter: AccountRouter) extends BaseRouter {
  override def route: Route =
    pathPrefix("api" / "v1") {
      concat(clientRouter.route, accountRouter.route)
    }
}
