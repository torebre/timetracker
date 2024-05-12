package com.kjipo.timetracker.export

import android.content.Context
import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.kjipo.timetracker.R
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.exportDateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


class ExportTimeEntryDataFileProvider : FileProvider(R.xml.export_files_path)

@Composable
fun ExportScreen(taskRepository: TaskRepository) {
    val context = LocalContext.current

    // https://developer.android.com/jetpack/compose/side-effects#remembercoroutinescope
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        doExport(taskRepository, coroutineScope, context)
    }) {
        Text("Export")
    }

}


fun exportData(file: File, context: Context) {
    val fileUri = FileProvider.getUriForFile(context, "com.kjipo.timetracker", file)

    // This is an implicit intent since no component name is given.
    // The system will decide which component should receive the intent
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
//        putExtra(Intent.EXTRA_STREAM, fileUri)
        putExtra(Intent.EXTRA_TEXT, "test")
        type = "text/csv"
    }
    context.startActivity(shareIntent)
}

private fun doExport(
    taskRepository: TaskRepository,
    coroutineScope: CoroutineScope,
    context: Context,
) {
    coroutineScope.launch(Dispatchers.IO) {
        val exportLines = exportAllTasks(taskRepository)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, exportLines)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "export_file_temp_${exportDateFormat.format(LocalDate.now())}.csv"
            )
            type = "text/csv"
        }
        context.startActivity(shareIntent)
    }
}


private suspend fun exportAllTasks(taskRepository: TaskRepository): String {
    val tasks = taskRepository.getTasksWithTimeEntries(null, null)

    val exportLines = tasks.map { taskWithTimeEntries ->
        val projectTitle = taskWithTimeEntries.project?.title ?: ""

        val timeEntryLines = taskWithTimeEntries.timeEntries.map { timeEntry ->
            val duration = timeEntry.getDuration()
            if (duration != null) {
                "${taskWithTimeEntries.task.taskId};${projectTitle};${timeEntry.timeEntryId};${taskWithTimeEntries.task.title};${
                    exportDateFormat.format(
                        LocalDateTime.ofInstant(timeEntry.start, ZoneId.systemDefault())
                    )
                };${duration.toMinutes()}"
            } else {
                null
            }
        }.filterNotNull()

        val timeEntryDayLines = taskWithTimeEntries.timeEntriesDay.map { timeEntry ->
            "${taskWithTimeEntries.task.taskId};${projectTitle};${timeEntry.id};${taskWithTimeEntries.task.title};${
                exportDateFormat.format(
                    timeEntry.date
                )
            };${timeEntry.duration.toMinutes()}"
        }

        timeEntryLines + timeEntryDayLines
    }.flatten()

    return "id;project_title;time_entry_id;date;duration_minutes\n" + exportLines.joinToString("\n")
}
