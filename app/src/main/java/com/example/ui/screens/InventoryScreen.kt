package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun InventoryScreen(
    batches: List<BatchRecord>,
    alerts: List<StockAlert>,
    quarantineRecords: List<BatchQuarantineRecord>,
    scannedBarcodes: List<ScannedBarcodeData>,
    suppliers: List<PharmaSupplier>,
    purchaseOrders: List<PurchaseOrder>,
    stockMovements: List<StockMovementRecord>,
    medicines: List<MedicineItem>,
    inventorySearchQuery: String,
    expiryFilter: ExpiryFilterWindow,
    inventorySort: InventorySortOrder,
    fefoAllocations: List<FefoAllocationItem>,
    csvReport: String?,
    onSetSearchQuery: (String) -> Unit,
    onSetExpiryFilter: (ExpiryFilterWindow) -> Unit,
    onSetInventorySort: (InventorySortOrder) -> Unit,
    onCalculateFefo: (String, Int) -> Unit,
    onExportCsv: () -> Unit,
    onDismissCsv: () -> Unit,
    onAddBatch: (String, String, Int, String, String) -> Unit,
    onEditBatch: (BatchRecord) -> Unit,
    onAdjustStock: (String, Int, String, String) -> Unit,
    onTransferBatch: (String, String) -> Unit,
    onArchiveBatch: (String) -> Unit,
    onQuarantineBatch: (String, QuarantineReason, String, String) -> Unit,
    onScanBarcode: (String) -> Unit,
    onCreatePO: (String, List<PurchaseOrderItem>) -> Unit,
    onApprovePO: (String, String) -> Unit,
    onTriggerJvSync: () -> Unit
) {
    var selectedSubtab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showQuarantineDialog by remember { mutableStateOf(false) }
    var showCreatePoDialog by remember { mutableStateOf(false) }

    // Dialog states for operations
    var editingBatch by remember { mutableStateOf<BatchRecord?>(null) }
    var adjustingBatch by remember { mutableStateOf<BatchRecord?>(null) }
    var transferringBatch by remember { mutableStateOf<BatchRecord?>(null) }
    var archivingBatch by remember { mutableStateOf<BatchRecord?>(null) }

    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Prominent In-Memory Demo Badge
        Surface(
            color = PharmaTealDark.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = PharmaTealPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ROOM PERSISTENT DB",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tenant: PharmaX Hub #01 (Dubai) • Room SQLite On-Device ACID Persistence",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onExportCsv,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("export_csv_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Inventory CSV",
                        tint = PharmaTealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Sub-navigation with 6 tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSubtab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PharmaTealPrimary,
            edgePadding = 8.dp
        ) {
            Tab(
                selected = selectedSubtab == 0,
                onClick = { selectedSubtab = 0 },
                text = { Text("Batches & Stock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("inventory_tab_stock")
            )
            Tab(
                selected = selectedSubtab == 1,
                onClick = { selectedSubtab = 1 },
                text = { Text("FEFO Picking Engine", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.LowPriority, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("inventory_tab_fefo")
            )
            Tab(
                selected = selectedSubtab == 2,
                onClick = { selectedSubtab = 2 },
                text = { Text("Stock Ledger", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("inventory_tab_ledger")
            )
            Tab(
                selected = selectedSubtab == 3,
                onClick = { selectedSubtab = 3 },
                text = { Text("Recall & Quarantine", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Dangerous, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("inventory_tab_quarantine")
            )
            Tab(
                selected = selectedSubtab == 4,
                onClick = { selectedSubtab = 4 },
                text = { Text("GS1 Barcode / QR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("inventory_tab_barcode")
            )
            Tab(
                selected = selectedSubtab == 5,
                onClick = { selectedSubtab = 5 },
                text = { Text("Suppliers & POs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("inventory_tab_pos")
            )
        }

        when (selectedSubtab) {
            0 -> BatchesAndStockDashboardView(
                batches = batches,
                alerts = alerts,
                purchaseOrders = purchaseOrders,
                searchQuery = inventorySearchQuery,
                onSearchChange = onSetSearchQuery,
                expiryFilter = expiryFilter,
                onExpiryFilterChange = onSetExpiryFilter,
                sortOrder = inventorySort,
                onSortOrderChange = onSetInventorySort,
                onAddBatchClicked = { showAddDialog = true },
                onExportCsvClicked = onExportCsv,
                onEditBatchClicked = { editingBatch = it },
                onAdjustBatchClicked = { adjustingBatch = it },
                onTransferBatchClicked = { transferringBatch = it },
                onArchiveBatchClicked = { archivingBatch = it },
                onTriggerJvSync = onTriggerJvSync
            )
            1 -> FefoPickingEngineView(
                medicines = medicines,
                batches = batches,
                allocations = fefoAllocations,
                onRunFefo = onCalculateFefo
            )
            2 -> StockLedgerView(
                stockMovements = stockMovements
            )
            3 -> RecallAndQuarantineView(
                records = quarantineRecords,
                batches = batches,
                onOpenQuarantineDialog = { showQuarantineDialog = true }
            )
            4 -> BarcodeScannerView(
                scannedList = scannedBarcodes,
                onSimulateScan = onScanBarcode
            )
            5 -> SuppliersAndPoView(
                suppliers = suppliers,
                purchaseOrders = purchaseOrders,
                onCreatePoClicked = { showCreatePoDialog = true },
                onApprovePO = onApprovePO
            )
        }
    }

    // Modal: Add Batch
    if (showAddDialog) {
        var medName by remember { mutableStateOf("") }
        var batchNo by remember { mutableStateOf("BAT-2026-") }
        var qtyStr by remember { mutableStateOf("50") }
        var expiryStr by remember { mutableStateOf("31-Dec-2027") }
        var tempStr by remember { mutableStateOf("2°C - 8°C") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Register Inward Batch (GRN)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medicine Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_batch_name_input")
                    )
                    OutlinedTextField(
                        value = batchNo,
                        onValueChange = { batchNo = it },
                        label = { Text("Batch Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_batch_no_input")
                    )
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantity Units") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_batch_qty_input")
                    )
                    OutlinedTextField(
                        value = expiryStr,
                        onValueChange = { expiryStr = it },
                        label = { Text("Expiry Date (e.g. 31-Dec-2027)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempStr,
                        onValueChange = { tempStr = it },
                        label = { Text("Storage Temperature Range") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = qtyStr.toIntOrNull() ?: 50
                        if (medName.isNotBlank() && batchNo.isNotBlank()) {
                            onAddBatch(medName, batchNo, qty, expiryStr, tempStr)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                    modifier = Modifier.testTag("submit_add_batch_button")
                ) {
                    Text("Inward & Catalog Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Edit Batch Config
    editingBatch?.let { batch ->
        var location by remember { mutableStateOf(batch.warehouseLocation) }
        var minThresholdStr by remember { mutableStateOf(batch.minThreshold.toString()) }
        var reorderThresholdStr by remember { mutableStateOf(batch.reorderThreshold.toString()) }
        var tempRange by remember { mutableStateOf(batch.storageTempRange) }

        AlertDialog(
            onDismissRequest = { editingBatch = null },
            title = { Text("Edit Batch Config (${batch.batchNumber})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(batch.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Warehouse Location / Shelf") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minThresholdStr,
                        onValueChange = { minThresholdStr = it },
                        label = { Text("Safety Stock Minimum Threshold") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reorderThresholdStr,
                        onValueChange = { reorderThresholdStr = it },
                        label = { Text("Automated Reorder Threshold") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempRange,
                        onValueChange = { tempRange = it },
                        label = { Text("Storage Temperature Range") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minTh = minThresholdStr.toIntOrNull() ?: batch.minThreshold
                        val reorderTh = reorderThresholdStr.toIntOrNull() ?: batch.reorderThreshold
                        onEditBatch(
                            batch.copy(
                                warehouseLocation = location,
                                minThreshold = minTh,
                                reorderThreshold = reorderTh,
                                storageTempRange = tempRange
                            )
                        )
                        editingBatch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBatch = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Stock Adjustment (Cycle count audit / damage / discrepancy)
    adjustingBatch?.let { batch ->
        var deltaStr by remember { mutableStateOf("+10") }
        var reason by remember { mutableStateOf("Physical Cycle Count Reconciliation") }
        var referenceId by remember { mutableStateOf("AUD-CC-${System.currentTimeMillis() % 10000}") }

        AlertDialog(
            onDismissRequest = { adjustingBatch = null },
            title = { Text("Adjust Stock: ${batch.batchNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(batch.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Current on-hand units: ${batch.quantity}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = deltaStr,
                        onValueChange = { deltaStr = it },
                        label = { Text("Unit Delta (e.g. +10, -5)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("adjust_stock_delta_input")
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Adjustment Reason") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = referenceId,
                        onValueChange = { referenceId = it },
                        label = { Text("Audit / Audit Report ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = deltaStr.replace("+", "").toIntOrNull() ?: 0
                        onAdjustStock(batch.batchNumber, delta, reason, referenceId)
                        adjustingBatch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                    modifier = Modifier.testTag("confirm_adjust_stock_button")
                ) {
                    Text("Post Stock Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustingBatch = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Transfer Location
    transferringBatch?.let { batch ->
        var newLocation by remember { mutableStateOf(batch.warehouseLocation) }

        AlertDialog(
            onDismissRequest = { transferringBatch = null },
            title = { Text("Relocate Batch: ${batch.batchNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(batch.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Current Location: ${batch.warehouseLocation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = newLocation,
                        onValueChange = { newLocation = it },
                        label = { Text("Target Location / Shelf Bay") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLocation.isNotBlank()) {
                            onTransferBatch(batch.batchNumber, newLocation)
                            transferringBatch = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary)
                ) {
                    Text("Confirm Relocation")
                }
            },
            dismissButton = {
                TextButton(onClick = { transferringBatch = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Archive Batch
    archivingBatch?.let { batch ->
        AlertDialog(
            onDismissRequest = { archivingBatch = null },
            title = { Text("Archive Batch ${batch.batchNumber}?") },
            text = {
                Text(
                    "Archiving removes this batch from active prescription fulfillment and FEFO order allocation. Its movement history remains preserved in the audit ledger.",
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onArchiveBatch(batch.batchNumber)
                        archivingBatch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaEmergencyRed)
                ) {
                    Text("Archive Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { archivingBatch = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: CSV Export Report Viewer
    csvReport?.let { csv ->
        AlertDialog(
            onDismissRequest = onDismissCsv,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Inventory CSV Export")
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(csv))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy CSV")
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = "Real-time snapshot generated from the active inventory state. Use copy to paste into spreadsheet applications.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            item {
                                Text(
                                    text = csv,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissCsv,
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Modal: Issue Quarantine Notice
    if (showQuarantineDialog) {
        var selectedBatchNumber by remember { mutableStateOf("BAT-2026-X88") }
        var selectedReason by remember { mutableStateOf(QuarantineReason.TEMPERATURE_EXCURSION) }
        var noticeId by remember { mutableStateOf("MOHAP-ALERT-2026-") }

        AlertDialog(
            onDismissRequest = { showQuarantineDialog = false },
            title = { Text("Issue Health Authority Quarantine Notice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Quarantining immediately locks all units of this batch from being dispensed, transferred via JV, or billed to customers.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = selectedBatchNumber,
                        onValueChange = { selectedBatchNumber = it },
                        label = { Text("Target Batch Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("quarantine_batch_input")
                    )
                    OutlinedTextField(
                        value = noticeId,
                        onValueChange = { noticeId = it },
                        label = { Text("Authority Notice / Reason ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Select Reason:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    QuarantineReason.values().take(4).forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason.label, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onQuarantineBatch(
                            selectedBatchNumber,
                            selectedReason,
                            noticeId,
                            "Dr. Salma Al-Khatib (DHA #8831)"
                        )
                        showQuarantineDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaEmergencyRed),
                    modifier = Modifier.testTag("confirm_quarantine_button")
                ) {
                    Text("Lock & Quarantine Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuarantineDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Create Purchase Order
    if (showCreatePoDialog) {
        var selectedSupplierId by remember { mutableStateOf(suppliers.firstOrNull()?.id ?: "") }
        AlertDialog(
            onDismissRequest = { showCreatePoDialog = false },
            title = { Text("Issue Wholesale Purchase Order") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Approved Supplier:", style = MaterialTheme.typography.labelSmall)
                    suppliers.forEach { sup ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedSupplierId == sup.id,
                                onClick = { selectedSupplierId = sup.id }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${sup.name} (${sup.leadTimeDays}d lead)", fontSize = 12.sp)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Auto-generated Replenishment Items:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• 100 units Ozempic 2mg DualDose ($11,000)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• 50 units Lantus SoloStar Insulin ($2,400)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreatePO(
                            selectedSupplierId,
                            listOf(
                                PurchaseOrderItem("med_4", "Ozempic 2mg DualDose", 100, 110.0, "2°C - 8°C"),
                                PurchaseOrderItem("med_1", "Lantus SoloStar (Insulin)", 50, 48.0, "2°C - 8°C")
                            )
                        )
                        showCreatePoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                    modifier = Modifier.testTag("submit_create_po_button")
                ) {
                    Text("Authorize PO")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePoDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun BatchesAndStockDashboardView(
    batches: List<BatchRecord>,
    alerts: List<StockAlert>,
    purchaseOrders: List<PurchaseOrder>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    expiryFilter: ExpiryFilterWindow,
    onExpiryFilterChange: (ExpiryFilterWindow) -> Unit,
    sortOrder: InventorySortOrder,
    onSortOrderChange: (InventorySortOrder) -> Unit,
    onAddBatchClicked: () -> Unit,
    onExportCsvClicked: () -> Unit,
    onEditBatchClicked: (BatchRecord) -> Unit,
    onAdjustBatchClicked: (BatchRecord) -> Unit,
    onTransferBatchClicked: (BatchRecord) -> Unit,
    onArchiveBatchClicked: (BatchRecord) -> Unit,
    onTriggerJvSync: () -> Unit
) {
    // 6 Overview KPIs Calculations
    val activeBatches = batches.filter { !it.isArchived }
    val totalUnits = activeBatches.sumOf { it.quantity }
    val lowStockCount = activeBatches.count { it.quantity <= it.minThreshold || it.status == BatchStatus.LOW_STOCK }
    val expiring90dCount = activeBatches.count { it.daysUntilExpiry() in 1..90 }
    val quarantinedCount = batches.count { it.isQuarantined }
    val pendingPoCount = purchaseOrders.count { it.status == PurchaseOrderStatus.SUBMITTED }

    // Filtering & Searching
    val filteredBatches = activeBatches.filter { batch ->
        val matchesSearch = searchQuery.isBlank() ||
                batch.medicineName.contains(searchQuery, ignoreCase = true) ||
                batch.batchNumber.contains(searchQuery, ignoreCase = true) ||
                batch.gtin.contains(searchQuery, ignoreCase = true) ||
                batch.warehouseLocation.contains(searchQuery, ignoreCase = true)

        val matchesExpiry = when (expiryFilter) {
            ExpiryFilterWindow.ALL -> true
            ExpiryFilterWindow.EXPIRING_30D -> batch.daysUntilExpiry() in 1..30
            ExpiryFilterWindow.EXPIRING_60D -> batch.daysUntilExpiry() in 1..60
            ExpiryFilterWindow.EXPIRING_90D -> batch.daysUntilExpiry() in 1..90
        }

        matchesSearch && matchesExpiry
    }.let { list ->
        // Sorting
        when (sortOrder) {
            InventorySortOrder.FEFO_EXPIRY_ASC -> list.sortedBy { it.daysUntilExpiry() }
            InventorySortOrder.STOCK_QUANTITY_DESC -> list.sortedByDescending { it.quantity }
            InventorySortOrder.STOCK_QUANTITY_ASC -> list.sortedBy { it.quantity }
            InventorySortOrder.MEDICINE_NAME_ASC -> list.sortedBy { it.medicineName }
            InventorySortOrder.BATCH_NUMBER_ASC -> list.sortedBy { it.batchNumber }
        }
    }

    var showSortMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1 of KPIs (3 Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "Active Batches",
                    value = "${activeBatches.size}",
                    sub = "$totalUnits Total Units",
                    accentColor = PharmaTealPrimary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Low Stock",
                    value = "$lowStockCount",
                    sub = "Needs Reorder",
                    accentColor = if (lowStockCount > 0) PharmaEmergencyRed else PharmaSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Expiring <90d",
                    value = "$expiring90dCount",
                    sub = "FEFO Priority",
                    accentColor = if (expiring90dCount > 0) PharmaWarningAmber else PharmaSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2 of KPIs (3 Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "Quarantined",
                    value = "$quarantinedCount",
                    sub = "Locked Lots",
                    accentColor = if (quarantinedCount > 0) PharmaEmergencyRed else PharmaSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Pending POs",
                    value = "$pendingPoCount",
                    sub = "Awaiting QA",
                    accentColor = PharmaCobalt,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Cold Vault IoT",
                    value = "4.2°C",
                    sub = "Target: 2-8°C",
                    accentColor = ColdChainSafe,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar & Sort Dropdown
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search by medicine, batch #, GTIN, bay...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inventory_search_input")
                )

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sort_batches_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Batches", tint = PharmaTealPrimary)
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        InventorySortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.label, fontSize = 12.sp) },
                                onClick = {
                                    onSortOrderChange(order)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Expiry Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ExpiryFilterWindow.values()) { window ->
                    FilterChip(
                        selected = expiryFilter == window,
                        onClick = { onExpiryFilterChange(window) },
                        label = { Text(window.label, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Section Header with Add Batch button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cataloged Lots (${filteredBatches.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAddBatchClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_batch_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Batch", fontSize = 12.sp)
                    }
                }
            }
        }

        // Batch Items
        if (filteredBatches.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching batches found", fontWeight = FontWeight.Bold)
                        Text("Try modifying your search or expiry filter", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredBatches) { batch ->
                BatchCard(
                    batch = batch,
                    onEdit = { onEditBatchClicked(batch) },
                    onAdjust = { onAdjustBatchClicked(batch) },
                    onTransfer = { onTransferBatchClicked(batch) },
                    onArchive = { onArchiveBatchClicked(batch) },
                    onTriggerJvSync = onTriggerJvSync
                )
            }
        }
    }
}

@Composable
fun BatchCard(
    batch: BatchRecord,
    onEdit: () -> Unit,
    onAdjust: () -> Unit,
    onTransfer: () -> Unit,
    onArchive: () -> Unit,
    onTriggerJvSync: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val daysLeft = batch.daysUntilExpiry()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(batch.medicineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Batch: ${batch.batchNumber} • GTIN: ${batch.gtin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Location: ${batch.warehouseLocation} • Mfr: ${batch.manufacturer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Adjust Units (+/-)", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onAdjust()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Transfer Location", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.MoveDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onTransfer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Thresholds & Temp", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Archive Batch", color = PharmaEmergencyRed, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = PharmaEmergencyRed, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onArchive()
                            }
                        )
                    }
                }
            }

            // Quarantine Notice Banner if locked
            if (batch.isQuarantined) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = PharmaEmergencyRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Dangerous, contentDescription = null, tint = PharmaEmergencyRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = batch.quarantineReason ?: "QUARANTINED: Blocked from dispensing & JV orders.",
                            color = PharmaEmergencyRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics row: Units, Available, Expiry, Storage Temp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Units", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${batch.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("Available", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${batch.availableQuantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (batch.availableQuantity > 0) PharmaSuccessGreen else PharmaEmergencyRed
                    )
                }
                Column {
                    Text("Expiry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(batch.expiryDate, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text(
                        text = if (daysLeft > 0) "$daysLeft days left" else "EXPIRED",
                        fontSize = 10.sp,
                        color = when {
                            daysLeft <= 0 -> PharmaEmergencyRed
                            daysLeft < 30 -> PharmaEmergencyRed
                            daysLeft < 90 -> PharmaWarningAmber
                            else -> PharmaSuccessGreen
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Storage Temp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(batch.storageTempRange, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = ColdChainSafe)
                }
            }

            // Low Stock & JV Reorder Suggestion
            if (batch.availableQuantity <= batch.reorderThreshold && !batch.isQuarantined) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️ Stock is at/below reorder threshold (${batch.reorderThreshold} units)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaWarningAmber
                    )
                    TextButton(
                        onClick = onTriggerJvSync,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Sync JV Stock", fontSize = 10.sp, color = PharmaTealPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun FefoPickingEngineView(
    medicines: List<MedicineItem>,
    batches: List<BatchRecord>,
    allocations: List<FefoAllocationItem>,
    onRunFefo: (String, Int) -> Unit
) {
    var selectedMedId by remember { mutableStateOf(medicines.firstOrNull()?.id ?: "med_1") }
    var requestedQtyStr by remember { mutableStateOf("10") }
    var hasRun by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Algorithm Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaTealPrimary.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LowPriority, contentDescription = null, tint = PharmaTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated FEFO Picking Engine (First-Expired, First-Out)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PharmaTealPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The FEFO engine automatically allocates lots with the earliest certified expiration dates to minimize inventory expiry wastage. Quarantined, recalled, or expired lots are strictly disqualified from allocation.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Selection & Input Controls
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select Medication to Allocate:", style = MaterialTheme.typography.labelSmall)

                    // Medicine Selector Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(medicines) { med ->
                            FilterChip(
                                selected = selectedMedId == med.id,
                                onClick = {
                                    selectedMedId = med.id
                                    val qty = requestedQtyStr.toIntOrNull() ?: 10
                                    onRunFefo(med.id, qty)
                                    hasRun = true
                                },
                                label = { Text(med.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = requestedQtyStr,
                            onValueChange = { requestedQtyStr = it },
                            label = { Text("Requested Units to Dispense") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("fefo_requested_qty_input")
                        )

                        Button(
                            onClick = {
                                val qty = requestedQtyStr.toIntOrNull() ?: 10
                                onRunFefo(selectedMedId, qty)
                                hasRun = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                            modifier = Modifier.testTag("run_fefo_button")
                        ) {
                            Text("Compute FEFO")
                        }
                    }
                }
            }
        }

        // Results Section
        item {
            Text(
                text = "FEFO Picking Sequence (${allocations.size} Candidate Lots)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (allocations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (!hasRun) "Select a medication above and click 'Compute FEFO' to preview picking priorities." else "No active batches available for this medicine.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allocations) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isEligible && item.allocatedQty > 0)
                            PharmaTealPrimary.copy(alpha = 0.05f)
                        else if (!item.isEligible)
                            PharmaEmergencyRed.copy(alpha = 0.05f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (item.isEligible) PharmaTealPrimary else PharmaEmergencyRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (item.isEligible) "PRIORITY #${item.pickingPriority}" else "DISQUALIFIED",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.batchNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Surface(
                                color = if (item.allocatedQty > 0) PharmaSuccessGreen else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Allocated: ${item.allocatedQty} units",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.allocatedQty > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Available: ${item.availableQty} units", fontSize = 11.sp)
                            Text("Expiry: ${item.expiryDate} (${item.daysUntilExpiry}d left)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Bay: ${item.warehouseLocation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (!item.isEligible) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⛔ Lot cannot be picked: Batch is quarantined or past expiry.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaEmergencyRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockLedgerView(
    stockMovements: List<StockMovementRecord>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = PharmaTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Immutable Stock Movement Ledger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Audit trail of every stock modification: goods receipt, order dispense, FEFO reservation, inventory count adjustment, bay relocation, and regulatory quarantine locks.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Ledger Records (${stockMovements.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(stockMovements) { movement ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = when (movement.movementType) {
                                    StockMovementType.GOODS_RECEIPT -> PharmaSuccessGreen.copy(alpha = 0.15f)
                                    StockMovementType.DISPENSE_SALE, StockMovementType.FEFO_RESERVATION -> PharmaCobalt.copy(alpha = 0.15f)
                                    StockMovementType.CYCLE_COUNT_ADJUSTMENT -> PharmaWarningAmber.copy(alpha = 0.15f)
                                    StockMovementType.WAREHOUSE_TRANSFER -> PharmaTealDark.copy(alpha = 0.15f)
                                    StockMovementType.QUARANTINE_LOCK -> PharmaEmergencyRed.copy(alpha = 0.15f)
                                    StockMovementType.BATCH_ARCHIVED -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = movement.movementType.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (movement.movementType) {
                                        StockMovementType.GOODS_RECEIPT -> PharmaSuccessGreen
                                        StockMovementType.DISPENSE_SALE, StockMovementType.FEFO_RESERVATION -> PharmaCobalt
                                        StockMovementType.CYCLE_COUNT_ADJUSTMENT -> PharmaWarningAmber
                                        StockMovementType.WAREHOUSE_TRANSFER -> PharmaTealDark
                                        StockMovementType.QUARANTINE_LOCK -> PharmaEmergencyRed
                                        StockMovementType.BATCH_ARCHIVED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = movement.batchNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        val deltaStr = if (movement.quantityDelta > 0) "+${movement.quantityDelta}" else "${movement.quantityDelta}"
                        Text(
                            text = "$deltaStr units",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (movement.quantityDelta >= 0) PharmaSuccessGreen else PharmaEmergencyRed
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${movement.medicineName} • Balance after: ${movement.balanceAfter} units",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Reason: ${movement.reason} (Ref: ${movement.referenceId})",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Actor: ${movement.actorName} (${movement.actorRole}) • ${movement.timestampFormatted}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    sub: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = accentColor)
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun RecallAndQuarantineView(
    records: List<BatchQuarantineRecord>,
    batches: List<BatchRecord>,
    onOpenQuarantineDialog: () -> Unit
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
                colors = CardDefaults.cardColors(containerColor = PharmaEmergencyRed.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = PharmaEmergencyRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Medicine Recall & Quarantine Sentinel",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PharmaEmergencyRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automated quarantine gateway blocks compromised, recalled, or cold-chain violated batches from ordering, billing, and JV routing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenQuarantineDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaEmergencyRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("open_quarantine_dialog_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Issue Quarantine Notice", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "Quarantine / Recall Notices (${records.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(records) { record ->
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
                            Text(record.medicineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Batch #: ${record.batchNumber} • ${record.manufacturer}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = PharmaEmergencyRed,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = record.status.label,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Reason: ${record.reason.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmaEmergencyRed)
                    Text("Notice Ref: ${record.authorityNoticeId} • Actioned by: ${record.quarantinedBy}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Locked Units: ${record.affectedUnits} • Patient Safety Alerts: ${record.patientAlertsDispatched}", fontSize = 10.sp, color = PharmaCobalt)
                }
            }
        }
    }
}

@Composable
fun BarcodeScannerView(
    scannedList: List<ScannedBarcodeData>,
    onSimulateScan: (String) -> Unit
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = PharmaTealLight,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GS1-128 & Tatmeen 2D DataMatrix Scanner",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Scans GTIN, batch number, serial numbers, and checks anti-counterfeit signatures.",
                        color = PharmaTextSecondaryDark,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onSimulateScan("(01)06291002019231(17)270228(10)BAT-2026-X88(21)SN-982187-DXB") },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scan_sample_genuine_button")
                        ) {
                            Text("Scan Genuine Lantus Box", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { onSimulateScan("(01)06291002019999(17)270630(10)BAT-2025-RECALL-99(21)SN-000412-RC") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PharmaEmergencyRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scan_sample_recalled_button")
                        ) {
                            Text("Scan Recalled Batch", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Scanned Barcode Verifications (${scannedList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(scannedList) { item ->
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
                            Text(item.medicineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("GTIN: ${item.gtin} • Batch: ${item.batchNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            color = when (item.verificationResult) {
                                BarcodeVerificationResult.GENUINE_VERIFIED -> PharmaSuccessGreen.copy(alpha = 0.15f)
                                BarcodeVerificationResult.QUARANTINED_BLOCKED, BarcodeVerificationResult.EXPIRED_BARCODE -> PharmaEmergencyRed.copy(alpha = 0.15f)
                                BarcodeVerificationResult.COUNTERFEIT_SUSPECT -> PharmaWarningAmber.copy(alpha = 0.15f)
                                BarcodeVerificationResult.UNREGISTERED -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.verificationResult.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (item.verificationResult) {
                                    BarcodeVerificationResult.GENUINE_VERIFIED -> PharmaSuccessGreen
                                    BarcodeVerificationResult.QUARANTINED_BLOCKED, BarcodeVerificationResult.EXPIRED_BARCODE -> PharmaEmergencyRed
                                    BarcodeVerificationResult.COUNTERFEIT_SUSPECT -> PharmaWarningAmber
                                    BarcodeVerificationResult.UNREGISTERED -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track & Trace: ${item.trackAndTraceId} • Serial: ${item.serialNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PharmaTealPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SuppliersAndPoView(
    suppliers: List<PharmaSupplier>,
    purchaseOrders: List<PurchaseOrder>,
    onCreatePoClicked: () -> Unit,
    onApprovePO: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Purchase Orders & Restocking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onCreatePoClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_po_button")
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New PO", fontSize = 12.sp)
                }
            }
        }

        items(purchaseOrders) { po ->
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
                            Text(po.poNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(po.supplierName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = when (po.status) {
                                PurchaseOrderStatus.RECEIVED_AND_INSPECTED -> PharmaSuccessGreen.copy(alpha = 0.15f)
                                PurchaseOrderStatus.PHARMACIST_APPROVED -> PharmaTealLight.copy(alpha = 0.15f)
                                PurchaseOrderStatus.IN_TRANSIT -> PharmaCyanAccent.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = po.status.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (po.status) {
                                    PurchaseOrderStatus.RECEIVED_AND_INSPECTED -> PharmaSuccessGreen
                                    PurchaseOrderStatus.PHARMACIST_APPROVED -> PharmaTealPrimary
                                    PurchaseOrderStatus.IN_TRANSIT -> PharmaCobalt
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    po.items.forEach { line ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${line.quantity}x ${line.medicineName}", fontSize = 11.sp)
                            Text("$${line.quantity * line.unitCost}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: $${po.totalCost}",
                            fontWeight = FontWeight.ExtraBold,
                            color = PharmaTealPrimary
                        )

                        if (po.status == PurchaseOrderStatus.SUBMITTED) {
                            Button(
                                onClick = { onApprovePO(po.id, "Dr. Salma Al-Khatib (DHA #8831)") },
                                colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("approve_po_${po.id}")
                            ) {
                                Text("Pharmacist Sign-Off", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "GMP Certified Suppliers (${suppliers.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(suppliers) { sup ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(sup.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("License: ${sup.licenseNumber} • Lead: ${sup.leadTimeDays} days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("★ ${sup.rating}", fontWeight = FontWeight.Bold, color = PharmaWarningAmber, fontSize = 12.sp)
                }
            }
        }
    }
}
