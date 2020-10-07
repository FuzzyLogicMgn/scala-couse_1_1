package ru.otus.sc.accounting.dao.impl.slick

import java.util.UUID

import ru.otus.sc.accounting.dao.{Client, ClientDao}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class ClientDaoSlickImpl(db: Database)(implicit ec: ExecutionContext) extends ClientDao {

  import ClientDaoSlickImpl._

  override def create(ent: Client): Future[Client] = {
    val newClient = ClientRow.fromClient(ent)
    val act = for {
      clientId <- clients.returning(clients.map(_.id)) += newClient
    } yield ent.copy(id = Some(clientId))
    db.run(act.transactionally)
  }

  override def update(ent: Client): Future[Option[Client]] = {
    ent.id match {
      case Some(clientId) =>
        val updateAct = clients.filter(cl => cl.id === clientId).map(u => u.name).update(ent.name)
        val act = for {
          upd <- clients.filter(cl => cl.id === clientId).forUpdate.result.headOption
          _ <- upd match {
            case Some(_) => updateAct
            case None    => DBIO.successful(())
          }
        } yield upd.map(_.toClient)
        db.run(act.transactionally)
      case None => Future.successful(None)
    }
  }

  override def read(entityId: UUID): Future[Option[Client]] = {
    val act = for {
      user <- clients.filter(cl => cl.id === entityId).result.headOption
    } yield user.map(_.toClient)
    db.run(act)
  }

  override def delete(entityId: UUID): Future[Option[Client]] = {
    val act = for {
      del <- clients.filter(_.id === entityId).forUpdate.result.headOption
      _ <- del match {
        case Some(_) => clients.filter(cl => cl.id === entityId).delete
        case None    => DBIO.successful(())
      }
    } yield del.map(_.toClient)
    db.run(act.transactionally)
  }

  override def findAll(): Future[Seq[Client]] = {
    val act = for {
      all <- clients.result
    } yield all.map(_.toClient)
    db.run(act)
  }

  def deleteAll(): Future[Unit] =
    db.run(clients.delete >> DBIO.successful(()))
}

object ClientDaoSlickImpl {

  case class ClientRow(id: Option[UUID], name: String) {
    def toClient: Client = Client(id, name)
  }

  object ClientRow extends ((Option[UUID], String) => ClientRow) {
    def fromClient(cl: Client): ClientRow = ClientRow(cl.id, cl.name)
  }

  class Clients(tag: Tag) extends Table[ClientRow](tag, "client") {
    val id   = column[UUID]("id", O.PrimaryKey, O.AutoInc)
    val name = column[String]("name")

    override def * = (id.?, name).mapTo[ClientRow]
  }

  val clients = TableQuery[Clients]
}
