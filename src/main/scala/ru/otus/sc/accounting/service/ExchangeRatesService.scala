package ru.otus.sc.accounting.service

import ru.otus.sc.accounting.model.{
  ExchangeRatesConvertRequest,
  ExchangeRatesConvertResponse,
  ExchangeRatesRequest,
  ExchangeRatesResponse
}
import ru.otus.sc.common.service.AppService

/**
  * Сервис получения актуального курсов валют
  */
trait ExchangeRatesService extends AppService {

  /**
    * Получить значение курса для пары валют
    * @param rq - запрос содержит индекс пары валют
    * @return - значение курса, если было найдено
    */
  def getExchangeRate(rq: ExchangeRatesRequest): ExchangeRatesResponse

  /**
    * Конвертация суммы в целевую валюту. В случае совпадения текущей и целевой валюты возвращается исходная сумма.
    * Иначе конвертация происходит с промежуточным привидением к валюте биллинга(по-умолчанию - рубли):
    * TRAN_CURRENCY => BILLING_CURRENCY => ACCOUNT_CURRENCY
    * Итого может быть максимум 2 конвертации.
    * Например, если на счету 100$ и транзакция пополнения идёт на 100 рублей:
    * 1) первая конвертация получается вырожденной, так как валюта траназкции совпадает с валютой биллинга
    * 2) вторая конвертация переводит сумму в валюте билинга к сумме в валюте счёта
    * @param request - сумма в исходной валюте и целевая валюта
    * @return - сумма в целевой валюте
    */
  def convertAmount(request: ExchangeRatesConvertRequest): ExchangeRatesConvertResponse

  override def getServiceName: String = "ExchangeService"
}
