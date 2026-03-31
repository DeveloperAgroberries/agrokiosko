package com.agroberriesmx.agrokiosko.ui.options

import android.annotation.SuppressLint
import android.util.Log
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
        val email: String,
        val totalSindicato: Double, // Nuevo
        val totalNeto: Double       // Nuevo
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
        Log.d("Pruebas", "Entre a activitis")
        viewModelScope.launch {
            _state.value = OptionsState.Loading
            val result = withContext(Dispatchers.IO) { getActivitiesUseCase(worker) }

            if (result != null) {
                var totalWeek = 0.0
                var totalSindicato = 0.0 // Variable para acumular el descuento real

                val ticketContent = StringBuilder()
                val workerCode = result[0].cCodigoTra
                val fullName = result[0].vNombreTra + " " + result[0].vApellidopatTra + " " + result[0].vApellidomatTra
                val workerEmail = result[0].vEmailTra

                val activitiesForDay = result.groupBy { formatDate(it.dDocumentoLab) }

                activitiesForDay.forEach { (date, activities) ->
                    var totalDia = 0.0
                    var totalCajasGeneralDia = 0.0
                    val desgloseCajasPorTamanoHoy = mutableMapOf<String, Double>()
                    val desglosePagosPorTamanoHoy = mutableMapOf<String, Double>()

                    val activitiesByCode = activities.groupBy { it.cCodigoAct }

                    ticketContent.appendLine("Fecha: $date")

                    activitiesByCode.forEach { (activityCode, activitiesForCode) ->
                        var totalForCode = 0.0

                        activitiesForCode.forEach { activity ->
                            val trimmedActivityCode = activity.cCodigoAct.trim()
                            val sueldo = activity.nSueldoLab.toDouble()

                            // ⭐ CORRECCIÓN: Sumamos lo que manda la API (C# ya validó si es "1" o "0")
                            // No calculamos manual, usamos el valor del objeto activity
                            // ⭐ EVALUACIÓN: Si la API dice que es sindicalizado, sumamos el descuento que mandó el C#
                            if (activity.esSindicalizado == true) {
                                totalSindicato += (activity.descuentoSindicato ?: 0.0)
                            }

                            if (trimmedActivityCode == "9112") {
                                totalForCode -= sueldo
                            } else {
                                totalForCode += sueldo
                            }

                            if (trimmedActivityCode == "0160") {
                                val cantidadCajas = activity.nCantidadLab.toDouble()
                                val tamano = activity.abreviadoTam?.trim() ?: "Sin Tamanio"

                                if (tamano != "Sin tamanio") {
                                    totalCajasGeneralDia += cantidadCajas
                                }

                                desgloseCajasPorTamanoHoy[tamano] = (desgloseCajasPorTamanoHoy[tamano] ?: 0.0) + cantidadCajas
                                desglosePagosPorTamanoHoy[tamano] = (desglosePagosPorTamanoHoy[tamano] ?: 0.0) + sueldo
                            }
                        }

                        val activityName = activitiesForCode.first().vNombreAct.trim().take(16)
                        val line = "${activityCode.trim()} $activityName ${"%.2f".format(totalForCode)}"

                        if (line.length > 32) {
                            ticketContent.appendLine(line.take(32))
                            ticketContent.appendLine(line.drop(32).take(32))
                        } else {
                            ticketContent.appendLine(line)
                        }
                        totalDia += totalForCode
                    }

                    ticketContent.appendLine("Total del Dia: ${"%.2f".format(totalDia)}")

                    if (desgloseCajasPorTamanoHoy.isNotEmpty()) {
                        ticketContent.appendLine("--- Desglose Cajas ---")
                        desgloseCajasPorTamanoHoy.forEach { (tamano, cantidad) ->
                            if (tamano == "Sin tamanio") {
                                desglosePagosPorTamanoHoy.forEach { (t, pagoTotal) ->
                                    if (t == "Sin tamanio") {
                                        ticketContent.appendLine("Pago septimo dia: ${"%.2f".format(pagoTotal)}")
                                    }
                                }
                            } else {
                                ticketContent.appendLine("Cajas de $tamano: ${"%.0f".format(cantidad)}")
                            }
                        }
                    }

                    if (totalCajasGeneralDia > 0.0) {
                        ticketContent.appendLine("Total Cajas: ${"%.0f".format(totalCajasGeneralDia)}")
                    }
                    ticketContent.appendLine("------------------------------\n")
                    totalWeek += totalDia
                }

                // ⭐ Cálculo del Neto Final respetando la validación de la API
                val totalNeto = totalWeek - totalSindicato

                val activitiesWithWorkerInfo = ActivitiesWithWorkerInfo(
                    ticketContent = ticketContent,
                    weekTotal = totalWeek,
                    workerCode = workerCode,
                    fullName = fullName,
                    email = workerEmail,
                    totalSindicato = totalSindicato,
                    totalNeto = totalNeto
                )
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