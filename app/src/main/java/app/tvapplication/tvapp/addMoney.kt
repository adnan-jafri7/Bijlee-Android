package app.tvapplication.tvapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_add_money.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.progressBar
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class addMoney : AppCompatActivity() {
    var username=""
    var shopName=""
    var shopMobile=""
    var balance=""
    var name=""
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_money)
        if (ConnectionManager().checkConnectivity(this)) {
            var etUsername: EditText = findViewById(R.id.etUsername)
            var btnGetDetails: Button = findViewById(R.id.btnGetDetails)
            var txtAgentNameValue: TextView = findViewById(R.id.txtValueAgentName)
            var txtAgentBalanceValue: TextView = findViewById(R.id.txtAgentBalanceValue)
            var txtAgentName:TextView=findViewById(R.id.txtAgentName)
            var txtAgentBalance:TextView=findViewById(R.id.txtAgentBalance)
            var etAmount: EditText = findViewById(R.id.etAmount)
            var btnRecharge: Button = findViewById(R.id.btnRecharge)
            var btnCancel:Button=findViewById(R.id.btnCancel)
            var progressBar2: ProgressBar =findViewById(R.id.progressBar2)

            var sharedPreferences =
                getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
            username = sharedPreferences.getString("username", "").toString()
            shopName = sharedPreferences.getString("name", "").toString()
            shopMobile = sharedPreferences.getString("mobile", "").toString()
            if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
                val dialog = AlertDialog.Builder(this@addMoney)
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
            if(username!="8979319339"){
                startActivity(Intent(this, dashboard::class.java))
                finish()
            }

            etUsername.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    if (s.length == 10) {
                        btnGetDetails.setBackgroundColor(0xFFF7C092.toInt())
                        btnGetDetails.isEnabled = true
                        val inputManager: InputMethodManager =
                            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(etUsername.windowToken, 0)
                    } else {
                        btnGetDetails.setBackgroundColor(0xDDDDDDDD.toInt())
                        btnGetDetails.isEnabled = false
                    }
                }
            })
            etAmount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    if (s.isNotEmpty()) {
                        btnRecharge.setBackgroundColor(0xFF4CAF50.toInt())
                        btnRecharge.isEnabled = true
                    } else {
                        btnRecharge.setBackgroundColor(0xDDDDDDDD.toInt())
                        btnRecharge.isEnabled = false
                    }
                }
            })

            btnGetDetails.setOnClickListener {
                btnGetDetails.visibility=View.GONE
                progressBar.visibility=View.VISIBLE
                var usernameValue=etUsername.text.toString()
                var queue=Volley.newRequestQueue(this)
                val url =
                    "$link/api/user_detail/findOne?_where=(username,eq,${usernameValue})"
                val jsonRequest = object : JsonArrayRequest(
                    Method.GET, url, null,
                    Response.Listener {
                        val response = it.toString()
                        if (response != "[]") {
                            val array = it.getJSONObject(0)
                            balance = array.getString("balance")
                            name=array.getString("name")
                            txtAgentBalanceValue.text=balance
                            txtValueAgentName.text=name
                            progressBar.visibility= View.GONE
                            txtValueAgentName.visibility= View.VISIBLE
                            txtAgentBalanceValue.visibility= View.VISIBLE
                            txtAgentBalance.visibility=View.VISIBLE
                            txtAgentName.visibility=View.VISIBLE
                            etAmount.visibility=View.VISIBLE
                            btnRecharge.visibility=View.VISIBLE
                            btnCancel.visibility=View.VISIBLE}
                        else{
                            progressBar.visibility= View.GONE
                            val builder = androidx.appcompat.app.AlertDialog.Builder(this@addMoney)
                            builder.setTitle("Alert")
                            builder.setMessage("No record found.")
                            builder.setCancelable(false)
                            builder.setPositiveButton("OK") { _, _ ->
                                txtValueAgentName.visibility= View.GONE
                                txtAgentBalanceValue.visibility= View.GONE
                                txtAgentBalance.visibility=View.GONE
                                txtAgentName.visibility=View.GONE
                                etAmount.visibility=View.GONE
                                btnRecharge.visibility=View.GONE
                                btnCancel.visibility=View.GONE
                                btnGetDetails.visibility=View.VISIBLE
                            }
                            builder.create()
                            builder.show()
                        }
                    }, Response.ErrorListener {
                        progressBar.visibility= View.GONE
                        val builder = androidx.appcompat.app.AlertDialog.Builder(this@addMoney)
                        builder.setTitle("Alert")
                        builder.setMessage("E1:Some error occurred.")
                        builder.setCancelable(false)
                        builder.setPositiveButton("OK") { _, _ ->
                            txtValueAgentName.visibility= View.GONE
                            txtAgentBalanceValue.visibility= View.GONE
                            txtAgentBalance.visibility=View.GONE
                            txtAgentName.visibility=View.GONE
                            etAmount.visibility=View.GONE
                            btnRecharge.visibility=View.GONE
                            btnCancel.visibility=View.GONE
                            btnGetDetails.visibility=View.VISIBLE
                        }
                        builder.create()
                        builder.show()
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
            btnCancel.setOnClickListener {
                txtValueAgentName.visibility= View.GONE
                txtAgentBalanceValue.visibility= View.GONE
                txtAgentBalance.visibility=View.GONE
                txtAgentName.visibility=View.GONE
                etAmount.visibility=View.GONE
                btnRecharge.visibility=View.GONE
                btnCancel.visibility=View.GONE
                btnGetDetails.visibility=View.VISIBLE
            }

            btnRecharge.setOnClickListener {
                val dialog = AlertDialog.Builder(this@addMoney)
                dialog.setTitle("Confirmation")
                dialog.setMessage("Are you sure you want to proceed?")
                dialog.setNegativeButton("No") { text, listener ->
                }
                dialog.setPositiveButton("Yes") { text, listener ->
                    btnCancel.visibility=View.GONE
                    btnRecharge.visibility=View.GONE
                    progressBar2.visibility=View.VISIBLE
                    var amount = etAmount.text.toString()
                    var newBalance = balance.toInt() + amount.toInt()
                    var queue = Volley.newRequestQueue(this)
                    val Df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val df: DateFormat = SimpleDateFormat("yyyyMMdd")
                    val curDate = df.format(Calendar.getInstance().time)
                    val currentDate = Df.format(Calendar.getInstance().time)
                    val tid="WL${amount}${etUsername.text}"
                    val jsonParams=JSONObject()
                    jsonParams.put(
                        "query",
                        "INSERT INTO `bill_details` (`Account`, `operatorName`, `amount`, `customerMobile`, `customerName`, `dueDate`, `billDate`, `paymentDate`, `Tid`, `shopName`, `shopMobile`, `status`, `balance`) VALUES ('${etUsername.text}', 'WalletLoad', '$amount', '8979319339', '$name', '$currentDate', '$currentDate', '$currentDate', '$tid', '$name', '${etUsername.text}', 'Credit', '$newBalance')"
                    )
                    val url =
                        "$link/dynamic/update"
                    val jsonRequest = object : JsonObjectRequest(
                        Request.Method.POST, url, jsonParams,
                        Response.Listener {
                            if (it.toString() == "{\"fieldCount\":0,\"affectedRows\":1,\"insertId\":0,\"serverStatus\":2,\"warningCount\":0,\"message\":\"\",\"protocol41\":true,\"changedRows\":0}") {
                                val url =
                                    "$link/dynamic/update"
                                val queue = Volley.newRequestQueue(this@addMoney)
                                jsonParams.put(
                                    "query",
                                    "UPDATE `user_detail` SET `balance`='$newBalance' WHERE username=${etUsername.text}"
                                )
                                val jsonRequest = object : JsonObjectRequest(
                                    Method.POST, url, jsonParams,
                                    Response.Listener {
                                        val message = it.getString("message")
                                        if (message == "(Rows matched: 1  Changed: 1  Warnings: 0") {
                                            val dialog = AlertDialog.Builder(this@addMoney)
                                            dialog.setTitle("Alert")
                                            dialog.setMessage("Recharge Successful. New Balance Rs.$newBalance.")
                                            dialog.setNegativeButton("OK") { text, listener ->
                                                btnRecharge.visibility=View.VISIBLE
                                                btnCancel.visibility=View.VISIBLE
                                                progressBar2.visibility=View.GONE
                                            }
                                            dialog.create()
                                            dialog.setCancelable(false)
                                            dialog.show()
                                        }
                                        else{
                                            val dialog = AlertDialog.Builder(this@addMoney)
                                            dialog.setTitle("Alert")
                                            dialog.setMessage("Recharge not Successful. New Balance $newBalance.")
                                            dialog.setNegativeButton("OK") { text, listener ->
                                                btnRecharge.visibility=View.VISIBLE
                                                btnCancel.visibility=View.VISIBLE
                                                progressBar2.visibility=View.GONE
                                            }
                                            dialog.create()
                                            dialog.setCancelable(false)
                                            dialog.show()

                                        }
                                    },
                                Response.ErrorListener {
                                    val dialog = AlertDialog.Builder(this@addMoney)
                                    dialog.setTitle("Alert")
                                    dialog.setMessage("Some error occurred.")
                                    dialog.setNegativeButton("OK") { text, listener ->
                                        btnRecharge.visibility=View.VISIBLE
                                        btnCancel.visibility=View.VISIBLE
                                        progressBar2.visibility=View.GONE
                                    }
                                    dialog.create()
                                    dialog.setCancelable(false)
                                    dialog.show()
                                })
                                {

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
                        },
                            Response.ErrorListener {
                                val dialog = AlertDialog.Builder(this@addMoney)
                                dialog.setTitle("Alert")
                                dialog.setMessage("Some error occurred.")
                                dialog.setNegativeButton("OK") { text, listener ->
                                    btnRecharge.visibility=View.VISIBLE
                                    btnCancel.visibility=View.VISIBLE
                                    progressBar2.visibility=View.GONE
                                }
                                dialog.create()
                                dialog.setCancelable(false)
                                dialog.show()
                            }
                    )
                    {

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
                dialog.create()
                dialog.setCancelable(false)
                dialog.show()
            }
        }
        else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@addMoney)
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

}