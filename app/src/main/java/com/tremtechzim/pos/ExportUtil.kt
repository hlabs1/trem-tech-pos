package com.tremtechzim.pos

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportUtil {

    private fun dir(c: Context): File =
        (c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: c.filesDir).apply {
            mkdirs()
        }

    fun csv(
        c: Context,
        name: String,
        rows: List<Array<String>>
    ): File {
        val f = File(dir(c), "$name-${stamp()}.csv")

        f.bufferedWriter().use { w ->
            rows.forEach { row ->
                w.append(
                    row.joinToString(",") {
                        "\"" + it.replace("\"", "\"\"") + "\""
                    }
                )
                w.append("\n")
            }
        }

        return f
    }

    /**
     * Creates a real Excel .xlsx file without Apache POI.
     * This avoids the XSSFWorkbook dependency that was breaking
     * the GitHub Actions build.
     */
    fun xlsx(
        c: Context,
        name: String,
        rows: List<Array<String>>
    ): File {

        val f = File(dir(c), "$name-${stamp()}.xlsx")

        ZipOutputStream(f.outputStream().buffered()).use { zip ->

            // [Content_Types].xml
            addEntry(
                zip,
                "[Content_Types].xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                    <Default Extension="rels"
                        ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                    <Default Extension="xml"
                        ContentType="application/xml"/>
                    <Override PartName="/xl/workbook.xml"
                        ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                    <Override PartName="/xl/worksheets/sheet1.xml"
                        ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """.trimIndent()
            )

            // _rels/.rels
            addEntry(
                zip,
                "_rels/.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship
                        Id="rId1"
                        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                        Target="xl/workbook.xml"/>
                </Relationships>
                """.trimIndent()
            )

            // xl/workbook.xml
            addEntry(
                zip,
                "xl/workbook.xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook
                    xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <sheets>
                        <sheet name="Export" sheetId="1" r:id="rId1"/>
                    </sheets>
                </workbook>
                """.trimIndent()
            )

            // xl/_rels/workbook.xml.rels
            addEntry(
                zip,
                "xl/_rels/workbook.xml.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship
                        Id="rId1"
                        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                        Target="worksheets/sheet1.xml"/>
                </Relationships>
                """.trimIndent()
            )

            // Worksheet
            val sheet = buildString {

                append(
                    """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                        <sheetData>
                    """.trimIndent()
                )

                append("\n")

                rows.forEachIndexed { rowIndex, row ->

                    val excelRow = rowIndex + 1

                    append("<row r=\"$excelRow\">")

                    row.forEachIndexed { columnIndex, value ->

                        val column = columnName(columnIndex)
                        val cellRef = "$column$excelRow"

                        append(
                            "<c r=\"$cellRef\" t=\"inlineStr\">" +
                                    "<is><t>${xmlEscape(value)}</t></is>" +
                                    "</c>"
                        )
                    }

                    append("</row>\n")
                }

                append(
                    """
                        </sheetData>
                    </worksheet>
                    """.trimIndent()
                )
            }

            addEntry(
                zip,
                "xl/worksheets/sheet1.xml",
                sheet
            )
        }

        return f
    }

    /**
     * Creates a database backup.
     */
    fun backup(
        c: Context,
        source: File
    ): File {

        val f = File(
            dir(c),
            "Trem-Tech-POS-Backup-${stamp()}.db"
        )

        source.copyTo(f, true)

        return f
    }

    private fun stamp(): String =
        SimpleDateFormat(
            "yyyyMMdd-HHmmss",
            Locale.US
        ).format(Date())

    /**
     * Converts a zero-based column number into an Excel column name.
     *
     * 0  -> A
     * 1  -> B
     * 25 -> Z
     * 26 -> AA
     */
    private fun columnName(index: Int): String {

        var n = index
        val result = StringBuilder()

        do {
            result.insert(
                0,
                ('A'.code + (n % 26)).toChar()
            )
            n = n / 26 - 1
        } while (n >= 0)

        return result.toString()
    }

    /**
     * Escapes text for XML.
     */
    private fun xmlEscape(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    /**
     * Adds a text/XML file into the XLSX ZIP package.
     */
    private fun addEntry(
        zip: ZipOutputStream,
        name: String,
        content: String
    ) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
