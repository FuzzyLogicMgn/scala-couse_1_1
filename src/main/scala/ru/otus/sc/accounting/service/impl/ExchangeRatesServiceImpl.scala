package ru.otus.sc.accounting.service.impl

import ru.otus.sc.accounting.dao.ExchangeRatesDao
import ru.otus.sc.accounting.model.{
  Amount,
  Currency,
  ExchangeRatesConvertRequest,
  ExchangeRatesConvertResponse,
  ExchangeRatesRequest,
  ExchangeRatesResponse
}
import ru.otus.sc.accounting.service.ExchangeRatesService

class ExchangeRatesServiceImpl(exchageRatesDao: ExchangeRatesDao) extends ExchangeRatesService {

  def getExchangeRate(rq: ExchangeRatesRequest): ExchangeRatesResponse =
    ExchangeRatesResponse(exchageRatesDao.getExchangeRates.get(rq.secid))

  override def convertAmount(request: ExchangeRatesConvertRequest): ExchangeRatesConvertResponse = {
    request.amount.currency match {
      case request.target => ExchangeRatesConvertResponse.Success(request.amount)
      case _ =>
        val toBillingCurrencyRate = exchageRatesDao.getBillingRates(request.amount.currency).rate
        val fromBillingToTargetCurrencyRate =
          1 / exchageRatesDao.getBillingRates(request.target).rate
        ExchangeRatesConvertResponse.Success(
          Amount(
            request.amount.value * toBillingCurrencyRate * fromBillingToTargetCurrencyRate,
            request.target
          )
        )
    }
  }
}
