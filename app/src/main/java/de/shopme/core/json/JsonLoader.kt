package de.shopme.core.json

import android.content.Context
import com.google.gson.Gson
import de.shopme.util.CategoryConfig

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