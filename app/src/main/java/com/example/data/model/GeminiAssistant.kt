package com.example.data.model

enum class AssistantLanguage(val label: String, val code: String) {
    ENGLISH("English", "en"),
    ARABIC("العربية (Arabic)", "ar"),
    HINDI("हिन्दी (Hindi)", "hi")
}

data class AssistantChatMessage(
    val id: String,
    val sender: String, // "USER" or "GEMINI_PHARMA_AI"
    val text: String,
    val language: AssistantLanguage,
    val timestamp: String,
    val hasDisclaimer: Boolean = true,
    val isSpeaking: Boolean = false
)
