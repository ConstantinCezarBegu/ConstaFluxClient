package com.constantin.constaflux.data.converter

import androidx.room.TypeConverter
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.Period

object LocalDateConverter {
    @TypeConverter
    @JvmStatic
    fun stringToDisplayTime(str: String): DisplayTime {
        val offsetDateTime = OffsetDateTime.parse(str)
        val timeNow = LocalDateTime.now(offsetDateTime.toZonedDateTime().zone)
        val timeOther = offsetDateTime.toLocalDateTime()

        val unixTimeStamp = offsetDateTime.toEpochSecond()

        val duration = Duration.between(timeOther, timeNow)

        val deltaMinutes = duration.toMinutes()
        if (deltaMinutes < 60) return DisplayTime(
            str,
            if (deltaMinutes == 0L) "now" else "$deltaMinutes minute${if (deltaMinutes > 1) "s" else ""} ago",
            unixTimeStamp
        )

        val deltaHours = duration.toHours()
        if (deltaHours < 24) return DisplayTime(
            str,
            "$deltaHours hour${if (deltaHours > 1) "s" else ""} ago",
            unixTimeStamp
        )

        val deltaDays = duration.toDays()
        if (deltaDays < timeNow.toLocalDate().lengthOfMonth()) return DisplayTime(
            str,
            if (deltaDays == 1L) "yesterday" else "$deltaDays days ago",
            unixTimeStamp
        )

        val periodDiff = Period.between(timeOther.toLocalDate(), timeNow.toLocalDate())

        val deltaYears = periodDiff.years
        if (deltaYears >= 1) return DisplayTime(
            str,
            "$deltaYears year${if (deltaYears > 1) "s" else ""} ago",
            unixTimeStamp
        )

        val deltaMonth = periodDiff.months
        return DisplayTime(
            str,
            "$deltaMonth month${if (deltaMonth > 1) "s" else ""} ago",
            unixTimeStamp
        )
    }
}