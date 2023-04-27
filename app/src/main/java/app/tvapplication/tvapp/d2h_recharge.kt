package app.tvapplication.tvapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_home.*
import java.util.HashMap

class d2h_recharge : AppCompatActivity() {
    var shopName=""
    var username=""
    var shopMobile=""
    var balance=""
    var operatorCode = "0"
    lateinit var btnPayNow:LottieAnimationView
    lateinit var etSubId:EditText
    lateinit var etAmount:EditText
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_d2h_recharge)
        var spinnerbankNames: Spinner = findViewById(R.id.spinnerOperatorName)
        btnPayNow=findViewById(R.id.animationSprinkled)
        etAmount=findViewById(R.id.etAmount)
        etSubId=findViewById(R.id.etSubId)
        var biller_id = 0
        ArrayAdapter.createFromResource(
            this,
            R.array.operatorNamesDTH_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerbankNames.adapter = adapter
            spinnerbankNames.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // TODO Auto-generated method stub
                    var id = parent.getItemAtPosition(position)
                    biller_id = position //this would give you the id of the selected item
                    when (biller_id) {
                        1 -> {
                            operatorCode = "27"
                        }
                        2 -> {
                            operatorCode = "23"
                        }
                        3 -> {
                            operatorCode = "26"
                        }
                        4 -> {
                            operatorCode = "28"
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            })
        }
        var sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        shopName = sharedPreferences.getString("name", "").toString()
        shopMobile = sharedPreferences.getString("mobile", "").toString()
        if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            val dialog = AlertDialog.Builder(this@d2h_recharge)
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
        if (ConnectionManager().checkConnectivity(this)) {
            getBalance()
            btnPayNow.setOnClickListener {
                if (!ConnectionManager().checkConnectivity(this)) {
                    val dialog = AlertDialog.Builder(this@d2h_recharge)
                    dialog.setTitle("Error")
                    dialog.setMessage("No internet connection found. Please turn on your internet.")
                    dialog.setNegativeButton("OK") { text, listener ->
                    }

                    dialog.create()
                    dialog.setCancelable(false)
                    dialog.show()
                }
                else{
                    if (biller_id == 0) {
                        val dialog = AlertDialog.Builder(this@d2h_recharge)
                        dialog.setTitle("Error")
                        dialog.setMessage("Please select operator")
                        dialog.setNegativeButton("ok") { text, listener ->
                        }

                        dialog.create()
                        dialog.setCancelable(false)
                        dialog.show()
                    }
                }
            }
        }
        else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@d2h_recharge)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and retry.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                finish()
            }
            builder.create()
            builder.show()
        }
    }
    fun getBalance(){
        if (ConnectionManager().checkConnectivity(this)) {
            val queue = Volley.newRequestQueue(this@d2h_recharge)
            val url =
                "$link/api/user_detail/findOne?_where=(username,eq,${username})"
            val jsonRequest = object : JsonArrayRequest(
                Method.GET, url, null,
                Response.Listener {
                    val response = it.toString()
                    if (response != "[]") {
                        val array = it.getJSONObject(0)
                        balance = array.getString("balance")
                        }
                }, Response.ErrorListener {
                    progressBalance.visibility= View.GONE
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
            queue.add(jsonRequest)}
        else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@d2h_recharge)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and retry.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                finish()
            }
            builder.create()
            builder.show()
        }
    }
}