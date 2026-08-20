
package com.tremtechzim.pos
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File

object ReceiptPdf {
 fun create(context:Context,r:Receipt,items:List<String>):File{
  val dir=context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir;dir.mkdirs()
  val f=File(dir,"Trem-Tech-Receipt-${r.id}.pdf");val doc=PdfDocument()
  val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,1).create());val c=page.canvas;val p=Paint()
  p.textSize=22f;c.drawText("Trem-Tech Zim",40f,45f,p);p.textSize=13f
  var y=75f;listOf("Receipt #${r.id}","Date: ${r.createdAt}","Customer: ${r.customer}","Payment: ${r.payment}","Status: ${r.status}").forEach{c.drawText(it,40f,y,p);y+=22f}
  y+=12f;p.textSize=12f;c.drawText("ITEMS",40f,y,p);y+=22f
  items.forEach{c.drawText(it.take(85),40f,y,p);y+=20f;if(y>790f){doc.finishPage(page);return create(context,r,items.takeLast(20))}}
  y+=10f;p.textSize=16f;c.drawText("TOTAL: $${"%.2f".format(r.total)}",40f,y,p);y+=35f;p.textSize=12f;c.drawText("Thank you for your business.",40f,y,p)
  doc.finishPage(page);doc.writeTo(f.outputStream());doc.close();return f
 }
}