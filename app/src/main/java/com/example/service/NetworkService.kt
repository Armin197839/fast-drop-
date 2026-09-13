package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale

class NetworkService(private val context: Context) {

    fun getWifiIp(): String? {
        // Method 1: Scan Network Interfaces for an active IPv4 address on WLAN/LAN
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            // Priority 1: wlan interfaces
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                if (intf.name.startsWith("wlan", ignoreCase = true) || 
                    intf.name.startsWith("ap", ignoreCase = true) ||
                    intf.name.startsWith("eth", ignoreCase = true)) {
                    val addrs = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            val ip = addr.hostAddress
                            if (!ip.isNullOrBlank() && !ip.startsWith("127.")) {
                                return ip
                            }
                        }
                    }
                }
            }

            // Priority 2: any non-loopback site-local IPv4
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val ip = addr.hostAddress
                        if (!ip.isNullOrBlank() && !ip.startsWith("127.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Method 2: Fallback to WifiManager
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiInfo = wifiManager?.connectionInfo
            val ipInt = wifiInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                return String.format(
                    Locale.US,
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
        } catch (_: Exception) {}

        return null
    }

    fun isConnectedToWifi(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = connectivityManager?.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (_: Exception) {
            false
        }
    }
}
