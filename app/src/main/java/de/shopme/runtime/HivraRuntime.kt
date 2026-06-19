package de.shopme.runtime

import android.app.Application
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import de.shopme.app.services.AppServices
import de.shopme.app.services.CatalogServices
import de.shopme.app.services.NutritionServices
import de.shopme.app.services.ShopBuddyServices
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.invite.PendingInviteStore
import de.shopme.data.nutrition.remote.OpenFoodFactsDataSource
import de.shopme.data.nutrition.repository.NutritionRepositoryImpl
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.FirestoreListener
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.ReplayCompletionNotifier
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.SyncScheduler
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.orchestrator.SyncRuntimeOrchestrator
import de.shopme.data.sync.runtime.ListenerActivationRegistry
import de.shopme.data.sync.runtime.SyncBootstrapper
import de.shopme.data.sync.runtime.SyncRuntimeStateHolder
import de.shopme.data.sync.telemetry.HistoricalRuntimeAnalyzer
import de.shopme.data.sync.telemetry.InMemoryRuntimeSnapshotStore
import de.shopme.data.sync.telemetry.RuntimeHealthEvaluator
import de.shopme.data.sync.telemetry.RuntimeHealthMonitor
import de.shopme.data.sync.telemetry.RuntimeIncidentDetector
import de.shopme.data.sync.telemetry.RuntimeIncidentTimeline
import de.shopme.data.sync.telemetry.RuntimeReliabilityAnalyzer
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsLogger
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsProvider
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.food.GeneratedFoodKnowledgeLoader
import de.shopme.domain.food.InMemoryFoodKnowledgeLookup
import de.shopme.domain.food.LocalFoodClassifier
import de.shopme.domain.life.NimelisEventBus
import de.shopme.domain.life.processor.NimelisLoggingProcessor
import de.shopme.domain.nutrition.pipeline.ProductionNutritionPipeline
import de.shopme.domain.nutrition.service.NutritionResolverImpl
import de.shopme.domain.nutrition.service.NutritionWarmupServiceImpl
import de.shopme.domain.nutrition.service.RuleBasedNutritionInsightService
import de.shopme.domain.nutrition.test.NutritionProductionPipelineTestRunner
import de.shopme.domain.recommendation.RecommendationAggregator
import de.shopme.domain.recommendation.RuleBasedRecommendationGenerator
import de.shopme.domain.recommendation.RuleBasedRuleFormatter
import de.shopme.domain.recommendation.nutrition.RuleBasedFatAnalyzer
import de.shopme.domain.recommendation.nutrition.RuleBasedNutritionAnalyzer
import de.shopme.domain.recommendation.nutrition.RuleBasedProteinAnalyzer
import de.shopme.domain.recommendation.nutrition.RuleBasedSaltAnalyzer
import de.shopme.domain.recommendation.nutrition.RuleBasedSugarAnalyzer
import de.shopme.domain.recommendation.pattern.RuleBasedPatternAnalyzer
import de.shopme.domain.recommendation.score.SimpleScoreCalculator
import de.shopme.domain.recommendation.shopping.RuleBasedShoppingAnalyzer
import de.shopme.domain.recommendation.shopping.ShoppingBalanceRule
import de.shopme.domain.recommendation.shopping.ShoppingCategoryCounter
import de.shopme.domain.recommendation.statistics.counter.FruitStatisticsCounter
import de.shopme.domain.recommendation.statistics.counter.ProcessedFoodStatisticsCounter
import de.shopme.domain.recommendation.statistics.counter.ProteinStatisticsCounter
import de.shopme.domain.recommendation.statistics.counter.VegetableStatisticsCounter
import de.shopme.domain.shopbuddy.DefaultShopBuddyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HivraRuntime(
    private val application: Application
) {

    // ------------------------------------------------------------
    // NUTRITION ENGINE
    // ------------------------------------------------------------

    val sugarAnalyzer by lazy {

        RuleBasedSugarAnalyzer()

    }

    val fatAnalyzer by lazy {

        RuleBasedFatAnalyzer()

    }

    val saltAnalyzer by lazy {

        RuleBasedSaltAnalyzer()

    }

    val proteinAnalyzer by lazy {

        RuleBasedProteinAnalyzer()

    }

    val scoreCalculator by lazy {

        SimpleScoreCalculator()

    }

    val nutritionAnalyzer by lazy {

        RuleBasedNutritionAnalyzer(

            sugarAnalyzer = sugarAnalyzer,

            fatAnalyzer = fatAnalyzer,

            saltAnalyzer = saltAnalyzer,

            proteinAnalyzer = proteinAnalyzer,

            scoreCalculator = scoreCalculator

        )

    }

    val fruitCounter by lazy {

        FruitStatisticsCounter(

            foodClassifier = foodClassifier

        )

    }

    val vegetableCounter by lazy {

        VegetableStatisticsCounter(

            foodClassifier = foodClassifier

        )

    }

    val proteinCounter by lazy {

        ProteinStatisticsCounter(

            foodClassifier = foodClassifier

        )

    }

    val processedFoodCounter by lazy {

        ProcessedFoodStatisticsCounter(

            foodClassifier = foodClassifier

        )

    }

    // ------------------------------------------------------------
    // RECOMMENDATION ENGINE
    // ------------------------------------------------------------

    val shoppingCategoryCounter by lazy {

        ShoppingCategoryCounter()

    }

    private val foodKnowledgeLoader by lazy {

        GeneratedFoodKnowledgeLoader()

    }

    val foodKnowledgeLookup by lazy {

        InMemoryFoodKnowledgeLookup(

            entries =
                foodKnowledgeLoader.load()

        )

    }

    val foodClassifier by lazy {

        LocalFoodClassifier(

            foodKnowledge = foodKnowledgeLookup

        )

    }

    val shoppingBalanceRule by lazy {

        ShoppingBalanceRule()

    }

    val shoppingAnalyzer by lazy {

        RuleBasedShoppingAnalyzer(

            categoryCounter = shoppingCategoryCounter,

            fruitCounter = fruitCounter,

            vegetableCounter = vegetableCounter,

            shoppingBalanceRule = shoppingBalanceRule

        )

    }

    val recommendationAggregator by lazy {

        RecommendationAggregator()

    }

    val patternAnalyzer by lazy {

        RuleBasedPatternAnalyzer()

    }

    val recommendationGenerator by lazy {

        RuleBasedRecommendationGenerator(

            nutritionAnalyzer = nutritionAnalyzer,

            shoppingAnalyzer = shoppingAnalyzer,

            patternAnalyzer = patternAnalyzer,

            recommendationAggregator = recommendationAggregator

        )

    }

    val ruleFormatter by lazy {

        RuleBasedRuleFormatter()

    }

    val shopBuddyService by lazy {

        DefaultShopBuddyService(

            recommendationGenerator =
                recommendationGenerator,

            ruleFormatter =
                ruleFormatter

        )

    }

    // ------------------------------------------------------------
    // OPEN FOOD TEST
    // ------------------------------------------------------------


    val nutritionRepository by lazy {

        NutritionRepositoryImpl(
            dao = database.nutritionDao(),
            mappingDao =
                database.nutritionReferenceMappingDao(),
            remote = OpenFoodFactsDataSource()
        )
    }

    val nutritionResolver by lazy {

        NutritionResolverImpl(

            repository = nutritionRepository

        )
    }

    val nutritionPipelineTestRunner by lazy {

        NutritionProductionPipelineTestRunner(

            pipeline = productionNutritionPipeline

        )
    }

    val productionNutritionPipeline by lazy {

        ProductionNutritionPipeline(

            repository = nutritionRepository,

            resolver = nutritionResolver

        )
    }

    val nutritionWarmupService by lazy {

        NutritionWarmupServiceImpl(

            pipeline = productionNutritionPipeline

        )
    }

    val nutritionInsightService by lazy {

        RuleBasedNutritionInsightService()

    }



    // ------------------------------------------------------------
    // GLOBAL RUNTIME SCOPE
    // ------------------------------------------------------------

    val runtimeScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    val syncScheduler = SyncScheduler(application)

    val telemetry = SyncTelemetryCollector()

    val healthMonitor =
        RuntimeHealthMonitor(
            telemetry = telemetry,
            evaluator =
                RuntimeHealthEvaluator()
        )

    val diagnosticsProvider =
        SyncRuntimeDiagnosticsProvider(

            telemetry = telemetry,

            healthMonitor = healthMonitor,

            reliabilityAnalyzer =
                RuntimeReliabilityAnalyzer(),

            incidentDetector =
                RuntimeIncidentDetector(),

            snapshotStore =
                InMemoryRuntimeSnapshotStore(),

            historicalAnalyzer =
                HistoricalRuntimeAnalyzer(),

            timeline =
                RuntimeIncidentTimeline()
        )

    val diagnosticsLogger =
        SyncRuntimeDiagnosticsLogger()



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
                ShopMeDatabase.MIGRATION_5_6,
                ShopMeDatabase.MIGRATION_6_7,
                ShopMeDatabase.MIGRATION_7_8
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

    val firestoreListener by lazy {

        FirestoreListener(
            dataSource = firestoreGateway,
            itemDao = itemDao,
            listDao = listDao,
            conflictResolver = conflictResolver,
            appScope = runtimeScope
        )
    }

    val syncCoordinator by lazy {

        val remoteApplyCoordinator =
            RemoteApplyCoordinator(
                itemDao = itemDao,
                changeQueueDao = changeQueueDao,
                remoteApplyStateDao =
                    database.remoteApplyStateDao(),
                telemetry = telemetry
            )

        val coordinator =
            SyncCoordinator(
                changeQueueDao = changeQueueDao,
                itemDao = itemDao,
                listDao = listDao,
                firestore = firestoreGateway,
                appScope = runtimeScope,
                firebaseAuth = FirebaseAuth.getInstance(),
                conflictResolver = conflictResolver,
                roomRepository = roomRepository,
                remoteApplyCoordinator =
                    remoteApplyCoordinator,
                telemetry = telemetry,

                diagnosticsProvider =
                    diagnosticsProvider,

                diagnosticsLogger =
                    diagnosticsLogger
            )

        roomRepository.attachSyncCoordinator(coordinator)

        coordinator
    }

    val syncRuntimeOrchestrator =
        SyncRuntimeOrchestrator(
            syncScheduler = syncScheduler
        )

    val runtimeStateHolder by lazy {
        SyncRuntimeStateHolder()
    }

    val listenerRegistry by lazy {
        ListenerActivationRegistry()
    }

    val syncBootstrapper by lazy {

        SyncBootstrapper(
            syncCoordinator = syncCoordinator,
            changeQueueDao = changeQueueDao,
            runtimeStateHolder = runtimeStateHolder,
            listenerRegistry = listenerRegistry,
            firestoreListener = firestoreListener
        )
    }



    val replayCompletionNotifier:
            ReplayCompletionNotifier =
        syncRuntimeOrchestrator

    // ------------------------------------------------------------
    // STARTUP
    // ------------------------------------------------------------

    fun start() {

        firestoreListener.bootstrapper =
            syncBootstrapper

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

        runtimeScope.launch {

            nutritionPipelineTestRunner.run()
        }
    }

    fun stopUserSync() {

        firestoreListener.stop()
    }

    val pendingInviteStore =
        PendingInviteStore(application)

    val appServices by lazy {

        AppServices(

            catalog = CatalogServices(),

            nutrition = NutritionServices(

                pipeline = productionNutritionPipeline,

                insightService = nutritionInsightService

            ),

            shopBuddy = ShopBuddyServices(

                nutritionInsightService = nutritionInsightService

            )

        )

    }
}