package de.shopme.testing.system.tools.knowledge.runtime

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidateCatalogRuntimeArtifactSizesTest {

    private val gson =
        Gson()

    @Test
    fun validateCatalogRuntimeArtifactSizes() {
        val runtimeDir =
            File("../data/generated/knowledge/runtime")

        assertTrue(
            runtimeDir.exists(),
            "Runtime knowledge dir missing: ${runtimeDir.path}"
        )

        val files =
            runtimeDir
                .listFiles { file ->
                    file.isFile && file.extension == "json"
                }
                ?.sortedBy { it.name }
                ?: emptyList()

        assertTrue(
            files.isNotEmpty(),
            "No runtime artifacts found in ${runtimeDir.path}"
        )

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("CATALOG RUNTIME ARTIFACT SIZE VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        files.forEach { file ->
            val entries =
                readEntryCount(file)

            val sizeKb =
                file.length() / 1024.0

            println()
            println(file.name)
            println("entries=$entries")
            println("sizeKb=${"%.2f".format(sizeKb)}")

            assertTrue(
                entries > 0,
                "Runtime artifact must not be empty: ${file.name}"
            )

            assertTrue(
                file.length() < MAX_RUNTIME_ARTIFACT_BYTES,
                "Runtime artifact too large: ${file.name} size=${file.length()} bytes"
            )
        }
    }

    private fun readEntryCount(
        file: File
    ): Int {
        val root =
            gson.fromJson(
                file.readText(),
                JsonObject::class.java
            )

        return root
            .getAsJsonObject("entries")
            ?.size()
            ?: 0
    }

    private companion object {

        const val MAX_RUNTIME_ARTIFACT_BYTES =
            5L * 1024L * 1024L
    }
}