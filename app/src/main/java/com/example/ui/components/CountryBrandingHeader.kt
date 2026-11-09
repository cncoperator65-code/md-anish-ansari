package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryConfig
import com.example.ui.theme.PharmaCyanAccent
import com.example.ui.theme.PharmaTealPrimary

/**
 * Reusable country-specific regional branding header for PharmaX AI.
 * Displays the master logo accompanied by the country flag, regional brand title,
 * and regulatory accreditation badge, with graceful fallback to the master brand identity.
 */
@Composable
fun CountryBrandingHeader(
    country: CountryConfig,
    onCountryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val regionalBrand = country.regionalBrand
    val isCustom = regionalBrand.isCustomAssetConfigured

    Row(
        modifier = modifier
            .testTag("country_branding_header")
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCountryClick() }
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Master PharmaX Emblem with Regional Accent Border
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(regionalBrand.primaryAccentHex)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = "${regionalBrand.brandTitle} Logo",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            // Regional Title + AI Chip + Flag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCustom) regionalBrand.brandTitle else "PharmaX",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(5.dp))
                Surface(
                    color = PharmaCyanAccent,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00363A),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = country.flagEmoji,
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Switch Country",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Country Name & Regulatory Authority Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Regulatory Authority",
                    tint = Color(regionalBrand.primaryAccentHex),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${country.countryName} • ${country.regulatoryCode}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(regionalBrand.primaryAccentHex),
                    maxLines = 1
                )
                Text(
                    text = "(${country.currencyCode} ${country.currencySymbol})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
