package com.dd3boh.outertune.ui.screens.settings.fragments

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DnsCustomUrlKey
import com.dd3boh.outertune.constants.DnsEnabledKey
import com.dd3boh.outertune.constants.DnsMode
import com.dd3boh.outertune.constants.DnsModeKey
import com.dd3boh.outertune.constants.ProxyEnabledKey
import com.dd3boh.outertune.constants.ProxyTypeKey
import com.dd3boh.outertune.constants.ProxyUrlKey
import com.dd3boh.outertune.extensions.toEnum
import com.dd3boh.outertune.ui.component.EditTextPreference
import com.dd3boh.outertune.ui.component.ListPreference
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.utils.DnsHelper
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.launch
import java.net.Proxy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NetworkFrag() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val (dnsEnabled, onDnsEnabledChange) = rememberPreference(key = DnsEnabledKey, defaultValue = false)
    val (dnsMode, onDnsModeChange) = rememberEnumPreference(key = DnsModeKey, defaultValue = DnsMode.CLOUDFLARE)
    val (dnsCustomUrl, onDnsCustomUrlChange) = rememberPreference(key = DnsCustomUrlKey, defaultValue = "https://cloudflare-dns.com/dns-query")

    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")

    var isTestingDns by remember { mutableStateOf(false) }
    var dnsTestResult by remember { mutableStateOf<String?>(null) }
    var dnsTestSuccess by remember { mutableStateOf<Boolean?>(null) }

    fun updateDnsState(newEnabled: Boolean, newMode: DnsMode, newCustomUrl: String = dnsCustomUrl) {
        onDnsEnabledChange(newEnabled)
        onDnsModeChange(newMode)
        if (newCustomUrl != dnsCustomUrl) {
            onDnsCustomUrlChange(newCustomUrl)
        }
        DnsHelper.applyDns(context)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Material 3 Expressive Active Status Banner
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (dnsEnabled && dnsMode != DnsMode.OFF)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (dnsEnabled) Icons.Rounded.Public else Icons.Rounded.Router,
                        contentDescription = null,
                        tint = if (dnsEnabled && dnsMode != DnsMode.OFF)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (dnsEnabled && dnsMode != DnsMode.OFF)
                            "DNS over HTTPS Active"
                        else
                            "System DNS (Default)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            !dnsEnabled || dnsMode == DnsMode.OFF -> "Queries use system network resolver"
                            dnsMode == DnsMode.CLOUDFLARE -> "Cloudflare 1.1.1.1 DoH"
                            dnsMode == DnsMode.ADGUARD -> "AdGuard Ad-Blocking DoH"
                            dnsMode == DnsMode.GOOGLE -> "Google Public DoH"
                            dnsMode == DnsMode.OPENDNS -> "OpenDNS DoH"
                            dnsMode == DnsMode.CUSTOM -> "Custom DoH Endpoint"
                            else -> "Encrypted DoH Active"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        PreferenceGroupTitle(
            title = stringResource(R.string.dns_over_https_title)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column {
                SwitchPreference(
                    title = { Text(stringResource(R.string.dns_over_https_title)) },
                    description = stringResource(R.string.dns_over_https_description),
                    icon = { Icon(Icons.Rounded.Dns, null) },
                    checked = dnsEnabled,
                    onCheckedChange = { enabled ->
                        updateDnsState(enabled, if (enabled && dnsMode == DnsMode.OFF) DnsMode.CLOUDFLARE else dnsMode)
                    }
                )

                AnimatedVisibility(
                    visible = dnsEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dns_provider_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // M3 Expressive DNS Provider Selector Chips
                        val providers = listOf(
                            DnsMode.CLOUDFLARE to stringResource(R.string.dns_provider_cloudflare),
                            DnsMode.ADGUARD to stringResource(R.string.dns_provider_adguard),
                            DnsMode.GOOGLE to stringResource(R.string.dns_provider_google),
                            DnsMode.OPENDNS to stringResource(R.string.dns_provider_opendns),
                            DnsMode.CUSTOM to stringResource(R.string.dns_provider_custom)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            providers.forEach { (mode, label) ->
                                val isSelected = dnsMode == mode
                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            updateDnsState(true, mode)
                                        },
                                    shape = CircleShape,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Custom DoH URL Input Field
                        AnimatedVisibility(
                            visible = dnsMode == DnsMode.CUSTOM,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = dnsCustomUrl,
                                    onValueChange = { newUrl ->
                                        updateDnsState(true, DnsMode.CUSTOM, newUrl)
                                    },
                                    label = { Text(stringResource(R.string.dns_custom_url_title)) },
                                    placeholder = { Text(stringResource(R.string.dns_custom_url_placeholder)) },
                                    leadingIcon = { Icon(Icons.Rounded.Link, null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Test DNS Connection Button & Result
                        Button(
                            onClick = {
                                isTestingDns = true
                                dnsTestResult = null
                                dnsTestSuccess = null
                                coroutineScope.launch {
                                    val res = DnsHelper.testDnsResolution(context, true, dnsMode, dnsCustomUrl)
                                    isTestingDns = false
                                    res.onSuccess { ms ->
                                        dnsTestSuccess = true
                                        dnsTestResult = context.getString(R.string.dns_test_success, ms)
                                    }.onFailure { err ->
                                        dnsTestSuccess = false
                                        dnsTestResult = context.getString(R.string.dns_test_failed, err.message ?: "Unknown error")
                                    }
                                }
                            },
                            enabled = !isTestingDns,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isTestingDns) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.dns_test_testing))
                            } else {
                                Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.dns_test_connection))
                            }
                        }

                        dnsTestResult?.let { result ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (dnsTestSuccess == true)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (dnsTestSuccess == true) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (dnsTestSuccess == true)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (dnsTestSuccess == true)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Proxy Settings Card
        PreferenceGroupTitle(
            title = stringResource(R.string.grp_proxy)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column {
                SwitchPreference(
                    title = { Text(stringResource(R.string.enable_proxy)) },
                    checked = proxyEnabled,
                    onCheckedChange = onProxyEnabledChange
                )

                AnimatedVisibility(proxyEnabled) {
                    Column {
                        ListPreference(
                            title = { Text(stringResource(R.string.proxy_type)) },
                            selectedValue = proxyType,
                            values = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
                            valueText = { it.name },
                            onValueSelected = onProxyTypeChange
                        )
                        EditTextPreference(
                            title = { Text(stringResource(R.string.proxy_url)) },
                            value = proxyUrl,
                            onValueChange = onProxyUrlChange
                        )
                    }
                }
            }
        }
    }
}
