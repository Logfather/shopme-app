package de.shopme.tools.knowledge.ai.openai

import com.google.gson.Gson

class JsonOpenAIChatResponseBodyDeserializer(
    private val gson: Gson = Gson()
) : OpenAIChatResponseBodyDeserializer {

    override fun deserialize(
        content: String
    ): OpenAIChatResponseBody {

        return gson.fromJson(
            content,
            OpenAIChatResponseBody::class.java
        )
    }
}