
package com.tremtechzim.pos
import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
object RestoreManager{
 private fun stamp()=SimpleDateFormat("yyyyMMdd-HHmmss",Locale.US).format(Date())
 fun restore(c:Context,backup:File):File{
  require(BackupManager.validate(backup)){"Invalid backup"}
  val current=c.getDatabasePath("tremtech_pos.db")
  val emergency=File(current.parentFile,"pre-restore-${stamp()}.db")
  if(current.exists())current.copyTo(emergency,true)
  c.deleteDatabase("tremtech_pos.db")
  backup.copyTo(current,true)
  return emergency
 }
}