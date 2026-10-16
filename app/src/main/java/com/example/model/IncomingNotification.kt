package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class IncomingNotification(
    val id: String = UUID.randomUUID().toString(),
    val appName: String,
    val sender: String,
    val title: String,
    val rawBody: String,
    val sanitizedBody: String,
    val isOtpSensitive: Boolean,
    val iconName: String = "Message",
    val brandColorLong: Long = 0xFF3B82F6,
    val timeAgo: String = "Just now",
    val timestampMs: Long = System.currentTimeMillis()
) {
    val brandColor: Color
        get() = Color(brandColorLong)
}

object OtpSanitizer {
    // High-confidence regex for OTPs, passcodes, and verification codes
    // Matches patterns like: "code 123456", "verification code is 482910", "passcode: 391-482", "otp: 991823"
    private val forwardOtpRegex = Regex(
        """(?i)\b(?:otp|one[- ]time\s+(?:passcode|password)|passcode|verification(?:\s+code)?|verify\s+code|security\s+code|auth(?:\s+code)?|login\s+code|2fa|pin\s+code|secret\s+code)\b.*?\b(?:\d{4,8}|\d{3}[-\s]\d{3})\b"""
    )
    // Matches reversed patterns like: "482910 is your verification code", "391-482 is your one-time passcode"
    private val reverseOtpRegex = Regex(
        """(?i)\b(?:\d{4,8}|\d{3}[-\s]\d{3})\b.*?\b(?:is\s+your|is\s+the)?\s*(?:otp|one[- ]time\s+(?:passcode|password)|passcode|verification(?:\s+code)?|verify\s+code|security\s+code|auth(?:\s+code)?|login\s+code|2fa|pin\s+code)\b"""
    )

    private val digitCodeRegex = Regex("""\b\d{4,8}\b""")
    private val dashedCodeRegex = Regex("""\b\d{3}[-\s]\d{3}\b""")

    fun isHighConfidenceOtp(appName: String, title: String, body: String): Boolean {
        val combined = "$appName $title $body"
        return forwardOtpRegex.containsMatchIn(combined) || reverseOtpRegex.containsMatchIn(combined)
    }

    fun sanitize(appName: String, title: String, body: String): Pair<Boolean, String> {
        val isSensitive = isHighConfidenceOtp(appName, title, body)

        if (!isSensitive) {
            return Pair(false, body)
        }

        // Scrub any digits / codes completely
        var cleaned = body
            .replace(digitCodeRegex, "••••••")
            .replace(dashedCodeRegex, "•••-•••")

        if (!cleaned.contains("•••")) {
            cleaned = "🔒 [Protected Content - OTP / Security Code Hidden]"
        } else {
            cleaned = "🔒 $cleaned (OTP Hidden for Security)"
        }

        return Pair(true, cleaned)
    }
}
