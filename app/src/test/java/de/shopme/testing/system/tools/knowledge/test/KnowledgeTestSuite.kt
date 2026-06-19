package de.shopme.testing.system.tools.knowledge.test

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(

    AllergenKnowledgeTest::class,
    AnimalWelfareKnowledgeTest::class,
    BiodiversityKnowledgeTest::class,
    CarbonKnowledgeTest::class,
    FairTradeKnowledgeTest::class,
    GlycemicIndexKnowledgeTest::class,
    LocalityKnowledgeTest::class,
    NutritionKnowledgeTest::class,
    PackagingKnowledgeTest::class,
    PesticideKnowledgeTest::class,
    PollinatorKnowledgeTest::class,
    ProcessingKnowledgeTest::class,
    WaterKnowledgeTest::class

)
class KnowledgeTestSuite {

    companion object {

        @BeforeClass
        @JvmStatic
        fun start() {

            KnowledgeTestRuntime.suiteStart()

        }

        @AfterClass
        @JvmStatic
        fun finish() {

            KnowledgeTestRuntime.suiteFinish()

        }

    }

}