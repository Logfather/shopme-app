package de.shopme.tools.knowledge.ai.openai

data class OpenAIProviderConfig(

    val apiKey: String,

    val model: String,

    val endpoint: String =
        "https://api.openai.com/v1/chat/completions"
) {

    companion object {

        fun fromEnvironment(): OpenAIProviderConfig {

            val apiKey = System.getenv("OPENAI_API_KEY")
                ?: throw IllegalStateException(
                    "Environment variable OPENAI_API_KEY is missing."
                )

            val model =
                System.getenv("OPENAI_MODEL")
                    ?: "gpt-5.5"

            return OpenAIProviderConfig(
                apiKey = apiKey,
                model = model
            )
        }
    }
}