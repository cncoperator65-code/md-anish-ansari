package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryConfig
import com.example.data.model.CountryConfigRegistry
import com.example.data.model.RegulatoryJurisdiction
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopPharmaAppBar(
    currentRole: AppRole,
    onRoleSelected: (AppRole) -> Unit,
    cartItemCount: Int,
    onCartClicked: () -> Unit,
    currentTier: SubscriptionTier,
    onSubscriptionClicked: () -> Unit,
    activeJurisdiction: RegulatoryJurisdiction = RegulatoryJurisdiction.UAE_MOHAP,
    activeCountry: CountryConfig = CountryConfigRegistry.UAE,
    onCountryClicked: () -> Unit = {},
    onAssistantClicked: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Main App Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Regional Logo & Title (Clickable to switch Country & Currency)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .testTag("appbar_country_branding_button")
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onCountryClicked() }
                        .padding(end = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(activeCountry.regionalBrand.primaryAccentHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = "${activeCountry.regionalBrand.brandTitle} Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeCountry.regionalBrand.brandTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeCountry.flagEmoji,
                                fontSize = 15.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Country",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        // Regulatory Authority & Currency Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${activeCountry.countryCode} • ${activeCountry.regulatoryCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(activeCountry.regionalBrand.primaryAccentHex),
                                maxLines = 1
                            )
                            Surface(
                                color = Color(activeCountry.regionalBrand.primaryAccentHex).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${activeCountry.currencyCode} ${activeCountry.currencySymbol}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(activeCountry.regionalBrand.primaryAccentHex),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Actions: Gemini Assistant, Subscription Badge, Cart Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Gemini Clinical AI Assistant Button
                    Surface(
                        color = PharmaTealLight.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .testTag("appbar_gemini_assistant_button")
                            .clickable { onAssistantClicked() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Gemini AI Guide",
                                tint = PharmaTealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Guide",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaTealPrimary
                            )
                        }
                    }

                    // Subscription Tier Pill
                    Surface(
                        color = when (currentTier) {
                            SubscriptionTier.B2C_PREMIUM -> PharmaTealPrimary.copy(alpha = 0.15f)
                            SubscriptionTier.B2B_PREMIUM_JV -> PharmaIndigo.copy(alpha = 0.15f)
                            SubscriptionTier.B2B_BASE -> PharmaCobalt.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .testTag("subscription_badge_button")
                            .clickable { onSubscriptionClicked() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Active Plan",
                                tint = when (currentTier) {
                                    SubscriptionTier.B2C_PREMIUM -> PharmaTealPrimary
                                    SubscriptionTier.B2B_PREMIUM_JV -> PharmaIndigo
                                    SubscriptionTier.B2B_BASE -> PharmaCobalt
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentTier) {
                                    SubscriptionTier.B2C_PREMIUM -> "B2C AI+"
                                    SubscriptionTier.B2B_PREMIUM_JV -> "JV Ent"
                                    SubscriptionTier.B2B_BASE -> "B2B Base"
                                    SubscriptionTier.FREE_TIER -> "Free"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Cart Icon (Customer view)
                    if (currentRole == AppRole.CUSTOMER) {
                        IconButton(
                            onClick = onCartClicked,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("cart_icon_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(
                                            containerColor = PharmaEmergencyRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = "$cartItemCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping Cart",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Role Switcher Chips Bar (Seamless switching between all 5 interfaces)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppRole.values().forEach { role ->
                    val isSelected = currentRole == role
                    Surface(
                        color = if (isSelected) PharmaTealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("role_chip_${role.name.lowercase()}")
                            .clickable { onRoleSelected(role) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                        ) {
                            Icon(
                                imageVector = when (role) {
                                    AppRole.CUSTOMER -> Icons.Default.Person
                                    AppRole.PHARMACY_B2B -> Icons.Default.Storefront
                                    AppRole.JV_MARKETPLACE -> Icons.Default.Handshake
                                    AppRole.LOGISTICS_GPS -> Icons.Default.LocalShipping
                                    AppRole.SUPER_ADMIN -> Icons.Default.AdminPanelSettings
                                },
                                contentDescription = role.shortLabel,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = role.shortLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
