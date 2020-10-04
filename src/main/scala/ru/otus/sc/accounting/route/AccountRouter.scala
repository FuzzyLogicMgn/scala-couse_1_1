package ru.otus.sc.accounting.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import ru.otus.sc.accounting.dao.{Account, Client, Transaction}
import ru.otus.sc.accounting.json.EntityJsonProtocol._
import ru.otus.sc.accounting.model._
import ru.otus.sc.accounting.service.AccountService
import ru.otus.sc.route.BaseRouter

class AccountRouter(accountService: AccountService) extends BaseRouter {
  override def route: Route =
    pathPrefix("account") {
      concat(
        createAccount,
        getAccount,
        postTran
      )
    }

  private val accountId2Account = JavaUUID.map(clientId => Account(None, clientId))

  private def createAccount: Route =
    (post & entity(as[Client])) { client =>
      client.id.map(clientId => onSuccess(accountService.create(AccountCreateRequest(Account(None, clientId)))) { res => complete(res.account) })
        .getOrElse(complete(StatusCodes.BadRequest))
    }

  private def getAccount: Route =
    (get & path(JavaUUID)) { accountId =>
      onSuccess(accountService.read(AccountReadRequest(accountId))) {
        case AccountReadResponse.Success(account) => complete(account)
        case AccountReadResponse.NotFound(_)      => complete(StatusCodes.NotFound)
      }
    }

  private def postTran: Route =
    (post & path(JavaUUID) & entity(as[Amount])) { (accountId, amount) =>
      onSuccess(
        accountService.postTransaction(
          AccountPostTransactionRequest(Transaction(None, accountId, amount))
        )
      ) {
        case AccountPostTransactionResponse.Success(transaction) => complete(transaction)
        case AccountPostTransactionResponse.RejectNotFoundAccount(accountId) =>
          complete(StatusCodes.NotFound, s"Account $accountId not found")
        case AccountPostTransactionResponse.RejectNotFoundRate(secId) =>
          complete(StatusCodes.NotFound, s"Currency $secId not found")
        case AccountPostTransactionResponse.RejectNotEnoughFunds(accountId, balance) =>
          complete(StatusCodes.Forbidden, s"Not enough funds: $balance on account $accountId")
      }
    }

}
