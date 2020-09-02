package ru.otus.sc

import ru.otus.sc.accounting.dao.impl.ExchangeRatesDaoImpl
import ru.otus.sc.accounting.dao.impl.map.{AccountDaoImpl, ClientDaoImpl, TransactionDaoImpl}
import ru.otus.sc.accounting.model.{
  AccountCreateRequest,
  AccountCreateResponse,
  AccountPostTransactionRequest,
  AccountPostTransactionResponse,
  AccountReadRequest,
  AccountReadResponse,
  ClientCreateRequest,
  ClientCreateResponse,
  ExchangeRate,
  ExchangeRatesRequest,
  ExchangeRatesResponse
}
import ru.otus.sc.accounting.service.{AccountService, ClientService, ExchangeRatesService}
import ru.otus.sc.accounting.service.impl.{
  AccountServiceImpl,
  ClientServiceImpl,
  ExchangeRatesServiceImpl
}
import ru.otus.sc.common.model.{EchoRequest, EchoResponse}
import ru.otus.sc.common.service.impl.{
  EchoServiceImpl,
  InvocationCounterServiceImpl,
  LocalStoreServiceImpl
}
import ru.otus.sc.common.service.{EchoService, InvocationCounterService, LocalStoreService}
import ru.otus.sc.greet.dao.impl.GreetingDaoImpl
import ru.otus.sc.greet.model._
import ru.otus.sc.greet.service.impl.GreetingServiceImpl
import ru.otus.sc.greet.service.GreetingService

import scala.util.{Failure, Success}

trait App {
  def greet(request: GreetRequest): GreetResponse
  def getCurrencyExchangeRate(request: ExchangeRatesRequest): ExchangeRatesResponse
  def echo[T](request: EchoRequest[T]): EchoResponse[T]
  def createClient(request: ClientCreateRequest): ClientCreateResponse
  def createAccount(request: AccountCreateRequest): AccountCreateResponse
  def readAccount(request: AccountReadRequest): AccountReadResponse
  def postTransaction(request: AccountPostTransactionRequest): AccountPostTransactionResponse
}

object App {
  private class AppImpl(
      greeting: GreetingService,
      exchangeRates: ExchangeRatesService,
      echoService: EchoService,
      counterService: InvocationCounterService,
      localStoreService: LocalStoreService,
      clientService: ClientService,
      accountService: AccountService
  ) extends App {
    def greet(request: GreetRequest): GreetResponse = {
      counterService.incrementAndGet(greeting.getServiceName)
      greeting.greet(request)
    }

    def getCurrencyExchangeRate(request: ExchangeRatesRequest): ExchangeRatesResponse = {
      counterService.incrementAndGet(exchangeRates.getServiceName)

      // Сначала пытаемся взять значение из локального кэша
      localStoreService.get(request.secid) match {
        case Some(value: ExchangeRate) =>
          App.log(s"Return rate from cache: $value")
          ExchangeRatesResponse(Some(value))
        case _ =>
          val response: ExchangeRatesResponse = exchangeRates.getExchangeRate(request)
          // Если удалось получить значение сохраняем его в кэш
          response.rate match {
            case Some(value) =>
              localStoreService.put(value.secid, value) match {
                case Failure(exception) => App.log(exception.getMessage)
                case Success(value)     =>
              }
            case None =>
          }
          response
      }
    }

    def echo[T](request: EchoRequest[T]): EchoResponse[T] = {
      counterService.incrementAndGet(echoService.getServiceName)
      echoService.echo(request)
    }

    override def createClient(request: ClientCreateRequest): ClientCreateResponse =
      clientService.create(request)

    override def createAccount(request: AccountCreateRequest): AccountCreateResponse =
      accountService.create(request)

    override def readAccount(request: AccountReadRequest): AccountReadResponse =
      accountService.read(request)

    override def postTransaction(
        request: AccountPostTransactionRequest
    ): AccountPostTransactionResponse = accountService.postTransaction(request)
  }

  def apply(): App = {
    val greetingDao     = new GreetingDaoImpl
    val greetingService = new GreetingServiceImpl(greetingDao)

    val exchangeRatesDao     = new ExchangeRatesDaoImpl
    val exchangeRatesService = new ExchangeRatesServiceImpl(exchangeRatesDao)
    val clientService        = new ClientServiceImpl(new ClientDaoImpl)
    val accountService       = new AccountServiceImpl(new AccountDaoImpl, new TransactionDaoImpl, exchangeRatesService)
    new AppImpl(
      greetingService,
      exchangeRatesService,
      new EchoServiceImpl,
      new InvocationCounterServiceImpl,
      // Кэшируем значения только для пары USD/RUB
      new LocalStoreServiceImpl(Set("USD/RUB")),
      clientService,
      accountService
    )
  }

  /**
    * TODO: Перейти на SLF4J или найти аналог для Scala
    * @param message - сообещение для логирования
    */
  def log(message: String): Unit = {
    System.err.println(message)
  }
}
