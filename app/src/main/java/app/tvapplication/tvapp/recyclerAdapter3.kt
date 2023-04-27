package app.tvapplication.tvapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.res.TypedArrayUtils.getString
import androidx.recyclerview.widget.RecyclerView
import com.tvapplication.tvapp.R

class recyclerAdapter3(val context: Context, private val itemList: ArrayList<plans>,var onImageClickListener:OnImageClickListener):
    RecyclerView.Adapter<recyclerAdapter3.BillViewHolder>() {
    var sharedPreferences: SharedPreferences? =context.getSharedPreferences("Credential File", Context.MODE_PRIVATE)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BillViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view3, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val text = itemList[position]
        holder.txtAmount.text =
            "${holder.itemView.resources.getString(R.string.Rs)}${text.amount}.00"
        holder.txtValidity.text = text.validity
        holder.txtDesc.text = text.desc

            holder.cardView.setOnClickListener(View.OnClickListener {
                onImageClickListener.onImageClick(
                    text.amount
                )
            })


        }

    override fun getItemCount(): Int {
        return itemList.size

    }

    class BillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtAmount: TextView = view.findViewById(R.id.txtAmount)
        var txtValidity: TextView = view.findViewById(R.id.txtValidity)
        var txtDesc: TextView = view.findViewById(R.id.txtDesc)
        var cardView: CardView = view.findViewById(R.id.cardView)
        //var etAmount:EditText=view.findViewById(R.id.etAmount)

    }

}
