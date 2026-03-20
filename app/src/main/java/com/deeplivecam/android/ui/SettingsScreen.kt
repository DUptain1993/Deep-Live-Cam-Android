package com.deeplivecam.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.deeplivecam.android.data.SettingsRepository
import com.deeplivecam.android.utils.Constants
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository.getInstance(context) }
    val settings by settingsRepository.settingsFlow.collectAsState(initial = SettingsRepository.AppSettings())
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quality Settings Section
            Text(
                "Quality Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Resolution selector
                    Text("Resolution", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResolutionChip("480p", settings.resolution) { scope.launch { settingsRepository.setResolution(it) } }
                        ResolutionChip("720p", settings.resolution) { scope.launch { settingsRepository.setResolution(it) } }
                        ResolutionChip("1080p", settings.resolution) { scope.launch { settingsRepository.setResolution(it) } }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Target FPS", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FpsChip("15", settings.targetFps) { scope.launch { settingsRepository.setTargetFps(it) } }
                        FpsChip("24", settings.targetFps) { scope.launch { settingsRepository.setTargetFps(it) } }
                        FpsChip("30", settings.targetFps) { scope.launch { settingsRepository.setTargetFps(it) } }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Output quality slider
                    Text("Output Quality: ${(settings.outputQuality * 100).toInt()}%", 
                        style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = settings.outputQuality,
                        onValueChange = { 
                            scope.launch { settingsRepository.setOutputQuality(it) }
                        },
                        valueRange = 0.5f..1.0f
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Performance Settings Section
            Text(
                "Performance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // GPU Acceleration
                    SettingSwitch(
                        title = "GPU Acceleration",
                        description = "Use GPU for faster processing (requires supported device)",
                        checked = settings.executionProvider == Constants.EXEC_GPU,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                settingsRepository.setExecutionProvider(
                                    if (enabled) Constants.EXEC_GPU else Constants.EXEC_NNAPI
                                )
                            }
                        }
                    )
                    
                    Divider()
                    
                    // Face detection sensitivity
                    Text("Face Detection Sensitivity", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Currently using default ML Kit sensitivity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Features Section
            Text(
                "Features",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Face Enhancement
                    SettingSwitch(
                        title = "Face Enhancement",
                        description = "Enhance face quality (may slow down processing)",
                        checked = settings.faceEnhancement,
                        onCheckedChange = { scope.launch { settingsRepository.setFaceEnhancement(it) } }
                    )
                    
                    Divider()
                    
                    // Many Faces Mode
                    SettingSwitch(
                        title = "Many Faces Mode",
                        description = "Swap multiple faces in one frame",
                        checked = settings.manyFaces,
                        onCheckedChange = { scope.launch { settingsRepository.setManyFaces(it) } }
                    )
                    
                    Divider()
                    
                    // Mouth Mask
                    SettingSwitch(
                        title = "Mouth Mask",
                        description = "Preserve mouth region for better lip sync",
                        checked = settings.mouthMask,
                        onCheckedChange = { scope.launch { settingsRepository.setMouthMask(it) } }
                    )
                    
                    Divider()
                    
                    // Mirror Camera
                    SettingSwitch(
                        title = "Mirror Camera",
                        description = "Flip camera preview horizontally",
                        checked = settings.mirrorCamera,
                        onCheckedChange = { scope.launch { settingsRepository.setMirrorCamera(it) } }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Safety Section
            Text(
                "Safety",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingSwitch(
                        title = "NSFW Filter (placeholder)",
                        description = "Not yet functional \u2014 a classifier model must be added",
                        checked = settings.nsfwFilter,
                        onCheckedChange = { scope.launch { settingsRepository.setNsfwFilter(it) } }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reset button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        settingsRepository.resetToDefaults()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun RowScope.ResolutionChip(
    resolution: String,
    selectedResolution: String,
    onSelect: (String) -> Unit
) {
    FilterChip(
        selected = selectedResolution == resolution,
        onClick = { onSelect(resolution) },
        label = { Text(resolution) },
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun RowScope.FpsChip(
    fps: String,
    selectedFps: String,
    onSelect: (String) -> Unit
) {
    FilterChip(
        selected = selectedFps == fps,
        onClick = { onSelect(fps) },
        label = { Text("${fps} FPS") },
        modifier = Modifier.weight(1f)
    )
}
