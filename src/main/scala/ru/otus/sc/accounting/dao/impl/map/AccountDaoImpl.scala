package ru.otus.sc.accounting.dao.impl.map

import ru.otus.sc.accounting.dao.{Account, AccountDao}

class AccountDaoImpl extends AccountDao with MapStoreDao[Account] {
}
