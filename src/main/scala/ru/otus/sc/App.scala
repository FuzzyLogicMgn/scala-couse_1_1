package ru.otus.sc

import ru.otus.sc.greet.dao.ExchangeRate
import ru.otus.sc.greet.dao.impl.{ExchangeRatesDaoImpl, GreetingDaoImpl}
import ru.otus.sc.greet.model._
import ru.otus.sc.greet.service.impl.{
  EchoServiceImpl,
  ExchangeRatesServiceImpl,
  GreetingServiceImpl,
  InvocationCounterServiceImpl,
  LocalStoreServiceImpl
}
import ru.otus.sc.greet.service.{
  EchoService,
  ExchangeRatesService,
  GreetingService,
  InvocationCounterService,
  LocalStoreService
}

trait App {
  def greet(request: GreetRequest): GreetResponse
  def getCurrencyExchangeRate(request: ExchangeRatesRequest): ExchangeRatesResponse
  def echo[T](request: EchoRequest[T]): EchoResponse[T]
}

object App {
  private class AppImpl(
      greeting: GreetingService,
      exchangeRates: ExchangeRatesService,
      echoService: EchoService,
      counterService: InvocationCounterService,
      localStoreService: LocalStoreService
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
                case Some(exception) => App.log(exception.getMessage)
                case None =>
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
  }

  def apply(): App = {
    val greetingDao     = new GreetingDaoImpl
    val greetingService = new GreetingServiceImpl(greetingDao)

    val exchangeRatesDao     = new ExchangeRatesDaoImpl
    val exchangeRatesService = new ExchangeRatesServiceImpl(exchangeRatesDao)
    new AppImpl(
      greetingService,
      exchangeRatesService,
      new EchoServiceImpl,
      new InvocationCounterServiceImpl,
      // Кэшируем значения только для пары USD/RUB
      new LocalStoreServiceImpl(Set("USD/RUB"))
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
