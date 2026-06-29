package de.shopme.tools.knowledge.ai.openai

import com.google.gson.Gson
import de.shopme.tools.knowledge.ai.AIProviderConfig
import java.net.HttpURLConnection
import java.net.URL

class RealOpenAIHttpClient(
    private val config: AIProviderConfig,
    private val bodyMapper: OpenAIChatRequestBodyMapper =
        OpenAIChatRequestBodyMapper(config),
    private val gson: Gson = Gson()
) : OpenAIHttpClient {

    override fun complete(
        request: OpenAIRequest
    ): String {

        val apiKey = requireNotNull(config.apiKey) {
            "OpenAI API key is missing"
        }

        val endpoint = config.endpoint
            ?: "https://api.openai.com/v1/chat/completions"

        val body = bodyMapper.map(request)
        val json = gson.toJson(body)

        val connection = URL(endpoint).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")

        connection.outputStream.use { output ->
            output.write(json.toByteArray(Charsets.UTF_8))
        }

        val statusCode = connection.responseCode

        val responseText = if (statusCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }
                ?: ""
        }

        connection.disconnect()

        if (statusCode !in 200..299) {
            error("OpenAI request failed with HTTP $statusCode: $responseText")
        }

        return responseText
    }
}