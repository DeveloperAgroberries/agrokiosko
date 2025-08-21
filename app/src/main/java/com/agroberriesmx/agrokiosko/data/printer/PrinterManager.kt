package com.agroberriesmx.agrokiosko.data.printer

import android.content.Context
import com.agroberriesmx.agrokiosko.data.bluetooth.BluetoothManagerHelper
import com.agroberriesmx.agrokiosko.data.logger.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException

class PrinterManager(context: Context) {

    private val bluetoothHelper = BluetoothManagerHelper(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var logUtil: LogUtil

    fun connectPrinterAndPrint(macAddress: String, data: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val device = bluetoothHelper.connectToBluetoothDevice(macAddress)
        if (device == null) {
            onError("El Bluetooth no esta disponible o esta desactivado")
            return
        }

        val bluetoothSocket = bluetoothHelper.createBluetoothSocket(device)
        if (bluetoothSocket == null) {
            onError("Error al conectar con la impresora")
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val outputStream = bluetoothSocket.outputStream
                val printData = data.toByteArray(Charsets.UTF_8)

                outputStream.use {stream ->
                    stream.write(printData)
                    delay(5000)
                    stream.flush()
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    logUtil.logErrorMessage("Error al imprimir: ${e.message}")
                    onError("Error al imprimir: ${e.message}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logUtil.logErrorMessage("Error inesperado: ${e.message}")
                    onError("Error inesperado: ${e.message}")
                }
            } finally {
                try {
                    bluetoothSocket.close() // Asegura que el socket siempre se cierre.
                } catch (e: IOException) {
                    // Loguea este error si es necesario.
                    logUtil.logErrorMessage("Error IO: ${e.message}")
                }
            }
        }
    }
}