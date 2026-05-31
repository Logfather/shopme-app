package de.shopme.data.invite

import android.content.Context

class PendingInviteStore(
    context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "shopme",
            Context.MODE_PRIVATE
        )

    companion object {

        private const val KEY_LIST_ID =
            "pending_list_id"

        private const val KEY_INVITE_ID =
            "pending_invite_id"
    }

    fun savePendingInvite(
        listId: String,
        inviteId: String
    ) {

        prefs.edit()
            .putString(
                KEY_LIST_ID,
                listId
            )
            .putString(
                KEY_INVITE_ID,
                inviteId
            )
            .apply()
    }

    fun getPendingListId(): String? {

        return prefs.getString(
            KEY_LIST_ID,
            null
        )
    }

    fun getPendingInviteId(): String? {

        return prefs.getString(
            KEY_INVITE_ID,
            null
        )
    }

    fun clearPendingInvite() {

        prefs.edit()
            .remove(KEY_LIST_ID)
            .remove(KEY_INVITE_ID)
            .apply()
    }
}