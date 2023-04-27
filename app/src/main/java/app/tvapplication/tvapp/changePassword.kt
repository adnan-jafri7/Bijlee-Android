package app.tvapplication.tvapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.util.HashMap
import java.util.regex.Matcher
import java.util.regex.Pattern


class changePassword : AppCompatActivity() {
    var username=""
    var shopName=""
    var shopMobile=""
    var current=false
    var new=false
    var confirm=false
    var currentPassword=""
    var newPassword=""
    var oldPassword=""
    var confirmPassword=""
    lateinit var progressBar:ProgressBar
    lateinit var passwordPolicy: TextView
    lateinit var btnChangePSW:Button
    lateinit var etCurrentPassword:EditText
    lateinit var etNewPassword:EditText
    lateinit var etConfirmPassword:EditText
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var sharedPreferences: SharedPreferences
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        sharedPreferences=getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        shopName = sharedPreferences.getString("name", "").toString()
        shopMobile = sharedPreferences.getString("mobile", "").toString()
        passwordPolicy=findViewById<TextView>(R.id.passwordPolicy)
        btnChangePSW=findViewById(R.id.btnChangePSW)
        etCurrentPassword=findViewById(R.id.etCurrentPassword)
        etNewPassword=findViewById(R.id.etNewPassword)
        etConfirmPassword=findViewById(R.id.etConfirmPassword)
        progressBar=findViewById(R.id.progressBar)
        if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            val dialog = AlertDialog.Builder(this@changePassword)
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


        passwordPolicy.text="Password Policies \n \u25CF Password should not contain any space. \n \u25CF Password should contain at least one digit(0-9). \n \u25CF Password length should be between 8 to 20 characters. \n \u25CF Password should contain at least one lowercase letter(a-z). \n \u25CF Password should contain at least one uppercase letter(A-Z). \n \u25CF Password should contain at least one special character."
        etCurrentPassword.addTextChangedListener(object : TextWatcher {
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
                if (s.length in 8..20) {
                    current=true
                    check()
                } else {
                    btnChangePSW.setBackgroundColor(0xDDDDDDDD.toInt())
                    btnChangePSW.isEnabled = false
                    current=false
                }
            }
        })

        etNewPassword.addTextChangedListener(object : TextWatcher {
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
                if (s.length in 8..20) {
                    new=true
                    check()
                } else {
                    btnChangePSW.setBackgroundColor(0xDDDDDDDD.toInt())
                    btnChangePSW.isEnabled = false
                    new=false
                }
            }
        })

        etConfirmPassword.addTextChangedListener(object : TextWatcher {
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
                if (s.length in 8..20) {
                    confirm=true
                    check()
                } else {
                    btnChangePSW.setBackgroundColor(0xDDDDDDDD.toInt())
                    btnChangePSW.isEnabled = false
                    confirm=false
                }
            }
        })

        btnChangePSW.setOnClickListener {
            confirmPassword=etNewPassword.text.toString()
            oldPassword=etCurrentPassword.text.toString()
            etCurrentPassword.isEnabled=false
            etNewPassword.isEnabled=false
            etConfirmPassword.isEnabled=false
            btnChangePSW.visibility=View.INVISIBLE
            progressBar.visibility= View.VISIBLE
            if (ConnectionManager().checkConnectivity(this)) {
                //for (i in 1..3) {
                    //oldPassword = Base64.encodeToString(oldPassword.toByteArray(), Base64.DEFAULT)
                    //confirmPassword = Base64.encodeToString(confirmPassword.toByteArray(), Base64.DEFAULT)
                    oldPassword=md5(oldPassword)
                    confirmPassword=md5(confirmPassword)
                //}
                val queue = Volley.newRequestQueue(this@changePassword)
                val url =
                    "$link/api/user_detail/findOne?_where=(username,eq,${username})"
                val jsonRequest = object : JsonArrayRequest(
                    Method.GET, url, null,
                    Response.Listener {
                        //Toast.makeText(this,"$username $confirmPassword",Toast.LENGTH_LONG).show()
                        //Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                        val response = it.toString()
                        Log.e("Current Password",oldPassword)
                        Log.e("New Password",confirmPassword)
                        if (response != "[]") {
                            val array = it.getJSONObject(0)
                            val mobileFetched = array.getString("mobile")
                            val passwordFetched = array.getString("passowrd")
                            Log.e("Password Fetched",passwordFetched)
                            if (passwordFetched.toString() == oldPassword) {

                                progressBar.visibility = View.INVISIBLE
                                btnChangePSW.visibility = View.VISIBLE
                                val queue = Volley.newRequestQueue(this)
                                val url =
                                    "$link/dynamic"
                                val jsonParams = JSONObject()
                                jsonParams.put(
                                    "query",
                                    "UPDATE `user_detail` SET `passowrd`='$confirmPassword' WHERE username=${username}"
                                )
                                val jsonRequest = object : JsonObjectRequest(
                                    Method.POST, url, jsonParams,
                                    Response.Listener {
                                        val dialog = AlertDialog.Builder(this@changePassword)
                                        dialog.setTitle("")
                                        dialog.setMessage("Password Changed Successfully. You have been logged out.")
                                        dialog.setNegativeButton("OK") { text, listener ->
                                            sharedPreferences.edit().clear().apply()
                                            progressBar.visibility = View.INVISIBLE
                                            btnChangePSW.visibility = View.VISIBLE
                                            etCurrentPassword.isEnabled = true
                                            etNewPassword.isEnabled = true
                                            etConfirmPassword.isEnabled = true
                                            finishAffinity()
                                            startActivity(Intent(this,LoginActivity::class.java))

                                        }


                                        dialog.create()
                                        dialog.setCancelable(false)
                                        dialog.show()


                                    },
                                    Response.ErrorListener {
                                        Toast.makeText(this,"Some Error Occurred",Toast.LENGTH_LONG).show()
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
                            else{
                                val dialog = AlertDialog.Builder(this@changePassword)
                                dialog.setTitle("")
                                dialog.setMessage("Invalid Current Password.")
                                dialog.setNegativeButton("OK") { text, listener ->
                                    progressBar.visibility=View.INVISIBLE
                                    btnChangePSW.visibility=View.VISIBLE
                                    etCurrentPassword.isEnabled=true
                                    etNewPassword.isEnabled=true
                                    etConfirmPassword.isEnabled=true
                                }


                                dialog.create()
                                dialog.setCancelable(false)
                                dialog.show()

                            }
                        }
                        else{
                            val dialog = AlertDialog.Builder(this@changePassword)
                            dialog.setTitle("")
                            dialog.setMessage("Invalid Current Password.")
                            dialog.setNegativeButton("OK") { text, listener ->
                                progressBar.visibility=View.INVISIBLE
                                btnChangePSW.visibility=View.VISIBLE
                                etCurrentPassword.isEnabled=true
                                etNewPassword.isEnabled=true
                                etConfirmPassword.isEnabled=true
                            }


                            dialog.create()
                            dialog.setCancelable(false)
                            dialog.show()

                        }
                    },Response.ErrorListener {
                        val dialog = AlertDialog.Builder(this@changePassword)
                        dialog.setTitle("")
                        dialog.setMessage("Something went wrong.")
                        dialog.setNegativeButton("OK") { text, listener ->
                            progressBar.visibility=View.INVISIBLE
                            btnChangePSW.visibility=View.VISIBLE
                            etCurrentPassword.isEnabled=true
                            etNewPassword.isEnabled=true
                            etConfirmPassword.isEnabled=true
                        }


                        dialog.create()
                        dialog.setCancelable(false)
                        dialog.show()


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

                else{
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@changePassword)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and retry.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                }
                builder.create()
                builder.show()
            }

        }


    }
    fun check() {
        var password=etNewPassword.text.toString()

        if (current && new && etConfirmPassword.text.toString()==etNewPassword.text.toString() && isValidPassword(password)) {
            btnChangePSW.setBackgroundColor(0xFF4CAF50.toInt())
            btnChangePSW.isEnabled = true
        } else {

            btnChangePSW.setBackgroundColor(0xDDDDDDDD.toInt())
            btnChangePSW.isEnabled = false
        }


        //Toast.makeText(this,"${isValidPassword(password)}",Toast.LENGTH_LONG).show()
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern: Pattern
        val specialCharacters = "-@%\\[\\}+'!/#$^?:;,\\(\"\\)~`.*=&\\{>\\]<_"
        val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[$specialCharacters])(?=\\S+$).{8,20}$"
        pattern = Pattern.compile(PASSWORD_REGEX)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }
    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

}