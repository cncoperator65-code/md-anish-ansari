package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PharmaDatabase
import com.example.data.local.entity.BatchEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.model.*
import com.example.data.repository.CountryConfigRepository
import com.example.data.repository.PharmaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PharmaX AI", appName)
  }

  @Test
  fun `verify escrow split logic`() {
    val split = PharmaRepository.calculateEscrowSplit(100.0, isJv = true)
    assertEquals(100.0, split.totalAmount, 0.01)
    assertEquals(85.0, split.pharmacyShare, 0.01)
    assertEquals(5.0, split.jvReferralShare, 0.01)
    assertEquals(7.0, split.coldChainLogistics, 0.01)
    assertEquals(3.0, split.platformEscrowFee, 0.01)
  }

  @Test
  fun `verify jv partner discovery`() {
    val repo = PharmaRepository()
    val outOfStockMed = repo.medicines.value.first { it.stockTotal == 0 }
    val result = repo.discoverJVPartnerForMedicine(outOfStockMed)
    assertTrue(result.stockAvailable > 0)
    assertTrue(result.fulfillingPartner.name.isNotEmpty())
  }

  @Test
  fun `verify medicine recall and batch quarantine blocks order`() {
    val repo = PharmaRepository()
    val testBatch = "BAT-2026-X88"

    // Initial state: not blocked
    val med1 = repo.medicines.value.first { it.id == "med_1" }
    assertFalse(med1.isRecalledOrQuarantined)

    // Action: Quarantine the batch
    val quarantineRecord = repo.quarantineBatch(
        batchNumber = testBatch,
        reason = QuarantineReason.CONTAMINATION_SUSPECT,
        authorityNoticeId = "ALERT-TEST-001",
        quarantinedBy = "Chief Pharmacist Dr. Salma"
    )

    assertEquals("ALERT-TEST-001", quarantineRecord.authorityNoticeId)
    assertTrue(repo.isBatchOrMedicineBlocked("med_1"))

    // Verify audit log entry was generated
    val auditLog = repo.auditLogs.value.first()
    assertEquals("BATCH_QUARANTINE_LOCKED", auditLog.action)
  }

  @Test
  fun `verify GS1 barcode verification catches recalled items`() {
    val repo = PharmaRepository()
    // Genuine Lantus Barcode
    val genuineScan = repo.verifyBarcodeScan("(01)06291002019231(17)270228(10)BAT-2026-X88(21)SN-982187-DXB")
    assertEquals(BarcodeVerificationResult.GENUINE_VERIFIED, genuineScan.verificationResult)

    // Recalled Barcode
    val recalledScan = repo.verifyBarcodeScan("(01)06291002019999(17)270630(10)BAT-2025-RECALL-99(21)SN-000412-RC")
    assertEquals(BarcodeVerificationResult.QUARANTINED_BLOCKED, recalledScan.verificationResult)
  }

  @Test
  fun `verify purchase order creation and pharmacist clinical signoff`() {
    val repo = PharmaRepository()
    val supplier = repo.suppliers.first()
    val items = listOf(
        PurchaseOrderItem("med_2", "Augmentin 1000mg", 50, 16.5)
    )

    val po = repo.createPurchaseOrder(supplier.id, items)
    assertEquals(PurchaseOrderStatus.SUBMITTED, po.status)
    assertEquals(825.0, po.totalCost, 0.01)

    // Pharmacist Approval
    repo.approvePurchaseOrder(po.id, "Dr. Salma Al-Khatib (DHA #8831)")
    val updatedPo = repo.purchaseOrders.value.first { it.id == po.id }
    assertEquals(PurchaseOrderStatus.PHARMACIST_APPROVED, updatedPo.status)
    assertNotNull(updatedPo.approvedByPharmacist)
  }

  @Test
  fun `verify cold chain excursion tripwire triggers quarantine`() {
    val repo = PharmaRepository()
    // Normal temp excursion test
    repo.triggerSimulatedExcursion(peakTempC = 5.2f, durationMins = 5)
    assertTrue(repo.coldChainTelemetry.value.isComplianceValid)

    // Critical breach (>8°C for >15 mins)
    repo.triggerSimulatedExcursion(peakTempC = 9.8f, durationMins = 30)
    assertFalse(repo.coldChainTelemetry.value.isComplianceValid)
    // Verify an excursion was logged
    assertTrue(repo.coldChainTelemetry.value.excursions.any { it.severity.contains("CRITICAL") })
  }

  @Test
  fun `verify escrow release with OTP handoff`() {
    val repo = PharmaRepository()
    val order = repo.orders.value.first()

    // Invalid OTP
    val fail = repo.releaseEscrowForOrder(order.id, "Anish Kumar", "000000")
    assertFalse(fail)

    // Valid OTP
    val success = repo.releaseEscrowForOrder(order.id, "Anish Kumar", "892410")
    assertTrue(success)

    val updatedOrder = repo.orders.value.first { it.id == order.id }
    assertEquals(DeliveryStage.DELIVERED, updatedOrder.deliveryStage)
    assertEquals("RELEASED_TO_PARTNERS", updatedOrder.escrowSplit.escrowStatus)
  }

  @Test
  fun `verify multilingual Gemini assistant clinical consultation`() {
    val repo = PharmaRepository()

    // English consultation
    repo.queryGeminiAssistant("How to store Lantus Insulin in cold storage?", AssistantLanguage.ENGLISH)
    val enMsg = repo.assistantMessages.value.last()
    assertTrue(enMsg.text.contains("2°C and 8°C"))
    assertTrue(enMsg.text.contains("Disclaimer"))

    // Arabic consultation
    repo.queryGeminiAssistant("كيف اخزن انسولين لانتوس؟", AssistantLanguage.ARABIC)
    val arMsg = repo.assistantMessages.value.last()
    assertTrue(arMsg.text.contains("8 درجات"))

    // Hindi consultation
    repo.queryGeminiAssistant("Lantus इंसुलिन को कैसे सुरक्षित रखें?", AssistantLanguage.HINDI)
    val hiMsg = repo.assistantMessages.value.last()
    assertTrue(hiMsg.text.contains("2°C से 8°C"))
  }

  @Test
  fun `verify FEFO allocation prioritizes earliest expiry and excludes quarantined lots`() {
    val repo = PharmaRepository()

    // Add a secondary batch for med_1 (Lantus) with earlier expiry (Dec 2026 is earlier than Feb 2027, but not expired)
    val earlierBatch = BatchRecord(
        batchId = "b_test_early",
        medicineId = "med_1",
        medicineName = "Lantus SoloStar (Insulin)",
        batchNumber = "BAT-EARLY-01",
        mfgDate = "01-Jan-2026",
        expiryDate = "31-Dec-2026",
        quantity = 10,
        storageTempRange = "2°C - 8°C"
    )
    repo.addBatch(earlierBatch)

    // Also add a quarantined batch for med_1 with future expiry
    val quarantinedBatch = BatchRecord(
        batchId = "b_test_quarantine",
        medicineId = "med_1",
        medicineName = "Lantus SoloStar (Insulin)",
        batchNumber = "BAT-QUAR-01",
        mfgDate = "01-Jan-2026",
        expiryDate = "15-Dec-2026", // even earlier expiry, but quarantined!
        quantity = 20,
        isQuarantined = true,
        quarantineReason = "Temp Breach"
    )
    repo.addBatch(quarantinedBatch)

    // Also add an expired batch for med_1
    val expiredBatch = BatchRecord(
        batchId = "b_test_expired",
        medicineId = "med_1",
        medicineName = "Lantus SoloStar (Insulin)",
        batchNumber = "BAT-EXPIRED-01",
        mfgDate = "01-Jan-2025",
        expiryDate = "01-Jan-2026", // past date
        quantity = 15
    )
    repo.addBatch(expiredBatch)

    // Request 12 units
    val fefoList = repo.calculateFefoAllocation("med_1", 12)

    // The earlier valid batch (BAT-EARLY-01) must be priority #1 and take 10 units
    val priority1 = fefoList.first { it.pickingPriority == 1 }
    assertEquals("BAT-EARLY-01", priority1.batchNumber)
    assertEquals(10, priority1.allocatedQty)
    assertTrue(priority1.isEligible)

    // The next valid batch (BAT-2026-X88) must take the remaining 2 units
    val priority2 = fefoList.first { it.pickingPriority == 2 }
    assertEquals("BAT-2026-X88", priority2.batchNumber)
    assertEquals(2, priority2.allocatedQty)
    assertTrue(priority2.isEligible)

    // The quarantined batch must NOT be eligible and have 0 allocated
    val quarItem = fefoList.first { it.batchNumber == "BAT-QUAR-01" }
    assertFalse(quarItem.isEligible)
    assertEquals(0, quarItem.allocatedQty)

    // The expired batch must NOT be eligible and have 0 allocated
    val expItem = fefoList.first { it.batchNumber == "BAT-EXPIRED-01" }
    assertFalse(expItem.isEligible)
    assertEquals(0, expItem.allocatedQty)
  }

  @Test
  fun `verify stock adjustment updates units and logs immutable movement record`() {
    val repo = PharmaRepository()
    val initialStock = repo.batches.value.first { it.batchNumber == "BAT-2025-A12" }.quantity

    // Deduct 5 units
    val success = repo.adjustStock(
        batchNumber = "BAT-2025-A12",
        delta = -5,
        reason = "Damaged during handling",
        referenceId = "AUD-DMG-001",
        actorName = "Auditor Alex"
    )
    assertTrue(success)

    val updatedBatch = repo.batches.value.first { it.batchNumber == "BAT-2025-A12" }
    assertEquals(initialStock - 5, updatedBatch.quantity)

    // Check stock movement record
    val latestMovement = repo.stockMovements.value.first()
    assertEquals("BAT-2025-A12", latestMovement.batchNumber)
    assertEquals(StockMovementType.CYCLE_COUNT_ADJUSTMENT, latestMovement.movementType)
    assertEquals(-5, latestMovement.quantityDelta)
    assertEquals(initialStock - 5, latestMovement.balanceAfter)
    assertEquals("Auditor Alex", latestMovement.actorName)
  }

  @Test
  fun `verify batch warehouse transfer and archiving`() {
    val repo = PharmaRepository()
    val testBatchNo = "BAT-2026-V44"

    // Transfer location
    val transferOk = repo.transferBatchLocation(testBatchNo, "Cold Vault #3 - Shelf B", "Logistics Handler")
    assertTrue(transferOk)

    val transferred = repo.batches.value.first { it.batchNumber == testBatchNo }
    assertEquals("Cold Vault #3 - Shelf B", transferred.warehouseLocation)

    // Archive batch
    val archiveOk = repo.archiveBatch(testBatchNo, "Dr. Salma")
    assertTrue(archiveOk)

    val archived = repo.batches.value.first { it.batchNumber == testBatchNo }
    assertTrue(archived.isArchived)
    assertFalse(archived.isFefoEligible)
  }

  @Test
  fun `verify inventory CSV report export`() {
    val repo = PharmaRepository()
    val csv = repo.generateInventoryCsvReport()
    assertTrue(csv.contains("BatchNumber,MedicineId,MedicineName,GTIN"))
    assertTrue(csv.contains("BAT-2026-X88"))
    assertTrue(csv.contains("BAT-2025-A12"))
  }

  @Test
  fun `verify Room database BatchDao operations directly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = PharmaDatabase.getInMemoryDatabase(context)
    val dao = db.batchDao()

    val testEntity = BatchEntity(
        batchNumber = "BAT-ROOM-TEST-01",
        batchId = "b_room_01",
        medicineId = "med_1",
        medicineName = "Lantus SoloStar (Insulin)",
        gtin = "06291002019231",
        manufacturer = "Sanofi Aventis",
        mfgDate = "01-Jan-2026",
        expiryDate = "31-Dec-2027",
        quantity = 75,
        reservedQuantity = 10,
        minThreshold = 20,
        reorderThreshold = 25,
        storageTempRange = "2°C - 8°C",
        currentTempCelsius = 4.2f,
        warehouseLocation = "Cold Vault #1",
        status = "OPTIMAL",
        isQuarantined = false,
        quarantineReason = null,
        isArchived = false,
        lastAuditedBy = "Lead Auditor"
    )

    dao.insertBatch(testEntity)
    val retrieved = dao.getBatchByNumber("BAT-ROOM-TEST-01")
    assertNotNull(retrieved)
    assertEquals(75, retrieved!!.quantity)
    assertEquals(10, retrieved.reservedQuantity)

    // Update location and quantity
    dao.updateLocation("BAT-ROOM-TEST-01", "Cold Vault #4 - Shelf A")
    dao.updateQuantity("BAT-ROOM-TEST-01", 90, "OPTIMAL")

    val updated = dao.getBatchByNumber("BAT-ROOM-TEST-01")!!
    assertEquals("Cold Vault #4 - Shelf A", updated.warehouseLocation)
    assertEquals(90, updated.quantity)

    db.close()
  }

  @Test
  fun `verify Room database StockMovementDao operations directly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = PharmaDatabase.getInMemoryDatabase(context)
    val dao = db.stockMovementDao()

    val movement = StockMovementEntity(
        id = "SM-TEST-999",
        batchNumber = "BAT-2026-X88",
        medicineName = "Lantus SoloStar (Insulin)",
        movementType = "FEFO_RESERVATION",
        quantityDelta = -5,
        balanceAfter = 9,
        actorName = "Order Routing Engine",
        actorRole = "System",
        reason = "Order #PHX-9901 Allocation",
        referenceId = "PHX-9901",
        timestampFormatted = "07-Nov 10:00"
    )

    dao.insertMovement(movement)
    val count = dao.getCount()
    assertEquals(1, count)

    val list = dao.getAllMovements()
    assertEquals(1, list.size)
    assertEquals("FEFO_RESERVATION", list.first().movementType)
    assertEquals(-5, list.first().quantityDelta)

    db.close()
  }

  @Test
  fun `verify stock reservation decrements available units and logs FEFO_RESERVATION movement`() {
    val repo = PharmaRepository()
    val testBatch = "BAT-2025-A12"
    val initial = repo.batches.value.first { it.batchNumber == testBatch }
    assertEquals(0, initial.reservedQuantity)
    assertEquals(42, initial.availableQuantity)

    // Reserve 8 units
    val reservedOk = repo.reserveBatchStock(testBatch, 8)
    assertTrue(reservedOk)

    val afterReserve = repo.batches.value.first { it.batchNumber == testBatch }
    assertEquals(8, afterReserve.reservedQuantity)
    assertEquals(34, afterReserve.availableQuantity)

    // Verify FEFO_RESERVATION movement was logged
    val movement = repo.stockMovements.value.first()
    assertEquals(StockMovementType.FEFO_RESERVATION, movement.movementType)
    assertEquals(-8, movement.quantityDelta)

    // Release 3 units of reservation
    val releasedOk = repo.releaseBatchReservation(testBatch, 3)
    assertTrue(releasedOk)

    val afterRelease = repo.batches.value.first { it.batchNumber == testBatch }
    assertEquals(5, afterRelease.reservedQuantity)
    assertEquals(37, afterRelease.availableQuantity)
  }

  @Test
  fun `verify stock reservation fails when requested units exceed available`() {
    val repo = PharmaRepository()
    val testBatch = "BAT-2025-A12" // total units: 42

    // Request 50 units (exceeds available 42)
    val fail = repo.reserveBatchStock(testBatch, 50)
    assertFalse(fail)

    val unchanged = repo.batches.value.first { it.batchNumber == testBatch }
    assertEquals(0, unchanged.reservedQuantity)
    assertEquals(42, unchanged.availableQuantity)
  }

  @Test
  fun `verify confirmation of batch dispense decrements both quantity and reserved quantity`() {
    val repo = PharmaRepository()
    val testBatch = "BAT-2026-X88" // quantity: 14

    // First reserve 4 units
    repo.reserveBatchStock(testBatch, 4)
    val mid = repo.batches.value.first { it.batchNumber == testBatch }
    assertEquals(4, mid.reservedQuantity)
    assertEquals(10, mid.availableQuantity)

    // Now confirm dispense of 4 units
    val dispenseOk = repo.confirmBatchDispense(testBatch, 4)
    assertTrue(dispenseOk)

    val afterDispense = repo.batches.value.first { it.batchNumber == testBatch }
    assertEquals(10, afterDispense.quantity)
    assertEquals(0, afterDispense.reservedQuantity)
    assertEquals(10, afterDispense.availableQuantity)

    val movement = repo.stockMovements.value.first()
    assertEquals(StockMovementType.DISPENSE_SALE, movement.movementType)
    assertEquals(-4, movement.quantityDelta)
  }

  @Test
  fun `verify low stock threshold triggers LOW_STOCK status upon adjustment`() {
    val repo = PharmaRepository()
    val testBatch = "BAT-2025-A12" // minThreshold = 15, current quantity = 42

    // Adjust by -30 units (new quantity = 12, which is <= minThreshold 15)
    val ok = repo.adjustStock(
        batchNumber = testBatch,
        delta = -30,
        reason = "Bulk outpatient dispensing",
        referenceId = "AUD-DISP-01"
    )
    assertTrue(ok)

    val updated = repo.batches.value.first { it.batchNumber == testBatch }
    assertEquals(12, updated.quantity)
    assertEquals(BatchStatus.LOW_STOCK, updated.status)
  }

  @Test
  fun `verify expiry window filtering correctly excludes expired and distant lots`() {
    val repo = PharmaRepository()

    // Query batches expiring within 90 days
    val expiring90d = repo.getBatchesExpiringWithin(90)
    // All items in the result must have daysUntilExpiry between 1 and 90
    assertTrue(expiring90d.all { it.daysUntilExpiry() in 1..90 })
    // Recalled or past expired lots must not be included
    assertFalse(expiring90d.any { it.daysUntilExpiry() <= 0 })
  }

  @Test
  fun `verify PharmaRepository data persistence across repository instances with Room DB`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = PharmaDatabase.getInMemoryDatabase(context)

    // Pre-populate db through first repo
    val repo1 = PharmaRepository(database = db)
    val newBatch = BatchRecord(
        batchId = "b_persist_01",
        medicineId = "med_1",
        medicineName = "Lantus SoloStar (Insulin)",
        batchNumber = "BAT-PERSIST-99",
        mfgDate = "01-Jan-2026",
        expiryDate = "31-Dec-2027",
        quantity = 88,
        storageTempRange = "2°C - 8°C"
    )
    repo1.addBatch(newBatch)

    // Check directly in SQLite database
    val inDb = db.batchDao().getBatchByNumber("BAT-PERSIST-99")
    assertNotNull(inDb)
    assertEquals(88, inDb!!.quantity)
    assertEquals("BAT-PERSIST-99", inDb.batchNumber)

    // Now instantiate a second repository on the same database
    val repo2 = PharmaRepository(database = db)
    val batchFromDb = repo2.database!!.batchDao().getBatchByNumber("BAT-PERSIST-99")
    assertNotNull(batchFromDb)
    assertEquals(88, batchFromDb!!.quantity)

    db.close()
  }

  @Test
  fun `verify UAE displays AED and correct regional logo`() {
    val uae = CountryConfigRegistry.UAE
    assertEquals("AE", uae.countryCode)
    assertEquals("AED", uae.currencyCode)
    assertEquals("د.إ", uae.currencySymbol)
    assertEquals("🇦🇪", uae.flagEmoji)
    assertEquals("PharmaX UAE", uae.regionalBrand.brandTitle)
    assertTrue(uae.regionalBrand.isCustomAssetConfigured)
    assertEquals("MOHAP / DHA / Tatmeen Compliant", uae.regionalBrand.regulatoryAccreditation)
  }

  @Test
  fun `verify USA displays USD and correct regional logo`() {
    val usa = CountryConfigRegistry.USA
    assertEquals("US", usa.countryCode)
    assertEquals("USD", usa.currencyCode)
    assertEquals("$", usa.currencySymbol)
    assertEquals("🇺🇸", usa.flagEmoji)
    assertEquals("PharmaX USA", usa.regionalBrand.brandTitle)
    assertTrue(usa.regionalBrand.isCustomAssetConfigured)
    assertTrue(usa.regulatoryAuthority.contains("FDA"))
  }

  @Test
  fun `verify India displays INR and correct regional logo`() {
    val ind = CountryConfigRegistry.INDIA
    assertEquals("IN", ind.countryCode)
    assertEquals("INR", ind.currencyCode)
    assertEquals("₹", ind.currencySymbol)
    assertEquals("🇮🇳", ind.flagEmoji)
    assertEquals("PharmaX Bharat / India", ind.regionalBrand.brandTitle)
    assertTrue(ind.regionalBrand.isCustomAssetConfigured)
    assertEquals("CDSCO", ind.regulatoryCode)
  }

  @Test
  fun `verify Nepal displays NPR and correct regional logo`() {
    val npl = CountryConfigRegistry.NEPAL
    assertEquals("NP", npl.countryCode)
    assertEquals("NPR", npl.currencyCode)
    assertEquals("रु", npl.currencySymbol)
    assertEquals("🇳🇵", npl.flagEmoji)
    assertEquals("PharmaX Nepal", npl.regionalBrand.brandTitle)
    assertTrue(npl.regionalBrand.isCustomAssetConfigured)
    assertEquals("DDA_NEPAL", npl.regulatoryCode)
  }

  @Test
  fun `verify Eurozone country displays EUR`() {
    val de = CountryConfigRegistry.GERMANY_EUROZONE
    assertEquals("DE", de.countryCode)
    assertEquals("EUR", de.currencyCode)
    assertEquals("€", de.currencySymbol)
    assertTrue(de.isEurozone)
    assertEquals("PharmaX Europe", de.regionalBrand.brandTitle)
  }

  @Test
  fun `verify non-euro European country displays its correct national currency (CHF)`() {
    val swiss = CountryConfigRegistry.SWITZERLAND_NON_EURO
    assertEquals("CH", swiss.countryCode)
    assertEquals("CHF", swiss.currencyCode)
    assertEquals("CHF", swiss.currencySymbol)
    assertFalse(swiss.isEurozone) // Crucial rule: Switzerland is European but non-euro
    assertEquals("SWISSMEDIC", swiss.regulatoryCode)
    assertEquals("PharmaX Switzerland", swiss.regionalBrand.brandTitle)
  }

  @Test
  fun `verify country selection persists after restart with SharedPreferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo1 = CountryConfigRepository(context)

    // Switch to Nepal
    repo1.selectCountry("NP", isManualUserSelection = true)
    assertEquals("NP", repo1.activeCountry.value.countryCode)

    // Simulate app restart by instantiating new repository instance with same context
    val repo2 = CountryConfigRepository(context)
    assertEquals("NP", repo2.activeCountry.value.countryCode)
    assertEquals("NPR", repo2.activeCountry.value.currencyCode)
    assertEquals("रु", repo2.activeCountry.value.currencySymbol)
  }

  @Test
  fun `verify missing regional logo gracefully falls back to PharmaX master brand`() {
    val master = CountryConfigRegistry.MASTER_FALLBACK_BRAND
    assertEquals("PharmaX AI Global", master.brandTitle)
    assertFalse(master.isCustomAssetConfigured)
    assertEquals("International Pharmaceutical Platform", master.regionalSubtitle)
  }

  @Test
  fun `verify currency formatting handles decimals with precision without floating point corruption`() {
    val usa = CountryConfigRegistry.USA
    val formattedUsd = usa.formatCurrency(java.math.BigDecimal("1299.99"))
    assertTrue(formattedUsd.contains("1,299.99") || formattedUsd.contains("1299.99"))

    val uae = CountryConfigRegistry.UAE
    val formattedAed = uae.formatCurrency(java.math.BigDecimal("500.50"))
    assertTrue(formattedAed.contains("500.50") || formattedAed.contains("500,50"))

    val npl = CountryConfigRegistry.NEPAL
    val converted = npl.convertFromUsd(java.math.BigDecimal("100.00"))
    assertEquals(java.math.BigDecimal("13488.00"), converted.convertedAmount)
    assertEquals("NPR", converted.targetCurrency)
    assertEquals("Nepal Rastra Bank Exchange Rate", converted.rateSource)
  }

  @Test
  fun `verify changing country does not corrupt stored base prices or financial transactions`() {
    val repo = PharmaRepository()
    val med = repo.medicines.value.first { it.id == "med_1" }
    val initialBasePrice = med.price

    // Switch country to India
    repo.selectCountry("IN")
    assertEquals("IN", repo.activeCountry.value.countryCode)
    // Underlying product catalog base price is preserved
    assertEquals(initialBasePrice, repo.medicines.value.first { it.id == "med_1" }.price, 0.001)

    // Switch country to Switzerland
    repo.selectCountry("CH")
    assertEquals("CH", repo.activeCountry.value.countryCode)
    assertEquals(initialBasePrice, repo.medicines.value.first { it.id == "med_1" }.price, 0.001)

    // Audit log recorded
    val logs = repo.auditLogs.value
    assertTrue(logs.any { it.action == "COUNTRY_SELECTION_UPDATED" && it.resource == "CH" })
  }
}
