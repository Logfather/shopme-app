package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.queue.ChangeQueueDao
import de.shopme.domain.life.NimelisEventBus
import de.shopme.testing.system.fake.FakeFirestoreGateway
import de.shopme.testing.system.scenario.ConcurrentItemUpdateScenario
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConcurrentItemUpdateTest {

    private lateinit var database: ShopMeDatabase

    private lateinit var itemDao: ItemDao
    private lateinit var listDao: ListDao
    private lateinit var changeQueueDao: ChangeQueueDao

    private lateinit var roomRepository: RoomShoppingRepository

    private lateinit var nimelisEventBus: NimelisEventBus

    @Before
    fun setup() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShopMeDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        itemDao = database.itemDao()
        listDao = database.listDao()
        changeQueueDao = database.changeQueueDao()

        nimelisEventBus = NimelisEventBus()

        roomRepository = RoomShoppingRepository(
            itemDao = itemDao,
            listDao = listDao,
            changeQueueDao = changeQueueDao,
            firestoreDataSource = FakeFirestoreGateway(),
            nimelisEventBus = nimelisEventBus
        )
    }

    @After
    fun teardown() {

        database.close()
    }

    @Test
    fun concurrentItemUpdateScenario_runsSuccessfully() = runTest {

        val context = HivraSystemContextTest(
            roomRepository = roomRepository,
            itemDao = itemDao,
            listDao = listDao,
            changeQueueDao = changeQueueDao,
            nimelisEventBus = nimelisEventBus
        )

        val runner = HivraSystemRunnerTest()

        runner.runScenario(
            scenario = ConcurrentItemUpdateScenario(),
            context = context
        )
    }
}