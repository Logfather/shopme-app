package de.shopme.runtime

import android.app.Application
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.invite.PendingInviteStore
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.FirestoreListener
import de.shopme.data.sync.ReplayCompletionNotifier
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.SyncScheduler
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.orchestrator.SyncRuntimeOrchestrator
import de.shopme.domain.life.NimelisEventBus
import de.shopme.domain.life.processor.NimelisLoggingProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class HivraRuntime(
    private val application: Application
) {

    // ------------------------------------------------------------
    // GLOBAL RUNTIME SCOPE
    // ------------------------------------------------------------

    val runtimeScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    val syncScheduler = SyncScheduler(application)



    // ------------------------------------------------------------
    // EVENT BUS
    // ------------------------------------------------------------

    val nimelisEventBus = NimelisEventBus()

    private val loggingProcessor = NimelisLoggingProcessor(
        eventBus = nimelisEventBus,
        scope = runtimeScope
    )

    // ------------------------------------------------------------
    // DATABASE
    // ------------------------------------------------------------

    val database: ShopMeDatabase by lazy {

        Room.databaseBuilder(
            application,
            ShopMeDatabase::class.java,
            "shopme_database"
        )
            .addMigrations(
                ShopMeDatabase.MIGRATION_4_5
            )
            .build()
    }

    // ------------------------------------------------------------
    // DAOS
    // ------------------------------------------------------------

    val itemDao by lazy {
        database.itemDao()
    }

    val listDao by lazy {
        database.listDao()
    }

    val changeQueueDao by lazy {
        database.changeQueueDao()
    }

    // ------------------------------------------------------------
    // FIRESTORE
    // ------------------------------------------------------------

    val firestoreGateway: FirestoreGateway by lazy {
        FirestoreDataSource()
    }

    // ------------------------------------------------------------
    // SYNC
    // ------------------------------------------------------------

    val conflictResolver by lazy {
        ConflictResolver()
    }

    val roomRepository by lazy {

        RoomShoppingRepository(
            itemDao = itemDao,
            listDao = listDao,
            changeQueueDao = changeQueueDao,
            firestoreDataSource = firestoreGateway,
            nimelisEventBus = nimelisEventBus
        )
    }

    val firestoreListener =
        FirestoreListener(
            dataSource = firestoreGateway,
            itemDao = itemDao,
            listDao = listDao,
            conflictResolver = conflictResolver,
            appScope = runtimeScope
        )

    val syncCoordinator by lazy {

        SyncCoordinator(
            changeQueueDao = changeQueueDao,
            itemDao = itemDao,
            listDao = listDao,
            firestore = firestoreGateway,
            appScope = runtimeScope,
            firebaseAuth = FirebaseAuth.getInstance(),
            conflictResolver = conflictResolver,
            roomRepository = roomRepository
        )
    }

    val syncRuntimeOrchestrator =
        SyncRuntimeOrchestrator(
            syncScheduler = syncScheduler
        )



    val replayCompletionNotifier:
            ReplayCompletionNotifier =
        syncRuntimeOrchestrator

    // ------------------------------------------------------------
    // STARTUP
    // ------------------------------------------------------------

    fun start() {

        RuntimeLog.runtime(
            "Starting runtime"
        )
        loggingProcessor.start()

        roomRepository.attachSyncCoordinator(syncCoordinator)

        RuntimeLog.runtime(
            "Runtime started"
        )

        syncRuntimeOrchestrator.onStartup()
    }

    fun startUserSync(
        userId: String
    ) {

        RuntimeLog.runtime(
            "Start user sync | uid=$userId"
        )

        firestoreListener.startListSync(
            userId
        )
    }

    fun stopUserSync() {

        RuntimeLog.runtime(
            "Stop user sync"
        )

        firestoreListener.stop()
    }

    val pendingInviteStore =
        PendingInviteStore(application)
}