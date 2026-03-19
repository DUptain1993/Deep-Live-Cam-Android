package com.deeplivecam.android.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var mediaFiles by remember { mutableStateOf<List<MediaFile>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<MediaFile?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Load media files
    LaunchedEffect(Unit) {
        mediaFiles = loadMediaFiles(context)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Output Gallery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            mediaFiles = loadMediaFiles(context)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                mediaFiles.isEmpty() -> {
                    EmptyGalleryView()
                }
                else -> {
                    GalleryGrid(
                        mediaFiles = mediaFiles,
                        onFileClick = { selectedFile = it },
                        onFileLongClick = {
                            selectedFile = it
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && selectedFile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null) },
            title = { Text("Delete File?") },
            text = { Text("Are you sure you want to delete ${selectedFile!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            selectedFile?.file?.delete()
                            mediaFiles = loadMediaFiles(context)
                            showDeleteDialog = false
                            selectedFile = null
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // File viewer dialog
    if (selectedFile != null && !showDeleteDialog) {
        FileViewerDialog(
            mediaFile = selectedFile!!,
            onDismiss = { selectedFile = null },
            onShare = {
                shareFile(context, selectedFile!!.file)
                selectedFile = null
            },
            onDelete = {
                showDeleteDialog = true
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryGrid(
    mediaFiles: List<MediaFile>,
    onFileClick: (MediaFile) -> Unit,
    onFileLongClick: (MediaFile) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(mediaFiles) { mediaFile ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .combinedClickable(
                        onClick = { onFileClick(mediaFile) },
                        onLongClick = { onFileLongClick(mediaFile) }
                    )
            ) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter(mediaFile.file),
                        contentDescription = mediaFile.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (mediaFile.isVideo) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = "Video",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGalleryView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No processed files yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Process images or videos to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FileViewerDialog(
    mediaFile: MediaFile,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (mediaFile.isVideo) Icons.Default.VideoFile else Icons.Default.Image,
                null
            )
        },
        title = { Text(mediaFile.name) },
        text = {
            Column {
                Text("Size: ${formatFileSize(mediaFile.file.length())}")
                Text("Date: ${formatDate(mediaFile.file.lastModified())}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, "Share")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private suspend fun loadMediaFiles(context: Context): List<MediaFile> = withContext(Dispatchers.IO) {
    val outputDir = File(context.getExternalFilesDir(null), "DeepLiveCam")
    if (!outputDir.exists()) return@withContext emptyList()
    
    outputDir.listFiles()
        ?.filter { it.isFile && (it.extension in listOf("jpg", "jpeg", "png", "mp4")) }
        ?.sortedByDescending { it.lastModified() }
        ?.map { file ->
            MediaFile(
                file = file,
                name = file.name,
                isVideo = file.extension == "mp4"
            )
        } ?: emptyList()
}

private fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = if (file.extension == "mp4") "video/*" else "image/*"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share File"))
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

data class MediaFile(
    val file: File,
    val name: String,
    val isVideo: Boolean
)
