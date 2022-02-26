package com.hide.videophoto.common.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {

    const val YYYYMMDD_HHMMSS = "yyyyMMdd-HHmmss"
    const val MILLISECOND = "millisecond"
    const val SECOND = "SECOND"

    /**
     * @param timeStamp    As second
     * @param outputFormat Expected output format
     * @return Date as string with #outputFormat
     */
    fun convertTimeStampToDate(
        timeStamp: Any?,
        outputFormat: String
    ): String {
        val sdf = SimpleDateFormat(outputFormat, Locale.getDefault())
        var date = Date()
        try {
            timeStamp?.run {
                date = when (this) {
                    is String -> Date(toLong() * 1000)
                    is Long -> Date(this * 1000)
                    is Double -> Date(toLong() * 1000)
                    is Float -> Date(toLong() * 1000)
                    is Int -> Date(toLong() * 1000)
                    else -> Date()
                }
            }
        } catch (ex: NumberFormatException) {
            ex.printStackTrace()
        }

        return sdf.format(date)
    }

    /**
     * @param timeStamp As millisecond
     * @return timestamp of the date(00:00:00:000) from timeStamp
     */
    fun convertTimeStampToStartOfDate(
        timeStamp: Long,
        timeZone: String = TimeZone.getDefault().id,
        unit: String = MILLISECOND
    ): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        cal.timeInMillis = timeStamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (unit) {
            SECOND -> {
                val result = cal.timeInMillis.toString()
                result.substring(0, result.length - 3).toLong()
            }
            else -> cal.timeInMillis
        }
    }

    /**
     * @param timeStamp As millisecond
     * @return timestamp of the date(23:59:59:999) from timeStamp
     */
    fun convertTimeStampToEndOfDate(
        timeStamp: Long,
        timeZone: String = TimeZone.getDefault().id,
        unit: String = MILLISECOND
    ): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        cal.timeInMillis = timeStamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)

        return when (unit) {
            SECOND -> {
                val result = cal.timeInMillis.toString()
                result.substring(0, result.length - 3).toLong()
            }
            else -> cal.timeInMillis
        }
    }
}