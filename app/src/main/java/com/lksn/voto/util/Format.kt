package com.lksn.voto.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Format {
    private val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = '.' }
    private fun number(value: Double): String = DecimalFormat("#,##0", symbols).format(Math.round(value))
    fun rupiah(value: Double): String = "Rp" + number(value)
    fun rupiahComma(value: Double): String = rupiah(value) + ",-"
    fun rupiahDot(value: Double): String = rupiah(value) + ".-"
}