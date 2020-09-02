import ru.otus.sc._
import ru.otus.sc.greet.model.{EchoRequest, ExchangeRatesRequest}

/**
 * Пока без тестов, всё в main.
 */
object Main {
  def main(args: Array[String]): Unit = {
    val app = App()
    // Эхо сервис
    System.out.println(s"Echo response: ${app.echo(EchoRequest("First message")).content}")
    System.out.println(s"Echo response: ${app.echo(EchoRequest("Second message")).content}")

    // Сервис курсов валют
    System.out.println(
      s"Exchange rate USD/RUB: ${app.getCurrencyExchangeRate(ExchangeRatesRequest("USD/RUB")).rate.get.rate}"
    )
    System.out.println(
      s"Exchange rate USD/RUB (from cache): ${app.getCurrencyExchangeRate(ExchangeRatesRequest("USD/RUB")).rate.get.rate}"
    )
  }
}
