package com.bankyar.util

import java.util.Calendar

object JalaliCalendar {

    private val monthNames = arrayOf(
        "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
        "مهر","آبان","آذر","دی","بهمن","اسفند"
    )

    fun toJalaliString(timestamp: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val (jy, jm, jd) = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "$jd ${monthNames[jm - 1]} $jy - $h:$min"
    }

    fun toJalaliShort(timestamp: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val (jy, jm, jd) = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return "$jd/${jm.toString().padStart(2,'0')}/$jy"
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val g_y = gy - 1600; val g_m = gm - 1; val g_d = gd - 1
        var g_d_no = 365 * g_y + (g_y + 3) / 4 - (g_y + 99) / 100 + (g_y + 399) / 400
        val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        val gDays = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        for (i in 0 until g_m) g_d_no += gDays[i]
        g_d_no += g_d
        var j_d_no = g_d_no - 79
        val j_np = j_d_no / 12053; j_d_no %= 12053
        var jy = 979 + 33 * j_np + 4 * (j_d_no / 1461); j_d_no %= 1461
        if (j_d_no >= 366) { jy += (j_d_no - 1) / 365; j_d_no = (j_d_no - 1) % 365 }
        val jDays = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var jm = 0; var rem = j_d_no
        for (i in 0..11) { if (rem >= jDays[i]) { rem -= jDays[i]; jm++ } else break }
        return Triple(jy, jm + 1, rem + 1)
    }
}
