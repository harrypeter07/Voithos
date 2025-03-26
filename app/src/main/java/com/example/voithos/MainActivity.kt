package com.example.voithos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.tensorflow.lite.Interpreter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var tvResults: TextView
    private lateinit var btnAnalyze: MaterialButton
    private lateinit var btnCompliance: MaterialButton
    private lateinit var btnGeneratePDF: MaterialButton
    private lateinit var btnSharePDF: MaterialButton
    private lateinit var btnVoiceCommand: MaterialButton
    private lateinit var tflite: Interpreter
    private val TAX_RATE = 0.18
    private val SPEECH_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResults = findViewById(R.id.tvResults)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        btnCompliance = findViewById(R.id.btnCompliance)
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF)
        btnSharePDF = findViewById(R.id.btnSharePDF)
        btnVoiceCommand = findViewById(R.id.btnVoiceCommand)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        loadModel()

        btnAnalyze.setOnClickListener {
            tvResults.text = "Running financial analysis..."
            val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/financial_report.xlsx"
            Executors.newSingleThreadExecutor().execute {
                analyzeFinancialReport(filePath)
            }
        }

        btnCompliance.setOnClickListener {
            tvResults.text = "Running ML compliance check..."
            Executors.newSingleThreadExecutor().execute {
                runMLComplianceCheck()
            }
        }

        btnGeneratePDF.setOnClickListener {
            tvResults.text = "Generating PDF report..."
            generatePDFReport(
                revenue = 10000.0,
                tax = 10000.0 * TAX_RATE,
                finalRevenue = 10000.0 * (1 - TAX_RATE),
                complianceStatus = "✅ Compliant",
                complianceIssues = 0,
                recommendations = "No issues detected."
            )
        }

        btnSharePDF.setOnClickListener {
            tvResults.text = "Sharing PDF report..."
            sharePDFReport()
        }

        btnVoiceCommand.setOnClickListener {
            startVoiceRecognition()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied, cannot analyze file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command: Analyze, Compliance, Generate, Share")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { command ->
                processVoiceCommand(command.lowercase(Locale.getDefault()))
            }
        }
    }

    private fun processVoiceCommand(command: String) {
        when {
            command.contains("analyze") -> btnAnalyze.performClick()
            command.contains("compliance") -> btnCompliance.performClick()
            command.contains("generate") -> btnGeneratePDF.performClick()
            command.contains("share") -> btnSharePDF.performClick()
            else -> runOnUiThread {
                Toast.makeText(this, "Command not recognized: $command", Toast.LENGTH_SHORT).show()
                tvResults.text = "Unrecognized command: $command\nTry: Analyze, Compliance, Generate, Share"
            }
        }
    }

    private fun loadModel() {
        try {
            val fileDescriptor = assets.openFd("compliance_model.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val mappedByteBuffer: MappedByteBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )
            tflite = Interpreter(mappedByteBuffer)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Failed to load ML model: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun runMLComplianceCheck() {
        if (!::tflite.isInitialized) {
            runOnUiThread { Toast.makeText(this, "ML model not loaded", Toast.LENGTH_SHORT).show() }
            return
        }
        try {
            val input = floatArrayOf(1.0f, 0.0f, 1.0f)
            val output = Array(1) { FloatArray(1) }
            tflite.run(input, output)
            val complianceScore = output[0][0]
            val result = if (complianceScore > 0.5f) "⚠️ Non-compliant" else "✅ Compliant"
            runOnUiThread {
                Toast.makeText(this, "ML Compliance Check: $result", Toast.LENGTH_LONG).show()
                tvResults.text = "ML Compliance Check:\nResult: $result\nScore: ${"%.2f".format(complianceScore)}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error during compliance check", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun analyzeFinancialReport(filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                runOnUiThread { tvResults.text = "Financial report file not found at $filePath" }
                return
            }
            val workbook = WorkbookFactory.create(file)
            val sheet = workbook.getSheetAt(0)
            var totalRevenue = 0.0
            for (row in sheet) {
                for (cell in row) {
                    val cellValue = cell.toString().trim()
                    if (cellValue.matches(Regex("\\d+(\\.\\d{1,2})?"))) {
                        totalRevenue += cellValue.toDouble()
                    }
                }
            }
            workbook.close()
            val taxPayable = totalRevenue * TAX_RATE
            val finalRevenue = totalRevenue - taxPayable
            runOnUiThread {
                tvResults.text = "Financial Analysis:\nRevenue: $${"%.2f".format(totalRevenue)}\nTax: $${"%.2f".format(taxPayable)}\nFinal Revenue: $${"%.2f".format(finalRevenue)}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { tvResults.text = "Error in financial analysis: ${e.message}" }
        }
    }

    private fun generatePDFReport(
        revenue: Double,
        tax: Double,
        finalRevenue: Double,
        complianceStatus: String,
        complianceIssues: Int,
        recommendations: String
    ) {
        try {
            val document = PDDocument()
            val page = PDPage()
            document.addPage(page)
            val contentStream = PDPageContentStream(document, page)
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20f)
            contentStream.newLineAtOffset(50f, 750f)
            contentStream.showText("Voithos Report")
            contentStream.newLineAtOffset(0f, -25f)
            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            contentStream.showText("Generated on: $timeStamp")
            contentStream.newLineAtOffset(0f, -30f)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
            contentStream.showText("Financial Data:")
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            contentStream.showText("Revenue: $${"%.2f".format(revenue)}")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Tax (18%): $${"%.2f".format(tax)}")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Revenue After Tax: $${"%.2f".format(finalRevenue)}")
            contentStream.newLineAtOffset(0f, -25f)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
            contentStream.showText("Compliance Data:")
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            contentStream.showText("Status: $complianceStatus")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Issues Detected: $complianceIssues")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Recommendations: $recommendations")
            contentStream.endText()
            contentStream.close()
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, "Voithos_Report.pdf")
            document.save(file)
            document.close()
            runOnUiThread {
                Toast.makeText(this, "PDF saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                tvResults.text = "PDF Report generated."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun sharePDFReport() {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "Voithos_Report.pdf")
        if (file.exists()) {
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Voithos Report")
                putExtra(Intent.EXTRA_TEXT, "Please find attached the Voithos financial and compliance report.")
            }
            startActivity(Intent.createChooser(intent, "Share PDF Report"))
        } else {
            Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show()
        }
    }
}