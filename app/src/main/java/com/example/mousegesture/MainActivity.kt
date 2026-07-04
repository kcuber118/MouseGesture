package com.example.mousegesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mousegesture.data.DataStorePreferencesRepository
import com.example.mousegesture.domain.preferences.PreferencesRepository
import com.example.mousegesture.service.MouseGestureAccessibilityService
import com.example.mousegesture.ui.theme.MouseGestureTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MouseGestureTheme {
                val appContext = (this as ComponentActivity).applicationContext
                val prefsRepo = remember {
                    DataStorePreferencesRepository(appContext)
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnboardingScreen(
                        modifier = Modifier.padding(innerPadding),
                        prefsRepo = prefsRepo,
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    prefsRepo: PreferencesRepository,
) {
    val context = LocalContext.current
    var isServiceEnabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Check service state on launch
    LaunchedEffect(Unit) {
        isServiceEnabled = isAccessibilityServiceEnabled(
            context,
            MouseGestureAccessibilityService::class.java,
        )
    }

    // Observe preferences for sensitivity slider
    val prefs by prefsRepo.preferencesFlow().collectAsState(
        initial = com.example.mousegesture.domain.preferences.UserPreferences(),
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Mouse Gesture",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isServiceEnabled) {
            Text(
                text = stringResource(R.string.service_enabled),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Overlay visibility state. Per ADR-0002.
            val overlayVisible = MouseGestureAccessibilityService.isOverlayVisible
            when (overlayVisible) {
                true -> Text(
                    text = "Overlay đang bật",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                false -> {
                    Text(
                        text = "Overlay đang tắt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        MouseGestureAccessibilityService.showOverlay()
                    }) {
                        Text(text = "Bật overlay")
                    }
                }
                null -> Text(
                    text = "Đang khởi động…",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.service_disabled),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }) {
                Text(text = stringResource(R.string.open_accessibility_settings))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sensitivity slider
        Text(
            text = "Độ nhạy (Sensitivity)",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "0.5", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = prefs.sensitivity,
                onValueChange = { newSensitivity ->
                    scope.launch {
                        prefsRepo.savePreferences(prefs.withSensitivity(newSensitivity))
                    }
                },
                valueRange = 0.5f..3.0f,
                modifier = Modifier.weight(1f),
            )
            Text(text = "3.0", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = "Hiện tại: ${"%.1f".format(prefs.sensitivity)}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Check if the given accessibility service is enabled by querying the system.
 * This is the reliable approach — the static [MouseGestureAccessibilityService.isRunning]
 * flag can be wrong if the process was killed.
 */
fun isAccessibilityServiceEnabled(
    context: Context,
    serviceClass: Class<out AccessibilityService>,
): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
    )
    return enabledServices.any { info ->
        val resolvedInfo = info.resolveInfo.serviceInfo
        resolvedInfo.packageName == context.packageName &&
            resolvedInfo.name == serviceClass.name
    }
}
