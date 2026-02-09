package com.safepaw.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.safepaw.app.data.models.Animal
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ContractGenerator {

    fun generateAdoptionContract(context: Context, animal: Animal) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        var yPos = 80f
        
        // Título
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("CONTRATO DE ADOPCIÓN - SAFE PAW", 50f, yPos, paint)
        
        yPos += 60f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        
        // Cuerpo del contrato
        val text = """
            Por el presente documento, se formaliza la adopción del animal con los 
            siguientes datos registrados en el sistema SafePaw:
            
            NOMBRE DEL ANIMAL: ${animal.nombre}
            ESPECIE: ${animal.especie}
            MICROCHIP: ${animal.microchip}
            ID INTERNO: ${animal.id_animal}
            
            El adoptante se compromete a brindar los cuidados necesarios, 
            alimentación adecuada y atención veterinaria requerida para el 
            bienestar del animal.
            
            Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())}
            
            __________________________          __________________________
                 Firma del Refugio                   Firma del Adoptante
        """.trimIndent()

        for (line in text.split("\n")) {
            canvas.drawText(line, 50f, yPos, paint)
            yPos += 25f
        }

        pdfDocument.finishPage(page)

        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "Contrato_${animal.nombre}_${animal.microchip}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF generado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
