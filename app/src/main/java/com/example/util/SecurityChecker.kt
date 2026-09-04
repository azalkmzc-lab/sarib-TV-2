package com.example.util

import android.content.Context
import com.example.security.AppSecurityGuard

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
        if (isVpnActive(context)) {
            return SecurityStatus(
                isSecure = false,
                issueType = SecurityIssueType.VPN_ACTIVE,
                message = "تم رصد اتصال VPN أو بروكسي نشط على الجهاز! يرجى إيقاف الـ VPN ومتابعة المشاهدة."
            )
        }
        return SecurityStatus(isSecure = true)
    }

    /**
     * Detects active VPN through ConnectivityManager and NetworkInterface scanning.
     */
    fun isVpnActive(context: Context): Boolean {
        return AppSecurityGuard.isVpnOrProxyActive(context)
    }

    /**
     * Detects HTTP / HTTPS proxy and user-installed CA certificate tampering.
     */
    fun isProxyOrCaTampered(context: Context): Boolean {
        return AppSecurityGuard.isVpnOrProxyActive(context)
    }
}
