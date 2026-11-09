package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    forecasts: List<DemandForecast>,
    isComplianceActive: Boolean,
    onToggleCompliance: () -> Unit,
    activeJurisdiction: RegulatoryJurisdiction,
    regulatoryProfiles: Map<RegulatoryJurisdiction, CountryRegulatoryProfile>,
    pharmacistCredential: PharmacistCredential,
    onSwitchJurisdiction: (RegulatoryJurisdiction) -> Unit,
    auditLogs: List<AuditLogEntry>
) {
    var selectedAdminSubtab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedAdminSubtab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PharmaTealPrimary
        ) {
            Tab(
                selected = selectedAdminSubtab == 0,
                onClick = { selectedAdminSubtab = 0 },
                text = { Text("AI Demand & KPIs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("admin_tab_demand")
            )
            Tab(
                selected = selectedAdminSubtab == 1,
                onClick = { selectedAdminSubtab = 1 },
                text = { Text("Regulatory Engine", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("admin_tab_regulatory")
            )
            Tab(
                selected = selectedAdminSubtab == 2,
                onClick = { selectedAdminSubtab = 2 },
                text = { Text("RBAC & Audit Trail", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("admin_tab_audit")
            )
        }

        when (selectedAdminSubtab) {
            0 -> AdminKpisAndDemandView(
                forecasts = forecasts,
                isComplianceActive = isComplianceActive,
                onToggleCompliance = onToggleCompliance
            )
            1 -> CountryRegulatoryView(
                activeJurisdiction = activeJurisdiction,
                profiles = regulatoryProfiles,
                pharmacistCredential = pharmacistCredential,
                onSwitchJurisdiction = onSwitchJurisdiction
            )
            2 -> SecurityAndAuditTrailView(auditLogs = auditLogs)
        }
    }
}

@Composable
fun AdminKpisAndDemandView(
    forecasts: List<DemandForecast>,
    isComplianceActive: Boolean,
    onToggleCompliance: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaBgDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PharmaTealLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SUPER ADMIN CONSOLE",
                                color = PharmaTealLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            color = PharmaSuccessGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(PharmaSuccessGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SYSTEMS NORMAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PharmaSuccessGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Global Pharmaceutical Platform Operations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Managing cross-border pharmacy licenses, AI demand intelligence, joint venture settlements, and escrow audits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PharmaTextSecondaryDark
                    )
                }
            }
        }

        // Platform KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Escrow Vault", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$48,920", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PharmaTealPrimary)
                        Text("Auto-split active", fontSize = 10.sp, color = PharmaSuccessGreen)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("OCR Extractions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1,842", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PharmaCobalt)
                        Text("98.4% Accuracy", fontSize = 10.sp, color = PharmaCobalt)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("JV Network", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("18 Hubs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PharmaIndigo)
                        Text("Dubai & Abu Dhabi", fontSize = 10.sp, color = PharmaIndigo)
                    }
                }
            }
        }

        // Regulatory & Compliance Safety Gate Switch
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PharmaTealPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Regulatory Compliance Enforcement",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Enforce UAE MOHAP & WHO Formulary checks, pharmacist verification signoff, and 2-8°C cold-chain logs before dispensing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isComplianceActive,
                        onCheckedChange = { onToggleCompliance() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PharmaSuccessGreen
                        ),
                        modifier = Modifier.testTag("compliance_switch")
                    )
                }
            }
        }

        // AI Demand Forecasting Section
        item {
            Text(
                text = "AI Demand Forecasting & Predictive Stocking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(forecasts) { forecast ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(forecast.medicineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        Surface(
                            color = if (forecast.stockOutRiskScore > 70) PharmaEmergencyRed.copy(alpha = 0.15f)
                            else PharmaSuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Risk: ${forecast.stockOutRiskScore}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (forecast.stockOutRiskScore > 70) PharmaEmergencyRed else PharmaSuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = forecast.seasonalFactor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Stock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${forecast.currentStock} units", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Projected 30d Demand", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${forecast.projectedDemand30d} units", fontWeight = FontWeight.Bold, color = PharmaCobalt)
                        }
                        Column {
                            Text("AI Suggested PO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+${forecast.suggestedReorderQty} units", fontWeight = FontWeight.Bold, color = PharmaTealPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountryRegulatoryView(
    activeJurisdiction: RegulatoryJurisdiction,
    profiles: Map<RegulatoryJurisdiction, CountryRegulatoryProfile>,
    pharmacistCredential: PharmacistCredential,
    onSwitchJurisdiction: (RegulatoryJurisdiction) -> Unit
) {
    val currentProfile = profiles[activeJurisdiction]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Select Operational Regulatory Jurisdiction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            RegulatoryJurisdiction.values().forEach { jur ->
                val isSelected = activeJurisdiction == jur
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PharmaTealPrimary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSwitchJurisdiction(jur) },
                            colors = RadioButtonDefaults.colors(selectedColor = PharmaTealPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${when (jur) {
                                    RegulatoryJurisdiction.UAE_MOHAP -> "🇦🇪 UAE"
                                    RegulatoryJurisdiction.US_FDA -> "🇺🇸 United States"
                                    RegulatoryJurisdiction.EU_EMA -> "🇪🇺 European Union"
                                    RegulatoryJurisdiction.INDIA_CDSCO -> "🇮🇳 India"
                                }} • ${jur.authorityName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Track & Trace: ${profiles[jur]?.trackAndTraceSystem}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Active Pharmacist Clinical Credentials
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PharmaTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Pharmacist Sign-Off Credential", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            color = PharmaSuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("ACTIVE & VERIFIED", color = PharmaSuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Pharmacist: ${pharmacistCredential.pharmacistName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("License: ${pharmacistCredential.licenseNumber} (${pharmacistCredential.issuingBody})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Digital Signature Key: ${pharmacistCredential.digitalSignatureKey}", fontSize = 10.sp, color = PharmaCobalt, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun SecurityAndAuditTrailView(
    auditLogs: List<AuditLogEntry>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaBgDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = PharmaCyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("IMMUTABLE SECURITY AUDIT TRAIL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All prescription approvals, batch quarantines, PO issuances, and escrow fund releases are cryptographically signed with SHA-256 for compliance audits.",
                        color = PharmaTextSecondaryDark,
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(auditLogs) { entry ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(entry.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PharmaTealPrimary)
                        Text(entry.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Actor: ${entry.actor} (${entry.role.displayName})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Resource: ${entry.resource}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Details: ${entry.details}", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Signature: ${entry.hashSignature} • IP: ${entry.ipAddress}", fontSize = 9.sp, color = PharmaCobalt, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
