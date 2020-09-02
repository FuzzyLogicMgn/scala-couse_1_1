package ru.otus.sc.accounting.model

// TODO: Use Enumeration
sealed trait Currency {
  def id: String
}

object Currency {

  object USD extends Currency {
    val id = "USD"
  }
  object RUB extends Currency {
    val id = "RUB"
  }
  object EUR extends Currency {
    val id = "EUR"
  }
  object CAD extends Currency {
    val id = "CAD"
  }
  object CHF extends Currency {
    val id = "CHF"
  }
  object CNY extends Currency {
    val id = "CNY"
  }
  object INR extends Currency {
    val id = "INR"
  }
  object JPY extends Currency {
    val id = "JPY"
  }
  object TRY extends Currency {
    val id = "TRY"
  }
  object UAH extends Currency {
    val id = "UAH"
  }

  def unapply(arg: String): Option[Currency] =
    arg match {
      case USD.id => Some(USD)
      case EUR.id => Some(EUR)
      case RUB.id => Some(RUB)
      case CAD.id => Some(CAD)
      case CHF.id => Some(CHF)
      case CNY.id => Some(CNY)
      case INR.id => Some(INR)
      case JPY.id => Some(JPY)
      case TRY.id => Some(TRY)
      case UAH.id => Some(UAH)
      case _      => None
    }

  def toSecId(from: Currency, to: Currency): String = s"${from.id}/${to.id}"
}

case class Amount(value: Double, currency: Currency = Currency.RUB)
