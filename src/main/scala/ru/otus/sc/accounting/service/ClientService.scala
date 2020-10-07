package ru.otus.sc.accounting.service

import ru.otus.sc.accounting.model._
import ru.otus.sc.common.service.AppService

import scala.concurrent.Future

/**
  * Сервис CRUD операция для владельцев счетов
  */
trait ClientService extends AppService {
  def create(clientCreateRequest: ClientCreateRequest): Future[ClientCreateResponse]
  def update(clientUpdateRequest: ClientUpdateRequest): Future[ClientUpdateResponse]
  def read(clientReadRequest: ClientReadRequest): Future[ClientReadResponse]
  def delete(clientDeleteRequest: ClientDeleteRequest): Future[ClientDeleteResponse]

  override def getServiceName: String = "ClientService"
}
