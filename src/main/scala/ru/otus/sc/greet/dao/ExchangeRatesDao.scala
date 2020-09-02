package ru.otus.sc.greet.dao

trait ExchangeRatesDao {
  /**
   * Получить данные с актуальными курсами валют
   * @return - отображение имя пары валют (secid) на структуру описывающую курс
   */
  def getExchangeRates: Map[String, ExchangeRate]
}
