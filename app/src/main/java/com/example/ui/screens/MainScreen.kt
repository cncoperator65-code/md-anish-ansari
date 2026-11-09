package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SubscriptionTier
import com.example.ui.components.CountrySelectorDialog
import com.example.ui.components.GeminiAssistantSheet
import com.example.ui.components.JvDiscoveryDialog
import com.example.ui.components.OrderConfirmationDialog
import com.example.ui.components.TopPharmaAppBar
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.PharmaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PharmaViewModel
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val batches by viewModel.batches.collectAsStateWithLifecycle()
    val stockAlerts by viewModel.stockAlerts.collectAsStateWithLifecycle()
    val pillReminders by viewModel.pillReminders.collectAsStateWithLifecycle()
    val currentTier by viewModel.currentTier.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isOcrScanning by viewModel.isOcrScanning.collectAsStateWithLifecycle()
    val activePrescription by viewModel.activePrescription.collectAsStateWithLifecycle()
    val jvDiscoveryState by viewModel.jvDiscoveryState.collectAsStateWithLifecycle()
    val confirmedOrder by viewModel.confirmedOrder.collectAsStateWithLifecycle()
    val liveTemp by viewModel.liveTelemetryTemp.collectAsStateWithLifecycle()
    val complianceActive by viewModel.globalComplianceActive.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()

    // Production modules states
    val quarantineRecords by viewModel.quarantineRecords.collectAsStateWithLifecycle()
    val scannedBarcodes by viewModel.scannedBarcodes.collectAsStateWithLifecycle()
    val purchaseOrders by viewModel.purchaseOrders.collectAsStateWithLifecycle()
    val coldChainData by viewModel.coldChainTelemetry.collectAsStateWithLifecycle()
    val ledgerTransactions by viewModel.ledgerTransactions.collectAsStateWithLifecycle()
    val partnerSettlements by viewModel.partnerSettlements.collectAsStateWithLifecycle()
    val activeJurisdiction by viewModel.activeJurisdiction.collectAsStateWithLifecycle()
    val pharmacistCredential by viewModel.activePharmacistCredential.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val assistantMessages by viewModel.assistantMessages.collectAsStateWithLifecycle()
    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val stockMovements by viewModel.stockMovements.collectAsStateWithLifecycle()
    val inventorySearchQuery by viewModel.inventorySearchQuery.collectAsStateWithLifecycle()
    val expiryFilter by viewModel.expiryFilter.collectAsStateWithLifecycle()
    val inventorySort by viewModel.inventorySort.collectAsStateWithLifecycle()
    val fefoAllocations by viewModel.fefoAllocations.collectAsStateWithLifecycle()
    val csvReport by viewModel.csvReport.collectAsStateWithLifecycle()
    val activeCountry by viewModel.activeCountry.collectAsStateWithLifecycle()
    val availableCountries by viewModel.availableCountries.collectAsStateWithLifecycle()

    var showSubscriptionSheet by remember { mutableStateOf(false) }
    var showAssistantSheet by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        topBar = {
            TopPharmaAppBar(
                currentRole = currentRole,
                onRoleSelected = { role -> viewModel.setRole(role) },
                cartItemCount = cartItems.sumOf { it.quantity },
                onCartClicked = {
                    viewModel.setRole(AppRole.CUSTOMER)
                    viewModel.setTab(3) // Cart tab
                },
                currentTier = currentTier,
                onSubscriptionClicked = { showSubscriptionSheet = true },
                activeJurisdiction = activeJurisdiction,
                activeCountry = activeCountry,
                onCountryClicked = { showCountryDialog = true },
                onAssistantClicked = { showAssistantSheet = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                AppRole.CUSTOMER -> {
                    CustomerScreen(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) },
                        medicines = medicines,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setCategory(it) },
                        onAddToCart = { viewModel.addToCart(it) },
                        cartItems = cartItems,
                        onUpdateCartQty = { id, delta -> viewModel.updateCartQty(id, delta) },
                        onCheckout = { method -> viewModel.checkout(method) },
                        pillReminders = pillReminders,
                        onTogglePill = { viewModel.togglePillTaken(it) },
                        prescription = activePrescription,
                        isOcrScanning = isOcrScanning,
                        onStartOcrScan = { sample -> viewModel.startPrescriptionOcrScan(sample) },
                        currentTier = currentTier,
                        onOpenSubscriptions = { showSubscriptionSheet = true },
                        country = activeCountry
                    )
                }

                AppRole.PHARMACY_B2B -> {
                    InventoryScreen(
                        batches = batches,
                        alerts = stockAlerts,
                        quarantineRecords = quarantineRecords,
                        scannedBarcodes = scannedBarcodes,
                        suppliers = viewModel.suppliers,
                        purchaseOrders = purchaseOrders,
                        stockMovements = stockMovements,
                        medicines = medicines,
                        inventorySearchQuery = inventorySearchQuery,
                        expiryFilter = expiryFilter,
                        inventorySort = inventorySort,
                        fefoAllocations = fefoAllocations,
                        csvReport = csvReport,
                        onSetSearchQuery = { viewModel.setInventorySearchQuery(it) },
                        onSetExpiryFilter = { viewModel.setExpiryFilter(it) },
                        onSetInventorySort = { viewModel.setInventorySort(it) },
                        onCalculateFefo = { medId, qty -> viewModel.calculateFefo(medId, qty) },
                        onExportCsv = { viewModel.generateCsvExport() },
                        onDismissCsv = { viewModel.dismissCsvReport() },
                        onAddBatch = { name, bNo, qty, exp, temp ->
                            viewModel.addManualBatch(name, bNo, qty, exp, temp)
                        },
                        onEditBatch = { batch -> viewModel.editBatch(batch) },
                        onAdjustStock = { bNo, delta, reason, ref ->
                            viewModel.adjustBatchStock(bNo, delta, reason, ref)
                        },
                        onTransferBatch = { bNo, loc -> viewModel.transferBatch(bNo, loc) },
                        onArchiveBatch = { bNo -> viewModel.archiveBatch(bNo) },
                        onQuarantineBatch = { bNo, reason, notice, actor ->
                            viewModel.quarantineBatch(bNo, reason, notice, actor)
                        },
                        onScanBarcode = { raw ->
                            viewModel.scanBarcode(raw)
                        },
                        onCreatePO = { supId, items ->
                            viewModel.createPurchaseOrder(supId, items)
                        },
                        onApprovePO = { poId, pharmacist ->
                            viewModel.approvePurchaseOrder(poId, pharmacist)
                        },
                        onTriggerJvSync = {
                            viewModel.setRole(AppRole.JV_MARKETPLACE)
                        }
                    )
                }

                AppRole.JV_MARKETPLACE -> {
                    JointVentureScreen(
                        partners = viewModel.jvPartners,
                        medicines = medicines,
                        onTestJvSync = { med -> viewModel.triggerJvDiscovery(med) }
                    )
                }

                AppRole.LOGISTICS_GPS -> {
                    DeliveryTrackingScreen(
                        orders = viewModel.orders.collectAsStateWithLifecycle().value,
                        liveTemp = liveTemp,
                        coldChainData = coldChainData,
                        ledgerTransactions = ledgerTransactions,
                        partnerSettlements = partnerSettlements,
                        onTriggerExcursion = { temp, mins ->
                            viewModel.triggerColdChainExcursion(temp, mins)
                        },
                        onReleaseEscrow = { orderId, name, otp ->
                            viewModel.releaseEscrowForOrder(orderId, name, otp)
                        }
                    )
                }

                AppRole.SUPER_ADMIN -> {
                    AdminDashboardScreen(
                        forecasts = viewModel.demandForecasts,
                        isComplianceActive = complianceActive,
                        onToggleCompliance = { viewModel.toggleCompliance() },
                        activeJurisdiction = activeJurisdiction,
                        regulatoryProfiles = viewModel.regulatoryProfiles,
                        pharmacistCredential = pharmacistCredential,
                        onSwitchJurisdiction = { jur -> viewModel.switchJurisdiction(jur) },
                        auditLogs = auditLogs
                    )
                }
            }
        }
    }

    // Modal Sheet for Subscriptions
    if (showSubscriptionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSubscriptionSheet = false }
        ) {
            SubscriptionScreen(
                plans = viewModel.subscriptionPlans,
                currentTier = currentTier,
                onSelectTier = { tier ->
                    viewModel.setSubscriptionTier(tier)
                    showSubscriptionSheet = false
                },
                country = activeCountry
            )
        }
    }

    // Modal Sheet for Multilingual Gemini Clinical AI Assistant
    if (showAssistantSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAssistantSheet = false }
        ) {
            GeminiAssistantSheet(
                messages = assistantMessages,
                activeLanguage = activeLanguage,
                onLanguageSelected = { lang -> viewModel.setAssistantLanguage(lang) },
                onSendMessage = { query -> viewModel.askGeminiAssistant(query) }
            )
        }
    }

    // JV Out-of-Stock Discovery Dialog
    jvDiscoveryState?.let { jvResult ->
        JvDiscoveryDialog(
            result = jvResult,
            onAccept = { res -> viewModel.acceptJvPartnerRoute(res) },
            onDismiss = { viewModel.dismissJvDiscovery() }
        )
    }

    // Order Confirmed & Escrow locked Dialog
    confirmedOrder?.let { order ->
        OrderConfirmationDialog(
            order = order,
            onTrackDelivery = {
                viewModel.dismissOrderConfirmation()
                viewModel.setRole(AppRole.LOGISTICS_GPS)
            },
            onDismiss = { viewModel.dismissOrderConfirmation() }
        )
    }

    // Country & Currency Selector Dialog
    if (showCountryDialog) {
        CountrySelectorDialog(
            currentCountry = activeCountry,
            availableCountries = availableCountries,
            onCountrySelected = { selected ->
                viewModel.selectCountry(selected.countryCode)
            },
            onDismissRequest = { showCountryDialog = false }
        )
    }
}
