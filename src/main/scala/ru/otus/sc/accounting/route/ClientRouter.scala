package ru.otus.sc.accounting.route

import java.util.UUID

import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import ru.otus.sc.accounting.dao.Client
import ru.otus.sc.accounting.json.AppTapir._
import ru.otus.sc.accounting.json.EntityJsonProtocol._
import ru.otus.sc.accounting.model.{ClientCreateRequest, ClientReadRequest, ClientReadResponse}
import ru.otus.sc.accounting.service.ClientService
import ru.otus.sc.route.BaseRouter

import scala.concurrent.{ExecutionContext, Future}

class ClientRouter(clientService: ClientService) (implicit ec: ExecutionContext) extends BaseRouter {
  override def route: Route = concat(createClientRoute, readClientRoute)

  private val createClientEndpoint: Endpoint[Client, String, Client, Any] = baseEndpoint
    .in("client")
    .post
    .in(anyJsonBody[Client]).description("JSON with client description")
    .errorOut(stringBody)
    .out(anyJsonBody[Client]).description("Returns created client")

  private val getClientEndpoint: Endpoint[UUID, String, Client, Any] = baseEndpoint
    .in("client")
    .get
    .in(path[UUID]("clientId")).description("Client identifier (UUID)")
    .errorOut(stringBody)
    .out(anyJsonBody[Client]).description("Returns client")

  private def createClient(client: Client): Future[Either[String, Client]] = {
    clientService.create(ClientCreateRequest(client)) map { response =>
      Right(response.client)
    }
  }

  private def getClient(clientId: UUID): Future[Either[String, Client]] = {
    clientService.read(ClientReadRequest(clientId)) map {
      case ClientReadResponse.Success(client) => Right(client)
      case ClientReadResponse.NotFound(_) => Left(s"Client with ID $clientId not found")
    }
  }

  val createClientRoute: Route = createClientEndpoint.toRoute(createClient)
  val readClientRoute: Route = getClientEndpoint.toRoute(getClient)

  def getEndpoints: Seq[Endpoint[_, _, _, _]] = Seq(createClientEndpoint, getClientEndpoint)
}
