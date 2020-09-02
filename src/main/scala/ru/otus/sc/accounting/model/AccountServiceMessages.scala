package ru.otus.sc.accounting.model

import java.util.UUID

import ru.otus.sc.accounting.dao.{Account, Transaction}

case class AccountCreateRequest(account: Account)
case class AccountCreateResponse(account: Account)

case class AccountReadRequest(id: UUID)
sealed trait AccountReadResponse
object AccountReadResponse {
  case class Success(account: Account) extends AccountReadResponse
  case class NotFound(id: UUID)        extends AccountReadResponse
}

case class AccountUpdateRequest(account: Account)
sealed trait AccountUpdateResponse
object AccountUpdateResponse {
  case class Success(account: Account) extends AccountUpdateResponse
  case class NotFound(id: UUID)        extends AccountUpdateResponse
  object AccountWithoutId              extends AccountUpdateResponse
}

case class AccountDeleteRequest(id: UUID)
sealed trait AccountDeleteResponse
object AccountDeleteResponse {
  case class Success(Account: Account) extends AccountDeleteResponse
  case class NotFound(id: UUID)        extends AccountDeleteResponse
}

sealed trait AccountFindRequest
object AccountFindRequest {
  case class ByClient(clientId: UUID) extends AccountFindRequest
  case class ByClientAndFilter(clientId: UUID, filter: Account => Boolean)
      extends AccountFindRequest
}
sealed trait AccountFindResponse
object AccountFindResponse {
  case class Success(accounts: Seq[Account]) extends AccountFindResponse
  case class NotFound(id: UUID)              extends AccountFindResponse
}

case class AccountPostTransactionRequest(tran: Transaction)
sealed trait AccountPostTransactionResponse
object AccountPostTransactionResponse {
  case class Success(transaction: Transaction)      extends AccountPostTransactionResponse
  case class RejectNotFoundAccount(accountId: UUID) extends AccountPostTransactionResponse
  case class RejectNotFoundRate(secId: String)      extends AccountPostTransactionResponse
  case class RejectNotEnoughFunds(accountId: UUID, balance: Amount)
      extends AccountPostTransactionResponse
}

sealed trait AccountFindTransactionRequest
object AccountFindTransactionRequest {
  case class ByAccount(accountId: UUID) extends AccountFindTransactionRequest
  case class ByAccountAndFilter(accountId: UUID, filter: Transaction => Boolean)
      extends AccountFindTransactionRequest
}
sealed trait AccountFindTransactionResponse
object AccountFindTransactionResponse {
  case class Success(transactions: Seq[Transaction]) extends AccountFindTransactionResponse
  case class NotFound(id: UUID)                      extends AccountFindTransactionResponse
}
