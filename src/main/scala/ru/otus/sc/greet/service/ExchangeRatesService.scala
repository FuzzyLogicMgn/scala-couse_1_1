package ru.otus.sc.greet.service

import ru.otus.sc.greet.model.{ExchangeRatesRequest, ExchangeRatesResponse}

/**
  * Сервис получения актуального курса пары валют по идентификатору
  */
trait ExchangeRatesService extends AppService {

  def getExchangeRate(rq: ExchangeRatesRequest): ExchangeRatesResponse

  override def getServiceName: String = "ExchangeService"
}
