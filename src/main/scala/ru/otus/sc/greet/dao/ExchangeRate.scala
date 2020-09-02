package ru.otus.sc.greet.dao

/**
 * Структура, описывающая курс пары валют
 * @param secid - имя пары валют (например, USD/RUB)
 * @param rate - значение курса пары валют
 */
case class ExchangeRate (secid: String, rate: Double)
