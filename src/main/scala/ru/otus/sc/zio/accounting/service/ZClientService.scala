package ru.otus.sc.zio.accounting.service

import ru.otus.sc.accounting.model._
import ru.otus.sc.common.service.AppService
import ru.otus.sc.zio.accounting.dao.ZClientDao.ZClientDao
import zio._
import zio.clock.Clock
import zio.logging.Logging
import zio.macros.accessible

@accessible
object ZClientService {
  type ZClientService = Has[Service]
  type Env            = Logging with Clock
  trait Service extends AppService {
    def create(clientCreateRequest: ClientCreateRequest): URIO[Env, ClientCreateResponse]
    def update(clientUpdateRequest: ClientUpdateRequest): URIO[Env, ClientUpdateResponse]
    def read(clientReadRequest: ClientReadRequest): URIO[Env, ClientReadResponse]
    def delete(clientDeleteRequest: ClientDeleteRequest): URIO[Env, ClientDeleteResponse]

    def getServiceName: String = "ClientService"
  }

  val live: URLayer[ZClientDao, ZClientService] =
    ZLayer.fromService(clientDao =>
      new Service {
        override def create(
            clientCreateRequest: ClientCreateRequest
        ): URIO[Env, ClientCreateResponse] =
          clientDao.create(clientCreateRequest.client) map { ClientCreateResponse }

        override def update(
            clientUpdateRequest: ClientUpdateRequest
        ): URIO[Env, ClientUpdateResponse] =
          clientUpdateRequest.client.id match {
            case Some(clientId) =>
              clientDao.update(clientUpdateRequest.client) map {
                case Some(client) => ClientUpdateResponse.Success(client)
                case None         => ClientUpdateResponse.NotFound(clientId)
              }
            case None => ZIO.succeed(ClientUpdateResponse.ClientWithoutId)
          }

        override def read(clientReadRequest: ClientReadRequest): URIO[Env, ClientReadResponse] =
          clientDao.read(clientReadRequest.id) map {
            case Some(value) => ClientReadResponse.Success(value)
            case None        => ClientReadResponse.NotFound(clientReadRequest.id)
          }

        override def delete(
            clientDeleteRequest: ClientDeleteRequest
        ): URIO[Env, ClientDeleteResponse] =
          clientDao.delete(clientDeleteRequest.id) map {
            case Some(value) => ClientDeleteResponse.Success(value)
            case None        => ClientDeleteResponse.NotFound(clientDeleteRequest.id)
          }
      }
    )
}
