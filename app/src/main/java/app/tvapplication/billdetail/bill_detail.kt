package app.tvapplication.billdetail

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import org.json.JSONException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import android.widget.DatePicker
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject


class billdata : Fragment() {

    var bill_list= arrayListOf<bill_data>()
    var shopMobile=""
    lateinit var recyclerView:RecyclerView
    lateinit var progressBar:RelativeLayout
    lateinit var etToDate:TextView
    lateinit var etFromDate:TextView
    lateinit var txtNoData:TextView
    lateinit var linearLayout:LinearLayout
    lateinit var btnNext:Button
    lateinit var btnPrev:Button
    var method=Request.Method.GET
    var jsonParams=JSONArray()
    var url=""
    var toDate = ""
    var fromDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var message= getArguments()?.getString("message")
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_bill_detail, container, false)
        var sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.preference_file),
            Context.MODE_PRIVATE
        )
        shopMobile = sharedPreferences.getString("username", "").toString()
        //Toast.makeText(context,"$shopMobile",Toast.LENGTH_SHORT).show()
        progressBar= view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.recyclerView)
        etFromDate = view.findViewById<TextView>(R.id.etFromDate)
        txtNoData=view.findViewById(R.id.txtNoData)
        etToDate = view.findViewById<TextView>(R.id.txtToDate)
        linearLayout=view.findViewById(R.id.LinearLayout)
        btnNext=view.findViewById(R.id.btnNext)
        btnPrev=view.findViewById(R.id.btnPrev)


        val btnSearchByDate = view.findViewById<Button>(R.id.btnDateSearch)
        toDate = ""
        fromDate = ""
        etFromDate.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val datePicker = DatePicker(context)
            datePicker.calendarViewShown = false
            builder.setView(datePicker)
            builder.setNegativeButton("Cancel", null)
            builder.setPositiveButton("OK") { text, listener ->
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                fromDate =
                    df.format(Date(datePicker.year - 1900, datePicker.month, datePicker.dayOfMonth))
                etFromDate.text = fromDate

            }

            builder.show()

        }
        etToDate.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val datePicker = DatePicker(context)
            datePicker.calendarViewShown = false
            builder.setView(datePicker)
            builder.setNegativeButton("Cancel", null)
            builder.setPositiveButton("OK") { text, listener ->
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                toDate =
                    df.format(Date(datePicker.year - 1900, datePicker.month, datePicker.dayOfMonth))
                etToDate.text = toDate
            }
            builder.show()
        }
//        val fab=view.findViewById(R.id.fab) as Button
        btnSearchByDate.setOnClickListener {
            if (fromDate.isNullOrEmpty()) {
                Toast.makeText(context, "Please select From Date", Toast.LENGTH_SHORT).show()
                etFromDate.error
            } else if (toDate.isNullOrEmpty()) {
                Toast.makeText(context, "Please select To Date", Toast.LENGTH_SHORT).show()
                etToDate.error
            } else {
                val df:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US);
                val fromdate:Date = df.parse("$fromDate")
                val todate:Date = df.parse("$toDate")
                    if (todate.before(fromdate)) {
                    Toast.makeText(context, "Invalid Date", Toast.LENGTH_SHORT).show()
                }
                    else{
                        bill_list.clear()
                        url="$link/dynamic"
                        //jsonParams.put("query","SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile")
                        method=Request.Method.POST
                        getData(url, method,jsonParams)

                }

            }
        }
        Toast.makeText(context,"$message",Toast.LENGTH_SHORT).show()
        if(message=="UB"){
            url="$link/api/bill_details?_where=(status,eq,Unpaid)&_sort=-paymentDate&_size=100"
            getData(url, Request.Method.GET,null)

        }
        else{
        url="$link/api/bill_details?_where=(shopMobile,eq,$shopMobile)&_sort=-paymentDate&_size=100"

        getData(url, Request.Method.GET,null)}
        return view
    }
        fun getData(url: String, method: Int, jsonObject: JSONArray?) {

            progressBar.visibility = View.VISIBLE
            val queue = Volley.newRequestQueue(activity as Context)
            val jsonObjectRequest = object : JsonArrayRequest(
                method,
                url,
                null,
                Response.Listener { response ->
                    //Toast.makeText(activity as Context,"$response",Toast.LENGTH_SHORT).show()
                    try {//val resArray=response.getJSONArray("items")
                        //Toast.makeText(activity as Context,"${resArray.length()}",Toast.LENGTH_SHORT).show()
                            if(response.length()==0){
                                txtNoData.visibility=View.VISIBLE
                                progressBar.visibility=View.GONE
                            }
                        else{
                                if(response.length()<100){
                                    linearLayout.visibility=View.GONE
                                }
                                txtNoData.visibility=View.GONE
                                progressBar.visibility=View.VISIBLE
                        for (i in 0 until response.length()) {
                            val resObject = response.getJSONObject(i)
                            //Toast.makeText(activity as Context,"$i",Toast.LENGTH_SHORT).show()
                            val bill = bill_data(
                                resObject.getString("Account"),
                                resObject.getString("dueDate"),
                                resObject.getString("amount"),
                                resObject.getString("status"),
                                resObject.getString("Tid"),
                                resObject.getString("billDate"),
                                resObject.getString("paymentDate"),
                                resObject.getString("customerName"),
                                resObject.getString("customerMobile"),
                                resObject.getString("shopName"),
                                resObject.getString("operatorName"),
                                resObject.getString("shopMobile"),
                                resObject.getString("balance")
                            )
                            bill_list.add(bill)
                            val layoutManager: RecyclerView.LayoutManager =
                                LinearLayoutManager(activity)
                            var recyclerAdapter =
                                recyclerAdapter(activity as Context, bill_list)
                            recyclerView.adapter = recyclerAdapter
                            recyclerView.layoutManager = layoutManager
                            progressBar.visibility = View.GONE
                            /*val a="mobile=8477086932"
                        for(i in bill_list){
                            if(bill_list.contains(a)){
                                Toast.makeText(context,"Data found",Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Toast.makeText(context,"$i",Toast.LENGTH_SHORT).show()
                                break

                            }
                        }
                        recyclerView.addItemDecoration(
                            DividerItemDecoration(
                                recyclerView.context,
                                (layoutManager as LinearLayoutManager).orientation
                            )
                        )*/
                        }}
                    } catch (e1: JSONException) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(activity as Context, "catch error $e1", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                Response.ErrorListener { error: VolleyError? ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(activity as Context, "volley error $error", Toast.LENGTH_SHORT)
                        .show()
                }) {

                /*Send the headers using the below method*/
                override fun getParams(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    //params["query"] = "SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '2021-08-01' AND '2021-09-11' AND shopMobile=$shopMobile"
                    //params.put("2ndParamName","valueoF2ndParam");
                    return params
                }

                override fun getBody(): ByteArray? {
                    val body = "{\"query\":\"SELECT * FROM bill_details WHERE CAST(paymentDate as date) BETWEEN '$fromDate' AND '$toDate' ORDER BY paymentDate DESC\"}"
                    return body.toByteArray()
                }
            }
            queue.add(jsonObjectRequest)
        }

}