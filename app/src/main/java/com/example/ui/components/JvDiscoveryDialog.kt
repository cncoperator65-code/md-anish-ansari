package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JVSyncResult
import com.example.ui.theme.*

@Composable
fun JvDiscoveryDialog(
    result: JVSyncResult,
    onAccept: (JVSyncResult) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Handshake, contentDescription = null, tint = PharmaIndigo, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("JV Stock Discovered", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = PharmaWarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Out of Stock at primary store. Auto-routed via PharmaX Joint Venture network.",
                        fontSize = 11.sp,
                        color = PharmaWarningAmber,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(
                    text = result.requestedMedicine,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fulfilling Partner:", style = MaterialTheme.typography.labelSmall)
                            Text(result.fulfillingPartner.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Distance / Transfer ETA:", style = MaterialTheme.typography.labelSmall)
                            Text("${result.fulfillingPartner.distanceKm} km • ~${result.estimatedTransferMins} mins", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Certified Batch #:", style = MaterialTheme.typography.labelSmall)
                            Text(result.batchNumber, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PharmaTealPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Available Units:", style = MaterialTheme.typography.labelSmall)
                            Text("${result.stockAvailable} in stock", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PharmaSuccessGreen)
                        }
                    }
                }

                Surface(
                    color = PharmaBgDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "AUTOMATED ESCROW SETTLEMENT",
                            color = PharmaCyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Partner Store Payout (85%):", fontSize = 11.sp, color = Color.White)
                            Text("$${result.partnerSplitAmount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PharmaX Escrow Fee (3%):", fontSize = 11.sp, color = PharmaTextSecondaryDark)
                            Text("$${result.platformFeeAmount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmaTextSecondaryDark)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAccept(result) },
                colors = ButtonDefaults.buttonColors(containerColor = PharmaIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("accept_jv_route_button")
            ) {
                Text("Confirm JV Route")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
