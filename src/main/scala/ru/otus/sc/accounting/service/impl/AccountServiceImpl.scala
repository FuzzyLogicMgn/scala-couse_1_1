package ru.otus.sc.accounting.service.impl

import java.util.UUID

import ru.otus.sc.accounting.dao.{Account, AccountDao, Transaction, TransactionDao}
import ru.otus.sc.accounting.model._
import ru.otus.sc.accounting.service.{AccountService, ExchangeRatesService}

import scala.concurrent.{ExecutionContext, Future}

class AccountServiceImpl(
    accountDao: AccountDao,
    transactionDao: TransactionDao,
    exchangeRatesService: ExchangeRatesService
)(implicit ec: ExecutionContext)
    extends AccountService {
  override def create(accountCreateRequest: AccountCreateRequest): Future[AccountCreateResponse] =
    accountDao.create(accountCreateRequest.account) map AccountCreateResponse

  override def update(accountUpdateRequest: AccountUpdateRequest): Future[AccountUpdateResponse] =
    accountUpdateRequest.account.id match {
      case Some(accountId) =>
        accountDao.update(accountUpdateRequest.account) map {
          case Some(account) => AccountUpdateResponse.Success(account)
          case None          => AccountUpdateResponse.NotFound(accountId)
        }
      case None => Future.successful(AccountUpdateResponse.AccountWithoutId)
    }

  override def delete(accountDeleteRequest: AccountDeleteRequest): Future[AccountDeleteResponse] =
    accountDao.delete(accountDeleteRequest.id) map {
      case Some(value) => AccountDeleteResponse.Success(value)
      case None        => AccountDeleteResponse.NotFound(accountDeleteRequest.id)
    }

  override def read(accountReadRequest: AccountReadRequest): Future[AccountReadResponse] =
    accountDao.read(accountReadRequest.id) map {
      case Some(value) => AccountReadResponse.Success(value)
      case None        => AccountReadResponse.NotFound(accountReadRequest.id)
    }

  override def find(accountFindRequest: AccountFindRequest): Future[AccountFindResponse] = {
    def find(
        accounts: Seq[Account],
        clientId: UUID,
        filterFcn: Option[Account => Boolean]
    ): AccountFindResponse = {
      val clientAccounts = accounts.filter(acc => acc.clientId == clientId)
      filterFcn.map(fcn => clientAccounts.filter(fcn)).getOrElse(clientAccounts) match {
        case Seq() => AccountFindResponse.NotFound(clientId)
        case items => AccountFindResponse.Success(items)
      }
    }

    accountDao.findAll() map { accounts =>
      accountFindRequest match {
        case AccountFindRequest.ByClient(clientId) => find(accounts, clientId, None)
        case AccountFindRequest.ByClientAndFilter(clientId, filter) =>
          find(accounts, clientId, Some(filter))
      }
    }
  }

  override def postTransaction(request: AccountPostTransactionRequest): Future[AccountPostTransactionResponse] =
    accountDao.read(request.tran.accountId) flatMap {
      case Some(account) =>
        exchangeRatesService.convertAmount(ExchangeRatesConvertRequest(request.tran.amount, account.amount.currency)
        ) match {
          case ExchangeRatesConvertResponse.Success(tranAmount) =>
            if (tranAmount.value < 0 && (account.amount.value < tranAmount.value.abs)) {
              Future.successful(AccountPostTransactionResponse.RejectNotEnoughFunds(
                request.tran.accountId,
                account.amount
              ))
            } else {
              transactionDao.create(request.tran) map { registeredTran =>
                accountDao.update(
                  account.copy(amount =
                    account.amount.copy(value = account.amount.value + tranAmount.value)
                  )
                )
                AccountPostTransactionResponse.Success(registeredTran)
              }
            }
          case ExchangeRatesConvertResponse.RateNotFound(secid) => Future.successful(AccountPostTransactionResponse.RejectNotFoundRate(secid))
        }
      case None => Future.successful(AccountPostTransactionResponse.RejectNotFoundAccount(request.tran.accountId))
    }

  override def findTransactions(
      accountFindTransactionRequest: AccountFindTransactionRequest
  ): Future[AccountFindTransactionResponse] = {
    def find(
        accounts: Seq[Transaction],
        accountId: UUID,
        filterFcn: Option[Transaction => Boolean]
    ): AccountFindTransactionResponse = {
      val clientAccounts = accounts.filter(tr => tr.accountId == accountId)
      filterFcn.map(fcn => clientAccounts.filter(fcn)).getOrElse(clientAccounts) match {
        case Seq() => AccountFindTransactionResponse.NotFound(accountId)
        case items => AccountFindTransactionResponse.Success(items)
      }
    }

    transactionDao.findAll() map { transactions =>
      accountFindTransactionRequest match {
        case AccountFindTransactionRequest.ByAccount(accountId) =>
          find(transactions, accountId, None)
        case AccountFindTransactionRequest.ByAccountAndFilter(accountId, filter) =>
          find(transactions, accountId, Some(filter))
      }
    }
  }
}
