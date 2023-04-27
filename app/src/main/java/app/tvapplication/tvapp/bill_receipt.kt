package app.tvapplication.tvapp

import android.Manifest
import android.R.attr.bitmap
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.tvapplication.tvapp.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream


class bill_receipt : AppCompatActivity() {
    var username=""
    var shopName=""
    var shopMobile=""
    var name:String?=""
    var amount=""
    var account=""
    var paymentDate=""
    var Tid=""
    lateinit var imgQR:ImageView
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_receipt)
        var operator=""
        var scrollView=findViewById<ScrollView>(R.id.ScrollView)
        var txtamount=findViewById<TextView>(R.id.txtAmountValue)
        var btnShare=findViewById<Button>(R.id.btnShare)
        var txtTitle=findViewById<TextView>(R.id.txtTitleBillSuccess)
        var txtName=findViewById<TextView>(R.id.txtNameValue)
        imgQR=findViewById(R.id.imgQR)
        var txtAccount=findViewById<TextView>(R.id.txtAccountNoValue)
        var txtDueDate=findViewById<TextView>(R.id.txtDueDateValue)
        var txtBillDate=findViewById<TextView>(R.id.txtBillDateValue)
        var txtPaymentDate=findViewById<TextView>(R.id.txtPaymentDateValue)
        var txtTid=findViewById<TextView>(R.id.txtTidValue)
        var txtShopName=findViewById<TextView>(R.id.txtShopNameValue)
        var txtShopMobile=findViewById<TextView>(R.id.txtShopMobileValue)
        var btnNew=findViewById<Button>(R.id.btnNew)
        var txtCharges=findViewById<TextView>(R.id.txtCharges)
        var txtTotal=findViewById<TextView>(R.id.txtTotal)
        var charges=""
        var total=""
        var sharedPreferences=getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
        username=sharedPreferences.getString("username","").toString()
        shopName=sharedPreferences.getString("name","").toString()
        shopMobile=sharedPreferences.getString("mobile","").toString()
        if (sharedPreferences.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            val dialog = AlertDialog.Builder(this@bill_receipt)
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
        btnNew.setOnClickListener{
            startActivity(Intent(this, dashboard::class.java))
            finishAffinity()
        }

        var dueDate=""
        var billDate=""
        var shopName=""
        var shopMobile=""
        amount=intent?.getStringExtra("amount").toString()
        name=intent?.getStringExtra("name").toString()
        account=intent?.getStringExtra("account").toString()
        dueDate=intent?.getStringExtra("dueDate").toString()
        billDate=intent?.getStringExtra("billDate").toString()
        paymentDate=intent?.getStringExtra("paymentDate").toString()
        Tid=intent?.getStringExtra("Tid").toString()
        shopName=intent?.getStringExtra("shopName").toString()
        shopMobile=intent?.getStringExtra("shopMobile").toString()
        operator=intent?.getStringExtra("operator").toString()
        txtName.text=name
        txtamount.text="${resources.getString(R.string.Rs)}${amount}.00"
        txtAccount.text=account
        txtDueDate.text=dueDate
        txtBillDate.text=billDate
        txtPaymentDate.text=paymentDate
        txtTid.text=Tid
        txtShopName.text=shopName
        txtShopMobile.text=shopMobile
        txtTitle.text="Bill Payment of ${operator} has been successful."

        //Calculating Convenience Charges & Total Amount
        if(amount.toInt()<1000){
            charges="10"
            txtCharges.text="Convenience Charges: ${resources.getString(R.string.Rs)}$charges.00"
            total=(amount.toInt()+charges.toInt()).toString()
            txtTotal.text="Total Amount: ${resources.getString(R.string.Rs)}$total.00"
        }
        else{
        charges=((amount.toInt()*1)/100).toString()
        total=(amount.toInt()+charges.toInt()).toString()
        txtCharges.text="Convenience Charges: ${resources.getString(R.string.Rs)}$charges.00"
        txtTotal.text="Total Amount: ${resources.getString(R.string.Rs)}$total.00"}

        btnShare.setOnClickListener {
            verifyStoragePermissions(this)
            btnNew.visibility=View.GONE
            btnShare.visibility = View.GONE
            var bitmap: Bitmap = getScreenShot(scrollView,scrollView.getChildAt(0).getHeight(),scrollView.getChildAt(0).getWidth())
            store(bitmap, "${Tid}.png")
            btnShare.visibility = View.VISIBLE
            btnNew.visibility=View.VISIBLE
        }
        var QR=getQrCodeBitmap()
        imgQR.setImageBitmap(QR)


    }
    fun getScreenShot(view: View, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }
    fun store(bm: Bitmap, fileName: String?) {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val dirPath: String =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                .toString() + "/DCIM/bill_receipt/"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {
            val fOut = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            Toast.makeText(this@bill_receipt, "$e", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this@bill_receipt, "No App Available", Toast.LENGTH_SHORT).show()
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
    fun getQrCodeBitmap(): Bitmap {
        val size = 512 //pixels
        val qrCodeContent = "$link2/bijlee/bill-receipt.php?tid=$Tid"
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}