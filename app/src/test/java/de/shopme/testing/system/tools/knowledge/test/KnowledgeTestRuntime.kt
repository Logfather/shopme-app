package de.shopme.testing.system.tools.knowledge.test

object KnowledgeTestRuntime {

    private var testCounter = 0

    fun suiteStart() {

        testCounter = 0

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 KNOWLEDGE TEST SUITE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

    fun suiteFinish() {

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🎉 KNOWLEDGE TEST SUITE COMPLETED")
        println("📊 Executed Tests : $testCounter")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

    fun start(name: String) {

        testCounter++

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 KNOWLEDGE TEST")
        println(name)

    }

    fun info(message: String) {

        println("ℹ️  $message")

    }

    fun success(message: String) {

        println("✅ $message")

    }

    fun warning(message: String) {

        println("⚠️  $message")

    }

    fun error(message: String) {

        println("❌ $message")

    }

    fun finish() {

        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

    fun statistic(

        label: String,

        value: Any

    ) {

        println(

            "📊 $label : $value"

        )

    }



}