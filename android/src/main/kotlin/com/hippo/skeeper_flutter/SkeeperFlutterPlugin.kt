package com.hippo.skeeper_flutter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.hippo.skeeper_flutter.Message.Companion.ACTION_CLOSE
import com.hippo.skeeper_flutter.Message.Companion.ACTION_OPEN
import com.hippo.skeeper_flutter.Message.Companion.ID_AUDIO
import com.hippo.skeeper_flutter.Message.Companion.NOITF_ID_AUDIO
import com.hippo.skeeper_flutter.Message.Companion.PARAM_MODE_FETUS2
import com.hippo.skeeper_flutter.Message.Companion.PARAM_MODE_HEART2
import com.hippo.skeeper_flutter.Message.Companion.PARAM_MODE_LUNG2
import com.hippo.skeeper_flutter.Message.Companion.STATUS_AUDIO_STOPPED
import com.punchthrough.blestarterappandroid.ble.ConnectionEventListener
import com.punchthrough.blestarterappandroid.ble.ConnectionManager
import com.punchthrough.blestarterappandroid.ble.ConnectionManager.skeeperNotify
import com.punchthrough.blestarterappandroid.ble.ConnectionManager.skeeperWrite
import com.punchthrough.blestarterappandroid.ble.NOTIFIABLE_SKEEPER_UUID
import com.punchthrough.blestarterappandroid.ble.WRITABLE_SKEEPER_UUID
import com.punchthrough.blestarterappandroid.ble.findCharacteristic
import com.punchthrough.blestarterappandroid.ble.toHexString
import com.smartsound.skeeper.AudioProcessing
import com.smartsound.skeeper.EventListener
import com.smartsound.skeeper.MODE_FETUS
import com.smartsound.skeeper.MODE_HEART
import com.smartsound.skeeper.PATH_EARJACK
import com.smartsound.skeeper.PATH_SPEAKER
import com.smartsound.skeeper.Request
import com.smartsound.skeeper.Response


import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"

fun logInfo(text: String) {
    Log.i("skeeperfl_tag", text)
}

fun logError(text: String) {
    Log.e("skeeperfl_tag", text)
}

/** SkeeperFlutterPlugin */
class SkeeperFlutterPlugin : FlutterPlugin,
    MethodCallHandler, //Application(), //Application.ActivityLifecycleCallbacks,
    ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity


    private var isScanning = false
    private var isSkeeperDevice = false
    private lateinit var skeeperDeviceInfo: Map<String, String>

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanFilters = mutableListOf<ScanFilter>()

    private val scanResults = mutableListOf<ScanResult>()

    private val isLocationPermissionGranted
        get() = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private var notifyingCharacteristics = mutableListOf<UUID>()


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "skeeper_flutter")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        init("onAttachedToEngine")
    }

    @SuppressLint("MissingPermission", "NewApi")
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "promptEnableBluetooth" -> {
                promptEnableBluetooth()
                result.success(null)
            }

            "startBleScan" -> {
                startBleScan()
                result.success(null)
            }

            "stopBleScan" -> {
                stopBleScan()
                result.success(null)
            }

            "getSkeeperDeviceInfo" -> {
                result.success(skeeperDeviceInfo)
            }

            "isSkeeperDevice" -> {
                result.success(isSkeeperDevice)
            }

            "startSendingData" -> {
                startSendingData()
                result.success(null)
            }

            "stopSendingData" -> {
                stopSendingData()
                result.success(null)
            }

            "useSpeaker" -> {
                useSpeaker()
                result.success(null)
            }

            "useEarpiece" -> {
                useEarpiece()
                result.success(null)
            }

            "switchToHeartMode" -> {
                switchToHeartMode()
                result.success(null)
            }

            "switchToFetusMode" -> {
                switchToFetusMode()
                result.success(null)
            }

            "switchToLungsMode" -> {
                switchToLungsMode()
                result.success(null)
            }

            "getBluetoothDevices" -> {
                logInfo("getBluetoothDevices called")
                val devices = scanResults.map { mapOf(
                    "name" to it.device.name,
                    "address" to it.device.address,
                    "alias" to (it.device.alias ?: ""))
                }
                result.success(devices)
            }

            "connectToDevice" -> {
                val address : String = if(call.hasArgument("address")) call.argument<String>("address").toString() else ""
                connectToDevice(address)
                result.success(null)
            }

            else -> {
                result.notImplemented() //connectToDevice
            }
        }
    }

    private lateinit var device: BluetoothDevice
    private lateinit var player: ClientPlayer
    private val sharedPrefFile = "blestarterappandroid"
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
    }

    private var path: Int = PATH_SPEAKER
    private var mode: Int = PARAM_MODE_HEART2

    /*******************************************
     * Public functions
     *******************************************/

    fun connectToDevice(address: String) {
//        init("connectToDevice")
        logInfo("connectToDevice => address = $address")
        device = scanResults.map { it.device }.toMutableSet().firstOrNull { it.address == address } ?: device
        ConnectionManager.connect(device, context)
        logInfo("connectToDevice called")

    }

    @ExperimentalUnsignedTypes
    fun startSendingData() {
        logInfo("startSendingData called")
        val payload = Request.getPayload(ID_AUDIO, ACTION_OPEN, mode, 0, "1.1.0")
        ConnectionManager.skeeperWrite?.let { characteristic ->
            logInfo("startSendingData skeeperWrite not null")
            ConnectionManager.writeCharacteristic(device, characteristic, payload)
        }
        player = ClientPlayer(context)
        player.create(device, mode, path)
    }

    @ExperimentalUnsignedTypes
    fun stopSendingData() {
        logInfo("stopSendingData called")
        val payload = Request.getPayload(ID_AUDIO, ACTION_CLOSE)
        ConnectionManager.skeeperWrite?.let { characteristic ->
            logInfo("stopSendingData skeeperWrite not null")
            ConnectionManager.writeCharacteristic(device, characteristic, payload)
        }
        player.destroy()
    }

    fun useSpeaker() {
        logInfo("useSpeaker called")
        path = PATH_SPEAKER
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("audio path", path)
        editor.apply()
//        startSendingData()
    }

    fun useEarpiece() {
        logInfo("useEarpiece called")
        path = PATH_EARJACK
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("audio path", path)
        editor.apply()
//        startSendingData()
    }

    fun switchToHeartMode() {
        logInfo("switchToHeartMode called")
        mode = PARAM_MODE_HEART2
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("play mode", mode)
        editor.apply()
//        startSendingData()
    }

    fun switchToFetusMode() {
        logInfo("switchToFetusMode called")
        mode = PARAM_MODE_FETUS2
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("play mode", mode)
        editor.apply()
//        startSendingData()
    }

    fun switchToLungsMode() {
        logInfo("switchToLungsMode called")
        mode = PARAM_MODE_LUNG2
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("play mode", mode)
        editor.apply()
//        startSendingData()
    }

    //===========================================================
    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Response.unregisterEventListener(eventListener)
        ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
        channel.setMethodCallHandler(null)

    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity

        init("onAttachedToActivity")
//        path = sharedPreferences.getInt("audio path", PATH_SPEAKER)
//        mode = sharedPreferences.getInt("play mode", PARAM_MODE_HEART2)
//
//        AudioProcessing.create(mode)
//        Response.registerEventListener(eventListener)
//        ConnectionManager.registerListener(connectionEventListener)
//        if (!bluetoothAdapter.isEnabled) {
//            promptEnableBluetooth()
//        }

    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        Response.unregisterEventListener(eventListener)
        AudioProcessing.destroy()
        ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
    }


    /*******************************************
     * Private functions
     *******************************************/


    @SuppressLint("NewApi")
    private fun init(source: String) {

        logInfo("running init() form $source")

        path = sharedPreferences.getInt("audio path", PATH_SPEAKER)
        mode = sharedPreferences.getInt("play mode", PARAM_MODE_HEART2)

        AudioProcessing.create(mode)
        Response.registerEventListener(eventListener)
        ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }


    @SuppressLint("NewApi")
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(activity, enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE, null)
        }
        ActivityCompat.requestPermissions(activity,
            arrayOf(Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN ),
            ENABLE_BLUETOOTH_REQUEST_CODE)
        logInfo("promptEnableBluetooth called")
    }

    @SuppressLint("NewApi", "MissingPermission")
    private fun startBleScan() {
        logInfo("startBleScan called")
        if (!bluetoothAdapter.isEnabled) promptEnableBluetooth()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            //scanResultAdapter.notifyDataSetChanged()
            scanFilters.add(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
                    .build()
            )
            bleScanner.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        logInfo("stopBleScan called")
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        logInfo("requestLocationPermission called")
        if (isLocationPermissionGranted) {
            return
        }
        activity.requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            LOCATION_PERMISSION_REQUEST_CODE
        )
//    runOnUiThread {
//      alert {
//        title = "Location permission required"
//        message = "Starting from Android M (6.0), the system requires apps to be granted " +
//                "location access in order to scan for BLE devices."
//        isCancelable = false
//        positiveButton(android.R.string.ok) {
//          requestPermission(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            LOCATION_PERMISSION_REQUEST_CODE
//          )
//        }
//      }.show()
//    }
    }

    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission", "NewApi")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                // scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    // Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                    context.toast("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                    logInfo("onScanResult => Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                // scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
            device = result.device
            isSkeeperDevice = true
            skeeperDeviceInfo = mapOf(
                "name" to device.name,
                "address" to device.address,
                "alias" to (device.alias ?: "")
            )
           // logInfo("onScanResult => skeeperDeviceInfo $skeeperDeviceInfo")
        }

        override fun onScanFailed(errorCode: Int) {
            isSkeeperDevice = false
            //Timber.e("onScanFailed: code $errorCode")
            logInfo("onScanResult => onScanFailed: code $errorCode")
            context.toast("onScanFailed: code $errorCode")
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            logInfo("connectionEventListener => level 0")
            onConnectionSetupComplete = { gatt ->
                ConnectionManager.skeeperNotify?.let { characteristic ->
                    ConnectionManager.enableNotifications(gatt.device, characteristic)
                }
                logInfo("connectionEventListener => onConnectionSetupComplete")
                val writeUuid = UUID.fromString(WRITABLE_SKEEPER_UUID)
                val notifyUuid = UUID.fromString(NOTIFIABLE_SKEEPER_UUID)
                gatt.findCharacteristic(writeUuid)?.let { characteristic ->
                    skeeperWrite = characteristic
                    //ConnectionManager.skeeperWrite = skeeperWrite // added by me
                }
                gatt.findCharacteristic(notifyUuid)?.let { characteristic ->
                    skeeperNotify = characteristic
                    //ConnectionManager.skeeperNotify = skeeperNotify // added by me
                }
            }
            onCharacteristicChanged = { newDevice, characteristic ->
                characteristic.value?.let {
                    val size = characteristic.value.size
                    if (size > 1) {
                        logInfo("Value changed on ${characteristic.uuid}: size = $size ")
                        //context.toast("Value changed on ${characteristic.uuid}: ${characteristic.value.toHexString()}")
                        activity.runOnUiThread {
                            channel.invokeMethod("onAudioData", characteristic.value)
                        }
                        Response.handler(characteristic.value)
                    }
                }
                logInfo("connectionEventListener => onCharacteristicChanged or data received")
            }
            onDisconnect = {
                logInfo("connectionEventListener => onDisconnect")
                activity.runOnUiThread {
                    context.toast("Disconnected or unable to connect to device.")
                    //          alert {
                    //            title = "Disconnected"
                    //            message = "Disconnected or unable to connect to device."
                    //            positiveButton("OK") {}
                    //          }.show()
                }
            }
            onNotificationsEnabled = { device, characteristic ->
                notifyingCharacteristics.add(characteristic.uuid)
                logInfo("connectionEventListener => onNotificationsEnabled")
            }

        }
    }

    private val eventListener by lazy {
        EventListener().apply {
            onError = {
                logError("err: id ${it.id}, action ${it.action}, err ${it.error}")
            }
            onNotificationEvent = { audioId: Int, status : Int ->
                logInfo("onVitalNotificationEvent ---> id: $audioId, status: $status")
                if (audioId == NOITF_ID_AUDIO && status == STATUS_AUDIO_STOPPED) {
                    val payload = Request.getPayload(ID_AUDIO, ACTION_CLOSE)
                    ConnectionManager.skeeperWrite?.let { characteristic ->
                        ConnectionManager.writeCharacteristic(device, characteristic, payload)
                    }
                }
            }
            onAudioEvent2 = { speaker: ShortArray, earjack: ShortArray, pcm: ShortArray, filtered: FloatArray ->

//                val res = if(path == PATH_SPEAKER) speaker else earjack
//                logInfo("onAudioEvent2 RAW=> audio received of size ${res.size} / stringValue = $res / stringCntValue = ${res.contentToString()}")
//
//                val res2 = AudioProcessing.getBuffer(path) ?: shortArrayOf()
//                logInfo("onAudioEvent2 AUDIO_PRO => audio received of size ${res2.size} / stringValue = $res2 / stringCntValue = ${res2.contentToString()}")

            }
            onVitalEvent = { bpm: Int, dB: Int, regularity: Int, beatCount: Int, err: Int  ->
                logInfo("onVitalEvent => bpm = $bpm / dB = $dB / regularity = $regularity / beatCount = $beatCount / err = $err")
            }
        }
    }

    /*******************************************
     * Extension functions
     *******************************************/

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private fun Application.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }


    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()


}
