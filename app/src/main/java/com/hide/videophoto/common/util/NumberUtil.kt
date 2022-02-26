package com.hide.videophoto.common.util

import android.content.Context
import com.hide.videophoto.R
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToLong

object NumberUtil {

    private val LOCALE = Locale.ENGLISH
    private val BYTE = 1
    private val KILOBYTE = BYTE * 1000f
    private val MEGABYTE = KILOBYTE * 1000f
    private val GIGABYTE = MEGABYTE * 1000f

    fun formatNumber(number: Any?, numberAfterDecimal: Int = 0): String {
        return when (number) {
            is Int -> {
                String.format(LOCALE, "%,.0" + 0 + "f", ((number * 100f).roundToLong() / 100f))
            }
            is BigInteger -> {
                String.format(LOCALE, "%,.0" + 0 + "f", number)
            }
            is Float -> {
                val newNumberAfterDecimal = if (numberHasSurPlus(number)) {
                    numberAfterDecimal
                } else {
                    0
                }
                String.format(
                    LOCALE,
                    "%,.0" + newNumberAfterDecimal + "f",
                    ((number * 100f).roundToLong() / 100f)
                )
            }
            is Double -> {
                val newNumberAfterDecimal = if (numberHasSurPlus(number)) {
                    numberAfterDecimal
                } else {
                    0
                }
                val bigNumber = BigDecimal.valueOf(number)
                String.format(LOCALE, "%,.0" + newNumberAfterDecimal + "f", bigNumber)
            }
            is Long -> {
                val bigNumber = BigDecimal.valueOf(number)
                String.format(LOCALE, "%,.0" + 0 + "f", bigNumber)
            }
            is BigDecimal -> {
                String.format(LOCALE, "%,.0" + numberAfterDecimal + "f", number)
            }
            else -> "NaN"
        }
    }

    fun parseDuration(duration: Int): String {
        val totalSecond = duration / 1000
        val second = totalSecond % 60
        val minute = if (totalSecond >= 60) {
            ((totalSecond / 60) % 60)
        } else {
            0
        }
        val hour = totalSecond / 60 / 60

        val strSecond = getTwoDigitsNumber(second)
        val strMinute = getTwoDigitsNumber(minute)
        val strHour = getTwoDigitsNumber(hour)
        return "$strHour:$strMinute:$strSecond"
    }

    fun parseSize(ctx: Context, size: Any?): String {
        return when (size) {
            is Int -> {
                when {
                    size >= GIGABYTE -> {
                        String.format(
                            ctx.getString(R.string.value_gb),
                            formatNumber(size / GIGABYTE, 2)
                        )
                    }
                    size >= MEGABYTE -> {
                        String.format(
                            ctx.getString(R.string.value_mb),
                            formatNumber(size / MEGABYTE, 2)
                        )
                    }
                    size >= KILOBYTE -> {
                        String.format(
                            ctx.getString(R.string.value_kb),
                            formatNumber(size / KILOBYTE, 2)
                        )
                    }
                    else -> {
                        String.format(ctx.getString(R.string.value_byte), formatNumber(size, 2))
                    }
                }
            }
            is Long -> {
                when {
                    size >= GIGABYTE -> {
                        String.format(
                            ctx.getString(R.string.value_gb),
                            formatNumber(size / GIGABYTE, 2)
                        )
                    }
                    size >= MEGABYTE -> {
                        String.format(
                            ctx.getString(R.string.value_mb),
                            formatNumber(size / MEGABYTE, 2)
                        )
                    }
                    size >= KILOBYTE -> {
                        String.format(
                            ctx.getString(R.string.value_kb),
                            formatNumber(size / KILOBYTE, 2)
                        )
                    }
                    else -> {
                        String.format(ctx.getString(R.string.value_byte), formatNumber(size, 2))
                    }
                }
            }
            else -> {
                size?.toString() ?: ""
            }
        }
    }

    fun getTwoDigitsNumber(number: Int): String {
        return if (number >= 10) {
            number.toString()
        } else {
            "0$number"
        }
    }

    fun convertStringToDecimalNumber(inputNumber: String): Double {
        var strNumber = inputNumber
        val decimalFormat = DecimalFormat()
        val format = NumberFormat.getInstance(LOCALE)
        try {
            val decimalSeparator = decimalFormat.decimalFormatSymbols.decimalSeparator.toString()
            if (decimalSeparator == ".") {
                if (strNumber.contains(",")) {
                    strNumber = strNumber.replace(",", "")
                }
            } else if (decimalSeparator == ",") {
                if (strNumber.contains(".")) {
                    strNumber = strNumber.replace(".", "")
                }
            }

            return format.parse(strNumber)?.toDouble() ?: 0.0
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return 0.0
    }

    fun round(value: Double, places: Int): Double {
        var newPlaces = places
        if (newPlaces < 0) {
            newPlaces = 0
        }

        var bd = BigDecimal(value)
        bd = bd.setScale(newPlaces, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun numberHasSurPlus(number: Any): Boolean {
        return when (number) {
            is Float -> ceil(number) > number
            is Double -> ceil(number) > number
            else -> false
        }
    }
}