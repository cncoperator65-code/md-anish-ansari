package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.PharmaRepository
import com.example.ui.theme.*

@Composable
fun CustomerScreen(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    medicines: List<MedicineItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: MedicineCategory,
    onCategorySelected: (MedicineCategory) -> Unit,
    onAddToCart: (MedicineItem) -> Unit,
    cartItems: List<CartItem>,
    onUpdateCartQty: (String, Int) -> Unit,
    onCheckout: (PaymentMethod) -> Unit,
    pillReminders: List<PillReminder>,
    onTogglePill: (String) -> Unit,
    prescription: PrescriptionScan?,
    isOcrScanning: Boolean,
    onStartOcrScan: (Int) -> Unit,
    currentTier: SubscriptionTier,
    onOpenSubscriptions: () -> Unit,
    country: CountryConfig = CountryConfigRegistry.UAE
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Customer Subtabs
        TabRow(
            selectedTabIndex = currentTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PharmaTealPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = currentTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text("Meds Catalog", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_customer_catalog")
            )
            Tab(
                selected = currentTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text("Rx OCR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_customer_ocr")
            )
            Tab(
                selected = currentTab == 2,
                onClick = { onTabSelected(2) },
                text = { Text("Pill AI Alerts", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_customer_pills")
            )
            Tab(
                selected = currentTab == 3,
                onClick = { onTabSelected(3) },
                text = {
                    Text(
                        if (cartItems.isNotEmpty()) "Cart (${cartItems.size})" else "Cart",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_customer_cart")
            )
        }

        when (currentTab) {
            0 -> CustomerCatalogTab(
                medicines = medicines,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                onAddToCart = onAddToCart,
                currentTier = currentTier,
                onOpenSubscriptions = onOpenSubscriptions,
                onGoToOcr = { onTabSelected(1) },
                country = country
            )
            1 -> PrescriptionOcrScreen(
                prescription = prescription,
                isScanning = isOcrScanning,
                onStartScan = onStartOcrScan,
                allMedicines = medicines,
                onAddToCart = onAddToCart
            )
            2 -> PillOrganizerTab(
                reminders = pillReminders,
                onTogglePill = onTogglePill,
                currentTier = currentTier,
                onUpgrade = onOpenSubscriptions
            )
            3 -> CartCheckoutTab(
                cartItems = cartItems,
                onUpdateCartQty = onUpdateCartQty,
                onCheckout = onCheckout,
                currentTier = currentTier,
                country = country
            )
        }
    }
}

@Composable
fun CustomerCatalogTab(
    medicines: List<MedicineItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: MedicineCategory,
    onCategorySelected: (MedicineCategory) -> Unit,
    onAddToCart: (MedicineItem) -> Unit,
    currentTier: SubscriptionTier,
    onOpenSubscriptions: () -> Unit,
    onGoToOcr: () -> Unit,
    country: CountryConfig = CountryConfigRegistry.UAE
) {
    val filteredMedicines = remember(medicines, searchQuery, selectedCategory) {
        medicines.filter { med ->
            val matchesCategory = selectedCategory == MedicineCategory.ALL || med.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    med.name.contains(searchQuery, ignoreCase = true) ||
                    med.genericName.contains(searchQuery, ignoreCase = true) ||
                    med.brand.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaTealDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PharmaMint.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "CORE OCR & JV BACKBONE",
                                color = Color(0xFFA7F3D0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (currentTier == SubscriptionTier.B2C_PREMIUM) {
                            Surface(
                                color = PharmaCyanAccent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00382E), modifier = Modifier.size(12.dp))
                                    Text("Free 15-Min Delivery", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00382E))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Instant Cold-Chain & Rx Delivery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Scan prescription handwriting with OCR, or auto-sync out-of-stock meds via Joint Venture partner pharmacies.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0F2FE)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onGoToOcr,
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaCyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("banner_scan_rx_button")
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color(0xFF00363A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Doctor Rx", color = Color(0xFF00363A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenSubscriptions,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Plans & Perks", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search medicine, generic formula, brand...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PharmaTealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("medicine_search_input")
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MedicineCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.label, fontSize = 12.sp) },
                        leadingIcon = {
                            if (category == MedicineCategory.COLD_CHAIN) {
                                Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharmaTealPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                    )
                }
            }
        }

        // Medicine Cards List
        items(filteredMedicines) { medicine ->
            MedicineItemCard(
                medicine = medicine,
                country = country,
                onAddToCart = { onAddToCart(medicine) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MedicineItemCard(
    medicine: MedicineItem,
    country: CountryConfig = CountryConfigRegistry.UAE,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (medicine.isColdChainRequired) {
                        Surface(
                            color = ColdChainSafe.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.AcUnit, contentDescription = null, tint = ColdChainSafe, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Cold-Chain (2-8°C)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColdChainSafe)
                            }
                        }
                    }

                    if (medicine.requiresPrescription) {
                        Surface(
                            color = PharmaCobalt.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Rx Required",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaCobalt,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Stock or JV badge
                if (medicine.isRecalledOrQuarantined) {
                    Surface(
                        color = PharmaEmergencyRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Dangerous, contentDescription = null, tint = PharmaEmergencyRed, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("RECALLED / BLOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PharmaEmergencyRed)
                        }
                    }
                } else if (medicine.stockTotal > 0) {
                    Text(
                        text = "In Stock (${medicine.stockTotal})",
                        style = MaterialTheme.typography.labelSmall,
                        color = PharmaSuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Surface(
                        color = PharmaWarningAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "JV Auto-Sync Ready",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaWarningAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = medicine.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${medicine.genericName} • ${medicine.brand}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = medicine.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            if (medicine.isRecalledOrQuarantined) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = PharmaEmergencyRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = medicine.quarantineNotice ?: "Health Authority Recall Active: Distribution Prohibited.",
                        color = PharmaEmergencyRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = country.formatCurrency(medicine.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (medicine.isRecalledOrQuarantined) MaterialTheme.colorScheme.onSurfaceVariant else PharmaTealPrimary
                    )
                    Text(
                        text = medicine.recommendedTemp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (medicine.isRecalledOrQuarantined) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_to_cart_${medicine.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = PharmaEmergencyRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Blocked (Recalled)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaEmergencyRed
                        )
                    }
                } else {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (medicine.stockTotal > 0) PharmaTealPrimary else PharmaIndigo
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_to_cart_${medicine.id}")
                    ) {
                        Icon(
                            imageVector = if (medicine.stockTotal > 0) Icons.Default.AddShoppingCart else Icons.Default.Handshake,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (medicine.stockTotal > 0) "Add to Cart" else "Get via JV Partner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PillOrganizerTab(
    reminders: List<PillReminder>,
    onTogglePill: (String) -> Unit,
    currentTier: SubscriptionTier,
    onUpgrade: () -> Unit
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Pill Organizer & Chronotherapy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = PharmaTealPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "B2C Premium Feature",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaTealPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Automated daily schedules calculated from your scanned prescriptions and medication biological half-lives.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(reminders) { reminder ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (reminder.isTakenToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = reminder.timeLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PharmaTealPrimary
                            )
                            if (reminder.isAiScheduled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = PharmaCyanAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "AI Scheduled",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmaCobalt,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reminder.medicineName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${reminder.dosage} • ${reminder.instruction}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { onTogglePill(reminder.id) },
                        modifier = Modifier.testTag("toggle_pill_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = if (reminder.isTakenToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Mark Taken",
                            tint = if (reminder.isTakenToday) PharmaSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartCheckoutTab(
    cartItems: List<CartItem>,
    onUpdateCartQty: (String, Int) -> Unit,
    onCheckout: (PaymentMethod) -> Unit,
    currentTier: SubscriptionTier,
    country: CountryConfig = CountryConfigRegistry.UAE
) {
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CARD_STRIPE) }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.RemoveShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Pharmacy Cart is Empty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Add medicines from the catalog or scan your prescription via OCR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val subtotal = cartItems.sumOf { it.medicine.price * it.quantity }
    val isPremium = currentTier == SubscriptionTier.B2C_PREMIUM
    val deliveryFee = if (isPremium) 0.0 else 9.50
    val total = subtotal + deliveryFee
    val hasJv = cartItems.any { it.medicine.stockTotal == 0 || it.medicine.name.contains("Ozempic") }
    val escrowSplit = PharmaRepository.calculateEscrowSplit(total, isJv = hasJv)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Order Summary (${cartItems.size} items)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Cart items
        items(cartItems) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.medicine.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${country.formatCurrency(item.medicine.price)} each",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.medicine.stockTotal == 0) {
                            Text(
                                text = "⚡ Out-of-Stock: Routed via Apex JV Partner",
                                style = MaterialTheme.typography.labelSmall,
                                color = PharmaIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onUpdateCartQty(item.medicine.id, -1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "${item.quantity}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { onUpdateCartQty(item.medicine.id, 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Multi-Party Escrow Settlement Preview Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaBgDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURE MULTI-PARTY ESCROW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaTealLight,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = PharmaMint.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Cold-Chain OTP Guaranteed",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaMint,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Funds are placed in escrow and automatically split between partners upon verified cold-chain delivery:",
                        fontSize = 11.sp,
                        color = PharmaTextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Partner Pharmacy (85%):", fontSize = 11.sp, color = Color.White)
                        Text(country.formatCurrency(escrowSplit.pharmacyShare), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    if (hasJv) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("JV Referral Share (5%):", fontSize = 11.sp, color = PharmaCyanAccent)
                            Text(country.formatCurrency(escrowSplit.jvReferralShare), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmaCyanAccent)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cold-Chain Logistics (7%):", fontSize = 11.sp, color = Color.White)
                        Text(country.formatCurrency(escrowSplit.coldChainLogistics), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PharmaX Platform Fee (3%):", fontSize = 11.sp, color = PharmaTextSecondaryDark)
                        Text(country.formatCurrency(escrowSplit.platformEscrowFee), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmaTextSecondaryDark)
                    }
                }
            }
        }

        // Payment Method Selector
        item {
            Text(
                text = "Select Secure Payment Method",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            PaymentMethod.values().forEach { method ->
                val isSelected = selectedPayment == method
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PharmaTealPrimary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedPayment = method }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPayment = method },
                            colors = RadioButtonDefaults.colors(selectedColor = PharmaTealPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = method.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = method.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Checkout Button Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                        Text(country.formatCurrency((subtotal * 100).toLong() / 100.0), style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cold-Chain Delivery:", style = MaterialTheme.typography.bodyMedium)
                        Text(if (isPremium) "FREE (Premium Perk)" else country.formatCurrency(deliveryFee), color = if (isPremium) PharmaMint else MaterialTheme.colorScheme.onSurface)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(country.formatCurrency((total * 100).toLong() / 100.0), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = PharmaTealPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onCheckout(selectedPayment) },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_and_pay_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authorize Escrow & Place Order", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
