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
import com.example.data.model.Order
import com.example.ui.theme.*

@Composable
fun OrderConfirmationDialog(
    order: Order,
    onTrackDelivery: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PharmaSuccessGreen, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Order Confirmed & Escrow Locked", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PharmaTealPrimary
                )

                Text(
                    text = "Your payment of $${order.totalAmount} is securely held in PharmaX Escrow. It will be released to partners only after verified cold-chain delivery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (order.isJointVentureRoute) {
                    Surface(
                        color = PharmaIndigo.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Handshake, contentDescription = null, tint = PharmaIndigo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JV Fulfilled: ${order.jvPartnerName ?: "Partner Hub"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaIndigo
                            )
                        }
                    }
                }

                Surface(
                    color = ColdChainSafe.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = ColdChainSafe, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cold Storage Van Active • ${order.targetTempRange}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColdChainSafe
                        )
                    }
                }

                Text(
                    text = "Courier: ${order.driverName} • ETA: ~${order.etaMinutes} mins",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onTrackDelivery,
                colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("track_live_delivery_button")
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Track Live GPS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
