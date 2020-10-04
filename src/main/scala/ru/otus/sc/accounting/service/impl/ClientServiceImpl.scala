package ru.otus.sc.accounting.service.impl

import ru.otus.sc.accounting.dao.{Client, ClientDao}
import ru.otus.sc.accounting.model._
import ru.otus.sc.accounting.service.ClientService

import scala.concurrent.{ExecutionContext, Future}

class ClientServiceImpl(clientDao: ClientDao)(implicit ec: ExecutionContext) extends ClientService {
  override def create(clientCreateRequest: ClientCreateRequest): Future[ClientCreateResponse] =
    clientDao.create(clientCreateRequest.client) map { ClientCreateResponse }

  override def update(clientUpdateRequest: ClientUpdateRequest): Future[ClientUpdateResponse] =
    clientUpdateRequest.client.id match {
      case Some(clientId) =>
        clientDao.update(clientUpdateRequest.client) map {
          case Some(client) => ClientUpdateResponse.Success(client)
          case None         => ClientUpdateResponse.NotFound(clientId)
        }
      case None => Future.successful(ClientUpdateResponse.ClientWithoutId)
    }

  override def read(clientReadRequest: ClientReadRequest): Future[ClientReadResponse] =
    clientDao.read(clientReadRequest.id) map {
      case Some(value) => ClientReadResponse.Success(value)
      case None        => ClientReadResponse.NotFound(clientReadRequest.id)
    }

  override def delete(clientDeleteRequest: ClientDeleteRequest): Future[ClientDeleteResponse] =
    clientDao.delete(clientDeleteRequest.id) map {
      case Some(value) => ClientDeleteResponse.Success(value)
      case None        => ClientDeleteResponse.NotFound(clientDeleteRequest.id)
    }
}
