package com.agroberriesmx.agrokiosko.domain.ticket

object TicketBuilder {
    val fechaHoy = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    val anio = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date())

    fun buildPayrollTicket(
        totalNOR: Double,
        totalOPE: Double,
        totalAGI: Double,
        totalRET: Double,
        totalFinal: Double,
        codigoTra: String,
        nombreCompleto: String,
        semana: String,
        rancho: String
    ): String {
        return """
        *************************
        RESUMEN DE PAGO DE NOMINA
        *************************
        SEMANA - $semana
        CODIGO TRABAJADOR: $codigoTra
        NOMBRE TRABAJADOR:
        $nombreCompleto
        RANCHO: $rancho
        
        F.IMPRESION:  $fechaHoy
        SUELDO:       $totalNOR
        7MO DIA + BA:      $totalOPE
        AGUINALDO:    $totalAGI
        RETENCIONES: -$totalRET
        -----------------------
        BA = BONO DE ASISTENCIA
        -----------------------
        TOTAL:        $totalFinal
        -----------------------
        
        KIOSKO AGROBERRIES MEXICO ${anio}
        Dudas?
        Comunicate con RRHH!
        ***********************
        
        
        
    """.trimIndent()
    }

    fun buildActivitiesTicket(
        activitiesData: StringBuilder,
        activitiesDataTotal: String,
        workerCode: String,
        workerName: String
    ): String {

        return """
***********************
DETALLE DE ACTIVIDADES
***********************
CODIGO TRABAJADOR: $workerCode
NOMBRE TRABAJADOR:
$workerName

F.IMPRESION:  $fechaHoy
$activitiesData        
-----------------------
TOTAL:        $activitiesDataTotal
-----------------------

KIOSKO AGROBERRIES MEXICO ${anio}
Dudas?
Comunicate con RRHH!
***********************



""".trimIndent()
    }
}