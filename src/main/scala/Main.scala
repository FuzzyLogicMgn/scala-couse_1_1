import ru.otus.sc._
import ru.otus.sc.accounting.dao.{Account, Client, Transaction}
import ru.otus.sc.accounting.model.{AccountCreateRequest, AccountPostTransactionRequest, AccountPostTransactionResponse, AccountReadRequest, AccountReadResponse, Amount, ClientCreateRequest, Currency, ExchangeRatesRequest}
import ru.otus.sc.common.model.EchoRequest

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

    val client  = app.createClient(ClientCreateRequest(Client(None, "SomeBody"))).client
    val account = app.createAccount(AccountCreateRequest(Account(None, client.id.get, Amount(0)))).account
    app.postTransaction(
      AccountPostTransactionRequest(
        Transaction(None, account.id.get, Amount(500, Currency.USD))
      )
    ) match {
      case AccountPostTransactionResponse.Success(transaction) => println(s"Transaction success")
      case AccountPostTransactionResponse.RejectNotFoundAccount(accountId) =>
        println(s"Transaction fail: account not found")
      case AccountPostTransactionResponse.RejectNotFoundRate(secId) =>
        println(s"Transaction fail: rate not found")
      case AccountPostTransactionResponse.RejectNotEnoughFunds(accountId, balance) =>
        println(s"Transaction fail: not enough funds")
    }
    app.readAccount(AccountReadRequest(account.id.get)) match {
      case AccountReadResponse.Success(account) => println(s"Current balance: ${account.amount}")
      case AccountReadResponse.NotFound(id) =>
        println(s"Account with id ${account.id.get} not found")
    }

  }
}
