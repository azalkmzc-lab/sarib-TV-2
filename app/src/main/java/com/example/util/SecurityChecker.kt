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
        if (isVpnActive(context)) {
            return SecurityStatus(
                isSecure = false,
                issueType = SecurityIssueType.VPN_ACTIVE,
                message = "تم اكتشاف اتصال VPN نشط. يرجى إيقاف برامج الـ VPN لتشغيل البث بأمان وبأعلى سرعة."
            )
        }

        if (isProxyOrCaTampered(context)) {
            return SecurityStatus(
                isSecure = false,
                issueType = SecurityIssueType.PROXY_OR_CUSTOM_CA_DETECTED,
                message = "تم اكتشاف وكيل شبكة (Proxy) أو شهادة CA غير مصرح بها. يرجى تعطيل البروكسي للمتابعة."
            )
        }

        return SecurityStatus(isSecure = true)
    }

    /**
     * Detects active VPN through ConnectivityManager and NetworkInterface scanning.
     */
    fun isVpnActive(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val activeNetwork = cm.activeNetwork
                    if (activeNetwork != null) {
                        val caps = cm.getNetworkCapabilities(activeNetwork)
                        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                            return true
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val allNetworks = cm.allNetworks
                    for (network in allNetworks) {
                        val caps = cm.getNetworkCapabilities(network)
                        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                            return true
                        }
                    }
                }
            }

            // Fallback: Scan network interfaces for VPN indicators (tun, ppp, wg, etc.)
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (!intf.isUp || intf.interfaceAddresses.isEmpty()) continue
                val name = intf.name.lowercase()
                if (name.contains("tun") || name.contains("ppp") || name.contains("p2p") ||
                    name.contains("tap") || name.contains("ipsec") || name.contains("wireguard") ||
                    name.contains("wg0") || name.contains("wg1") || name.contains("shadowsocks")
                ) {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("SecurityChecker", "Error checking VPN: ${e.message}")
        }
        return false
    }

    /**
     * Detects HTTP / HTTPS proxy and user-installed CA certificate tampering.
     */
    fun isProxyOrCaTampered(context: Context): Boolean {
        try {
            // 1. Check System Properties for HTTP/HTTPS Proxy
            val proxyHost = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")
            val httpsProxyHost = System.getProperty("https.proxyHost")
            val httpsProxyPort = System.getProperty("https.proxyPort")

            if (!proxyHost.isNullOrBlank() && proxyPort != null && proxyPort != "-1") {
                return true
            }
            if (!httpsProxyHost.isNullOrBlank() && httpsProxyPort != null && httpsProxyPort != "-1") {
                return true
            }

            // 2. Check for User-Installed Custom CA certificates (often used in MITM attacks / traffic sniffing)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val keyStore = KeyStore.getInstance("AndroidCAStore")
                keyStore.load(null, null)
                val aliases = keyStore.aliases()
                while (aliases.hasMoreElements()) {
                    val alias = aliases.nextElement()
                    if (alias.startsWith("user:")) {
                        // User-installed certificate detected
                        val cert = keyStore.getCertificate(alias) as? X509Certificate
                        val issuer = cert?.issuerDN?.name ?: ""
                        // If it's a known sniffing tool CA (Charles, PortSwigger/Burp, Fiddler, HttpCanary)
                        if (issuer.contains("charles", ignoreCase = true) ||
                            issuer.contains("burp", ignoreCase = true) ||
                            issuer.contains("portswigger", ignoreCase = true) ||
                            issuer.contains("fiddler", ignoreCase = true) ||
                            issuer.contains("canary", ignoreCase = true) ||
                            issuer.contains("mitmproxy", ignoreCase = true)
                        ) {
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SecurityChecker", "Error checking Proxy/CA: ${e.message}")
        }
        return false
    }
}
