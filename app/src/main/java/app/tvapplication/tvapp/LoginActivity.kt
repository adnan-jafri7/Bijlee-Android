package app.tvapplication.tvapp

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.tvapplication.tvapp.R
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity(){
    lateinit var imgFlash:ImageView
    lateinit var txtSignup:TextView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    var PERMISSION_ID = 44
    var phoneNumber=""

    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        var sharedPreferences=getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        verifyStoragePermissions(this)
        //GetNumber()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //createLocationRequest()
        //getLastLocation()
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val progressBar=findViewById<ProgressBar>(R.id.progressBar)
        var etMobile = findViewById<EditText>(R.id.etPhone)
        var etPassword = findViewById<EditText>(R.id.etPassword)
        imgFlash=findViewById(R.id.imgFlash)
        var Mobile=""
        var Password=""
        var balance=""
        txtSignup=findViewById(R.id.txtSignup)
        var isLoggedIn=sharedPreferences.getBoolean("isLoggedIn",false)
        if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            sharedPreferences.edit().clear().apply()}
        if(isLoggedIn){
            startActivity(Intent(this, dashboard::class.java))
            finish()
        }

        txtSignup.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
        }


        btnLogin.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this)) {

            btnLogin.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            Mobile = etMobile.text.toString()
            Password = etPassword.text.toString()
            if (Mobile.length<10) {
                val dialog = AlertDialog.Builder(this@LoginActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Please enter correct Mobile No")
                dialog.setNegativeButton("OK") { text, listener ->
                    btnLogin.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }

                dialog.create()
                dialog.setCancelable(false)
                dialog.show()
            }
            else if (Password.isNullOrEmpty()) {
                btnLogin.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                val dialog = AlertDialog.Builder(this@LoginActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Please enter correct Password")
                dialog.setNegativeButton("OK") { text, listener ->
                }

                dialog.create()
                dialog.setCancelable(false)
                dialog.show()
            }
            else {


                    //Password = Base64.encodeToString(Password.toByteArray(), Base64.DEFAULT)
                Password= md5(Password)
                   //Log.e("Log", "${Password}")
                //Toast.makeText(this, "$Password", Toast.LENGTH_SHORT).show()
                //Toast.makeText(this, "$Mobile", Toast.LENGTH_SHORT).show()
                val queue = Volley.newRequestQueue(this@LoginActivity)
                val url =
                    "http://3.84.82.121:3000/api/user_detail/findOne?_where=(username,eq,${Mobile})~and(passowrd,eq,$Password)"
                //Log.e("Log", "${url}")
                val jsonRequest = object : JsonArrayRequest(
                    Method.GET, url, null,
                    Response.Listener {
                        //Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                        val response = it.toString()
                        Log.e("DPSW", "${Password}")
                        //Log.e("DPSW", "${passwordFetched}")
                        if (response != "[]") {
                            val array = it.getJSONObject(0)
                            val mobileFetched = array.getString("mobile")
                            val passwordFetched = array.getString("passowrd")
                            val name=array.getString("name")
                            val username=array.getString("username")
                            val user_status=array.getInt("user_status")
                            balance = array.getString("balance")
                            /*Log.e("Log", "$balance")
                            Log.e("Log", "${mobileFetched}")
                            Log.e("Log", "${passwordFetched}")*/
                            //Log.e("DPSW", "${Password}")
                            //Log.e("DPSW", "${passwordFetched}")
                            //Log.e("EPSW", "${passwordFetched.equals(Password)}")

                                if (user_status == 0) {
                                    val dialog = AlertDialog.Builder(this@LoginActivity)
                                    dialog.setTitle("Error")
                                    dialog.setMessage("Your account is not activated.")
                                    dialog.setNegativeButton("OK") { text, listener ->
                                        btnLogin.visibility = View.VISIBLE
                                        progressBar.visibility = View.GONE
                                    }

                                    dialog.create()
                                    dialog.setCancelable(false)
                                    dialog.show()

                                } else {
                                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                    sharedPreferences.edit().putString("balance", balance).apply()
                                    sharedPreferences.edit().putString("name", name).apply()
                                    sharedPreferences.edit().putString("mobile", mobileFetched)
                                        .apply()
                                    sharedPreferences.edit().putString("username", username).apply()
                                    sharedPreferences.edit().putLong(
                                        "ExpiredDate",
                                        System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10080)
                                    ).apply()
                                    intent = Intent(this, dashboard::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            else{
                                val dialog = AlertDialog.Builder(this@LoginActivity)
                                dialog.setTitle("Error")
                                dialog.setMessage("Invalid Username or Password.")
                                dialog.setNegativeButton("OK") { text, listener ->
                                    btnLogin.visibility = View.VISIBLE
                                    progressBar.visibility = View.GONE
                                }

                                dialog.create()
                                dialog.setCancelable(false)
                                dialog.show()


                        }

                    },
                    Response.ErrorListener {
                        btnLogin.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Some Error Occurred ", Toast.LENGTH_SHORT).show()

                    }) {

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        return headers

                    }

                }
                jsonRequest.retryPolicy = DefaultRetryPolicy(
                    50000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                jsonRequest.setShouldCache(false)
                queue.add(jsonRequest)


            }

        }
            else {
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@LoginActivity)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and retry.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                }
                builder.create()
                builder.show()
            }
        }}

    private fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )


        }
    }
    /*@SuppressLint("MissingPermission")
    private fun getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(OnCompleteListener<Location?> { task ->
                        val location: Location? = task.getResult()
                        if (location == null) {
                            requestNewLocationData()
                        } else {

                        }
                    })
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        var mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.setPriority(PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(5)
        mLocationRequest.setFastestInterval(0)
        mLocationRequest.setNumUpdates(1)

        // setting LocationRequest
        // on FusedLocationClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
        }
    }

    // method to check for permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ), PERMISSION_ID
        )
    }

    // method to check
    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // If everything is alright then
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    fun createLocationRequest() {
        val REQUEST_CHECK_SETTINGS:Int = 0x1
        val locationRequest = com.google.android.gms.location.LocationRequest().apply{
            interval = 10000
            fastestInterval = 5000
            priority=PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@LoginActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }*/

    fun GetNumber() {
        if (ActivityCompat.checkSelfPermission(
                this,
                READ_SMS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                READ_PHONE_NUMBERS
            ) ==
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission check

            // Create obj of TelephonyManager and ask for current telephone service
            val telephonyManager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            phoneNumber = telephonyManager.line1Number
            Toast.makeText(this,"$phoneNumber",Toast.LENGTH_SHORT).show()
            return
        } else {
            // Ask for permission
            requestPermission()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                val telephonyManager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                if (ActivityCompat.checkSelfPermission(this, READ_SMS) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        READ_PHONE_NUMBERS
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        READ_PHONE_STATE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                phoneNumber = telephonyManager.line1Number

                Toast.makeText(this,"${phoneNumber.substring(2,12)}",Toast.LENGTH_SHORT).show()
            }
            else -> throw IllegalStateException("Unexpected value: $requestCode")
        }
    }
    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }



}



