package ru.otus.sc.accounting.dao

import ru.otus.sc.accounting.model.{Currency, ExchangeRate}


trait ExchangeRatesDao {

  /**
    * Получить данные с актуальными курсами валют
    * @return - отображение имя пары валют (secid) на структуру описывающую курс
    */
  def getExchangeRates: Map[String, ExchangeRate]

  /**
    * Получить курс валюты биллинга для данной валюты
    * @param currency - валюта, для которой нужно узнать курс
    * @return - курс валюты биллинга
    */
  def getBillingRates(currency: Currency): ExchangeRate
}
