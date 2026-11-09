package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun DeliveryTrackingScreen(
    orders: List<Order>,
    liveTemp: Float,
    coldChainData: ColdChainTelemetryData,
    ledgerTransactions: List<LedgerTransaction>,
    partnerSettlements: List<PartnerSettlementSummary>,
    onTriggerExcursion: (Float, Int) -> Unit,
    onReleaseEscrow: (String, String, String) -> Unit
) {
    var selectedDeliverySubtab by remember { mutableStateOf(0) }
    val activeOrder = orders.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedDeliverySubtab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PharmaTealPrimary
        ) {
            Tab(
                selected = selectedDeliverySubtab == 0,
                onClick = { selectedDeliverySubtab = 0 },
                text = { Text("GPS Route", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("delivery_tab_gps")
            )
            Tab(
                selected = selectedDeliverySubtab == 1,
                onClick = { selectedDeliverySubtab = 1 },
                text = { Text("Cold-Chain IoT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("delivery_tab_iot")
            )
            Tab(
                selected = selectedDeliverySubtab == 2,
                onClick = { selectedDeliverySubtab = 2 },
                text = { Text("Agent Handoff", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("delivery_tab_agent")
            )
            Tab(
                selected = selectedDeliverySubtab == 3,
                onClick = { selectedDeliverySubtab = 3 },
                text = { Text("Financial Ledger", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("delivery_tab_ledger")
            )
        }

        when (selectedDeliverySubtab) {
            0 -> GpsRouteView(activeOrder = activeOrder)
            1 -> AdvancedColdChainIoTView(
                telemetry = coldChainData,
                onTriggerExcursion = onTriggerExcursion
            )
            2 -> DeliveryAgentHandoffView(
                order = activeOrder,
                onReleaseEscrow = onReleaseEscrow
            )
            3 -> FinancialLedgerView(
                transactions = ledgerTransactions,
                settlements = partnerSettlements
            )
        }
    }
}

@Composable
fun GpsRouteView(activeOrder: Order?) {
    val infiniteTransition = rememberInfiniteTransition(label = "gps_van_anim")
    val vanProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "van_progress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Map Route Canvas Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaBgDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        for (i in 0..6) {
                            val y = h * (i / 6f)
                            drawLine(color = Color(0xFF132A36), start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
                        }
                        for (i in 0..8) {
                            val x = w * (i / 8f)
                            drawLine(color = Color(0xFF132A36), start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 1f)
                        }

                        val start = Offset(w * 0.15f, h * 0.75f)
                        val waypoint = Offset(w * 0.50f, h * 0.40f)
                        val end = Offset(w * 0.85f, h * 0.25f)

                        drawLine(
                            color = PharmaTealLight.copy(alpha = 0.5f),
                            start = start,
                            end = waypoint,
                            strokeWidth = 6f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )
                        drawLine(
                            color = PharmaTealLight.copy(alpha = 0.5f),
                            start = waypoint,
                            end = end,
                            strokeWidth = 6f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )

                        drawCircle(color = PharmaIndigo, radius = 16f, center = start)
                        drawCircle(color = Color.White, radius = 6f, center = start)
                        drawCircle(color = PharmaEmergencyRed, radius = 16f, center = end)
                        drawCircle(color = Color.White, radius = 6f, center = end)

                        val curX = if (vanProgress < 0.5f) {
                            start.x + (waypoint.x - start.x) * (vanProgress / 0.5f)
                        } else {
                            waypoint.x + (end.x - waypoint.x) * ((vanProgress - 0.5f) / 0.5f)
                        }
                        val curY = if (vanProgress < 0.5f) {
                            start.y + (waypoint.y - start.y) * (vanProgress / 0.5f)
                        } else {
                            waypoint.y + (end.y - waypoint.y) * ((vanProgress - 0.5f) / 0.5f)
                        }

                        drawCircle(color = PharmaCyanAccent.copy(alpha = 0.35f), radius = 28f, center = Offset(curX, curY))
                        drawCircle(color = PharmaCyanAccent, radius = 18f, center = Offset(curX, curY))
                        drawCircle(color = Color.White, radius = 8f, center = Offset(curX, curY))
                    }

                    Surface(
                        color = Color(0xDD0B202B),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PharmaMint))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE GPS COURIER EN-ROUTE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        color = Color(0xDD0B202B),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = PharmaCyanAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ETA: ${activeOrder?.etaMinutes ?: 15} Mins", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Order Snapshot Card
        if (activeOrder != null) {
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
                            Column {
                                Text("Order #${activeOrder.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Destination: ${activeOrder.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                color = PharmaTealLight.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = activeOrder.deliveryStage.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmaTealPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedColdChainIoTView(
    telemetry: ColdChainTelemetryData,
    onTriggerExcursion: (Float, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Multi-Sensor Telemetry Overview
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = PharmaCyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("IOT COLD-CHAIN TELEMETRY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Surface(
                            color = if (telemetry.isComplianceValid) PharmaMint else PharmaEmergencyRed,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (telemetry.isComplianceValid) "INTEGRITY INTACT" else "EXCURSION ALERT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (telemetry.isComplianceValid) Color(0xFF00382E) else Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Vaccine Compartment", color = PharmaTextSecondaryDark, fontSize = 11.sp)
                            Text(
                                text = "${telemetry.liveTempC}°C",
                                color = if (telemetry.isComplianceValid) ColdChainSafe else PharmaEmergencyRed,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("MKT: ${telemetry.meanKineticTempC}°C", color = PharmaMint, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Ambient: ${telemetry.ambientTempC}°C", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Humidity: ${telemetry.humidityRh}% RH", color = Color.White, fontSize = 12.sp)
                            Text("Battery: ${telemetry.batteryPercent}%", color = PharmaCyanAccent, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFF142F3E),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PharmaMint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Electronic Seal: ${telemetry.tamperSealStatus.label}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Excursion Simulator Action Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cold-Chain Integrity Tripwire Testing",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Simulate sensor spikes to test the automated batch quarantine tripwire when cold storage rules are violated (>8°C):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTriggerExcursion(4.1f, 0) },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("simulate_temp_normal_button")
                        ) {
                            Text("Safe (4.1°C)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onTriggerExcursion(9.6f, 25) },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaEmergencyRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("simulate_temp_breach_button")
                        ) {
                            Text("Tripwire (9.6°C Breach)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Excursion Event History
        item {
            Text(
                text = "Excursion Logs & Corrective Actions (${telemetry.excursions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(telemetry.excursions) { exc ->
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
                        Text("${exc.timestamp} • ${exc.peakTempC}°C for ${exc.durationMinutes}m", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(exc.severity, color = if (exc.severity.contains("CRITICAL")) PharmaEmergencyRed else PharmaWarningAmber, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Text(exc.actionTaken, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun DeliveryAgentHandoffView(
    order: Order?,
    onReleaseEscrow: (String, String, String) -> Unit
) {
    var recipientName by remember { mutableStateOf("Anish Kumar") }
    var enteredOtp by remember { mutableStateOf("892410") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = PharmaTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Courier & Recipient Identity Verification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "To release multi-party funds from escrow, the courier must verify the recipient's national identity and receive the tamper-evident cold-chain OTP code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Name (Emirates ID / National ID)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("recipient_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { enteredOtp = it },
                        label = { Text("6-Digit Cold-Chain Escrow OTP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("escrow_otp_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (order != null) {
                                onReleaseEscrow(order.id, recipientName, enteredOtp)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_otp_release_button")
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Handoff & Release Escrow", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialLedgerView(
    transactions: List<LedgerTransaction>,
    settlements: List<PartnerSettlementSummary>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Partner Settlement Clearinghouse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(settlements) { s ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(s.partnerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Pending: $${s.pendingEscrowLocked}", fontWeight = FontWeight.Bold, color = PharmaWarningAmber, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Settled Volume: $${s.totalSettledPayouts}", fontSize = 11.sp, color = PharmaSuccessGreen)
                        Text(s.payoutMethod, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Text(
                text = "Double-Entry Transaction Ledger (${transactions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(transactions) { txn ->
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
                        Text(txn.type.label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("$${txn.grossAmount}", fontWeight = FontWeight.ExtraBold, color = PharmaTealPrimary, fontSize = 13.sp)
                    }
                    Text("${txn.sourceAccount} ➔ ${txn.destinationAccount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Ref: ${txn.id} • Order: ${txn.orderId} • Status: ${txn.status}", fontSize = 10.sp, color = PharmaCobalt)
                }
            }
        }
    }
}
