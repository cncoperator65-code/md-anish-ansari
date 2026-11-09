package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantChatMessage
import com.example.data.model.AssistantLanguage
import com.example.ui.theme.*

@Composable
fun GeminiAssistantSheet(
    messages: List<AssistantChatMessage>,
    activeLanguage: AssistantLanguage,
    onLanguageSelected: (AssistantLanguage) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var queryText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PharmaTealPrimary, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Gemini AI",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PharmaX Clinical AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Multilingual Medication & Storage Consultation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Language Selector Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(AssistantLanguage.values()) { lang ->
                    val isSelected = activeLanguage == lang
                    Surface(
                        color = if (isSelected) PharmaTealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("lang_chip_${lang.code}")
                    ) {
                        Text(
                            text = lang.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mandatory Medical Disclaimer
        Surface(
            color = PharmaWarningAmber.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = PharmaWarningAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Medical Disclaimer: AI consultation provides educational drug information only. No autonomous prescribing or clinical diagnosis.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Inquiries Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val quickPrompts = when (activeLanguage) {
                AssistantLanguage.ARABIC -> listOf(
                    "كيف يتم تخزين أنسولين لانتوس؟",
                    "ما هي جرعة وتداخلات أوجمنتين؟",
                    "إرشادات حبوب ليبيتور قبل النوم"
                )
                AssistantLanguage.HINDI -> listOf(
                    "Lantus इंसुलिन को कैसे स्टोर करें?",
                    "Augmentin दवा लेने का सही समय?",
                    "Panadol Extra की सुरक्षित डोज़"
                )
                AssistantLanguage.ENGLISH -> listOf(
                    "How to store Lantus Insulin?",
                    "Augmentin food & drug interactions?",
                    "Atorvastatin evening timing guide"
                )
            }

            items(quickPrompts) { prompt ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("quick_prompt_${prompt.take(6)}")
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = PharmaTealPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) PharmaTealPrimary else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "You" else "PharmaX Clinical AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) Color.White.copy(alpha = 0.8f) else PharmaTealPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = {
                    Text(
                        when (activeLanguage) {
                            AssistantLanguage.ARABIC -> "اكتب استفسارك الدوائي هنا..."
                            AssistantLanguage.HINDI -> "अपनी दवा के बारे में यहाँ पूछें..."
                            AssistantLanguage.ENGLISH -> "Ask about dosage, storage, interactions..."
                        },
                        fontSize = 12.sp
                    )
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("assistant_query_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (queryText.isNotBlank()) {
                        onSendMessage(queryText)
                        queryText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(PharmaTealPrimary, shape = RoundedCornerShape(14.dp))
                    .testTag("assistant_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
