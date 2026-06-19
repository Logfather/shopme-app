package de.shopme.data.sync.remote

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "remote_apply_state"
)
data class RemoteApplyStateEntity(

    @PrimaryKey
    val entityId: String,

    val lastAppliedRemoteVersion: Long,

    val lastAppliedAt: Long
)