package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExtractedDrug
import com.example.data.model.MedicineItem
import com.example.data.model.PrescriptionScan
import com.example.data.model.VerificationState
import com.example.ui.theme.*

@Composable
fun PrescriptionOcrScreen(
    prescription: PrescriptionScan?,
    isScanning: Boolean,
    onStartScan: (sampleIndex: Int) -> Unit,
    allMedicines: List<MedicineItem>,
    onAddToCart: (MedicineItem) -> Unit
) {
    var selectedSample by remember { mutableStateOf(0) }

    // Laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_offset"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // OCR Scanner Viewfinder Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaBgDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Scanning Viewfinder Reticle
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, PharmaTealLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF061A23))
                    ) {
                        // Document preview watermark
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = "OCR Scanner",
                                tint = PharmaTealLight.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "CORE OCR BACKBONE ACTIVE",
                                color = PharmaTealLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Extracting handwriting, Rx details, doctor license & dosages",
                                color = PharmaTextSecondaryDark,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }

                        // Laser line if scanning
                        if (isScanning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(laserOffset)
                                    .align(Alignment.TopCenter)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    PharmaCyanAccent,
                                                    Color.White,
                                                    PharmaCyanAccent,
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        // Status pill in corner
                        Surface(
                            color = if (isScanning) PharmaCyanAccent else PharmaSuccessGreen,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PARSING...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("OCR READY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Scan Prescription
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Optical Character Recognition (OCR) Engine",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scan handwritten or printed doctor prescription to auto-extract items and verify formulary compliance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedSample = 0
                                onStartScan(0)
                            },
                            enabled = !isScanning,
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_sample_1_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Rx #1 (Endo)", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedSample = 1
                                onStartScan(1)
                            },
                            enabled = !isScanning,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_sample_2_button")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Rx #2 (Pulmo)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Extracted Document Information
        if (prescription != null) {
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
                                Text(
                                    text = prescription.patientName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${prescription.doctorName} • ${prescription.doctorLicense}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = prescription.clinicHospital,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PharmaCobalt
                                )
                            }

                            // Pharmacist status pill
                            Surface(
                                color = PharmaTealLight.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = PharmaTealPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = prescription.verificationState.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmaTealPrimary
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = "AI Safety & Interaction Checks",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        prescription.safetyInteractions.forEach { note ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = PharmaMint,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Extracted Medications Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Extracted Medications (${prescription.extractedDrugs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            prescription.extractedDrugs.forEach { drug ->
                                val matched = allMedicines.find { it.id == drug.matchedMedicineId }
                                if (matched != null) onAddToCart(matched)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_all_rx_to_cart_button")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add All to Cart", fontSize = 12.sp)
                    }
                }
            }

            // Extracted Medication Cards
            items(prescription.extractedDrugs) { drug ->
                ExtractedDrugCard(
                    drug = drug,
                    allMedicines = allMedicines,
                    onAddToCart = onAddToCart
                )
            }
        }
    }
}

@Composable
fun ExtractedDrugCard(
    drug: ExtractedDrug,
    allMedicines: List<MedicineItem>,
    onAddToCart: (MedicineItem) -> Unit
) {
    val matchedMedicine = allMedicines.find { it.id == drug.matchedMedicineId }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.drugName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dosage: ${drug.dosage} • ${drug.frequency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Duration: ${drug.durationDays} days supply",
                        style = MaterialTheme.typography.labelSmall,
                        color = PharmaTealPrimary
                    )
                }

                Surface(
                    color = PharmaCyanAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "OCR ${(drug.confidenceScore * 100).toInt()}% Conf",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (matchedMedicine != null) {
                    Text(
                        text = "Stock: ${if (matchedMedicine.stockTotal > 0) "${matchedMedicine.stockTotal} units" else "Auto-routed via JV"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (matchedMedicine.stockTotal > 0) PharmaSuccessGreen else PharmaWarningAmber
                    )

                    Button(
                        onClick = { onAddToCart(matchedMedicine) },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_rx_item_${drug.drugName.take(6)}")
                    ) {
                        Text("Add to Cart - $${matchedMedicine.price}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
