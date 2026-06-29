package de.shopme.tools.knowledge.update

import de.shopme.tools.knowledge.update.steps.KnowledgeUpdateStep

class AdminKnowledgeUpdateService(

    private val steps: List<KnowledgeUpdateStep> = emptyList()

) {

    fun runWeeklyUpdate(): AdminKnowledgeUpdateResult {

        steps.forEach { step ->

            step.execute()

        }

        return AdminKnowledgeUpdateResult(

            success = true,

            message =
                "Knowledge update completed"

        )
    }
}