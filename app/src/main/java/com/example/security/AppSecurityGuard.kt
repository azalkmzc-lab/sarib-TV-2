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
     */
    fun isVpnOrProxyActive(context: Context): Boolean {
        try {
            // 1. Check ConnectivityManager NetworkCapabilities for TRANSPORT_VPN
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
                } else {
                    @Suppress("DEPRECATION")
                    val networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_VPN)
                    if (networkInfo != null && networkInfo.isConnected) {
                        return true
                    }
                }
            }

            // 2. Inspect all network interfaces for virtual tunnels (tun, tap, ppp, wg, utun, etc.)
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (!intf.isUp || intf.interfaceAddresses.isEmpty()) continue
                val name = intf.name.lowercase()
                if (name.contains("tun") ||
                    name.contains("ppp") ||
                    name.contains("p2p") ||
                    name.contains("tap") ||
                    name.contains("wg") ||
                    name.contains("utun") ||
                    name.contains("dummy")
                ) {
                    return true
                }
            }

            // 3. Check for System Proxy (used by Charles / Burp / HttpCanary / Packet Capture)
            val proxyHost = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")
            if (!proxyHost.isNullOrBlank() && proxyPort != null) {
                return true
            }

        } catch (e: Exception) {
            Log.w(TAG, "VPN check exception: ${e.message}")
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
     * Composable hook that launches a 2-second interval periodic VPN security check.
     */
    @Composable
    fun rememberVpnSecurityMonitor(
        context: Context,
        onVpnDetected: () -> Unit
    ): Boolean {
        var vpnDetected by remember { mutableStateOf(false) }

        DisposableEffect(Unit) {
            val scope = CoroutineScope(Dispatchers.Main)
            var monitorJob: Job? = scope.launch {
                while (isActive) {
                    val isVpn = isVpnOrProxyActive(context)
                    if (isVpn) {
                        vpnDetected = true
                        onVpnDetected()
                        break
                    }
                    delay(2000) // Periodic check every 2 seconds
                }
            }

            onDispose {
                monitorJob?.cancel()
                monitorJob = null
            }
        }

        return vpnDetected
    }
}

/**
 * Composable hook that launches a 2-second interval periodic VPN security check.
 * If VPN is detected, [onVpnDetected] is invoked immediately.
 */
@Composable
fun rememberVpnSecurityMonitor(
    context: Context,
    onVpnDetected: () -> Unit
): Boolean {
    var vpnDetected by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val scope = CoroutineScope(Dispatchers.Main)
        var monitorJob: Job? = scope.launch {
            while (isActive) {
                val isVpn = AppSecurityGuard.isVpnOrProxyActive(context)
                if (isVpn) {
                    vpnDetected = true
                    onVpnDetected()
                    break
                }
                delay(2000) // Periodic check every 2 seconds
            }
        }

        onDispose {
            monitorJob?.cancel()
            monitorJob = null
        }
    }

    return vpnDetected
}
