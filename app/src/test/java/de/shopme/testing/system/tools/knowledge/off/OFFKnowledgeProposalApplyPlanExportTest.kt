package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.off.OFFKnowledgeProposal
import de.shopme.tools.knowledge.off.OFFKnowledgeProposalApplyPlanBuilder
import org.junit.Test
import java.io.File

class OFFKnowledgeProposalApplyPlanExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportApplyPlan() {

        val proposalFile =
            File(
                "data/generated/off/off_knowledge_proposals.json"
            )

        val outputFile =
            File(
                "data/generated/off/off_knowledge_apply_plan.json"
            )

        require(
            proposalFile.exists()
        ) {
            "Proposal file not found: ${proposalFile.absolutePath}"
        }

        val proposals =
            gson.fromJson(
                proposalFile.readText(),
                Array<OFFKnowledgeProposal>::class.java
            ).toList()

        val applyPlan =
            OFFKnowledgeProposalApplyPlanBuilder()
                .build(
                    proposals = proposals
                )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(
                applyPlan
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF APPLY PLAN")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Proposals : ${proposals.size}")
        println("Entries   : ${applyPlan.entries.size}")
        println("Output    : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}