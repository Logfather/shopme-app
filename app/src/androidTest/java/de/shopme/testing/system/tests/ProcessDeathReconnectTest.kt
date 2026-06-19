package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProcessDeathReconnectTest {

    private lateinit var database:
            ShopMeDatabase

    val telemetry = SyncTelemetryCollector()


    @Before
    fun setup() {

        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                ShopMeDatabase::class.java
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun staleReconnectSnapshot_isIgnoredAfterRestart() =
        runBlocking {

            // --------------------------------------------------------
            // FIRST RUNTIME
            // --------------------------------------------------------

            val coordinatorA =
                RemoteApplyCoordinator(
                    itemDao = database.itemDao(),
                    changeQueueDao = database.changeQueueDao(),
                    remoteApplyStateDao =
                        database.remoteApplyStateDao(),
                    telemetry = telemetry
                )

            val newest =
                ShoppingItemEntity(
                    id = "restart-item",
                    listId = "list-1",
                    name = "Newest",
                    quantity = 1,
                    category = "Test",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 100L,
                    updatedAt = 999L
                )

            coordinatorA.applyRemoteItem(
                newest
            )

            // --------------------------------------------------------
            // SIMULATED PROCESS DEATH
            // --------------------------------------------------------

            val coordinatorB =
                RemoteApplyCoordinator(
                    itemDao = database.itemDao(),
                    changeQueueDao = database.changeQueueDao(),
                    remoteApplyStateDao =
                        database.remoteApplyStateDao(),
                    telemetry = telemetry
                )

            // --------------------------------------------------------
            // STALE RECONNECT SNAPSHOT
            // --------------------------------------------------------

            val stale =
                ShoppingItemEntity(
                    id = "restart-item",
                    listId = "list-1",
                    name = "STALE",
                    quantity = 1,
                    category = "Test",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 100L,
                    updatedAt = 100L
                )

            coordinatorB.applyRemoteItem(
                stale
            )

            // --------------------------------------------------------
            // VERIFY
            // --------------------------------------------------------

            val final =
                database.itemDao()
                    .getById("restart-item")
                    ?: throw AssertionError(
                        "Missing item"
                    )

            assertEquals(
                "Newest",
                final.name
            )

            assertEquals(
                999L,
                final.updatedAt
            )
        }
}