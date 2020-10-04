package ru.otus.sc.accounting.service.impl

import ru.otus.sc.accounting.dao.{Client, ClientDao}
import ru.otus.sc.accounting.model._
import ru.otus.sc.accounting.service.ClientService

class ClientServiceImpl(clientDao: ClientDao) extends ClientService {
  override def create(clientCreateRequest: ClientCreateRequest): ClientCreateResponse =
    ClientCreateResponse(clientDao.create(clientCreateRequest.client))

  override def update(clientUpdateRequest: ClientUpdateRequest): ClientUpdateResponse =
    clientUpdateRequest.client.id match {
      case Some(clientId) =>
        clientDao.update(clientUpdateRequest.client) match {
          case Some(client) => ClientUpdateResponse.Success(client)
          case None         => ClientUpdateResponse.NotFound(clientId)
        }
      case None => ClientUpdateResponse.ClientWithoutId
    }

  override def read(clientReadRequest: ClientReadRequest): ClientReadResponse =
    clientDao.read(clientReadRequest.id) match {
      case Some(value) => ClientReadResponse.Success(value)
      case None        => ClientReadResponse.NotFound(clientReadRequest.id)
    }

  override def delete(clientDeleteRequest: ClientDeleteRequest): ClientDeleteResponse =
    clientDao.delete(clientDeleteRequest.id) match {
      case Some(value) => ClientDeleteResponse.Success(value)
      case None        => ClientDeleteResponse.NotFound(clientDeleteRequest.id)
    }
}
