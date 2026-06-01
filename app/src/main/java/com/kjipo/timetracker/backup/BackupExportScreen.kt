package com.kjipo.timetracker.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.database.AppDatabase
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class BackupExportState {
    Idle,
    Exporting,
    Success,
    Error
}

@Composable
fun BackupExportScreen(database: AppDatabase) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportState by remember { mutableStateOf(BackupExportState.Idle) }
    var errorMessage by remember { mutableStateOf("") }

    val exporter = remember { BackupExporter(database, context) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            exportState = BackupExportState.Exporting
            scope.launch {
                try {
                    exporter.exportToUri(uri)
                    exportState = BackupExportState.Success
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Unknown error"
                    exportState = BackupExportState.Error
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Backup export",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Creates a ZIP file containing all app data as JSON.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (exportState) {
            BackupExportState.Idle -> {
                Button(onClick = {
                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm"))
                    createDocumentLauncher.launch("timetracker-backup-$timestamp.zip")
                }) {
                    Text("Export backup")
                }
            }
            BackupExportState.Exporting -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Exporting...")
            }
            BackupExportState.Success -> {
                Text("Backup exported successfully!", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { exportState = BackupExportState.Idle }) {
                    Text("Done")
                }
            }
            BackupExportState.Error -> {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { exportState = BackupExportState.Idle }) {
                    Text("Try again")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Future imports will be able to restore data from this file.",
            style = MaterialTheme.typography.bodySmall
        )
        // TODO import:
        // - Open ZIP selected through ACTION_OPEN_DOCUMENT
        // - Read backup.json
        // - Validate manifest.formatVersion
        // - Convert DTOs to entities
        // - Replace current data inside database transaction
        // - Insert parent tables before child/cross-reference tables
    }
}
