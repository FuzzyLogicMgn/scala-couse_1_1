package ru.otus.sc.accounting.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import ru.otus.sc.accounting.dao.Client
import ru.otus.sc.accounting.json.EntityJsonProtocol._
import ru.otus.sc.accounting.model.{ClientCreateRequest, ClientReadRequest, ClientReadResponse}
import ru.otus.sc.accounting.service.ClientService
import ru.otus.sc.route.BaseRouter

class ClientRouter(clientService: ClientService) extends BaseRouter {
  override def route: Route =
    pathPrefix("client") {
      concat(createClient, getClient)
    }

  private val ClientIdRequest = JavaUUID.map(id => ClientReadRequest(id))

  private def createClient: Route =
    (post & entity(as[Client])) { client =>
      onSuccess(clientService.create(ClientCreateRequest(client))) { response =>
        complete(response.client)
      }
    }

  private def getClient: Route =
    (get & path(ClientIdRequest)) { readRq =>
      onSuccess(clientService.read(readRq)) {
        case ClientReadResponse.Success(client) => complete(client)
        case ClientReadResponse.NotFound(_)     => complete(StatusCodes.NotFound)
      }
    }
}
