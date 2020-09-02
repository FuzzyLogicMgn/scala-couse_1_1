package ru.otus.sc.accounting.service

import ru.otus.sc.accounting.model._
import ru.otus.sc.common.service.AppService

/**
 * Сервис CRUD операция для владельцев счетов
 */
trait ClientService extends AppService {
  def create(clientCreateRequest: ClientCreateRequest): ClientCreateResponse
  def update(clientUpdateRequest: ClientUpdateRequest): ClientUpdateResponse
  def read(clientReadRequest: ClientReadRequest): ClientReadResponse
  def delete(clientDeleteRequest: ClientDeleteRequest): ClientDeleteResponse

  override def getServiceName: String = "ClientService"
}
