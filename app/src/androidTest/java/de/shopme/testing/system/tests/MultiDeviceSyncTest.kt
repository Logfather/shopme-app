package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.life.NimelisEventBus
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.scenario.MultiDeviceSyncScenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultiDeviceSyncTest {

    private lateinit var databaseA: ShopMeDatabase
    private lateinit var databaseB: ShopMeDatabase

    private lateinit var deviceA: MultiDeviceContextTest
    private lateinit var deviceB: MultiDeviceContextTest

    @Before
    fun setup() {

        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        // ============================================================
        // SHARED REMOTE
        // ============================================================

        val sharedGateway =
            InMemoryFakeFirestoreGateway()

        // ============================================================
        // DEVICE A
        // ============================================================

        databaseA = Room.inMemoryDatabaseBuilder(
            context,
            ShopMeDatabase::class.java
        ).allowMainThreadQueries().build()

        val eventBusA = NimelisEventBus()

        val repositoryA = RoomShoppingRepository(
            itemDao = databaseA.itemDao(),
            listDao = databaseA.listDao(),
            changeQueueDao = databaseA.changeQueueDao(),
            firestoreDataSource = sharedGateway,
            nimelisEventBus = eventBusA
        )
        val testScope =
            CoroutineScope(
                SupervisorJob() + Dispatchers.IO
            )

        val syncCoordinatorA = SyncCoordinator(
            changeQueueDao = databaseA.changeQueueDao(),
            itemDao = databaseA.itemDao(),
            listDao = databaseA.listDao(),
            firestore = sharedGateway,
            appScope = testScope,
            firebaseAuth = FirebaseAuth.getInstance(),
            conflictResolver = ConflictResolver(),
            roomRepository = repositoryA,
            syncDebounceMs = 0
        )

        repositoryA.attachSyncCoordinator(syncCoordinatorA)

        deviceA = MultiDeviceContextTest(
            deviceName = "DeviceA",
            roomRepository = repositoryA,
            itemDao = databaseA.itemDao(),
            listDao = databaseA.listDao(),
            changeQueueDao = databaseA.changeQueueDao(),
            nimelisEventBus = eventBusA,
            syncCoordinator = syncCoordinatorA
        )

        // ============================================================
        // DEVICE B
        // ============================================================

        databaseB = Room.inMemoryDatabaseBuilder(
            context,
            ShopMeDatabase::class.java
        ).allowMainThreadQueries().build()

        val eventBusB = NimelisEventBus()

        val repositoryB = RoomShoppingRepository(
            itemDao = databaseB.itemDao(),
            listDao = databaseB.listDao(),
            changeQueueDao = databaseB.changeQueueDao(),
            firestoreDataSource = sharedGateway,
            nimelisEventBus = eventBusB
        )

        val syncCoordinatorB = SyncCoordinator(
            changeQueueDao = databaseB.changeQueueDao(),
            itemDao = databaseB.itemDao(),
            listDao = databaseB.listDao(),
            firestore = sharedGateway,
            appScope = testScope,
            firebaseAuth = FirebaseAuth.getInstance(),
            conflictResolver = ConflictResolver(),
            roomRepository = repositoryB
        )

        repositoryB.attachSyncCoordinator(syncCoordinatorB)

        deviceB = MultiDeviceContextTest(
            deviceName = "DeviceB",
            roomRepository = repositoryB,
            itemDao = databaseB.itemDao(),
            listDao = databaseB.listDao(),
            changeQueueDao = databaseB.changeQueueDao(),
            nimelisEventBus = eventBusB,
            syncCoordinator = syncCoordinatorB
        )
    }

    @After
    fun teardown() {
        databaseA.close()
        databaseB.close()
    }

    @Test
    fun multiDeviceSyncScenario_runsSuccessfully() = runTest {

        MultiDeviceSyncScenario().run(
            deviceA = deviceA,
            deviceB = deviceB
        )
    }
}