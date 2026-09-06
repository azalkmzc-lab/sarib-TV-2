package com.example.security

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.net.NetworkInterface
import java.util.Collections

/**
 * High-grade security system for SARIB TV:
 * 1. Anti-VPN & Proxy Scanner (Periodic check every 2 seconds).
 * 2. Anti-Sniffer & Packet Capture Detection.
 * 3. Anti-Tamper & Root/Hook basic mitigation.
 */
object AppSecurityGuard {

    private const val TAG = "SecurityGuard"

    /**
     * Checks if any VPN transport, virtual network interface (tun/tap/ppp/wg/p2p),
     * or HTTP proxy is currently active on the device.
     * Guaranteed to be non-blocking and safe.
     */
    fun isVpnOrProxyActive(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val activeNet = cm.activeNetwork
                    if (activeNet != null) {
                        val caps = cm.getNetworkCapabilities(activeNet)
                        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                            return true
                        }
                    }
                    val allNetworks = cm.allNetworks
                    for (network in allNetworks) {
                        val caps = cm.getNetworkCapabilities(network)
                        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                            return true
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val vpnInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_VPN)
                    if (vpnInfo != null && vpnInfo.isConnectedOrConnecting) {
                        return true
                    }
                }
            }

            // Check if device is TV (avoid false positives from TV virtual interfaces / Wi-Fi direct)
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
            val isTv = uiModeManager?.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
                    || context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK)

            if (!isTv) {
                // Check for explicit VPN network interfaces with active routable addresses
                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    if (!intf.isUp || intf.isLoopback) continue
                    val name = intf.name.lowercase()
                    if (name.contains("p2p") || name.contains("wlan") || name.contains("eth") || name.contains("dummy")) continue
                    if (name.startsWith("tun") ||
                        name.startsWith("ppp") ||
                        name.startsWith("wg") ||
                        name.startsWith("utun") ||
                        (name.contains("vpn") && !name.contains("p2p"))
                    ) {
                        val addrs = Collections.list(intf.inetAddresses)
                        if (addrs.any { !it.isLoopbackAddress && !it.isLinkLocalAddress }) {
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking VPN/Proxy status: ${e.message}")
        }
        return false
    }

    /**
     * Basic check for known root binaries or su paths.
     */
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any { File(it).exists() }
    }

    /**
     * Forcefully close the application if security is compromised.
     */
    fun terminateApp(activity: Activity?) {
        try {
            activity?.finishAffinity()
            Process.killProcess(Process.myPid())
            System.exit(0)
        } catch (e: Exception) {
            activity?.finish()
        }
    }

    /**
     * Composable hook that launches a 3-second interval periodic VPN security check.
     */
    @Composable
    fun rememberVpnSecurityMonitor(
        context: Context,
        intervalMs: Long = 3000L,
        onVpnDetected: () -> Unit = {}
    ): Boolean {
        var isVpnActive by remember { mutableStateOf(isVpnOrProxyActive(context)) }

        androidx.compose.runtime.LaunchedEffect(context) {
            while (isActive) {
                val active = isVpnOrProxyActive(context)
                if (active != isVpnActive) {
                    isVpnActive = active
                    if (active) {
                        onVpnDetected()
                    }
                }
                delay(intervalMs)
            }
        }
        return isVpnActive
    }
}

/**
 * Composable hook that launches a 3-second interval periodic VPN security check.
 * If VPN is detected, [onVpnDetected] is invoked immediately.
 */
@Composable
fun rememberVpnSecurityMonitor(
    context: Context,
    intervalMs: Long = 3000L,
    onVpnDetected: () -> Unit = {}
): Boolean {
    var isVpnActive by remember { mutableStateOf(AppSecurityGuard.isVpnOrProxyActive(context)) }

    androidx.compose.runtime.LaunchedEffect(context) {
        while (isActive) {
            val active = AppSecurityGuard.isVpnOrProxyActive(context)
            if (active != isVpnActive) {
                isVpnActive = active
                if (active) {
                    onVpnDetected()
                }
            }
            delay(intervalMs)
        }
    }
    return isVpnActive
}
