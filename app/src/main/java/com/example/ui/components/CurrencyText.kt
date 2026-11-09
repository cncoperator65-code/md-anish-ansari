package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryConfig
import java.math.BigDecimal

/**
 * Reusable M3 component for displaying prices accurately with country currency code,
 * symbol, and optional original base-price transparency.
 */
@Composable
fun CurrencyText(
    amount: Double,
    country: CountryConfig,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null,
    showOriginalUsd: Boolean = false,
    baseAmountUsd: Double? = null
) {
    val formattedPrice = country.formatCurrency(amount)

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formattedPrice,
                style = style,
                color = color,
                fontWeight = fontWeight ?: style.fontWeight
            )
        }

        if (showOriginalUsd && baseAmountUsd != null && country.countryCode != "US") {
            Text(
                text = "(orig. $${String.format(java.util.Locale.US, "%.2f", baseAmountUsd)} • ${country.currencyCode})",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
