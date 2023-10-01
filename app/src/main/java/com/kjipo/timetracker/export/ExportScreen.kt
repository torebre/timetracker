package com.kjipo.timetracker.export

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File


@Composable
fun ExportScreen() {
    val context = LocalContext.current

    val fileToExportTo = File(context.cacheDir.resolve("exportFiles"), "export_file_temp.zip")
    Button(onClick = { exportData(fileToExportTo, context) }) {
        Text("Export")
    }

}