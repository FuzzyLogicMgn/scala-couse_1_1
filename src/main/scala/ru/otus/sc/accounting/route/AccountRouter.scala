package ru.otus.sc.accounting.route

import java.util.UUID

import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import ru.otus.sc.accounting.dao.{Account, Client, Transaction}
import ru.otus.sc.accounting.json.AppTapir
import ru.otus.sc.accounting.json.AppTapir.{Endpoint, anyJsonBody, stringBody, _}
import ru.otus.sc.accounting.json.EntityJsonProtocol._
import ru.otus.sc.accounting.model._
import ru.otus.sc.accounting.service.AccountService
import ru.otus.sc.route.BaseRouter
import sttp.model.StatusCode

import scala.concurrent.{ExecutionContext, Future}

class AccountRouter(accountService: AccountService)(implicit ec: ExecutionContext) extends BaseRouter {

  override def route: Route = concat(createAccountRoute, getAccountRoute, postTranRoute)

  private val createAccountEndpoint: Endpoint[Client, String, Account, Any] = baseEndpoint
    .in("account")
    .post
    .in(anyJsonBody[Client]).description("JSON with client description")
    .errorOut(stringBody)
    .out(anyJsonBody[Account]).description("Returns created account")

  private def createAccount(client: Client): Future[Either[String, Account]] =
    client.id.map(clientId => accountService.create(AccountCreateRequest(Account(None, clientId))) map { res => Right(res.account) })
      .getOrElse(Future.successful(Left("Client ID must be specified")))

  private val createAccountRoute = createAccountEndpoint.toRoute(createAccount)

  private val getAccountEndpoint: Endpoint[UUID, String, Account, Any] = baseEndpoint
    .get
    .in("account")
    .in(path[UUID]("accountId")).description("Account identifier (UUID)")
    .errorOut(stringBody)
    .out(anyJsonBody[Account]).description("Returns account")

  private def getAccount(accountId: UUID): Future[Either[String, Account]] =
    accountService.read(AccountReadRequest(accountId)) map {
      case AccountReadResponse.Success(account) => Right(account)
      case AccountReadResponse.NotFound(_) => Left(s"Account with ID=$accountId not found")
    }

  private val getAccountRoute = getAccountEndpoint.toRoute(getAccount)

  private val postTransactionEndpoint: Endpoint[(UUID, Amount), ErrorInfo, Transaction, Any] = baseEndpoint
    .in("account")
    .post
    .in(path[UUID]("accountId")).description("Account identifier (UUID)")
    .in(anyJsonBody[Amount]).description("Transaction amount")
    .errorOut(oneOf[ErrorInfo](
      statusMapping(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
      statusMapping(StatusCode.Forbidden, jsonBody[Forbidden].description("not enough funds")),
      statusDefaultMapping(jsonBody[Unknown].description("unknown"))
    ))
    .out(anyJsonBody[Transaction]).description("Returns created transaction")

  def postTran(params: (UUID, Amount)): Future[Either[ErrorInfo, Transaction]] =
    accountService.postTransaction(
      AccountPostTransactionRequest(Transaction(None, params._1, params._2))
    ) map {
      case AccountPostTransactionResponse.Success(transaction) => Right(transaction)
      case AccountPostTransactionResponse.RejectNotFoundAccount(accountId) => Left(NotFound(s"Account $accountId not found"))
      case AccountPostTransactionResponse.RejectNotFoundRate(secId) => Left(NotFound(s"Currency $secId not found"))
      case AccountPostTransactionResponse.RejectNotEnoughFunds(accountId, balance) => Left(Forbidden( s"Not enough funds: $balance on account $accountId"))
    }

  private val postTranRoute = postTransactionEndpoint.toRoute(postTran)

  override def getEndpoints: Seq[AppTapir.Endpoint[_, _, _, _]] = Seq(createAccountEndpoint, getAccountEndpoint, postTransactionEndpoint)
}
