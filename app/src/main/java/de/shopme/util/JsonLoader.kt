package de.shopme.util

import android.content.Context
import com.google.gson.Gson

fun loadCategoryConfig(
    context: Context,
    fileName: String
): CategoryConfig {

    val json = context.assets
        .open(fileName)
        .bufferedReader()
        .use { it.readText() }

    return Gson().fromJson(json, CategoryConfig::class.java)
}