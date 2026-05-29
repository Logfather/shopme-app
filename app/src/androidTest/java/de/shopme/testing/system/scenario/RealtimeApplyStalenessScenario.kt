package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class RealtimeApplyStalenessScenario {

    suspend fun run(
        deviceA: MultiDeviceContextTest,
        sharedGateway: InMemoryFakeFirestoreGateway
    ) {

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )

        Log.d(
            "HIVRA_TEST",
            "Starting RealtimeApplyStalenessScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Create initial item
        // ------------------------------------------------

        val initialTimestamp =
            System.currentTimeMillis()

        val initialItem =
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = "shared-list",
                name = "Milk",
                quantity = 1,
                category = "Realtime",
                isChecked = false,
                deletedAt = null,
                createdAt = initialTimestamp,
                updatedAt = initialTimestamp
            )

        deviceA.roomRepository
            .createItem(initialItem)

        delay(1500)

        Log.d(
            "HIVRA_TEST",
            "Initial item created"
        )

        // ------------------------------------------------
        // STEP 2
        // Local update becomes newer
        // ------------------------------------------------

        val localUpdated =
            initialItem.copy(
                name = "Milk_LOCAL_NEWER",
                quantity = 99,
                updatedAt = System.currentTimeMillis() + 5000
            )

        deviceA.itemDao
            .upsert(localUpdated)

        Log.d(
            "HIVRA_TEST",
            "Applied LOCAL newer state updatedAt=${localUpdated.updatedAt}"
        )

        // ------------------------------------------------
        // STEP 3
        // Simulate stale remote snapshot
        // ------------------------------------------------

        val staleRemote =
            initialItem.copy(
                name = "Milk_REMOTE_STALE",
                quantity = 1,
                updatedAt = initialTimestamp
            )

        sharedGateway.updateItem(
            listId = staleRemote.listId,
            item = staleRemote
        )

        Log.d(
            "HIVRA_TEST",
            "Injected STALE remote snapshot updatedAt=${staleRemote.updatedAt}"
        )

        // ------------------------------------------------
        // STEP 4
        // Wait for realtime listener apply
        // ------------------------------------------------

        delay(3000)

        // ------------------------------------------------
        // STEP 5
        // Validate stale remote was ignored
        // ------------------------------------------------

        val finalLocal =
            deviceA.itemDao
                .getById(initialItem.id)
                ?: error("Final local item missing")

        Log.d(
            "HIVRA_TEST",
            "FINAL local name=${finalLocal.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL local quantity=${finalLocal.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL local updatedAt=${finalLocal.updatedAt}"
        )

        val staleIgnored =
            finalLocal.name == "Milk_LOCAL_NEWER" &&
                    finalLocal.quantity == 99

        Log.d(
            "HIVRA_TEST",
            "STALE REMOTE IGNORED=$staleIgnored"
        )

        check(staleIgnored) {
            "Realtime stale remote overwrote newer local state"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished RealtimeApplyStalenessScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}