package de.shopme.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("shopme_prefs")

class ListPreferences(private val context: Context) {

    private val LIST_ID = stringPreferencesKey("list_id")

    val listIdFlow: Flow<String?> =
        context.dataStore.data.map { it[LIST_ID] }

    suspend fun saveListId(id: String) {
        context.dataStore.edit {
            it[LIST_ID] = id
        }
    }
}