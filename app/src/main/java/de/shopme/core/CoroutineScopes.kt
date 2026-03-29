package de.shopme.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Singleton
class AppScope {

    val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
}