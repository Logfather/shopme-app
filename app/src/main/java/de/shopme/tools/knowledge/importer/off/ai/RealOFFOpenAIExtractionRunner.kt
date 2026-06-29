package de.shopme.tools.knowledge.importer.off.ai

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import de.shopme.tools.knowledge.ai.openai.OpenAIProvider
import de.shopme.tools.knowledge.ai.openai.OpenAIProviderConfig
import de.shopme.tools.knowledge.ai.openai.RealOpenAIHttpClient
import java.io.File

object RealOFFOpenAIExtractionRunner {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {

        val openAIConfig = OpenAIProviderConfig.fromEnvironment()

        val config = AIProviderConfig(
            providerName = "openai",
            model = openAIConfig.model,
            apiKey = openAIConfig.apiKey,
            endpoint = openAIConfig.endpoint,
            temperature = 0.0
        )

        val provider = OpenAIProvider(
            config = config,
            httpClient = RealOpenAIHttpClient(
                config = config
            )
        )

        val client = ExternalOFFAIExtractionClient(
            provider = provider,
            promptBuilder = OFFPromptBuilder(
                template = OFFPromptTemplate()
            ),
            parser = JsonAIProviderResponseParser()
        )

        val result = client.extract(
            OFFAIExtractionBatch(
                source = "manual_open_food_facts_test",
                sourceVersion = "manual-v1",
                products = listOf(
                    OFFAIExtractionInput(
                        code = "123",
                        productName = "Apple",
                        productNameDe = "Apfel",
                        brands = null,
                        categories = "Fruits",
                        ingredientsText = "Apple",
                        ingredientsTextDe = "Apfel",
                        labels = null,
                        countries = "Germany",
                        quantity = "1 piece"
                    )
                )
            )
        )

        println("Candidates: ${result.candidates.size}")

        val outputFile = File(
            "build/ai-import/openai-knowledge-import-batch.json"
        )

        outputFile.parentFile.mkdirs()

        val json = GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(result)

        outputFile.writeText(json)

        println("Written KnowledgeImportBatch to:")
        println(outputFile.absolutePath)

        result.candidates.forEach { candidate ->
            println(candidate)
        }
    }
}