package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CountryConfig
import com.example.data.model.CountryConfigRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Repository responsible for resolving, persisting, and managing the authoritative active country configuration.
 * Adheres strictly to the resolution priority:
 * 1. Verified account/business country from backend
 * 2. User-selected country persisted in preferences
 * 3. Device locale/SIM country as a suggestion only
 * 4. Fallback to default (UAE)
 */
class CountryConfigRepository(
    private val context: Context? = null
) {
    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("pharmax_country_prefs", Context.MODE_PRIVATE)
    }

    private val _activeCountry = MutableStateFlow(resolveInitialCountry())
    val activeCountry: StateFlow<CountryConfig> = _activeCountry.asStateFlow()

    private val _availableCountries = MutableStateFlow(CountryConfigRegistry.ALL_COUNTRIES)
    val availableCountries: StateFlow<List<CountryConfig>> = _availableCountries.asStateFlow()

    // Verified backend organization country (if assigned by backend tenant verification)
    private var verifiedBackendCountryCode: String? = null

    /**
     * Resolves country following the required multi-tier priority.
     */
    private fun resolveInitialCountry(): CountryConfig {
        // Priority 1: Verified backend tenant country (e.g. if pharmacy has legal license in UAE or USA)
        verifiedBackendCountryCode?.let { code ->
            return CountryConfigRegistry.findByCode(code)
        }

        // Priority 2: Stored profile preference persisted in SharedPreferences
        prefs?.getString(KEY_SAVED_COUNTRY_CODE, null)?.let { savedCode ->
            return CountryConfigRegistry.findByCode(savedCode)
        }

        // Priority 3: Device locale as a hint
        val deviceCountry = Locale.getDefault().country
        if (!deviceCountry.isNullOrBlank()) {
            val matched = CountryConfigRegistry.ALL_COUNTRIES.firstOrNull {
                it.countryCode.equals(deviceCountry, ignoreCase = true)
            }
            if (matched != null) {
                return matched
            }
        }

        // Priority 4: Default baseline
        return CountryConfigRegistry.UAE
    }

    /**
     * Updates the active country upon manual selection or verified authorization.
     * Persists selection across app restarts.
     * Returns true if selection changed successfully.
     */
    fun selectCountry(countryCode: String, isManualUserSelection: Boolean = true): Boolean {
        val target = CountryConfigRegistry.findByCode(countryCode)
        _activeCountry.value = target

        if (isManualUserSelection) {
            prefs?.edit()?.putString(KEY_SAVED_COUNTRY_CODE, target.countryCode)?.apply()
        }
        return true
    }

    /**
     * Sets the authoritative backend-verified tenant country.
     */
    fun setVerifiedBackendCountry(countryCode: String) {
        verifiedBackendCountryCode = countryCode
        selectCountry(countryCode, isManualUserSelection = false)
    }

    /**
     * Returns true if a regional logo asset is configured for the country, or false if falling back to master.
     */
    fun isRegionalLogoConfigured(config: CountryConfig): Boolean {
        return config.regionalBrand.isCustomAssetConfigured
    }

    companion object {
        private const val KEY_SAVED_COUNTRY_CODE = "saved_country_code"
    }
}
