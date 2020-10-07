import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ru.otus.sc.Config
import ru.otus.sc.accounting.dao.impl.ExchangeRatesDaoImpl
import ru.otus.sc.accounting.dao.impl.map.TransactionDaoImpl
import ru.otus.sc.accounting.dao.impl.slick.{AccountDaoSlickImpl, ClientDaoSlickImpl}
import ru.otus.sc.accounting.route.{AccountRouter, ClientRouter, DocRouter}
import ru.otus.sc.accounting.service.impl.{AccountServiceImpl, ClientServiceImpl, ExchangeRatesServiceImpl}
import ru.otus.sc.db.Migrations
import ru.otus.sc.route.AppRouter
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.Using

//TODO:
// 1. Logging

/**
  * CREATE USER:
  * curl -X POST -d '{"name":"bob"}' -H 'Content-Type: application/json' http://localhost:8080/api/v1/client
  * CREATE ACCOUNT:
  * curl -X POST -d '{"id":{userID}, "name":"bob"}' -H 'Content-Type: application/json' http://localhost:8080/api/v1/account
  * POST TRANSACTION:
  * curl -X POST -d '{"value":150}' -H 'Content-Type: application/json' http://localhost:8080/api/v1/account/{accountID}
  * OR Scala-Otus.postman_collection.json
  */
object Main {

  def createRouter(database: Database)(implicit ec: ExecutionContext): AppRouter = {

    val dao          = new ClientDaoSlickImpl(database)
    val service      = new ClientServiceImpl(dao)
    val clientRouter = new ClientRouter(service)

    val accDao          = new AccountDaoSlickImpl(database)
    val tranDao         = new TransactionDaoImpl
    val exchangeService = new ExchangeRatesServiceImpl(new ExchangeRatesDaoImpl)
    val accRouter       = new AccountRouter(new AccountServiceImpl(accDao, tranDao, exchangeService))

    val docRouter = new DocRouter(clientRouter, accRouter)

    new AppRouter(clientRouter, accRouter, docRouter)
  }

  def main(args: Array[String]): Unit = {
    val config = Config.default

    implicit val system: ActorSystem = ActorSystem("system")
    import system.dispatcher

    Using.resource(Database.forURL(config.dbUrl, config.dbUserName, config.dbPassword)) { db =>
      new Migrations(config).applyMigrationsSync()
      val binding = Http().newServerAt("localhost", 8080).bind(createRouter(db).route)

      binding.foreach(b => println(s"Start listen on ${b.localAddress}"))

      StdIn.readLine()

      binding.map(_.unbind()).onComplete(_ => system.terminate())
    }

  }
}
