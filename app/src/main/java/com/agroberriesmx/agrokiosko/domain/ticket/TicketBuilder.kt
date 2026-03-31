package com.agroberriesmx.agrokiosko.domain.ticket

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketBuilder {
    val fechaHoy = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    val anio = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    val horaActual = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date())

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
        
        F.IMPRESION: 
        $fechaHoy-$horaActual
        -----------------------
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
        subtotal: Double,       // Cambiamos a Double para recibir el dato real
        descuento: Double,      // Recibimos el descuento de la API
        neto: Double,           // Recibimos el neto de la API
        workerCode: String,
        workerName: String
    ): String {

        // ⭐ Solo mostramos el bloque de descuento si el descuento es mayor a cero
        val seccionFinanciera = if (descuento > 0.0) {
            """
SUBTOTAL:      ${"%.2f".format(subtotal)}
DESC. SIND (2%): -${"%.2f".format(descuento)}
TOTAL NETO:    ${"%.2f".format(neto)}
            """.trimIndent()
        } else {
            "TOTAL:        ${"%.2f".format(subtotal)}"
        }

        return """
***********************
DETALLE DE ACTIVIDADES
***********************
CODIGO TRABAJADOR: $workerCode
NOMBRE TRABAJADOR:
$workerName

FECHA Y HORA DE IMPRESION: 
$fechaHoy-$horaActual
-----------------------
$activitiesData        
-----------------------
$seccionFinanciera
-----------------------

KIOSKO AGROBERRIES MEXICO $anio
Dudas?
Comunicate con RRHH!
***********************
""".trimIndent() + "\n\n\n"
    }
}