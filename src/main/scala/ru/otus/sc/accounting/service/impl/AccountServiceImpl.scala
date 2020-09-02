package ru.otus.sc.accounting.service.impl

import ru.otus.sc.accounting.dao.{Account, AccountDao, TransactionDao}
import ru.otus.sc.accounting.model._
import ru.otus.sc.accounting.service.{AccountService, ExchangeRatesService}

class AccountServiceImpl(
    accountDao: AccountDao,
    transactionDao: TransactionDao,
    exchangeRatesService: ExchangeRatesService
) extends AccountService {
  override def create(accountCreateRequest: AccountCreateRequest): AccountCreateResponse =
    AccountCreateResponse(
      accountDao.create(accountCreateRequest.account)
    )

  override def update(accountUpdateRequest: AccountUpdateRequest): AccountUpdateResponse =
    accountUpdateRequest.account.id match {
      case Some(accountId) =>
        accountDao.update(accountUpdateRequest.account) match {
          case Some(account) => AccountUpdateResponse.Success(account)
          case None          => AccountUpdateResponse.NotFound(accountId)
        }
      case None => AccountUpdateResponse.AccountWithoutId
    }

  override def delete(accountDeleteRequest: AccountDeleteRequest): AccountDeleteResponse =
    accountDao.delete(accountDeleteRequest.id) match {
      case Some(value) => AccountDeleteResponse.Success(value)
      case None        => AccountDeleteResponse.NotFound(accountDeleteRequest.id)
    }

  override def read(accountReadRequest: AccountReadRequest): AccountReadResponse =
    accountDao.read(accountReadRequest.id) match {
      case Some(value) => AccountReadResponse.Success(value)
      case None        => AccountReadResponse.NotFound(accountReadRequest.id)
    }

  override def find(accountFindRequest: AccountFindRequest): AccountFindResponse = {
    accountFindRequest match {
      case AccountFindRequest.ByClient(clientId) =>
        accountDao.findAll().filter(acc => acc.clientId == clientId) match {
          case Seq() => AccountFindResponse.NotFound(clientId)
          case items => AccountFindResponse.Success(items)
        }
      case AccountFindRequest.ByClientAndFilter(clientId, filter) =>
        accountDao.findAll().filter(acc => acc.clientId == clientId && filter(acc)) match {
          case Seq() => AccountFindResponse.NotFound(clientId)
          case items => AccountFindResponse.Success(items)
        }
    }
  }

  override def postTransaction(
      request: AccountPostTransactionRequest
  ): AccountPostTransactionResponse =
    accountDao.read(request.tran.accountId) match {
      case Some(account) =>
        exchangeRatesService.convertAmount(
          ExchangeRatesConvertRequest(request.tran.amount, account.amount.currency)
        ) match {
          case ExchangeRatesConvertResponse.Success(tranAmount) =>
            tranAmount match {
              case _ if tranAmount.value < 0 && (account.amount.value < tranAmount.value.abs) =>
                AccountPostTransactionResponse.RejectNotEnoughFunds(
                  request.tran.accountId,
                  account.amount
                )
              case _ =>
                val registeredTran = transactionDao.create(request.tran)
                accountDao.update(
                  account.copy(amount =
                    account.amount.copy(value = account.amount.value + tranAmount.value)
                  )
                )
                AccountPostTransactionResponse.Success(registeredTran)
            }
          case ExchangeRatesConvertResponse.RateNotFound(secid) =>
            AccountPostTransactionResponse.RejectNotFoundRate(secid)
        }
      case None => AccountPostTransactionResponse.RejectNotFoundAccount(request.tran.accountId)
    }

  override def findTransactions(
      accountFindTransactionRequest: AccountFindTransactionRequest
  ): AccountFindTransactionResponse = {
    accountFindTransactionRequest match {
      case AccountFindTransactionRequest.ByAccount(accountId) =>
        transactionDao.findAll().filter(tr => tr.accountId == accountId) match {
          case Seq() => AccountFindTransactionResponse.NotFound(accountId)
          case items => AccountFindTransactionResponse.Success(items)
        }
      case AccountFindTransactionRequest.ByAccountAndFilter(accountId, filter) =>
        transactionDao.findAll().filter(tr => tr.accountId == accountId && filter(tr)) match {
          case Seq() => AccountFindTransactionResponse.NotFound(accountId)
          case items => AccountFindTransactionResponse.Success(items)
        }
    }
  }
}
