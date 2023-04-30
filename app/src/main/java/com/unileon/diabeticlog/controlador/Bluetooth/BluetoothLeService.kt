package com.unileon.diabeticlog.controlador.Bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


/**
 * A service that interacts with the BLE device via the Android BLE API.
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLeService : Service() {

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices())

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String,
                                characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
      /*  if (UUID_GLUCOSE_MEASUREMENT == characteristic.uuid) {

            val flag = characteristic.properties
            val timeOffsetPresent: Boolean = flag and 0x01 > 0
            val typeAndLocationPresent: Boolean = flag and 0x02 > 0
            val concentrationUnit = if (flag and 0x04 > 0) "mmol/L" else "mg/Dl"
            val sensorStatusAnnunciationPresent: Boolean = flag and 0x08 > 0
            val contextInfoFollows: Boolean = flag and 0x10 > 0

            // Sequence number is used to match the reading with an optional glucose context
            val sequenceNumber = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1)


            // DateTime is always in little endian
            val year: Int = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,3)
            val month: Int = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,5)
            val day: Int = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,6)
            val hour: Int = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,7)
            val min: Int = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,8)
            val sec: Int = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,9)

            val calendar = GregorianCalendar(year, month - 1, day, hour, min, sec)
            // Read timestamp
            var timestamp = calendar.time

            if (timeOffsetPresent) {
                val timeOffset: Int = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 1)
                timestamp = Date(timestamp.getTime() + timeOffset * 60000)
            }

            if (typeAndLocationPresent) {
                val glucoseConcentration: Float = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 1)
                val multiplier = if (concentrationUnit === "mg/dl") 100000 else 1000
                val value = glucoseConcentration * multiplier
                Log.d(TAG,
                    String.format(
                        Locale.ENGLISH,
                        "%.1f %s, at (%s)",
                        value,
                        if (concentrationUnit === "mmol/L") "mmol/L" else "mg/dL",
                        timestamp
                    ))
                intent.putExtra(EXTRA_DATA, value.toString())
            }

          /*  var format = -1
            if (flag and 0x01 != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16
                Log.d(TAG, "Heart rate format UINT16.")
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8
                Log.d(TAG, "Heart rate format UINT8.")
            }
            val heartRate = characteristic.getIntValue(format, 1)!!
            Log.d(TAG, String.format("Received heart rate: %d", heartRate))
            intent.putExtra(EXTRA_DATA, heartRate.toString())*/



        } else {
            // For all other profiles, writes the data formatted in HEX.
            val data = characteristic.value
            if (data != null && data.size > 0) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data)
                    stringBuilder.append(String.format("%02X ", byteChar))
                intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
            }
        }


       */
        if (UUID_GLUCOSE_MEASUREMENT == characteristic.uuid) {

            val packet = characteristic.value
            var offset = 0
            var kgl = 0f
            var mol = 0f
            var mgdl = 0.0

            if (packet.size >= 14) {
                val data = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)
                val flags = data.get(0).toInt()
                val timeOffsetPresent: Boolean = flags and 0x01 > 0
                val typeAndLocationPresent: Boolean = flags and 0x02 > 0
                val concentrationUnitKgL = flags and 0x04 == 0
                val sensorStatusAnnunciationPresent: Boolean = flags and 0x08 > 0
                val contextInfoFollows = flags and 0x10 > 0
                val sequence = data.getShort(1).toInt()

                val year = data.getShort(3).toInt()
                val month = data.get(5).toInt()
                val day = data.get(6).toInt()
                val hour = data.get(7).toInt()
                val minute = data.get(8).toInt()
                val second = data.get(9).toInt()
                var ptr = 10
                if (timeOffsetPresent) {
                    offset = data.getShort(ptr).toInt()
                    ptr += 2
                }

                if (concentrationUnitKgL) {
                    kgl = BluetoothHelper().getSfloat16(data.get(ptr), data.get(ptr + 1))
                    mgdl = kgl * 100000.toDouble()
                } else {
                    mol = BluetoothHelper().getSfloat16(data.get(ptr), data.get(ptr + 1))
                    mgdl = mol * 1000 * MMOLL_TO_MGDL
                }
                ptr += 2
                if (typeAndLocationPresent) {
                    val typeAndLocation: Int = data.get(ptr).toInt()
                    val sampleLocation = typeAndLocation and 0xF0 shr 4
                    val sampleType = typeAndLocation and 0x0F
                    ptr++
                }
                if (sensorStatusAnnunciationPresent) {
                    val status: Int = data.get(ptr).toInt()
                }
                val calendar = Calendar.getInstance()
                calendar[year, month - 1, day, hour, minute] = second
                val time = calendar.timeInMillis

                val dataGlucose = mgdl.toString() + "mg/dl"
                Toast.makeText(this, "Glucose data: " + mgdl + " mg/dl", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "time: " + hour + ":" + minute + ":" + second
                        + "  " + day + "/" + month + "/" + year + " timeoffset: " + offset + " timestamp: " + time, Toast.LENGTH_SHORT).show()
                Log.d(TAG, String.format("Glucose data: %.1f %s, at (%s)", mgdl, "mg/dL", time))
                intent.putExtra(EXTRA_DATA, dataGlucose)

            }
        } else {
            // For all other profiles, writes the data formatted in HEX.
            val data = characteristic.value
            if (data != null && data.size > 0) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data)
                    stringBuilder.append(String.format("%02X ", byteChar))
                intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
            }
        }


        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        internal val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    private val mBinder = LocalBinder()

    /**
     * Initializes a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param address The device address of the destination device.
     * *
     * *
     * @return Return true if the connection is initiated successfully. The connection result
     * *         is reported asynchronously through the
     * *         `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * *         callback.
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
            && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                return true
            } else {
                return false
            }
        }

        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     * @param characteristic Characteristic to act on.
     * *
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        // This is specific to Heart Rate Measurement.
        if (UUID_GLUCOSE_MEASUREMENT == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     * @return A `List` of supported services.
     */
    val supportedGattServices: List<BluetoothGattService>?
        get() {
            if (mBluetoothGatt == null) return null

            return mBluetoothGatt!!.services
        }

    companion object {
        private val TAG = BluetoothLeService::class.java.simpleName

        private val STATE_DISCONNECTED = 0
        private val STATE_CONNECTING = 1
        private val STATE_CONNECTED = 2

        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

        val UUID_GLUCOSE_MEASUREMENT = UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb")

        const val MMOLL_TO_MGDL = 18.0182
        const val MGDL_TO_MMOLL = 1 / MMOLL_TO_MGDL
    }
}
