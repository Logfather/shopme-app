package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.life.NimelisEventBus
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.scenarios.RealtimeListenerOverwriteScenario
import de.shopme.testing.system.tests.sync.TestRuntimeDiagnostics
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealtimeListenerOverwriteTest {

    private lateinit var databaseA: ShopMeDatabase
    private lateinit var databaseB: ShopMeDatabase

    @Before
    fun setup() {

        databaseA = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShopMeDatabase::class.java
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

        databaseB = Room.inMemoryDatabaseBuilder(
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
        databaseB.close()
    }

    @Test
    fun realtimeListenerOverwriteScenario_runsSuccessfully() = runBlocking<Unit> {

        val sharedGateway =
            InMemoryFakeFirestoreGateway()

        val eventBusA =
            NimelisEventBus()

        val eventBusB =
            NimelisEventBus()

        val repositoryA =
            RoomShoppingRepository(
                itemDao = databaseA.itemDao(),
                listDao = databaseA.listDao(),
                changeQueueDao = databaseA.changeQueueDao(),
                firestoreDataSource = sharedGateway,
                nimelisEventBus = eventBusA
            )

        val repositoryB =
            RoomShoppingRepository(
                itemDao = databaseB.itemDao(),
                listDao = databaseB.listDao(),
                changeQueueDao = databaseB.changeQueueDao(),
                firestoreDataSource = sharedGateway,
                nimelisEventBus = eventBusB
            )

        val telemetry = SyncTelemetryCollector()

        val remoteApplyCoordinatorA =
            RemoteApplyCoordinator(
                itemDao = databaseA.itemDao(),
                changeQueueDao = databaseA.changeQueueDao(),
                remoteApplyStateDao =
                    databaseA.remoteApplyStateDao(),
                telemetry = telemetry
            )

        val syncCoordinatorA =
            SyncCoordinator(
                changeQueueDao = databaseA.changeQueueDao(),
                itemDao = databaseA.itemDao(),
                listDao = databaseA.listDao(),
                firestore = sharedGateway,
                appScope = this,
                firebaseAuth = null,
                conflictResolver = ConflictResolver(),
                roomRepository = repositoryA,
                remoteApplyCoordinator = remoteApplyCoordinatorA,
                telemetry = telemetry,

                diagnosticsProvider =
                    TestRuntimeDiagnostics.provider(
                        telemetry
                    ),

                diagnosticsLogger =
                    TestRuntimeDiagnostics.logger()
            )

        val remoteApplyCoordinatorB =
            RemoteApplyCoordinator(
                itemDao = databaseB.itemDao(),
                changeQueueDao = databaseB.changeQueueDao(),
                remoteApplyStateDao =
                    databaseB.remoteApplyStateDao(),
                telemetry = telemetry
            )

        val syncCoordinatorB =
            SyncCoordinator(
                changeQueueDao = databaseB.changeQueueDao(),
                itemDao = databaseB.itemDao(),
                listDao = databaseB.listDao(),
                firestore = sharedGateway,
                appScope = this,
                firebaseAuth = null,
                conflictResolver = ConflictResolver(),
                roomRepository = repositoryB,
                remoteApplyCoordinator = remoteApplyCoordinatorB,
                telemetry = telemetry,

                diagnosticsProvider =
                    TestRuntimeDiagnostics.provider(
                        telemetry
                    ),

                diagnosticsLogger =
                    TestRuntimeDiagnostics.logger()
            )

        repositoryA.attachSyncCoordinator(syncCoordinatorA)
        repositoryB.attachSyncCoordinator(syncCoordinatorB)

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

        val deviceB =
            MultiDeviceContextTest(
                deviceName = "DeviceB",
                roomRepository = repositoryB,
                itemDao = databaseB.itemDao(),
                listDao = databaseB.listDao(),
                changeQueueDao = databaseB.changeQueueDao(),
                nimelisEventBus = eventBusB,
                syncCoordinator = syncCoordinatorB
            )

        RealtimeListenerOverwriteScenario()
            .run(
                deviceA = deviceA,
                deviceB = deviceB,
                sharedGateway = sharedGateway
            )
    }
}