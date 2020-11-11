package ru.otus.sc.zio.accounting.service

import java.util.UUID

import ru.otus.sc.accounting.dao.{Account, Transaction}
import ru.otus.sc.accounting.model._
import ru.otus.sc.common.service.AppService
import ru.otus.sc.zio.accounting.dao.ZAccountDao.ZAccountDao
import ru.otus.sc.zio.accounting.dao.ZTransactionDao.ZTransactionDao
import ru.otus.sc.zio.accounting.dao.{ZAccountDao, ZTransactionDao}
import ru.otus.sc.zio.accounting.service.ZExchangeRatesService.ZExchangeRatesService
import ru.otus.sc.zio.utils.LoggingUtils.localTimed
import zio._
import zio.clock.Clock
import zio.logging.Logging
import zio.macros.accessible

/**
  * Сервис для операций со счетами.
  * Помимо CRUD операций поддержана возможность производить транзакции по счёту
  */
@accessible
object ZAccountService {

  type ZAccountService = Has[Service]
  type Env             = Logging with Clock

  trait Service extends AppService {
    def create(accountCreateRequest: AccountCreateRequest): URIO[Env, AccountCreateResponse]
    def delete(accountDeleteRequest: AccountDeleteRequest): URIO[Env, AccountDeleteResponse]
    def update(accountUpdateRequest: AccountUpdateRequest): URIO[Env, AccountUpdateResponse]
    def read(accountReadRequest: AccountReadRequest): URIO[Env, AccountReadResponse]
    def find(accountFindRequest: AccountFindRequest): URIO[Env, AccountFindResponse]
    def postTransaction(
        accountPostTransactionRequest: AccountPostTransactionRequest
    ): URIO[Env, AccountPostTransactionResponse]
    def findTransactions(
        accountFindTransactionRequest: AccountFindTransactionRequest
    ): URIO[Env, AccountFindTransactionResponse]
    def getServiceName: String = "AccountService"
  }

  val live: URLayer[ZAccountDao with ZExchangeRatesService with ZTransactionDao, ZAccountService] =
    ZLayer.fromServices[
      ZAccountDao.Service,
      ZExchangeRatesService.Service,
      ZTransactionDao.Service,
      Service
    ]((accountDao, exchangeRatesService, transactionDao) =>
      new Service {
        override def create(
            accountCreateRequest: AccountCreateRequest
        ): URIO[Env, AccountCreateResponse] =
          localTimed("AccountService", "Create")(
            accountDao.create(accountCreateRequest.account) map AccountCreateResponse
          )

        override def delete(
            accountDeleteRequest: AccountDeleteRequest
        ): URIO[Env, AccountDeleteResponse] =
          localTimed("AccountService", "Delete")(
            accountDao.delete(accountDeleteRequest.id) map {
              case Some(value) => AccountDeleteResponse.Success(value)
              case None        => AccountDeleteResponse.NotFound(accountDeleteRequest.id)
            }
          )

        override def update(
            accountUpdateRequest: AccountUpdateRequest
        ): URIO[Env, AccountUpdateResponse] =
          localTimed("AccountService", "Update")(accountUpdateRequest.account.id match {
            case Some(accountId) =>
              accountDao.update(accountUpdateRequest.account) map {
                case Some(account) => AccountUpdateResponse.Success(account)
                case None          => AccountUpdateResponse.NotFound(accountId)
              }
            case None => ZIO.succeed(AccountUpdateResponse.AccountWithoutId)
          })

        override def read(
            accountReadRequest: AccountReadRequest
        ): URIO[Env, AccountReadResponse] =
          localTimed("AccountService", "Read")(accountDao.read(accountReadRequest.id) map {
            case Some(value) => AccountReadResponse.Success(value)
            case None        => AccountReadResponse.NotFound(accountReadRequest.id)
          })

        override def find(
            accountFindRequest: AccountFindRequest
        ): URIO[Env, AccountFindResponse] =
          localTimed("AccountService", "Find")({
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
          })

        override def postTransaction(
                                      request: AccountPostTransactionRequest
                                    ): URIO[Env, AccountPostTransactionResponse] =
          localTimed("AccountService", "PostTran")(accountDao.read(request.tran.accountId) flatMap {
            case Some(account) =>
              exchangeRatesService.convertAmount(
                ExchangeRatesConvertRequest(request.tran.amount, account.amount.currency)
              ) flatMap {
                case ExchangeRatesConvertResponse.Success(tranAmount) =>
                  if (tranAmount.value < 0 && (account.amount.value < tranAmount.value.abs)) {
                    ZIO.succeed(AccountPostTransactionResponse.RejectNotEnoughFunds(
                      request.tran.accountId,
                      account.amount
                    ))
                  } else {
                    transactionDao.create(request.tran) flatMap  { registeredTran =>
                      val finalAccount = account.copy(amount =
                        account.amount.copy(value = account.amount.value + tranAmount.value)
                      )
                      accountDao.update(finalAccount) map  { _ =>
                        AccountPostTransactionResponse.Success(registeredTran)
                      }
                    }
                  }
                case ExchangeRatesConvertResponse.RateNotFound(secid) =>
                  ZIO.succeed(AccountPostTransactionResponse.RejectNotFoundRate(secid))
              }
            case None =>
              ZIO.succeed(
                AccountPostTransactionResponse.RejectNotFoundAccount(request.tran.accountId)
              )
          })

        override def findTransactions(
            accountFindTransactionRequest: AccountFindTransactionRequest
        ): URIO[Env, AccountFindTransactionResponse] =
          localTimed("AccountService", "FindTran")(
            {
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
          )
      }
    )

}
