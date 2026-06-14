package com.kjipo.timetracker.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.R
import com.kjipo.timetracker.database.AppDatabase
import kotlinx.coroutines.launch

enum class BackupRestoreState {
    Idle,
    WaitingForConfirmation,
    Restoring,
    Success,
    Error
}

@Composable
fun BackupRestoreScreen(database: AppDatabase, onGoToTasks: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var restoreState by remember { mutableStateOf(BackupRestoreState.Idle) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val importer = remember { BackupImporter(database, context) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            restoreState = BackupRestoreState.WaitingForConfirmation
        }
    }

    if (restoreState == BackupRestoreState.WaitingForConfirmation) {
        AlertDialog(
            onDismissRequest = { restoreState = BackupRestoreState.Idle },
            title = { Text(stringResource(R.string.restore_confirmation_title)) },
            text = { Text(stringResource(R.string.restore_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = {
                    val uri = selectedUri
                    if (uri != null) {
                        restoreState = BackupRestoreState.Restoring
                        scope.launch {
                            try {
                                importer.restoreFromUri(uri)
                                restoreState = BackupRestoreState.Success
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Unknown error"
                                restoreState = BackupRestoreState.Error
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreState = BackupRestoreState.Idle }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.restore_backup_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.restore_backup_description),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (restoreState) {
            BackupRestoreState.Idle, BackupRestoreState.WaitingForConfirmation -> {
                Button(onClick = {
                    openDocumentLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                }) {
                    Text(stringResource(R.string.select_backup_file))
                }
            }
            BackupRestoreState.Restoring -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.restoring))
            }
            BackupRestoreState.Success -> {
                Text(
                    text = stringResource(R.string.restore_success),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoToTasks) {
                    Text(stringResource(R.string.go_to_tasks))
                }
            }
            BackupRestoreState.Error -> {
                Text(
                    text = stringResource(R.string.restore_error, errorMessage),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { restoreState = BackupRestoreState.Idle }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
