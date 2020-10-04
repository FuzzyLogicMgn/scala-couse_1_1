package ru.otus.sc.accounting.dao.impl

import ru.otus.sc.App
import ru.otus.sc.accounting.dao.ExchangeRatesDao
import ru.otus.sc.accounting.model.{Currency, ExchangeRate}

import scala.xml.XML

/**
  * Класс для получения значений курсов валют
  */
class ExchangeRatesDaoImpl extends ExchangeRatesDao {
  val uri = "https://iss.moex.com/iss/statistics/engines/futures/markets/indicativerates/securities"

  private val BILLING_CURRENCY = Currency.RUB

  lazy val exchangeRatesBySecId: Map[String, ExchangeRate] = {
    import java.net.URI
    import java.net.http.HttpResponse.BodyHandlers
    import java.net.http.{HttpClient, HttpRequest}

    // Запрашиваем XML с курсами валют
    App.log("Request currency exchange rates...")
    val client   = HttpClient.newHttpClient
    val request  = HttpRequest.newBuilder.uri(URI.create(uri)).build
    val response = client.send(request, BodyHandlers.ofString)
    App.log(response.body)

    // Разбираем XML в отображение <имя_пары_валют> - <значение_курса>
    val doc = XML.loadString(response.body)
    val id2Rate: Seq[(String, ExchangeRate)] = for {
      item <- doc \\ "row" if item \@ "secid" != ""
    } yield {
      val secid = item \@ "secid"
      val rate  = item \@ "rate"
      //App.log(s"Rate: $secid: $rate")
      secid -> ExchangeRate(secid, rate.toDouble)
    }
    id2Rate.toMap
  }

  lazy val billingCurrencyRate: Map[Currency, ExchangeRate] = {
    val currencyPairRegex = """([A-Z]{3})/([A-Z]{3})""".r
    exchangeRatesBySecId
      .flatMap(entry =>
        entry._1 match {
          case currencyPairRegex(first, BILLING_CURRENCY.id) =>
            first match {
              case Currency(cur) => Seq(cur -> entry._2)
              case _             => Seq()
            }
          case _ => Seq()
        }
      )
  }

  override def getExchangeRates: Map[String, ExchangeRate] = exchangeRatesBySecId

  override def getBillingRates(currency: Currency): ExchangeRate =
    currency match {
      case BILLING_CURRENCY => ExchangeRate("BILLING_CURRENCY", 1)
      case _                => billingCurrencyRate(currency)
    }
}
