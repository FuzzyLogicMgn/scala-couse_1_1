package ru.otus.sc.greet.dao.impl
import ru.otus.sc.App
import ru.otus.sc.greet.dao.{ExchangeRate, ExchangeRatesDao}

import scala.xml.XML

/**
 * Класс для получения значений курсов валют
 */
class ExchangeRatesDaoImpl extends ExchangeRatesDao {
  val uri = "https://iss.moex.com/iss/statistics/engines/futures/markets/indicativerates/securities"
  lazy val exchangeRatesBySecId: Map[String, ExchangeRate] = {
    import java.net.URI
    import java.net.http.HttpResponse.BodyHandlers
    import java.net.http.{HttpClient, HttpRequest}

    // Запрашиваем XML с курсами валют
    App.log("Request currency exchange rates...")
    val client   = HttpClient.newHttpClient
    val request  = HttpRequest.newBuilder.uri(URI.create(uri)).build
    val response = client.send(request, BodyHandlers.ofString)
    //App.log(response.body)

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

  override def getExchangeRates: Map[String, ExchangeRate] = exchangeRatesBySecId
}
