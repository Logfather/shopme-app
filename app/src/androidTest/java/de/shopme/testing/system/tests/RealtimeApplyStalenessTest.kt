package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.shopme.core.coroutines.AppScope
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.FirestoreListener
import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.life.NimelisEventBus
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.scenarios.RealtimeApplyStalenessScenario
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealtimeApplyStalenessTest {

    private lateinit var databaseA: ShopMeDatabase

    val appScope =
        AppScope()

    @Before
    fun setup() {

        databaseA = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShopMeDatabase::class.java
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {

        databaseA.close()
    }

    @Test
    fun realtimeApplyStalenessScenario_runsSuccessfully() = runBlocking<Unit> {

        val sharedGateway =
            InMemoryFakeFirestoreGateway()

        val eventBusA =
            NimelisEventBus()

        val repositoryA =
            RoomShoppingRepository(
                itemDao = databaseA.itemDao(),
                listDao = databaseA.listDao(),
                changeQueueDao = databaseA.changeQueueDao(),
                firestoreDataSource = sharedGateway,
                nimelisEventBus = eventBusA
            )

        val syncCoordinatorA =
            SyncCoordinator(
                changeQueueDao = databaseA.changeQueueDao(),
                itemDao = databaseA.itemDao(),
                listDao = databaseA.listDao(),
                firestore = sharedGateway,
                appScope = appScope,
                firebaseAuth = null,
                conflictResolver = ConflictResolver(),
                roomRepository = repositoryA
            )

        repositoryA.attachSyncCoordinator(syncCoordinatorA)

        val firestoreListener =
            FirestoreListener(
                dataSource = sharedGateway,
                itemDao = databaseA.itemDao(),
                listDao = databaseA.listDao(),
                conflictResolver = ConflictResolver(),
                appScope = appScope
            )

        firestoreListener.startItemSync(
            listId = "shared-list"
        )

        val deviceA =
            MultiDeviceContextTest(
                deviceName = "DeviceA",
                roomRepository = repositoryA,
                itemDao = databaseA.itemDao(),
                listDao = databaseA.listDao(),
                changeQueueDao = databaseA.changeQueueDao(),
                nimelisEventBus = eventBusA,
                syncCoordinator = syncCoordinatorA
            )

        RealtimeApplyStalenessScenario()
            .run(
                deviceA = deviceA,
                sharedGateway = sharedGateway
            )
    }
}