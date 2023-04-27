package app.tvapplication.billdetail

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.internal.ContextUtils.getActivity
import com.tvapplication.tvapp.R
import app.tvapplication.tvapp.bill_receipt
import org.json.JSONException
import org.json.JSONObject
import java.security.AccessController.getContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class recyclerAdapter(val context: Context, private val itemList:ArrayList<bill_data>):RecyclerView.Adapter<recyclerAdapter.BillViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.recycler_view,parent,false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val text = itemList[position]
        holder.dueDate.text = text.dueDate
        holder.account.text = text.account
        holder.amount.text = text.amount
        holder.billDate=text.billDate
        holder.paymentDate=text.paymentDate
        holder.Tid.text = text.tId
        holder.balance.text = "Rs.${text.balance}"
        holder.customerName=text.customerName
        holder.customerMobile=text.customerMobile
        holder.shopName=text.shopName
        holder.shopMobile=text.shopMobile
        holder.operatorName=text.operatorName
        if(text.status=="Credit"){
            holder.status.setImageResource(R.drawable.wallet)
            holder.line.visibility = View.GONE
            holder.status.isEnabled=false
            holder.cardView.isEnabled=false

        }
        else if (text.status == "Unpaid") {
            holder.status.setImageResource(R.drawable.close)
            holder.line.visibility = View.GONE
        } else {
            holder.status.setImageResource(R.drawable.check)
            holder.line.visibility = View.VISIBLE
        }
        holder.status.setOnClickListener {
            if (text.status == "Paid") {
                //Toast.makeText(context, "Already Paid", Toast.LENGTH_SHORT).show()
            } else {
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                var dateTime = df.format(Calendar.getInstance().time)
                holder.status.visibility = View.GONE
                holder.progressBar.visibility = View.VISIBLE
                val queue = Volley.newRequestQueue(context)
                val url =
                    "$linkAPI/v2/bills/validation.php?api_key=12f4e4-164827-868329-2620eb-e4b21e&number=${text.account}&amount=10&opid=88&order_id=478245232&opt1=opt1&opt2=opt2&opt3=opt3&opt4=opt4&opt5=opt5&opt6=opt6&opt7=opt7&opt8=Bills&opt9=opt9&opt10=opt10&mobile=7906025575"
                //val url="http://54.90.235.144:3001/api/v1/bbps_validate.php?userid=adnan.jafr&key=318184005984622&operator=UPE&amount=10&orderid=ABCD00001&customer_mobile=8477086932&customer_name=ABCD&optional1=xx&optional2=xx&optional3=xx&service=${text.account}"
                val jsonRequest = object : JsonObjectRequest(
                    Method.GET, url, null,
                    Response.Listener {
                        try {
                            //Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
                            val status = it.getString("status")
                            val error = it.getString("message")
                            Log.e("error", error)
                            if (status == "FAILED" && error == "Payment received for the billing period - no bill due") {

                                val queue = Volley.newRequestQueue(context)
                                val url =
                                    "$link/dynamic"
                                val jsonParams = JSONObject()
                                jsonParams.put("query","UPDATE `bill_details` SET `status`='Paid', updateDate='$dateTime' WHERE Account=${holder.account.text}")
                                /*jsonParams.put("account", text.account)
                                jsonParams.put("amount", text.amount)
                                jsonParams.put("date", text.dueDate)
                                jsonParams.put("status", "Paid")
                                jsonParams.put("_id", text.tId)
                                jsonParams.put("tId", text.tId)
                                jsonParams.put("mobile", text.customerMobile)
                                jsonParams.put("_updatedDate", text.paymentDate)
                                jsonParams.put("_createdDate", text.billDate)
                                jsonParams.put("paymentGateway", "GPAY")*/
                                val jsonRequest = object : JsonObjectRequest(
                                    Method.POST, url, jsonParams,
                                    Response.Listener {
                                        try {
                                            holder.progressBar.visibility = View.GONE
                                            holder.status.visibility = View.VISIBLE
                                            holder.status.setImageResource(R.drawable.check)
                                            //holder.line.visibility = View.VISIBLE
                                            holder.status.isClickable = false
                                            /*Toast.makeText(
                                                context,
                                                "Paid Successfully",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()*/


                                        } catch (e1: JSONException) {
                                            Toast.makeText(
                                                context,
                                                "Json Error: $e1",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            holder.progressBar.visibility = View.GONE
                                            holder.status.visibility = View.VISIBLE
                                            holder.status.setImageResource(R.drawable.close)
                                            holder.line.visibility = View.GONE
                                        }
                                    },
                                    Response.ErrorListener {
                                        Toast.makeText(context, "Volley Error: $it", Toast.LENGTH_SHORT)
                                            .show()
                                        holder.progressBar.visibility = View.GONE
                                        holder.status.visibility = View.VISIBLE
                                        holder.status.setImageResource(R.drawable.close)
                                        holder.line.visibility = View.GONE
                                    }) {
                                    override fun getHeaders(): MutableMap<String, String> {
                                        val headers = HashMap<String, String>()
                                        //headers["Content-type"] = "application/json"
                                        return headers

                                    }
                                }
                                jsonRequest.setShouldCache(false)
                                jsonRequest.retryPolicy = DefaultRetryPolicy(
                                    50000,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                                )

                                queue.add(jsonRequest)
                                //Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show()
                            } else {
                                holder.progressBar.visibility = View.GONE
                                holder.status.visibility = View.VISIBLE
                                holder.status.setImageResource(R.drawable.close)
                                holder.line.visibility = View.GONE
                                //Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()

                            }

                        } catch (e: JSONException) {
                            Toast.makeText(context, "Kwik Error $e", Toast.LENGTH_SHORT).show()
                            holder.progressBar.visibility = View.GONE
                            holder.status.visibility = View.VISIBLE
                            holder.status.setImageResource(R.drawable.close)
                            holder.line.visibility = View.GONE
                        }
                    },
                    Response.ErrorListener {
                        Toast.makeText(context, "Kwik Error $it", Toast.LENGTH_SHORT).show()
                        holder.progressBar.visibility = View.GONE
                        holder.status.visibility = View.VISIBLE
                        holder.status.setImageResource(R.drawable.close)
                        holder.line.visibility = View.GONE
                    }
                ) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        //headers["Content-type"] = "application/json"
                        return headers

                    }
                }
                jsonRequest.retryPolicy = DefaultRetryPolicy(
                    50000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                jsonRequest.setShouldCache(false)
                //jsonRequest.setShouldCache(false)
                queue.add(jsonRequest)

            }
            /*val alert = AlertDialog.Builder(context)
                val mView = LayoutInflater.from(context).inflate(R.layout.dialog, null)
                val accountMobile: EditText = mView.findViewById(R.id.txt_input)
                val payGateway: EditText = mView.findViewById(R.id.txt_input2)
                val btn_cancel = mView.findViewById(R.id.btn_cancel) as Button
                val btn_okay = mView.findViewById(R.id.btn_okay) as Button
                alert.setView(mView)
                val alertDialog = alert.create()
                alertDialog.show()
                alertDialog.setCanceledOnTouchOutside(false)
                btn_cancel.setOnClickListener {
                    Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
                btn_okay.setOnClickListener {
                    var paymentGateway = payGateway.text.toString()
                    var accountMobile = accountMobile.text.toString()
                    if(accountMobile.isNullOrEmpty()){
                        Toast.makeText(context,"Please Enter Mobile",Toast.LENGTH_SHORT).show()
                    }
                    else if(paymentGateway.isNullOrEmpty()){
                        Toast.makeText(context,"Please Enter Payment Gateway",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        alertDialog.dismiss()
                    holder.status.visibility = View.GONE
                    holder.progressBar.visibility = View.VISIBLE
                    val queue = Volley.newRequestQueue(context)
                    val url = "https://adnanjafri783.wixsite.com/website-1/_functions/mFunction"
                    val jsonParams = JSONObject()
                    jsonParams.put("account", text.account)
                    jsonParams.put("amount", text.amount)
                    jsonParams.put("date", text.date)
                    jsonParams.put("mobile", "8979319339")
                    jsonParams.put("status", "Paid")
                    jsonParams.put("_id", text.id)
                    jsonParams.put("tId",text.tId)
                    jsonParams.put("_updatedDate", text.uDate)
                    jsonParams.put("_createdDate", text.uDate)
                    jsonParams.put("paymentGateway",paymentGateway)
                    jsonParams.put("accountMobile",accountMobile)
                    val jsonRequest = object : JsonObjectRequest(
                        Method.PUT, url, jsonParams,
                        Response.Listener {
                            try {
                                holder.progressBar.visibility = View.GONE
                                holder.status.visibility = View.VISIBLE
                                holder.status.setImageResource(R.drawable.check)
                                holder.line.visibility=View.VISIBLE
                                holder.status.isClickable=false
                                Toast.makeText(context, "Paid Successfully", Toast.LENGTH_LONG)
                                    .show()


                            } catch (e1: JSONException) {
                                Toast.makeText(context, "$e1", Toast.LENGTH_SHORT).show()
                                holder.progressBar.visibility = View.GONE
                                holder.status.visibility = View.VISIBLE
                                holder.status.setImageResource(R.drawable.close)
                                holder.line.visibility=View.GONE
                            }
                        },
                        Response.ErrorListener {
                            Toast.makeText(
                                context,
                                "$it",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            holder.progressBar.visibility = View.GONE
                            holder.status.visibility = View.VISIBLE
                            holder.status.setImageResource(R.drawable.close)
                            holder.line.visibility=View.GONE

                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            //headers["Content-type"] = "application/json"
                            return headers
                        }
                    }
                    queue.add(jsonRequest)

                }}*/

        }
        holder.cardView.setOnClickListener{
            val intent=Intent(context, bill_receipt::class.java)
            intent.putExtra("name", holder.customerName)
            intent.putExtra("account", holder.account.text)
            intent.putExtra("dueDate", holder.dueDate.text)
            intent.putExtra("billDate", holder.billDate)
            intent.putExtra("amount", holder.amount.text)
            intent.putExtra("paymentDate", holder.paymentDate)
            intent.putExtra("operator", holder.operatorName)
            intent.putExtra("Tid", holder.Tid.text)
            intent.putExtra("shopName", holder.shopName)
            intent.putExtra("shopMobile", holder.shopMobile)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    class BillViewHolder(view: View):RecyclerView.ViewHolder(view){
        val dueDate:TextView=view.findViewById(R.id.txtDate)
        val amount:TextView=view.findViewById(R.id.txtAmount)
        val account:TextView=view.findViewById(R.id.txtAccount)
        var status:ImageButton=view.findViewById(R.id.txtStatus)
        val progressBar:ProgressBar=view.findViewById(R.id.Progress)
        val Tid:TextView=view.findViewById(R.id.txtTid)
        val balance:TextView=view.findViewById(R.id.txtMobile)
        var billDate=""
        var paymentDate=""
        var customerName=""
        var customerMobile=""
        var shopName=""
        var shopMobile=""
        var operatorName=""
        val line:View=view.findViewById(R.id.line)
        val cardView:CardView=view.findViewById(R.id.cardView)
        //val fab=view.findViewById<View>(R.id.fab)
    }
}