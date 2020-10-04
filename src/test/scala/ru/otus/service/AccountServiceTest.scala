package ru.otus.service

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.sc.accounting.dao.{Account, AccountDao, Transaction, TransactionDao}
import ru.otus.sc.accounting.model._
import ru.otus.sc.accounting.service.ExchangeRatesService
import ru.otus.sc.accounting.service.impl.AccountServiceImpl

import scala.concurrent.{ExecutionContextExecutor, Future}

//TODO: Написать больше тестовых сценариев для транзакций
class AccountServiceTest extends AnyFreeSpec with MockFactory {

  val account1: Account = Account(Some(UUID.randomUUID()), UUID.randomUUID(), Amount(0))
  val account2: Account = Account(Some(UUID.randomUUID()), UUID.randomUUID(), Amount(0))
  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  "AccountServiceTests " - {

    "Post transaction" in {

      val accountDao      = mock[AccountDao]
      val tranDao         = mock[TransactionDao]
      val exchangeService = mock[ExchangeRatesService]
      val srv             = new AccountServiceImpl(accountDao, tranDao, exchangeService)
      val tran            = Transaction(None, account1.id.get, Amount(1, Currency.USD))
      val tran2           = tran.copy(id = Some(UUID.randomUUID()))
      val finalAccount    = account1.copy(amount = Amount(75))

      (accountDao.read _).expects(account1.id.get).returns(Future.successful(Some(account1)))
      (accountDao.update _).expects(finalAccount).returns(Future.successful(Some(finalAccount)))
      (tranDao.create _).expects(tran).returns(Future.successful(tran2))
      (exchangeService.convertAmount _)
        .expects(ExchangeRatesConvertRequest(tran.amount, account1.amount.currency))
        .returns(ExchangeRatesConvertResponse.Success(Amount(75)))

      srv.postTransaction(
        AccountPostTransactionRequest(tran)
      ).futureValue shouldBe AccountPostTransactionResponse.Success(tran2)

    }

    "Should be able to create account " in {
      val dao = mock[AccountDao]
      val srv = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])

      (dao.create _).expects(account1).returns(Future.successful(account2))

      srv.create(AccountCreateRequest(account1)).futureValue shouldBe AccountCreateResponse(account2)
    }

    "getAccount" - {
      "should return account" in {
        val dao = mock[AccountDao]
        val srv = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])
        val id  = UUID.randomUUID()

        (dao.read _).expects(id).returns(Future.successful(Some(account1)))

        srv.read(AccountReadRequest(id)).futureValue shouldBe AccountReadResponse.Success(account1)
      }

      "should return NotFound on unknown account" in {
        val dao = mock[AccountDao]
        val srv = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])
        val id  = UUID.randomUUID()

        (dao.read _).expects(id).returns(Future.successful(None))

        srv.read(AccountReadRequest(id)).futureValue shouldBe AccountReadResponse.NotFound(id)
      }
    }

    "updateAccount" - {
      "should update existing account" in {
        val dao = mock[AccountDao]
        val srv = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])

        (dao.update _).expects(account1).returns(Future.successful(Some(account2)))

        srv.update(AccountUpdateRequest(account1)).futureValue shouldBe AccountUpdateResponse.Success(account2)
      }

      "should return NotFound on unknown account" in {
        val dao = mock[AccountDao]
        val srv = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])

        (dao.update _).expects(account1).returns(Future.successful(None))

        srv.update(AccountUpdateRequest(account1)).futureValue shouldBe AccountUpdateResponse.NotFound(
          account1.id.get
        )
      }

      "should return CantUpdateAccountWithoutId on account without id" in {
        val dao     = mock[AccountDao]
        val srv     = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])
        val Account = account1.copy(id = None)

        srv.update(AccountUpdateRequest(Account)).futureValue shouldBe AccountUpdateResponse.AccountWithoutId
      }
    }

    "deleteAccount" - {
      "should delete account" in {
        val dao = mock[AccountDao]
        val srv = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])
        val id  = UUID.randomUUID()

        (dao.delete _).expects(id).returns(Future.successful(Some(account1)))

        srv.delete(AccountDeleteRequest(id)).futureValue shouldBe AccountDeleteResponse.Success(account1)
      }

      "should return NotFound on unknown account" in {
        val dao = mock[AccountDao]
        val srv = new AccountServiceImpl(dao, mock[TransactionDao], mock[ExchangeRatesService])
        val id  = UUID.randomUUID()

        (dao.delete _).expects(id).returns(Future.successful(None))

        srv.delete(AccountDeleteRequest(id)).futureValue shouldBe AccountDeleteResponse.NotFound(id)
      }
    }

  }
}
