package com.example

import com.example.model.OtpSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun otpSanitizer_detectsSixDigitVerificationCode() {
        val input = "Your Google verification code is 482910. Do not share this with anyone."
        val (isSensitive, sanitized) = OtpSanitizer.sanitize("Messages", "Google", input)
        
        assertTrue("Expected message to be detected as sensitive", isSensitive)
        assertFalse("Expected raw digits to be scrubbed from text", sanitized.contains("482910"))
        assertTrue("Expected masked bullets in output", sanitized.contains("••••••"))
    }

    @Test
    fun otpSanitizer_detectsDashedPinCode() {
        val input = "Chase Bank: Your one-time passcode is 391-482 for card transaction."
        val (isSensitive, sanitized) = OtpSanitizer.sanitize("Chase", "Bank Alert", input)

        assertTrue("Expected banking OTP to be sensitive", isSensitive)
        assertFalse("Expected raw dashed digits to be masked", sanitized.contains("391-482"))
        assertTrue("Expected masked dash bullets in output", sanitized.contains("•••-•••"))
    }

    @Test
    fun otpSanitizer_passesNormalChatMessagesUnaltered() {
        val input = "Hey, are you free for dinner at 7?"
        val (isSensitive, sanitized) = OtpSanitizer.sanitize("WhatsApp", "Alex", input)

        assertFalse("Expected regular chat to not be flagged as sensitive", isSensitive)
        assertEquals("Expected text to remain unchanged", input, sanitized)
    }

    @Test
    fun otpSanitizer_passesAddressesAndOrderNumbersWithoutOtpKeywords() {
        val input = "Meet me at 1234 Elm Street for order 987654"
        val (isSensitive, sanitized) = OtpSanitizer.sanitize("Messages", "Friend", input)

        assertFalse("Expected plain address and non-OTP numbers to not be sensitive", isSensitive)
        assertEquals(input, sanitized)
    }

    @Test
    fun otpSanitizer_detectsReverseVerificationPattern() {
        val input = "849201 is your login code. Do not share."
        val (isSensitive, sanitized) = OtpSanitizer.sanitize("Messages", "Service", input)

        assertTrue("Expected reversed pattern to be detected as sensitive", isSensitive)
        assertFalse(sanitized.contains("849201"))
        assertTrue(sanitized.contains("••••••"))
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
