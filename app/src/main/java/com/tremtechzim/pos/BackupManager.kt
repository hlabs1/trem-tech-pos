
package com.tremtechzim.pos
import android.content.Context
import android.os.Environment
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

data class BackupInfo(val file:File,val sha256:String,val created:String)

object BackupManager{
 private fun dir(c:Context)= (c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?:c.filesDir).apply{mkdirs()}
 fun create(c:Context):BackupInfo{
  val f=File(dir(c),"Trem-Tech-POS-${stamp()}.db");c.getDatabasePath("tremtech_pos.db").copyTo(f,true)
  return BackupInfo(f,hash(f),stamp())
 }
 fun list(c:Context):List<BackupInfo>{
  return dir(c).listFiles()?.filter{it.name.startsWith("Trem-Tech-POS-")&&it.name.endsWith(".db") }?.sortedByDescending{it.lastModified()}?.map{BackupInfo(it,hash(it),SimpleDateFormat("yyyyMMdd-HHmmss",Locale.US).format(Date(it.lastModified())))}?:emptyList()
 }
 fun validate(f:File):Boolean=f.exists()&&f.length()>1024&&hash(f).isNotBlank()
 private fun hash(f:File):String{val md=MessageDigest.getInstance("SHA-256");f.inputStream().use{b->val x=ByteArray(8192);var n=b.read(x);while(n>0){md.update(x,0,n);n=b.read(x)}};return md.digest().joinToString(""){"%02x".format(it)}}
 private fun stamp()=SimpleDateFormat("yyyyMMdd-HHmmss",Locale.US).format(Date())
}