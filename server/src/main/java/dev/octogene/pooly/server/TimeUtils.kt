package dev.octogene.pooly.server

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun getNextDayAt(
    time: LocalTime = LocalTime(0, 1),
    currentInstant: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.UTC
): Instant {
    val todayLocalDate = currentInstant.toLocalDateTime(timeZone).date
    val nextDayLocalDate = todayLocalDate.plus(1, DateTimeUnit.DAY)
    return LocalDateTime(nextDayLocalDate, time).toInstant(timeZone)
}
