package app.dkdstrb.excitedtune.utils

import android.content.Context
import android.util.Log
import app.dkdstrb.excitedtune.constants.DnsCustomUrlKey
import app.dkdstrb.excitedtune.constants.DnsEnabledKey
import app.dkdstrb.excitedtune.constants.DnsMode
import app.dkdstrb.excitedtune.constants.DnsModeKey
import app.dkdstrb.excitedtune.extensions.toEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object DnsHelper {

    private const val TAG = "DnsHelper"

    private val bootstrapClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private var activeDnsInstance: Dns = Dns.SYSTEM

    fun getActiveDns(): Dns = activeDnsInstance

    /**
     * Re-creates the Dns instance based on user preferences and updates YouTube / OkHttp instances.
     */
    fun applyDns(context: Context) {
        val enabled = context.dataStore[DnsEnabledKey] ?: false
        val mode = context.dataStore[DnsModeKey].toEnum(defaultValue = DnsMode.CLOUDFLARE)
        val customUrl = context.dataStore[DnsCustomUrlKey] ?: ""

        activeDnsInstance = createDns(context, enabled, mode, customUrl)
        Log.d(TAG, "DNS updated: enabled=$enabled, mode=$mode, instance=$activeDnsInstance")
        // Applied to app-owned OkHttp clients (e.g. YTPlayerUtils) via getActiveDns().
        // The Metrolist innertube module no longer exposes a global dns hook.
    }

    private fun createDns(
        context: Context,
        enabled: Boolean,
        mode: DnsMode,
        customUrl: String
    ): Dns {
        if (!enabled || mode == DnsMode.OFF) return Dns.SYSTEM

        val dohUrlString = when (mode) {
            DnsMode.CLOUDFLARE -> "https://cloudflare-dns.com/dns-query"
            DnsMode.ADGUARD -> "https://dns.adguard-dns.com/dns-query"
            DnsMode.GOOGLE -> "https://dns.google/dns-query"
            DnsMode.OPENDNS -> "https://doh.opendns.com/dns-query"
            DnsMode.CUSTOM -> customUrl.trim().ifBlank { "https://cloudflare-dns.com/dns-query" }
            DnsMode.OFF -> return Dns.SYSTEM
        }

        val dohUrl = dohUrlString.toHttpUrlOrNull() ?: return Dns.SYSTEM

        val bootstrapIps = when (mode) {
            DnsMode.CLOUDFLARE -> listOf(
                InetAddress.getByName("1.1.1.1"),
                InetAddress.getByName("1.0.0.1"),
                InetAddress.getByName("2606:4700:4700::1111"),
                InetAddress.getByName("2606:4700:4700::1001")
            )
            DnsMode.ADGUARD -> listOf(
                InetAddress.getByName("94.140.14.14"),
                InetAddress.getByName("94.140.15.15")
            )
            DnsMode.GOOGLE -> listOf(
                InetAddress.getByName("8.8.8.8"),
                InetAddress.getByName("8.8.4.4")
            )
            DnsMode.OPENDNS -> listOf(
                InetAddress.getByName("208.67.222.222"),
                InetAddress.getByName("208.67.220.220")
            )
            DnsMode.CUSTOM, DnsMode.OFF -> emptyList()
        }

        return try {
            val cacheDir = File(context.cacheDir, "doh_cache").apply { mkdirs() }
            val builder = DnsOverHttps.Builder()
                .client(bootstrapClient)
                .url(dohUrl)

            if (bootstrapIps.isNotEmpty()) {
                builder.bootstrapDnsHosts(bootstrapIps)
            }

            builder.build()
        } catch (e: Exception) {
            Log.e(TAG, "Error building DnsOverHttps: ${e.message}")
            Dns.SYSTEM
        }
    }

    /**
     * Tests resolving a domain using the configured Dns instance and measures latency in milliseconds.
     */
    suspend fun testDnsResolution(
        context: Context,
        enabled: Boolean,
        mode: DnsMode,
        customUrl: String,
        testHostname: String = "music.youtube.com"
    ): Result<Long> = withContext(Dispatchers.IO) {
        val testDns = createDns(context, enabled, mode, customUrl)
        val startTime = System.currentTimeMillis()
        runCatching {
            val addresses = testDns.lookup(testHostname)
            if (addresses.isEmpty()) {
                throw Exception("No IP addresses returned for $testHostname")
            }
            System.currentTimeMillis() - startTime
        }
    }
}
