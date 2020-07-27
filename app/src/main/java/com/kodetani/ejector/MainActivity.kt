package com.kodetani.ejector

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var port: UsbSerialPort? = null
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val accessory: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        accessory?.apply {
                            //call method to set up accessory communication
                            enablePort(this)
                        }
                    } else {
                        Log.d("TAG", "permission denied for accessory $accessory")
                    }
                }
            }
        }
    }

    fun enablePort(po: UsbDevice) {
        connectToDevices()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectToDevices()
    }

    private fun connectToDevices() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            textStatus.text = "No available driver..."
            textStatus.setTextColor(Color.RED)
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        Log.e("driver", "AVAILABLE DRIVER $driver")
        Log.e("driver", "AVAILABLE DRIVER ${driver.device?.deviceId}")
        Log.e("driver", "AVAILABLE DRIVER ${driver.device?.deviceName}")

        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            textStatus.text = "No connection, need permission..."
            textStatus.setTextColor(Color.RED)
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter)
            manager.requestPermission(driver.device, permissionIntent)
            return
        }

        port = driver.ports[0]
        port?.open(connection)
        port?.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        textStatus.text = "port opened..."
        textStatus.setTextColor(Color.BLUE)

        btnReject.setOnClickListener {
            port?.write("r".toByteArray(), 100)
            var bytes = ByteArray(20)
            port?.read(bytes, 100)
            textStatus.text = String(bytes)
        }
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.kodetani.ejector.USB_PERMISSION"
    }
}