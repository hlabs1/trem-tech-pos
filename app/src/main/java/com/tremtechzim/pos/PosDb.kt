
package com.tremtechzim.pos
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

data class StockResult(val kind:String,val id:Long,val productId:Long,val name:String,val brand:String,val model:String,val identifier:String,val price:Double,val quantity:Double,val status:String)
data class Totals(val sales:String,val expenses:String,val net:String)
data class Receipt(val id:Long,val total:Double,val payment:String,val status:String,val customer:String,val createdAt:String)
data class AccountSummary(val credit:Double,val layby:Double)
data class AccountTransaction(val kind:String,val id:Long,val amount:Double,val balance:Double,val status:String,val date:String,val description:String)
data class Dashboard(val sales:Double,val expenses:Double,val grossProfit:Double,val stockValue:Double,val receivables:Double,val layby:Double,val transactions:Int)
data class ReportRow(val label:String,val sales:Double,val expenses:Double,val profit:Double)
data class UserAccount(val id:Long,val username:String,val role:String)
data class Movement(val date:String,val type:String,val product:String,val qty:Double,val reference:String)
data class ProfitRow(val saleId:Long,val date:String,val revenue:Double,val cost:Double,val profit:Double)
data class StatementRow(val date:String,val kind:String,val reference:String,val debit:Double,val credit:Double,val balance:Double)
data class SupplierBalance(val supplierId:Long,val name:String,val purchased:Double,val paid:Double,val balance:Double)
data class Location(val id:Long,val name:String)
data class Transfer(val id:Long,val product:String,val qty:Double,val from:String,val to:String,val date:String,val status:String)
data class Refund(val id:Long,val saleId:Long,val amount:Double,val reason:String,val date:String,val user:String)
data class LocationStock(val location:String,val product:String,val quantity:Double,val value:Double)

class PosDb(c:Context):SQLiteOpenHelper(c,"tremtech_pos.db",null,3){
 override fun onCreate(db:SQLiteDatabase){
  db.execSQL("CREATE TABLE suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,contact_person TEXT,phone TEXT,whatsapp TEXT,email TEXT,address TEXT,city TEXT,country TEXT,notes TEXT)")
  db.execSQL("CREATE TABLE product_types(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE,serialized INTEGER)")
  db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT,type_id INTEGER,name TEXT,category TEXT,brand TEXT,model TEXT,barcode TEXT,qr_code TEXT,sku TEXT,warranty TEXT,notes TEXT)")
  db.execSQL("CREATE TABLE purchases(id INTEGER PRIMARY KEY AUTOINCREMENT,supplier_id INTEGER,received_at TEXT,invoice_ref TEXT,quantity REAL,purchase_total REAL,extra_costs REAL,unit_cost REAL,intended_selling_price REAL,warranty TEXT,notes TEXT)")
  db.execSQL("CREATE TABLE stock_units(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,purchase_id INTEGER,serial_number TEXT,imei1 TEXT,imei2 TEXT,colour TEXT,storage_ram TEXT,condition TEXT,barcode TEXT,qr_code TEXT,cost_price REAL,selling_price REAL,status TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE stock_batches(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,purchase_id INTEGER,quantity REAL,cost_price REAL,selling_price REAL,status TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,phone TEXT,whatsapp TEXT,email TEXT,address TEXT)")
  db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER,total REAL,payment TEXT,status TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE sale_items(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,product_id INTEGER,stock_unit_id INTEGER,stock_batch_id INTEGER,quantity REAL,unit_price REAL,description TEXT)")
  db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,amount REAL,method TEXT,reference TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE credit_accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,original_amount REAL,balance REAL,due_date TEXT,status TEXT)")
  db.execSQL("CREATE TABLE laybys(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,total REAL,balance REAL,status TEXT,release_status TEXT)")
  db.execSQL("CREATE TABLE layby_items(id INTEGER PRIMARY KEY AUTOINCREMENT,layby_id INTEGER,stock_unit_id INTEGER,quantity REAL)")
  db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,amount REAL,method TEXT,reference TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE credit_accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,original_amount REAL,balance REAL,due_date TEXT,status TEXT)")
  db.execSQL("CREATE TABLE laybys(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,total REAL,balance REAL,status TEXT,release_status TEXT)")
  db.execSQL("CREATE TABLE layby_items(id INTEGER PRIMARY KEY AUTOINCREMENT,layby_id INTEGER,stock_unit_id INTEGER,quantity REAL)")
  db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT UNIQUE,password_hash TEXT,salt TEXT,role TEXT,active INTEGER)")
  db.execSQL("CREATE TABLE audit_log(id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER,action TEXT,reference TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE supplier_payments(id INTEGER PRIMARY KEY AUTOINCREMENT,supplier_id INTEGER,amount REAL,method TEXT,reference TEXT,created_at TEXT,user_id INTEGER)")
  db.execSQL("CREATE TABLE locations(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE)")
  db.execSQL("CREATE TABLE stock_transfers(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,quantity REAL,from_location_id INTEGER,to_location_id INTEGER,status TEXT,reference TEXT,created_at TEXT,user_id INTEGER)")
  db.execSQL("CREATE TABLE refunds(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,amount REAL,reason TEXT,created_at TEXT,user_id INTEGER)")
  db.execSQL("CREATE TABLE location_stock(id INTEGER PRIMARY KEY AUTOINCREMENT,location_id INTEGER,product_id INTEGER,quantity REAL,cost_price REAL,UNIQUE(location_id,product_id))")
  db.execSQL("INSERT OR IGNORE INTO locations(name) VALUES('Main Store')")
  db.execSQL("CREATE TABLE stock_movements(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,stock_unit_id INTEGER,movement_type TEXT,quantity REAL,reference TEXT,created_at TEXT,user_id INTEGER)")
  val salt=Security.newSalt();db.execSQL("INSERT INTO users(username,password_hash,salt,role,active) VALUES(?,?,?,?,1)",arrayOf("admin",Security.hash("admin",salt),android.util.Base64.encodeToString(salt,android.util.Base64.NO_WRAP),"ADMIN"))
  db.execSQL("CREATE TABLE expenses(id INTEGER PRIMARY KEY AUTOINCREMENT,description TEXT,amount REAL,created_at TEXT)")
  listOf("Cellphone","Tablet","Laptop/Computer","TV","Radio","Inverter","Battery","Solar Equipment","Spare Part","Hardware","Stationery","Fishing/Camping","Printing Material","Other").forEach{
   val s=if(it in listOf("Cellphone","Tablet","Laptop/Computer","TV","Radio","Inverter","Battery"))1 else 0
   db.execSQL("INSERT INTO product_types(name,serialized) VALUES(?,?)",arrayOf(it,s))
  }
 }
 override fun onUpgrade(db:SQLiteDatabase,o:Int,n:Int){if(o<2){db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,phone TEXT,whatsapp TEXT,email TEXT,address TEXT)");db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER,total REAL,payment TEXT,status TEXT,created_at TEXT)");db.execSQL("CREATE TABLE sale_items(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,product_id INTEGER,stock_unit_id INTEGER,stock_batch_id INTEGER,quantity REAL,unit_price REAL,description TEXT)")
  db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,amount REAL,method TEXT,reference TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE credit_accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,original_amount REAL,balance REAL,due_date TEXT,status TEXT)")
  db.execSQL("CREATE TABLE laybys(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,customer_id INTEGER,total REAL,balance REAL,status TEXT,release_status TEXT)")
  db.execSQL("CREATE TABLE layby_items(id INTEGER PRIMARY KEY AUTOINCREMENT,layby_id INTEGER,stock_unit_id INTEGER,quantity REAL)")
  db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT UNIQUE,password_hash TEXT,salt TEXT,role TEXT,active INTEGER)")
  db.execSQL("CREATE TABLE audit_log(id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER,action TEXT,reference TEXT,created_at TEXT)")
  db.execSQL("CREATE TABLE supplier_payments(id INTEGER PRIMARY KEY AUTOINCREMENT,supplier_id INTEGER,amount REAL,method TEXT,reference TEXT,created_at TEXT,user_id INTEGER)")
  db.execSQL("CREATE TABLE locations(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE)")
  db.execSQL("CREATE TABLE stock_transfers(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,quantity REAL,from_location_id INTEGER,to_location_id INTEGER,status TEXT,reference TEXT,created_at TEXT,user_id INTEGER)")
  db.execSQL("CREATE TABLE refunds(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,amount REAL,reason TEXT,created_at TEXT,user_id INTEGER)")
  db.execSQL("CREATE TABLE location_stock(id INTEGER PRIMARY KEY AUTOINCREMENT,location_id INTEGER,product_id INTEGER,quantity REAL,cost_price REAL,UNIQUE(location_id,product_id))")
  db.execSQL("INSERT OR IGNORE INTO locations(name) VALUES('Main Store')")
  db.execSQL("CREATE TABLE stock_movements(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,stock_unit_id INTEGER,movement_type TEXT,quantity REAL,reference TEXT,created_at TEXT,user_id INTEGER)")
  val salt=Security.newSalt();db.execSQL("INSERT INTO users(username,password_hash,salt,role,active) VALUES(?,?,?,?,1)",arrayOf("admin",Security.hash("admin",salt),android.util.Base64.encodeToString(salt,android.util.Base64.NO_WRAP),"ADMIN"))}
  if(o<3){
   var found=false
   db.rawQuery("PRAGMA table_info(sale_items)",null).use{c->while(c.moveToNext())if(c.getString(1)=="stock_batch_id")found=true}
   if(!found)db.execSQL("ALTER TABLE sale_items ADD COLUMN stock_batch_id INTEGER")
  }
 }
 fun now()=SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(Date())
 fun today()=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
 fun search(q:String):List<StockResult>{
  val out=mutableListOf<StockResult>();val like="%${q.trim()}%"
  readableDatabase.rawQuery("""SELECT u.id,u.product_id,p.name,p.brand,p.model,COALESCE(u.imei1,''),COALESCE(u.serial_number,''),COALESCE(u.barcode,''),COALESCE(u.qr_code,''),u.selling_price,u.status
    FROM stock_units u JOIN products p ON p.id=u.product_id
    WHERE u.status='AVAILABLE' AND (?='' OR p.name LIKE ? OR p.brand LIKE ? OR p.model LIKE ? OR p.sku LIKE ? OR u.imei1 LIKE ? OR u.imei2 LIKE ? OR u.serial_number LIKE ? OR u.barcode LIKE ? OR u.qr_code LIKE ?) LIMIT 30""",
    arrayOf(q,like,like,like,like,like,like,like,like,like)).use{c->while(c.moveToNext()){
      val ident=listOf(c.getString(5),c.getString(6),c.getString(7),c.getString(8)).firstOrNull{it.isNotBlank()}?:"Serialized unit"
      out+=StockResult("UNIT",c.getLong(0),c.getLong(1),c.getString(2),c.getString(3),c.getString(4),ident,c.getDouble(9),1.0,c.getString(10))
    }}
  readableDatabase.rawQuery("""SELECT b.id,b.product_id,p.name,p.brand,p.model,p.sku,b.quantity,b.selling_price,b.status
    FROM stock_batches b JOIN products p ON p.id=b.product_id
    WHERE b.status='AVAILABLE' AND b.quantity>0 AND (?='' OR p.name LIKE ? OR p.brand LIKE ? OR p.model LIKE ? OR p.sku LIKE ? OR p.barcode LIKE ? OR p.qr_code LIKE ?) LIMIT 30""",
    arrayOf(q,like,like,like,like,like,like)).use{c->while(c.moveToNext())out+=StockResult("BATCH",c.getLong(0),c.getLong(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5)?:"",c.getDouble(7),c.getDouble(6),c.getString(8))}
  return out
 }
 fun completeSale(items:List<CartItem>,payment:String,customerId:Long?):Long{
  require(items.isNotEmpty()){"Cart is empty"}
  val d=writableDatabase;d.beginTransaction()
  try{
   val total=items.sumOf{it.price*it.qty};require(total>0){"Sale total must be greater than zero"}
   val s=d.compileStatement("INSERT INTO sales(customer_id,total,payment,status,created_at) VALUES(?,?,?,?,?)")
   if(customerId==null)s.bindNull(1)else s.bindLong(1,customerId);s.bindDouble(2,total);s.bindString(3,payment);s.bindString(4,"PAID");s.bindString(5,now());val sale=s.executeInsert()
   items.forEach{it->
    require(it.qty>0){"Quantity must be greater than zero"}
    insertSaleItem(d,sale,it)
    if(it.kind=="UNIT"){
     val n=d.compileStatement("UPDATE stock_units SET status='SOLD' WHERE id=? AND status='AVAILABLE'").apply{bindLong(1,it.id)}.executeUpdateDelete()
     require(n==1){"Serialized stock is no longer available: ${it.name}"}
     addMovement(it.productId,it.id,"SALE",it.qty,"Sale #$sale",null)
    }else{
     val n=d.compileStatement("UPDATE stock_batches SET quantity=quantity-?,status=CASE WHEN quantity-?<=0 THEN 'OUT_OF_STOCK' ELSE 'AVAILABLE' END WHERE id=? AND status='AVAILABLE' AND quantity>=?").apply{bindDouble(1,it.qty);bindDouble(2,it.qty);bindLong(3,it.id);bindDouble(4,it.qty)}.executeUpdateDelete()
     require(n==1){"Insufficient batch stock: ${it.name}"}
     addMovement(it.productId,null,"SALE",it.qty,"Sale #$sale",null)
    }
   }
   d.setTransactionSuccessful();return sale
  }finally{d.endTransaction()}
 }

  fun completeSaleWithTerms(items:List<CartItem>,payment:String,customerId:Long?,paid:Double,dueDate:String,reference:String):Long{
   require(items.isNotEmpty()){"Cart is empty"};require(paid>=0){"Payment cannot be negative"}
   val d=writableDatabase;d.beginTransaction()
   try{
    val total=items.sumOf{it.price*it.qty};require(total>0){"Sale total must be greater than zero"};require(paid<=total+0.00001){"Payment cannot exceed sale total"}
    val status=when(payment){"Credit"->"CREDIT";"Layby"->"LAYBY";else->"PAID"}
    val s=d.compileStatement("INSERT INTO sales(customer_id,total,payment,status,created_at) VALUES(?,?,?,?,?)")
    if(customerId==null)s.bindNull(1)else s.bindLong(1,customerId);s.bindDouble(2,total);s.bindString(3,payment);s.bindString(4,status);s.bindString(5,now());val sale=s.executeInsert()
    items.forEach{it->
     require(it.qty>0){"Quantity must be greater than zero"};insertSaleItem(d,sale,it)
     if(it.kind=="UNIT"){
      val target=if(payment=="Layby")"RESERVED" else "SOLD"
      val n=d.compileStatement("UPDATE stock_units SET status=? WHERE id=? AND status='AVAILABLE'").apply{bindString(1,target);bindLong(2,it.id)}.executeUpdateDelete()
      require(n==1){"Serialized stock is no longer available: ${it.name}"}
      addMovement(it.productId,it.id,if(payment=="Layby")"RESERVE" else "SALE",it.qty,"Sale #$sale",null)
     }else{
      val n=d.compileStatement("UPDATE stock_batches SET quantity=quantity-?,status=CASE WHEN quantity-?<=0 THEN 'OUT_OF_STOCK' ELSE 'AVAILABLE' END WHERE id=? AND status='AVAILABLE' AND quantity>=?").apply{bindDouble(1,it.qty);bindDouble(2,it.qty);bindLong(3,it.id);bindDouble(4,it.qty)}.executeUpdateDelete()
      require(n==1){"Insufficient batch stock: ${it.name}"};addMovement(it.productId,null,"SALE",it.qty,"Sale #$sale",null)
     }
    }
    if(paid>0)d.execSQL("INSERT INTO payments(sale_id,customer_id,amount,method,reference,created_at) VALUES(?,?,?,?,?,?)",arrayOf(sale,customerId,paid,payment,reference,now()))
    val balance=(total-paid).coerceAtLeast(0.0)
    if(payment=="Credit"){
     require(customerId!=null){"A customer is required for Credit sales"}
     d.execSQL("INSERT INTO credit_accounts(sale_id,customer_id,original_amount,balance,due_date,status) VALUES(?,?,?,?,?,?)",arrayOf(sale,customerId,total,balance,dueDate,if(balance<=0.00001)"PAID" else "OPEN"))
    }
    if(payment=="Layby"){
     require(customerId!=null){"A customer is required for Layby sales"}
     val lay=d.compileStatement("INSERT INTO laybys(sale_id,customer_id,total,balance,status,release_status) VALUES(?,?,?,?,?,?)")
     lay.bindLong(1,sale);lay.bindLong(2,customerId);lay.bindDouble(3,total);lay.bindDouble(4,balance);lay.bindString(5,if(balance<=0.00001)"PAID" else "OPEN");lay.bindString(6,if(balance<=0.00001)"READY_TO_RELEASE" else "HELD");val lid=lay.executeInsert()
     items.filter{it.kind=="UNIT"}.forEach{d.execSQL("INSERT INTO layby_items(layby_id,stock_unit_id,quantity) VALUES(?,?,?)",arrayOf(lid,it.id,it.qty))}
    }
    d.setTransactionSuccessful();return sale
   }finally{d.endTransaction()}
  }

  private fun insertSaleItem(d:SQLiteDatabase,saleId:Long,item:CartItem){
   d.execSQL("INSERT INTO sale_items(sale_id,product_id,stock_unit_id,stock_batch_id,quantity,unit_price,description) VALUES(?,?,?,?,?,?,?)",arrayOf(saleId,item.productId,if(item.kind=="UNIT")item.id else null,if(item.kind=="BATCH")item.id else null,item.qty,item.price,item.name))
  }

 fun receipts(limit:Int=50):List<Receipt>{
  val out=mutableListOf<Receipt>()
  readableDatabase.rawQuery("SELECT s.id,s.total,s.payment,s.status,COALESCE(c.name,'Walk-in customer'),s.created_at FROM sales s LEFT JOIN customers c ON c.id=s.customer_id ORDER BY s.id DESC LIMIT ?",arrayOf(limit.toString())).use{c->while(c.moveToNext())out+=Receipt(c.getLong(0),c.getDouble(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5))}
  return out
 }
 fun accountSummary(customerId:Long):AccountSummary{
  val credit=readableDatabase.rawQuery("SELECT COALESCE(SUM(balance),0) FROM credit_accounts WHERE customer_id=? AND status='OPEN'",arrayOf(customerId.toString())).use{it.moveToFirst();it.getDouble(0)}
  val lay=readableDatabase.rawQuery("SELECT COALESCE(SUM(balance),0) FROM laybys WHERE customer_id=? AND status='OPEN'",arrayOf(customerId.toString())).use{it.moveToFirst();it.getDouble(0)}
  return AccountSummary(credit,lay)
 }

 fun recordAccountPayment(customerId:Long, accountType:String, accountId:Long, amount:Double, method:String, reference:String):Boolean{
  if(amount<=0)return false
  val d=writableDatabase;d.beginTransaction()
  try{
   val table=if(accountType=="Credit")"credit_accounts" else "laybys"
   val cur=d.rawQuery("SELECT balance,sale_id FROM $table WHERE id=?",arrayOf(accountId.toString()))
   if(!cur.moveToFirst()){cur.close();return false}
   val bal=cur.getDouble(0);val saleId=cur.getLong(1);cur.close()
   val pay=amount.coerceAtMost(bal)
   val customerCol=if(accountType=="Credit")"customer_id" else "customer_id"
   d.execSQL("INSERT INTO payments(sale_id,customer_id,amount,method,reference,created_at) VALUES(?,?,?,?,?,?)",arrayOf(saleId,customerId,pay,method,reference,now()))
   val newBal=bal-pay
   d.execSQL("UPDATE $table SET balance=?,status=? WHERE id=?",arrayOf(newBal,if(newBal<=0.00001)"PAID" else "OPEN",accountId))
   if(accountType=="Layby"&&newBal<=0.00001)d.execSQL("UPDATE laybys SET release_status='READY_TO_RELEASE' WHERE id=?",arrayOf(accountId))
   d.setTransactionSuccessful();return true
  }finally{d.endTransaction()}
 }
 fun customerAccounts(customerId:Long):List<AccountTransaction>{
  val out=mutableListOf<AccountTransaction>()
  readableDatabase.rawQuery("SELECT id,original_amount,balance,status,due_date FROM credit_accounts WHERE customer_id=? ORDER BY id DESC",arrayOf(customerId.toString())).use{c->while(c.moveToNext())out+=AccountTransaction("Credit",c.getLong(0),c.getDouble(1),c.getDouble(2),c.getString(3),c.getString(4),"Credit sale") }
  readableDatabase.rawQuery("SELECT id,total,balance,status,release_status FROM laybys WHERE customer_id=? ORDER BY id DESC",arrayOf(customerId.toString())).use{c->while(c.moveToNext())out+=AccountTransaction("Layby",c.getLong(0),c.getDouble(1),c.getDouble(2),c.getString(3),c.getString(4),"Layby • ${c.getString(4)}") }
  return out
 }
 fun customersDetailed():List<Triple<Long,String,String>>{val r=mutableListOf<Triple<Long,String,String>>();readableDatabase.rawQuery("SELECT id,name,COALESCE(phone,'') FROM customers ORDER BY name",null).use{c->while(c.moveToNext())r+=Triple(c.getLong(0),c.getString(1),c.getString(2))};return r}


 fun addMovement(productId:Long,unitId:Long?,type:String,qty:Double,reference:String,userId:Long?){
  writableDatabase.execSQL("INSERT INTO stock_movements(product_id,stock_unit_id,movement_type,quantity,reference,created_at,user_id) VALUES(?,?,?,?,?,?,?)",arrayOf(productId,unitId,type,qty,reference,now(),userId))
 }
 fun movements(limit:Int=100):List<Movement>{
  val r=mutableListOf<Movement>();readableDatabase.rawQuery("SELECT m.created_at,m.movement_type,p.name,m.quantity,COALESCE(m.reference,'') FROM stock_movements m JOIN products p ON p.id=m.product_id ORDER BY m.id DESC LIMIT ?",arrayOf(limit.toString())).use{c->while(c.moveToNext())r+=Movement(c.getString(0),c.getString(1),c.getString(2),c.getDouble(3),c.getString(4))};return r
 }
 fun users():List<UserAccount>{val r=mutableListOf<UserAccount>();readableDatabase.rawQuery("SELECT id,username,role FROM users WHERE active=1 ORDER BY username",null).use{c->while(c.moveToNext())r+=UserAccount(c.getLong(0),c.getString(1),c.getString(2))};return r}
 fun addUser(username:String,password:String,role:String){val salt=Security.newSalt();writableDatabase.execSQL("INSERT INTO users(username,password_hash,salt,role,active) VALUES(?,?,?,?,1)",arrayOf(username,Security.hash(password,salt),android.util.Base64.encodeToString(salt,android.util.Base64.NO_WRAP),role))}
 fun verify(username:String,password:String):UserAccount?{readableDatabase.rawQuery("SELECT id,username,password_hash,salt,role FROM users WHERE username=? AND active=1",arrayOf(username)).use{c->if(c.moveToFirst()){val salt=android.util.Base64.decode(c.getString(3),android.util.Base64.NO_WRAP);if(Security.hash(password,salt)==c.getString(2))return UserAccount(c.getLong(0),c.getString(1),c.getString(4))}};return null}
 fun supplierPurchases():List<Pair<String,Double>>{val r=mutableListOf<Pair<String,Double>>();readableDatabase.rawQuery("SELECT COALESCE(s.name,'Unknown'),COALESCE(SUM(p.purchase_total+p.extra_costs),0) FROM purchases p LEFT JOIN suppliers s ON s.id=p.supplier_id GROUP BY p.supplier_id ORDER BY SUM(p.purchase_total+p.extra_costs) DESC",null).use{c->while(c.moveToNext())r+=c.getString(0) to c.getDouble(1)};return r}

 fun profitRows(limit:Int=100):List<ProfitRow>{
  val r=mutableListOf<ProfitRow>()
  readableDatabase.rawQuery("""SELECT s.id,s.created_at,s.total,
   COALESCE((SELECT SUM(si.quantity*COALESCE(su.cost_price,sb.cost_price)) FROM sale_items si
    LEFT JOIN stock_units su ON su.id=si.stock_unit_id
    LEFT JOIN stock_batches sb ON sb.id=si.stock_batch_id WHERE si.sale_id=s.id),0)
   FROM sales s ORDER BY s.id DESC LIMIT ?""",arrayOf(limit.toString())).use{c->
    while(c.moveToNext()){val rev=c.getDouble(2);val cost=c.getDouble(3);r+=ProfitRow(c.getLong(0),c.getString(1),rev,cost,rev-cost)}
  };return r
 }
 fun customerStatement(customerId:Long):List<StatementRow>{
  val r=mutableListOf<StatementRow>();var bal=0.0
  readableDatabase.rawQuery("""SELECT s.created_at,'SALE',CAST(s.id AS TEXT),s.total,0.0
   FROM sales s WHERE s.customer_id=? UNION ALL
   SELECT p.created_at,'PAYMENT',COALESCE(p.reference,'Payment'),0.0,p.amount
   FROM payments p WHERE p.customer_id=? ORDER BY 1""",arrayOf(customerId.toString(),customerId.toString())).use{c->
    while(c.moveToNext()){bal += c.getDouble(3)-c.getDouble(4);r+=StatementRow(c.getString(0),c.getString(1),c.getString(2),c.getDouble(3),c.getDouble(4),bal)}
  };return r
 }
 fun overdueCredits():List<Pair<String,Double>>{
  val r=mutableListOf<Pair<String,Double>>()
  readableDatabase.rawQuery("""SELECT COALESCE(c.name,'Unknown'),SUM(a.balance) FROM credit_accounts a
   LEFT JOIN customers c ON c.id=a.customer_id WHERE a.status='OPEN' AND a.due_date < date('now') GROUP BY a.customer_id""",null).use{c->while(c.moveToNext())r+=c.getString(0) to c.getDouble(1)}
  return r
 }
 fun exportRows(table:String):List<Array<String>>{
  val safe=when(table){
   "products"->"SELECT id,name,brand,model,sku,category FROM products"
   "customers"->"SELECT id,name,phone,whatsapp,email,address FROM customers"
   "sales"->"SELECT id,customer_id,total,payment,status,created_at FROM sales"
   "movements"->"SELECT id,product_id,stock_unit_id,movement_type,quantity,reference,created_at FROM stock_movements"
   else->return emptyList()
  }
  val r=mutableListOf<Array<String>>()
  readableDatabase.rawQuery(safe,null).use{c->
   val n=c.columnCount
   r.add(Array(n){i->c.getColumnName(i)})
   while(c.moveToNext())r.add(Array(n){i->c.getString(i)?:(" ")})
  }
  return r
 }
 fun audit(userId:Long?,action:String,reference:String=""){writableDatabase.execSQL("INSERT INTO audit_log(user_id,action,reference,created_at) VALUES(?,?,?,?)",arrayOf(userId,action,reference,now()))}
 fun auditRows(limit:Int=100):List<String>{val r=mutableListOf<String>();readableDatabase.rawQuery("SELECT a.created_at,COALESCE(u.username,'SYSTEM'),a.action,a.reference FROM audit_log a LEFT JOIN users u ON u.id=a.user_id ORDER BY a.id DESC LIMIT ?",arrayOf(limit.toString())).use{c->while(c.moveToNext())r+=" ${c.getString(0)} • ${c.getString(1)} • ${c.getString(2)} • ${c.getString(3)}"};return r}
 fun hasRole(userId:Long?,vararg roles:String):Boolean{if(userId==null)return false;readableDatabase.rawQuery("SELECT role FROM users WHERE id=? AND active=1",arrayOf(userId.toString())).use{c->if(c.moveToFirst())return roles.contains(c.getString(0))};return false}
 fun changePassword(userId:Long,password:String){val salt=Security.newSalt();writableDatabase.execSQL("UPDATE users SET password_hash=?,salt=? WHERE id=?",arrayOf(Security.hash(password,salt),android.util.Base64.encodeToString(salt,android.util.Base64.NO_WRAP),userId))}

 fun adjustBatch(batchId:Long,delta:Double,reason:String,userId:Long?){val d=writableDatabase;d.beginTransaction();try{d.execSQL("UPDATE stock_batches SET quantity=quantity+?,status=CASE WHEN quantity+?<=0 THEN 'OUT_OF_STOCK' ELSE 'AVAILABLE' END WHERE id=? AND quantity+?>=0",arrayOf(delta,delta,batchId,delta));d.execSQL("INSERT INTO stock_movements(product_id,stock_unit_id,movement_type,quantity,reference,created_at,user_id) SELECT product_id,NULL,'ADJUSTMENT',?, ?, ?, ? FROM stock_batches WHERE id=?",arrayOf(delta,reason,now(),userId,batchId));audit(userId,"STOCK_ADJUSTMENT","Batch #$batchId $reason");d.setTransactionSuccessful()}finally{d.endTransaction()}}
 fun returnSerialized(unitId:Long,reason:String,userId:Long?){val d=writableDatabase;d.execSQL("UPDATE stock_units SET status='AVAILABLE' WHERE id=? AND status='SOLD'",arrayOf(unitId));d.rawQuery("SELECT product_id FROM stock_units WHERE id=?",arrayOf(unitId.toString())).use{if(it.moveToFirst())d.execSQL("INSERT INTO stock_movements(product_id,stock_unit_id,movement_type,quantity,reference,created_at,user_id) VALUES(?,?,?,?,?,?,?)",arrayOf(it.getLong(0),unitId,"RETURN",1.0,reason,now(),userId))};audit(userId,"SERIALIZED_RETURN","Unit #$unitId $reason")}

 fun supplierBalances():List<SupplierBalance>{
  val r=mutableListOf<SupplierBalance>()
  readableDatabase.rawQuery("""SELECT s.id,s.name,COALESCE((SELECT SUM(p.purchase_total+p.extra_costs) FROM purchases p WHERE p.supplier_id=s.id),0),
   COALESCE((SELECT SUM(sp.amount) FROM supplier_payments sp WHERE sp.supplier_id=s.id),0) FROM suppliers s ORDER BY s.name""",null).use{c->while(c.moveToNext()){val bought=c.getDouble(2);val paid=c.getDouble(3);r+=SupplierBalance(c.getLong(0),c.getString(1),bought,paid,bought-paid)}}
  return r
 }
 fun supplierPayment(id:Long,amount:Double,method:String,reference:String,userId:Long?){writableDatabase.execSQL("INSERT INTO supplier_payments(supplier_id,amount,method,reference,created_at,user_id) VALUES(?,?,?,?,?,?)",arrayOf(id,amount,method,reference,now(),userId));audit(userId,"SUPPLIER_PAYMENT","Supplier #$id")}
 fun locations():List<Location>{val r=mutableListOf<Location>();readableDatabase.rawQuery("SELECT id,name FROM locations ORDER BY name",null).use{c->while(c.moveToNext())r+=Location(c.getLong(0),c.getString(1))};return r}
 fun addLocation(name:String){writableDatabase.execSQL("INSERT INTO locations(name) VALUES(?)",arrayOf(name))}
 fun createTransfer(productId:Long,qty:Double,from:Long,to:Long,ref:String,userId:Long?){writableDatabase.execSQL("INSERT INTO stock_transfers(product_id,quantity,from_location_id,to_location_id,status,reference,created_at,user_id) VALUES(?,?,?,?,?,?,?,?)",arrayOf(productId,qty,from,to,"COMPLETED",ref,now(),userId));audit(userId,"STOCK_TRANSFER",ref)}
 fun transfers(limit:Int=100):List<Transfer>{val r=mutableListOf<Transfer>();readableDatabase.rawQuery("""SELECT t.id,p.name,t.quantity,fl.name,tl.name,t.created_at,t.status FROM stock_transfers t JOIN products p ON p.id=t.product_id JOIN locations fl ON fl.id=t.from_location_id JOIN locations tl ON tl.id=t.to_location_id ORDER BY t.id DESC LIMIT ?""",arrayOf(limit.toString())).use{c->while(c.moveToNext())r+=Transfer(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6))};return r}

 fun refundSale(saleId:Long,amount:Double,reason:String,userId:Long?){
  require(amount>0){"Refund amount must be greater than zero"}
  val d=writableDatabase;d.beginTransaction()
  try{
   val cur=d.rawQuery("SELECT total,status FROM sales WHERE id=?",arrayOf(saleId.toString()))
   if(!cur.moveToFirst()){cur.close();throw IllegalArgumentException("Sale #$saleId not found")}
   val total=cur.getDouble(0);val status=cur.getString(1);cur.close();require(status!="REFUNDED"){"Sale is already refunded"}
   val already=d.rawQuery("SELECT COALESCE(SUM(amount),0) FROM refunds WHERE sale_id=?",arrayOf(saleId.toString())).use{it.moveToFirst();it.getDouble(0)}
   require(already+amount<=total+0.00001){"Refund exceeds remaining sale balance"}
   d.execSQL("INSERT INTO refunds(sale_id,amount,reason,created_at,user_id) VALUES(?,?,?,?,?)",arrayOf(saleId,amount,reason,now(),userId))
   if(already+amount>=total-0.00001){
    d.rawQuery("SELECT product_id,stock_unit_id,stock_batch_id,quantity FROM sale_items WHERE sale_id=?",arrayOf(saleId.toString())).use{c->
     while(c.moveToNext()){
      val productId=c.getLong(0);val unitId=if(c.isNull(1))null else c.getLong(1);val batchId=if(c.isNull(2))null else c.getLong(2);val qty=c.getDouble(3)
      if(unitId!=null){d.execSQL("UPDATE stock_units SET status='AVAILABLE' WHERE id=? AND status IN('SOLD','RESERVED')",arrayOf(unitId));addMovement(productId,unitId,"RETURN",qty,"Refund #$saleId",userId)}
      else if(batchId!=null){d.execSQL("UPDATE stock_batches SET quantity=quantity+?,status='AVAILABLE' WHERE id=?",arrayOf(qty,batchId));addMovement(productId,null,"RETURN",qty,"Refund #$saleId",userId)}
     }
    }
    d.execSQL("UPDATE sales SET status='REFUNDED' WHERE id=?",arrayOf(saleId))
   }
   audit(userId,"SALE_REFUND","Sale #$saleId $reason");d.setTransactionSuccessful()
  }finally{d.endTransaction()}
 }
 fun refunds(limit:Int=100):List<Refund>{val r=mutableListOf<Refund>();readableDatabase.rawQuery("""SELECT r.id,r.sale_id,r.amount,r.reason,r.created_at,COALESCE(u.username,'SYSTEM') FROM refunds r LEFT JOIN users u ON u.id=r.user_id ORDER BY r.id DESC LIMIT ?""",arrayOf(limit.toString())).use{c->while(c.moveToNext())r+=Refund(c.getLong(0),c.getLong(1),c.getDouble(2),c.getString(3),c.getString(4),c.getString(5))};return r}

 fun locationStock():List<LocationStock>{val r=mutableListOf<LocationStock>();readableDatabase.rawQuery("""SELECT l.name,p.name,ls.quantity,ls.quantity*ls.cost_price FROM location_stock ls JOIN locations l ON l.id=ls.location_id JOIN products p ON p.id=ls.product_id ORDER BY l.name,p.name""",null).use{c->while(c.moveToNext())r+=LocationStock(c.getString(0),c.getString(1),c.getDouble(2),c.getDouble(3))};return r}
 fun setLocationStock(locationId:Long,productId:Long,qty:Double,cost:Double,userId:Long?){writableDatabase.execSQL("""INSERT INTO location_stock(location_id,product_id,quantity,cost_price) VALUES(?,?,?,?) ON CONFLICT(location_id,product_id) DO UPDATE SET quantity=excluded.quantity,cost_price=excluded.cost_price""",arrayOf(locationId,productId,qty,cost));audit(userId,"LOCATION_STOCK_UPDATE","Location #$locationId Product #$productId")}
 fun transferLocationStock(productId:Long,qty:Double,from:Long,to:Long,ref:String,userId:Long?){val d=writableDatabase;d.beginTransaction();try{d.execSQL("UPDATE location_stock SET quantity=quantity-? WHERE location_id=? AND product_id=? AND quantity>=?",arrayOf(qty,from,productId,qty));d.execSQL("INSERT INTO location_stock(location_id,product_id,quantity,cost_price) SELECT ?,product_id,?,cost_price FROM location_stock WHERE location_id=? AND product_id=? ON CONFLICT(location_id,product_id) DO UPDATE SET quantity=location_stock.quantity+excluded.quantity",arrayOf(to,qty,from,productId));createTransfer(productId,qty,from,to,ref,userId);d.setTransactionSuccessful()}finally{d.endTransaction()}}
 fun dashboard():Dashboard{
  val sales=readableDatabase.rawQuery("SELECT COALESCE(SUM(total),0) FROM sales WHERE created_at LIKE ?",arrayOf("${today()}%")).use{it.moveToFirst();it.getDouble(0)}
  val expenses=readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM expenses WHERE created_at LIKE ?",arrayOf("${today()}%")).use{it.moveToFirst();it.getDouble(0)}
  val costUnits=readableDatabase.rawQuery("SELECT COALESCE(SUM(cost_price),0) FROM stock_units WHERE status='AVAILABLE'",null).use{it.moveToFirst();it.getDouble(0)}
  val costBatch=readableDatabase.rawQuery("SELECT COALESCE(SUM(quantity*cost_price),0) FROM stock_batches WHERE status='AVAILABLE'",null).use{it.moveToFirst();it.getDouble(0)}
  val receivables=readableDatabase.rawQuery("SELECT COALESCE(SUM(balance),0) FROM credit_accounts WHERE status='OPEN'",null).use{it.moveToFirst();it.getDouble(0)}
  val layby=readableDatabase.rawQuery("SELECT COALESCE(SUM(balance),0) FROM laybys WHERE status='OPEN'",null).use{it.moveToFirst();it.getDouble(0)}
  val grossProfit=readableDatabase.rawQuery("""SELECT COALESCE(SUM(s.total-COALESCE((SELECT SUM(si.quantity*COALESCE(su.cost_price,sb.cost_price,0)) FROM sale_items si LEFT JOIN stock_units su ON su.id=si.stock_unit_id LEFT JOIN stock_batches sb ON sb.id=si.stock_batch_id WHERE si.sale_id=s.id),0)),0) FROM sales s WHERE s.created_at LIKE ? AND s.status<>'REFUNDED'""",arrayOf("${today()}%")).use{it.moveToFirst();it.getDouble(0)}
  val tx=readableDatabase.rawQuery("SELECT COUNT(*) FROM sales WHERE created_at LIKE ?",arrayOf("${today()}%")).use{it.moveToFirst();it.getInt(0)}
  return Dashboard(sales,expenses,grossProfit,costUnits+costBatch,receivables,layby,tx)
 }
 fun report(days:Int):List<ReportRow>{
  val out=mutableListOf<ReportRow>();val fmt=SimpleDateFormat("yyyy-MM-dd",Locale.US);val cal=Calendar.getInstance()
  repeat(days){val day=fmt.format(cal.time);val sales=readableDatabase.rawQuery("SELECT COALESCE(SUM(total),0) FROM sales WHERE created_at LIKE ?",arrayOf("$day%")).use{it.moveToFirst();it.getDouble(0)}
   val exp=readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM expenses WHERE created_at LIKE ?",arrayOf("$day%")).use{it.moveToFirst();it.getDouble(0)}
   out+=ReportRow(day,sales,exp,sales-exp);cal.add(Calendar.DAY_OF_YEAR,-1)}
  return out
 }
 fun salesByPayment():List<Pair<String,Double>>{
  val r=mutableListOf<Pair<String,Double>>();readableDatabase.rawQuery("SELECT payment,COALESCE(SUM(total),0) FROM sales GROUP BY payment ORDER BY SUM(total) DESC",null).use{c->while(c.moveToNext())r+=c.getString(0) to c.getDouble(1)};return r
 }
 fun lowStock(limit:Double=3.0):List<Pair<String,Double>>{
  val r=mutableListOf<Pair<String,Double>>()
  readableDatabase.rawQuery("SELECT p.name,b.quantity FROM stock_batches b JOIN products p ON p.id=b.product_id WHERE b.status='AVAILABLE' AND b.quantity>0 AND b.quantity<=? ORDER BY b.quantity",arrayOf(limit.toString())).use{c->
   while(c.moveToNext())r.add(c.getString(0) to c.getDouble(1))
  }
  return r
 }
 fun addCustomer(n:String,p:String,w:String,e:String,a:String)=writableDatabase.execSQL("INSERT INTO customers(name,phone,whatsapp,email,address) VALUES(?,?,?,?,?)",arrayOf(n,p,w,e,a))
 fun customers():List<Pair<Long,String>>{val r=mutableListOf<Pair<Long,String>>();readableDatabase.rawQuery("SELECT id,name FROM customers ORDER BY name",null).use{c->while(c.moveToNext())r+=c.getLong(0) to c.getString(1)};return r}
 fun addExpense(d:String,a:Double)=writableDatabase.execSQL("INSERT INTO expenses(description,amount,created_at) VALUES(?,?,?)",arrayOf(d,a,now()))
 fun totals():Totals{
  fun sumToday(table:String,column:String):Double{
   return readableDatabase.rawQuery("SELECT COALESCE(SUM($column),0) FROM $table WHERE created_at LIKE ?",arrayOf("${today()}%")).use{it.moveToFirst();it.getDouble(0)}
  }
  val a=sumToday("sales","total")
  val e=sumToday("expenses","amount")
  return Totals("%.2f".format(a),"%.2f".format(e),"%.2f".format(a-e))
 }
}
data class CartItem(val kind:String,val id:Long,val productId:Long,val name:String,val price:Double,var qty:Double)
