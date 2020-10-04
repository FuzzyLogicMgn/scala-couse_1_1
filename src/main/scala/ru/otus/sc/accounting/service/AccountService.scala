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

/**
  * Сервис для операций со счетами.
  * Помимо CRUD операций поддержана возможность производить транзакции по счёту
  */
trait AccountService extends AppService {
  def create(accountCreateRequest: AccountCreateRequest): AccountCreateResponse
  def delete(accountDeleteRequest: AccountDeleteRequest): AccountDeleteResponse
  def update(accountUpdateRequest: AccountUpdateRequest): AccountUpdateResponse
  def read(accountReadRequest: AccountReadRequest): AccountReadResponse
  def find(accountFindRequest: AccountFindRequest): AccountFindResponse
  def postTransaction(
      accountPostTransactionRequest: AccountPostTransactionRequest
  ): AccountPostTransactionResponse
  def findTransactions(
      accountFindTransactionRequest: AccountFindTransactionRequest
  ): AccountFindTransactionResponse

  override def getServiceName: String = "AccountService"
}
