package de.shopme.testing.system.tools.knowledge.runtime

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidateRuntimeAssetsLoadTest {

    private val gson =
        Gson()

    @Test
    fun validateRuntimeAssetsLoadFromAndroidAssets() {
        val assetsRuntimeDir =
            File("src/main/assets/knowledge/runtime")

        assertTrue(
            assetsRuntimeDir.exists(),
            "Runtime assets dir missing: ${assetsRuntimeDir.path}"
        )

        val files =
            assetsRuntimeDir
                .listFiles { file ->
                    file.isFile &&
                            file.extension == "json"
                }
                ?.sortedBy {
                    it.name
                }
                ?: emptyList()

        assertTrue(
            files.isNotEmpty(),
            "No runtime asset artifacts found"
        )

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("RUNTIME ASSETS LOAD VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        files.forEach { file ->

            val root =
                gson.fromJson(
                    file.readText(),
                    JsonObject::class.java
                )

            val entries =
                root.getAsJsonObject("entries")

            assertTrue(
                entries != null,
                "Runtime asset missing entries object: ${file.name}"
            )

            assertTrue(
                entries.size() > 0,
                "Runtime asset must not be empty: ${file.name}"
            )

            println(
                "${file.name} entries=${entries.size()}"
            )
        }

        assertTrue(
            files.size == 23,
            "Expected 23 runtime assets but found ${files.size}"
        )
    }
}