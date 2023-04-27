package app.tvapplication.tvapp

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Color.BLACK
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_rechargereceipt.*
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Math.hypot
import java.lang.Math.max
import java.util.HashMap

class rechargereceipt : AppCompatActivity() {
    var MobileNo: String? = ""
    var amount:String? = ""
    var operatorCode: String? = ""
    var operatorRef: String? = ""
    var orderId: String? = ""
    var balance = ""
    var username = ""
    var shopName = ""
    var shopMobile = ""
    var operatorName:String?=""
    var rechargeDate:String?=""
    private var isRevealed = false
    lateinit var progressBar1:ProgressBar
    lateinit var progressBar2:ProgressBar
    lateinit var progressBar3:ProgressBar
    lateinit var txtAuth:TextView
    lateinit var txtProRec:TextView
    lateinit var imgCheck1:ImageView
    lateinit var imgCheck2:ImageView
    lateinit var txtTitleBillSuccess: TextView
    lateinit var txtAmountValue: TextView
    lateinit var txtMobileNoValue: TextView
    lateinit var txtOrderIdValue: TextView
    lateinit var txtOptRefValue: TextView
    lateinit var txtShopName: TextView
    lateinit var txtPaymentDateValue:TextView
    lateinit var txtShopMobile: TextView
    lateinit var txtTidValue:TextView
    lateinit var statusLayout:RelativeLayout
    lateinit var imgFailed:ImageView
    lateinit var txtStatus2:TextView
    lateinit var txtAlert:TextView
    lateinit var btnDone:Button
    lateinit var RlFull:RelativeLayout
    var chargedAmount:Double=0.000

    override fun onCreate(savedInstanceState: Bundle?) {
        var sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        shopName = sharedPreferences.getString("name", "").toString()
        shopMobile = sharedPreferences.getString("mobile", "").toString()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rechargereceipt)
        var scrollView: ScrollView = findViewById(R.id.scrollView)
        txtAlert=findViewById(R.id.txtAlert)
        statusLayout=findViewById(R.id.statusLayout)
        txtTitleBillSuccess = findViewById(R.id.txtTitleBillSuccess)
        txtPaymentDateValue=findViewById(R.id.txtPaymentDateValue)
        txtTidValue=findViewById(R.id.txtTidValue)
        txtStatus2=findViewById(R.id.txtStatus2)
        RlFull=findViewById(R.id.RlFull)
        var imgCheck: ImageView = findViewById(R.id.imgCheck)
        imgFailed=findViewById(R.id.imgFailed)
        txtAmountValue= findViewById(R.id.txtAmountValue)
        txtMobileNoValue= findViewById(R.id.txtAccountNoValue)
        txtOrderIdValue = findViewById(R.id.txtOrderIdValue)
        txtOptRefValue = findViewById(R.id.txtOptRefValue)
        txtShopName = findViewById(R.id.txtShopNameValue)
        txtShopMobile = findViewById(R.id.txtShopMobileValue)
        btnDone=findViewById(R.id.btnDone)
        var progressLayout: RelativeLayout = findViewById(R.id.progressLayout)
        progressBar1= findViewById(R.id.progressBar1)
        progressBar2= findViewById(R.id.progressBar2)
        progressBar3= findViewById(R.id.progressBar3)
        txtAuth = findViewById(R.id.txtAuth)
        txtProRec = findViewById(R.id.txtProRec)
        imgCheck1 = findViewById(R.id.imgCheck1)
        imgCheck2 = findViewById(R.id.imgCheck2)

        MobileNo = intent.getStringExtra("MobileNo")
        operatorCode = intent.getStringExtra("operatorId")
        amount = intent.getStringExtra("amount")
        orderId = intent.getStringExtra("orderId")
        operatorName=intent.getStringExtra("operatorName")
        rechargeDate=intent.getStringExtra("rechargeDate")
        btnDone.setOnClickListener {
            finishAffinity()
            intent= Intent(this,dashboard::class.java)
            startActivity(intent)
        }

        if (ConnectionManager().checkConnectivity(this)) {
            txtAlert.visibility=View.VISIBLE
            val queue = Volley.newRequestQueue(this@rechargereceipt)
            val url =
                "$link/api/user_detail/findOne?_where=(username,eq,${username})"
            val jsonRequest = object : JsonArrayRequest(
                Method.GET, url, null,
                Response.Listener {
                    val response = it.toString()
                    if (response != "[]") {
                        val array = it.getJSONObject(0)
                        balance = array.getString("balance")
                        //Toast.makeText(this, "Balance $balance", Toast.LENGTH_SHORT).show()
                        if (balance.toInt() < amount?.toInt()!!) {
                            progressBar1.visibility=View.GONE
                            progressBar3.visibility=View.GONE
                            txtAuth.text = "Insufficient Balance"
                            txtStatus2.text="Recharge Failed."
                            txtStatus2.visibility=View.VISIBLE
                            imgFailed.visibility=View.VISIBLE
                            imgCheck1.setImageResource(R.drawable.close)
                            imgCheck1.visibility=View.VISIBLE
                            txtAlert.visibility=View.GONE
                            btnDone.visibility=View.VISIBLE

                        }
                        else{
                            recharging()
                        }

                    }
                }, Response.ErrorListener {
                    progressBar1.visibility=View.GONE
                    txtAuth.text = "Some Error Occurred"
                    imgCheck1.setImageResource(R.drawable.close)
                    imgCheck1.visibility=View.VISIBLE
                    txtAlert.visibility=View.GONE
                    btnDone.visibility=View.VISIBLE
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
        else{
            progressBar1.visibility=View.GONE
            progressBar3.visibility=View.GONE
            txtStatus2.text="Recharge Failed."
            txtStatus2.visibility=View.VISIBLE
            imgFailed.visibility=View.VISIBLE
            imgCheck1.setImageResource(R.drawable.close)
            imgCheck1.visibility=View.VISIBLE
            txtAuth.text="Some Error Occurred."
            txtAlert.visibility=View.GONE
            btnDone.visibility=View.VISIBLE
        }

    }

    fun recharging(){
        //chargedAmount=(((3* (amount?.toDouble() ?:))/100).toDouble())
        Log.e("chargedAmount","$chargedAmount")
        val queue=Volley.newRequestQueue(this)
        val jsonParams=JSONObject()
        balance=(balance.toInt()- amount?.toInt()!!).toString()
        Log.e("balance","$balance")
        //Toast.makeText(this, "first $balance", Toast.LENGTH_SHORT).show()
        jsonParams.put(
            "query",
            "UPDATE `user_detail` SET `balance`='$balance' WHERE username=$username"
        )
        val url =
            "$link/dynamic/update"
        val jsonRequest = object : JsonObjectRequest(
            Method.POST, url, jsonParams,
            Response.Listener {
                val message = it.getString("message")
                if (message == "(Rows matched: 1  Changed: 1  Warnings: 0") {
                    val queue = Volley.newRequestQueue(this@rechargereceipt)
                    //jsonParams.put("query","UPDATE `user_detail` SET `balance`='${balance}' WHERE username=${username}")
                    jsonParams.put(
                        "query",
                        "INSERT INTO `mob_recharge` (`mobile`, `amount`, `tid`, `shopName`, `shopMobile`, `operatorName`, `operatorId`, `rechargeDate`, `location`, `status`, `operatorRef`, `balance`) VALUES ('$MobileNo', '$amount', '$orderId', '$shopName', '$shopMobile', '$operatorName', '$operatorCode', '$rechargeDate', '', 'Pending', '', '$balance')"
                    )
                    val url =
                        "$link/dynamic/update"
                    val jsonRequest = object : JsonObjectRequest(
                        Method.POST, url, jsonParams,
                        Response.Listener {

                            if (it.toString() == "{\"fieldCount\":0,\"affectedRows\":1,\"insertId\":0,\"serverStatus\":2,\"warningCount\":0,\"message\":\"\",\"protocol41\":true,\"changedRows\":0}") {
                                progressBar1.visibility=View.GONE
                                imgCheck1.setImageResource(R.drawable.check)
                                txtAuth.setTextColor(0xDDDDDDDD.toInt())
                                txtProRec.setTextColor(BLACK)
                                imgCheck1.visibility=View.VISIBLE
                                progressBar2.visibility=View.VISIBLE

                                val queue = Volley.newRequestQueue(this@rechargereceipt)
                                val url="$linkAPI/v2/recharge.php?api_key=12f4e4-164827-868329-2620eb-e4b21e&number=$MobileNo&amount=$amount&opid=$operatorCode&state_code=0&order_id=$orderId"
                                val jsonRequest = object : JsonObjectRequest(
                                    Method.POST, url, jsonParams,
                                    Response.Listener {
                                        //Toast.makeText(this,"$it",Toast.LENGTH_SHORT).show()
                                                      var status=it.getString("status")
                                       // Toast.makeText(this,"$status",Toast.LENGTH_SHORT).show()
                                        if(status=="SUCCESS"){
                                            val opr_id=it.getString("opr_id")
                                            success(opr_id)
                                        }
                                        else if(status=="PENDING"||status=="FAILED"||status=="REVERSAL"){
                                            pending()
                                        }

                                        /*else if(status=="FAILED"){
                                            failed()
                                        }*/

                                    },
                                    Response.ErrorListener {
                                        progressBar2.visibility=View.GONE
                                        progressBar3.visibility=View.GONE
                                        txtProRec.text = "Some Error Occurred."
                                        txtStatus2.text="Recharge Failed."
                                        txtStatus2.visibility=View.VISIBLE
                                        imgFailed.visibility=View.VISIBLE
                                        imgCheck2.setImageResource(R.drawable.close)
                                        imgCheck2.visibility=View.VISIBLE
                                        txtAlert.visibility=View.GONE
                                        btnDone.visibility=View.VISIBLE
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
                            progressBar2.visibility=View.GONE
                            progressBar3.visibility=View.GONE
                            txtProRec.text = "Some Error Occurred."
                            txtStatus2.text="Recharge Failed."
                            txtStatus2.visibility=View.VISIBLE
                            imgFailed.visibility=View.VISIBLE
                            imgCheck2.setImageResource(R.drawable.close)
                            imgCheck2.visibility=View.VISIBLE
                            txtAlert.visibility=View.GONE
                            btnDone.visibility=View.VISIBLE
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
                else{
                    progressBar1.visibility=View.GONE
                    progressBar3.visibility=View.GONE
                    txtAuth.text = "Some Error Occurred."
                    txtStatus2.text="Recharge Failed"
                    txtStatus2.visibility=View.VISIBLE
                    imgFailed.visibility=View.VISIBLE
                    imgCheck1.setImageResource(R.drawable.close)
                    imgCheck1.visibility=View.VISIBLE
                    txtAlert.visibility=View.GONE
                    btnDone.visibility=View.VISIBLE
                }

            },
            Response.ErrorListener {
                progressBar2.visibility=View.GONE
                progressBar3.visibility=View.GONE
                txtProRec.text = "Some Error Occurred."
                txtStatus2.text="Recharge Failed."
                txtStatus2.visibility=View.VISIBLE
                imgFailed.visibility=View.VISIBLE
                imgCheck2.setImageResource(R.drawable.close)
                imgCheck2.visibility=View.VISIBLE
                txtAlert.visibility=View.GONE
                btnDone.visibility=View.VISIBLE
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

    fun pending(){
        progressBar3.visibility=View.GONE
        imgFailed.setImageResource(R.drawable.pending)
        imgFailed.visibility=View.VISIBLE
        txtStatus2.visibility=View.VISIBLE
        txtStatus2.text="Recharge Pending"
        val queue = Volley.newRequestQueue(this@rechargereceipt)
        val url="$linkAPI/v2/status.php?api_key=12f4e4-164827-868329-2620eb-e4b21e&order_id=$orderId"
        val jsonRequest = object : JsonObjectRequest(
            Method.GET, url,null,
            Response.Listener {try {
                var status = it.getJSONObject("response").getString("status")
                if (status == "PENDING") {
                    txtProRec.text="Recharging Pending"
                    progressBar2.visibility=View.GONE
                    imgCheck2.setImageResource(R.drawable.pending)
                    imgCheck2.visibility=View.VISIBLE
                    btnDone.visibility=View.VISIBLE
                } else if (status == "SUCCESS") {
                    txtProRec.text = "Recharge Success."
                    val opr_id = it.getString("opr_id")
                    success(opr_id)
                } /*else if ((status == "FAILED")||(status=="REVERSAL")) {
                    failed()
                }*/
            }
            catch (e:JSONException){
                //Toast.makeText(this,"$e",Toast.LENGTH_SHORT).show()
                Log.e("JSONException",e.toString())
            }

            },
            Response.ErrorListener {
                progressBar2.visibility=View.GONE
                progressBar3.visibility=View.GONE
                txtProRec.text = "Some Error Occurred."
                txtStatus2.text="Recharge Failed."
                txtStatus2.visibility=View.VISIBLE
                imgFailed.visibility=View.VISIBLE
                imgCheck2.setImageResource(R.drawable.close)
                imgCheck2.visibility=View.VISIBLE
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
        //queue.add(jsonRequest)
    }

    fun failed(){
        //Toast.makeText(this,"$status",Toast.LENGTH_SHORT).show()
        balance=(balance.toInt()+ amount?.toInt()!!).toString()
        val queue=Volley.newRequestQueue(this)
        val jsonParams=JSONObject()
        jsonParams.put(
            "query",
            "UPDATE `user_detail` SET `balance`='$balance' WHERE username=$username"
        )
        val url =
            "$link/dynamic/update"
        val jsonRequest = object : JsonObjectRequest(
            Method.POST, url, jsonParams,
            Response.Listener {
                val message = it.getString("message")
                if (message == "(Rows matched: 1  Changed: 1  Warnings: 0") {
                    val queue=Volley.newRequestQueue(this)
                    val jsonParams=JSONObject()
                    jsonParams.put("query","UPDATE `mob_recharge` SET status='FAILED', balance='$balance' WHERE shopMobile='$shopMobile' AND tid='$orderId'")
                    val url2="$link/dynamic/update"
                    val jsonRequest = object : JsonObjectRequest(
                        Method.POST, url2, jsonParams,
                        Response.Listener {
                            progressBar3.visibility=View.GONE
                            txtAlert.visibility=View.GONE
                            imgFailed.setImageResource(R.drawable.failed)
                            imgFailed.visibility=View.VISIBLE
                            txtStatus2.visibility=View.VISIBLE
                            txtStatus2.text="RECHARGE FAILED"
                            txtProRec.text="Amount Refunded."
                            progressBar2.visibility=View.GONE
                            imgCheck2.visibility=View.VISIBLE
                            imgCheck2.setImageResource(R.drawable.close)
                            btnDone.visibility=View.VISIBLE

                        },
                    Response.ErrorListener {
                        progressBar2.visibility=View.GONE
                        progressBar3.visibility=View.GONE
                        txtProRec.text = "Some Error Occurred."
                        txtStatus2.text="Recharge Failed."
                        txtStatus2.visibility=View.VISIBLE
                        imgFailed.visibility=View.VISIBLE
                        imgCheck2.setImageResource(R.drawable.close)
                        imgCheck2.visibility=View.VISIBLE
                        txtAlert.visibility=View.GONE
                        btnDone.visibility=View.VISIBLE
                    })
                    {

                        override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                            val headers = java.util.HashMap<kotlin.String, kotlin.String>()
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
            progressBar2.visibility=View.GONE
            progressBar3.visibility=View.GONE
            txtProRec.text = "Some Error Occurred."
            txtStatus2.text="Recharge Failed."
            txtStatus2.visibility=View.VISIBLE
            imgFailed.visibility=View.VISIBLE
            imgCheck2.setImageResource(R.drawable.close)
            imgCheck2.visibility=View.VISIBLE
            txtAlert.visibility=View.GONE
            btnDone.visibility=View.VISIBLE
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

    fun success(opr_id:String){
        txtProRec.text="Recharge Success."
        operatorRef=opr_id
        val queue=Volley.newRequestQueue(this)
        val jsonParams=JSONObject()
        jsonParams.put("query","UPDATE `mob_recharge` SET operatorRef='$operatorRef', status='Success' WHERE shopMobile='$shopMobile' AND tid='$orderId'")
        val url2="$link/dynamic/update"
        val jsonRequest = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : JsonObjectRequest(
            Method.POST, url2, jsonParams,
            Response.Listener {
                Log.e("response","$it")
                //Toast.makeText(this,"$it",Toast.LENGTH_SHORT).show()
                val message = it.getString("message")
                if (message == "(Rows matched: 1  Changed: 1  Warnings: 0") {
                    progressBar3.visibility=View.GONE
                    txtProRec.text="Recharge Success."
                    progressBar2.visibility=View.INVISIBLE
                    imgCheck2.visibility=View.VISIBLE
                    progressLayout.visibility=View.INVISIBLE
                    statusLayout.visibility=View.VISIBLE
                    imgSuccess.visibility=View.VISIBLE
                    txtStatus2.text="Recharge Success."
                    txtStatus2.visibility=View.VISIBLE
                    txtAlert.visibility=View.GONE
                    revealLayoutFun()
                    Handler().postDelayed({
                        RlFull.visibility=View.GONE
                        txtTitleBillSuccess.text="Recharge of $operatorName has been successfully processed."
                        txtMobileNoValue.text=MobileNo
                        txtAmountValue.text="${resources.getString(R.string.Rs)}$amount.00"
                        txtOrderIdValue.text=orderId
                        txtOptRefValue.text=operatorRef
                        txtPaymentDateValue.text=rechargeDate
                        txtTidValue.text=orderId
                        txtShopName.text=shopName
                        txtShopMobile.text=shopMobile
                        progressLayout.visibility=View.INVISIBLE
                        statusLayout.visibility=View.INVISIBLE
                        scrollView.visibility=View.VISIBLE
                        btnDone.visibility=View.VISIBLE

                    },3000)



                }

            },
            Response.ErrorListener {
                progressBar2.visibility=View.GONE
                progressBar3.visibility=View.GONE
                txtProRec.text = "Some Error Occurred."
                txtStatus2.text="Recharge Failed."
                txtStatus2.visibility=View.VISIBLE
                imgFailed.visibility=View.VISIBLE
                imgCheck2.setImageResource(R.drawable.close)
                imgCheck2.visibility=View.VISIBLE
                txtAlert.visibility=View.GONE
                btnDone.visibility=View.VISIBLE
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

    override fun onBackPressed() {
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revealLayoutFun() {

        // based on the boolean value the
        // reveal layout should be toggled
        if (!isRevealed) {

            // get the right and bottom side
            // lengths of the reveal layout
            val x: Int = RlFull.right / 2
            val y: Int = RlFull.bottom / 2

            // here the starting radius of the reveal
            // layout is 0 when it is not visible
            val startRadius = 0

            // make the end radius should
            // match the while parent view
            val endRadius = hypot(
                RlFull.width.toDouble(),
                RlFull.height.toDouble()
            ).toInt()

            // and set the background tint of the FAB to white
            // color so that it can be visible


            // create the instance of the ViewAnimationUtils to
            // initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                RlFull,
                x,
                y,
                startRadius.toFloat(),
                endRadius.toFloat()
            )

            // make the invisible reveal layout to visible
            // so that upon revealing it can be visible to user
            RlFull.visibility = View.VISIBLE
            // now start the reveal animation
            anim.start()

            // set the boolean value to true as the reveal
            // layout is visible to the user
            isRevealed = true

        } else {

            // get the right and bottom side lengths
            // of the reveal layout
            val x: Int = RlFull.right / 2
            val y: Int = RlFull.bottom / 2

            // here the starting radius of the reveal layout is its full width
            val startRadius: Int = max(RlFull.width, RlFull.height)

            // and the end radius should be zero at this
            // point because the layout should be closed
            val endRadius = 0

            // now set the background tint of the FAB to green
            // so that it can be visible to the user


            // now again set the icon of the FAB to plus

            // create the instance of the ViewAnimationUtils
            // to initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                RlFull,
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
                    RlFull.visibility = View.GONE
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