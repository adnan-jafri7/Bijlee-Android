package app.tvapplication.tvapp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_home.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import android.R.string.no
import android.R.string.no
import android.R.string.no
import android.content.Intent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import app.tvapplication.billdetail.report_MainActivity
import com.tvapplication.tvapp.R
import org.w3c.dom.Text


class business_report : Fragment() {
    lateinit var txtToday:TextView
    lateinit var txtMonthly:TextView
    lateinit var txtUnpaid:TextView
    lateinit var txtPaid:TextView
    lateinit var txtUnpaidTotal:TextView
    lateinit var txtPaidTotal:TextView
    lateinit var txtTodayCount:TextView
    lateinit var txtMonthlyCount:TextView
    lateinit var txtUnpaidCount:TextView
    lateinit var txtPaidCount:TextView
    lateinit var txtUnpaidTotalCount:TextView
    lateinit var txtPaidTotalCount:TextView
    lateinit var cardTS:CardView
    lateinit var cardTMS:CardView
    lateinit var cardUBTM:CardView
    lateinit var cardPBTM:CardView
    lateinit var cardUB:CardView
    lateinit var cardPB:CardView
    var firstDate=""
    var dateTime=""
    var shopMobile=""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_business_report, container, false)

        var sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.preference_file),
            Context.MODE_PRIVATE
        )
        shopMobile = sharedPreferences.getString("username", "").toString()
        txtToday = view.findViewById<TextView>(R.id.txtTodaySale)
        txtTodayCount=view.findViewById(R.id.txtTodaySaleCount)
        txtMonthly = view.findViewById<TextView>(R.id.txtMonthlySale)
        txtMonthlyCount=view.findViewById(R.id.txtMonthlySaleCount)
        txtUnpaid=view.findViewById(R.id.txtUnpaid)
        txtUnpaidCount=view.findViewById(R.id.txtUnpaidCount)
        txtPaid=view.findViewById(R.id.txtPaid)
        txtPaidCount=view.findViewById(R.id.txtPaidCount)
        txtPaidTotal=view.findViewById(R.id.txtPaidTotal)
        txtPaidTotalCount=view.findViewById(R.id.txtPaidTotalCount)
        txtUnpaidTotal=view.findViewById(R.id.txtUnpaidTotal)
        txtUnpaidTotalCount=view.findViewById(R.id.txtUnpaidTotalCount)
        cardTS=view.findViewById(R.id.cardTS)
        cardTMS=view.findViewById(R.id.cardTMS)
        cardUBTM=view.findViewById(R.id.cardUBTM)
        cardPBTM=view.findViewById(R.id.cardPBTM)
        cardUB=view.findViewById(R.id.cardUB)
        cardPB=view.findViewById(R.id.cardPB)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateTime = df.format(Calendar.getInstance().time)
        var cal: Calendar = Calendar.getInstance()
        val dt = Date()
        cal.time = dt
        val days = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, days)
        firstDate= df.format(cal.time)
        //Toast.makeText(context,"$firstDate",Toast.LENGTH_SHORT).show()
        todaySale()
        monthlySale()
        monthlyPaid()
        monthlyUnpaid()
        TotalPaid()
        TotalUnPaid()

        cardUB.setOnClickListener {
            Toast.makeText(context,"Total Unpaid Bills",Toast.LENGTH_SHORT).show()
            val intent = Intent(
                requireActivity().baseContext,
                report_MainActivity::class.java
            )
            intent.putExtra("message", "UB")
            requireActivity().startActivity(intent)
        }
        return view
    }

    //method to get value of Today's Sale
    fun todaySale() {
            val queue = Volley.newRequestQueue(activity as Context)
            var url = "$link/dynamic"
            val jsonObjectRequest = object : JsonArrayRequest(
                Method.POST,
                url,
                null,
                Response.Listener {
                    for (i in 0 until it.length()) {
                        val resObject = it.getJSONObject(i)
                        var amount = resObject.getString("total")
                        var count=resObject.getString("count")
                        if(amount=="null"){
                            txtToday.text="${resources.getString(R.string.Rs)}00.00"
                            txtTodayCount.text="00"
                        }
                        else{
                        txtToday.text = "${resources.getString(R.string.Rs)}${amount.toString()}.00"
                        txtTodayCount.text=count.toString()
                        }
                    }
                },
                Response.ErrorListener { }) {

                /*Send the headers using the below method*/
                override fun getParams(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    //params["query"] = "SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile"
                    return params
                }

                override fun getBody(): ByteArray? {
                    val body =
                        "{\"query\":\"SELECT (SELECT SUM(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$dateTime' AND '$dateTime' AND shopMobile='$shopMobile') as total, (SELECT COUNT(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$dateTime' AND '$dateTime' AND shopMobile='$shopMobile') as count\"}"
                    return body.toByteArray()
                }
            }
            queue.add(jsonObjectRequest)

        }

    //method to get value of Monthly Sale
    fun monthlySale() {
        val queue = Volley.newRequestQueue(activity as Context)
        var url = "$link/dynamic"
        val jsonObjectRequest = object : JsonArrayRequest(
            Method.POST,
            url,
            null,
            Response.Listener {
                for (i in 0 until it.length()) {
                    val resObject = it.getJSONObject(i)
                    var amount = resObject.getString("total")
                    var count=resObject.getString("count")
                    if(amount=="null"){
                        txtMonthly.text="${resources.getString(R.string.Rs)}00.00"
                        txtMonthlyCount.text="00"
                    }
                    else{
                        txtMonthly.text = "${resources.getString(R.string.Rs)}${amount.toString()}.00"
                        txtMonthlyCount.text=count.toString()
                    }
                }
            },
            Response.ErrorListener { }) {

            /*Send the headers using the below method*/
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                //params["query"] = "SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile"
                //params.put("2ndParamName","valueoF2ndParam");
                return params
            }

            override fun getBody(): ByteArray? {
                val body =
                    "{\"query\":\"SELECT (SELECT SUM(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$firstDate' AND '$dateTime' AND status<>'credit') as total, (SELECT COUNT(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$firstDate' AND '$dateTime' AND status<>'credit') as count\"}"
                return body.toByteArray()
            }
        }
        queue.add(jsonObjectRequest)

    }

    //method to get value of Unpaid Bills of current Month
    fun monthlyUnpaid() {
        val queue = Volley.newRequestQueue(activity as Context)
        var url = "$link/dynamic"
        val jsonObjectRequest = object : JsonArrayRequest(
            Method.POST,
            url,
            null,
            Response.Listener {
                for (i in 0 until it.length()) {
                    val resObject = it.getJSONObject(i)
                    var amount = resObject.getString("total")
                    var count=resObject.getString("count")
                    if(amount=="null"){
                        txtUnpaid.text="${resources.getString(R.string.Rs)}00.00"
                        txtUnpaidCount.text="00"
                    }
                    else{
                        txtUnpaid.text = "${resources.getString(R.string.Rs)}${amount.toString()}.00"}
                        txtUnpaidCount.text=count.toString()
                }
            },
            Response.ErrorListener { }) {

            /*Send the headers using the below method*/
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                //params["query"] = "SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile"
                //params.put("2ndParamName","valueoF2ndParam");
                return params
            }

            override fun getBody(): ByteArray? {
                val body =
                    "{\"query\":\"SELECT (SELECT SUM(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$firstDate' AND '$dateTime' AND status='Unpaid') as total, (SELECT COUNT(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$firstDate' AND '$dateTime' AND status='Unpaid') as count\"}"
                return body.toByteArray()
            }
        }
        queue.add(jsonObjectRequest)

    }


    ////method to get value of Paid Bills of current Month
    fun monthlyPaid() {
        val queue = Volley.newRequestQueue(activity as Context)
        var url = "$link/dynamic"
        val jsonObjectRequest = object : JsonArrayRequest(
            Method.POST,
            url,
            null,
            Response.Listener {
                for (i in 0 until it.length()) {
                    val resObject = it.getJSONObject(i)
                    var amount = resObject.getString("total")
                    var count=resObject.getString("count")
                    if(amount=="null"){
                        txtPaid.text="${resources.getString(R.string.Rs)}00.00"
                        txtPaidCount.text="00"
                    }
                    else{
                        txtPaid.text = "${resources.getString(R.string.Rs)}${amount.toString()}.00"
                        txtPaidCount.text=count.toString()

                    }
                }
            },
            Response.ErrorListener { }) {

            /*Send the headers using the below method*/
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                //params["query"] = "SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile"
                //params.put("2ndParamName","valueoF2ndParam");
                return params
            }

            override fun getBody(): ByteArray? {
                val body =
                    "{\"query\":\"SELECT (SELECT SUM(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$firstDate' AND '$dateTime' AND status='paid' AND status<>'credit') as total, (SELECT COUNT(amount) FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$firstDate' AND '$dateTime' AND status='paid') as count\"}"
                return body.toByteArray()
            }
        }
        queue.add(jsonObjectRequest)

    }

    //method to get value of Total Paid Bills
    fun TotalPaid() {
        val queue = Volley.newRequestQueue(activity as Context)
        var url = "$link/dynamic"
        val jsonObjectRequest = object : JsonArrayRequest(
            Method.POST,
            url,
            null,
            Response.Listener {
                for (i in 0 until it.length()) {
                    val resObject = it.getJSONObject(i)
                    var amount = resObject.getString("total")
                    var count=resObject.getString("count")
                    if(amount=="null"){
                        txtPaidTotal.text="${resources.getString(R.string.Rs)}00.00"
                        txtPaidTotalCount.text="00"
                    }
                    else{
                        txtPaidTotal.text = "${resources.getString(R.string.Rs)}${amount.toString()}.00"
                        txtPaidTotalCount.text=count.toString()
                    }
                }
            },
            Response.ErrorListener { }) {

            /*Send the headers using the below method*/
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                //params["query"] = "SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile"
                //params.put("2ndParamName","valueoF2ndParam");
                return params
            }

            override fun getBody(): ByteArray? {
                val body =
                    "{\"query\":\"SELECT (SELECT SUM(amount) FROM bill_details WHERE status='Paid') as total,(SELECT COUNT(amount) from bill_details WHERE status='Paid') as count\"}"
                return body.toByteArray()
            }
        }
        queue.add(jsonObjectRequest)

    }


    fun TotalUnPaid() {
        val queue = Volley.newRequestQueue(activity as Context)
        var url = "$link/dynamic"
        val jsonObjectRequest = object : JsonArrayRequest(
            Method.POST,
            url,
            null,
            Response.Listener {
                for (i in 0 until it.length()) {
                    val resObject = it.getJSONObject(i)
                    var amount = resObject.getString("total")
                    var count=resObject.getString("count")
                    if(amount=="null"){
                        txtUnpaidTotal.text="${resources.getString(R.string.Rs)}00.00"
                        txtUnpaidTotalCount.text="00"
                    }
                    else{
                        txtUnpaidTotal.text = "${resources.getString(R.string.Rs)}${amount.toString()}.00"}
                        txtUnpaidTotalCount.text=count.toString()
                }
            },
            Response.ErrorListener { }) {

            /*Send the headers using the below method*/
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                //params["query"] = "SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile"
                //params.put("2ndParamName","valueoF2ndParam");
                return params
            }

            override fun getBody(): ByteArray? {
                val body =
                    "{\"query\":\"SELECT (SELECT SUM(amount) FROM bill_details WHERE status='Unpaid') as total,(SELECT COUNT(amount) from bill_details WHERE status='Unpaid') as count\"}"
                return body.toByteArray()
            }
        }
        queue.add(jsonObjectRequest)

    }



}