
package com.tremtechzim.pos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class MainActivity:AppCompatActivity(){
 lateinit var db:PosDb
 val cart=mutableListOf<CartItem>()
 var currentUser:UserAccount?=null
 override fun onCreate(b:Bundle?){super.onCreate(b);db=PosDb(this);login()}
 fun e(h:String)=EditText(this).apply{hint=h}
 fun b(t:String,a:()->Unit)=Button(this).apply{text=t;setOnClickListener{a()}}
 fun root(t:String)=LinearLayout(this).apply{
    orientation=LinearLayout.VERTICAL
    setPadding(18,18,18,18)
    addView(TextView(this@MainActivity).apply{
        text=t
        textSize=24f
    })
}
 
 
 fun aboutScreen(){val r=root("About Trem-Tech POS");r.addView(titleView("Trem-Tech POS"));r.addView(TextView(this).apply{text="Business Management & Point of Sale\\n\\nVersion 1.12\\nBuilt for Trem-Tech Zim\\n\\nModules\\n• Sales & payments\\n• Stock & IMEI/serial tracking\\n• Customers, credit & layby\\n• Suppliers & purchasing\\n• Profit & reports\\n• Barcode / QR workflow\\n• Receipts & statements\\n• Multi-location inventory\\n• Backup & restore\\n• Users, roles & audit trail";textSize=17f;setPadding(20,10,20,20)});r.addView(b("BACK"){dash()});setContentView(r)}

 fun login(){
  val r=root("Trem-Tech POS Login");val u=e("Username");val p=e("Password");p.inputType=129;r.addView(u);r.addView(p)
  r.addView(b("LOGIN"){val x=db.verify(u.text.toString(),p.text.toString());if(x==null)toast("Invalid login")else{currentUser=x;dash()}})
  r.addView(TextView(this).apply{text="Default first-run account: admin / admin — change it immediately.";textSize=14f})
  setContentView(r)
 }

 private fun titleView(t:String)=TextView(this).apply{
    text=t
    textSize=24f
    setTextColor(
        androidx.core.content.ContextCompat.getColor(
            this@MainActivity,
            com.tremtechzim.pos.R.color.trem_navy
        )
    )
    setPadding(20,20,20,14)
    setTypeface(typeface,android.graphics.Typeface.BOLD)
}

private fun section(t:String)=TextView(this).apply{
    text=t
    textSize=14f
    setTextColor(
        androidx.core.content.ContextCompat.getColor(
            this@MainActivity,
            com.tremtechzim.pos.R.color.trem_blue
        )
    )
    setPadding(20,18,20,8)
    setTypeface(typeface,android.graphics.Typeface.BOLD)
}

private fun metric(t:String)=TextView(this).apply{
    text=t
    textSize=17f
    setTextColor(
        androidx.core.content.ContextCompat.getColor(
            this@MainActivity,
            com.tremtechzim.pos.R.color.trem_text
        )
    )
    setPadding(20,16,20,16)
    setBackgroundColor(
        androidx.core.content.ContextCompat.getColor(
            this@MainActivity,
            com.tremtechzim.pos.R.color.trem_card
        )
    )
}

 fun dash(){val r=root("Trem-Tech POS");val x=db.totals();r.addView(TextView(this).apply{text="Today\\nSales: $${x.sales}\\nExpenses: $${x.expenses}\\nNet: $${x.net}";textSize=18f})
  r.addView(b("🛒 NEW SALE"){checkout()});r.addView(b("🔎 STOCK SEARCH"){searchScreen()});r.addView(b("📷 SCAN BARCODE / QR"){scanner()});r.addView(b("🧾 RECEIPTS"){receipts()});r.addView(b("👥 CUSTOMERS"){customer()});r.addView(b("💳 CREDIT / LAYBY ACCOUNTS"){accounts()});r.addView(b("📊 REPORTS & DASHBOARD"){reports()});r.addView(b("📦 STOCK MOVEMENTS"){movementScreen()});r.addView(b("🏢 SUPPLIER REPORTS"){supplierReport()});if(currentUser?.role=="ADMIN")r.addView(b("🔐 USERS & PERMISSIONS"){usersScreen()});r.addView(b("🛡️ SECURITY & AUDIT"){securityScreen()});if(currentUser?.role=="ADMIN"||currentUser?.role=="MANAGER")r.addView(b("📤 EXPORT / BACKUP"){exportScreen()});r.addView(b("🏢 SUPPLIER BALANCES"){supplierBalancesScreen()});if(currentUser?.role=="ADMIN"||currentUser?.role=="MANAGER")r.addView(b("↩️ PROCESS REFUND"){refundScreen()});r.addView(b("📜 REFUND HISTORY"){refundsScreen()});r.addView(b("🔄 STOCK TRANSFERS"){transfersScreen()});r.addView(b("📍 LOCATION INVENTORY"){locationStockScreen()});r.addView(b("ℹ️ ABOUT TREM-TECH POS"){aboutScreen()});r.addView(b("🚪 LOG OUT"){currentUser=null;login()});r.addView(b("💰 EXPENSE"){expense()});setContentView(r)}
 fun searchScreen(){val r=root("Stock Search");val q=e("Name / model / SKU / IMEI / serial / barcode / QR");val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};r.addView(q);r.addView(list)
  fun run(){list.removeAllViews();db.search(q.text.toString()).forEach{item->list.addView(b("${item.name} ${item.brand} ${item.model}\\n${item.identifier} • $${item.price} • ${item.quantity} available"){cart+=CartItem(item.kind,item.id,item.productId,item.name,item.price,1.0);toast("Added to cart");checkout()})}}
  r.addView(b("SEARCH"){run()});r.addView(b("📷 SCAN"){scanner()});r.addView(b("BACK"){dash()});setContentView(r)}
 fun scanner(){
  if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),77);toast("Camera permission requested");return}
  val frame=FrameLayout(this);val preview=PreviewView(this);frame.addView(preview,ViewGroup.LayoutParams(-1,-1));val close=b("CLOSE"){dash()};frame.addView(close,FrameLayout.LayoutParams(-1,150));setContentView(frame)
  val provider=ProcessCameraProvider.getInstance(this);provider.addListener({
   val p=provider.get();val previewUse=Preview.Builder().build();previewUse.setSurfaceProvider(preview.surfaceProvider)
   val analyzer=ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
   val scanner=BarcodeScanning.getClient();val exec=Executors.newSingleThreadExecutor()
   analyzer.setAnalyzer(exec){imageProxy->
    val media=imageProxy.image
    if(media!=null){val img=InputImage.fromMediaImage(media,imageProxy.imageInfo.rotationDegrees);scanner.process(img).addOnSuccessListener{codes->
      val value=codes.firstOrNull()?.rawValue
      if(!value.isNullOrBlank()){imageProxy.close();exec.shutdown();provider.get().unbindAll();runOnUiThread{findScanned(value)}} else imageProxy.close()
    }.addOnFailureListener{imageProxy.close()}}else imageProxy.close()
   }
   p.unbindAll();p.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,previewUse,analyzer)
  },ContextCompat.getMainExecutor(this))
 }
 fun findScanned(value:String){val results=db.search(value);if(results.isEmpty()){toast("No available stock found for $value");scanner()}else{val i=results.first();cart+=CartItem(i.kind,i.id,i.productId,i.name,i.price,1.0);toast("Scanned: ${i.name}");checkout()}}
 fun checkout(){
  val r=root("Checkout");val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};r.addView(list)
  fun refresh(){list.removeAllViews();if(cart.isEmpty())list.addView(TextView(this).apply{text="Cart is empty.";textSize=18f})else cart.forEachIndexed{i,x->list.addView(TextView(this).apply{text="${i+1}. ${x.name} × ${x.qty.toInt()} @ $${x.price} = $${"%.2f".format(x.price*x.qty)}";textSize=17f})};list.addView(TextView(this).apply{text="TOTAL: $${"%.2f".format(cart.sumOf{it.price*it.qty})}";textSize=21f})};refresh()
  r.addView(b("🔎 ADD ITEM"){searchScreen()});r.addView(b("📷 SCAN ITEM"){scanner()});r.addView(b("🗑 CLEAR"){cart.clear();refresh()})
  val cust=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Walk-in customer")+db.customers().map{it.second})};r.addView(cust)
  val pay=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("USD Cash","ZiG Cash","EcoCash","Bank Transfer","Card","Credit","Layby","Split Payment"))};r.addView(pay)
  val paid=e("Amount paid / deposit");val due=e("Credit due date (YYYY-MM-DD)").apply{setText(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()))};val ref=e("Payment / bank / terminal reference")
  r.addView(paid);r.addView(due);r.addView(ref)
  r.addView(b("COMPLETE SALE"){
   if(cart.isEmpty()){toast("Cart is empty");return@b}
   val method=pay.selectedItem.toString();val total=cart.sumOf{it.price*it.qty};val p=paid.text.toString().toDoubleOrNull()?:if(method=="USD Cash"||method=="ZiG Cash"||method=="EcoCash"||method=="Bank Transfer"||method=="Card")total else 0.0
   if((method=="Credit"||method=="Layby") && cust.selectedItemPosition==0){toast("Select a customer for credit/layby");return@b}
   if(method=="Credit"&&p>total){toast("Payment exceeds total");return@b}
   val cid=if(cust.selectedItemPosition==0)null else db.customers()[cust.selectedItemPosition-1].first
   val id=db.completeSaleWithTerms(cart,method,cid,p,due.text.toString(),ref.text.toString());cart.clear();receiptDetail(id);})
  r.addView(b("BACK"){dash()});setContentView(r)}
 fun receiptDetail(id:Long){val rs=db.receipts(50).firstOrNull{it.id==id};val r=root("Receipt #$id");if(rs!=null){r.addView(TextView(this).apply{text="Trem-Tech Zim\\nReceipt #${rs.id}\\n${rs.createdAt}\\nCustomer: ${rs.customer}\\nPayment: ${rs.payment}\\nStatus: ${rs.status}\\nTOTAL: $${"%.2f".format(rs.total)}\\n\\nThank you for your business.";textSize=18f});r.addView(b("📄 CREATE PDF RECEIPT"){val f=ReceiptPdf.create(this,rs,listOf("Sale #${rs.id}","Payment: ${rs.payment}","Customer: ${rs.customer}","Total: $${"%.2f".format(rs.total)}"));toast("PDF created: ${f.name}")});r.addView(b("📤 SHARE / SEND RECEIPT"){shareReceipt(rs)})};r.addView(b("BACK"){dash()});setContentView(r)}
 fun shareReceipt(rs:Receipt?){if(rs==null)return;val i=android.content.Intent(android.content.Intent.ACTION_SEND);i.type="text/plain";i.putExtra(android.content.Intent.EXTRA_TEXT,"Trem-Tech Zim Receipt #${rs.id}\\nTotal: $${rs.total}\\nPayment: ${rs.payment}\\nThank you for your business.");startActivity(android.content.Intent.createChooser(i,"Send receipt"))}
 fun receipts(){val r=root("Receipt History");db.receipts().forEach{rs->r.addView(b("#${rs.id} • ${rs.customer} • $${"%.2f".format(rs.total)} • ${rs.payment}"){receiptDetail(rs.id)})};r.addView(b("BACK"){dash()});setContentView(r)}
 fun customer(){
    val r=root("New Customer")
    val a=listOf(
        e("Name"),
        e("Phone"),
        e("WhatsApp"),
        e("Email"),
        e("Address")
    )

    a.forEach{r.addView(it)}

    r.addView(b("SAVE"){
        if(a[0].text.isBlank()){
            toast("Name required")
        }else{
            db.addCustomer(
                a[0].text.toString(),
                a[1].text.toString(),
                a[2].text.toString(),
                a[3].text.toString(),
                a[4].text.toString()
            )
            toast("Saved")
            dash()
        }
    })

    r.addView(b("BACK"){dash()})
    setContentView(r)
}
 fun expense(){val r=root("Expense");val d=e("Description");val a=e("Amount");r.addView(d);r.addView(a);r.addView(b("SAVE"){val v=a.text.toString().toDoubleOrNull();if(v==null||v<=0)toast("Enter valid amount")else{db.addExpense(d.text.toString(),v);toast("Saved");dash()}});r.addView(b("BACK"){dash()});setContentView(r)}
 
 
 
 
 
 
 
 
 fun restoreScreen() {
    val r = root("Restore Database")
    val xs = BackupManager.list(this)

    if (xs.isEmpty()) {
        r.addView(
            TextView(this).apply {
                text = "No verified backups available."
                textSize = 16f
            }
        )
    } else {
        xs.forEach { x ->
            r.addView(
                b("RESTORE ${x.file.name}") {
                    if (!BackupManager.validate(x.file)) {
                        toast("Backup failed validation")
                    } else {
                        try {
                            val emergency = RestoreManager.restore(this, x.file)
                            toast("Restored. Emergency backup: ${emergency.name}")
                            login()
                        } catch (t: Throwable) {
                            toast("Restore failed: ${t.message}")
                        }
                    }
                }
            )
        }
    }

    r.addView(b("BACK") {
        exportScreen()
    })

    setContentView(r)
}
 fun locationStockScreen(){val r=root("Location Inventory");db.locationStock().forEach{x->r.addView(TextView(this).apply{text="${x.location}\\n${x.product}: ${"%.2f".format(x.quantity)} units • Cost $${"%.2f".format(x.value)}";textSize=16f})};r.addView(b("BACK"){dash()});setContentView(r)}
fun backupVerifyScreen() {
    val r = root("Backup Verification")
    val xs = BackupManager.list(this)

    if (xs.isEmpty()) {
        r.addView(
            TextView(this).apply {
                text = "No backups found."
                textSize = 16f
            }
        )
    } else {
        xs.forEach { x ->
            r.addView(
                TextView(this).apply {
                    text = "${x.file.name}\n" +
                           "Valid: ${BackupManager.validate(x.file)}\n" +
                           "SHA-256: ${x.sha256}"
                    textSize = 14f
                }
            )
        }
    }

    r.addView(b("BACK") {
        exportScreen()
    })

    setContentView(r)
}
 fun refundScreen(){val r=root("Refund Sale");val sid=e("Sale ID");val amt=e("Refund amount");val reason=e("Reason");r.addView(sid);r.addView(amt);r.addView(reason);r.addView(b("PROCESS REFUND"){val s=sid.text.toString().toLongOrNull();val v=amt.text.toString().toDoubleOrNull();if(s==null||v==null||v<=0||reason.text.isBlank())toast("Enter sale, amount and reason")else{db.refundSale(s,v,reason.text.toString(),currentUser?.id);toast("Refund recorded");dash()}});r.addView(b("BACK"){dash()});setContentView(r)}
 fun refundsScreen(){val r=root("Refund History");db.refunds().forEach{x->r.addView(TextView(this).apply{text="#${x.id} Sale #${x.saleId} — $${"%.2f".format(x.amount)}\\n${x.reason}\\n${x.date} • ${x.user}";textSize=15f})};r.addView(b("BACK"){dash()});setContentView(r)}

 fun supplierBalancesScreen(){
  val r=root("Supplier Balances");db.supplierBalances().forEach{s->r.addView(TextView(this).apply{text="${s.name}\\nPurchased: $${"%.2f".format(s.purchased)} • Paid: $${"%.2f".format(s.paid)} • Balance: $${"%.2f".format(s.balance)}";textSize=16f});if(s.balance>0.01)r.addView(b("PAY ${s.name}"){supplierPay(s.supplierId,s.name,s.balance)})}
  r.addView(b("BACK"){dash()});setContentView(r)
 }
 fun supplierPay(id:Long,name:String,max:Double){val r=root("Supplier Payment");val a=e("Amount (max $${"%.2f".format(max)})");val ref=e("Reference");val m=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("USD Cash","Bank Transfer","EcoCash","Other"))};r.addView(a);r.addView(m);r.addView(ref);r.addView(b("SAVE"){val v=a.text.toString().toDoubleOrNull();if(v==null||v<=0||v>max)toast("Enter a valid amount")else{db.supplierPayment(id,v,m.selectedItem.toString(),ref.text.toString(),currentUser?.id);toast("Supplier payment recorded");supplierBalancesScreen()}});r.addView(b("BACK"){supplierBalancesScreen()});setContentView(r)}
 fun transfersScreen(){val r=root("Stock Transfers");db.transfers().forEach{x->r.addView(TextView(this).apply{text="#${x.id} ${x.product} × ${x.qty}\\n${x.from} → ${x.to}\\n${x.date} • ${x.status}";textSize=16f})};val loc=db.locations();if(loc.size>=2){val prod=e("Product ID");val qty=e("Quantity");val ref=e("Reference");r.addView(prod);r.addView(qty);r.addView(ref);r.addView(b("CREATE TRANSFER"){val pi=prod.text.toString().toLongOrNull();val q=qty.text.toString().toDoubleOrNull();if(pi==null||q==null||q<=0)toast("Enter valid product ID and quantity")else{db.createTransfer(pi,q,loc[0].id,loc[1].id,ref.text.toString(),currentUser?.id);toast("Transfer recorded");transfersScreen()}})}else r.addView(b("ADD SECOND LOCATION"){val n=e("Location name");r.addView(n);db.addLocation("Warehouse")});r.addView(b("BACK"){dash()});setContentView(r)}

 fun securityScreen(){
  val r=root("Security & Audit")
  r.addView(TextView(this).apply{text="Logged in: ${currentUser?.username}\\nRole: ${currentUser?.role}";textSize=18f})
  r.addView(b("CHANGE MY PASSWORD"){changeMyPassword()})
  if(currentUser?.role=="ADMIN")r.addView(b("VIEW AUDIT LOG"){auditScreen()})
  r.addView(b("BACK"){dash()});setContentView(r)
 }
 fun changeMyPassword(){val r=root("Change Password");val p=e("New password");p.inputType=129;val q=e("Confirm password");q.inputType=129;r.addView(p);r.addView(q);r.addView(b("SAVE"){if(p.text.length<6)toast("Use at least 6 characters")else if(p.text.toString()!=q.text.toString())toast("Passwords do not match")else{db.changePassword(currentUser!!.id,p.text.toString());db.audit(currentUser!!.id,"PASSWORD_CHANGED");toast("Password changed");securityScreen()}});r.addView(b("BACK"){securityScreen()});setContentView(r)}
 fun auditScreen(){val r=root("Audit Log");db.auditRows().forEach{x->r.addView(TextView(this).apply{text=x;textSize=14f})};r.addView(b("BACK"){securityScreen()});setContentView(r)}

 fun exportScreen(){
  val r=root("Export & Backup")
  r.addView(b("Products CSV"){shareFile(ExportUtil.csv(this,"products",db.exportRows("products")))})
  r.addView(b("Customers CSV"){shareFile(ExportUtil.csv(this,"customers",db.exportRows("customers")))})
  r.addView(b("Sales CSV"){shareFile(ExportUtil.csv(this,"sales",db.exportRows("sales")))})
  r.addView(b("Stock Movements CSV"){shareFile(ExportUtil.csv(this,"movements",db.exportRows("movements")))})
  r.addView(b("Sales Excel (.xlsx)"){shareFile(ExportUtil.xlsx(this,"sales",db.exportRows("sales")))})
  r.addView(b("📊 PROFIT REPORT"){profitReport()})
  r.addView(b("⚠️ OVERDUE CREDIT"){overdueReport()})
  r.addView(b("CREATE VERIFIED BACKUP"){val x=BackupManager.create(this);toast("Backup: ${x.file.name}\nSHA-256: ${x.sha256.take(12)}…")})
  r.addView(b("VERIFY BACKUPS"){backupVerifyScreen()});r.addView(b("⚠️ RESTORE BACKUP"){restoreScreen()})
  r.addView(b("BACK"){dash()});setContentView(r)
 }
 fun shareFile(f:java.io.File){toast("Created ${f.name}");val u=androidx.core.content.FileProvider.getUriForFile(this,"com.tremtechzim.pos.fileprovider",f);val i=android.content.Intent(android.content.Intent.ACTION_SEND);i.type="application/octet-stream";i.putExtra(android.content.Intent.EXTRA_STREAM,u);i.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(android.content.Intent.createChooser(i,"Share file"))}
 fun backupDatabase(){val f=ExportUtil.backup(this,getDatabasePath("tremtech_pos.db"));toast("Backup created: ${f.name}")}
 fun profitReport(){val r=root("Gross Profit");db.profitRows().forEach{x->r.addView(TextView(this).apply{text="#${x.saleId} ${x.date}\\nRevenue $${"%.2f".format(x.revenue)} • Cost $${"%.2f".format(x.cost)} • Profit $${"%.2f".format(x.profit)}";textSize=16f})};r.addView(b("BACK"){exportScreen()});setContentView(r)}
 fun overdueReport(){val r=root("Overdue Credit");val rows=db.overdueCredits();if(rows.isEmpty())r.addView(TextView(this).apply{text="No overdue credit accounts."})else rows.forEach{(n,v)->r.addView(TextView(this).apply{text="$n — $${"%.2f".format(v)} overdue";textSize=17f})};r.addView(b("BACK"){exportScreen()});setContentView(r)}

 fun movementScreen(){val r=root("Stock Movement History");db.movements().forEach{x->r.addView(TextView(this).apply{text="${x.date}\\n${x.type} • ${x.product} • ${x.qty} • ${x.reference}";textSize=16f})};r.addView(b("BACK"){dash()});setContentView(r)}
 fun supplierReport(){val r=root("Supplier Purchasing");val rows=db.supplierPurchases();if(rows.isEmpty())r.addView(TextView(this).apply{text="No purchasing records yet."})else rows.forEach{(n,v)->r.addView(TextView(this).apply{text="$n: $${"%.2f".format(v)}";textSize=17f})};r.addView(b("BACK"){dash()});setContentView(r)}
 fun usersScreen(){val r=root("Users & Permissions");db.users().forEach{x->r.addView(TextView(this).apply{text="${x.username} — ${x.role}";textSize=17f})};val u=e("New username");val p=e("Temporary password");p.inputType=129;val roles=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("CASHIER","MANAGER","ADMIN"))};r.addView(u);r.addView(p);r.addView(roles);r.addView(b("ADD USER"){if(u.text.isBlank()||p.text.isBlank())toast("Enter username and password")else{try{db.addUser(u.text.toString(),p.text.toString(),roles.selectedItem.toString());toast("User added");usersScreen()}catch(e:Exception){toast("Username already exists")}}});r.addView(b("BACK"){dash()});setContentView(r)}

 fun reports(){
  val d=db.dashboard();val r=root("Business Dashboard")
  r.addView(TextView(this).apply{text="TODAY\\nSales: $${"%.2f".format(d.sales)}\\nExpenses: $${"%.2f".format(d.expenses)}\\nNet before stock cost: $${"%.2f".format(d.sales-d.expenses)}\\nTransactions: ${d.transactions}\\n\\nSTOCK\\nStock cost value: $${"%.2f".format(d.stockValue)}\\nCredit outstanding: $${"%.2f".format(d.receivables)}\\nLayby outstanding: $${"%.2f".format(d.layby)}";textSize=18f})
  r.addView(TextView(this).apply{text="\\nSALES BY PAYMENT";textSize=18f})
  db.salesByPayment().forEach{(m,v)->r.addView(TextView(this).apply{text="$m: $${"%.2f".format(v)}"})}
  r.addView(TextView(this).apply{text="\\nLAST 7 DAYS";textSize=18f})
  db.report(7).forEach{x->r.addView(TextView(this).apply{text="${x.label}: Sales $${"%.2f".format(x.sales)} | Expenses $${"%.2f".format(x.expenses)} | Net $${"%.2f".format(x.profit)}"})}
  r.addView(TextView(this).apply{text="\\nLOW STOCK (≤3)";textSize=18f})
  val low=db.lowStock();if(low.isEmpty())r.addView(TextView(this).apply{text="No low-stock batch items."}) else low.forEach{(n,q)->r.addView(TextView(this).apply{text="$n — ${"%.0f".format(q)} left"})}
  r.addView(b("BACK"){dash()});setContentView(r)
 }

 fun accounts(){
  val r=root("Credit / Layby Accounts");val people=db.customersDetailed()
  if(people.isEmpty())r.addView(TextView(this).apply{text="No customers yet.";textSize=18f})
  people.forEach{(id,name,phone)->r.addView(b("$name  $phone"){accountDetail(id,name)})}
  r.addView(b("BACK"){dash()});setContentView(r)
 }
 fun accountDetail(cid:Long,name:String){
  val r=root(name+" — Accounts");val tx=db.customerAccounts(cid)
  if(tx.isEmpty())r.addView(TextView(this).apply{text="No credit or layby accounts.";textSize=18f})
  tx.forEach{t->
   r.addView(TextView(this).apply{text="${t.kind} #${t.id}\\nOriginal: $${"%.2f".format(t.amount)}   Balance: $${"%.2f".format(t.balance)}\\nStatus: ${t.status}  ${t.date}";textSize=17f})
   if(t.balance>0.00001)r.addView(b("💵 RECORD PAYMENT"){accountPayment(cid,t)})
  }
  r.addView(b("BACK"){accounts()});setContentView(r)
 }
 fun accountPayment(cid:Long,t:AccountTransaction){
  val r=root("Payment — ${t.kind} #${t.id}");val amt=e("Amount");val ref=e("Reference");val m=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("USD Cash","ZiG Cash","EcoCash","Bank Transfer","Card","Other"))};r.addView(TextView(this).apply{text="Outstanding: $${"%.2f".format(t.balance)}";textSize=18f});r.addView(amt);r.addView(m);r.addView(ref)
  r.addView(b("SAVE PAYMENT"){val v=amt.text.toString().toDoubleOrNull();if(v==null||v<=0)toast("Enter amount")else if(db.recordAccountPayment(cid,t.kind,t.id,v,m.selectedItem.toString(),ref.text.toString())){toast("Payment recorded");accountDetail(cid,"Customer")}else toast("Could not record payment")})
  r.addView(b("BACK"){accountDetail(cid,"Customer")});setContentView(r)
 }

 fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
}
