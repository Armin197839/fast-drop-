package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

data class DiscoveredPeer(
    val ip: String,
    val port: Int = 8080,
    val name: String = "دستگاه FastDrop"
)

class PeerDiscoveryService {

    suspend fun scanLocalSubnet(baseIp: String, port: Int = 8080): List<DiscoveredPeer> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<DiscoveredPeer>()
        val parts = baseIp.split(".")
        if (parts.size != 4) return@withContext discovered

        val subnetPrefix = "${parts[0]}.${parts[1]}.${parts[2]}."
        val myLastSegment = parts[3].toIntOrNull() ?: -1

        // Probe active FastDrop nodes in local subnet (proactive probe of standard range)
        val ranges = (1..254).filter { it != myLastSegment }

        for (host in ranges.take(50)) { // Fast sweep on nearby clients
            val testIp = "$subnetPrefix$host"
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(testIp, port), 80) // 80ms fast timeout
                socket.close()
                discovered.add(DiscoveredPeer(ip = testIp, port = port, name = "FastDrop ($testIp)"))
            } catch (_: Exception) {}
        }
        discovered
    }
}
