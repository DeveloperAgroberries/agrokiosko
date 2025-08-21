package com.agroberriesmx.agrokiosko.ui.options

sealed class OptionsState {
    data object Empty: OptionsState()
    data object Loading:OptionsState()
    data class Error(val error: String):OptionsState()
    data class SuccessSharedLogs(val success: String):OptionsState()
    data class SuccessPayroll(val totalsPayroll: OptionsViewModel.PayrollWithWorkerInfo):OptionsState()
    data class SuccessActivities(val activitiesData: OptionsViewModel.ActivitiesWithWorkerInfo):OptionsState()
}