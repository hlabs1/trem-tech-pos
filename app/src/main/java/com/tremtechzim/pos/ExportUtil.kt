
package com.tremtechzim.pos
import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object ExportUtil {
 private fun dir(c:Context):File=(c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?:c.filesDir).apply{mkdirs()}
 fun csv(c:Context,name:String,rows:List<Array<String>>):File{
  val f=File(dir(c),"$name-${stamp()}.csv");f.bufferedWriter().use{w->rows.forEach{row->w.append(row.joinToString(","){ "\""+it.replace("\"","\"\"")+"\"" }).append("\n")}};return f
 }
 fun xlsx(c:Context,name:String,rows:List<Array<String>>):File{
  val f=File(dir(c),"$name-${stamp()}.xlsx");val wb=XSSFWorkbook();val sh=wb.createSheet("Export")
  rows.forEachIndexed{ri,row->row.forEachIndexed{ci,v->sh.createRow(ri).createCell(ci).setCellValue(v)}};f.outputStream().use{wb.write(it)};wb.close();return f
 }
 fun backup(c:Context,source:File):File{val f=File(dir(c),"Trem-Tech-POS-Backup-${stamp()}.db");source.copyTo(f,true);return f}
 private fun stamp()=SimpleDateFormat("yyyyMMdd-HHmmss",Locale.US).format(Date())
}