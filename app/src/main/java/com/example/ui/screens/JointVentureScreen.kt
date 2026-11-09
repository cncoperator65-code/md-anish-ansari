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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JVPartner
import com.example.data.model.MedicineItem
import com.example.ui.theme.*

@Composable
fun JointVentureScreen(
    partners: List<JVPartner>,
    medicines: List<MedicineItem>,
    onTestJvSync: (MedicineItem) -> Unit
) {
    var selectedMedForTest by remember { mutableStateOf(medicines.find { it.stockTotal == 0 } ?: medicines.first()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card: JV Engine
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaIndigo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "JOINT VENTURE (JV) INTER-STORE SYNC",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = PharmaMint,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "3 Partners Online",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00382E),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Automated Multi-Store Stock Discovery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "When an emergency or chronic drug is depleted at your branch, the JV network locates nearby partner inventories, guarantees certified batches, and splits customer payments automatically into escrow.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E7FF)
                    )
                }
            }
        }

        // Live Out-of-Stock Discovery Simulator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Simulate Out-of-Stock Discovery Engine",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select a medicine to simulate automatic partner routing and escrow splits:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedMedForTest.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Local Stock: ${selectedMedForTest.stockTotal} units • $${selectedMedForTest.price}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedMedForTest.stockTotal == 0) PharmaEmergencyRed else PharmaSuccessGreen
                            )
                        }

                        Button(
                            onClick = { onTestJvSync(selectedMedForTest) },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaIndigo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("simulate_jv_discovery_button")
                        ) {
                            Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run JV Sync", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Revenue & Split Model Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaBgDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "JV SPLIT PAYOUT MODEL",
                        color = PharmaCyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("85% Fulfilling Partner Store", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Direct wholesale settlement", color = PharmaTextSecondaryDark, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("5% Originating Store Referral Fee", color = PharmaMint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Passive commission", color = PharmaTextSecondaryDark, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("7% Cold-Chain Logistics Pool", color = ColdChainSafe, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Temperature transport cost", color = PharmaTextSecondaryDark, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("3% PharmaX AI Escrow & Platform Fee", color = PharmaCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Cloud matching & verification", color = PharmaTextSecondaryDark, fontSize = 11.sp)
                    }
                }
            }
        }

        // Certified JV Partners Directory
        item {
            Text(
                text = "Certified JV Partner Pharmacies (${partners.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(partners) { partner ->
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(partner.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = PharmaTealPrimary, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "${partner.branchCity} • Lic: ${partner.licenseNo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = PharmaSuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = PharmaWarningAmber, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("${partner.reliabilityRating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shared Wholesale SKUs: ${partner.sharedStockSkus}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PharmaCobalt
                        )

                        Surface(
                            color = PharmaIndigo.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Commission: ${partner.commissionSharePct}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
