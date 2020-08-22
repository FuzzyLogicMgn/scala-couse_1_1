package ru.otus.sc.greet.model

import ru.otus.sc.greet.dao.ExchangeRate

/**
 * Ответ от сервиса курсов валют
 * @param rate - описание курса пары валют
 */
case class ExchangeRatesResponse(rate: Option[ExchangeRate])
