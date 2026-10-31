package com.powerk.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PowerkApp() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var snapshot by remember { mutableStateOf<Snapshot?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var commandFailed by remember { mutableStateOf(false) }
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val pending = remember { mutableStateMapOf<String, Boolean>() }

    suspend fun refresh() {
        if (!Server.configured || loading) return
        loading = true
        try {
            snapshot = Api.snapshot(Server.base, Server.token)
            error = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Retain the last snapshot, explicitly stale, with commands disabled.
            error = e.message ?: e.javaClass.simpleName
        } finally { loading = false }
    }
    LaunchedEffect(lifecycle, Server.host, Server.port, Server.token, Server.secure) {
        snapshot = null
        error = null
        feedback = null
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { refresh(); delay(2000) }
        }
    }
    fun command(mac: String, outlet: Int, on: Boolean) {
        if (error != null || snapshot?.strips?.none { it.mac == mac && it.online } != false) return
        val keys = if (outlet == 0) (1..4).map { "$mac:$it" } else listOf("$mac:$outlet")
        if (keys.any { pending.containsKey(it) }) return
        keys.forEach { pending[it] = on }
        feedback = resources.getString(R.string.command_pending)
        commandFailed = false
        scope.launch {
            try {
                Api.setOutlet(Server.base, Server.token, mac, outlet, on)
                feedback = resources.getString(R.string.command_confirmed)
                refresh()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                commandFailed = true
                feedback = resources.getString(R.string.command_error_detail, e.message ?: resources.getString(R.string.command_failed))
            } finally { keys.forEach { pending.remove(it) } }
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, null) }, label = { Text(stringResource(R.string.tab_plugs)) })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Settings, null) }, label = { Text(stringResource(R.string.tab_settings)) })
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (tab == 0) PlugsScreen(snapshot, error, loading, pending, feedback, commandFailed,
                onRefresh = { scope.launch { refresh() } }, onOpenSettings = { tab = 1 },
                onCommand = { mac, outlet, on -> command(mac, outlet, on) })
            else SettingsScreen()
        }
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(Space.xs / 4, MaterialTheme.colorScheme.outlineVariant)) {
        content()
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
}

@Composable
private fun PlugsScreen(
    snapshot: Snapshot?, error: String?, loading: Boolean, pending: Map<String, Boolean>,
    feedback: String?, commandFailed: Boolean, onRefresh: () -> Unit, onOpenSettings: () -> Unit,
    onCommand: (String, Int, Boolean) -> Unit,
) {
    val strips = snapshot?.strips.orEmpty()
    val healthy = error == null && snapshot != null
    val live = liveSummary(strips, healthy)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.power_overview), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
                    Text(stringResource(R.string.dashboard_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRefresh, enabled = Server.configured && !loading) {
                    Icon(Icons.Filled.Refresh, stringResource(R.string.refresh))
                }
            }
        }
        if (error != null) item { MessageCard(stringResource(R.string.connection_stale, error), true) }
        if (feedback != null) item { MessageCard(feedback, commandFailed) }
        if (!Server.configured) item {
            EmptyState(stringResource(R.string.no_server_title), stringResource(R.string.no_server_body), stringResource(R.string.open_settings), onOpenSettings)
        } else {
            item {
                Panel {
                    Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
                        Text(stringResource(R.string.ac_power), style = MaterialTheme.typography.titleSmall)
                        Text("${measurement(live.powerW)} W",
                            style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.live_strip_count, live.reporting), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(stringResource(R.string.active_outlets, live.active, live.outlets), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.your_strips)) }
            items(strips, key = { it.mac }) { strip -> StripCard(strip, healthy, pending, onCommand) }
            if (snapshot != null && strips.isEmpty()) item { EmptyState(stringResource(R.string.no_strip_title), stringResource(R.string.no_strip_body)) }
            if (snapshot == null && error == null) item { MessageCard(stringResource(R.string.connecting, Server.host), false) }
            item {
                val context = LocalContext.current
                OutlinedButton(onClick = {
                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(Server.base + "/#history-title")))
                }, modifier = Modifier.fillMaxWidth().heightIn(min = Space.target)) { Text(stringResource(R.string.open_history)) }
            }
        }
    }
}

@Composable
private fun StripCard(strip: Strip, healthy: Boolean, pending: Map<String, Boolean>, onCommand: (String, Int, Boolean) -> Unit) {
    val enabled = healthy && strip.online
    val anyPending = (1..4).any { pending.containsKey("${strip.mac}:$it") }
    var details by rememberSaveable(strip.mac) { mutableStateOf(false) }
    Panel {
        Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                SectionTitle(strip.name.ifBlank { stringResource(R.string.strip_name) })
                Text("${strip.model} · ${stringResource(R.string.strip_fw, strip.fw)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(if (enabled) R.string.online else R.string.offline_stale), style = MaterialTheme.typography.labelLarge)
            }
            Text("${measurement(strip.powerW)} W", style = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"))
            Text(stringResource(if (enabled) R.string.live_ac_readings else R.string.stale_ac_readings), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Metric(stringResource(R.string.ac_energy), "${measurement(strip.energyKwh, 3)} kWh")
            Metric(stringResource(R.string.voltage), "${measurement(strip.voltage)} V")
            Metric(stringResource(R.string.current), "${measurement(strip.currentA)} A")
            Metric(stringResource(R.string.wifi), "${strip.rssi ?: "—"} dBm")
            // Explicit bulk actions never derive a command from mixed outlet state.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                OutlinedButton(onClick = { onCommand(strip.mac, 0, true) }, enabled = enabled && !anyPending, modifier = Modifier.heightIn(min = Space.target)) { Text(stringResource(R.string.turn_all_on)) }
                OutlinedButton(onClick = { onCommand(strip.mac, 0, false) }, enabled = enabled && !anyPending, modifier = Modifier.heightIn(min = Space.target)) { Text(stringResource(R.string.turn_all_off)) }
            }
            BoxWithConstraints {
                // At large font scales use one column to preserve labels and switch targets.
                val columns = if (maxWidth >= Space.section * 9 && androidx.compose.ui.platform.LocalDensity.current.fontScale <= 1.2f) 2 else 1
                Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                    strip.outlets.sortedBy { it.n }.chunked(columns).forEach { outlets ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                            outlets.forEach { outlet ->
                                key(outlet.n) { OutletTile(outlet, pending["${strip.mac}:${outlet.n}"], enabled,
                                    { onCommand(strip.mac, outlet.n, it) }, Modifier.weight(1f)) }
                            }
                            if (outlets.size < columns) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            if (strip.model.equals("lgutap", ignoreCase = true) && strip.fw == "0.1.54-1.0.66") {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(Modifier.padding(Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                        Text(stringResource(R.string.usb_title), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.usb_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            OutlinedButton(onClick = { details = !details }, modifier = Modifier.heightIn(min = Space.target),
                content = { Text(stringResource(if (details) R.string.hide_details else R.string.device_details)) })
            if (details) Text(strip.mac, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
    }
}

@Composable
private fun OutletTile(outlet: Outlet, target: Boolean?, enabled: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier) {
    val pending = target != null
    val label = stringResource(R.string.switch_outlet, outlet.n)
    val state = stringResource(when {
        pending -> R.string.command_pending
        !enabled -> R.string.offline_stale
        outlet.on -> R.string.state_on
        else -> R.string.state_off
    })
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Space.xs / 4, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            Text(stringResource(R.string.outlet_n, outlet.n), style = MaterialTheme.typography.titleSmall)
            Switch(checked = target ?: outlet.on, onCheckedChange = onToggle, enabled = enabled && !pending,
                modifier = Modifier.heightIn(min = Space.target).semantics { contentDescription = label; stateDescription = state },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                ))
            Text(state, style = MaterialTheme.typography.bodySmall, color = if (enabled && outlet.on && !pending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${measurement(outlet.powerW)} W", style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"))
            Text("${measurement(outlet.energyKwh, 3)} kWh", style = MaterialTheme.typography.bodySmall)
            Text("${outlet.tempC} °C", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MessageCard(text: String, isError: Boolean) {
    Surface(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, shape = MaterialTheme.shapes.medium,
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface) {
        Text(text, Modifier.padding(Space.lg), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyState(title: String, body: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Panel {
        Column(Modifier.padding(Space.xxl), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
            Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            SectionTitle(title)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action != null && onAction != null) OutlinedButton(onClick = onAction, modifier = Modifier.heightIn(min = Space.target)) { Text(action) }
        }
    }
}
@Composable
private fun SettingsScreen() {
    var host by remember { mutableStateOf(Server.host) }
    var port by remember { mutableStateOf(Server.port.toString()) }
    var token by remember { mutableStateOf(Server.token) }
    var secure by remember { mutableStateOf(Server.secure) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var lang by remember { mutableStateOf(Lang.code) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current

    fun commit() = Server.save(host, port.toIntOrNull() ?: 8080, token, secure)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.lg),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
    ) {
        Text(
            stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Panel {
            Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
                Text(
                    stringResource(R.string.language_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val options = listOf(
                    null to stringResource(R.string.lang_system),
                    "en" to stringResource(R.string.lang_english),
                    "ar" to stringResource(R.string.lang_arabic),
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = lang == value,
                            onClick = {
                                if (lang != value) {
                                    lang = value
                                    Lang.set(context, value)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        ) { Text(label) }
                    }
                }
            }
        }

        Panel {
            Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
                Text(
                    stringResource(R.string.server_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.server_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // technical values stay left-to-right even in RTL locales
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text(stringResource(R.string.field_host)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it.filter(Char::isDigit).take(5) },
                            label = { Text(stringResource(R.string.field_port)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = token,
                            onValueChange = { token = it },
                            label = { Text(stringResource(R.string.field_token)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.weight(1.4f),
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.use_https), Modifier.weight(1f))
                    Switch(checked = secure, onCheckedChange = { secure = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    Button(
                        onClick = {
                            commit()
                            status = resources.getString(R.string.saved)
                            statusOk = true
                        },
                        modifier = Modifier.weight(1f).heightIn(min = Space.target),
                    ) { Text(stringResource(R.string.save)) }
                    OutlinedButton(
                        enabled = !busy,
                        onClick = {
                            commit()
                            busy = true
                            status = null
                            scope.launch {
                                try {
                                    val snap = Api.snapshot(Server.base, Server.token)
                                    status = resources.getString(
                                        R.string.connected_summary,
                                        snap.strips.size,
                                        snap.strips.count { it.online },
                                    )
                                    statusOk = true
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    status = e.message ?: "connection failed"
                                    statusOk = false
                                }
                                busy = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.test)) }
                }
                if (busy) Text(stringResource(R.string.testing), style = MaterialTheme.typography.bodySmall)
                if (status != null) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (statusOk) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (statusOk) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(status ?: "", Modifier.padding(Space.md), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedButton(
                    onClick = {
                        commit()
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(Server.base),
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = Server.configured,
                ) { Text(stringResource(R.string.open_web_ui)) }
            }
        }

        Panel {
            Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Text(
                    stringResource(R.string.setup_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                SetupStep(1, stringResource(R.string.setup_step_1))
                SetupStep(2, stringResource(R.string.setup_step_2))
                SetupStep(3, stringResource(R.string.setup_step_3))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        "python powerk.py provision --ip <ip> --ssid WIFI --password PW",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = Space.section),
                    )
                }
                SetupStep(4, stringResource(R.string.setup_step_4))
                HorizontalDivider(Modifier.padding(vertical = Space.xs))
                Text(
                    stringResource(R.string.setup_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SetupStep(number: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(Space.xxl),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("$number", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}
