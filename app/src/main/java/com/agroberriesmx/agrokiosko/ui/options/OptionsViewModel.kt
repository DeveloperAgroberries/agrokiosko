package com.agroberriesmx.agrokiosko.ui.options

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroberriesmx.agrokiosko.data.logger.LogUtil
import com.agroberriesmx.agrokiosko.domain.usecase.GetActivitiesUseCase
import com.agroberriesmx.agrokiosko.domain.usecase.GetPayrollUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class OptionsViewModel @Inject constructor(
    private val getPayrollUseCase: GetPayrollUseCase,
    private val getActivitiesUseCase: GetActivitiesUseCase
) : ViewModel() {
    @Inject lateinit var logUtil: LogUtil
    private var _state = MutableStateFlow<OptionsState>(OptionsState.Empty)
    val state: StateFlow<OptionsState> = _state

    data class PayrollWithWorkerInfo(
        val fullName: String,
        val workerCode: String,
        val weekCode: String,
        val email: String,
        val fieldName: String,
        val totals: Map<String, Double>
    )

    data class ActivitiesWithWorkerInfo(
        val ticketContent: StringBuilder,
        val weekTotal: Double,
        val workerCode: String,
        val fullName: String,
        val email: String
    )

    fun getPayroll(worker: String) {
        viewModelScope.launch {
            try{
                _state.value = OptionsState.Loading
                val result = withContext(Dispatchers.IO) { getPayrollUseCase(worker) }
                if (result != null) {
                    val totals = mutableMapOf<String, Double>()
                    totals["NOR"] = 0.0
                    totals["RET"] = 0.0
                    totals["OPE"] = 0.0
                    totals["EXT"] = 0.0
                    totals["AGI"] = 0.0

                    for (item in result) {
                        val tipo = item.cTipoLab
                        if (totals.containsKey(tipo)) {
                            totals[tipo] = totals[tipo]!! + item.nSueldoLab.toDouble()
                        }
                    }

                    val total = totals["NOR"]!! + totals["OPE"]!! + totals["EXT"]!! + totals["AGI"]!! - totals["RET"]!!
                    totals["TOTAL"] = total

                    val fullName = result[0].vNombreTra + " " + result[0].vApellidopatTra + " " + result[0].vApellidomatTra
                    val workerCode = result[0].cCodigoTra
                    val weekCode = result[0].cCodigoSem
                    val workerEmail = result[0].vEmailTra
                    val fieldName = result[0].vNombreCam
                    val payrollWithWorkerInfo = PayrollWithWorkerInfo(fullName, workerCode, weekCode, workerEmail, fieldName, totals)
                    _state.value = OptionsState.SuccessPayroll(payrollWithWorkerInfo)
                    totals.clear()
                } else {
                    _state.value = OptionsState.Error("Ha ocurrido un error, vuelve a intentarlo")
                }
            } catch (e: Exception) {
                _state.value = OptionsState.Error("Error al obtener datos: ${e.message}")
            }
        }
    }

    fun getActivities(worker: String) {
        viewModelScope.launch {
            _state.value = OptionsState.Loading
            val result = withContext(Dispatchers.IO) { getActivitiesUseCase(worker) }
            if (result != null) {
                var totalWeek = 0.0
                val ticketContent = StringBuilder()
                val workerCode = result[0].cCodigoTra
                val fullName = result[0].vNombreTra + " " + result[0].vApellidopatTra + " " + result[0].vApellidomatTra
                val workerEmail = result[0].vEmailTra

                val activitiesForDay = result.groupBy { formatDate(it.dDocumentoLab) }

                activitiesForDay.forEach { (date, activities) ->
                    var totalDia = 0.0

                    val activitiesByCode = activities.groupBy { it.cCodigoAct }

                    ticketContent.appendLine("Fecha: $date")

                    activitiesByCode.forEach { (activityCode, activitiesForCode) ->
                        var totalForCode = 0.0
                        activitiesForCode.forEach { activity ->
                            if (activity.cCodigoAct.trim() == "9112") {
                                totalForCode -= activity.nSueldoLab.toDouble()
                            } else {
                                totalForCode += activity.nSueldoLab.toDouble()
                            }
                        }

                        // Asegúrate de que cada línea no exceda los 32 caracteres
                        val activityName = activitiesForCode.first().vNombreAct.trim()
                            .take(16) // Limitar nombre a 16 caracteres
                        val line = "${activityCode.trim()} $activityName ${"%.2f".format(totalForCode)}"

                        // Si la línea es más larga de 32 caracteres, recórtala o divídela
                        val maxLineLength = 32
                        if (line.length > maxLineLength) {
                            val part1 = line.take(maxLineLength)    // Primera parte
                            val part2 = line.drop(maxLineLength)    // Segunda parte (si es necesario)

                            ticketContent.appendLine(part1)
                            ticketContent.appendLine(part2.take(maxLineLength))  // Asegúrate de que la segunda parte no exceda el límite
                        } else {
                            ticketContent.appendLine(line)
                        }

                        totalDia += totalForCode
                    }
                    ticketContent.appendLine("Total del Dia: ${"%.2f".format(totalDia)}")
                    ticketContent.appendLine("------------------------------\n")
                    totalWeek += totalDia;
                }
                val activitiesWithWorkerInfo = ActivitiesWithWorkerInfo(ticketContent, totalWeek, workerCode, fullName, workerEmail)
                _state.value = OptionsState.SuccessActivities(activitiesWithWorkerInfo)
            } else {
                _state.value = OptionsState.Error("Ha ocurrido un error, vuelve a intentarlo")
            }
        }
    }

    fun shareLogs() {
        _state.value = OptionsState.SuccessSharedLogs(logUtil.shareLogFile())
    }

    @SuppressLint("NewApi")
    private fun formatDate(dateString: String): String {
        val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val formatterOutput = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(dateString, formatterInput)
        return date.format(formatterOutput)
    }

    fun resetState() {
        _state.value = OptionsState.Empty
    }
}