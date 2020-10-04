package ru.otus.sc.accounting.dao.impl.map

import ru.otus.sc.accounting.dao.{Client, ClientDao}

class ClientDaoImpl extends ClientDao with MapStoreDao[Client] {

}
