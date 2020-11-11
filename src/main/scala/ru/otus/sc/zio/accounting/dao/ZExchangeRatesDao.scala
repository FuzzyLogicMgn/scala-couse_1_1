package ru.otus.sc.zio.accounting.dao

import ru.otus.sc.accounting.dao.ExchangeRatesDao
import ru.otus.sc.accounting.dao.impl.ExchangeRatesDaoImpl
import ru.otus.sc.accounting.model.Currency.Currency
import ru.otus.sc.accounting.model.ExchangeRate
import zio.{Has, ZLayer}

object ZExchangeRatesDao {
  type ZExchangeRatesDao = Has[Service]

  trait Service extends ExchangeRatesDao

  val live: ZLayer[Any, Nothing, ZExchangeRatesDao] = ZLayer.fromFunction(env =>
    new Service {
      val e = new ExchangeRatesDaoImpl()

      /**
        * Получить данные с актуальными курсами валют
        *
        * @return - отображение имя пары валют (secid) на структуру описывающую курс
        */
      override def getExchangeRates: Map[String, ExchangeRate] = e.getExchangeRates

      /**
        * Получить курс валюты биллинга для данной валюты
        *
        * @param currency - валюта, для которой нужно узнать курс
        * @return - курс валюты биллинга
        */
      override def getBillingRates(currency: Currency): ExchangeRate = e.getBillingRates(currency)
    }
  )
}
