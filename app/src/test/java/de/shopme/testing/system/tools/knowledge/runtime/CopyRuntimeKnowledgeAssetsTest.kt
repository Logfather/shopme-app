package de.shopme.testing.system.tools.knowledge.runtime

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CopyRuntimeKnowledgeAssetsTest {

    @Test
    fun copyGeneratedRuntimeKnowledgeIntoAppAssets() {

        val generatedRuntimeDirectory =
            File(
                "../data/generated/knowledge/runtime"
            )

        val appRuntimeAssetDirectory =
            File(
                "src/main/assets/knowledge/runtime"
            )

        assertTrue(
            generatedRuntimeDirectory.isDirectory,
            "Generated runtime directory missing: " +
                    generatedRuntimeDirectory.absolutePath
        )

        val generatedFiles =
            generatedRuntimeDirectory
                .listFiles { file ->
                    file.isFile &&
                            file.extension.equals(
                                other = "json",
                                ignoreCase = true
                            )
                }
                ?.sortedBy {
                    it.name
                }
                .orEmpty()

        assertEquals(
            EXPECTED_RUNTIME_ARTIFACT_COUNT,
            generatedFiles.size,
            "Unexpected number of generated runtime artifacts. " +
                    "Expected $EXPECTED_RUNTIME_ARTIFACT_COUNT, " +
                    "but found ${generatedFiles.size}: " +
                    generatedFiles.joinToString {
                        it.name
                    }
        )

        ensureDirectoryExists(
            directory = appRuntimeAssetDirectory
        )

        deleteExistingRuntimeAssets(
            directory = appRuntimeAssetDirectory
        )

        generatedFiles.forEach { sourceFile ->

            val targetFile =
                File(
                    appRuntimeAssetDirectory,
                    sourceFile.name
                )

            sourceFile.copyTo(
                target = targetFile,
                overwrite = true
            )

            assertTrue(
                targetFile.isFile,
                "Runtime asset was not copied: " +
                        targetFile.absolutePath
            )

            assertEquals(
                sourceFile.readBytes().contentHashCode(),
                targetFile.readBytes().contentHashCode(),
                "Copied runtime asset differs from generated source: " +
                        sourceFile.name
            )
        }

        val copiedFiles =
            appRuntimeAssetDirectory
                .listFiles { file ->
                    file.isFile &&
                            file.extension.equals(
                                other = "json",
                                ignoreCase = true
                            )
                }
                ?.sortedBy {
                    it.name
                }
                .orEmpty()

        assertEquals(
            generatedFiles.map {
                it.name
            },
            copiedFiles.map {
                it.name
            },
            "App runtime assets differ from generated runtime artifacts"
        )

        assertEquals(
            EXPECTED_RUNTIME_ARTIFACT_COUNT,
            copiedFiles.size
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("RUNTIME KNOWLEDGE ASSETS COPIED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println(
            "Generated source : " +
                    generatedRuntimeDirectory.path
        )
        println(
            "App asset target : " +
                    appRuntimeAssetDirectory.path
        )
        println(
            "Artifacts copied : " +
                    copiedFiles.size
        )

        copiedFiles.forEach { file ->

            println(
                file.name
                    .padEnd(32) +
                        formatFileSize(
                            bytes = file.length()
                        )
            )
        }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }


    private fun ensureDirectoryExists(
        directory: File
    ) {

        if (!directory.exists()) {
            check(
                directory.mkdirs()
            ) {
                "Could not create runtime asset directory: " +
                        directory.absolutePath
            }
        }

        require(directory.isDirectory) {
            "Runtime asset path is not a directory: " +
                    directory.absolutePath
        }
    }


    private fun deleteExistingRuntimeAssets(
        directory: File
    ) {

        directory
            .listFiles()
            .orEmpty()
            .forEach { file ->

                check(
                    file.deleteRecursively()
                ) {
                    "Could not delete old runtime asset: " +
                            file.absolutePath
                }
            }
    }


    private fun formatFileSize(
        bytes: Long
    ): String {

        val kilobytes =
            bytes.toDouble() /
                    1024.0

        return if (kilobytes < 1024.0) {

            "%.1f KB".format(
                kilobytes
            )

        } else {

            val megabytes =
                kilobytes /
                        1024.0

            "%.2f MB".format(
                megabytes
            )
        }
    }


    companion object {

        private const val EXPECTED_RUNTIME_ARTIFACT_COUNT =
            23
    }
}