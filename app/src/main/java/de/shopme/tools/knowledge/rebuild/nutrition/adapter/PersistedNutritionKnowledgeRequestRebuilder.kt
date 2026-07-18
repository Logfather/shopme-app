package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestContract
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRequestRebuildResult
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRequestRebuilder
import java.io.File

class PersistedNutritionKnowledgeRequestRebuilder(
    private val requestFile: File,
    private val rebuildRequests: () -> Unit
) : NutritionKnowledgeRequestRebuilder {

    override fun rebuild():
            NutritionKnowledgeRequestRebuildResult {

        rebuildRequests()

        require(requestFile.isFile) {
            "Nutrition match request file was not created: " +
                    requestFile.absolutePath
        }

        val root =
            JsonParser.parseString(
                requestFile.readText()
            )
                .asJsonObject

        val version =
            root["version"]
                ?.takeIf {
                    it.isJsonPrimitive &&
                            it.asJsonPrimitive.isNumber
                }
                ?.asInt
                ?: error(
                    "Missing nutrition match request version."
                )

        require(
            version ==
                    CatalogKnowledgeMatchRequestContract.CURRENT_VERSION
        ) {
            "Unsupported nutrition match request version: " +
                    version
        }

        val requests =
            root["requests"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: error(
                    "Missing nutrition match requests array."
                )

        return NutritionKnowledgeRequestRebuildResult(
            requestCount =
                requests.size(),
            requestFile =
                requestFile.absolutePath
        )
    }
}