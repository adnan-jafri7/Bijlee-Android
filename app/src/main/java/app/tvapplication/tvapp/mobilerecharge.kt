package app.tvapplication.tvapp

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tvapplication.tvapp.R
import android.text.Editable

import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import android.content.Intent
import android.view.inputmethod.InputMethodManager

import android.content.res.ColorStateList
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonArrayRequest
import kotlinx.android.synthetic.main.activity_add_money.*
import kotlinx.android.synthetic.main.activity_mobilerecharge.*
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import android.animation.ObjectAnimator





class mobilerecharge : AppCompatActivity(),OnImageClickListener {
    var operatorCode = "0"
    var checkMob = false
    var circle=""
    var operator = ""
    var checkAmount = false
    var operator_id = 0
    var username = ""
    var shopName = ""
    var shopMobile = ""
    lateinit var btnProceed: Button
    lateinit var btnPlan: Button
    lateinit var rr: RelativeLayout
    lateinit var rl: RelativeLayout
    lateinit var txtMobileNo: TextView
    lateinit var txtOr: TextView
    lateinit var ll: LinearLayout
    lateinit var txtEditMob: ImageView
    lateinit var btnRecharge: Button
    lateinit var recyclerView: RecyclerView
    lateinit var recyclerView2: RecyclerView
    lateinit var txtValidating: TextView
    lateinit var imgValFailed: ImageView
    lateinit var progressbar: ProgressBar
    lateinit var progressBar6: ProgressBar
    lateinit var etMobileNo:EditText
    lateinit var etAmount:EditText
    var validity=""
    var desc=""

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobilerecharge)
        rr = findViewById(R.id.rr)
        btnRecharge = findViewById(R.id.btnRecharge)
        recyclerView2 = findViewById(R.id.recyclerView2)
        rl = findViewById(R.id.rl)
        txtOr = findViewById(R.id.txtOr)
        recyclerView = findViewById(R.id.recyclerView)
        txtMobileNo = findViewById(R.id.txtMobileNo)
        txtEditMob = findViewById(R.id.txtEditMob)
        imgValFailed = findViewById(R.id.imgValFailed)
        txtValidating = findViewById(R.id.txtValidating)
        progressbar = findViewById(R.id.progressbar)
        progressBar6 = findViewById(R.id.progressBar6)
        ll = findViewById(R.id.ll)
        etMobileNo= findViewById(R.id.etMobileNo)
        var spinnerOperatorName: Spinner = findViewById(R.id.spinnerOperatorName)
        var spinnerCircle: Spinner = findViewById(R.id.spinnerCircle)
        etAmount= findViewById(R.id.etAmount)
        var progressbar = findViewById<ProgressBar>(R.id.progressbar)
        btnProceed = findViewById(R.id.btnProceed)
        btnPlan = findViewById(R.id.btnPlan)
        recentRecharges()
        var sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        shopName = sharedPreferences.getString("name", "").toString()
        shopMobile = sharedPreferences.getString("mobile", "").toString()
        if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            val dialog = android.app.AlertDialog.Builder(this)
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
        ArrayAdapter.createFromResource(
            this,
            R.array.operatorName_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerOperatorName.adapter = adapter
            spinnerOperatorName.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // TODO Auto-generated method stub
                    var id = parent.getItemAtPosition(position)
                    operator_id = position //this would give you the id of the selected item
                    check()
                    when (operator_id) {
                        1 -> {
                            operatorCode = "8"
                            etAmount.isEnabled = true
                            btnPlan.setBackgroundColor(0xFF4CAF50.toInt())
                            btnPlan.isEnabled = true
                        }
                        2 -> {
                            operatorCode = "3"
                            etAmount.isEnabled = true
                            btnPlan.setBackgroundColor(0xFF4CAF50.toInt())
                            btnPlan.isEnabled = true
                        }
                        3 -> {
                            operatorCode = "1"
                            etAmount.isEnabled = true
                            btnPlan.setBackgroundColor(0xFF4CAF50.toInt())
                            btnPlan.isEnabled = true
                        }
                        4 -> {
                            operatorCode = "5"
                            etAmount.isEnabled = true
                            btnPlan.setBackgroundColor(0xFF4CAF50.toInt())
                            btnPlan.isEnabled = true
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        }

        etMobileNo.addTextChangedListener(object : TextWatcher {
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
                    //btnProceed.setBackgroundColor(0xFF4CAF50.toInt())
                    btnProceed.isEnabled = true
                    val colorFrom=Color.DKGRAY
                    val colorTo = Color.parseColor("#4CAF50")
                    val duration = 1000
                    ObjectAnimator.ofObject(btnProceed, "backgroundColor", ArgbEvaluator(), colorFrom,colorTo)
                        .setDuration(duration.toLong())
                        .start()
                    //checkMob=true
                    //check()
                    val inputManager: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(etMobileNo.windowToken, 0)
                } else {
                    btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
                    btnProceed.isEnabled = false
                    //checkMob=false
                    //check()
                    //etAmount.isEnabled=false
                    //btnPlan.setBackgroundColor(0xFFDDDDDD.toInt())
                    //btnPlan.isEnabled=false
                }
            }
        })
        btnProceed.setOnClickListener {
            txtRecentRecharges.visibility = View.GONE
            line.visibility = View.GONE
            recyclerView.visibility = View.GONE
            rr.visibility = View.GONE
            rl.visibility = View.VISIBLE
            txtMobileNo.text = etMobileNo.text
            var queue = Volley.newRequestQueue(this@mobilerecharge)
            val url =
                "$linkAPI/v2/operator_fetch.php?api_key=12f4e4-164827-868329-2620eb-e4b21e&number=${etMobileNo.text.toString()}"
            val jsonRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener {
                    //Toast.makeText(this@mobilerecharge,"$it",Toast.LENGTH_SHORT).show()
                    rr.visibility = View.VISIBLE
                    rl.visibility = View.GONE
                    txtMobileNo.visibility = View.VISIBLE
                    spinnerOperatorName.visibility = View.VISIBLE
                    etAmount.visibility = View.VISIBLE
                    btnPlan.visibility = View.VISIBLE
                    btnProceed.visibility = View.INVISIBLE
                    btnRecharge.visibility = View.VISIBLE
                    btnRecharge.isEnabled = true
                    txtOr.visibility = View.VISIBLE
                    ll.visibility = View.VISIBLE
                    etMobileNo.visibility = View.GONE
                    txtEditMob.visibility = View.VISIBLE
                    var data = it.getJSONObject("details")
                    operator = data.getString("operator")
                    circle = data.getString("Circle")
                    //Toast.makeText(this@mobilerecharge,"$operator",Toast.LENGTH_SHORT).show()
                    when (operator) {
                        "VODAFONE" -> {
                            spinnerOperatorName.setSelection(2)

                        }
                        "Reliance Jio Infocomm Limited" -> {
                            spinnerOperatorName.setSelection(1)

                        }
                        "BSNL GSM" -> {
                            spinnerOperatorName.setSelection(4)

                        }
                        "AIRTEL" -> {
                            spinnerOperatorName.setSelection(3)


                        }
                    }

                },
                Response.ErrorListener {
                    imgValFailed.visibility = View.VISIBLE
                    txtValidating.text = "Validation Failed."
                    progressbar.visibility = View.INVISIBLE
                    //Toast.makeText(this@mobilerecharge,"$it",Toast.LENGTH_SHORT).show()

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

        txtEditMob.setOnClickListener {
            txtMobileNo.visibility = View.GONE
            spinnerOperatorName.visibility = View.GONE
            etAmount.visibility = View.GONE
            btnPlan.visibility = View.GONE
            btnProceed.visibility = View.VISIBLE
            txtOr.visibility = View.GONE
            ll.visibility = View.GONE
            etMobileNo.visibility = View.VISIBLE
            txtEditMob.visibility = View.GONE
            btnRecharge.visibility = View.GONE
            progressBar6.visibility=View.GONE
            recyclerView2.visibility=View.GONE

        }
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
                if (!(s.isEmpty())) {
                    checkAmount = true
                    check()

                } else {
                    checkAmount = false
                    check()
                }
            }
        })

        btnRecharge.setOnClickListener {
            var mView = layoutInflater.inflate(R.layout.dialog, null)
            var txtMobile:TextView=mView.findViewById(R.id.txtMobile)
            var txtAmount:TextView=mView.findViewById(R.id.txtAmount)
            val btn_cancel = mView.findViewById(R.id.btn_cancel) as Button
            val btn_okay = mView.findViewById(R.id.btn_okay) as Button
            val dialog = android.app.AlertDialog.Builder(this)
            txtMobile.text=etMobileNo.text
            txtAmount.text="${resources.getString(R.string.Rs)}${etAmount.text}.00"
            dialog.setView(mView)
            val alertDialog = dialog.create()
            alertDialog.show()
            alertDialog.setCancelable(false)
            btn_okay.setOnClickListener {
                val df: DateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                val Df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val curDate = df.format(Calendar.getInstance().time)
                val rechargeDate = Df.format(Calendar.getInstance().time)
                var intent = Intent(this, rechargereceipt::class.java)
                intent.putExtra("MobileNo", "${etMobileNo.text}")
                intent.putExtra("operatorId", "$operatorCode")
                intent.putExtra("amount", "${etAmount.text}")
                intent.putExtra("orderId", "${curDate}")
                intent.putExtra("rechargeDate", rechargeDate)
                intent.putExtra("operatorName", operator)
                startActivity(intent)
                finish()
            }
            btn_cancel.setOnClickListener {
                alertDialog.dismiss()

            }


        }

        btnPlan.setOnClickListener {
            plans(operatorCode)


        }

    }

    fun check() {
        if (checkAmount && operator_id != 0) {
            btnRecharge.setBackgroundColor(0xFF4CAF50.toInt())
            btnRecharge.isEnabled = true
        } else {
            btnRecharge.setBackgroundColor(0xDDDDDDDD.toInt())
            btnRecharge.isEnabled = false

        }
    }

    fun recentRecharges() {
        var bill_list = arrayListOf<rc_data>()
        val queue = Volley.newRequestQueue(this)
        val url = "$link/dynamic/update"
        val jsonRequest = object : JsonArrayRequest(
            Method.POST,
            url,
            null,
            Response.Listener {
                progressBar6.visibility = View.GONE
                for (i in 0 until it.length()) {
                    val resObject = it.getJSONObject(i)
                    val rc = rc_data(
                        resObject.getString("mobile"),
                        resObject.getString("amount"),
                        resObject.getString("tid"),
                        resObject.getString("shopName"),
                        resObject.getString("shopMobile"),
                        resObject.getString("operatorName"),
                        resObject.getString("operatorId"),
                        resObject.getString("rechargeDate"),
                        resObject.getString("location"),
                        resObject.getString("status"),
                        resObject.getString("operatorRef"),
                        resObject.getString("balance")

                    )
                    bill_list.add(rc)
                }
                val layoutManager: RecyclerView.LayoutManager =
                    LinearLayoutManager(this)
                var recyclerAdapter2: recyclerAdapter2 =
                    recyclerAdapter2(this, bill_list)
                recyclerView.adapter = recyclerAdapter2
                recyclerView.layoutManager = layoutManager

            },
            Response.ErrorListener {
                Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
            }) {

            override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                val headers = java.util.HashMap<kotlin.String, kotlin.String>()
                headers["Content-type"] = "application/json"
                return headers

            }

            override fun getBody(): ByteArray? {
                val body =
                    "{\"query\":\"SELECT * FROM mob_recharge WHERE shopMobile=$shopMobile ORDER BY rechargeDate DESC\"}"
                return body.toByteArray()
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

    fun plans(operatorId:String) {
        progressBar6.visibility=View.VISIBLE
        var plan_list = arrayListOf<plans>()
        val queue = Volley.newRequestQueue(this)
        val url =
            "$linkAPI/v2/recharge_plans.php?api_key=12f4e4-164827-868329-2620eb-e4b21e&state_code=1&opid=$operatorId"
        val jsonRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try{
                    if(operatorCode=="8") {
                        var plans = it.getJSONObject("plans")
                        var FullTT = plans.getJSONArray("FULLTT")
                        var Topup = plans.getJSONArray("TOPUP")
                        var Data = plans.getJSONArray("DATA")
                        var Frc = plans.getJSONArray("FRC")
                        var JioPhone = plans.getJSONArray("JioPhone")
                        var Roaming = plans.getJSONArray("Romaing")
                        for (i in 0 until FullTT.length()) {
                            val resObject = FullTT.getJSONObject(i)
                            var data_plan = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan)
                        }
                        for (i in 0 until Topup.length()) {
                            val resObject = Topup.getJSONObject(i)
                            var data_plan2 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan2)
                        }
                        for (i in 0 until Data.length()) {
                            val resObject = Data.getJSONObject(i)
                            var data_plan3 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan3)
                        }
                        for (i in 0 until Frc.length()) {
                            val resObject = Frc.getJSONObject(i)
                            var data_plan4 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan4)
                        }
                        for (i in 0 until JioPhone.length()) {
                            val resObject = JioPhone.getJSONObject(i)
                            var data_plan5 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                "Jio Phone Plan: ${resObject.getString("desc")}"
                            )
                            plan_list.add(data_plan5)
                        }
                        progressBar6.visibility = View.GONE
                        recyclerView2.visibility = View.VISIBLE
                        val layoutManager: RecyclerView.LayoutManager =
                            LinearLayoutManager(this)
                        var recyclerAdapter3: recyclerAdapter3 =
                            recyclerAdapter3(this, plan_list, this)
                        recyclerView2.adapter = recyclerAdapter3
                        recyclerView2.layoutManager = layoutManager

                        Log.e("plans", "$FullTT")
                    }
                    else if(operatorCode=="1"||operatorCode=="3"){
                        var plans = it.getJSONObject("plans")
                        var FullTT = plans.getJSONArray("FULLTT")
                        var Topup = plans.getJSONArray("TOPUP")
                        var Data = plans.getJSONArray("DATA")
                        var Frc = plans.getJSONArray("STV")
                        var Roaming = plans.getJSONArray("Romaing")
                        for (i in 0 until FullTT.length()) {
                            val resObject = FullTT.getJSONObject(i)
                            var data_plan = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan)
                        }
                        for (i in 0 until Topup.length()) {
                            val resObject = Topup.getJSONObject(i)
                            var data_plan2 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan2)
                        }
                        for (i in 0 until Data.length()) {
                            val resObject = Data.getJSONObject(i)
                            var data_plan3 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan3)
                        }
                        for (i in 0 until Frc.length()) {
                            val resObject = Frc.getJSONObject(i)
                            var data_plan4 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan4)
                        }
                        for (i in 0 until Frc.length()) {
                            val resObject = Frc.getJSONObject(i)
                            var data_plan5 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan5)
                        }
                        for (i in 0 until Roaming.length()) {
                            val resObject = Roaming.getJSONObject(i)
                            var data_plan5 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan5)
                        }
                        progressBar6.visibility = View.GONE
                        recyclerView2.visibility = View.VISIBLE
                        val layoutManager: RecyclerView.LayoutManager =
                            LinearLayoutManager(this)
                        var recyclerAdapter3: recyclerAdapter3 =
                            recyclerAdapter3(this, plan_list, this)
                        recyclerView2.adapter = recyclerAdapter3
                        recyclerView2.layoutManager = layoutManager

                        Log.e("plans", "$it")

                    }

                    else if(operatorCode=="5"){
                        var plans = it.getJSONObject("plans")
                        var FullTT = plans.getJSONArray("SMS")
                        var Topup = plans.getJSONArray("TOPUP")
                        var Data = plans.getJSONArray("3G/4G")
                        var Frc = plans.getJSONArray("RATE CUTTER")
                        var Roaming = plans.getJSONArray("Romaing")
                        for (i in 0 until FullTT.length()) {
                            val resObject = FullTT.getJSONObject(i)
                            var data_plan = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan)
                        }
                        for (i in 0 until Topup.length()) {
                            val resObject = Topup.getJSONObject(i)
                            var data_plan2 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan2)
                        }
                        for (i in 0 until Data.length()) {
                            val resObject = Data.getJSONObject(i)
                            var data_plan3 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan3)
                        }
                        for (i in 0 until Frc.length()) {
                            val resObject = Frc.getJSONObject(i)
                            var data_plan4 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan4)
                        }
                        for (i in 0 until Frc.length()) {
                            val resObject = Frc.getJSONObject(i)
                            var data_plan5 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan5)
                        }
                        for (i in 0 until Roaming.length()) {
                            val resObject = Roaming.getJSONObject(i)
                            var data_plan5 = plans(
                                resObject.getString("rs"),
                                resObject.getString("validity"),
                                resObject.getString("desc")
                            )
                            plan_list.add(data_plan5)
                        }
                        progressBar6.visibility = View.GONE
                        recyclerView2.visibility = View.VISIBLE
                        val layoutManager: RecyclerView.LayoutManager =
                            LinearLayoutManager(this)
                        var recyclerAdapter3: recyclerAdapter3 =
                            recyclerAdapter3(this, plan_list, this)
                        recyclerView2.adapter = recyclerAdapter3
                        recyclerView2.layoutManager = layoutManager

                        Log.e("plans", "$it")



                    }
                }
                catch (e:JSONException){
                    Log.e("JSONException","$e")
                }


            },
            Response.ErrorListener {
                Log.e("Volley Error","$it")
                progressBar6.visibility=View.GONE
                Toast.makeText(this,"Some error occurred",Toast.LENGTH_SHORT).show()
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

    override fun onImageClick(imageData: String?) {
        //Toast.makeText(this,"$imageData",Toast.LENGTH_SHORT).show()
        etAmount.setText(imageData)

    }
}