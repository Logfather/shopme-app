package de.shopme.data.sync

import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.sync.logging.SyncLog
import de.shopme.data.sync.queue.ChangeQueueDao
import de.shopme.data.sync.remote.RemoteApplyStateDao
import de.shopme.data.sync.remote.RemoteApplyStateEntity
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.data.sync.telemetry.SyncTelemetryEvent
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RemoteApplyCoordinator(
    private val itemDao: ItemDao,
    private val changeQueueDao: ChangeQueueDao,
    private val remoteApplyStateDao:
    RemoteApplyStateDao,
    private val telemetry: SyncTelemetryCollector

) {

    // ============================================================
    // ENTITY APPLY SERIALIZATION
    // ============================================================

    private val entityMutexes =
        mutableMapOf<String, Mutex>()

    // ============================================================
    // PUBLIC API
    // ============================================================

    suspend fun applyRemoteItem(
        remote: ShoppingItemEntity
    ) {

        val mutex =
            entityMutexes.getOrPut(remote.id) {
                Mutex()
            }

        mutex.withLock {

            SyncLog.realtime(
                "[Apply] ENTER id=${remote.id} updatedAt=${remote.updatedAt}"
            )

            // ============================================================
            // GUARD — INVALID VERSION
            // ============================================================

            if (remote.updatedAt <= 0L) {

                SyncLog.realtime(
                    "[Guard] INVALID updatedAt id=${remote.id}"
                )

                return
            }

            // ============================================================
            // LOAD STATE
            // ============================================================

            val local =
                itemDao.getById(remote.id)

            val activeMutations =
                changeQueueDao.getActiveByEntityId(remote.id)

            val applyState =
                remoteApplyStateDao
                    .getState(remote.id)

            val lastAppliedVersion =
                applyState?.lastAppliedRemoteVersion

            // ============================================================
            // GUARD — STALE / DUPLICATE REMOTE SNAPSHOT
            // ============================================================

            if (
                lastAppliedVersion != null &&
                remote.updatedAt <= lastAppliedVersion
            ) {

                SyncLog.realtime(
                    "[Guard] STALE_REMOTE id=${remote.id} remote=${remote.updatedAt} last=$lastAppliedVersion"
                )

                telemetry.emit(
                    SyncTelemetryEvent.StaleRemoteDiscarded(
                        entityId = remote.id
                    )
                )

                return
            }

            // ============================================================
            // GUARD — IDENTICAL ENTITY
            // ============================================================

            if (
                local != null &&
                remote.updatedAt == local.updatedAt &&
                remote.deletedAt == local.deletedAt &&
                remote.name == local.name &&
                remote.quantity == local.quantity &&
                remote.category == local.category &&
                remote.isChecked == local.isChecked
            ) {

                SyncLog.realtime(
                    "[Guard] IDENTICAL skip id=${remote.id}"
                )

                remoteApplyStateDao.upsert(
                    RemoteApplyStateEntity(
                        entityId = remote.id,
                        lastAppliedRemoteVersion = remote.updatedAt,
                        lastAppliedAt = System.currentTimeMillis()
                    )
                )

                return
            }

            // ============================================================
            // GUARD — LOCAL PENDING PROTECTION
            // ============================================================

            if (
                activeMutations.isNotEmpty() &&
                local != null &&
                local.updatedAt >= remote.updatedAt
            ) {

                SyncLog.realtime(
                    "[Guard] LOCAL_PENDING_PROTECTION id=${remote.id} local=${local.updatedAt} remote=${remote.updatedAt}"
                )

                return
            }

            // ============================================================
            // DELETE HAS ABSOLUTE PRIORITY
            // ============================================================

            if (remote.deletedAt != null) {

                SyncLog.apply(
                    "[Delete] APPLY REMOTE id=${remote.id}"
                )

                itemDao.upsert(remote)

                remoteApplyStateDao.upsert(
                    RemoteApplyStateEntity(
                        entityId = remote.id,
                        lastAppliedRemoteVersion = remote.updatedAt,
                        lastAppliedAt = System.currentTimeMillis()
                    )
                )

                return
            }

            // ============================================================
            // INSERT
            // ============================================================

            if (local == null) {

                SyncLog.apply(
                    "[Insert] APPLY REMOTE id=${remote.id}"
                )

                itemDao.upsert(remote)

                remoteApplyStateDao.upsert(
                    RemoteApplyStateEntity(
                        entityId = remote.id,
                        lastAppliedRemoteVersion = remote.updatedAt,
                        lastAppliedAt = System.currentTimeMillis()
                    )
                )

                return
            }

            // ============================================================
            // REMOTE NEWER
            // ============================================================

            if (remote.updatedAt > local.updatedAt) {

                SyncLog.apply(
                    "[Apply] REMOTE_NEWER id=${remote.id} remote=${remote.updatedAt} local=${local.updatedAt}"
                )

                itemDao.upsert(remote)

                remoteApplyStateDao.upsert(
                    RemoteApplyStateEntity(
                        entityId = remote.id,
                        lastAppliedRemoteVersion = remote.updatedAt,
                        lastAppliedAt = System.currentTimeMillis()
                    )
                )

                telemetry.emit(
                    SyncTelemetryEvent.RemoteNewerApplied(
                        entityId = remote.id
                    )
                )

                return
            }

            // ============================================================
            // LOCAL NEWER
            // ============================================================

            if (remote.updatedAt < local.updatedAt) {

                SyncLog.realtime(
                    "[Guard] LOCAL_NEWER keep local id=${remote.id} local=${local.updatedAt} remote=${remote.updatedAt}"
                )

                return
            }

            // ============================================================
            // SAME VERSION BUT DIFFERENT CONTENT
            // ============================================================

            if (remote != local) {

                SyncLog.conflict(
                    "[Repair] SAME_VERSION_DIFFERENT_PAYLOAD id=${remote.id}"
                )

                itemDao.upsert(remote)

                remoteApplyStateDao.upsert(
                    RemoteApplyStateEntity(
                        entityId = remote.id,
                        lastAppliedRemoteVersion = remote.updatedAt,
                        lastAppliedAt = System.currentTimeMillis()
                    )
                )

                return
            }

            // ============================================================
            // NO-OP
            // ============================================================

            SyncLog.realtime(
                "[Resolve] NO_OP id=${remote.id}"
            )

            remoteApplyStateDao.upsert(
                RemoteApplyStateEntity(
                    entityId = remote.id,
                    lastAppliedRemoteVersion = remote.updatedAt,
                    lastAppliedAt = System.currentTimeMillis()
                )
            )
        }
    }
}