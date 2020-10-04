package ru.otus.sc.accounting.service

import ru.otus.sc.accounting.model.{
  AccountCreateRequest,
  AccountCreateResponse,
  AccountDeleteRequest,
  AccountDeleteResponse,
  AccountFindRequest,
  AccountFindResponse,
  AccountFindTransactionRequest,
  AccountFindTransactionResponse,
  AccountPostTransactionRequest,
  AccountPostTransactionResponse,
  AccountReadRequest,
  AccountReadResponse,
  AccountUpdateRequest,
  AccountUpdateResponse
}
import ru.otus.sc.common.service.AppService

import scala.concurrent.Future

/**
  * Сервис для операций со счетами.
  * Помимо CRUD операций поддержана возможность производить транзакции по счёту
  */
trait AccountService extends AppService {
  def create(accountCreateRequest: AccountCreateRequest): Future[AccountCreateResponse]
  def delete(accountDeleteRequest: AccountDeleteRequest): Future[AccountDeleteResponse]
  def update(accountUpdateRequest: AccountUpdateRequest): Future[AccountUpdateResponse]
  def read(accountReadRequest: AccountReadRequest): Future[AccountReadResponse]
  def find(accountFindRequest: AccountFindRequest): Future[AccountFindResponse]
  def postTransaction(
      accountPostTransactionRequest: AccountPostTransactionRequest
  ): Future[AccountPostTransactionResponse]
  def findTransactions(
      accountFindTransactionRequest: AccountFindTransactionRequest
  ): Future[AccountFindTransactionResponse]

  override def getServiceName: String = "AccountService"
}
