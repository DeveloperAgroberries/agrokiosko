package com.agroberriesmx.agrokiosko.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import okio.IOException
import java.util.UUID

class BluetoothManagerHelper(context: Context) {
    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    fun connectToBluetoothDevice(macAddress: String): BluetoothDevice? {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return null
        }

        return try {
            bluetoothAdapter.getRemoteDevice(macAddress)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun createBluetoothSocket(device: BluetoothDevice): BluetoothDeviceSocket?{
        return try{
            val socket = device.createInsecureRfcommSocketToServiceRecord(
                UUID.fromString(SPP_UUID.toString())
            )
            socket.connect()
            BluetoothDeviceSocket(socket)
        }catch (e: IOException) {
            null
        }
    }
}

class BluetoothDeviceSocket(val socket: BluetoothSocket) {
    val outputStream = socket.outputStream

    fun close() {
        socket.close()
    }
}