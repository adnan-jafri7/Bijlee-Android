package app.tvapplication.tvapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.tvapplication.billdetail.report_MainActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.imgGetBalance
import kotlinx.android.synthetic.main.activity_home.progressBalance
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class dashboard : AppCompatActivity() {
    var Mobile=""
    var username=""
    var shopName=""
    var shopMobile=""
    var Account = ""
    var due_amount=""
    var balance=""
    var user_status=0
    lateinit var progress: ProgressDialog
    lateinit var txtMobileRecharge:TextView
    lateinit var txtBalance:TextView
    lateinit var txtDthRecharge:TextView
    lateinit var txtChangePassword:TextView
    lateinit var txtDMT:TextView
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    lateinit var sharedPreferences:SharedPreferences
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {


            super.onCreate(savedInstanceState)
        sharedPreferences=getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        setContentView(R.layout.activity_dashboard)
        if (sharedPreferences.getLong("AuthTime", -1) < System.currentTimeMillis()) {
            sharedPreferences.edit().putBoolean("isAuthReq",true).apply()}
        var isAuthReq=sharedPreferences.getBoolean("isAuthReq",true)
        if(isAuthReq){
        bioAuth()}
        verifyStoragePermissions(this)


        if (ConnectionManager().checkConnectivity(this)) {
            var txtElectricity = findViewById<TextView>(R.id.txtElectricity)
            var txtLogout = findViewById<TextView>(R.id.txtLogout)
            var txtHistory = findViewById<TextView>(R.id.txtAccountHistory)
            var txtBusinessReports=findViewById<TextView>(R.id.txtDateSearch)
            var add_Money:TextView=findViewById(R.id.addMoney)
            txtChangePassword=findViewById(R.id.txtChangePassword)
            txtBalance = findViewById<TextView>(R.id.txtBalance)
            txtMobileRecharge = findViewById(R.id.txtMobile)
            txtDthRecharge=findViewById(R.id.txtDTH)
            txtDMT=findViewById(R.id.txtDMT)
            var txtHeading = findViewById<TextView>(R.id.txtHeading)
            var sharedPreferences =
                getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
            username = sharedPreferences.getString("username", "").toString()
            shopName = sharedPreferences.getString("name", "").toString()
            shopMobile = sharedPreferences.getString("mobile", "").toString()

            txtDMT.setOnClickListener {
                startActivity(Intent(this,dmt::class.java))

            }

            txtBusinessReports.setOnClickListener {
                if(username=="8979319339") {
                    val fm = supportFragmentManager
                    val fragment = business_report()
                    fm.beginTransaction().add(R.id.DashboardLL, fragment).commit()
                }
                else{
                    Toast.makeText(this,"You're not allowed to view this section.",Toast.LENGTH_SHORT).show()
                }
            }

            if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
                val dialog = AlertDialog.Builder(this@dashboard)
                dialog.setTitle("")
                dialog.setMessage("Session Expired. You have been logged out.")
                dialog.setNegativeButton("OK") { text, listener ->
                    sharedPreferences.edit().clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }


                dialog.create()
                dialog.setCancelable(false)
                dialog.show()
            }
            var isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
            if (!isLoggedIn) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            add_Money.setOnClickListener {
                if(username!="8979319339"){
                    Toast.makeText(this,"You are not authorized to view this section.",Toast.LENGTH_LONG).show()
                }
                else{
                    var intent=Intent(this,app.tvapplication.tvapp.addMoney::class.java)
                    startActivity(intent)
                }
            }
            txtHistory.setOnClickListener {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("type", "Electricity")
                startActivity(Intent(this, report_MainActivity::class.java))
            }
            txtMobileRecharge.setOnClickListener {
                val intent = Intent(this, mobilerecharge::class.java)
                startActivity(intent)

            }
            txtDthRecharge.setOnClickListener {
                startActivity(Intent(this,d2h_recharge::class.java))

            }
            txtChangePassword.setOnClickListener{
                startActivity(Intent(this,changePassword::class.java))
            }
            txtLogout.setOnClickListener {
                val dialog = AlertDialog.Builder(this@dashboard)
                dialog.setTitle("Confirmation")
                dialog.setMessage("Are you sure you want to logout?")
                dialog.setNegativeButton("No") { text, listener ->
                }
                dialog.setPositiveButton("Yes") { text, listener ->
                    sharedPreferences.edit().clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()

                }

                dialog.create()
                dialog.setCancelable(false)
                dialog.show()
            }
            txtElectricity.setOnClickListener {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
            imgGetBalance.setOnClickListener {
                getBalance()
            }
            txtHeading.text = "Hi, $shopName"
            getBalance()
        }
        else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@dashboard)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                ActivityCompat.finishAffinity(this)
            }
            builder.create()
            builder.show()
        }
    }

    fun getBalance(){
        imgGetBalance.visibility= View.GONE
        progressBalance.visibility= View.VISIBLE
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait..")
        progress.setProgressStyle(ProgressDialog.BUTTON_NEGATIVE)
        progress.isIndeterminate = true
        progress.setCancelable(false)
        progress.show()
        val queue = Volley.newRequestQueue(this@dashboard)

        val url =
            "$link/api/user_detail/findOne?_where=(username,eq,${username})"

        //Toast.makeText(this,"$url",Toast.LENGTH_LONG).show()
        val jsonRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            Response.Listener {
                val response = it.toString()
                if (response != "[]") {
                    val array = it.getJSONObject(0)
                    balance = array.getString("balance")
                    user_status=array.getInt("user_status")
                    userStatus(user_status)
                    progressBalance.visibility= View.GONE
                    imgGetBalance.visibility= View.VISIBLE
                    txtBalance.visibility=View.VISIBLE
                    txtBalance.text="${resources.getString(R.string.Rs)} ${balance.toString()}.00"}
            }, Response.ErrorListener {
                progressBalance.visibility= View.GONE
                imgGetBalance.visibility= View.VISIBLE
                progress.hide()
            }){

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
    fun verifyStoragePermissions(activity: Activity) {
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
    @RequiresApi(Build.VERSION_CODES.N)
    fun bioAuth() {

        var biometricPrompt: androidx.biometric.BiometricPrompt
        var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo
        var biometricManager: androidx.biometric.BiometricManager =
            androidx.biometric.BiometricManager.from(this@dashboard)
        when (biometricManager.canAuthenticate()) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                //Toast.makeText(this, "Biometric Available", Toast.LENGTH_SHORT).show()
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                //Toast.makeText(this, "Biometric Not Available", Toast.LENGTH_SHORT).show()
                finish()
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                //Toast.makeText(this, "No finger available. Please setup fingerprint lock. first", Toast.LENGTH_SHORT).show()
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                         DEVICE_CREDENTIAL)
                }
                startActivityForResult(enrollIntent, 0)
                finish()
            }

        }
        var executor: Executor = ContextCompat.getMainExecutor(this)
        biometricPrompt =androidx.biometric.BiometricPrompt(this@dashboard, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    /*Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()*/
                    finish()
                }

                override fun onAuthenticationSucceeded(
                    result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    /*Toast.makeText(applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()*/
                    sharedPreferences.edit().putLong("AuthTime",System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)).apply()
                    sharedPreferences.edit().putBoolean("isAuthReq",false).apply()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    //Toast.makeText(applicationContext, "Authentication failed",Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enter your phone screen lock pattern, PIN, password or fingerprint.")
            .setSubtitle("Unlock TVApp")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)

    }

    fun userStatus(user_status:Int) {

        if (user_status == 0) {
            val dialog = AlertDialog.Builder(this@dashboard)
            dialog.setTitle("Error")
            dialog.setMessage("Your account has been deactivated. Please Contact Admin.")
            dialog.setNegativeButton("OK") { text, listener ->
                sharedPreferences.edit().clear().apply()
                startActivity(Intent(this,LoginActivity::class.java))
                finishAffinity()
                progress.hide()

            }

            dialog.create()
            dialog.setCancelable(false)
            dialog.show()
        }
        else{
            progress.hide()
        }
    }

}