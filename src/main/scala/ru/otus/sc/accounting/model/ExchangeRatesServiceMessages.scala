package ru.otus.sc.accounting.model

import ru.otus.sc.accounting.model.Currency.Currency

case class ExchangeRatesRequest(secid: String)
case class ExchangeRatesResponse(rate: Option[ExchangeRate])

case class ExchangeRatesConvertRequest(amount: Amount, target: Currency)
sealed trait ExchangeRatesConvertResponse
object ExchangeRatesConvertResponse {
  case class Success(amount: Amount) extends ExchangeRatesConvertResponse
  case class RateNotFound(secid: String) extends ExchangeRatesConvertResponse
}
