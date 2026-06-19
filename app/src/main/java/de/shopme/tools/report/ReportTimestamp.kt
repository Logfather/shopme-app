package de.shopme.tools.report

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ReportTimestamp {

    private val formatter =

        DateTimeFormatter.ofPattern(

            "dd.MM.yyyy HH:mm:ss"

        )

    fun now(): String =

        LocalDateTime.now()

            .format(

                formatter

            )

}