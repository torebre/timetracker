package com.kjipo.timetracker.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.kjipo.timetracker.R
import java.io.File


class ExportTimeEntryDataFileProvider: FileProvider(R.xml.export_files_path)

fun exportData(file: File, context: Context) {
    val fileUri = FileProvider.getUriForFile(context, "com.kjipo.timetracker", file)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, fileUri)
        type = "text/csv"
    }
    context.startActivity(shareIntent)
}

