package com.kjipo.timetracker.reports

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File


fun exportData(file: File, context: Context) {
    val fileUri = FileProvider.getUriForFile(context, "com.kjipo.timetracker", file)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, fileUri)
        type = "text/csv"
    }
    context.startActivity(shareIntent)
}

