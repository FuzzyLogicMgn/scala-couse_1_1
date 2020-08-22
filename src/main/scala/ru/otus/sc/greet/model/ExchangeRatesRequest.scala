package ru.otus.sc.greet.model

/**
 * Запрос к сервису курсов валют
 * @param secid - имя пары валют (например, USD/RUB)
 */
case class ExchangeRatesRequest(secid: String)
