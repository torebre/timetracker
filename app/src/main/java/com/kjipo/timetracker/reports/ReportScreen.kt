package com.kjipo.timetracker.reports

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import java.io.File


class ReportScreenInput(uiState: ReportsModel.ReportsUiState)


class ReportScreenInputParameterProvider: PreviewParameterProvider<ReportScreenInput> {
    override val values = sequenceOf(ReportScreenInput(ReportsModel.ReportsUiState()))
}


@Composable
fun ReportScreen(reportsModel: ReportsModel) {
    val uiState = reportsModel.uiState.collectAsState()

    ReportScreen(ReportScreenInput(uiState.value))

}

@Preview(showBackground = true)
@Composable
fun ReportScreen(@PreviewParameter(PreviewParameterProvider::class) reportScreenInput: ReportScreenInput) {
    val context = LocalContext.current

    // TODO Get file to export to

    // TODO Just here for testing
    CalendarComponent(calendarUiState = CalendarUiState())

    val fileToExportTo = File(context.cacheDir, "export_file_temp.zip")
    Button(onClick = { exportData(fileToExportTo, context) }) {
        Text("Export")
    }

}

