package app.tvapplication.billdetail

data class bill_data(
    val account:String,
    val dueDate:String,
    val amount:String,
    var status:String,
    val tId:String,
    val billDate:String,
    val paymentDate:String,
    val customerName:String,
    val customerMobile:String,
    val shopName:String,
    val operatorName:String,
    val shopMobile:String,
    val balance:String

)