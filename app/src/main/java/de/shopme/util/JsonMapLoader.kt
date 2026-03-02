package de.shopme.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadJsonMap(
    context: Context,
    fileName: String
): Map<String, String> {

    val json = context.assets
        .open(fileName)
        .bufferedReader()
        .use { it.readText() }

    val type = object : TypeToken<Map<String, String>>() {}.type

    return Gson().fromJson(json, type)
}