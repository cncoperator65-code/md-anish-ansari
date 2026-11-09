package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PharmaDatabase
import com.example.data.model.*
import com.example.data.repository.PharmaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppRole(val title: String, val shortLabel: String, val subtitle: String) {
    CUSTOMER("B2C Customer", "Customer", "Meds, OCR & Pill Organizer"),
    PHARMACY_B2B("B2B Pharmacy", "Pharmacy", "Batch Inventory & POs"),
    JV_MARKETPLACE("JV Network", "JV Network", "Out-of-Stock Sync & Splits"),
    LOGISTICS_GPS("GPS & Cold-Chain", "Delivery", "Live Van Route & Temp IoT"),
    SUPER_ADMIN("Super Admin", "Admin", "AI Demand, Audits & Recall")
}

data class UiNotification(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isError: Boolean = false
)

class PharmaViewModel(
    private val repository: PharmaRepository = PharmaRepository()
) : ViewModel() {

    // Current Role & Subtab
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Data streams from repository
    val medicines: StateFlow<List<MedicineItem>> = repository.medicines
    val batches: StateFlow<List<BatchRecord>> = repository.batches
    val jvPartners: List<JVPartner> = repository.jvPartners
    val stockAlerts: StateFlow<List<StockAlert>> = repository.stockAlerts
    val demandForecasts: List<DemandForecast> = repository.demandForecasts
    val pillReminders: StateFlow<List<PillReminder>> = repository.pillReminders
    val subscriptionPlans: List<SubscriptionPlan> = repository.subscriptionPlans
    val currentTier: StateFlow<SubscriptionTier> = repository.currentTier
    val orders: StateFlow<List<Order>> = repository.orders
    val prescriptions: StateFlow<List<PrescriptionScan>> = repository.prescriptions

    // Production Modules Streams
    val quarantineRecords: StateFlow<List<BatchQuarantineRecord>> = repository.quarantineRecords
    val scannedBarcodes: StateFlow<List<ScannedBarcodeData>> = repository.scannedBarcodes
    val suppliers: List<PharmaSupplier> = repository.suppliers
    val purchaseOrders: StateFlow<List<PurchaseOrder>> = repository.purchaseOrders
    val coldChainTelemetry: StateFlow<ColdChainTelemetryData> = repository.coldChainTelemetry
    val ledgerTransactions: StateFlow<List<LedgerTransaction>> = repository.ledgerTransactions
    val partnerSettlements: StateFlow<List<PartnerSettlementSummary>> = repository.partnerSettlements
    val activeJurisdiction: StateFlow<RegulatoryJurisdiction> = repository.activeJurisdiction
    val regulatoryProfiles: Map<RegulatoryJurisdiction, CountryRegulatoryProfile> = repository.regulatoryProfiles
    val activePharmacistCredential: StateFlow<PharmacistCredential> = repository.activePharmacistCredential
    val activeUserRole: StateFlow<UserAccessRole> = repository.activeUserRole
    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs
    val assistantMessages: StateFlow<List<AssistantChatMessage>> = repository.assistantMessages
    val activeLanguage: StateFlow<AssistantLanguage> = repository.activeLanguage
    val stockMovements: StateFlow<List<StockMovementRecord>> = repository.stockMovements

    // Global Country & Currency System
    val activeCountry: StateFlow<CountryConfig> = repository.activeCountry
    val availableCountries: StateFlow<List<CountryConfig>> = repository.availableCountries

    // Inventory Dashboard Search, Filter & Sort states
    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _expiryFilter = MutableStateFlow(ExpiryFilterWindow.ALL)
    val expiryFilter: StateFlow<ExpiryFilterWindow> = _expiryFilter.asStateFlow()

    private val _inventorySort = MutableStateFlow(InventorySortOrder.FEFO_EXPIRY_ASC)
    val inventorySort: StateFlow<InventorySortOrder> = _inventorySort.asStateFlow()

    // FEFO Allocation Calculator state
    private val _fefoAllocations = MutableStateFlow<List<FefoAllocationItem>>(emptyList())
    val fefoAllocations: StateFlow<List<FefoAllocationItem>> = _fefoAllocations.asStateFlow()

    private val _csvReport = MutableStateFlow<String?>(null)
    val csvReport: StateFlow<String?> = _csvReport.asStateFlow()

    // Cart
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Search query & Category filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MedicineCategory.ALL)
    val selectedCategory: StateFlow<MedicineCategory> = _selectedCategory.asStateFlow()

    // Notifications
    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // OCR Scanning State
    private val _isOcrScanning = MutableStateFlow(false)
    val isOcrScanning: StateFlow<Boolean> = _isOcrScanning.asStateFlow()

    private val _activePrescription = MutableStateFlow<PrescriptionScan?>(null)
    val activePrescription: StateFlow<PrescriptionScan?> = _activePrescription.asStateFlow()

    // JV Discovery Dialog State
    private val _jvDiscoveryState = MutableStateFlow<JVSyncResult?>(null)
    val jvDiscoveryState: StateFlow<JVSyncResult?> = _jvDiscoveryState.asStateFlow()

    // Order Success Dialog State
    private val _confirmedOrder = MutableStateFlow<Order?>(null)
    val confirmedOrder: StateFlow<Order?> = _confirmedOrder.asStateFlow()

    // Live IoT Temperature for Cold Chain
    private val _liveTelemetryTemp = MutableStateFlow(4.2f)
    val liveTelemetryTemp: StateFlow<Float> = _liveTelemetryTemp.asStateFlow()

    // Global Settings
    private val _globalComplianceActive = MutableStateFlow(true)
    val globalComplianceActive: StateFlow<Boolean> = _globalComplianceActive.asStateFlow()

    init {
        // Automatically set initial active prescription
        _activePrescription.value = repository.prescriptions.value.firstOrNull()

        // Real-time IoT temperature telemetry heartbeat
        viewModelScope.launch {
            var toggle = true
            while (true) {
                delay(3500)
                toggle = !toggle
                _liveTelemetryTemp.value = if (toggle) 4.1f else 4.4f
            }
        }
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
        _currentTab.value = 0
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: MedicineCategory) {
        _selectedCategory.value = category
    }

    fun clearNotification() {
        _notification.value = null
    }

    fun showToast(msg: String, isError: Boolean = false) {
        _notification.value = UiNotification(message = msg, isError = isError)
    }

    // Cart Actions (With strict Quarantine / Recall Protection)
    fun addToCart(medicine: MedicineItem) {
        if (medicine.isRecalledOrQuarantined || repository.isBatchOrMedicineBlocked(medicine.id)) {
            showToast("BLOCKED: Batch is RECALLED / QUARANTINED by Health Authority!", isError = true)
            return
        }

        if (medicine.stockTotal <= 0) {
            // Trigger Joint Venture Engine!
            triggerJvDiscovery(medicine)
            return
        }

        _cartItems.update { current ->
            val existing = current.find { it.medicine.id == medicine.id }
            if (existing != null) {
                current.map {
                    if (it.medicine.id == medicine.id) it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                current + CartItem(medicine, 1)
            }
        }
        showToast("Added ${medicine.name} to cart")
    }

    fun removeFromCart(medicineId: String) {
        _cartItems.update { current ->
            current.filterNot { it.medicine.id == medicineId }
        }
    }

    fun updateCartQty(medicineId: String, delta: Int) {
        _cartItems.update { current ->
            current.mapNotNull { item ->
                if (item.medicine.id == medicineId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else item
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Joint Venture Out-of-Stock Discovery
    fun triggerJvDiscovery(medicine: MedicineItem) {
        val result = repository.discoverJVPartnerForMedicine(medicine)
        _jvDiscoveryState.value = result
    }

    fun dismissJvDiscovery() {
        _jvDiscoveryState.value = null
    }

    fun acceptJvPartnerRoute(result: JVSyncResult) {
        val medicine = medicines.value.find { it.name == result.requestedMedicine }
            ?: return
        _cartItems.update { current ->
            current + CartItem(medicine.copy(stockTotal = result.stockAvailable), 1)
        }
        _jvDiscoveryState.value = null
        showToast("Routed via JV Partner: ${result.fulfillingPartner.name} (Escrow Secured)")
    }

    // Checkout & Order Placement
    fun checkout(paymentMethod: PaymentMethod) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        // Verify none of the items are quarantined
        val blockedItem = items.find { it.medicine.isRecalledOrQuarantined || repository.isBatchOrMedicineBlocked(it.medicine.id) }
        if (blockedItem != null) {
            showToast("Checkout blocked: ${blockedItem.medicine.name} is under safety quarantine!", isError = true)
            return
        }

        val isPremium = currentTier.value == SubscriptionTier.B2C_PREMIUM
        val subtotal = items.sumOf { it.medicine.price * it.quantity }
        val deliveryFee = if (isPremium) 0.0 else 9.50
        val total = subtotal + deliveryFee
        val hasJv = items.any { it.medicine.stockTotal == 0 || it.medicine.name.contains("Ozempic") }

        val split = PharmaRepository.calculateEscrowSplit(total, isJv = hasJv)
        val orderId = "PHX-${(1000..9999).random()}"
        val order = Order(
            id = orderId,
            customerName = "Anish Kumar",
            deliveryAddress = "Marina Promenade Tower A, Dubai",
            items = items,
            subtotal = (subtotal * 100).toLong() / 100.0,
            deliveryFee = deliveryFee,
            totalAmount = (total * 100).toLong() / 100.0,
            isJointVentureRoute = hasJv,
            jvPartnerName = if (hasJv) "Apex Medical Care Pharmacy (Dubai)" else null,
            escrowSplit = split,
            paymentMethod = paymentMethod,
            deliveryStage = DeliveryStage.CONFIRMED,
            driverName = "Capt. Omar Al-Mansoor",
            driverPhone = "+971 50 123 4567",
            liveTempCelsius = 4.2f,
            targetTempRange = "2°C - 8°C",
            etaMinutes = if (isPremium) 15 else 35,
            createdAtFormatted = SimpleDateFormat("dd-MMM HH:mm", Locale.getDefault()).format(Date())
        )

        repository.addOrder(order)
        clearCart()
        _confirmedOrder.value = order
    }

    fun dismissOrderConfirmation() {
        _confirmedOrder.value = null
    }

    // Subscription
    fun setSubscriptionTier(tier: SubscriptionTier) {
        repository.updateSubscriptionTier(tier)
        showToast("Switched active plan to ${tier.title}")
    }

    fun togglePillTaken(id: String) {
        repository.togglePillTaken(id)
    }

    // OCR Scan Simulation
    fun startPrescriptionOcrScan(sampleIndex: Int = 0) {
        viewModelScope.launch {
            _isOcrScanning.value = true
            delay(1800) // Simulated optical character recognition processing
            _isOcrScanning.value = false

            val newScan = if (sampleIndex == 1) {
                PrescriptionScan(
                    id = "RX-${UUID.randomUUID().toString().take(6).uppercase()}",
                    patientName = "Fatima Al-Zahra (Age 29)",
                    doctorName = "Dr. Michael Chen, MD, FCCP",
                    doctorLicense = "MOH-P-77192",
                    clinicHospital = "Al Zahra Respiratory Medical Center",
                    issuedDate = "06-Nov-2026",
                    rawOcrText = """
                        AL ZAHRA RESPIRATORY CENTER
                        PATIENT: Fatima Al-Zahra (Age 29)
                        PHYSICIAN: Dr. Michael Chen (Lic # MOH-P-77192)
                        --------------------------------------------
                        1. Ventolin Evohaler (Salbutamol 100mcg)
                           Sig: 2 puffs inhaled PRN for acute wheezing.
                        2. Augmentin 1000mg Duo
                           Sig: 1 tab BID x 7 days with food.
                        --------------------------------------------
                        Verified Digital Rx [Valid until Dec 2026]
                    """.trimIndent(),
                    extractedDrugs = listOf(
                        ExtractedDrug(
                            drugName = "Ventolin Evohaler (Salbutamol)",
                            matchedMedicineId = "med_5",
                            strength = "100 mcg",
                            dosage = "2 Puffs",
                            frequency = "As needed for wheezing",
                            durationDays = 30,
                            confidenceScore = 0.99f,
                            isAvailableInStock = true
                        ),
                        ExtractedDrug(
                            drugName = "Augmentin 1000mg Duo",
                            matchedMedicineId = "med_2",
                            strength = "1000 mg",
                            dosage = "1 Tablet",
                            frequency = "Twice daily with meals",
                            durationDays = 7,
                            confidenceScore = 0.97f,
                            isAvailableInStock = true
                        )
                    ),
                    safetyInteractions = listOf(
                        "Formulary Safe: No drug-drug contradictions between Salbutamol and Augmentin.",
                        "Antibiotic Stewardship: 7-day course confirmed."
                    ),
                    verificationState = VerificationState.PHARMACIST_APPROVED,
                    pharmacistNote = "Verified by Chief Pharmacist Kareem A. (License # DHA-3101)"
                )
            } else {
                repository.prescriptions.value.first()
            }

            _activePrescription.value = newScan
            repository.addPrescription(newScan)
            showToast("OCR Backbone: Extracted ${newScan.extractedDrugs.size} items with 98% confidence")
        }
    }

    // Add Batch in Pharmacy
    fun addManualBatch(
        medName: String,
        batchNo: String,
        qty: Int,
        expiry: String,
        temp: String
    ) {
        val newBatch = BatchRecord(
            batchId = "b_${System.currentTimeMillis() % 10000}",
            medicineId = "med_manual",
            medicineName = medName,
            batchNumber = batchNo,
            mfgDate = "01-Nov-2026",
            expiryDate = expiry,
            quantity = qty,
            storageTempRange = temp,
            status = BatchStatus.OPTIMAL
        )
        repository.addBatch(newBatch)
        showToast("Batch $batchNo cataloged in inventory")
    }

    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
    }

    fun setExpiryFilter(filter: ExpiryFilterWindow) {
        _expiryFilter.value = filter
    }

    fun setInventorySort(sort: InventorySortOrder) {
        _inventorySort.value = sort
    }

    fun calculateFefo(medicineId: String, qty: Int) {
        val result = repository.calculateFefoAllocation(medicineId, qty)
        _fefoAllocations.value = result
    }

    fun editBatch(batch: BatchRecord, actor: String = "Dr. Salma Al-Khatib") {
        repository.editBatch(batch, actor)
        showToast("Batch ${batch.batchNumber} updated successfully")
    }

    fun adjustBatchStock(
        batchNumber: String,
        delta: Int,
        reason: String,
        referenceId: String,
        actor: String = "Inventory Auditor"
    ) {
        val success = repository.adjustStock(batchNumber, delta, reason, referenceId, actor)
        if (success) {
            val sign = if (delta >= 0) "+$delta" else "$delta"
            showToast("Stock adjusted ($sign units) for Batch $batchNumber")
        } else {
            showToast("Stock adjustment failed: resulting units cannot be negative", isError = true)
        }
    }

    fun transferBatch(
        batchNumber: String,
        newLocation: String,
        actor: String = "Logistics Handler"
    ) {
        repository.transferBatchLocation(batchNumber, newLocation, actor)
        showToast("Batch $batchNumber transferred to $newLocation")
    }

    fun archiveBatch(batchNumber: String, actor: String = "Compliance Manager") {
        repository.archiveBatch(batchNumber, actor)
        showToast("Batch $batchNumber archived from active fulfillment")
    }

    fun generateCsvExport(): String {
        val csv = repository.generateInventoryCsvReport()
        _csvReport.value = csv
        showToast("Inventory CSV report generated")
        return csv
    }

    fun dismissCsvReport() {
        _csvReport.value = null
    }

    fun attachPersistence(context: Context) {
        val db = PharmaDatabase.getInstance(context)
        repository.attachDatabase(db)
    }

    fun reserveBatchStock(batchNumber: String, requestedQty: Int): Boolean {
        val success = repository.reserveBatchStock(batchNumber, requestedQty)
        if (success) {
            showToast("Reserved $requestedQty units in lot $batchNumber")
        } else {
            showToast("Stock reservation failed: Insufficient eligible units", isError = true)
        }
        return success
    }

    fun releaseBatchReservation(batchNumber: String, qty: Int): Boolean {
        val success = repository.releaseBatchReservation(batchNumber, qty)
        if (success) {
            showToast("Released reservation of $qty units in lot $batchNumber")
        }
        return success
    }

    fun toggleCompliance() {
        _globalComplianceActive.value = !_globalComplianceActive.value
        showToast(
            if (_globalComplianceActive.value) "UAE MOHAP & Global Regulatory Gate Active"
            else "Testing Sandbox Mode Active"
        )
    }

    // --- Production Actions: Quarantine, Barcode, PO, Cold-Chain, AI Assistant ---

    fun quarantineBatch(
        batchNumber: String,
        reason: QuarantineReason,
        noticeId: String,
        quarantinedBy: String
    ) {
        repository.quarantineBatch(batchNumber, reason, noticeId, quarantinedBy)
        showToast("Batch $batchNumber quarantined! Sales and JV transfers blocked.", isError = true)
    }

    fun scanBarcode(rawCode: String) {
        val result = repository.verifyBarcodeScan(rawCode)
        showToast("Scanned: ${result.verificationResult.label}")
    }

    fun createPurchaseOrder(supplierId: String, items: List<PurchaseOrderItem>) {
        val po = repository.createPurchaseOrder(supplierId, items)
        showToast("Created PO #${po.poNumber} ($${po.totalCost})")
    }

    fun approvePurchaseOrder(poId: String, pharmacistName: String) {
        repository.approvePurchaseOrder(poId, pharmacistName)
        showToast("PO $poId clinically approved by $pharmacistName")
    }

    fun triggerColdChainExcursion(peakTempC: Float, durationMins: Int) {
        repository.triggerSimulatedExcursion(peakTempC, durationMins)
        if (peakTempC > 8.0f) {
            showToast("Cold-Chain Breach (${peakTempC}°C)! Batch auto-quarantined.", isError = true)
        } else {
            showToast("Temperature restored to safe range.")
        }
    }

    fun releaseEscrowForOrder(orderId: String, recipientName: String, otpCode: String) {
        val success = repository.releaseEscrowForOrder(orderId, recipientName, otpCode)
        if (success) {
            showToast("Escrow Released! Partner & Courier payouts cleared.")
        } else {
            showToast("Invalid Delivery OTP. Escrow remains secured in vault.", isError = true)
        }
    }

    fun switchJurisdiction(jurisdiction: RegulatoryJurisdiction) {
        repository.setJurisdiction(jurisdiction)
        showToast("Formulary switched to ${jurisdiction.authorityName}")
    }

    fun selectCountry(countryCode: String) {
        repository.selectCountry(countryCode, isManual = true)
        val selected = repository.activeCountry.value
        showToast("Active country switched to ${selected.countryName} (${selected.currencyCode} ${selected.currencySymbol})")
    }

    fun formatCurrency(amount: Double): String {
        return repository.formatCurrency(amount)
    }

    fun setAssistantLanguage(lang: AssistantLanguage) {
        repository.setAssistantLanguage(lang)
    }

    fun askGeminiAssistant(prompt: String) {
        repository.queryGeminiAssistant(prompt, activeLanguage.value)
    }
}
