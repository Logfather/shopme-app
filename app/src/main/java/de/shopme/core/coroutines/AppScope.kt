package de.shopme.core.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppScope : CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext =
        job + Dispatchers.IO
}