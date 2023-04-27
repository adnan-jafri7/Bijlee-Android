package app.tvapplication.tvapp

import android.animation.Animator
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import app.tvapplication.billdetail.report_MainActivity
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {
    var Mobile=""
    var username=""
    var shopName=""
    var shopMobile=""
    var Account = ""
    var due_amount=""
    var balance=""
    var type:String?=""
    lateinit var progress: ProgressDialog
    private var isRevealed = false
    lateinit var RlSuccess:RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        var sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        shopName = sharedPreferences.getString("name", "").toString()
        shopMobile = sharedPreferences.getString("mobile", "").toString()
        if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            val dialog = AlertDialog.Builder(this@HomeActivity)
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


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if (ConnectionManager().checkConnectivity(this)) {
            getBalance()
            var operatorCode = "0"
            var txtReports = findViewById<TextView>(R.id.txtReports)
            var txtBalance = findViewById<TextView>(R.id.txtBalance)
            var progressBalance = findViewById<ProgressBar>(R.id.progressBalance)
            var etAccount: EditText = findViewById(R.id.etAccountNo)
            var txtLogout = findViewById<TextView>(R.id.txtLogout)
            RlSuccess=findViewById(R.id.RlFull)
            txtReports.setOnClickListener {
                val intent = Intent(this, report_MainActivity::class.java)
                startActivity(intent)
            }
            txtLogout.setOnClickListener {
                val dialog = AlertDialog.Builder(this@HomeActivity)
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
            //getting balance
            imgGetBalance.setOnClickListener {
                getBalance()

            }
            type = intent?.getStringExtra("type")
            when (type) {
                "MobileRecharge" -> {
                    mobileRecharge()
                }
            }


            var spinnerbankNames: Spinner = findViewById(R.id.spinnerOperatorName)
            var biller_id = 0
            ArrayAdapter.createFromResource(
                this,
                R.array.operatorNames_array,
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
                                operatorCode = "88"
                                etAccount.filters = arrayOf<InputFilter>(LengthFilter(10))
                            }
                            2 -> {
                                operatorCode = "114"
                                etAccount.filters = arrayOf<InputFilter>(LengthFilter(12))
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }

                })
            }
            var btnPayBill = findViewById<Button>(R.id.btnPayBill)
            var btnFetchBill = findViewById<Button>(R.id.btnfetchBill)
            var btnCancel = findViewById<Button>(R.id.btnCancel)
            var txtCusNameT = findViewById<TextView>(R.id.txtCustomerName)
            var txtBillDateT = findViewById<TextView>(R.id.txtBillDate)
            var txtDueDateT = findViewById<TextView>(R.id.txtDueDate)
            var txtAmountT = findViewById<TextView>(R.id.txtAmount)
            var progressBar = findViewById<ProgressBar>(R.id.progressBar)
            var txtLoading = findViewById<TextView>(R.id.txtLoading)
            var txtCustomerName = findViewById<TextView>(R.id.txtValueCustName)
            var txtBillDate = findViewById<TextView>(R.id.txtValueBillDate)
            var txtDueDate = findViewById<TextView>(R.id.txtValueDueDate)
            var txtAmount = findViewById<TextView>(R.id.txtValueAmount)
            var etMobile = findViewById<EditText>(R.id.etMobileNo)

            var customer_name = ""
            var bill_date = ""
            var due_date = ""
            var operator = ""



            btnFetchBill.setOnClickListener {
                if (!ConnectionManager().checkConnectivity(this)) {
                    val dialog = AlertDialog.Builder(this@HomeActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("No internet connection found. Please turn on your internet.")
                    dialog.setNegativeButton("OK") { text, listener ->
                    }

                    dialog.create()
                    dialog.setCancelable(false)
                    dialog.show()
                } else {

                    Account = etAccount.text.toString()
                    Mobile = "+91${etMobile.text}"
                    if (biller_id == 0) {
                        val dialog = AlertDialog.Builder(this@HomeActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Please select operator")
                        dialog.setNegativeButton("ok") { text, listener ->
                        }

                        dialog.create()
                        dialog.setCancelable(false)
                        dialog.show()
                    } else if (Account.length < 10 && biller_id == 1) {
                        val dialog = AlertDialog.Builder(this@HomeActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Please enter correct Account No")
                        dialog.setNegativeButton("ok") { text, listener ->
                        }

                        dialog.create()
                        dialog.setCancelable(false)
                        dialog.show()
                    } else if (Account.length < 12 && biller_id == 2) {
                        val dialog = AlertDialog.Builder(this@HomeActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Please enter correct Account No!")
                        dialog.setNegativeButton("ok") { text, listener ->
                        }

                        dialog.create()
                        dialog.setCancelable(false)
                        dialog.show()
                    } /*else if (etMobile.text.length<10) {
                        val dialog = AlertDialog.Builder(this@HomeActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Invalid Mobile Number!")
                        dialog.setNegativeButton("ok") { text, listener ->
                        }

                        dialog.create()
                        dialog.setCancelable(false)
                        dialog.show()
                    }*/
                    else {
                        progress = ProgressDialog(this)
                        progress.setMessage("Fetching Bill Details..")
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        progress.isIndeterminate = true
                        progress.setCancelable(false)
                        progress.show()
                        btnFetchBill.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        txtLoading.visibility = View.VISIBLE
                        val queue = Volley.newRequestQueue(this@HomeActivity)
                        //Toast.makeText(this, "${Mobile}", Toast.LENGTH_SHORT).show()

                        //Toast.makeText(this,"$link",Toast.LENGTH_LONG).show()
                        val url =
                            "$linkAPI/v2/bills/validation.php?api_key=12f4e4-164827-868329-2620eb-e4b21e&number=${Account}&amount=10&opid=${operatorCode}&order_id=478245232&opt1=opt1&opt2=opt2&opt3=opt3&opt4=opt4&opt5=opt5&opt6=opt6&opt7=opt7&opt8=Bills&opt9=opt9&opt10=opt10&mobile=7906025575"
                        val jsonRequest = object : JsonObjectRequest(
                            Method.GET, url, null,
                            Response.Listener {
                                //Toast.makeText(this, "${it}", Toast.LENGTH_SHORT).show()
                                var status = it.getString("status")
                                var message = it.getString("message")
                                customer_name = it.getString("customer_name")
                                due_date = it.getString("due_date")
                                bill_date = it.getString("bill_date")
                                due_amount = it.getString("due_amount")
                                operator = it.getString("provider")
                                if (status == "FAILED") {
                                    progress.dismiss()
                                    val dialog = AlertDialog.Builder(this@HomeActivity)
                                    dialog.setTitle("Error")
                                    dialog.setMessage(message)
                                    dialog.setNegativeButton("ok") { text, listener ->
                                    }

                                    dialog.create()
                                    dialog.setCancelable(false)
                                    dialog.show()
                                    btnFetchBill.visibility = View.VISIBLE
                                    txtCusNameT.visibility = View.GONE
                                    txtValueCustName.visibility = View.GONE
                                    txtBillDateT.visibility = View.GONE
                                    txtValueBillDate.visibility = View.GONE
                                    txtDueDateT.visibility = View.GONE
                                    txtValueDueDate.visibility = View.GONE
                                    txtAmountT.visibility = View.GONE
                                    txtValueAmount.visibility = View.GONE
                                    btnPayBill.visibility = View.GONE
                                    progressBar.visibility = View.GONE
                                    txtLoading.visibility = View.GONE
                                } else {
                                    progress.dismiss()
                                    btnFetchBill.visibility = View.GONE
                                    txtCusNameT.visibility = View.VISIBLE
                                    txtValueCustName.visibility = View.VISIBLE
                                    txtBillDateT.visibility = View.VISIBLE
                                    txtValueBillDate.visibility = View.VISIBLE
                                    txtDueDateT.visibility = View.VISIBLE
                                    txtValueDueDate.visibility = View.VISIBLE
                                    txtAmountT.visibility = View.VISIBLE
                                    txtValueAmount.visibility = View.VISIBLE
                                    btnPayBill.visibility = View.VISIBLE
                                    btnCancel.visibility = View.VISIBLE
                                    progressBar.visibility = View.GONE
                                    txtLoading.visibility = View.GONE
                                    txtCustomerName.text = customer_name
                                    txtBillDate.text = bill_date
                                    txtDueDate.text = due_date
                                    txtAmount.text = due_amount
                                }

                            },
                            Response.ErrorListener {
                                progress.dismiss()
                                //Toast.makeText(this, "${it}", Toast.LENGTH_SHORT).show()
                                val dialog = AlertDialog.Builder(this@HomeActivity)
                                dialog.setTitle("Error")
                                dialog.setMessage("Some Error Occurred.")
                                dialog.setNegativeButton("OK") { text, listener ->
                                }

                                dialog.create()
                                dialog.setCancelable(false)
                                dialog.show()
                                btnFetchBill.visibility = View.VISIBLE
                                txtCusNameT.visibility = View.GONE
                                txtValueCustName.visibility = View.GONE
                                txtBillDateT.visibility = View.GONE
                                txtValueBillDate.visibility = View.GONE
                                txtDueDateT.visibility = View.GONE
                                txtValueDueDate.visibility = View.GONE
                                txtAmountT.visibility = View.GONE
                                txtValueAmount.visibility = View.GONE
                                btnPayBill.visibility = View.GONE
                                progressBar.visibility = View.GONE
                                txtLoading.visibility = View.GONE
                            }
                        ) {

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


            }
            btnCancel.setOnClickListener {
                btnFetchBill.visibility = View.VISIBLE
                txtCusNameT.visibility = View.GONE
                txtValueCustName.visibility = View.GONE
                txtBillDateT.visibility = View.GONE
                txtValueBillDate.visibility = View.GONE
                txtDueDateT.visibility = View.GONE
                txtValueDueDate.visibility = View.GONE
                txtAmountT.visibility = View.GONE
                txtValueAmount.visibility = View.GONE
                btnPayBill.visibility = View.GONE
                progressBar.visibility = View.GONE
                txtLoading.visibility = View.GONE
                btnCancel.visibility = View.GONE

            }
            btnPayBill.setOnClickListener {
                if (!ConnectionManager().checkConnectivity(this)) {
                    val dialog = AlertDialog.Builder(this@HomeActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("No internet connection found. Please turn on your internet.")
                    dialog.setNegativeButton("OK") { text, listener ->
                    }

                    dialog.create()
                    dialog.setCancelable(false)
                    dialog.show()
                } else {
                    val progressLayout = findViewById<RelativeLayout>(R.id.LoadingLayout)
                    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val Df: DateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                    val currentDate = Df.format(Calendar.getInstance().time)
                    val queue = Volley.newRequestQueue(this@HomeActivity)
                    var tid = "JCC$currentDate"
                    var dateTime = df.format(Calendar.getInstance().time)
                    val dialog = AlertDialog.Builder(this@HomeActivity)
                    dialog.setTitle("Confirmation")
                    dialog.setMessage("Are you sure you want to submit?")
                    dialog.setNegativeButton("No") { text, listener ->
                    }
                    dialog.setPositiveButton("Yes") { text, listener ->
                        progressLayout.visibility = View.VISIBLE
                        btnPayBill.visibility = View.GONE
                        btnCancel.visibility = View.GONE
                        getBalance()
                        if (balance.toInt() < due_amount.toInt()) {
                            val dialog = AlertDialog.Builder(this@HomeActivity)
                            dialog.setTitle("Error")
                            dialog.setMessage("Insufficient Balance")
                            dialog.setNegativeButton("OK") { text, listener ->
                                btnFetchBill.visibility = View.GONE
                                txtCusNameT.visibility = View.VISIBLE
                                txtValueCustName.visibility = View.VISIBLE
                                txtBillDateT.visibility = View.VISIBLE
                                txtValueBillDate.visibility = View.VISIBLE
                                txtDueDateT.visibility = View.VISIBLE
                                txtValueDueDate.visibility = View.VISIBLE
                                txtAmountT.visibility = View.VISIBLE
                                txtValueAmount.visibility = View.VISIBLE
                                btnPayBill.visibility = View.VISIBLE
                                progressBar.visibility = View.GONE
                                btnCancel.visibility = View.VISIBLE
                                txtLoading.visibility = View.GONE
                                progressLayout.visibility = View.GONE
                            }

                            dialog.create()
                            dialog.setCancelable(false)
                            dialog.show()

                        } else {
                            var bal=balance
                            //Toast.makeText(this, "-one $balance", Toast.LENGTH_SHORT).show()
                            bal = ((bal.toInt()) - (due_amount.toInt())).toString()
                            //Toast.makeText(this, "zero $balance", Toast.LENGTH_SHORT).show()
                            val jsonParams = JSONObject()
                            /*jsonParams.put("account", Account)
                        jsonParams.put("amount", due_amount)
                        jsonParams.put("date", due_date)
                        jsonParams.put("mobile", Mobile)
                        jsonParams.put("status", "Unpaid")
                        jsonParams.put("paymentGateway", "")
                        jsonParams.put("tId", tid)
                        jsonParams.put("name",customer_name)
                        jsonParams.put("operator",operator)
                        jsonParams.put("paymentDate",dateTime)
                        jsonParams.put("billDate",bill_date)
                        jsonParams.put("shopName","Jafri Computer Centre")
                        jsonParams.put("balance",balance)*/
                            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            //balance=(balance.toInt()-due_amount.toInt()).toString()
                            val queue = Volley.newRequestQueue(this@HomeActivity)
                            //jsonParams.put("query","UPDATE `user_detail` SET `balance`='${balance}' WHERE username=${username}")
                            jsonParams.put(
                                "query",
                                "INSERT INTO `bill_details` (`Account`, `operatorName`, `amount`, `customerMobile`, `customerName`, `dueDate`, `billDate`, `paymentDate`, `Tid`, `shopName`, `shopMobile`, `status`, `balance`) VALUES ('$Account', '$operator', '$due_amount', '$Mobile', '$customer_name', '$due_date', '$bill_date', '$dateTime', '$tid', '$shopName', '$shopMobile', 'Unpaid', '$bal')"
                            )
                            val url =
                                "$link/dynamic/update"
                            val jsonRequest = object : JsonObjectRequest(
                                Method.POST, url, jsonParams,
                                Response.Listener {
                                    //Toast.makeText(this, "${it}", Toast.LENGTH_SHORT).show()
                                    Log.e("error", it.toString())
                                    if (it.toString() == "{\"fieldCount\":0,\"affectedRows\":1,\"insertId\":0,\"serverStatus\":2,\"warningCount\":0,\"message\":\"\",\"protocol41\":true,\"changedRows\":0}") {
                                        val queue = Volley.newRequestQueue(this@HomeActivity)
                                        //Toast.makeText(this, "first $balance", Toast.LENGTH_SHORT).show()
                                        //balance = (balance.toInt() - due_amount.toInt()).toString()
                                        jsonParams.put(
                                            "query",
                                            "UPDATE `user_detail` SET `balance`='$bal' WHERE username=$username"
                                        )
                                        val url =
                                            "$link/dynamic/update"
                                        val jsonRequest = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                                        object : JsonObjectRequest(
                                            Method.POST, url, jsonParams,
                                            Response.Listener {
                                                //Toast.makeText(this, "second $balance", Toast.LENGTH_SHORT).show()
                                                val message = it.getString("message")
                                                Log.e("balance_error", message)
                                                Log.e("balance_error", bal)
                                                if (message == "(Rows matched: 1  Changed: 1  Warnings: 0") {
                                                    revealLayoutFun()
                                                    Handler().postDelayed({
                                                    val intent =
                                                        Intent(this, bill_receipt::class.java)
                                                    intent.putExtra("name", customer_name)
                                                    intent.putExtra("account", Account)
                                                    intent.putExtra("dueDate", due_date)
                                                    intent.putExtra("billDate", bill_date)
                                                    intent.putExtra("amount", due_amount)
                                                    intent.putExtra("paymentDate", dateTime)
                                                    intent.putExtra("operator", operator)
                                                    intent.putExtra("Tid", tid)
                                                    intent.putExtra(
                                                        "shopName",
                                                        shopName
                                                    )
                                                    intent.putExtra("shopMobile", username)
                                                    //sendMessage()
                                                    startActivity(intent)},2000)
                                                } else {
                                                    //balance = (balance.toInt() + due_amount.toInt()).toString()
                                                    val queue = Volley.newRequestQueue(this@HomeActivity)
                                                    //Toast.makeText(this, "${balance}", Toast.LENGTH_SHORT).show()
                                                    //balance = (balance.toInt() - due_amount.toInt()).toString()
                                                    jsonParams.put(
                                                        "query",
                                                        "DELETE FROM `bill_details` WHERE Tid='$tid'"
                                                    )
                                                    val url =
                                                        "$link/dynamic/update"
                                                    val jsonRequest = object : JsonObjectRequest(
                                                        Method.POST, url, jsonParams,
                                                        Response.Listener {
                                                        },Response.ErrorListener {  })
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
                                                    val dialog =
                                                        AlertDialog.Builder(this@HomeActivity)
                                                    dialog.setTitle("Error")
                                                    dialog.setMessage("E1:Some error occurred")
                                                    dialog.setNegativeButton("OK") { text, listener ->
                                                        btnFetchBill.visibility = View.GONE
                                                        txtCusNameT.visibility = View.VISIBLE
                                                        txtValueCustName.visibility = View.VISIBLE
                                                        txtBillDateT.visibility = View.VISIBLE
                                                        txtValueBillDate.visibility = View.VISIBLE
                                                        txtDueDateT.visibility = View.VISIBLE
                                                        txtValueDueDate.visibility = View.VISIBLE
                                                        txtAmountT.visibility = View.VISIBLE
                                                        txtValueAmount.visibility = View.VISIBLE
                                                        btnPayBill.visibility = View.VISIBLE
                                                        progressBar.visibility = View.GONE
                                                        btnCancel.visibility = View.VISIBLE
                                                        txtLoading.visibility = View.GONE
                                                        progressLayout.visibility = View.GONE

                                                    }

                                                    dialog.create()
                                                    dialog.setCancelable(false)
                                                    dialog.show()
                                                }
                                            }, Response.ErrorListener {
                                                //balance = (balance.toInt() + due_amount.toInt()).toString()
                                                val dialog = AlertDialog.Builder(this@HomeActivity)
                                                dialog.setTitle("Error")
                                                dialog.setMessage("E2:Some error occurred")
                                                dialog.setNegativeButton("OK") { text, listener ->
                                                    btnFetchBill.visibility = View.GONE
                                                    txtCusNameT.visibility = View.VISIBLE
                                                    txtValueCustName.visibility = View.VISIBLE
                                                    txtBillDateT.visibility = View.VISIBLE
                                                    txtValueBillDate.visibility = View.VISIBLE
                                                    txtDueDateT.visibility = View.VISIBLE
                                                    txtValueDueDate.visibility = View.VISIBLE
                                                    txtAmountT.visibility = View.VISIBLE
                                                    txtValueAmount.visibility = View.VISIBLE
                                                    btnPayBill.visibility = View.VISIBLE
                                                    progressBar.visibility = View.GONE
                                                    btnCancel.visibility = View.VISIBLE
                                                    txtLoading.visibility = View.GONE
                                                    progressLayout.visibility = View.GONE
                                                }

                                                dialog.create()
                                                dialog.setCancelable(false)
                                                dialog.show()


                                                Log.e("Volley Error", "${it}")
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
                                },


                                Response.ErrorListener {
                                    //balance = (balance.toInt() + due_amount.toInt()).toString()
                                    val dialog = AlertDialog.Builder(this@HomeActivity)
                                    dialog.setTitle("Error")
                                    dialog.setMessage("E3:Some error occurred")
                                    dialog.setNegativeButton("OK") { text, listener ->
                                        btnFetchBill.visibility = View.GONE
                                        txtCusNameT.visibility = View.VISIBLE
                                        txtValueCustName.visibility = View.VISIBLE
                                        txtBillDateT.visibility = View.VISIBLE
                                        txtValueBillDate.visibility = View.VISIBLE
                                        txtDueDateT.visibility = View.VISIBLE
                                        txtValueDueDate.visibility = View.VISIBLE
                                        txtAmountT.visibility = View.VISIBLE
                                        txtValueAmount.visibility = View.VISIBLE
                                        btnPayBill.visibility = View.VISIBLE
                                        progressBar.visibility = View.GONE
                                        btnCancel.visibility = View.VISIBLE
                                        txtLoading.visibility = View.GONE
                                        progressLayout.visibility = View.GONE
                                    }

                                    dialog.create()
                                    dialog.setCancelable(false)
                                    dialog.show()


                                    Log.e("Volley Error", "${it}")
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


                    dialog.create()
                    dialog.setCancelable(false)
                    dialog.show()
                }
            }
        } else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@HomeActivity)
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
    //Mobile Recharge Module
    private fun mobileRecharge() {
        txtTitle.text="Mobile Recharge"

    }

    private fun sendMessage() {
        var df: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val currentDate = df.format(Calendar.getInstance().time)
        val sms: SmsManager = SmsManager.getDefault()
        var message="Bill Payment of $spinnerOperatorName having Account No ${Account} and Amount Rs.${due_amount} has been successful on ${currentDate}. Thank You"
        //Toast.makeText(this,"${CusMobile.length}",Toast.LENGTH_SHORT).show()
        if(Mobile.length==13){
            sms.sendTextMessage(Mobile, null, message, null, null)}
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
    fun getBalance(){
        if (ConnectionManager().checkConnectivity(this)) {
        imgGetBalance.visibility=View.GONE
        progressBalance.visibility=View.VISIBLE
        val queue = Volley.newRequestQueue(this@HomeActivity)
        val url =
            "$link/api/user_detail/findOne?_where=(username,eq,${username})"
        val jsonRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            Response.Listener {
                val response = it.toString()
                if (response != "[]") {
                    val array = it.getJSONObject(0)
                    balance = array.getString("balance")
                    progressBalance.visibility=View.GONE
                    imgGetBalance.visibility=View.VISIBLE
                    txtBalance.text="Balance: ${balance.toString()}"}
            },Response.ErrorListener {
                progressBalance.visibility=View.GONE
                imgGetBalance.visibility=View.VISIBLE
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
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@HomeActivity)
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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revealLayoutFun() {

        // based on the boolean value the
        // reveal layout should be toggled
        if (!isRevealed) {

            // get the right and bottom side
            // lengths of the reveal layout
            val x: Int = RlSuccess.right / 2
            val y: Int = RlSuccess.bottom / 2

            // here the starting radius of the reveal
            // layout is 0 when it is not visible
            val startRadius = 0

            // make the end radius should
            // match the while parent view
            val endRadius = Math.hypot(
                RlSuccess.width.toDouble(),
                RlSuccess.height.toDouble()
            ).toInt()

            // and set the background tint of the FAB to white
            // color so that it can be visible


            // create the instance of the ViewAnimationUtils to
            // initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                RlSuccess,
                x,
                y,
                startRadius.toFloat(),
                endRadius.toFloat()
            )

            // make the invisible reveal layout to visible
            // so that upon revealing it can be visible to user
            RlSuccess.visibility = View.VISIBLE
            // now start the reveal animation
            anim.start()

            // set the boolean value to true as the reveal
            // layout is visible to the user
            isRevealed = true

        } else {

            // get the right and bottom side lengths
            // of the reveal layout
            val x: Int = RlSuccess.right / 2
            val y: Int = RlSuccess.bottom / 2

            // here the starting radius of the reveal layout is its full width
            val startRadius: Int = Math.max(RlSuccess.width, RlSuccess.height)

            // and the end radius should be zero at this
            // point because the layout should be closed
            val endRadius = 0

            // now set the background tint of the FAB to green
            // so that it can be visible to the user


            // now again set the icon of the FAB to plus

            // create the instance of the ViewAnimationUtils
            // to initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                RlSuccess,
                x,
                y,
                startRadius.toFloat(),
                endRadius.toFloat()
            )

            // now as soon as the animation is ending, the reveal
            // layout should also be closed
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    RlSuccess.visibility = View.GONE
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })

            // start the closing animation
            anim.start()

            // set the boolean variable to false
            // as the reveal layout is invisible
            isRevealed = false
        }
    }
    }
