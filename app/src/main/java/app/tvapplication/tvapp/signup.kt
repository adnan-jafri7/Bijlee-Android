package app.tvapplication.tvapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import app.tvapplication.tvapp.ConnectionManager
import app.tvapplication.tvapp.LoginActivity
import app.tvapplication.tvapp.link
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import okio.HashingSource.md5
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class signup : AppCompatActivity() {
    var fname = false
    lateinit var watcher: TextWatcher
    lateinit var progress:ProgressDialog
    lateinit var animationLL:LinearLayout
    lateinit var sprinkled:LottieAnimationView
    var newPassword=""
    var lname = false
    var sname = false
    var mobile = false
    var email = false
    var address = false
    var pincode = false
    var password = false
    var cpassword = false
    lateinit var etFirstName: EditText
    lateinit var etLastName: EditText
    lateinit var etShopName: EditText
    lateinit var etMobile: EditText
    lateinit var etEmail: EditText
    lateinit var etAddress: EditText
    lateinit var etPincode: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var txtPolicies: TextView
    lateinit var btnSignUp: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etShopName = findViewById(R.id.etShopName)
        etMobile = findViewById(R.id.etMobile)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etPincode = findViewById(R.id.etPincode)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        txtPolicies = findViewById(R.id.txtPolicies)
        btnSignUp = findViewById(R.id.btnSignUp)
        animationLL=findViewById(R.id.animationLL)
        sprinkled=findViewById(R.id.animationSprinkled)
        var drawableRight = etPassword.context.resources.getDrawable(R.drawable.check)
        var drawableRightClose = etPassword.context.resources.getDrawable(R.drawable.close)
        etPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        txtPolicies.setOnClickListener {
            val dialog = AlertDialog.Builder(this@signup)
            dialog.setTitle("Password Policies")
            dialog.setMessage(
                " ● Password should not contain any space. \n" +
                        " ● Password should contain at least one digit(0-9). \n" +
                        " ● Password length should be between 8 to 20 characters. \n" +
                        " ● Password should contain at least one lowercase letter(a-z). \n" +
                        " ● Password should contain at least one uppercase letter(A-Z). \n" +
                        " ● Password should contain at least one special character."
            )
            dialog.setNegativeButton("OK") { text, listener ->
            }

            dialog.create()
            dialog.setCancelable(true)
            dialog.show()
        }
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
                    etFirstName.hasFocus() -> {
                        if (s.length > 2) {
                            etFirstName.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRight,
                                null
                            )
                            fname = true
                            check()

                        } else {
                            etFirstName.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRightClose,
                                null
                            )
                            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                            btnSignUp.isEnabled = false
                        }
                    }
                    etShopName.hasFocus()->{
                        if (s.length > 4) {
                            etShopName.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRight,
                                null
                            )
                            sname = true
                            check()

                        } else {
                            etShopName.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRightClose,
                                null
                            )
                            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                            btnSignUp.isEnabled = false
                        }

                    }
                    etMobile.hasFocus()->{
                        if (s.length ==10) {
                            etMobile.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRight,
                                null
                            )
                            mobile = true
                            check()

                        } else {
                            etMobile.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRightClose,
                                null
                            )
                            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                            btnSignUp.isEnabled = false
                        }
                    }
                    etEmail.hasFocus()->{
                        if (s.isNotEmpty() && isValidEmail(s)) {
                            etEmail.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRight,
                                null
                            )
                            email = true
                            check()

                        } else {
                            etEmail.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRightClose,
                                null
                            )
                            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                            btnSignUp.isEnabled = false
                        }

                    }
                    etAddress.hasFocus()->{
                        if (s.length>10) {
                            etAddress.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRight,
                                null
                            )
                            address = true
                            check()

                        } else {
                            etAddress.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRightClose,
                                null
                            )
                            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                            btnSignUp.isEnabled = false
                        }
                    }
                        etPincode.hasFocus()->{
                            if (s.length>5) {
                                etPincode.setCompoundDrawablesWithIntrinsicBounds(
                                    null,
                                    null,
                                    drawableRight,
                                    null
                                )
                                pincode = true
                                check()

                            } else {
                                etPincode.setCompoundDrawablesWithIntrinsicBounds(
                                    null,
                                    null,
                                    drawableRightClose,
                                    null
                                )
                                btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                                btnSignUp.isEnabled = false
                        }

                    }
                    etPassword.hasFocus()->{
                        if (isValidPassword(s.toString())) {
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRight,
                                null
                            )
                            password = true
                            check()

                        } else {
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRightClose,
                                null
                            )
                            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                            btnSignUp.isEnabled = false
                        }

                    }
                    etConfirmPassword.hasFocus()->{
                        if (isValidPassword(s.toString()) && s.toString()==etPassword.text.toString()) {
                            etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRight,
                                null
                            )
                            cpassword = true
                            check()

                        } else {
                            etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                drawableRightClose,
                                null
                            )
                            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
                            btnSignUp.isEnabled = false
                        }
                    }
                }

            }


        }
        btnSignUp.setOnClickListener{
            newPassword=etPassword.text.toString()
            if (ConnectionManager().checkConnectivity(this)) {
                animationLL.visibility= View.VISIBLE

                    //newPassword = Base64.encodeToString(newPassword.toByteArray(), Base64.DEFAULT)
                    newPassword= md5(newPassword)
                val queue=Volley.newRequestQueue(this)
                val url="$link/api/user_detail/findOne?_where=(mobile,eq,${etMobile.text})~or(email,eq,${etEmail.text})"
                val jsonRequest = object : JsonArrayRequest(
                    Method.GET, url, null,
                    Response.Listener {
                        //Toast.makeText(this,"$username $confirmPassword",Toast.LENGTH_LONG).show()
                        //Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                        val response = it.toString()
                        if (response != "[]") {
                            animationLL.visibility= View.INVISIBLE
                           // Toast.makeText(this,"$response",Toast.LENGTH_LONG).show()
                            val array = it.getJSONObject(0)
                            val mobileFetched = array.getString("mobile")
                            val emailFetched = array.getString("email").lowercase()
                            if(mobileFetched==etMobile.text.toString()){
                                val builder = androidx.appcompat.app.AlertDialog.Builder(this@signup)
                                builder.setTitle("Error")
                                builder.setMessage("Mobile Number already exists.")
                                builder.setCancelable(false)
                                builder.setPositiveButton("Ok") { _, _ ->
                                }
                                builder.create()
                                builder.show()

                            }
                            else if(emailFetched==etEmail.text.toString().lowercase()){
                                val builder = androidx.appcompat.app.AlertDialog.Builder(this@signup)
                                builder.setTitle("Error")
                                builder.setMessage("Email Already Exists.")
                                builder.setCancelable(false)
                                builder.setPositiveButton("Ok") { _, _ ->
                                }
                                builder.create()
                                builder.show()
                            }
                        }
                        else{
                            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val currentDate = df.format(Calendar.getInstance().time)
                            val jsonParams = JSONObject()
                            val queue = Volley.newRequestQueue(this@signup)
                            jsonParams.put(
                                "query",
                                "INSERT INTO `user_detail` (`fname`, `lname`, `name`, `mobile`, `email`, `address`, `pincode`, `username`, `passowrd`, `balance`, `user_status`, `creationDate`) VALUES ('${etFirstName.text}', '${etLastName.text}', '${etShopName.text}', '${etMobile.text}', '${etEmail.text}', '${etAddress.text}', '${etPincode.text}', '${etMobile.text}', '${newPassword}', '0', '0', '${currentDate}')"
                            )
                            val url =
                                "$link/dynamic/update"
                            val jsonRequest = object : JsonObjectRequest(
                                Method.POST, url, jsonParams,
                                Response.Listener {
                                    //Toast.makeText(this, "${it}", Toast.LENGTH_SHORT).show()
                                    //Log.e("error", it.toString())
                                    if (it.toString() == "{\"fieldCount\":0,\"affectedRows\":1,\"insertId\":0,\"serverStatus\":2,\"warningCount\":0,\"message\":\"\",\"protocol41\":true,\"changedRows\":0}") {
                                        animationLL.visibility= View.INVISIBLE
                                        sprinkled.visibility=View.VISIBLE
                                        val builder = androidx.appcompat.app.AlertDialog.Builder(this@signup)
                                        builder.setTitle("Congratulations!")
                                        builder.setMessage("Your Account has been created successfully.")
                                        builder.setCancelable(false)
                                        builder.setPositiveButton("OK") { _, _ ->
                                            startActivity(Intent(this,LoginActivity::class.java))
                                            finishAffinity()
                                        }
                                        builder.create()
                                        builder.show()

                                    }
                                    else{
                                        animationLL.visibility= View.INVISIBLE
                                        Toast.makeText(this,"E1:Some Error Occurred.",Toast.LENGTH_LONG).show()

                                    }
                                                  },
                            Response.ErrorListener {
                                animationLL.visibility= View.INVISIBLE
                                Toast.makeText(this,"E2:Some Error Occurred",Toast.LENGTH_LONG).show()
                            }
                            )
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
                        progress.hide()
                        Toast.makeText(this,"E3:Some Error Occurred",Toast.LENGTH_LONG).show()
                    }
                )
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
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@signup)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and retry.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                }
                builder.create()
                builder.show()
            }

        }
        etFirstName.addTextChangedListener(watcher)
        etShopName.addTextChangedListener(watcher)
        etMobile.addTextChangedListener(watcher)
        etEmail.addTextChangedListener(watcher)
        etAddress.addTextChangedListener(watcher)
        etPincode.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etConfirmPassword.addTextChangedListener(watcher)
    }
    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern: Pattern
        val specialCharacters = "-@%\\[\\}+'!/#$^?:;,\\(\"\\)~`.*=&\\{>\\]<_"
        val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[$specialCharacters])(?=\\S+$).{8,20}$"
        pattern = Pattern.compile(PASSWORD_REGEX)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }
    fun check() {

        if (fname && sname && mobile && email && address && pincode && password && cpassword) {
            btnSignUp.setBackgroundColor(0xFF4CAF50.toInt())
            btnSignUp.isEnabled = true
        } else {

            btnSignUp.setBackgroundColor(0xDDDDDDDD.toInt())
            btnSignUp.isEnabled = false
        }


        //Toast.makeText(this,"${isValidPassword(password)}",Toast.LENGTH_LONG).show()
    }
    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

}
