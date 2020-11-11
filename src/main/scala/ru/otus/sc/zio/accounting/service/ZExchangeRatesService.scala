package ru.otus.sc.zio.accounting.service

import ru.otus.sc.accounting.model.{Amount, ExchangeRatesConvertRequest, ExchangeRatesConvertResponse}
import ru.otus.sc.zio.accounting.dao.ZExchangeRatesDao.ZExchangeRatesDao
import zio.clock.Clock
import zio.logging.Logging
import zio._
import zio.macros.accessible

@accessible
object ZExchangeRatesService {
  type ZExchangeRatesService = Has[Service]
  type Env                   = Logging with Clock
  trait Service {
    def convertAmount(request: ExchangeRatesConvertRequest): URIO[Env, ExchangeRatesConvertResponse]
  }

  val live: URLayer[ZExchangeRatesDao, ZExchangeRatesService] =
    ZLayer.fromService(exchangeRatesDao =>
      new Service {
        override def convertAmount(
            request: ExchangeRatesConvertRequest
        ): URIO[Env, ExchangeRatesConvertResponse] =
          request.amount.currency match {
            case request.target => ZIO.succeed(ExchangeRatesConvertResponse.Success(request.amount))
            case _ =>
              val toBillingCurrencyRate =
                exchangeRatesDao.getBillingRates(request.amount.currency).rate
              val fromBillingToTargetCurrencyRate =
                1 / exchangeRatesDao.getBillingRates(request.target).rate
              ZIO.succeed(
                ExchangeRatesConvertResponse.Success(
                  Amount(
                    request.amount.value * toBillingCurrencyRate * fromBillingToTargetCurrencyRate,
                    request.target
                  )
                )
              )
          }
      }
    )
}
