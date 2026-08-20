
package com.tremtechzim.pos
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File

object StatementPdf{
 fun create(context:Context,title:String,lines:List<String>):File{
  val dir=context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?:context.filesDir;dir.mkdirs()
  val f=File(dir,title.replace(" ","-")+".pdf");val doc=PdfDocument();val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,1).create());val c=page.canvas;val p=Paint();p.textSize=20f;c.drawText("Trem-Tech Zim",40f,45f,p);p.textSize=14f;var y=75f
  for(line in lines){if(y>800f)break;c.drawText(line.take(90),40f,y,p);y+=22f};doc.finishPage(page);doc.writeTo(f.outputStream());doc.close();return f
 }
}