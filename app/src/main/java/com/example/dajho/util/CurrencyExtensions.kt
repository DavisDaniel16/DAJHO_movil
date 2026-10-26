package com.example.dajho.util

import java.util.Locale
import kotlin.math.roundToLong

/**
 * Utilidades para manejo seguro de precisión financiera con céntimos enteros (Long) (Fase 2).
 */
fun Long.toCurrencyString(): String {
    val dollars = this / 100.0
    return String.format(Locale.US, "$%.2f", dollars)
}

fun Double.toCentsLong(): Long {
    return (this * 100.0).roundToLong()
}

fun String.toCentsLong(): Long {
    val clean = this.replace("$", "").trim()
    val value = clean.toDoubleOrNull() ?: 0.0
    return value.toCentsLong()
}
