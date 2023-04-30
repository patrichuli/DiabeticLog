package com.unileon.diabeticlog.vista

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unileon.diabeticlog.R

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothScanAcitvity : AppCompatActivity() {

    companion object {

        private val REQUEST_ENABLE_BT = 1
        // Stops scanning after 10 seconds.
        private val SCAN_PERIOD: Long = 10000
        private val PERMISSION_REQUEST_COARSE_LOCATION = 1
        private val TAG: String = "BluetoothScanActivity"
    }

    //  private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mDeviceList: ArrayList<BluetoothDevice> = ArrayList()
    private lateinit var mBleDeviceAdapter: BLEDeviceAdapter
    private var mScanning: Boolean = false
    private var mHandler: Handler = Handler()

    @SuppressLint("WrongConstant")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_scan_acitvity)

        //shows the button to go back to the home page
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Ask for location permission if not already allowed
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED
            ) ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }


        // Checks if Bluetooth is supported on the device.
        if (!checkBluetoothFeatures()) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        //init all views
        initViews()
    }

    //arrow to go back
    override fun onSupportNavigateUp(): Boolean {

        onBackPressed()
        return true
    }

    fun initViews() {
        val mListView = findViewById<RecyclerView>(R.id.list_devices)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        mListView.layoutManager = manager
        mBleDeviceAdapter = BLEDeviceAdapter()
        mListView.adapter = mBleDeviceAdapter

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_escaneo, menu)
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).isVisible = false
            menu.findItem(R.id.menu_scan).isVisible = true
            menu.findItem(R.id.menu_refresh).actionView = null
        } else {
            menu.findItem(R.id.menu_stop).isVisible = true
            menu.findItem(R.id.menu_scan).isVisible = false
            menu.findItem(R.id.menu_refresh).setActionView(
                R.layout.actionbar_indeterminate_progress)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_scan -> {
                mDeviceList.clear()
                scanLeDevice(true)
                true
            }
            R.id.menu_stop -> {
                scanLeDevice(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkBluetoothFeatures(): Boolean {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            return false
        }

        // Initializes a Bluetooth adapter.
        val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
        return mBluetoothAdapter != null
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }



    override fun onResume() {
        super.onResume()

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Initializes list view adapter.
        mDeviceList.clear()
        scanLeDevice(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
        mDeviceList.clear()
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed({
                mScanning = false
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
                invalidateOptionsMenu()
            }, SCAN_PERIOD)

            mScanning = true
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            mScanning = false
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        }
        invalidateOptionsMenu()
    }

    private fun addDevice(device: BluetoothDevice) {
        runOnUiThread {
            if (!mDeviceList.contains(device))
                mDeviceList.add(device)

            mBleDeviceAdapter.notifyDataSetChanged()
        }
    }

    private inner class BLEDeviceAdapter : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int {
            return mDeviceList.size
        }

        private var context: Context? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            val view = LayoutInflater.from(context).inflate(R.layout.listitem_device, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = mDeviceList[position]
            if (device.name.isNullOrEmpty()) {
                holder.mDeviceAddr?.visibility = View.GONE
                holder.mDeviceName?.visibility = View.GONE
            } else {
                holder.mDeviceName?.text = device.name
                holder.mDeviceAddr?.text = device.address
                holder.mDeviceAddr?.visibility = View.VISIBLE
                holder.mDeviceName?.visibility = View.VISIBLE
            }
            val intent = Intent(context, DeviceControlActivity::class.java)
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.name)
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.address)
            if (mScanning) {
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
                mScanning = false
            }
            startActivity(intent)
        }

    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val mDeviceName: TextView? = itemView?.findViewById(R.id.device_name)
        val mDeviceAddr: TextView? = itemView?.findViewById(R.id.device_address)
    }

    // Device scan callback.
    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            if (!mDeviceList.contains(device))
                mDeviceList.add(device)

            mBleDeviceAdapter.notifyDataSetChanged()
        }
    }

}