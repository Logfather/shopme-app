package de.shopme.testing.system.tests

import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.domain.life.NimelisEventBus

data class HivraSystemContextTest(

    val roomRepository: RoomShoppingRepository,

    val itemDao: ItemDao,

    val listDao: ListDao,

    val changeQueueDao: ChangeQueueDao,

    val nimelisEventBus: NimelisEventBus
)