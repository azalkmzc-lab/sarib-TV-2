package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import java.net.NetworkInterface
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.Collections

enum class SecurityIssueType {
    VPN_ACTIVE,
    PROXY_OR_CUSTOM_CA_DETECTED
}

data class SecurityStatus(
    val isSecure: Boolean,
    val issueType: SecurityIssueType? = null,
    val message: String = ""
)

object SecurityChecker {

    /**
     * Checks if the device is currently running under a VPN or using a CA Proxy/Man-In-The-Middle tool.
     */
    fun performSecurityAudit(context: Context): SecurityStatus {
        // Disabled: VPN and proxy restrictions bypassed upon user request
        return SecurityStatus(isSecure = true)
    }

    /**
     * Detects active VPN through ConnectivityManager and NetworkInterface scanning.
     */
    fun isVpnActive(context: Context): Boolean {
        // Disabled upon user request
        return false
    }

    /**
     * Detects HTTP / HTTPS proxy and user-installed CA certificate tampering.
     */
    fun isProxyOrCaTampered(context: Context): Boolean {
        // Disabled upon user request
        return false
    }
}
