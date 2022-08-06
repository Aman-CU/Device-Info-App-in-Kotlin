package com.example.deviceinfoassignment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice.getDeviceId
import android.location.Location
import android.location.LocationManager
import android.media.audiofx.Equalizer
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.util.Log.d
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var latitudeTv: TextView
    private lateinit var longitudeTv: TextView
    private lateinit var imei_tv: TextView
    private lateinit var internet_status: TextView
    private lateinit var charging_status: TextView
    private lateinit var battery_level: TextView
    private lateinit var button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imei_tv = findViewById(R.id.imei_tv)
        internet_status = findViewById(R.id.internet_status_tv)
        charging_status = findViewById(R.id.chargin_status_tv)
        battery_level = findViewById(R.id.battery_level_tv)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        latitudeTv = findViewById(R.id.latitude_tv)
        longitudeTv = findViewById(R.id.longitude_tv)

        button = findViewById(R.id.button)



        allfunctions()





        //retrofit


        retrofitPost()

        //Handler

//        var runnable = Runnable {
//            allfunctions()
//
//
//        }
//
//        var hand = Handler()
//        hand.postDelayed(runnable,10000)




        button.setOnClickListener {
            allfunctions()
        }


    }

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun Imei(){


        var IMEI: String? = null
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val myImei = telephonyManager.deviceId
            if(myImei != null){
                IMEI = myImei
                imei_tv.text = IMEI
                Log.d("myemi",IMEI)
            }
        }catch (ex: Exception){
            Toast.makeText(this,ex.toString(),Toast.LENGTH_SHORT).show()
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission. READ_PHONE_STATE) !=
            PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat. shouldShowRequestPermissionRationale(this, android.Manifest.permission. READ_PHONE_STATE))
            {

            }
            else
            {
                ActivityCompat. requestPermissions(this, arrayOf(android.Manifest. permission.READ_PHONE_STATE), 2)

            }
        }
    }


    private fun internetStatus(){

        var context = this
        var connectivity : ConnectivityManager? = null
        var info : NetworkInfo? = null

        connectivity = context.getSystemService(Service.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        if ( connectivity != null)
        {
            info = connectivity!!.activeNetworkInfo

            if (info != null)
            {
                if (info!!.state == NetworkInfo.State.CONNECTED)
                {
                    internet_status.text = true.toString()
                }
            }
            else
            {
                internet_status.text = false.toString()

            }
        }
    }

    private fun chargingStatus(){

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }

        // isCharging if true indicates charging is ongoing and vice-versa
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        // Display whatever the state in the form of a Toast
        if(isCharging) {
            charging_status.text = true.toString()
//            Toast.makeText(applicationContext, "Charging", Toast.LENGTH_LONG).show()
        } else {
            charging_status.text = false.toString()
//            Toast.makeText(applicationContext,"Not Charging", Toast.LENGTH_LONG).show()
        }
    }

    private fun batteryLevel(){


        val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager

        val batLevel:Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        battery_level.text = batLevel.toString()
//        Toast.makeText(applicationContext,"Battery is $batLevel%",Toast.LENGTH_LONG).show()
    }

    private fun getLocation(){
        if (checkPermission()){

            if (isLocationEnabled()){

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task ->
                   val location: Location? = task.result

                    if(location == null){

                        Toast.makeText(this,"Null",Toast.LENGTH_SHORT).show()

                    }else {

                        latitudeTv.text = ""+location.latitude
                        longitudeTv.text = ","+location.longitude
                    }

                }

            }else{

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        }else{

            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION)
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
            ){

            return true
        }
        return false

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getLocation()
            }else {
                Toast.makeText(this,"Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrofitPost(){
        val retrofit = ServiceBuilder.buildService(API_Interface::class.java)
        val obj = RequestModel("${imei_tv.text}","${internet_status.text}","${charging_status.text}","${battery_level.text}","28.36188,-109.0447")
        retrofit.requestLogin(obj).enqueue(
            object : Callback<RequestModel> {
                override fun onResponse(
                    call: Call<RequestModel>,
                    response: Response<RequestModel>
                ) {
                    if(response.isSuccessful){
                        Log.e("Successful",response.body().toString())
                        Log.e("Successful",response.code().toString())
                        Log.e("Successful",response.message())
                    }

                }

                override fun onFailure(call: Call<RequestModel>, t: Throwable) {
                    t.printStackTrace()
                    Log.e("Error",t.message.toString())
                }
            }
        )
    }
    private fun allfunctions(){

        Imei()
        internetStatus()
        chargingStatus()
        batteryLevel()
        getLocation()
        retrofitPost()
    }

}






