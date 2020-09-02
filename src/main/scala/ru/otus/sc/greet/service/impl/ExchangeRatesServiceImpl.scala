package ru.otus.sc.greet.service.impl
import ru.otus.sc.greet.dao.ExchangeRatesDao
import ru.otus.sc.greet.model.{ExchangeRatesRequest, ExchangeRatesResponse}
import ru.otus.sc.greet.service.ExchangeRatesService

class ExchangeRatesServiceImpl(exchageRatesDao: ExchangeRatesDao) extends ExchangeRatesService {

  def getExchangeRate(rq: ExchangeRatesRequest): ExchangeRatesResponse =
    ExchangeRatesResponse(exchageRatesDao.getExchangeRates.get(rq.secid))
}
