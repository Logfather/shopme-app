package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import de.shopme.tools.knowledge.reader.ResourceCatalogReader
import org.junit.Test
import java.io.File

class CarbonRuntimeCoverageDebugTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun debugCarbonRuntimeCoverage() {

        val runtimeCarbonFile =
            File(
                "src/main/assets/knowledge/runtime/carbon_footprint.json"
            )

        val masterCarbonFile =
            File(
                "src/main/assets/knowledge/runtime/carbon_footprint_master.json"
            )

        val offCarbonFile =
            File(
                "build/off/carbon_footprint_final.json"
            )

        require(runtimeCarbonFile.exists()) {
            "Runtime carbon file not found: ${runtimeCarbonFile.absolutePath}"
        }

        val catalog =
            ResourceCatalogReader()
                .read()

        val runtimeCarbon =
            gson.fromJson(
                runtimeCarbonFile.readText(),
                CarbonKnowledge::class.java
            )

        val masterCarbonEntries =
            if (masterCarbonFile.exists()) {
                gson.fromJson(
                    masterCarbonFile.readText(),
                    CarbonKnowledge::class.java
                ).entries.size
            } else {
                0
            }

        val offCarbonEntries =
            if (offCarbonFile.exists()) {
                gson.fromJson(
                    offCarbonFile.readText(),
                    CarbonKnowledge::class.java
                ).entries.size
            } else {
                0
            }

        val runtimeCarbonKeys =
            runtimeCarbon
                .entries
                .keys

        val catalogNames =
            catalog
                .map {
                    it.normalized
                }
                .toSet()

        val directNameMatches =
            catalogNames
                .filter {
                    it in runtimeCarbonKeys
                }
                .sorted()

        val catalogWithCarbonMetadata =
            catalog
                .filter { item ->

                    item.knowledge
                        ?.carbon
                        ?.reference
                        ?.isNotBlank() == true
                }

        val successfulMetadataLookups =
            catalogWithCarbonMetadata
                .filter { item ->

                    val reference =
                        item.knowledge
                            ?.carbon
                            ?.reference

                    reference != null &&
                            runtimeCarbon.entries.containsKey(reference)
                }

        val missingMetadataButRuntimeEntryExists =
            catalog
                .filter { item ->

                    val hasCarbonMetadata =
                        item.knowledge
                            ?.carbon
                            ?.reference
                            ?.isNotBlank() == true

                    !hasCarbonMetadata &&
                            runtimeCarbon.entries.containsKey(
                                item.normalized
                            )
                }
                .map {
                    it.normalized
                }
                .sorted()

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON RUNTIME COVERAGE DEBUG")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Catalog items                  : ${catalog.size}")
        println("Master carbon entries           : $masterCarbonEntries")
        println("OFF carbon entries              : $offCarbonEntries")
        println("Runtime carbon entries          : ${runtimeCarbon.entries.size}")
        println()
        println("Catalog direct name matches     : ${directNameMatches.size}")
        println("Catalog with carbon metadata    : ${catalogWithCarbonMetadata.size}")
        println("Successful metadata lookups     : ${successfulMetadataLookups.size}")
        println("Missing metadata but entry exists: ${missingMetadataButRuntimeEntryExists.size}")

        println()
        println("First 100 direct name matches:")
        directNameMatches
            .take(100)
            .forEach {
                println("- $it")
            }

        println()
        println("First 100 missing metadata but runtime entry exists:")
        missingMetadataButRuntimeEntryExists
            .take(100)
            .forEach {
                println("- $it")
            }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}