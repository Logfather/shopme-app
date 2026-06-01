package de.shopme.testing.system.scenario

import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.sync.queue.ChangeQueueEntity
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.system.fake.FakeFirestoreGateway
import java.util.UUID

class EventDrivenReplayScenario(
    private val database: ShopMeDatabase,
    private val firestore: FakeFirestoreGateway
) {

    suspend fun enqueueCreateItem(): ShoppingItemEntity {

        val now =
            System.currentTimeMillis()

        val item =
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = "list-1",
                name = "Milk",
                quantity = 1,
                category = "Food",
                isChecked = false,
                deletedAt = null,
                createdAt = now,
                updatedAt = now
            )

        database
            .itemDao()
            .upsert(item)

        database
            .changeQueueDao()
            .insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = item.id,
                    listId = item.listId,
                    operation = "CREATE",
                    payload = null,
                    createdAt = now,
                    state = "PENDING",
                    retryCount = 0,
                    lastAttemptAt = null,
                    nextRetryAt = null,
                    progress = null,
                    errorMessage = null,
                    baseVersion = item.updatedAt
                )
            )

        return item
    }

    suspend fun enqueueCreateUpdateDeleteSequence(): ShoppingItemEntity {

        val created =
            enqueueCreateItem()

        val updateTime =
            created.updatedAt + 1000

        val updated =
            created.copy(
                name = "Coffee Premium",
                updatedAt = updateTime
            )

        database
            .itemDao()
            .upsert(updated)

        database
            .changeQueueDao()
            .insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = updated.id,
                    listId = updated.listId,
                    operation = "UPDATE",
                    payload = null,
                    createdAt = updateTime,
                    state = "PENDING",
                    retryCount = 0,
                    lastAttemptAt = null,
                    nextRetryAt = null,
                    progress = null,
                    errorMessage = null,
                    baseVersion = created.updatedAt
                )
            )

        val deleteTime =
            updateTime + 1000

        val deleted =
            updated.copy(
                deletedAt = deleteTime,
                updatedAt = deleteTime
            )

        database
            .itemDao()
            .upsert(deleted)

        database
            .changeQueueDao()
            .insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = deleted.id,
                    listId = deleted.listId,
                    operation = "DELETE",
                    payload = null,
                    createdAt = deleteTime,
                    state = "PENDING",
                    retryCount = 0,
                    lastAttemptAt = null,
                    nextRetryAt = null,
                    progress = null,
                    errorMessage = null,
                    baseVersion = updated.updatedAt
                )
            )

        return deleted
    }

    suspend fun enqueueRetryItem(): ShoppingItemEntity {

        val now =
            System.currentTimeMillis()

        val item =
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = "missing-list",
                name = "Broken",
                quantity = 1,
                category = "Debug",
                isChecked = false,
                deletedAt = null,
                createdAt = now,
                updatedAt = now
            )

        database
            .itemDao()
            .upsert(item)

        database
            .changeQueueDao()
            .insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = item.id,
                    listId = item.listId,
                    operation = "CREATE",
                    payload = null,
                    createdAt = now,
                    state = "PENDING",
                    retryCount = 0,
                    lastAttemptAt = null,
                    nextRetryAt = null,
                    progress = null,
                    errorMessage = null,
                    baseVersion = item.updatedAt
                )
            )

        firestore.shouldFailWrites = true

        return item
    }
}