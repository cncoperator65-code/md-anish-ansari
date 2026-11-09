package com.example.data.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Regional brand asset definition for PharmaX AI country branches.
 */
data class RegionalBrandConfig(
    val brandTitle: String,
    val regionalSubtitle: String,
    val regulatoryAccreditation: String,
    val primaryAccentHex: Long,
    val secondaryAccentHex: Long,
    val regionalTag: String,
    val badgeIconName: String,
    val isCustomAssetConfigured: Boolean = true
)

/**
 * Centralized immutable configuration model for global countries.
 */
data class CountryConfig(
    val countryCode: String,            // ISO 3166-1 alpha-2 (e.g. "AE", "US", "IN", "NP", "GB", "DE", "CH")
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,           // ISO 4217 (e.g. "AED", "USD", "INR", "NPR", "GBP", "EUR", "CHF")
    val currencySymbol: String,
    val currencyDisplayName: String,
    val localeLanguage: String,
    val localeCountry: String,
    val timeZone: String,
    val dateFormatPattern: String,
    val regulatoryAuthority: String,
    val regulatoryCode: String,
    val taxName: String,
    val taxRatePercent: Double,
    val regionalBrand: RegionalBrandConfig,
    val exchangeRateToUsd: BigDecimal,  // Central Bank Reference / Trusted FX Benchmark
    val fxSource: String = "Central Bank Reference Benchmark",
    val fxLastUpdated: String = "2026-11-08T00:00:00Z",
    val isEurozone: Boolean = false,
    val supportedPaymentGateways: List<String> = listOf("STRIPE_CONNECT", "LOCAL_BANK_RAIL")
) {
    val locale: Locale get() = Locale(localeLanguage, localeCountry)

    /**
     * Formats an amount precisely with locale and currency symbol.
     */
    fun formatCurrency(amount: Double): String {
        return formatCurrency(BigDecimal.valueOf(amount))
    }

    fun formatCurrency(amount: BigDecimal): String {
        val rounded = amount.setScale(2, RoundingMode.HALF_UP)
        return try {
            val format = NumberFormat.getCurrencyInstance(locale)
            // Use custom currency symbol if standard format does not display local glyph
            val formatted = format.format(rounded)
            if (currencySymbol.isNotEmpty() && !formatted.contains(currencySymbol)) {
                "$currencySymbol ${String.format(locale, "%,.2f", rounded)}"
            } else {
                formatted
            }
        } catch (e: Exception) {
            "$currencySymbol ${String.format(Locale.US, "%,.2f", rounded)}"
        }
    }

    /**
     * Converts a USD-based amount to this country's local currency using precise BigDecimal arithmetic.
     * Retains exact traceability of the conversion.
     */
    fun convertFromUsd(baseAmountUsd: BigDecimal): ConvertedPrice {
        val converted = baseAmountUsd.multiply(exchangeRateToUsd).setScale(2, RoundingMode.HALF_UP)
        return ConvertedPrice(
            baseAmount = baseAmountUsd,
            baseCurrency = "USD",
            convertedAmount = converted,
            targetCurrency = currencyCode,
            exchangeRate = exchangeRateToUsd,
            rateSource = fxSource,
            rateTimestamp = fxLastUpdated,
            formattedDisplay = formatCurrency(converted)
        )
    }

    fun convertFromUsd(baseAmountUsd: Double): ConvertedPrice {
        return convertFromUsd(BigDecimal.valueOf(baseAmountUsd))
    }
}

data class ConvertedPrice(
    val baseAmount: BigDecimal,
    val baseCurrency: String,
    val convertedAmount: BigDecimal,
    val targetCurrency: String,
    val exchangeRate: BigDecimal,
    val rateSource: String,
    val rateTimestamp: String,
    val formattedDisplay: String
)

/**
 * Pre-configured global country dataset for PharmaX AI.
 * Covers UAE, USA, India, Nepal, UK, Eurozone (Germany, France), and Non-Eurozone European countries (Switzerland, Sweden).
 */
object CountryConfigRegistry {

    val UAE = CountryConfig(
        countryCode = "AE",
        countryName = "United Arab Emirates",
        flagEmoji = "🇦🇪",
        currencyCode = "AED",
        currencySymbol = "د.إ",
        currencyDisplayName = "UAE Dirham",
        localeLanguage = "ar",
        localeCountry = "AE",
        timeZone = "Asia/Dubai",
        dateFormatPattern = "dd/MM/yyyy",
        regulatoryAuthority = "Ministry of Health & Prevention (MOHAP) / DHA / Tatmeen",
        regulatoryCode = "MOHAP",
        taxName = "VAT",
        taxRatePercent = 5.0,
        exchangeRateToUsd = BigDecimal("3.6725"), // Fixed peg
        fxSource = "UAE Central Bank Official Peg",
        regionalBrand = RegionalBrandConfig(
            brandTitle = "PharmaX UAE",
            regionalSubtitle = "Middle East Regional Hub • Dubai & Abu Dhabi",
            regulatoryAccreditation = "MOHAP / DHA / Tatmeen Compliant",
            primaryAccentHex = 0xFF00897B, // Pharma Teal
            secondaryAccentHex = 0xFFD4AF37, // Gold accent
            regionalTag = "UAE Regional Office",
            badgeIconName = "ic_brand_uae",
            isCustomAssetConfigured = true
        ),
        supportedPaymentGateways = listOf("STRIPE_CONNECT", "NETWORK_INTERNATIONAL", "RAZORPAY_UAE")
    )

    val USA = CountryConfig(
        countryCode = "US",
        countryName = "United States",
        flagEmoji = "🇺🇸",
        currencyCode = "USD",
        currencySymbol = "$",
        currencyDisplayName = "US Dollar",
        localeLanguage = "en",
        localeCountry = "US",
        timeZone = "America/New_York",
        dateFormatPattern = "MM/dd/yyyy",
        regulatoryAuthority = "U.S. Food and Drug Administration (FDA / DSCSA)",
        regulatoryCode = "US_FDA",
        taxName = "State Sales Tax",
        taxRatePercent = 7.25,
        exchangeRateToUsd = BigDecimal("1.0000"),
        fxSource = "US Federal Reserve Base",
        regionalBrand = RegionalBrandConfig(
            brandTitle = "PharmaX USA",
            regionalSubtitle = "North America Operations • DSCSA Verified",
            regulatoryAccreditation = "U.S. FDA & DEA Licensed",
            primaryAccentHex = 0xFF1E40AF, // Deep Cobalt Blue
            secondaryAccentHex = 0xFFDC2626, // Crimson
            regionalTag = "USA Headquarters",
            badgeIconName = "ic_brand_usa",
            isCustomAssetConfigured = true
        ),
        supportedPaymentGateways = listOf("STRIPE_CONNECT", "ACH_TRANSFER", "APPLE_PAY")
    )

    val INDIA = CountryConfig(
        countryCode = "IN",
        countryName = "India",
        flagEmoji = "🇮🇳",
        currencyCode = "INR",
        currencySymbol = "₹",
        currencyDisplayName = "Indian Rupee",
        localeLanguage = "en",
        localeCountry = "IN",
        timeZone = "Asia/Kolkata",
        dateFormatPattern = "dd/MM/yyyy",
        regulatoryAuthority = "Central Drugs Standard Control Org (CDSCO)",
        regulatoryCode = "CDSCO",
        taxName = "GST (Pharma)",
        taxRatePercent = 12.0,
        exchangeRateToUsd = BigDecimal("84.30"),
        fxSource = "Reserve Bank of India Reference Rate",
        regionalBrand = RegionalBrandConfig(
            brandTitle = "PharmaX Bharat / India",
            regionalSubtitle = "South Asia Regional Hub • Bengaluru & Mumbai",
            regulatoryAccreditation = "CDSCO & State FDA Verified",
            primaryAccentHex = 0xFFD97706, // Deep Saffron
            secondaryAccentHex = 0xFF059669, // India Green
            regionalTag = "India Regional Hub",
            badgeIconName = "ic_brand_india",
            isCustomAssetConfigured = true
        ),
        supportedPaymentGateways = listOf("RAZORPAY_ROUTE", "UPI_AUTOPAY", "STRIPE_CONNECT")
    )

    val NEPAL = CountryConfig(
        countryCode = "NP",
        countryName = "Nepal",
        flagEmoji = "🇳🇵",
        currencyCode = "NPR",
        currencySymbol = "रु",
        currencyDisplayName = "Nepalese Rupee",
        localeLanguage = "ne",
        localeCountry = "NP",
        timeZone = "Asia/Kathmandu",
        dateFormatPattern = "dd/MM/yyyy",
        regulatoryAuthority = "Department of Drug Administration (DDA Nepal)",
        regulatoryCode = "DDA_NEPAL",
        taxName = "VAT",
        taxRatePercent = 13.0,
        exchangeRateToUsd = BigDecimal("134.88"), // Pegged to INR at 1.6
        fxSource = "Nepal Rastra Bank Exchange Rate",
        regionalBrand = RegionalBrandConfig(
            brandTitle = "PharmaX Nepal",
            regionalSubtitle = "Himalayan Healthcare Hub • Kathmandu",
            regulatoryAccreditation = "DDA Nepal Certified",
            primaryAccentHex = 0xFFDC2626, // Crimson Red
            secondaryAccentHex = 0xFF1E3A8A, // Blue border
            regionalTag = "Nepal Regional Office",
            badgeIconName = "ic_brand_nepal",
            isCustomAssetConfigured = true
        ),
        supportedPaymentGateways = listOf("FONEPAY", "ESEWA", "STRIPE_CONNECT")
    )

    val UNITED_KINGDOM = CountryConfig(
        countryCode = "GB",
        countryName = "United Kingdom",
        flagEmoji = "🇬🇧",
        currencyCode = "GBP",
        currencySymbol = "£",
        currencyDisplayName = "British Pound",
        localeLanguage = "en",
        localeCountry = "GB",
        timeZone = "Europe/London",
        dateFormatPattern = "dd/MM/yyyy",
        regulatoryAuthority = "Medicines & Healthcare products Regulatory Agency (MHRA)",
        regulatoryCode = "MHRA",
        taxName = "VAT",
        taxRatePercent = 20.0,
        exchangeRateToUsd = BigDecimal("0.7725"),
        fxSource = "Bank of England Reference Rate",
        regionalBrand = RegionalBrandConfig(
            brandTitle = "PharmaX UK",
            regionalSubtitle = "UK & British Isles Division • London Hub",
            regulatoryAccreditation = "MHRA & GPhC Regulated",
            primaryAccentHex = 0xFF1E3A8A, // Navy
            secondaryAccentHex = 0xFFDC2626, // Red
            regionalTag = "UK Hub",
            badgeIconName = "ic_brand_uk",
            isCustomAssetConfigured = true
        ),
        supportedPaymentGateways = listOf("STRIPE_CONNECT", "BACS_DIRECT_DEBIT")
    )

    val GERMANY_EUROZONE = CountryConfig(
        countryCode = "DE",
        countryName = "Germany (Eurozone)",
        flagEmoji = "🇩🇪",
        currencyCode = "EUR",
        currencySymbol = "€",
        currencyDisplayName = "Euro",
        localeLanguage = "de",
        localeCountry = "DE",
        timeZone = "Europe/Berlin",
        dateFormatPattern = "dd.MM.yyyy",
        regulatoryAuthority = "European Medicines Agency (EMA) / BfArM",
        regulatoryCode = "EMA_BFARM",
        taxName = "MwSt",
        taxRatePercent = 19.0,
        isEurozone = true,
        exchangeRateToUsd = BigDecimal("0.9250"),
        fxSource = "European Central Bank Reference Rate",
        regionalBrand = RegionalBrandConfig(
            brandTitle = "PharmaX Europe",
            regionalSubtitle = "Eurozone Operations • Frankfurt Hub",
            regulatoryAccreditation = "EMA & BfArM FMD Verified",
            primaryAccentHex = 0xFF0284C7, // EU Blue
            secondaryAccentHex = 0xFFFBBF24, // EU Star Gold
            regionalTag = "Eurozone Regional Hub",
            badgeIconName = "ic_brand_eu",
            isCustomAssetConfigured = true
        ),
        supportedPaymentGateways = listOf("SEPA_CREDIT", "STRIPE_CONNECT", "SOFORT")
    )

    val SWITZERLAND_NON_EURO = CountryConfig(
        countryCode = "CH",
        countryName = "Switzerland",
        flagEmoji = "🇨🇭",
        currencyCode = "CHF",
        currencySymbol = "CHF",
        currencyDisplayName = "Swiss Franc",
        localeLanguage = "de",
        localeCountry = "CH",
        timeZone = "Europe/Zurich",
        dateFormatPattern = "dd.MM.yyyy",
        regulatoryAuthority = "Swissmedic (Swiss Agency for Therapeutic Products)",
        regulatoryCode = "SWISSMEDIC",
        taxName = "MWST",
        taxRatePercent = 8.1,
        isEurozone = false, // Distinct national currency, non-euro European country
        exchangeRateToUsd = BigDecimal("0.8750"),
        fxSource = "Swiss National Bank Benchmark",
        regionalBrand = RegionalBrandConfig(
            brandTitle = "PharmaX Switzerland",
            regionalSubtitle = "Alpine Biopharma Division • Basel & Zurich",
            regulatoryAccreditation = "Swissmedic GMP Certified",
            primaryAccentHex = 0xFFB91C1C, // Swiss Red
            secondaryAccentHex = 0xFFFFFFFF, // White Cross
            regionalTag = "Swiss National Division",
            badgeIconName = "ic_brand_swiss",
            isCustomAssetConfigured = true
        ),
        supportedPaymentGateways = listOf("TWINT", "POSTFINANCE", "STRIPE_CONNECT")
    )

    // Master fallback brand configuration for countries without a bespoke regional emblem
    val MASTER_FALLBACK_BRAND = RegionalBrandConfig(
        brandTitle = "PharmaX AI Global",
        regionalSubtitle = "International Pharmaceutical Platform",
        regulatoryAccreditation = "Standard Certified Pharmacy Network",
        primaryAccentHex = 0xFF00897B,
        secondaryAccentHex = 0xFF00ACC1,
        regionalTag = "Global Master",
        badgeIconName = "ic_brand_master",
        isCustomAssetConfigured = false
    )

    val ALL_COUNTRIES: List<CountryConfig> = listOf(
        UAE,
        USA,
        INDIA,
        NEPAL,
        UNITED_KINGDOM,
        GERMANY_EUROZONE,
        SWITZERLAND_NON_EURO
    )

    fun findByCode(code: String): CountryConfig {
        return ALL_COUNTRIES.firstOrNull { it.countryCode.equals(code, ignoreCase = true) }
            ?: UAE // Default to UAE as configured in current app baseline
    }
}
