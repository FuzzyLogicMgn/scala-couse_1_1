package dao.map

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import dao.ClientServiceDaoTest
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import ru.otus.sc.Config
import ru.otus.sc.accounting.dao.ClientDao
import ru.otus.sc.accounting.dao.impl.slick.ClientDaoSlickImpl
import ru.otus.sc.db.Migrations
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Should uncomment create extension if not exists "uuid-ossp";
 * in src/main/resources/db/migration
 */
class ClientServiceSlickDaoTest extends ClientServiceDaoTest with ForAllTestContainer {

  override val container: PostgreSQLContainer = PostgreSQLContainer()
  var db: Database                            = _

  override def afterStart(): Unit = {
    new Migrations(Config(container.jdbcUrl, container.username, container.password))
      .applyMigrationsSync()
    db = Database.forURL(container.jdbcUrl, container.username, container.password)
  }

  override def beforeStop(): Unit = {
    db.close()
    super.beforeStop()
  }

  override def createDao(): ClientDao = {
    val dao = new ClientDaoSlickImpl(db)
    dao.deleteAll().futureValue
    dao
  }
}
