package com.controlprestamos.core.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

fun openWhatsAppReminder(
    context: Context,
    rawPhone: String,
    message: String
): Boolean {
    val phone = normalizePhoneForWhatsApp(rawPhone)

    if (phone.isBlank() || message.isBlank()) {
        return false
    }

    val encodedMessage = URLEncoder.encode(message, "UTF-8")
    val uri = Uri.parse("https://wa.me/$phone?text=$encodedMessage")

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

private fun normalizePhoneForWhatsApp(rawPhone: String): String {
    val digits = rawPhone.filter { it.isDigit() }

    if (digits.isBlank()) return ""

    // Regla práctica para Venezuela:
    // 0412xxxxxxx -> 58412xxxxxxx
    // Si ya viene con código internacional, se conserva.
    return when {
        digits.length == 11 && digits.startsWith("0") -> "58" + digits.drop(1)
        else -> digits
    }
}

