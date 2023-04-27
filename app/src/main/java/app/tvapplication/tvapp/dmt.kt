package app.tvapplication.tvapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_dmt.*

class dmt : AppCompatActivity() {
    lateinit var watcher: TextWatcher
    lateinit var etSenderMobile:EditText
    lateinit var etSenderName:EditText
    lateinit var etBankAC:EditText
    lateinit var etConfirmBankAC:EditText
    lateinit var etIFSC:EditText
    lateinit var etBenName:EditText
    lateinit var etAmount:EditText
    lateinit var etConfirmAmount:EditText
    lateinit var btnProceed:Button
    var smobile=false
    var sname=false
    var ac=false
    var cac=false
    var bname=false
    var ifsc=false
    var Mobile=""
    var username=""
    var shopName=""
    var shopMobile=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dmt)
        etSenderMobile=findViewById(R.id.etSenderMobile)
        etSenderName=findViewById(R.id.etSenderName)
        etBankAC=findViewById(R.id.etBankAC)
        etConfirmBankAC=findViewById(R.id.etConfirmBankAC)
        etIFSC=findViewById(R.id.etIFSC)
        etBenName=findViewById(R.id.etBenName)
        etAmount=findViewById(R.id.etAmount)
        etConfirmAmount=findViewById(R.id.etConfirmAmount)
        btnProceed=findViewById(R.id.btnProceed)
        var sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        shopName = sharedPreferences.getString("name", "").toString()
        shopMobile = sharedPreferences.getString("mobile", "").toString()
        if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            val dialog = AlertDialog.Builder(this@dmt)
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
        if (ConnectionManager().checkConnectivity(this)) {

            etAmount.isEnabled=false
            etConfirmAmount.isEnabled=false
            watcher = object : TextWatcher {
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
                    when (hasWindowFocus()) {
                        etSenderMobile.hasFocus() -> {
                            if (s.length == 10) {
                                smobile = true
                                check()

                            } else {
                                btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
                                btnProceed.isEnabled = false
                            }
                        }
                        etSenderName.hasFocus()->{
                            if (s.length > 3) {
                                sname = true
                                check()

                            } else {
                                btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
                                btnProceed.isEnabled = false
                            }

                        }
                        etBankAC.hasFocus()->{
                            if (s.length>4) {
                                ac = true
                                check()

                            } else {
                                btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
                                btnProceed.isEnabled = false
                            }
                        }
                        etConfirmBankAC.hasFocus()->{
                            if (s.length>4) {
                                cac = true
                                check()

                            } else {
                                btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
                                btnProceed.isEnabled = false
                            }

                        }
                        etBenName.hasFocus()->{
                            if (s.length>1) {
                                bname = true
                                check()

                            } else {
                                btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
                                btnProceed.isEnabled = false
                            }
                        }
                        etIFSC.hasFocus()->{
                            if (s.length==11) {
                                ifsc = true
                                check()

                            } else {
                                btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
                                btnProceed.isEnabled = false
                            }

                        }
                    }

                }


            }
            etSenderMobile.addTextChangedListener(watcher)
            etSenderName.addTextChangedListener(watcher)
            etBankAC.addTextChangedListener(watcher)
            etConfirmBankAC.addTextChangedListener(watcher)
            etBenName.addTextChangedListener(watcher)
            etIFSC.addTextChangedListener(watcher)

        }
        else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@dmt)
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


    fun check() {

        if (sname && smobile && ac && cac && bname && ifsc && etConfirmBankAC.text.toString()==(etBankAC.text.toString())) {
            btnProceed.setBackgroundColor(0xFF4CAF50.toInt())
            btnProceed.isEnabled = true
            btnVerify.setBackgroundColor(0xFF4CAF50.toInt())
            btnVerify.isEnabled = true
        } else {

            btnProceed.setBackgroundColor(0xDDDDDDDD.toInt())
            btnProceed.isEnabled = false
        }


        //Toast.makeText(this,"${isValidPassword(password)}",Toast.LENGTH_LONG).show()
    }
}