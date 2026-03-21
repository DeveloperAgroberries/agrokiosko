package com.agroberriesmx.agrokiosko.ui.options

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.agroberriesmx.agrokiosko.data.logger.LogUtil
import com.agroberriesmx.agrokiosko.data.mailer.AutoMailer
import com.agroberriesmx.agrokiosko.data.printer.PrinterManager
import com.agroberriesmx.agrokiosko.databinding.FragmentOptionsBinding
import com.agroberriesmx.agrokiosko.domain.ticket.TicketBuilder
import com.journeyapps.barcodescanner.CaptureActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import com.agroberriesmx.agrokiosko.R


@AndroidEntryPoint
class OptionsFragment : Fragment() {
    private var _binding: FragmentOptionsBinding? = null
    private val binding get() = _binding!!
    private val optionsViewModel: OptionsViewModel by viewModels()
    private lateinit var printerManager: PrinterManager
    private lateinit var logUtil: LogUtil
    private lateinit var mailer: AutoMailer

    companion object {
        //const val BLUETOOTH_PRINTER_ADDRESS = "10:22:33:80:C1:25"
        const val TICKET_PRINT_PREFS = "TicketPrintPrefs"
    }

    private lateinit var btnBuscarImpresora: Button
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        if (granted) {
            Toast.makeText(requireContext(), "Permisos otorgados", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Permisos denegados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleScanResult(
        result: ActivityResult,
        type: String,
        handleAction: suspend (String) -> Unit
    ) {
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val scannedData = result.data?.getStringExtra("SCAN_RESULT")
            if (!scannedData.isNullOrEmpty()) {
                lifecycleScope.launch {
                    try {
                        processScannedData(scannedData, type, handleAction)
                    } catch (e: Exception) {
                        logUtil.logMessage("ERROR: Fallo al procesar el escaneo: ${e.message}")
                        showToast("Error al procesar el escaneo. Por favor, inténtalo nuevamente.")
                    }
                }
            } else {
                logUtil.logMessage("ALERTA: Escaneo cancelado o sin resultado")
                showToast("Escaneo cancelado o sin resultado")
            }
        } else {
            logUtil.logMessage("ALERTA: Escaneo cancelado o sin resultado")
            showToast("Escaneo cancelado o sin resultado")
        }
    }

    private suspend fun processScannedData(
        scannedData: String,
        type: String,
        handleAction: suspend (String) -> Unit
    ) {
        logUtil.logMessage("--------------------------------------------------")
        logUtil.logMessage("Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())}")
        logUtil.logMessage("Dato escaneado: $scannedData")

        val canPrint = canPrintTicketForUser(scannedData, type)
        if (canPrint) {
            logUtil.logMessage("Tipo de ticket: $type")
            handleAction(scannedData)
        } else {
            logUtil.logMessage("Limite de impresiones para el usuario.")
            showToast("Ya has Impreso 3 veces este ticket el día de hoy.")
        }
    }

    private val barcodeLauncherForPayroll =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleScanResult(result, "nomina") { scannedData ->
                optionsViewModel.getPayroll(scannedData)
            }
        }

    private val barcodeLauncherForActivities =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleScanResult(result, "actividades") { scannedData ->
                optionsViewModel.getActivities(scannedData)
            }
        }

    private val barcodeLauncherForBonus =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleScanResult(result, "bono") { scannedData ->
                optionsViewModel.getPayroll(scannedData)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ✅ Se añade un escuchador de clic al botón de políticas
        binding.btnPoliticas.setOnClickListener {
            // ✅ Navega a PoliticasFragment cuando se presiona el botón
            findNavController().navigate(R.id.action_optionsFragment_to_menuPoliticasFragment)
        }
        printerManager = PrinterManager(requireContext())
        logUtil = LogUtil(requireContext())
        mailer = AutoMailer(requireContext())
        btnBuscarImpresora = binding.btnBuscarImpresora
        initUI()
    }

    private fun initUI() {
        initUIState()
        initListeners()
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                optionsViewModel.state.collect {
                    when (it) {
                        OptionsState.Loading -> loadingState()
                        is OptionsState.SuccessSharedLogs -> sharedLogsState(it)
                        is OptionsState.SuccessPayroll -> payrollState(it)
                        is OptionsState.SuccessActivities -> activitiesState(it)
                        is OptionsState.Error -> errorState()
                        OptionsState.Empty -> emptyState() // Agrega esta línea
                    }
                }
            }
        }
    }

    private fun emptyState() {
        // Lógica para el estado inicial, por ejemplo, ocultar el ProgressBar
        binding.pbView.visibility = View.GONE
    }

    private fun loadingState() {

    }

    private fun sharedLogsState(state: OptionsState.SuccessSharedLogs){
        showToast(state.toString())
        optionsViewModel.resetState() // Restablecer el estado después de usarlo
    }

    private fun payrollState(state: OptionsState.SuccessPayroll) {
        checkAndRequestPermissions(
            TicketBuilder.buildPayrollTicket(
                redondearADosDecimales(state.totalsPayroll.totals["NOR"] ?: 0.0),
                redondearADosDecimales(state.totalsPayroll.totals["OPE"] ?: 0.0),
                redondearADosDecimales(state.totalsPayroll.totals["AGI"] ?: 0.0),
                redondearADosDecimales(state.totalsPayroll.totals["RET"] ?: 0.0),
                redondearADosDecimales(state.totalsPayroll.totals["TOTAL"] ?: 0.0),
                state.totalsPayroll.workerCode,
                state.totalsPayroll.fullName,
                state.totalsPayroll.weekCode,
                state.totalsPayroll.fieldName
            ), state.totalsPayroll.workerCode, state.totalsPayroll.email, "nomina"
        )
        optionsViewModel.resetState() // Restablecer el estado después de usarlo
    }

    private fun activitiesState(state: OptionsState.SuccessActivities) {
        checkAndRequestPermissions(
            TicketBuilder.buildActivitiesTicket(
                state.activitiesData.ticketContent,
                redondearADosDecimales(state.activitiesData.weekTotal).toString(),
                state.activitiesData.workerCode,
                state.activitiesData.fullName
            ), state.activitiesData.workerCode, state.activitiesData.email,"actividades"
        )
        optionsViewModel.resetState() // Restablecer el estado después de usarlo
    }

    private fun errorState() {
        binding.pbView.visibility = View.GONE
        logUtil.logErrorMessage("Ocurrio un error al cargar los datos")
        showToast("Ocurrio un error al cargar los datos")

        // Restablece el ViewModel a un estado Empty para evitar re-renderizados
        optionsViewModel.resetState()
    }

    private fun initListeners() {
       /* binding.btnPrintPayroll.setOnClickListener {
            startScannerForPrintPayroll()
        }*/

        binding.btnPrintActivities.setOnClickListener {
            startScannerForPrintActivities()
        }

        /* binding.btnCheckBonus.setOnClickListener {
            startScannerForPrintBonus()
        }*/

        binding.ivSettings.setOnClickListener {
            optionsViewModel.shareLogs()
        }

        // --- AÑADE ESTO ---
        btnBuscarImpresora.setOnClickListener {
            choosePrinter { selectedAddress ->
                showToast("Impresora seleccionada: $selectedAddress")
            }
        }
        // ------------------
    }

    private fun startScannerForPrintPayroll() {
        lifecycleScope.launch {
            val scanIntent = Intent(requireContext(), CaptureActivity::class.java)
            barcodeLauncherForPayroll.launch(scanIntent)
        }
    }

    private fun startScannerForPrintActivities() {
        lifecycleScope.launch {
            val scanIntent = Intent(requireContext(), CaptureActivity::class.java)
            barcodeLauncherForActivities.launch(scanIntent)
        }
    }

    private fun startScannerForPrintBonus() {
        lifecycleScope.launch {
            val scanIntent = Intent(requireContext(), CaptureActivity::class.java)
            barcodeLauncherForBonus.launch(scanIntent)
        }
    }

    private fun checkAndRequestPermissions(data: String, workerCode: String, toEmail: String, ticketType: String) {
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val requiredPermissions = arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )

                val missingPermissions = requiredPermissions.filter { permission ->
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                }

                if (missingPermissions.isNotEmpty()) {
                    // Solicitar permisos
                    requestPermissionLauncher.launch(missingPermissions.toTypedArray())
                } else {
                    // Todos los permisos ya están otorgados
                    val canPrint = canPrintTicketForUser(workerCode, ticketType)
                    if (canPrint) {
                        incrementPrintCountForUser(workerCode, ticketType)
                        printTicket(data,
                            onSuccess = { showToast("Impresión completada.") },
                            onError = { errorMessage -> showToast("Error al imprimir: $errorMessage") })
                        mailer.sendEmailInBackground(toEmail,"Nomina",data)
                    } else {
                        logUtil.logMessage("Limite de impresiones para el usuario.")
                        showToast("Ya has impreso 3 veces este ticket el día de hoy.")
                    }
                }
            } else {
                // No se requiere permiso explícito para versiones anteriores a Android 12
                val canPrint = canPrintTicketForUser(workerCode, ticketType)
                if (canPrint) {
                    incrementPrintCountForUser(workerCode, ticketType)
                    printTicket(data,
                        onSuccess = { showToast("Impresión completada.") },
                        onError = { errorMessage -> showToast("Error al imprimir: $errorMessage") })
                    mailer.sendEmailInBackground(toEmail,"Nomina",data)
                } else {
                    logUtil.logMessage("Limite de impresiones para el usuario.")
                    showToast("Ya has impreso 3 veces este ticket el día de hoy.")
                }
            }
        }
    }

    private fun redondearADosDecimales(valor: Double): Double {
        return BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    private suspend fun canPrintTicketForUser(userId: String, ticketType: String): Boolean {
        val context = context ?: return false  // Retorna false si el contexto es nulo (Fragmento o actividad destruida)

        return withContext(Dispatchers.Main) {
            val sharedPreferences = context.getSharedPreferences(TICKET_PRINT_PREFS, Context.MODE_PRIVATE)

            // Verificar que el contexto es válido antes de proceder con SharedPreferences
            if (sharedPreferences == null) {
                logUtil.logErrorMessage("SharedPreferences no accesible")
                return@withContext false
            }

            val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            var lastPrintDate = ""
            var printCount = 0

            try {
                synchronized(sharedPreferences) {
                    lastPrintDate = sharedPreferences.getString("lastPrintDate_${userId}_$ticketType", "") ?: ""

                    // Si la fecha no coincide, reiniciar el contador
                    if (lastPrintDate != currentDate) {
                        sharedPreferences.edit().apply {
                            putInt("printCount_${userId}_$ticketType", 0)
                            putString("lastPrintDate_${userId}_$ticketType", currentDate)
                        }.apply() // Usar apply() para evitar un posible bloqueo con commit()
                    }

                    // Obtener el contador de impresiones
                    printCount = sharedPreferences.getInt("printCount_${userId}_$ticketType", 0)
                }
            }catch (io: IOException) {
                logUtil.logErrorMessage("Error de IO: ${io.message}")
                return@withContext false
            } catch (e: Exception) {
                logUtil.logErrorMessage("Error al acceder a SharedPreferences: ${e.message}")
                return@withContext false
            }

            // Verificar si el número de impresiones es menor que 3 Ricardo Dimas
            printCount.toInt() < 3
        }
    }

    private suspend fun incrementPrintCountForUser(userId: String, ticketType: String) {
        return withContext(Dispatchers.IO) {
            val printCount = getPrintCountForUser(userId, ticketType)
            setPrintCountForUser(userId, ticketType, printCount + 1)
        }
    }

    private fun getPrintCountForUser(userId: String, ticketType: String): Int {
        val sharedPreferences =
            requireContext().getSharedPreferences(TICKET_PRINT_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getInt("printCount_${userId}_$ticketType", 0)
    }

    private fun setPrintCountForUser(userId: String, ticketType: String, newCount: Int) {
        val sharedPreferences =
            requireContext().getSharedPreferences(TICKET_PRINT_PREFS, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putInt("printCount_${userId}_$ticketType", newCount)
            putString("lastPrintDate_${userId}_$ticketType", java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()))
            apply()
        }
    }

    // En OptionsFragment.kt
    private fun printTicket(data: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!::printerManager.isInitialized) {
                    logUtil.logErrorMessage("La clase PrinterManager no está inicializada.")
                    withContext(Dispatchers.Main) {
                        showToast("Error de inicialización. Por favor, reinicia la app.")
                    }
                    return@launch
                }

                val prefs = requireContext().getSharedPreferences(TICKET_PRINT_PREFS, Context.MODE_PRIVATE)
                val printerAddress = prefs.getString("lastPrinterAddress", null)

                if (printerAddress == null) {
                    withContext(Dispatchers.Main) {
                        showToast("Primero selecciona una impresora usando el botón 'Buscar impresora'.")
                    }
                    return@launch
                }

                // Proceder con la impresión si la dirección existe
                printerManager.connectPrinterAndPrint(
                    printerAddress,
                    data,
                    onSuccess,
                    onError
                )
            } catch (e: Exception) {
                logUtil.logErrorMessage("Excepción al imprimir: ${e.message}")
                withContext(Dispatchers.Main) { showToast("Excepción al imprimir: ${e.message}") }
            }
        }
    }

    private fun showToast(message: String) {
        if(isAdded){
            try {
                if(message != null) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    //Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
                } else {
                    logUtil.logErrorMessage("El message esta vacio.")
                }
            } catch (e: Exception) {
                logUtil.logErrorMessage("Mostrando mensaje en Toast: ${e.message}")
            }
        }
    }

    private fun choosePrinter(onPrinterSelected: (String) -> Unit) {
        // Verificar si los permisos de Bluetooth están otorgados
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Si no tiene el permiso, solicitárselo al usuario
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))
                showToast("Permisos de Bluetooth necesarios para buscar impresoras.")
                return
            }
        }

        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            showToast("Activa el Bluetooth para buscar impresoras")
            return
        }

        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            showToast("No hay impresoras emparejadas")
            return
        }

        val deviceNames = pairedDevices.map { it.name }.toTypedArray()
        val deviceAddresses = pairedDevices.map { it.address }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Selecciona impresora")
            .setItems(deviceNames) { _, which ->
                val selectedAddress = deviceAddresses[which]
                // Guardamos la impresora seleccionada
                requireContext()
                    .getSharedPreferences(TICKET_PRINT_PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString("lastPrinterAddress", selectedAddress)
                    .apply()
                onPrinterSelected(selectedAddress)
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOptionsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}