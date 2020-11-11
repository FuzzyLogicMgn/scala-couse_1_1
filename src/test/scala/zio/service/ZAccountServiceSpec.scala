package zio.service

import java.util.UUID

import ru.otus.sc.accounting.dao.{Account, Transaction}
import ru.otus.sc.accounting.model.AccountReadResponse.Success
import ru.otus.sc.accounting.model._
import ru.otus.sc.zio.accounting.dao.{ZAccountDao, ZTransactionDao}
import ru.otus.sc.zio.accounting.service.{ZAccountService, ZExchangeRatesService}
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.test.Assertion._
import zio.test.mock.Expectation._
import zio.test.mock._
import zio.test.{testM, _}

@mockable[ZAccountDao.Service]
object AccountDaoMock

@mockable[ZTransactionDao.Service]
object TransactionDaoMock

@mockable[ZExchangeRatesService.Service]
object ExchangeRateServiceMock

object ZAccountServiceSpec extends DefaultRunnableSpec {

  private val account  = Account(Some(UUID.randomUUID()), UUID.randomUUID(), Amount(0))
  private val account2 = Account(Some(UUID.randomUUID()), UUID.randomUUID(), Amount(0))
  private val envMock = TransactionDaoMock.Read(equalTo(UUID.randomUUID()), value(None)).atMost(0) ++
    ExchangeRateServiceMock
      .ConvertAmount(
        equalTo(ExchangeRatesConvertRequest(Amount(0), Currency.USD)),
        value(ExchangeRatesConvertResponse.Success(Amount(0, Currency.USD)))
      )
      .atMost(0)

  type TestEnv = Environment with Logging

  override def spec: Spec[_root_.zio.test.environment.TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("AccountServiceTests")(
      testM("createAccount") {
        val env    = AccountDaoMock.Create(equalTo(account), value(account2))
        val action = ZAccountService.create(AccountCreateRequest(account))
        assertM(
          action.provideSomeLayer[TestEnv]((env ++ envMock) >>> ZAccountService.live)
        )(hasField("account", _.account, equalTo(account2)))
      },
      testM("updateAccount") {
        val updateAccount = account.copy(clientId = UUID.randomUUID())
        val env           = AccountDaoMock.Update(equalTo(updateAccount), value(Some(updateAccount)))
        val action        = ZAccountService.update(AccountUpdateRequest(updateAccount))
        assertM(
          action.provideSomeLayer[TestEnv]((env ++ envMock) >>> ZAccountService.live) map {
            case AccountUpdateResponse.Success(acc) => acc
          }
        )(equalTo(updateAccount))
      },
      testM("readAccount") {
        val accountId = UUID.randomUUID()
        val env       = AccountDaoMock.Read(equalTo(accountId), value(Some(account2)))
        val action    = ZAccountService.read(AccountReadRequest(accountId))
        val act       = action.provideSomeLayer[TestEnv]((env ++ envMock) >>> ZAccountService.live)
        assertM(act.map {
          case Success(acc) => acc
        })(equalTo(account2))
      },
      testM("postTransaction") {
        val tran         = Transaction(None, account.id.get, Amount(1, Currency.USD))
        val tran2        = tran.copy(id = Some(UUID.randomUUID()))
        val finalAccount = account.copy(amount = Amount(75))

        val env = AccountDaoMock.Update(equalTo(finalAccount), value(Some(finalAccount))) &&
          AccountDaoMock.Read(equalTo(account.id.get), value(Some(account))) &&
          TransactionDaoMock.Create(equalTo(tran), value(tran2)) &&
          ExchangeRateServiceMock.ConvertAmount(
            equalTo(ExchangeRatesConvertRequest(tran.amount, account.amount.currency)),
            value(ExchangeRatesConvertResponse.Success(Amount(75)))
          )

        val action = ZAccountService.postTransaction(AccountPostTransactionRequest(tran))

        assertM(action.provideSomeLayer[TestEnv](env >>> ZAccountService.live) map {
          case AccountPostTransactionResponse.Success(transaction) => transaction
        })(equalTo(tran2))
      }
    ).provideSomeLayerShared[Environment](Slf4jLogger.makeWithAnnotationsAsMdc(Nil))
}
