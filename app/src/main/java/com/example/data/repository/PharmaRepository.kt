package com.example.data.repository

import com.example.data.local.PharmaDatabase
import com.example.data.local.entity.BatchEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class PharmaRepository(
    var database: PharmaDatabase? = null,
    val countryRepository: CountryConfigRepository = CountryConfigRepository(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    init {
        database?.let { setupDatabaseHooks(it) }
    }

    fun attachDatabase(db: PharmaDatabase) {
        this.database = db
        setupDatabaseHooks(db)
    }

    private fun setupDatabaseHooks(db: PharmaDatabase) {
        coroutineScope.launch {
            try {
                if (db.batchDao().getCount() == 0) {
                    db.batchDao().insertBatches(initialBatches.map { BatchEntity.fromDomain(it) })
                }
                if (db.stockMovementDao().getCount() == 0) {
                    db.stockMovementDao().insertMovements(initialStockMovements.map { StockMovementEntity.fromDomain(it) })
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
        coroutineScope.launch {
            db.batchDao().getAllBatchesFlow().collect { entities ->
                if (entities.isNotEmpty()) {
                    _batches.value = entities.map { it.toDomain() }
                }
            }
        }
        coroutineScope.launch {
            db.stockMovementDao().getAllMovementsFlow().collect { entities ->
                if (entities.isNotEmpty()) {
                    _stockMovements.value = entities.map { it.toDomain() }
                }
            }
        }
    }

    // Initial Medicines
    private val initialMedicines = listOf(
        MedicineItem(
            id = "med_1",
            name = "Lantus SoloStar (Insulin Glargine)",
            genericName = "Insulin Glargine 100 U/mL",
            brand = "Sanofi Aventis",
            strength = "100 U/mL (5x3mL Pens)",
            category = MedicineCategory.COLD_CHAIN,
            price = 68.50,
            requiresPrescription = true,
            description = "Long-acting human insulin analog for glycemic control in diabetes mellitus. Requires strict cold-chain refrigeration.",
            stockTotal = 14,
            isColdChainRequired = true,
            recommendedTemp = "2°C - 8°C (Do Not Freeze)",
            dosageInstructions = "Inject subcutaneously once daily at the same time every day.",
            precautions = "Keep in chilled cold-chain storage. Never expose to heat.",
            barcodeGtin = "06291002019231"
        ),
        MedicineItem(
            id = "med_2",
            name = "Augmentin 1000mg Duo",
            genericName = "Amoxicillin + Clavulanate Potassium",
            brand = "GSK Healthcare",
            strength = "1000 mg (14 Tablets)",
            category = MedicineCategory.PRESCRIPTION,
            price = 24.80,
            requiresPrescription = true,
            description = "Broad-spectrum antibacterial for respiratory, ENT, and soft-tissue bacterial infections.",
            stockTotal = 42,
            isColdChainRequired = false,
            recommendedTemp = "15°C - 25°C (Cool & Dry)",
            dosageInstructions = "Take one tablet every 12 hours with meals.",
            precautions = "Complete full course as prescribed by licensed physician.",
            barcodeGtin = "06291002019248"
        ),
        MedicineItem(
            id = "med_3",
            name = "Lipitor (Atorvastatin Calcium)",
            genericName = "Atorvastatin",
            brand = "Pfizer Global",
            strength = "20 mg (30 Film-coated tabs)",
            category = MedicineCategory.CHRONIC_CARE,
            price = 32.00,
            requiresPrescription = true,
            description = "HMG-CoA reductase inhibitor for lowering LDL cholesterol and cardiovascular risk reduction.",
            stockTotal = 28,
            isColdChainRequired = false,
            recommendedTemp = "20°C - 25°C",
            dosageInstructions = "Take once daily in the evening.",
            precautions = "Avoid large quantities of grapefruit juice while taking this medication.",
            barcodeGtin = "06291002019255"
        ),
        MedicineItem(
            id = "med_4",
            name = "Ozempic DualDose Pen",
            genericName = "Semaglutide 2mg / 1.5mL",
            brand = "Novo Nordisk",
            strength = "2 mg pre-filled pen",
            category = MedicineCategory.COLD_CHAIN,
            price = 145.00,
            requiresPrescription = true,
            description = "GLP-1 receptor agonist for type 2 diabetes management and glycemic control. Pharma-grade cold storage essential.",
            stockTotal = 0, // OUT OF STOCK LOCALLY -> Triggers Joint Venture (JV) Engine!
            isColdChainRequired = true,
            recommendedTemp = "2°C - 8°C (Refrigerated)",
            dosageInstructions = "Subcutaneous injection once weekly on same day.",
            precautions = "Strict cold-chain monitoring during delivery required.",
            barcodeGtin = "06291002019262"
        ),
        MedicineItem(
            id = "med_5",
            name = "Ventolin Evohaler",
            genericName = "Salbutamol Sulfate 100mcg",
            brand = "GlaxoSmithKline",
            strength = "100 mcg (200 actuations)",
            category = MedicineCategory.EMERGENCY,
            price = 18.20,
            requiresPrescription = true,
            description = "Fast-acting bronchodilator for immediate relief of acute asthma bronchospasm and wheezing.",
            stockTotal = 19,
            isColdChainRequired = false,
            recommendedTemp = "Below 30°C",
            dosageInstructions = "Inhale 1 to 2 puffs during sudden shortness of breath.",
            precautions = "Keep inhaler clean and always carry for acute emergencies.",
            barcodeGtin = "06291002019279"
        ),
        MedicineItem(
            id = "med_6",
            name = "Panadol Extra Advance",
            genericName = "Paracetamol 500mg + Caffeine 65mg",
            brand = "Haleon / GSK",
            strength = "Pack of 24 Caplets",
            category = MedicineCategory.OVER_THE_COUNTER,
            price = 6.40,
            requiresPrescription = false,
            description = "Fast pain and fever relief for headaches, migraine, backache, and toothache.",
            stockTotal = 120,
            isColdChainRequired = false,
            recommendedTemp = "15°C - 25°C",
            dosageInstructions = "1-2 caplets every 4-6 hours as needed. Do not exceed 8 caplets in 24 hours.",
            precautions = "Do not take with other acetaminophen/paracetamol containing products.",
            barcodeGtin = "06291002019286"
        ),
        MedicineItem(
            id = "med_recalled_sample",
            name = "Valsartan 160mg (RECALLED BATCH)",
            genericName = "Valsartan BP",
            brand = "CardioPharma Labs",
            strength = "160 mg (28 Tablets)",
            category = MedicineCategory.RECALLED,
            price = 21.00,
            requiresPrescription = true,
            description = "RECALLED: Global health authority safety notice due to NDMA trace detection. Blocked from dispensing.",
            stockTotal = 0,
            isColdChainRequired = false,
            recommendedTemp = "15°C - 25°C",
            isRecalledOrQuarantined = true,
            quarantineNotice = "MOHAP RECALL NOTICE #RC-2026-091: Trace impurity detected. Prohibited from sale.",
            barcodeGtin = "06291002019999"
        )
    )

    private val _medicines = MutableStateFlow(initialMedicines)
    val medicines: StateFlow<List<MedicineItem>> = _medicines.asStateFlow()

    // Batches for B2B Inventory
    private val initialBatches = listOf(
        BatchRecord(
            batchId = "b_01",
            medicineId = "med_1",
            medicineName = "Lantus SoloStar (Insulin)",
            batchNumber = "BAT-2026-X88",
            mfgDate = "15-Jan-2026",
            expiryDate = "28-Feb-2027",
            quantity = 14,
            minThreshold = 20,
            storageTempRange = "2°C - 8°C",
            currentTempCelsius = 4.2f,
            warehouseLocation = "Cold Vault #1 - Shelf C",
            status = BatchStatus.LOW_STOCK,
            isQuarantined = false
        ),
        BatchRecord(
            batchId = "b_02",
            medicineId = "med_2",
            medicineName = "Augmentin 1000mg Duo",
            batchNumber = "BAT-2025-A12",
            mfgDate = "10-Nov-2025",
            expiryDate = "15-Dec-2026",
            quantity = 42,
            minThreshold = 15,
            storageTempRange = "15°C - 25°C",
            currentTempCelsius = 21.0f,
            warehouseLocation = "Aisle 3 - Bay B2",
            status = BatchStatus.OPTIMAL,
            isQuarantined = false
        ),
        BatchRecord(
            batchId = "b_03",
            medicineId = "med_3",
            medicineName = "Lipitor 20mg",
            batchNumber = "BAT-2024-K09",
            mfgDate = "05-Aug-2024",
            expiryDate = "30-Nov-2026",
            quantity = 28,
            minThreshold = 25,
            storageTempRange = "20°C - 25°C",
            currentTempCelsius = 22.4f,
            warehouseLocation = "Aisle 1 - Bay F4",
            status = BatchStatus.EXPIRING_SOON,
            isQuarantined = false
        ),
        BatchRecord(
            batchId = "b_04",
            medicineId = "med_4",
            medicineName = "Ozempic 2mg DualDose",
            batchNumber = "BAT-2026-Z00",
            mfgDate = "01-Jan-2026",
            expiryDate = "31-Mar-2027",
            quantity = 0,
            minThreshold = 10,
            storageTempRange = "2°C - 8°C",
            currentTempCelsius = 4.0f,
            warehouseLocation = "Cold Vault #2 (Depleted)",
            status = BatchStatus.LOW_STOCK,
            isQuarantined = false
        ),
        BatchRecord(
            batchId = "b_05",
            medicineId = "med_5",
            medicineName = "Ventolin Evohaler",
            batchNumber = "BAT-2026-V44",
            mfgDate = "12-Feb-2026",
            expiryDate = "12-Feb-2028",
            quantity = 19,
            minThreshold = 10,
            storageTempRange = "Below 30°C",
            currentTempCelsius = 21.8f,
            warehouseLocation = "Emergency Station Bay",
            status = BatchStatus.OPTIMAL,
            isQuarantined = false
        ),
        BatchRecord(
            batchId = "b_06_quarantine",
            medicineId = "med_recalled_sample",
            medicineName = "Valsartan 160mg",
            batchNumber = "BAT-2025-RECALL-99",
            mfgDate = "01-Jul-2025",
            expiryDate = "30-Jun-2027",
            quantity = 150,
            minThreshold = 10,
            storageTempRange = "15°C - 25°C",
            currentTempCelsius = 22.0f,
            warehouseLocation = "Quarantine Cage Q-1",
            status = BatchStatus.RECALLED_QUARANTINED,
            isQuarantined = true,
            quarantineReason = "Authority Recall Notice #RC-2026-091: Trace NDMA. Barcode scans auto-block."
        )
    )

    private val _batches = MutableStateFlow(initialBatches)
    val batches: StateFlow<List<BatchRecord>> = _batches.asStateFlow()

    // Immutable Stock Movement Ledger
    private val initialStockMovements = listOf(
        StockMovementRecord(
            id = "SM-001",
            batchNumber = "BAT-2026-X88",
            medicineName = "Lantus SoloStar (Insulin)",
            movementType = StockMovementType.GOODS_RECEIPT,
            quantityDelta = 50,
            balanceAfter = 50,
            actorName = "Inventory Team A",
            actorRole = "Inventory Coordinator",
            reason = "Initial Inward Goods Receipt PO-DXB-101",
            referenceId = "PO-DXB-101",
            timestampFormatted = "01-Nov 09:30"
        ),
        StockMovementRecord(
            id = "SM-002",
            batchNumber = "BAT-2026-X88",
            medicineName = "Lantus SoloStar (Insulin)",
            movementType = StockMovementType.DISPENSE_SALE,
            quantityDelta = -36,
            balanceAfter = 14,
            actorName = "System Auto-Dispense",
            actorRole = "System Order Router",
            reason = "Prescription Refill Order #PHX-8902",
            referenceId = "PHX-8902",
            timestampFormatted = "04-Nov 14:15"
        ),
        StockMovementRecord(
            id = "SM-003",
            batchNumber = "BAT-2025-A12",
            medicineName = "Augmentin 1000mg Duo",
            movementType = StockMovementType.GOODS_RECEIPT,
            quantityDelta = 60,
            balanceAfter = 60,
            actorName = "Inventory Team B",
            actorRole = "Receiving Officer",
            reason = "Supplier PO Delivery",
            referenceId = "PO-DXB-094",
            timestampFormatted = "02-Nov 11:00"
        ),
        StockMovementRecord(
            id = "SM-004",
            batchNumber = "BAT-2025-A12",
            medicineName = "Augmentin 1000mg Duo",
            movementType = StockMovementType.DISPENSE_SALE,
            quantityDelta = -18,
            balanceAfter = 42,
            actorName = "Dr. Salma Al-Khatib",
            actorRole = "Licensed Pharmacist",
            reason = "Dispensed Outpatient Order #PHX-9082",
            referenceId = "PHX-9082",
            timestampFormatted = "05-Nov 16:20"
        ),
        StockMovementRecord(
            id = "SM-005",
            batchNumber = "BAT-2025-RECALL-99",
            medicineName = "Valsartan 160mg",
            movementType = StockMovementType.QUARANTINE_LOCK,
            quantityDelta = -150,
            balanceAfter = 0,
            actorName = "Compliance Sentinel",
            actorRole = "Compliance Officer",
            reason = "MOHAP Recall Notice #RC-2026-091 - Trace Impurity",
            referenceId = "RC-2026-091",
            timestampFormatted = "06-Nov 09:14"
        )
    )
    private val _stockMovements = MutableStateFlow(initialStockMovements)
    val stockMovements: StateFlow<List<StockMovementRecord>> = _stockMovements.asStateFlow()

    // Recall & Quarantine Records
    private val initialQuarantineRecords = listOf(
        BatchQuarantineRecord(
            id = "QRN-2026-001",
            batchNumber = "BAT-2025-RECALL-99",
            medicineId = "med_recalled_sample",
            medicineName = "Valsartan 160mg",
            manufacturer = "CardioPharma Labs",
            reason = QuarantineReason.REGULATORY_RECALL,
            status = QuarantineStatus.QUARANTINED,
            quarantinedBy = "Chief Pharmacist Dr. Salma K. (DHA #8831)",
            authorityNoticeId = "MOHAP-ALERT-2026-091",
            affectedUnits = 150,
            patientAlertsDispatched = 18,
            resolutionNotes = "Stock moved to locked red-tag quarantine cage. Vendor credit issued."
        )
    )
    private val _quarantineRecords = MutableStateFlow(initialQuarantineRecords)
    val quarantineRecords: StateFlow<List<BatchQuarantineRecord>> = _quarantineRecords.asStateFlow()

    // Barcode Scanning Log
    private val initialScannedBarcodes = listOf(
        ScannedBarcodeData(
            gtin = "06291002019231",
            serialNumber = "SN-982187-DXB",
            batchNumber = "BAT-2026-X88",
            expiryDate = "28-Feb-2027",
            rawPayload = "(01)06291002019231(17)270228(10)BAT-2026-X88(21)SN-982187-DXB",
            verificationResult = BarcodeVerificationResult.GENUINE_VERIFIED,
            medicineName = "Lantus SoloStar (Insulin)",
            trackAndTraceId = "TATMEEN-AE-889102",
            manufacturer = "Sanofi Aventis"
        ),
        ScannedBarcodeData(
            gtin = "06291002019999",
            serialNumber = "SN-000412-RC",
            batchNumber = "BAT-2025-RECALL-99",
            expiryDate = "30-Jun-2027",
            rawPayload = "(01)06291002019999(17)270630(10)BAT-2025-RECALL-99(21)SN-000412-RC",
            verificationResult = BarcodeVerificationResult.QUARANTINED_BLOCKED,
            medicineName = "Valsartan 160mg",
            trackAndTraceId = "TATMEEN-RECALL-ALERT",
            manufacturer = "CardioPharma Labs"
        )
    )
    private val _scannedBarcodes = MutableStateFlow(initialScannedBarcodes)
    val scannedBarcodes: StateFlow<List<ScannedBarcodeData>> = _scannedBarcodes.asStateFlow()

    // Suppliers Directory
    val suppliers = listOf(
        PharmaSupplier(
            id = "sup_01",
            name = "Novartis Middle East Logistics",
            licenseNumber = "MOHAP-SUP-88219",
            country = "United Arab Emirates",
            rating = 4.95f,
            contactEmail = "orders.gulf@novartis.com",
            leadTimeDays = 2,
            isGmpCertified = true
        ),
        PharmaSupplier(
            id = "sup_02",
            name = "Sanofi Gulf Distribution Hub",
            licenseNumber = "MOHAP-SUP-77142",
            country = "United Arab Emirates",
            rating = 4.90f,
            contactEmail = "supply.uae@sanofi.com",
            leadTimeDays = 1,
            isGmpCertified = true
        ),
        PharmaSupplier(
            id = "sup_03",
            name = "GSK Healthcare Regional Dist",
            licenseNumber = "MOHAP-SUP-44901",
            country = "United Arab Emirates",
            rating = 4.85f,
            contactEmail = "commercial@gsk.com",
            leadTimeDays = 3,
            isGmpCertified = true
        )
    )

    // Purchase Orders
    private val initialPurchaseOrders = listOf(
        PurchaseOrder(
            id = "PO-2026-901",
            poNumber = "PO-DXB-901",
            supplierId = "sup_01",
            supplierName = "Novartis Middle East Logistics",
            items = listOf(
                PurchaseOrderItem("med_4", "Ozempic 2mg DualDose", 100, 110.0, "2°C - 8°C"),
                PurchaseOrderItem("med_1", "Lantus SoloStar (Insulin)", 50, 48.0, "2°C - 8°C")
            ),
            totalCost = 13400.0,
            orderDate = "04-Nov-2026",
            expectedDeliveryDate = "08-Nov-2026",
            status = PurchaseOrderStatus.IN_TRANSIT,
            approvedByPharmacist = "Dr. Salma K. (DHA #8831)",
            temperatureAtArrivalC = null,
            qaInspectionNotes = "Shipped via Temperature-Controlled Cold-Chain Reefer."
        ),
        PurchaseOrder(
            id = "PO-2026-902",
            poNumber = "PO-DXB-902",
            supplierId = "sup_03",
            supplierName = "GSK Healthcare Regional Dist",
            items = listOf(
                PurchaseOrderItem("med_2", "Augmentin 1000mg Duo", 200, 16.5, "15°C - 25°C")
            ),
            totalCost = 3300.0,
            orderDate = "01-Nov-2026",
            expectedDeliveryDate = "03-Nov-2026",
            status = PurchaseOrderStatus.RECEIVED_AND_INSPECTED,
            approvedByPharmacist = "Dr. Tariq M. (DHA #4401)",
            temperatureAtArrivalC = 21.4f,
            qaInspectionNotes = "Batch seals inspected. Released to primary retail stock."
        )
    )
    private val _purchaseOrders = MutableStateFlow(initialPurchaseOrders)
    val purchaseOrders: StateFlow<List<PurchaseOrder>> = _purchaseOrders.asStateFlow()

    // Advanced Cold-Chain IoT Multi-Sensor Telemetry
    private val initialColdChainTelemetry = ColdChainTelemetryData(
        sensorId = "IOT-SENS-7741",
        containerId = "VAULT-REFRIG-COLD-01",
        liveTempC = 4.2f,
        ambientTempC = 26.8f,
        targetMinC = 2.0f,
        targetMaxC = 8.0f,
        humidityRh = 44,
        meanKineticTempC = 4.3f,
        tamperSealStatus = TamperSealStatus.SEAL_LOCKED_INTACT,
        batteryPercent = 94,
        excursions = listOf(
            ExcursionEvent(
                id = "EXC-01",
                timestamp = "Yesterday 14:15",
                peakTempC = 5.8f,
                durationMinutes = 6,
                meanKineticTempC = 4.6f,
                severity = "MINOR",
                actionTaken = "Container door closed promptly. In-spec."
            )
        ),
        isComplianceValid = true
    )
    private val _coldChainTelemetry = MutableStateFlow(initialColdChainTelemetry)
    val coldChainTelemetry: StateFlow<ColdChainTelemetryData> = _coldChainTelemetry.asStateFlow()

    // Financial Double-Entry Ledger
    private val initialLedger = listOf(
        LedgerTransaction(
            id = "TXN-901",
            orderId = "PHX-9082",
            timestamp = "06-Nov 10:42",
            type = TransactionType.CUSTOMER_ESCROW_DEPOSIT,
            sourceAccount = "Customer Card (Stripe Connect)",
            destinationAccount = "PharmaX Escrow Vault",
            grossAmount = 169.80,
            netAmount = 169.80,
            escrowLocked = true,
            status = "ESCROW_LOCKED"
        ),
        LedgerTransaction(
            id = "TXN-902",
            orderId = "PHX-9082",
            timestamp = "06-Nov 10:42",
            type = TransactionType.PARTNER_SHARE_PAYOUT,
            sourceAccount = "PharmaX Escrow Vault",
            destinationAccount = "Apex Medical Care Pharmacy",
            grossAmount = 144.33,
            netAmount = 144.33,
            escrowLocked = true,
            status = "PENDING_DELIVERY_RELEASE"
        ),
        LedgerTransaction(
            id = "TXN-903",
            orderId = "PHX-9082",
            timestamp = "06-Nov 10:42",
            type = TransactionType.JV_REFERRAL_COMMISSION,
            sourceAccount = "PharmaX Escrow Vault",
            destinationAccount = "PharmaX Hub #01 (Referral)",
            grossAmount = 8.49,
            netAmount = 8.49,
            escrowLocked = true,
            status = "PENDING_DELIVERY_RELEASE"
        ),
        LedgerTransaction(
            id = "TXN-904",
            orderId = "PHX-9082",
            timestamp = "06-Nov 10:42",
            type = TransactionType.COLD_LOGISTICS_FEE,
            sourceAccount = "PharmaX Escrow Vault",
            destinationAccount = "Cold-Chain Fleet Logistics Partner",
            grossAmount = 11.88,
            netAmount = 11.88,
            escrowLocked = true,
            status = "PENDING_DELIVERY_RELEASE"
        ),
        LedgerTransaction(
            id = "TXN-905",
            orderId = "PHX-9082",
            timestamp = "06-Nov 10:42",
            type = TransactionType.PLATFORM_ESCROW_FEE,
            sourceAccount = "PharmaX Escrow Vault",
            destinationAccount = "PharmaX Platform Fee Account",
            grossAmount = 5.10,
            netAmount = 5.10,
            escrowLocked = false,
            status = "SETTLED"
        )
    )
    private val _ledgerTransactions = MutableStateFlow(initialLedger)
    val ledgerTransactions: StateFlow<List<LedgerTransaction>> = _ledgerTransactions.asStateFlow()

    // Partner Settlements
    private val initialSettlements = listOf(
        PartnerSettlementSummary(
            partnerId = "jv_01",
            partnerName = "Apex Medical Care Pharmacy",
            totalVolumeGross = 42800.0,
            pendingEscrowLocked = 1840.0,
            totalSettledPayouts = 34500.0,
            lastPayoutDate = "05-Nov-2026",
            payoutMethod = "Stripe Connect Automated Daily Clearing"
        ),
        PartnerSettlementSummary(
            partnerId = "jv_02",
            partnerName = "Al-Noor Central Pharma JV",
            totalVolumeGross = 29400.0,
            pendingEscrowLocked = 980.0,
            totalSettledPayouts = 23900.0,
            lastPayoutDate = "04-Nov-2026",
            payoutMethod = "Direct Bank IBAN Wire"
        )
    )
    private val _partnerSettlements = MutableStateFlow(initialSettlements)
    val partnerSettlements: StateFlow<List<PartnerSettlementSummary>> = _partnerSettlements.asStateFlow()

    // Global Country & Currency System
    val activeCountry: StateFlow<CountryConfig> = countryRepository.activeCountry
    val availableCountries: StateFlow<List<CountryConfig>> = countryRepository.availableCountries

    fun selectCountry(countryCode: String, isManual: Boolean = true): Boolean {
        val success = countryRepository.selectCountry(countryCode, isManual)
        val newCountry = countryRepository.activeCountry.value
        recordAudit(
            actor = "Country Compliance Controller",
            role = UserAccessRole.COMPLIANCE_OFFICER,
            action = "COUNTRY_SELECTION_UPDATED",
            resource = newCountry.countryCode,
            details = "Active country set to ${newCountry.countryName} (${newCountry.currencyCode} ${newCountry.currencySymbol})"
        )
        // Sync matching regulatory jurisdiction if standard mapping exists
        when (newCountry.countryCode) {
            "AE" -> _activeJurisdiction.value = RegulatoryJurisdiction.UAE_MOHAP
            "US" -> _activeJurisdiction.value = RegulatoryJurisdiction.US_FDA
            "IN" -> _activeJurisdiction.value = RegulatoryJurisdiction.INDIA_CDSCO
            "DE", "FR" -> _activeJurisdiction.value = RegulatoryJurisdiction.EU_EMA
        }
        return success
    }

    fun formatCurrency(amount: Double): String {
        return activeCountry.value.formatCurrency(amount)
    }

    // Country-Specific Regulatory Engine
    private val _activeJurisdiction = MutableStateFlow(RegulatoryJurisdiction.UAE_MOHAP)
    val activeJurisdiction: StateFlow<RegulatoryJurisdiction> = _activeJurisdiction.asStateFlow()

    val regulatoryProfiles = mapOf(
        RegulatoryJurisdiction.UAE_MOHAP to CountryRegulatoryProfile(
            jurisdiction = RegulatoryJurisdiction.UAE_MOHAP,
            trackAndTraceSystem = "UAE Tatmeen (MOHAP GS1 2D DataMatrix)",
            pharmacistVerificationMandatory = true,
            coldChainLogisticsAuditRequired = true,
            maxControlledDaysSupply = 30,
            requiresDigitalLicenseSignature = true
        ),
        RegulatoryJurisdiction.US_FDA to CountryRegulatoryProfile(
            jurisdiction = RegulatoryJurisdiction.US_FDA,
            trackAndTraceSystem = "US FDA DSCSA (Drug Supply Chain Security Act)",
            pharmacistVerificationMandatory = true,
            coldChainLogisticsAuditRequired = true,
            maxControlledDaysSupply = 30,
            requiresDigitalLicenseSignature = true
        ),
        RegulatoryJurisdiction.EU_EMA to CountryRegulatoryProfile(
            jurisdiction = RegulatoryJurisdiction.EU_EMA,
            trackAndTraceSystem = "EU EMA FMD (Falsified Medicines Directive)",
            pharmacistVerificationMandatory = true,
            coldChainLogisticsAuditRequired = true,
            maxControlledDaysSupply = 28,
            requiresDigitalLicenseSignature = true
        ),
        RegulatoryJurisdiction.INDIA_CDSCO to CountryRegulatoryProfile(
            jurisdiction = RegulatoryJurisdiction.INDIA_CDSCO,
            trackAndTraceSystem = "India CDSCO Schedule H1 & QR Track",
            pharmacistVerificationMandatory = true,
            coldChainLogisticsAuditRequired = true,
            maxControlledDaysSupply = 30,
            requiresDigitalLicenseSignature = true
        )
    )

    private val _activePharmacistCredential = MutableStateFlow(
        PharmacistCredential(
            licenseNumber = "DHA-PHARM-88312-DXB",
            pharmacistName = "Dr. Salma Al-Khatib, PharmD, RPh",
            jurisdiction = RegulatoryJurisdiction.UAE_MOHAP,
            issuingBody = "Dubai Health Authority (DHA)",
            expiryDate = "31-Dec-2027",
            isVerifiedActive = true,
            digitalSignatureKey = "SHA256:8891-AA-DHA-PHARMACIST-SIG-VERIFIED"
        )
    )
    val activePharmacistCredential: StateFlow<PharmacistCredential> = _activePharmacistCredential.asStateFlow()

    // Security & RBAC
    private val _activeUserRole = MutableStateFlow(UserAccessRole.LICENSED_PHARMACIST)
    val activeUserRole: StateFlow<UserAccessRole> = _activeUserRole.asStateFlow()

    // Immutable Security Audit Log Trail
    private val initialAuditLogs = listOf(
        AuditLogEntry(
            id = "AUD-001",
            timestamp = "06-Nov 08:30:12",
            actor = "Dr. Salma Al-Khatib",
            role = UserAccessRole.LICENSED_PHARMACIST,
            action = "PHARMACIST_VERIFY_RX",
            resource = "Prescription #RX-2026-9912",
            details = "Verified patient identity, approved Insulin Glargine & Atorvastatin.",
            ipAddress = "192.168.1.10",
            hashSignature = generateSha256("AUD-001-PHARMACIST_VERIFY_RX")
        ),
        AuditLogEntry(
            id = "AUD-002",
            timestamp = "06-Nov 09:14:05",
            actor = "System Auto-Monitor",
            role = UserAccessRole.COMPLIANCE_OFFICER,
            action = "BATCH_QUARANTINE_LOCK",
            resource = "Batch #BAT-2025-RECALL-99",
            details = "MOHAP Recall Notice #RC-2026-091 applied. 150 units locked from catalog.",
            ipAddress = "10.0.0.1",
            hashSignature = generateSha256("AUD-002-BATCH_QUARANTINE_LOCK")
        )
    )
    private val _auditLogs = MutableStateFlow(initialAuditLogs)
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Gemini-Powered Multilingual Assistant
    private val _activeLanguage = MutableStateFlow(AssistantLanguage.ENGLISH)
    val activeLanguage: StateFlow<AssistantLanguage> = _activeLanguage.asStateFlow()

    private val initialAssistantMessages = listOf(
        AssistantChatMessage(
            id = "msg_welcome",
            sender = "GEMINI_PHARMA_AI",
            text = "Welcome to PharmaX AI Clinical Guide. I can answer inquiries regarding your prescribed medications, dosage timings, food interactions, and cold-chain storage.\n\n⚠️ Medical Disclaimer: I provide educational information only. Never alter dosages without consulting your licensed physician.",
            language = AssistantLanguage.ENGLISH,
            timestamp = "10:00 AM",
            hasDisclaimer = true
        )
    )
    private val _assistantMessages = MutableStateFlow(initialAssistantMessages)
    val assistantMessages: StateFlow<List<AssistantChatMessage>> = _assistantMessages.asStateFlow()

    // Joint Venture Partners
    val jvPartners = listOf(
        JVPartner(
            id = "jv_01",
            name = "Apex Medical Care Pharmacy",
            licenseNo = "MOH-DXB-98442",
            branchCity = "Dubai Downtown (2.4 km)",
            distanceKm = 2.4,
            sharedStockSkus = 420,
            reliabilityRating = 4.9f,
            commissionSharePct = 5.0,
            isOnline = true
        ),
        JVPartner(
            id = "jv_02",
            name = "Al-Noor Central Pharma JV",
            licenseNo = "MOH-AUH-11029",
            branchCity = "Al Barsha Hub (4.8 km)",
            distanceKm = 4.8,
            sharedStockSkus = 890,
            reliabilityRating = 4.8f,
            commissionSharePct = 5.0,
            isOnline = true
        ),
        JVPartner(
            id = "jv_03",
            name = "CareFirst Regional Hospital Dispensary",
            licenseNo = "MOH-SHJ-33918",
            branchCity = "Healthcare City (6.1 km)",
            distanceKm = 6.1,
            sharedStockSkus = 1250,
            reliabilityRating = 4.95f,
            commissionSharePct = 4.5,
            isOnline = true
        )
    )

    // Stock Alerts
    private val initialAlerts = listOf(
        StockAlert(
            id = "alt_1",
            title = "Zero Stock Detected - JV Sync Activated",
            message = "Ozempic 2mg is out of stock in Primary Store. 3 JV partner pharmacies discovered with certified batches.",
            medicineName = "Ozempic DualDose Pen",
            severity = "CRITICAL",
            timestamp = "5 mins ago"
        ),
        StockAlert(
            id = "alt_2",
            title = "Batch Expiry Imminent (<60 Days)",
            message = "Lipitor 20mg (Batch BAT-2024-K09) expires within 60 days. Auto-promoted for early B2C fulfillment.",
            medicineName = "Lipitor 20mg",
            severity = "WARNING",
            timestamp = "1 hour ago"
        ),
        StockAlert(
            id = "alt_3",
            title = "Cold-Chain Integrity Check OK",
            message = "Cold Vault #1 current temperature at 4.2°C. Well within safe 2°C - 8°C boundaries.",
            medicineName = "Lantus SoloStar (Insulin)",
            severity = "INFO",
            timestamp = "3 hours ago"
        )
    )
    private val _stockAlerts = MutableStateFlow(initialAlerts)
    val stockAlerts: StateFlow<List<StockAlert>> = _stockAlerts.asStateFlow()

    // Demand Forecasts
    val demandForecasts = listOf(
        DemandForecast(
            medicineName = "Ozempic DualDose Pen",
            currentStock = 0,
            projectedDemand30d = 85,
            suggestedReorderQty = 100,
            stockOutRiskScore = 98,
            seasonalFactor = "+42% demand increase across metro clinics"
        ),
        DemandForecast(
            medicineName = "Lantus SoloStar",
            currentStock = 14,
            projectedDemand30d = 45,
            suggestedReorderQty = 50,
            stockOutRiskScore = 72,
            seasonalFactor = "Steady chronic care refill demand"
        ),
        DemandForecast(
            medicineName = "Augmentin 1000mg",
            currentStock = 42,
            projectedDemand30d = 60,
            suggestedReorderQty = 30,
            stockOutRiskScore = 24,
            seasonalFactor = "Post-flu season plateau"
        ),
        DemandForecast(
            medicineName = "Ventolin Inhaler",
            currentStock = 19,
            projectedDemand30d = 35,
            suggestedReorderQty = 25,
            stockOutRiskScore = 55,
            seasonalFactor = "+18% weather change allergy surge"
        )
    )

    // Pill Reminders
    private val initialReminders = listOf(
        PillReminder(
            id = "pill_1",
            medicineName = "Metformin HCl 500mg",
            dosage = "1 Tablet",
            timeLabel = "08:00 AM (Morning)",
            instruction = "Take with breakfast to minimize GI discomfort",
            isTakenToday = true,
            isAiScheduled = true
        ),
        PillReminder(
            id = "pill_2",
            medicineName = "Lantus Insulin (Glargine)",
            dosage = "18 Units Sub-Q",
            timeLabel = "01:30 PM (Midday)",
            instruction = "Rotate injection site (abdomen/thigh)",
            isTakenToday = false,
            isAiScheduled = true
        ),
        PillReminder(
            id = "pill_3",
            medicineName = "Lipitor (Atorvastatin) 20mg",
            dosage = "1 Tablet",
            timeLabel = "09:00 PM (Night)",
            instruction = "Take with evening glass of water",
            isTakenToday = false,
            isAiScheduled = true
        )
    )
    private val _pillReminders = MutableStateFlow(initialReminders)
    val pillReminders: StateFlow<List<PillReminder>> = _pillReminders.asStateFlow()

    // Subscription Plans
    val subscriptionPlans = listOf(
        SubscriptionPlan(
            id = "sub_free",
            tier = SubscriptionTier.FREE_TIER,
            name = "Basic Patient",
            priceMonthly = "FREE",
            priceAnnual = "$0 / year",
            audienceBadge = "B2C Customers",
            features = listOf(
                "Standard 2-3 hour medicine delivery",
                "Standard digital payment methods",
                "Basic prescription upload",
                "Digital receipt & order tracking"
            ),
            isPopular = false,
            colorHex = 0xFF64748B
        ),
        SubscriptionPlan(
            id = "sub_premium_b2c",
            tier = SubscriptionTier.B2C_PREMIUM,
            name = "AI Health+ Premium",
            priceMonthly = "$12.99 / mo",
            priceAnnual = "$119 / year (Save 24%)",
            audienceBadge = "B2C Customers",
            features = listOf(
                "AI-powered Pill Organizer & refill alerts",
                "100% Free Shipping on all orders",
                "Priority 15-min Emergency delivery",
                "Instant AI Drug-Drug interaction check",
                "Cold-chain temperature guarantee certificate"
            ),
            isPopular = true,
            colorHex = 0xFF00897B
        ),
        SubscriptionPlan(
            id = "sub_b2b_base",
            tier = SubscriptionTier.B2B_BASE,
            name = "Pharmacy Base OS",
            priceMonthly = "$149 / mo",
            priceAnnual = "$1,490 / year",
            audienceBadge = "Pharmacies & Clinics",
            features = listOf(
                "Batch & expiry date inventory tracking",
                "Local store order execution & billing",
                "Barcode & batch scanner integration",
                "Basic stock replenishment notifications"
            ),
            isPopular = false,
            colorHex = 0xFF0284C7
        ),
        SubscriptionPlan(
            id = "sub_b2b_jv",
            tier = SubscriptionTier.B2B_PREMIUM_JV,
            name = "Joint Venture (JV) Enterprise",
            priceMonthly = "$399 / mo",
            priceAnnual = "$3,990 / year",
            audienceBadge = "Wholesale & JV Partners",
            features = listOf(
                "Multi-store Out-of-Stock auto routing (JV sync)",
                "AI predictive demand forecasting (30/60/90 days)",
                "Multi-party Escrow automated split payouts",
                "Shared wholesale inventory marketplace pool",
                "Cold-chain logistics IoT telemetry integration"
            ),
            isPopular = true,
            colorHex = 0xFF4F46E5
        )
    )

    private val _currentTier = MutableStateFlow(SubscriptionTier.B2C_PREMIUM)
    val currentTier: StateFlow<SubscriptionTier> = _currentTier.asStateFlow()

    // Active Orders
    private val initialOrders = listOf(
        Order(
            id = "PHX-9082",
            customerName = "Anish Kumar",
            deliveryAddress = "Apt 1402, Marina Promenade, Dubai Marina",
            items = listOf(
                CartItem(initialMedicines[3], 1), // Ozempic (via JV partner)
                CartItem(initialMedicines[1], 1)  // Augmentin
            ),
            subtotal = 169.80,
            deliveryFee = 0.00, // Premium free shipping
            totalAmount = 169.80,
            isJointVentureRoute = true,
            jvPartnerName = "Apex Medical Care Pharmacy (Dubai Downtown)",
            escrowSplit = calculateEscrowSplit(169.80, isJv = true),
            paymentMethod = PaymentMethod.CARD_STRIPE,
            deliveryStage = DeliveryStage.OUT_FOR_DELIVERY,
            driverName = "Capt. Tariq Al-Hashmi",
            driverPhone = "+971 52 443 8901",
            liveTempCelsius = 4.3f,
            targetTempRange = "2°C - 8°C (Inspected)",
            etaMinutes = 14,
            createdAtFormatted = "Today, 10:42 AM"
        )
    )
    private val _orders = MutableStateFlow(initialOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Prescriptions List
    private val initialPrescription = PrescriptionScan(
        id = "RX-2026-9912",
        patientName = "Anish Kumar (Age 34)",
        doctorName = "Dr. Tariq Mansoor, MD, MRCP",
        doctorLicense = "DHA-LIC-2026-44018",
        clinicHospital = "Emirates Specialty Hospital & Diabetes Center",
        issuedDate = "05-Nov-2026",
        rawOcrText = """
            CLINIC: Emirates Specialty Hospital
            PHYSICIAN: Dr. Tariq Mansoor, MD (Lic # DHA-LIC-2026-44018)
            PATIENT: Anish Kumar | DOB: 14-Oct-1992
            DATE: 05-Nov-2026
            -------------------------------------------------
            Rx:
            1. Lantus SoloStar 100 U/mL (Insulin Glargine)
               Sig: 18 units Sub-Q once daily at night.
               Mitte: 5 prefilled pens (Cold-Chain 2-8°C).
            2. Lipitor 20mg (Atorvastatin)
               Sig: 1 tab PO qHS. Mitte: 30 tabs.
            3. Panadol Extra Advance
               Sig: 1-2 tabs PRN for tension headache.
            -------------------------------------------------
            Signed: Dr. Tariq Mansoor [Verified Digital Signature]
        """.trimIndent(),
        extractedDrugs = listOf(
            ExtractedDrug(
                drugName = "Lantus SoloStar (Insulin Glargine)",
                matchedMedicineId = "med_1",
                strength = "100 U/mL",
                dosage = "18 Units Sub-Q",
                frequency = "Once daily at bedtime",
                durationDays = 30,
                confidenceScore = 0.98f,
                isAvailableInStock = true
            ),
            ExtractedDrug(
                drugName = "Lipitor (Atorvastatin)",
                matchedMedicineId = "med_3",
                strength = "20 mg",
                dosage = "1 Tablet",
                frequency = "Once daily (Night)",
                durationDays = 30,
                confidenceScore = 0.96f,
                isAvailableInStock = true
            ),
            ExtractedDrug(
                drugName = "Panadol Extra Advance",
                matchedMedicineId = "med_6",
                strength = "500 mg",
                dosage = "1-2 Caplets",
                frequency = "As needed for pain",
                durationDays = 10,
                confidenceScore = 0.99f,
                isAvailableInStock = true
            )
        ),
        safetyInteractions = listOf(
            "Drug Interaction Safety: No severe contraindications detected between Insulin Glargine and Atorvastatin.",
            "Formulary Check: All prescribed items match UAE MOHAP / WHO Essential Medicines database.",
            "Cold-Chain Requirement: Lantus SoloStar requires monitored 2°C - 8°C transport."
        ),
        verificationState = VerificationState.PHARMACIST_APPROVED,
        pharmacistNote = "Verified by Licensed Pharmacist Dr. Salma K. (DHA #8831). Dispensing authorized.",
        scannedAt = System.currentTimeMillis() - 3600000L
    )

    private val _prescriptions = MutableStateFlow(listOf(initialPrescription))
    val prescriptions: StateFlow<List<PrescriptionScan>> = _prescriptions.asStateFlow()

    // --- Actions & Production Methods ---

    fun updateSubscriptionTier(tier: SubscriptionTier) {
        _currentTier.value = tier
    }

    fun togglePillTaken(id: String) {
        _pillReminders.update { list ->
            list.map { reminder ->
                if (reminder.id == id) reminder.copy(isTakenToday = !reminder.isTakenToday)
                else reminder
            }
        }
    }

    fun addBatch(record: BatchRecord, actorName: String = "Inventory Officer") {
        _batches.update { listOf(record) + it }
        val movement = recordStockMovement(
            batchNumber = record.batchNumber,
            medicineName = record.medicineName,
            type = StockMovementType.GOODS_RECEIPT,
            delta = record.quantity,
            balanceAfter = record.quantity,
            actorName = actorName,
            actorRole = "Inventory Coordinator",
            reason = "Batch Inward Receipt & Cataloging",
            referenceId = "GRN-${System.currentTimeMillis() % 10000}"
        )
        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().insertBatch(BatchEntity.fromDomain(record))
                db.stockMovementDao().insertMovement(StockMovementEntity.fromDomain(movement))
            }
        }
        recordAudit(
            actor = actorName,
            role = UserAccessRole.INVENTORY_MANAGER,
            action = "BATCH_CATALOGED",
            resource = record.batchNumber,
            details = "Added batch ${record.batchNumber} (${record.quantity} units, exp: ${record.expiryDate})"
        )
    }

    fun editBatch(updated: BatchRecord, actorName: String = "Dr. Salma Al-Khatib") {
        val old = _batches.value.find { it.batchNumber == updated.batchNumber }
        _batches.update { list ->
            list.map { b ->
                if (b.batchNumber == updated.batchNumber) updated else b
            }
        }
        var movement: StockMovementRecord? = null
        if (old != null && old.warehouseLocation != updated.warehouseLocation) {
            movement = recordStockMovement(
                batchNumber = updated.batchNumber,
                medicineName = updated.medicineName,
                type = StockMovementType.WAREHOUSE_TRANSFER,
                delta = 0,
                balanceAfter = updated.quantity,
                actorName = actorName,
                actorRole = "Inventory Coordinator",
                reason = "Relocated from ${old.warehouseLocation} to ${updated.warehouseLocation}",
                referenceId = "LOC-TRF-${System.currentTimeMillis() % 10000}"
            )
        }
        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().updateBatch(BatchEntity.fromDomain(updated))
                movement?.let { db.stockMovementDao().insertMovement(StockMovementEntity.fromDomain(it)) }
            }
        }
        recordAudit(
            actor = actorName,
            role = UserAccessRole.INVENTORY_MANAGER,
            action = "BATCH_CONFIG_UPDATED",
            resource = updated.batchNumber,
            details = "Updated batch parameters: minThreshold=${updated.minThreshold}, reorderThreshold=${updated.reorderThreshold}"
        )
    }

    fun adjustStock(
        batchNumber: String,
        delta: Int,
        reason: String,
        referenceId: String,
        actorName: String = "Inventory Auditor"
    ): Boolean {
        val target = _batches.value.find { it.batchNumber == batchNumber } ?: return false
        val newQuantity = target.quantity + delta
        if (newQuantity < 0) return false

        val newStatus = when {
            target.isQuarantined -> BatchStatus.RECALLED_QUARANTINED
            newQuantity <= target.minThreshold -> BatchStatus.LOW_STOCK
            target.daysUntilExpiry() < 60 -> BatchStatus.EXPIRING_SOON
            else -> BatchStatus.OPTIMAL
        }

        val updated = target.copy(quantity = newQuantity, status = newStatus)
        _batches.update { list ->
            list.map { if (it.batchNumber == batchNumber) updated else it }
        }

        // Update medicine stockTotal
        _medicines.update { meds ->
            meds.map { m ->
                if (m.id == target.medicineId) {
                    val newTotal = (m.stockTotal + delta).coerceAtLeast(0)
                    m.copy(stockTotal = newTotal)
                } else m
            }
        }

        val movement = recordStockMovement(
            batchNumber = batchNumber,
            medicineName = target.medicineName,
            type = StockMovementType.CYCLE_COUNT_ADJUSTMENT,
            delta = delta,
            balanceAfter = newQuantity,
            actorName = actorName,
            actorRole = "Inventory Auditor",
            reason = reason,
            referenceId = referenceId
        )

        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().updateQuantity(batchNumber, newQuantity, newStatus.name)
                db.stockMovementDao().insertMovement(StockMovementEntity.fromDomain(movement))
            }
        }

        recordAudit(
            actor = actorName,
            role = UserAccessRole.INVENTORY_MANAGER,
            action = "STOCK_ADJUSTMENT_LOGGED",
            resource = batchNumber,
            details = "Adjusted by $delta units. New balance: $newQuantity. Reason: $reason (#$referenceId)"
        )
        return true
    }

    fun transferBatchLocation(
        batchNumber: String,
        newLocation: String,
        actorName: String = "Logistics Handler"
    ): Boolean {
        val target = _batches.value.find { it.batchNumber == batchNumber } ?: return false
        val oldLoc = target.warehouseLocation
        _batches.update { list ->
            list.map { if (it.batchNumber == batchNumber) it.copy(warehouseLocation = newLocation) else it }
        }

        val movement = recordStockMovement(
            batchNumber = batchNumber,
            medicineName = target.medicineName,
            type = StockMovementType.WAREHOUSE_TRANSFER,
            delta = 0,
            balanceAfter = target.quantity,
            actorName = actorName,
            actorRole = "Logistics Handler",
            reason = "Transferred from $oldLoc to $newLocation",
            referenceId = "TRF-${System.currentTimeMillis() % 10000}"
        )

        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().updateLocation(batchNumber, newLocation)
                db.stockMovementDao().insertMovement(StockMovementEntity.fromDomain(movement))
            }
        }

        recordAudit(
            actor = actorName,
            role = UserAccessRole.INVENTORY_MANAGER,
            action = "BATCH_LOCATION_TRANSFERRED",
            resource = batchNumber,
            details = "Location changed from $oldLoc to $newLocation"
        )
        return true
    }

    fun archiveBatch(batchNumber: String, actorName: String = "Compliance Manager"): Boolean {
        val target = _batches.value.find { it.batchNumber == batchNumber } ?: return false
        _batches.update { list ->
            list.map { if (it.batchNumber == batchNumber) it.copy(isArchived = true) else it }
        }

        val movement = recordStockMovement(
            batchNumber = batchNumber,
            medicineName = target.medicineName,
            type = StockMovementType.BATCH_ARCHIVED,
            delta = 0,
            balanceAfter = target.quantity,
            actorName = actorName,
            actorRole = "Compliance Manager",
            reason = "Decommissioned / Archived from active fulfillment",
            referenceId = "ARC-${System.currentTimeMillis() % 10000}"
        )

        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().archiveBatch(batchNumber)
                db.stockMovementDao().insertMovement(StockMovementEntity.fromDomain(movement))
            }
        }

        recordAudit(
            actor = actorName,
            role = UserAccessRole.COMPLIANCE_OFFICER,
            action = "BATCH_ARCHIVED",
            resource = batchNumber,
            details = "Batch archived by $actorName"
        )
        return true
    }

    fun reserveBatchStock(batchNumber: String, requestedQty: Int): Boolean {
        val target = _batches.value.find { it.batchNumber == batchNumber } ?: return false
        if (target.availableQuantity < requestedQty || !target.isFefoEligible) return false

        val newReserved = target.reservedQuantity + requestedQty
        val updated = target.copy(reservedQuantity = newReserved)
        _batches.update { list ->
            list.map { if (it.batchNumber == batchNumber) updated else it }
        }

        val movement = recordStockMovement(
            batchNumber = batchNumber,
            medicineName = target.medicineName,
            type = StockMovementType.FEFO_RESERVATION,
            delta = -requestedQty,
            balanceAfter = updated.availableQuantity,
            actorName = "Order Routing System",
            actorRole = "System Order Router",
            reason = "Stock Reserved for Order Allocation",
            referenceId = "RES-${System.currentTimeMillis() % 10000}"
        )

        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().updateReservedQuantity(batchNumber, newReserved)
                db.stockMovementDao().insertMovement(StockMovementEntity.fromDomain(movement))
            }
        }
        return true
    }

    fun releaseBatchReservation(batchNumber: String, qty: Int): Boolean {
        val target = _batches.value.find { it.batchNumber == batchNumber } ?: return false
        val newReserved = (target.reservedQuantity - qty).coerceAtLeast(0)
        val updated = target.copy(reservedQuantity = newReserved)
        _batches.update { list ->
            list.map { if (it.batchNumber == batchNumber) updated else it }
        }

        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().updateReservedQuantity(batchNumber, newReserved)
            }
        }
        return true
    }

    fun confirmBatchDispense(batchNumber: String, qty: Int): Boolean {
        val target = _batches.value.find { it.batchNumber == batchNumber } ?: return false
        val newQty = (target.quantity - qty).coerceAtLeast(0)
        val newReserved = (target.reservedQuantity - qty).coerceAtLeast(0)
        val newStatus = when {
            target.isQuarantined -> BatchStatus.RECALLED_QUARANTINED
            newQty <= target.minThreshold -> BatchStatus.LOW_STOCK
            target.daysUntilExpiry() < 60 -> BatchStatus.EXPIRING_SOON
            else -> BatchStatus.OPTIMAL
        }
        val updated = target.copy(quantity = newQty, reservedQuantity = newReserved, status = newStatus)
        _batches.update { list ->
            list.map { if (it.batchNumber == batchNumber) updated else it }
        }

        val movement = recordStockMovement(
            batchNumber = batchNumber,
            medicineName = target.medicineName,
            type = StockMovementType.DISPENSE_SALE,
            delta = -qty,
            balanceAfter = newQty,
            actorName = "Pharmacist Dispensing",
            actorRole = "Licensed Pharmacist",
            reason = "Prescription Order Dispensed",
            referenceId = "DSP-${System.currentTimeMillis() % 10000}"
        )

        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().updateQuantity(batchNumber, newQty, newStatus.name)
                db.batchDao().updateReservedQuantity(batchNumber, newReserved)
                db.stockMovementDao().insertMovement(StockMovementEntity.fromDomain(movement))
            }
        }
        return true
    }

    fun recordStockMovement(
        batchNumber: String,
        medicineName: String,
        type: StockMovementType,
        delta: Int,
        balanceAfter: Int,
        actorName: String,
        actorRole: String,
        reason: String,
        referenceId: String
    ): StockMovementRecord {
        val record = StockMovementRecord(
            id = "SM-${System.currentTimeMillis() % 100000}",
            batchNumber = batchNumber,
            medicineName = medicineName,
            movementType = type,
            quantityDelta = delta,
            balanceAfter = balanceAfter,
            actorName = actorName,
            actorRole = actorRole,
            reason = reason,
            referenceId = referenceId,
            timestampFormatted = SimpleDateFormat("dd-MMM HH:mm", Locale.getDefault()).format(Date())
        )
        _stockMovements.update { listOf(record) + it }
        return record
    }

    // FEFO (First-Expired, First-Out) Allocation Engine
    fun calculateFefoAllocation(medicineId: String, requestedQty: Int): List<FefoAllocationItem> {
        val activeBatches = _batches.value.filter { it.medicineId == medicineId && !it.isArchived }

        // Sort candidate batches: eligible lots by earliest days until expiry; ineligible lots placed at the bottom
        val sorted = activeBatches.sortedWith(
            compareBy<BatchRecord> { !it.isFefoEligible }
                .thenBy { it.daysUntilExpiry() }
        )

        var remainingToAllocate = requestedQty
        var priorityCounter = 1

        return sorted.map { batch ->
            val isEligible = batch.isFefoEligible
            val available = batch.availableQuantity
            val allocated = if (isEligible && remainingToAllocate > 0) {
                val take = minOf(available, remainingToAllocate)
                remainingToAllocate -= take
                take
            } else {
                0
            }

            FefoAllocationItem(
                batchNumber = batch.batchNumber,
                medicineName = batch.medicineName,
                expiryDate = batch.expiryDate,
                daysUntilExpiry = batch.daysUntilExpiry(),
                availableQty = available,
                allocatedQty = allocated,
                pickingPriority = if (isEligible) priorityCounter++ else 999,
                isEligible = isEligible,
                storageCondition = batch.storageTempRange,
                warehouseLocation = batch.warehouseLocation
            )
        }
    }

    fun getBatchesExpiringWithin(days: Long): List<BatchRecord> {
        return _batches.value.filter { !it.isArchived && it.daysUntilExpiry() in 1..days }
    }

    fun generateInventoryCsvReport(): String {
        val sb = StringBuilder()
        sb.append("BatchNumber,MedicineId,MedicineName,GTIN,Manufacturer,MfgDate,ExpiryDate,DaysUntilExpiry,Quantity,ReservedQty,AvailableQty,MinThreshold,ReorderThreshold,StorageTemp,WarehouseLocation,Status,IsQuarantined,IsArchived\n")
        _batches.value.forEach { b ->
            sb.append("${b.batchNumber},")
            sb.append("${b.medicineId},")
            sb.append("\"${b.medicineName}\",")
            sb.append("${b.gtin},")
            sb.append("\"${b.manufacturer}\",")
            sb.append("${b.mfgDate},")
            sb.append("${b.expiryDate},")
            sb.append("${b.daysUntilExpiry()},")
            sb.append("${b.quantity},")
            sb.append("${b.reservedQuantity},")
            sb.append("${b.availableQuantity},")
            sb.append("${b.minThreshold},")
            sb.append("${b.reorderThreshold},")
            sb.append("\"${b.storageTempRange}\",")
            sb.append("\"${b.warehouseLocation}\",")
            sb.append("${b.status.name},")
            sb.append("${b.isQuarantined},")
            sb.append("${b.isArchived}\n")
        }
        return sb.toString()
    }

    fun addPrescription(scan: PrescriptionScan) {
        _prescriptions.update { listOf(scan) + it }
    }

    fun addOrder(order: Order) {
        _orders.update { listOf(order) + it }
    }

    // Medicine Recall & Batch Quarantine
    fun quarantineBatch(
        batchNumber: String,
        reason: QuarantineReason,
        authorityNoticeId: String,
        quarantinedBy: String
    ): BatchQuarantineRecord {
        val targetBatch = _batches.value.find { it.batchNumber == batchNumber }
        val medicineName = targetBatch?.medicineName ?: "Unknown Medication"
        val medicineId = targetBatch?.medicineId ?: "med_unknown"

        val record = BatchQuarantineRecord(
            id = "QRN-${System.currentTimeMillis() % 10000}",
            batchNumber = batchNumber,
            medicineId = medicineId,
            medicineName = medicineName,
            manufacturer = "PharmaGlobal Certified",
            reason = reason,
            status = QuarantineStatus.QUARANTINED,
            quarantinedBy = quarantinedBy,
            authorityNoticeId = authorityNoticeId,
            affectedUnits = targetBatch?.quantity ?: 50,
            patientAlertsDispatched = 12
        )

        _quarantineRecords.update { listOf(record) + it }

        // Update Batch state to RECALLED_QUARANTINED
        _batches.update { list ->
            list.map { b ->
                if (b.batchNumber == batchNumber) {
                    b.copy(
                        status = BatchStatus.RECALLED_QUARANTINED,
                        isQuarantined = true,
                        quarantineReason = "QUARANTINED: ${reason.label} (#$authorityNoticeId)"
                    )
                } else b
            }
        }

        database?.let { db ->
            coroutineScope.launch {
                db.batchDao().quarantineBatch(batchNumber, "QUARANTINED: ${reason.label} (#$authorityNoticeId)")
            }
        }

        // Update medicine item to block sale
        _medicines.update { list ->
            list.map { m ->
                if (m.id == medicineId) {
                    m.copy(
                        isRecalledOrQuarantined = true,
                        quarantineNotice = "BATCH RECALLED: $batchNumber - ${reason.label}"
                    )
                } else m
            }
        }

        recordAudit(
            actor = quarantinedBy,
            role = UserAccessRole.LICENSED_PHARMACIST,
            action = "BATCH_QUARANTINE_LOCKED",
            resource = "Batch $batchNumber",
            details = "Quarantined due to ${reason.label}. Notice: $authorityNoticeId"
        )

        return record
    }

    fun isBatchOrMedicineBlocked(medicineId: String): Boolean {
        val med = _medicines.value.find { it.id == medicineId }
        if (med?.isRecalledOrQuarantined == true) return true
        val batch = _batches.value.find { it.medicineId == medicineId }
        return batch?.isQuarantined == true
    }

    // Barcode & QR Verification
    fun verifyBarcodeScan(rawBarcode: String): ScannedBarcodeData {
        val clean = rawBarcode.trim()
        val isRecalled = clean.contains("RECALL") || clean.contains("BAT-2025-RECALL")
        val isExpired = clean.contains("2024-K09")

        val resultStatus = when {
            isRecalled -> BarcodeVerificationResult.QUARANTINED_BLOCKED
            isExpired -> BarcodeVerificationResult.EXPIRED_BARCODE
            clean.length > 8 -> BarcodeVerificationResult.GENUINE_VERIFIED
            else -> BarcodeVerificationResult.UNREGISTERED
        }

        val scanData = ScannedBarcodeData(
            gtin = if (clean.length >= 14) clean.take(14) else "06291002019231",
            serialNumber = "SN-${(100000..999999).random()}",
            batchNumber = if (isRecalled) "BAT-2025-RECALL-99" else "BAT-2026-X88",
            expiryDate = if (isExpired) "30-Nov-2024" else "28-Feb-2027",
            rawPayload = clean,
            verificationResult = resultStatus,
            medicineName = if (isRecalled) "Valsartan 160mg" else "Lantus SoloStar (Insulin)",
            trackAndTraceId = "TATMEEN-GS1-${(1000..9999).random()}",
            manufacturer = if (isRecalled) "CardioPharma Labs" else "Sanofi Aventis"
        )

        _scannedBarcodes.update { listOf(scanData) + it }

        recordAudit(
            actor = "Scanner Station",
            role = UserAccessRole.INVENTORY_MANAGER,
            action = "BARCODE_VERIFIED",
            resource = scanData.batchNumber,
            details = "Result: ${resultStatus.label} for GTIN ${scanData.gtin}"
        )

        return scanData
    }

    // Purchase Order & Supplier Management
    fun createPurchaseOrder(
        supplierId: String,
        items: List<PurchaseOrderItem>
    ): PurchaseOrder {
        val supplier = suppliers.find { it.id == supplierId } ?: suppliers.first()
        val total = items.sumOf { it.unitCost * it.quantity }
        val po = PurchaseOrder(
            id = "PO-${System.currentTimeMillis() % 10000}",
            poNumber = "PO-DXB-${(100..999).random()}",
            supplierId = supplier.id,
            supplierName = supplier.name,
            items = items,
            totalCost = total,
            orderDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date()),
            expectedDeliveryDate = "In ${supplier.leadTimeDays} days",
            status = PurchaseOrderStatus.SUBMITTED,
            approvedByPharmacist = null,
            qaInspectionNotes = "Submitted to supplier portal."
        )

        _purchaseOrders.update { listOf(po) + it }
        recordAudit(
            actor = "Inventory Coordinator",
            role = UserAccessRole.INVENTORY_MANAGER,
            action = "PO_CREATED",
            resource = po.poNumber,
            details = "Created PO for ${supplier.name} totaling $$total"
        )
        return po
    }

    fun approvePurchaseOrder(poId: String, pharmacistName: String) {
        _purchaseOrders.update { list ->
            list.map { po ->
                if (po.id == poId) {
                    po.copy(
                        status = PurchaseOrderStatus.PHARMACIST_APPROVED,
                        approvedByPharmacist = pharmacistName,
                        qaInspectionNotes = "Pharmacist verified. Authorized for dispatch."
                    )
                } else po
            }
        }
        recordAudit(
            actor = pharmacistName,
            role = UserAccessRole.LICENSED_PHARMACIST,
            action = "PO_PHARMACIST_APPROVED",
            resource = poId,
            details = "Clinical pharmacist approved replenishment order."
        )
    }

    // Cold-Chain Advanced IoT Telemetry
    fun triggerSimulatedExcursion(peakTempC: Float, durationMins: Int) {
        val isExcursionSevere = peakTempC > 8.0f && durationMins > 15
        val excursion = ExcursionEvent(
            id = "EXC-${System.currentTimeMillis() % 10000}",
            timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
            peakTempC = peakTempC,
            durationMinutes = durationMins,
            meanKineticTempC = (peakTempC + 4.2f) / 2f,
            severity = if (isExcursionSevere) "CRITICAL_BATCH_INVALIDATION" else "MINOR",
            actionTaken = if (isExcursionSevere) "Automated Quarantine Triggered. Batch flagged for QA inspection."
            else "Compressor stepped up. Temperature restored."
        )

        _coldChainTelemetry.update { current ->
            current.copy(
                liveTempC = peakTempC,
                excursions = listOf(excursion) + current.excursions,
                isComplianceValid = !isExcursionSevere
            )
        }

        if (isExcursionSevere) {
            quarantineBatch(
                batchNumber = "BAT-2026-X88",
                reason = QuarantineReason.TEMPERATURE_EXCURSION,
                authorityNoticeId = "IOT-EXCURSION-TRIPWIRE",
                quarantinedBy = "PharmaX IoT Automated Sentinel"
            )
        }
    }

    // Financial Ledger Escrow Release
    fun releaseEscrowForOrder(orderId: String, recipientName: String, otpCode: String): Boolean {
        if (otpCode != "892410" && otpCode != "123456") return false

        _orders.update { list ->
            list.map { o ->
                if (o.id == orderId) {
                    o.copy(
                        deliveryStage = DeliveryStage.DELIVERED,
                        escrowSplit = o.escrowSplit.copy(escrowStatus = "RELEASED_TO_PARTNERS")
                    )
                } else o
            }
        }

        _ledgerTransactions.update { list ->
            list.map { txn ->
                if (txn.orderId == orderId) {
                    txn.copy(escrowLocked = false, status = "CLEARED_AND_SETTLED")
                } else txn
            }
        }

        recordAudit(
            actor = "Courier Capt. Omar",
            role = UserAccessRole.COURIER_AGENT,
            action = "ESCROW_RELEASED",
            resource = orderId,
            details = "Recipient $recipientName verified OTP $otpCode. Cold-chain seal intact."
        )
        return true
    }

    // Regulatory Jurisdiction
    fun setJurisdiction(jurisdiction: RegulatoryJurisdiction) {
        _activeJurisdiction.value = jurisdiction
        recordAudit(
            actor = "Regulatory Compliance Officer",
            role = UserAccessRole.COMPLIANCE_OFFICER,
            action = "JURISDICTION_SWITCHED",
            resource = jurisdiction.countryCode,
            details = "Active formulary updated to ${jurisdiction.authorityName}"
        )
    }

    // Security Audit Logger
    fun recordAudit(
        actor: String,
        role: UserAccessRole,
        action: String,
        resource: String,
        details: String
    ) {
        val entry = AuditLogEntry(
            id = "AUD-${System.currentTimeMillis() % 100000}",
            timestamp = SimpleDateFormat("dd-MMM HH:mm:ss", Locale.getDefault()).format(Date()),
            actor = actor,
            role = role,
            action = action,
            resource = resource,
            details = details,
            hashSignature = generateSha256("$actor:$action:$resource:${System.currentTimeMillis()}")
        )
        _auditLogs.update { listOf(entry) + it }
    }

    // Gemini Multilingual Assistant
    fun setAssistantLanguage(lang: AssistantLanguage) {
        _activeLanguage.value = lang
    }

    fun queryGeminiAssistant(promptText: String, lang: AssistantLanguage) {
        val userMsg = AssistantChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "USER",
            text = promptText,
            language = lang,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            hasDisclaimer = false
        )

        val cleanPrompt = promptText.lowercase()
        val replyText = when (lang) {
            AssistantLanguage.ARABIC -> {
                when {
                    cleanPrompt.contains("انسولين") || cleanPrompt.contains("lantus") || cleanPrompt.contains("insulin") ->
                        "يجب تخزين أقلام الأنسولين (Lantus) غير المفتوحة في الثلاجة بين 2 و8 درجات مئوية. لا تقم بتجميدها إطلاقاً. يمكن حفظ القلم قيد الاستخدام في درجة حرارة الغرفة (أقل من 30°م) لمدة 28 يوماً.\n\n⚠️ إخلاء مسؤولية طبي: هذه معلومات تثقيفية ولا تغني عن استشارة طبيبك."
                    cleanPrompt.contains("جرعة") || cleanPrompt.contains("تناول") ->
                        "يرجى الالتزام بالجرعة المدونة على وصفتك المعتمدة. خذ مضاد أوجمنتين مع وجبة طعام لتقليل اضطرابات المعدة.\n\n⚠️ إخلاء مسؤولية طبي: لا تغير الجرعات دون استشارة الصيدلي."
                    else ->
                        "أهلاً بك في المساعد الصيدلاني الذكي PharmaX AI. كيف يمكنني مساعدتك بشأن أدويتك أو تخزين سلسلة التبريد اليوم؟\n\n⚠️ إخلاء مسؤولية طبي: لا نقدم تشخيصاً مستقلاً أو وصفات طبية."
                }
            }
            AssistantLanguage.HINDI -> {
                when {
                    cleanPrompt.contains("insulin") || cleanPrompt.contains("इंसुलिन") ->
                        "Lantus इंसुलिन को 2°C से 8°C के बीच रेफ्रिजरेटर में रखना अनिवार्य है। इसे कभी भी फ्रीजर में न रखें। इस्तेमाल किया जा रहा पेन 28 दिनों तक कमरे के सामान्य तापमान (<30°C) पर सुरक्षित रहता है।\n\n⚠️ मेडिकल डिस्क्लेमर: यह केवल शैक्षणिक जानकारी है। डॉक्टर की सलाह के बिना डोज़ न बदलें।"
                    cleanPrompt.contains("panadol") || cleanPrompt.contains("दवा") ->
                        "Panadol Extra को भोजन के बाद पानी के साथ लें। 24 घंटे में 8 से अधिक गोलियां न लें।\n\n⚠️ मेडिकल डिस्क्लेमर: गंभीर लक्षण होने पर तुरंत नज़दीकी डॉक्टर से संपर्क करें।"
                    else ->
                        "PharmaX AI क्लिनिकल असिस्टेंट में आपका स्वागत है। आप अपनी दवाओं, कोल्ड-चेन स्टोरेज या डोज़ के बारे में कोई भी प्रश्न पूछ सकते हैं।\n\n⚠️ मेडिकल डिस्क्लेमर: हम स्वचालित प्रिस्क्रिप्शन या निदान प्रदान नहीं करते हैं।"
                }
            }
            AssistantLanguage.ENGLISH -> {
                when {
                    cleanPrompt.contains("insulin") || cleanPrompt.contains("lantus") || cleanPrompt.contains("cold") ->
                        "Lantus SoloStar (Insulin Glargine) must be maintained in cold-chain storage between 2°C and 8°C prior to first use. Never freeze. Once in-use, the pen may be stored at room temperature (<30°C) for up to 28 days.\n\n⚠️ Clinical Disclaimer: Educational guidance only. Always confirm insulin titration units with your endocrinologist."
                    cleanPrompt.contains("augmentin") || cleanPrompt.contains("antibiotic") ->
                        "Augmentin 1000mg Duo should be taken at the start of a meal to enhance absorption and minimize gastrointestinal discomfort. Complete the full prescribed course even if symptoms resolve.\n\n⚠️ Clinical Disclaimer: Do not stop antibiotic regimens prematurely without physician authorization."
                    cleanPrompt.contains("interaction") || cleanPrompt.contains("atorvastatin") ->
                        "Atorvastatin (Lipitor) is best taken in the evening. Avoid consuming large volumes of grapefruit juice as it inhibits CYP3A4 metabolism.\n\n⚠️ Clinical Disclaimer: Consult your licensed pharmacist for customized drug-drug interaction reviews."
                    else ->
                        "PharmaX AI Clinical Assistant is online. You can inquire about verified formulary details, cold-chain temperature thresholds, or food-drug interactions.\n\n⚠️ Clinical Disclaimer: We do not provide autonomous clinical diagnosis or unverified prescribing."
                }
            }
        }

        val aiMsg = AssistantChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sender = "GEMINI_PHARMA_AI",
            text = replyText,
            language = lang,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            hasDisclaimer = true
        )

        _assistantMessages.update { it + userMsg + aiMsg }
    }

    fun discoverJVPartnerForMedicine(medicine: MedicineItem): JVSyncResult {
        val partner = jvPartners.first()
        val total = medicine.price
        val split = calculateEscrowSplit(total, isJv = true)
        return JVSyncResult(
            requestedMedicine = medicine.name,
            requestingPharmacy = "PharmaX Hub #01 (Jumeirah)",
            fulfillingPartner = partner,
            stockAvailable = 18,
            batchNumber = "BAT-JV-${System.currentTimeMillis() % 10000}",
            estimatedTransferMins = 16,
            partnerPrice = total,
            partnerSplitAmount = split.pharmacyShare,
            platformFeeAmount = split.platformEscrowFee,
            isEscrowReserved = true
        )
    }

    companion object {
        fun calculateEscrowSplit(totalAmount: Double, isJv: Boolean): EscrowSplit {
            val pharmacyShare = if (isJv) totalAmount * 0.85 else totalAmount * 0.90
            val jvReferralShare = if (isJv) totalAmount * 0.05 else 0.0
            val coldChainLogistics = totalAmount * 0.07
            val platformFee = totalAmount - (pharmacyShare + jvReferralShare + coldChainLogistics)
            return EscrowSplit(
                totalAmount = totalAmount,
                pharmacyShare = (pharmacyShare * 100).toLong() / 100.0,
                jvReferralShare = (jvReferralShare * 100).toLong() / 100.0,
                coldChainLogistics = (coldChainLogistics * 100).toLong() / 100.0,
                platformEscrowFee = (platformFee * 100).toLong() / 100.0,
                escrowStatus = "ESCROW_LOCKED_PENDING_DELIVERY"
            )
        }

        private fun generateSha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }.take(16)
        }
    }
}
