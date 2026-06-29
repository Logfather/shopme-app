package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.off.OFFKnowledgeImportCandidateBuilder
import de.shopme.tools.knowledge.off.OFFKnowledgeProposalApplyPlan
import org.junit.Test
import java.io.File

class OFFKnowledgeImportCandidateExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportImportCandidates() {

        val applyPlanFile =
            File(
                "build/off/off_knowledge_apply_plan.json"
            )

        val outputFile =
            File(
                "build/off/off_knowledge_import_candidates.json"
            )

        require(applyPlanFile.exists()) {
            "OFF apply plan not found: ${applyPlanFile.absolutePath}"
        }

        val applyPlan =
            gson.fromJson(
                applyPlanFile.readText(),
                OFFKnowledgeProposalApplyPlan::class.java
            )

        val importCandidates =
            OFFKnowledgeImportCandidateBuilder()
                .build(
                    plan = applyPlan
                )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(
                importCandidates
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF KNOWLEDGE IMPORT CANDIDATES")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Apply entries     : ${applyPlan.entries.size}")
        println("Import candidates : ${importCandidates.size}")
        println("Output            : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}