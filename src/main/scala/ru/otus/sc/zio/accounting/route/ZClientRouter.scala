package ru.otus.sc.zio.accounting.route

import java.util.UUID

import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import ru.otus.sc.accounting.dao.Client
import ru.otus.sc.accounting.json.AppTapir.{Endpoint, anyJsonBody, path, stringBody, _}
import ru.otus.sc.accounting.json.EntityJsonProtocol._
import ru.otus.sc.accounting.model.{ClientCreateRequest, ClientReadRequest, ClientReadResponse}
import ru.otus.sc.zio.accounting.service.ZClientService.{Env, ZClientService}
import ru.otus.sc.route.BaseRouter
import ru.otus.sc.zio.accounting.service.ZClientService
import ru.otus.sc.zio.route.ZDirectives
import ru.otus.sc.zio.route.ZDirectives.ZDirectives
import zio.{Has, URLayer, ZIO, ZLayer}

import scala.concurrent.Future

object ZClientRouter {

  type ZClientRouter = Has[Service]

  trait Service extends BaseRouter {

  }

  val live: URLayer[ZDirectives with ZClientService, ZClientRouter] = ZLayer.fromServices[ZDirectives.Service, ZClientService.Service, Service]((directives, clientService) => new Service {
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
      val res: ZIO[Env, Nothing, Right[Nothing, Client]] = clientService.create(ClientCreateRequest(client)) map { response =>
        Right(response.client)
      }
      directives.runToFuture(res)
    }

    private def getClient(clientId: UUID): Future[Either[String, Client]] = {
      val res = clientService.read(ClientReadRequest(clientId)) map {
        case ClientReadResponse.Success(client) => Right(client)
        case ClientReadResponse.NotFound(_) => Left(s"Client with ID $clientId not found")
      }
      directives.runToFuture(res)
    }

    val createClientRoute: Route = createClientEndpoint.toRoute(createClient)
    val readClientRoute: Route = getClientEndpoint.toRoute(getClient)

    def getEndpoints: Seq[Endpoint[_, _, _, _]] = Seq(createClientEndpoint, getClientEndpoint)
  })
}
