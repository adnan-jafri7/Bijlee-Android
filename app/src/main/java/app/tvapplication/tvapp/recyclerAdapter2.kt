package app.tvapplication.tvapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_rechargereceipt.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class recyclerAdapter2(val context: Context, private val itemList: ArrayList<rc_data>):
    RecyclerView.Adapter<recyclerAdapter2.BillViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BillViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view2, parent, false)
        return BillViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val text = itemList[position]
        holder.txtMobile.text = text.mobile
        holder.txtAmount.text =
            "${holder.itemView.resources.getString(R.string.Rs)}${text.amount}.00"
        holder.txtRechargeDate.text = text.rechargeDate
        holder.txtBalance.text =
            "${holder.itemView.resources.getString(R.string.Rs)}${text.balance}.00"
        holder.txtOprId.text = text.operatorRef
        holder.txtStatus.text = text.status
        if(text.operatorName=="AIRTEL"){
            holder.imgOpt.setImageResource(R.drawable.airtel)
        }
        else if(text.operatorName=="Reliance Jio Infocomm Limited"){
            holder.imgOpt.setImageResource(R.drawable.jio)

        }
        else if(text.operatorName=="VODAFONE"){
            holder.imgOpt.setImageResource(R.drawable.vi)
        }
        else if(text.operatorName=="BSNL GSM"){
            holder.imgOpt.setImageResource(R.drawable.bsnl)
        }
        if (text.status == "Success") {
            holder.cardView.setBackgroundColor(Color.parseColor("#404CAF50"))
            holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_success, 0, 0, 0)
        } else if (text.status == "FAILED") {
            holder.cardView.setBackgroundColor(Color.parseColor("#40F49F99"))
            holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.failed, 0, 0, 0)
            holder.txtOprId.text = holder.itemView.resources.getString(R.string.status)
            //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFAF8FD"))
        } else {
            holder.cardView.setBackgroundColor(Color.parseColor("#40FFC107"))
            holder.txtOprId.text = holder.itemView.resources.getString(R.string.status)
            holder.txtOprId.setTextColor(0xFF0000FF.toInt())
            holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pending, 0, 0, 0)}


            holder.txtOprId.setOnClickListener {
                holder.txtOprId.visibility = View.GONE
                holder.progressBar.visibility = View.VISIBLE
                //Toast.makeText(context,"${text.tid}",Toast.LENGTH_SHORT).show()
                val queue = Volley.newRequestQueue(context)
                val url="$linkAPI/v2/status.php?api_key=12f4e4-164827-868329-2620eb-e4b21e&order_id=${text.tid}"
                val jsonRequest = object : JsonObjectRequest(
                    Method.GET, url,null,
                    Response.Listener {
                        Log.e("response api","$it")
                        try {
                            if (it.getJSONObject("response").getString("status") == "SUCCESS") {
                                val operatorRef=it.getJSONObject("response").getString("opr_id")
                                val queue=Volley.newRequestQueue(context)
                                val jsonParams= JSONObject()
                                jsonParams.put("query","UPDATE `mob_recharge` SET operatorRef='$operatorRef', status='Success' WHERE shopMobile='${text.shopMobile}' AND tid='${text.tid}'")
                                val url2="$link/dynamic/update"
                                val jsonRequest = object : JsonObjectRequest(
                                    Method.POST, url2, jsonParams,
                                    Response.Listener {
                                        Log.e("response","$it")
                                        val message = it.getString("message")
                                        if (message == "(Rows matched: 1  Changed: 1  Warnings: 0") {
                                            holder.txtOprId.text=operatorRef
                                            holder.cardView.setBackgroundColor(Color.parseColor("#404CAF50"))
                                            holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                                                R.drawable.ic_success,
                                                0,
                                                0,
                                                0
                                            )
                                            holder.progressBar.visibility=View.GONE
                                            holder.txtStatus.text = "Success"
                                            holder.txtOprId.text= operatorRef
                                            holder.txtOprId.visibility=View.VISIBLE
                                            holder.txtOprId.isEnabled=false
                                        }

                                    },
                                    Response.ErrorListener {
                                        holder.cardView.setBackgroundColor(Color.parseColor("#0000FF"))
                                        holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.pending,
                                            0,
                                            0,
                                            0
                                        )
                                        holder.progressBar.visibility=View.GONE
                                        holder.txtStatus.text = "Pending"
                                        holder.txtOprId.visibility=View.VISIBLE
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
                                holder.txtOprId.text=it.getJSONObject("response").getString("opr_id")
                                holder.cardView.setBackgroundColor(Color.parseColor("#404CAF50"))
                                holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_success,
                                    0,
                                    0,
                                    0
                                )
                                holder.progressBar.visibility=View.GONE
                                holder.txtStatus.text = "Success"
                                holder.txtOprId.text= it.getJSONObject("response").getString("opr_id").toString()
                                holder.txtOprId.visibility=View.VISIBLE
                                holder.txtOprId.isEnabled=false



                            }
                           else if ((it.getJSONObject("response").getString("status") == "FAILED") || (it.getJSONObject("response").getString("status") == "REVERSAL") || (it.getJSONObject("response").getString("message") == "Invalid Transaction Id")) {
                                text.balance=(text.balance.toInt()+text.amount.toInt()).toString()
                                val queue=Volley.newRequestQueue(context)
                                val jsonParams=JSONObject()
                                jsonParams.put(
                                    "query",
                                    "UPDATE `user_detail` SET `balance`='${text.balance}' WHERE username=${text.shopMobile}"
                                )
                                val url =
                                    "$link/dynamic/update"
                                val jsonRequest = object : JsonObjectRequest(
                                    Method.POST, url, jsonParams,
                                    Response.Listener {
                                        val message = it.getString("message")
                                        if (message == "(Rows matched: 1  Changed: 1  Warnings: 0") {
                                            val queue=Volley.newRequestQueue(context)
                                            val jsonParams=JSONObject()
                                            jsonParams.put("query","UPDATE `mob_recharge` SET status='FAILED', balance='${text.balance}' WHERE shopMobile='${text.shopMobile}' AND tid='${text.tid}'")
                                            val url2="$link/dynamic/update"
                                            val jsonRequest = object : JsonObjectRequest(
                                                Method.POST, url2, jsonParams,
                                                Response.Listener {
                                                    holder.cardView.setBackgroundColor(Color.parseColor("#40F49F99"))
                                                    holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                                                        R.drawable.failed,
                                                        0,
                                                        0,
                                                        0
                                                    )
                                                    holder.progressBar.visibility=View.GONE
                                                    holder.txtStatus.text = "FAILED"


                                                },
                                                Response.ErrorListener {
                                                    holder.cardView.setBackgroundColor(Color.parseColor("#400000FF"))
                                                    holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                                                        R.drawable.pending,
                                                        0,
                                                        0,
                                                        0
                                                    )
                                                    holder.progressBar.visibility=View.GONE
                                                    holder.txtStatus.text = "Pending"
                                                    holder.txtOprId.visibility=View.VISIBLE
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
                                        holder.cardView.setBackgroundColor(Color.parseColor("#400000FF"))
                                        holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.pending,
                                            0,
                                            0,
                                            0
                                        )
                                        holder.progressBar.visibility=View.GONE
                                        holder.txtStatus.text = "Pending"
                                        holder.txtOprId.visibility=View.VISIBLE
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
                                holder.cardView.setBackgroundColor(Color.parseColor("#400000FF"))
                                holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.pending,
                                    0,
                                    0,
                                    0
                                )
                                holder.progressBar.visibility=View.GONE
                                holder.txtStatus.text = "Pending"
                                holder.txtOprId.visibility=View.VISIBLE

                            }
                        }
                        catch (E:JSONException){
                            Toast.makeText(context,"Some error occurred",Toast.LENGTH_SHORT).show()
                            holder.progressBar.visibility=View.GONE
                            holder.txtStatus.text = "Pending"
                            holder.txtOprId.visibility=View.VISIBLE
                        }
                    },
                Response.ErrorListener {
                    holder.cardView.setBackgroundColor(Color.parseColor("#400000FF"))
                    holder.txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.pending,
                        0,
                        0,
                        0
                    )
                    holder.progressBar.visibility=View.GONE
                    holder.txtStatus.text = "Pending"
                    holder.txtOprId.visibility=View.VISIBLE
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

    }

    override fun getItemCount(): Int {
        return itemList.size

    }

    class BillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtMobile: TextView = view.findViewById(R.id.txtMobile)
        var txtAmount:TextView=view.findViewById(R.id.txtAmount)
        var txtRechargeDate:TextView=view.findViewById(R.id.txtRechargeDate)
        var txtBalance:TextView=view.findViewById(R.id.txtBalance)
        var txtOprId:TextView=view.findViewById(R.id.txtOprId)
        var txtStatus:TextView=view.findViewById(R.id.txtStatus)
        var cardView:CardView=view.findViewById(R.id.cardView)
        var progressBar: ProgressBar =view.findViewById(R.id.progressBar5)
        var imgOpt: ImageView =view.findViewById(R.id.imgOpr)
    }
}