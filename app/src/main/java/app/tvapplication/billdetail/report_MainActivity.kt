package app.tvapplication.billdetail


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.tvapplication.tvapp.ConnectionManager
import app.tvapplication.tvapp.HomeActivity
import app.tvapplication.tvapp.LoginActivity
import com.tvapplication.tvapp.R

class report_MainActivity : AppCompatActivity() {
    var username=""
    var shopName=""
    var shopMobile=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_mainactivity)
        val intent = intent
        val message = intent.getStringExtra("message")
        if (ConnectionManager().checkConnectivity(this)) {

        var sharedPreferences=getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username=sharedPreferences.getString("username","").toString()
        shopName=sharedPreferences.getString("name","").toString()
        shopMobile=sharedPreferences.getString("mobile","").toString()
                if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            val dialog = AlertDialog.Builder(this@report_MainActivity)
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
        var isLoggedIn=sharedPreferences.getBoolean("isLoggedIn",false)
        if(!isLoggedIn){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
            if(message=="UB"){
                val fm = supportFragmentManager
                    val bundle = Bundle()
                    bundle.putString("message", "UB")
                    val fragobj = billdata()
                    fragobj.arguments = bundle
                    fm.beginTransaction().add(R.id.Rl,fragobj).commit()


            }
            else{
        val fm = supportFragmentManager
        val fragment = billdata()
        fm.beginTransaction().add(R.id.Rl, fragment).commit()}
    }
    else {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this@report_MainActivity)
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