package app.tvapplication.tvapp


import android.Manifest
import android.R.id.message
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var webView: WebView
    lateinit var txtOutlet: TextView
    lateinit var txtMobile: TextView
    lateinit var mView:View
    lateinit var Mobile:EditText
    var CusMobile=""
    var Amount = ""
    var Account = ""
    var dateTime: String = ""
    var tts: TextToSpeech? = null
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        sendMessage()
        mView = layoutInflater.inflate(R.layout.dialog, null)
        Mobile= mView.findViewById(R.id.txtValidity)
        if (ConnectionManager().checkConnectivity(this)) {
            setContentView(R.layout.activity_main)
            verifyStoragePermissions(this@MainActivity)
            webView = findViewById(R.id.WebView)
            val btnSubmit: Button = findViewById(R.id.btnSubmit)
            loadPage("https://www.amazon.in/hfc/bill/electricity")
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    webView.evaluateJavascript(
                        """
                |var _fullScrollIntervalID = setInterval(function() {
                |    if (window.scrollY + window.innerHeight >= document.body.scrollHeight) {
                |        window.clearInterval(_fullScrollIntervalID);
                |    } else {
                |        window.scrollBy(0, 10);
                |    }
                |}, 17);
            """.trimMargin(), null
                    )
                    webView.loadUrl("javascript:(function(){" + "document.getElementById('a-autoid-2-announce').style.display='none';})()")
                }
            }
            btnSubmit.setOnClickListener {
                btnshowMessage()

            }
            var btnShare = findViewById<Button>(R.id.btnShare)
            btnShare.setOnClickListener {
                finish()
                intent = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(intent)
            }
        } else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                ActivityCompat.finishAffinity(this)
            }
            builder.create()
            builder.show()
        }
    }


    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    fun getScreenShot(view: View): Bitmap {
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(screenView.drawingCache)
        screenView.isDrawingCacheEnabled = false
        return bitmap
    }

    fun store(bm: Bitmap, fileName: String?) {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val dirPath: String =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                .toString() + "/DCIM/Screenshots/"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {
            val fOut = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_LONG).show()
        }
        shareImage(file)
    }

    private fun shareImage(file: File) {
        val uri = Uri.fromFile(file)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this@MainActivity, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadPage(url: String) {
        webView.visibility = View.VISIBLE
        webView.webViewClient = WebViewClient()
        webView.settings.loadsImagesAutomatically = false
        webView.settings.setSupportZoom(true)
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = false
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webView.settings.domStorageEnabled = true
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webView.settings.useWideViewPort = true
        webView.settings.savePassword = true
        webView.settings.saveFormData = true
        //webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
        webView.settings.setEnableSmoothTransition(true)
        webView.loadUrl(url)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                view.loadUrl(url)
                return true
            }
        }
    }

    fun btnshowMessage() {
        if (ConnectionManager().checkConnectivity(this)) {
            val alert = AlertDialog.Builder(this@MainActivity)
            val account: EditText = mView.findViewById(R.id.txtMobile)
            val amount: EditText = mView.findViewById(R.id.txtValidity)
            val btn_cancel = mView.findViewById(R.id.btn_cancel) as Button
            val btn_okay = mView.findViewById(R.id.btn_okay) as Button
            alert.setView(mView)
            val alertDialog = alert.create()
            val Min = 0
            val Max = 200
            val x = Min + (Math.random() * (Max - Min + 1)).toInt()
            val y = Min + (Math.random() * (Max - Min + 1)).toInt()
            alertDialog.cancel()
            val wmlp: WindowManager.LayoutParams? = alertDialog.window?.attributes

            wmlp?.gravity = Gravity.TOP or Gravity.LEFT
            wmlp?.x = x
            wmlp?.y = y
            alertDialog.show()
            alertDialog.setCanceledOnTouchOutside(false)
            btn_cancel.setOnClickListener {
                Toast.makeText(this, "Cancel Clicked", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }
            btn_okay.setOnClickListener {
                Account = account.text.toString()
                Amount = amount.text.toString()
                CusMobile= "+91${Mobile.text}"
                if (account.length() != 10) {
                    account.error = "Enter Correct Account number"

                } else if (Amount.isNullOrEmpty()) {
                    amount.error = "Enter Amount"
                }
                /*else if(CusMobile.length !=10){
                    CusMobile="+910000000000"
                }*/
                else {
                    val progressLayout = findViewById<LinearLayout>(R.id.LLP)
                    progressLayout.visibility = View.VISIBLE
                    btnSubmit.visibility = View.GONE
                    val df: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                    val Df: DateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                    val currentDate = Df.format(Calendar.getInstance().time)
                    var date: TextView = findViewById(R.id.date)
                    var linearLayout: LinearLayout = findViewById(R.id.LL)
                    var Tid: TextView = findViewById(R.id.Tid)
                    tts = TextToSpeech(this, this)
                    var tid = "JCC" + "$currentDate"
                    dateTime = df.format(Calendar.getInstance().time)
                    val queue = Volley.newRequestQueue(this@MainActivity)
                    val url =
                        "http://54.90.235.144:3001/api/v1/bbps_validate.php?userid=adnan.jafr&key=318184005984622&operator=UPE&amount=10&orderid=${tid}&customer_mobile=8477086932&customer_name=ABCD&optional1=xx&optional2=xx&optional3=xx&service=${Account}"
                    val jsonRequest = object : JsonObjectRequest(
                        Method.GET, url, null,
                        Response.Listener {
                            try {
                                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                                val amount = it.getString("dueamount")
                                val duedate = it.getString("duedate")
                                if (Amount != amount) {

                                    val dialog = AlertDialog.Builder(this@MainActivity)
                                    dialog.setTitle("Error")
                                    dialog.setMessage("Invalid account no or amount!")
                                    dialog.setNegativeButton("ok") { text, listener ->
                                        progressLayout.visibility = View.GONE
                                        btnSubmit.visibility = View.VISIBLE

                                    }

                                    dialog.create()
                                    dialog.setCancelable(false)
                                    dialog.show()

                                } else {
                                    val url =
                                        "https://adnanjafri783.wixsite.com/website-1/_functions/myFunction/"
                                    val jsonParams = JSONObject()
                                    jsonParams.put("account", Account)
                                    jsonParams.put("amount", Amount)
                                    jsonParams.put("date", duedate)
                                    jsonParams.put("mobile", CusMobile)
                                    jsonParams.put("status", "Unpaid")
                                    jsonParams.put("paymentGateway", "")
                                    jsonParams.put("tId", tid)
                                    val jsonRequest = object : JsonObjectRequest(
                                        Method.POST, url, jsonParams,
                                        Response.Listener {
                                            date.text = "on $dateTime"
                                            txtOutlet = findViewById(R.id.txtOutlet)
                                            txtMobile = findViewById(R.id.txtMobile)
                                            txtOutlet.visibility = View.GONE
                                            txtMobile.visibility = View.GONE
                                            linearLayout.visibility = View.VISIBLE
                                            Tid.text = "Transaction ID: JCC" + "$currentDate"
                                            btnSubmit.text = "Print or Share"
                                            speakOut()
                                            sendMessage()
                                            progressLayout.visibility = View.GONE
                                            btnSubmit.visibility = View.VISIBLE
                                            btnShare.visibility = View.VISIBLE
                                            btnSubmit.setOnClickListener {

                                                btnSubmit.visibility = View.GONE
                                                btnShare.visibility = View.GONE
                                                val rootView =
                                                    window.decorView.findViewById<View>(android.R.id.content)
                                                var bitmap: Bitmap = getScreenShot(rootView)
                                                store(bitmap, "JCC${currentDate}.png")
                                                btnSubmit.visibility = View.VISIBLE
                                                btnShare.visibility = View.VISIBLE
                                            }
                                            try {
                                                //Toast.makeText(this@MainActivity, "$it", Toast.LENGTH_LONG).show()
                                            } catch (e1: JSONException) {
                                                Toast.makeText(
                                                    this,
                                                    "Some Error Occurred ",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                                progressLayout.visibility = View.GONE
                                                btnSubmit.visibility = View.VISIBLE
                                            }
                                        },
                                        Response.ErrorListener {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Some Error Occurred",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            progressLayout.visibility = View.GONE
                                            btnSubmit.visibility = View.VISIBLE


                                        }) {
                                        override fun getHeaders(): MutableMap<String, String> {
                                            val headers = HashMap<String, String>()
                                            //headers["Content-type"] = "application/json"
                                            return headers
                                        }
                                    }
                                    queue.add(jsonRequest)

                                }

                            } catch (e: JSONException) {
                                Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
                                progressLayout.visibility = View.GONE
                                btnSubmit.visibility = View.VISIBLE
                            }

                        },
                        Response.ErrorListener {
                            Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                            progressLayout.visibility = View.GONE
                            btnSubmit.visibility = View.VISIBLE
                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            //headers["Content-type"] = "application/json"
                            return headers

                        }
                    }
                    queue.add(jsonRequest)
                    /*val url = "https://adnanjafri783.wixsite.com/website-1/_functions/myFunction/"
                    val jsonParams = JSONObject()
                    jsonParams.put("account", Account)
                    jsonParams.put("amount", Amount)
                    jsonParams.put("date", dateTime)
                    jsonParams.put("mobile", Mobile)
                    jsonParams.put("status", "Unpaid")
                    jsonParams.put("paymentGateway", "")
                    jsonParams.put("tId", tid)
                    val jsonRequest = object : JsonObjectRequest(
                        Method.POST, url, jsonParams,
                        Response.Listener {
                            date.text = "on $dateTime"
                            txtOutlet = findViewById(R.id.txtOutlet)
                            txtMobile = findViewById(R.id.txtMobile)
                            txtOutlet.visibility = View.GONE
                            txtMobile.visibility = View.GONE
                            linearLayout.visibility = View.VISIBLE
                            Tid.text = "Transaction ID: JCC" + "$currentDate"
                            btnSubmit.text = "Print or Share"
                            speakOut()
                            progressLayout.visibility = View.GONE
                            btnSubmit.visibility = View.VISIBLE
                            btnShare.visibility = View.VISIBLE
                            btnSubmit.setOnClickListener {

                                btnSubmit.visibility = View.GONE
                                btnShare.visibility = View.GONE
                                val rootView =
                                    window.decorView.findViewById<View>(android.R.id.content)
                                var bitmap: Bitmap = getScreenShot(rootView)
                                store(bitmap, "JCC${currentDate}.png")
                                btnSubmit.visibility = View.VISIBLE
                                btnShare.visibility = View.VISIBLE
                            }
                            try {
                                //Toast.makeText(this@MainActivity, "$it", Toast.LENGTH_LONG).show()
                            } catch (e1: JSONException) {
                                Toast.makeText(this, "Some Error Occured ", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        Response.ErrorListener {
                            Toast.makeText(
                                this@MainActivity,
                                "Some Error Occured",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            progressLayout.visibility = View.GONE
                            btnSubmit.visibility = View.VISIBLE


                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            //headers["Content-type"] = "application/json"
                            return headers
                        }
                    }
                    queue.add(jsonRequest)*/
                    //Toast.makeText(this, "Okay Clicked", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            }
            alertDialog.show()
        } else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and try again.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->

            }
            builder.create()
            builder.show()

        }
    }

    private fun sendMessage() {
        var df: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val currentDate = df.format(Calendar.getInstance().time)
        val sms: SmsManager = SmsManager.getDefault()
        var message="Bill Payment of Account No ${Account} and Rs.${Amount} has been successfully paid on ${currentDate}. Thank You"
        //Toast.makeText(this,"${CusMobile.length}",Toast.LENGTH_SHORT).show()
        if(CusMobile.length==13){
        sms.sendTextMessage(CusMobile, null, message, null, null)}
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Confirmation")
            dialog.setMessage("Are you sure you want to exit?")
            dialog.setPositiveButton("Yes") { _, _ ->
                ActivityCompat.finishAffinity(this)
            }
            dialog.setNegativeButton("No") { _, _ ->

            }
            dialog.create()
            dialog.setCancelable(false)
            dialog.show()
        }
        return false
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts?.setLanguage(Locale("hi", "IN"))



            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(
                    this@MainActivity,
                    "The Language specified is not supported!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            Toast.makeText(this@MainActivity, "Initialization failed", Toast.LENGTH_SHORT).show()

        }


    }

    private fun speakOut() {
        val text = "बिल भुगतान जिसका मूल्य ${Amount} रुपय है सफलतापूर्वक भरा गया है "
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }}