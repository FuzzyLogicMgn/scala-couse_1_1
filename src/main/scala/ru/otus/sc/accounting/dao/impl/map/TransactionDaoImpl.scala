package ru.otus.sc.accounting.dao.impl.map

import ru.otus.sc.accounting.dao.{Transaction, TransactionDao}

class TransactionDaoImpl extends TransactionDao with MapStoreDao[Transaction] {

}
