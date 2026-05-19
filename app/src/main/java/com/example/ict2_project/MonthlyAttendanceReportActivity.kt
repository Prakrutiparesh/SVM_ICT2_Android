package com.example.ict2_project

import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import android.os.Environment
import android.view.View
import android.widget.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ict2_project.api.ApiService
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.models.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument

class MonthlyAttendanceReportActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var sessionSpinner: AutoCompleteTextView
    private lateinit var mediumSpinner: AutoCompleteTextView
    private lateinit var classSpinner: AutoCompleteTextView
    private lateinit var sectionSpinner: AutoCompleteTextView
    private lateinit var yearSpinner: AutoCompleteTextView
    private lateinit var monthSpinner: AutoCompleteTextView
    private lateinit var btnGeneratePDF: Button
    private lateinit var progressBar: ProgressBar

    private var sessionList = listOf<Session>()
    private var classList = listOf<Class>()
    private var sectionList = listOf<Section>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_attendance_report)

        apiService = RetrofitClient.instance
        initViews()
        setupSpinners()
        loadSessions()

        btnGeneratePDF.setOnClickListener { generatePDF() }
    }

    private fun initViews() {
        sessionSpinner = findViewById(R.id.spinnerSession)
        mediumSpinner = findViewById(R.id.spinnerMedium)
        classSpinner = findViewById(R.id.spinnerClass)
        sectionSpinner = findViewById(R.id.spinnerSection)
        yearSpinner = findViewById(R.id.spinnerYear)
        monthSpinner = findViewById(R.id.spinnerMonth)
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF)
        progressBar = findViewById(R.id.progressBar)

        // Year spinner
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 2).toList()
        val yearAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_item,
            years
        )

        yearSpinner.setAdapter(yearAdapter)
        yearSpinner.setText(currentYear.toString(), false)

        // Month spinner
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_item,
            months
        )

        monthSpinner.setAdapter(monthAdapter)

        monthSpinner.setText(
            months[Calendar.getInstance().get(Calendar.MONTH)],
            false
        )
    }

    private fun setupSpinners() {

        val mediums = listOf("Gujarati", "English")

        val mediumAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_item,
            mediums
        )

        mediumSpinner.setAdapter(mediumAdapter)

        // Medium select hone ke baad hi class load
        mediumSpinner.setOnItemClickListener { _, _, _, _ ->

            if (sessionSpinner.text.toString().isNotEmpty()) {
                loadClasses()
            } else {
                toast("Please select session first")
                mediumSpinner.setText("", false)
            }
        }

        // Class select
        classSpinner.setOnItemClickListener { _, _, _, _ ->
            loadSections()
        }
    }

    private fun loadSessions() {
        apiService.getSessions().enqueue(
            object : Callback<List<Session>> {

                override fun onResponse(
                    call: Call<List<Session>>,
                    response: Response<List<Session>>
                ) {

                    if (response.isSuccessful) {

                        sessionList = response.body() ?: emptyList()

                        val sessionNames = sessionList.map { it.sessionName }

                        val adapter = ArrayAdapter(
                            this@MonthlyAttendanceReportActivity,
                            R.layout.dropdown_item,
                            sessionNames
                        )

                        sessionSpinner.setAdapter(adapter)

                        sessionSpinner.setOnItemClickListener { _, _, _, _ ->

                            // Session change hone par reset karo
                            classSpinner.setText("", false)
                            sectionSpinner.setText("", false)

                            classList = emptyList()
                            sectionList = emptyList()

                            // Medium selected hai tabhi load karo
                            if (mediumSpinner.text.toString().isNotEmpty()) {
                                loadClasses()
                            }
                        }
                    } else {
                        toast("Failed to load sessions")
                    }
                }

                override fun onFailure(call: Call<List<Session>>, t: Throwable) {
                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun loadClasses() {

        val selectedSession =
            sessionList.find {
                it.sessionName == sessionSpinner.text.toString()
            } ?: return

        val medium = mediumSpinner.text.toString()

        if (medium.isEmpty()) {
            toast("Select medium")
            return
        }

        // 🔥 OLD DATA CLEAR
        classSpinner.setText("", false)
        sectionSpinner.setText("", false)

        classList = emptyList()
        sectionList = emptyList()

        // 🔥 CLEAR OLD ADAPTERS
        classSpinner.setAdapter(
            ArrayAdapter(
                this,
                R.layout.dropdown_item,
                emptyList<String>()
            )
        )

        sectionSpinner.setAdapter(
            ArrayAdapter(
                this,
                R.layout.dropdown_item,
                emptyList<String>()
            )
        )

        apiService.getClasses(medium)
            .enqueue(object : Callback<List<Class>> {

                override fun onResponse(
                    call: Call<List<Class>>,
                    response: Response<List<Class>>
                ) {

                    if (response.isSuccessful) {

                        classList =
                            response.body()?.filter {
                                it.sessionId == selectedSession.sessionId &&
                                        it.medium.equals(medium, ignoreCase = true)
                            } ?: emptyList()

                        val classNames = classList.map { it.className }

                        val adapter = ArrayAdapter(
                            this@MonthlyAttendanceReportActivity,
                            R.layout.dropdown_item,
                            classNames
                        )

                        classSpinner.setAdapter(adapter)

                        if (classNames.isEmpty()) {
                            toast("No classes found for selected medium")
                        }

                    } else {
                        toast("Failed to load classes")
                    }
                }

                override fun onFailure(call: Call<List<Class>>, t: Throwable) {
                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun loadSections() {

        val selectedClass =
            classList.find {
                it.className == classSpinner.text.toString()
            } ?: return

        apiService.getSectionsByClass(selectedClass.classId)
            .enqueue(object : Callback<List<Section>> {

                override fun onResponse(
                    call: Call<List<Section>>,
                    response: Response<List<Section>>
                ) {

                    if (response.isSuccessful) {

                        sectionList = response.body() ?: emptyList()

                        val sectionNames =
                            sectionList.map { it.sectionName }

                        val adapter = ArrayAdapter(
                            this@MonthlyAttendanceReportActivity,
                            R.layout.dropdown_item,
                            sectionNames
                        )

                        sectionSpinner.setAdapter(adapter)

                    } else {
                        toast("Failed to load sections")
                    }
                }

                override fun onFailure(call: Call<List<Section>>, t: Throwable) {
                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun generatePDF() {
        val selectedSession =
            sessionList.find {
                it.sessionName == sessionSpinner.text.toString()
            }

        if (selectedSession == null) {
            toast("Select session")
            return
        }
        val selectedClass =
            classList.find {
                it.className == classSpinner.text.toString()
            }

        if (selectedClass == null) {
            toast("Select class")
            return
        }
        val selectedSection =
            sectionList.find {
                it.sectionName == sectionSpinner.text.toString()
            }

        if (selectedSection == null) {
            toast("Select section")
            return
        }

        val sessionId = selectedSession.sessionId
        val classId = selectedClass.classId
        val sectionId = selectedSection.sectionId
        val medium = mediumSpinner.text.toString().takeIf { it != "All" }
        val year =
            yearSpinner.text.toString().toInt()

        val month =
            monthSpinner.text.toString()
        val monthNumber = when (month) {
            "Jan" -> 1
            "Feb" -> 2
            "Mar" -> 3
            "Apr" -> 4
            "May" -> 5
            "Jun" -> 6
            "Jul" -> 7
            "Aug" -> 8
            "Sep" -> 9
            "Oct" -> 10
            "Nov" -> 11
            else -> 12
        }
        progressBar.visibility = View.VISIBLE
        btnGeneratePDF.isEnabled = false

        lifecycleScope.launch {
            try {
                // ✅ Correct function name
                val response = apiService.getMonthlyAttendanceReportRawJson(
                    sessionId,
                    medium,
                    classId,
                    sectionId,
                    year,
                    monthNumber
                )
                if (response.isSuccessful && response.body() != null) {
                    val jsonElement = response.body()!!
                    val gson = Gson()
                    try {
                        val report =
                            gson.fromJson(jsonElement, MonthlyReportResponse::class.java)
                        if (report.students != null && report.students.isNotEmpty()) {
                            createAndSavePdf(report)
                        } else {
                            toast("No attendance data for selected month")
                        }
                    } catch (e: Exception) {
                        // Server returned error JSON instead of data
                        val errorMsg = if (jsonElement.isJsonObject) {
                            jsonElement.asJsonObject.get("message")?.asString ?: "Unknown error"
                        } else {
                            jsonElement.asString
                        }
                        toast("Server error: $errorMsg")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorCode = response.code()
                    Log.e("MonthlyReport", "Error code: $errorCode, body: $errorBody")
                    toast("Server error ($errorCode): ${errorBody ?: "no response body"}")
                }
            } catch (e: Exception) {
                toast("Network error: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
                btnGeneratePDF.isEnabled = true
            }
        }
    }

    private fun createAndSavePdf(report: MonthlyReportResponse) {
        val pdfDocument = PdfDocument()
        val pageWidth = 1800
        val pageHeight = 1000
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas
        var y = 25f          // current Y position on page
        var pageNumber = 1

        // Paints
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 16f
            color = Color.BLACK
        }
        val boldPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val infoPaint = Paint().apply {
            textSize = 13f
            color = Color.BLACK
            isFakeBoldText = true
        }
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#E5E5E5")
            style = Paint.Style.FILL
        }
        val presentPaint = Paint().apply {
            color = Color.parseColor("#C8E6C9")
            style = Paint.Style.FILL
        }
        val absentPaint = Paint().apply {
            color = Color.parseColor("#FFCDD2")
            style = Paint.Style.FILL
        }

        // Get filter values
        val sessionName = sessionSpinner.text.toString()

        val className = classSpinner.text.toString()

        val sectionName = sectionSpinner.text.toString()

        val monthName = monthSpinner.text.toString()

        val yearName = yearSpinner.text.toString()

        val dates = report.dates ?: emptyList()
        val numDates = dates.size

        val rollWidth = 70f
        val nameWidth = 320f
        val dateWidth = 40f
        val headerHeight = 150f
        val totalWidth = 60f
        val rowHeight = 45f

        // Helper to draw a new page with header
        fun drawPageHeader(canvas: android.graphics.Canvas, startY: Float): Float {
            var yPos = startY

            // Title
            canvas.drawText("Monthly Attendance Report", 20f, yPos, titlePaint)
            yPos += 20f

            // Filter info in one line (or multiple if needed)
            val filterText =
                "Session: $sessionName | Class: $className | Section: $sectionName | Month: $monthName $yearName"
            canvas.drawText(filterText, 20f, yPos, infoPaint)
            yPos += 20f

            // Header row
            // Header row
            var x = 20f

// Roll No Header
            canvas.drawRect(
                x, yPos, x + rollWidth, yPos + headerHeight, headerBgPaint
            )
            canvas.drawRect(
                x, yPos, x + rollWidth, yPos + headerHeight, borderPaint
            )

            canvas.drawText(
                "Roll", x + 5, yPos + 22, boldPaint
            )

            x += rollWidth

// Student Name Header
            canvas.drawRect(
                x, yPos, x + nameWidth, yPos + headerHeight, headerBgPaint
            )

            canvas.drawRect(
                x, yPos, x + nameWidth, yPos + headerHeight, borderPaint
            )

            canvas.drawText(
                "Student Name", x + 8, yPos + 22, boldPaint
            )

            x += nameWidth

// DATE HEADERS
            for (dateStr in dates) {

                canvas.drawRect(
                    x, yPos, x + dateWidth, yPos + headerHeight, headerBgPaint
                )

                canvas.drawRect(
                    x, yPos, x + dateWidth, yPos + headerHeight, borderPaint
                )

                val formattedDate = formatDateHeader(dateStr)

                // Rotate date vertically
                canvas.save()

                val centerX = x + dateWidth / 2
                val centerY = yPos + headerHeight / 2

                canvas.rotate(-90f, centerX, centerY)
                canvas.drawText(
                    formattedDate, centerX - 55, centerY + 5, boldPaint
                )
                canvas.restore()
                x += dateWidth
            }

// T P A %
            for (header in listOf("T", "P", "A", "%")) {

                canvas.drawRect(
                    x, yPos, x + totalWidth, yPos + headerHeight, headerBgPaint
                )

                canvas.drawRect(
                    x, yPos, x + totalWidth, yPos + headerHeight, borderPaint
                )

                canvas.drawText(
                    header, x + 8, yPos + 22, boldPaint
                )

                x += totalWidth
            }

            yPos += headerHeight
            // Horizontal line below header
            canvas.drawLine(
                20f, yPos - 2f, x, yPos - 2f, borderPaint
            )
            return yPos
        }

        // Draw first page header
        y = drawPageHeader(canvas, y)

        // Draw student rows
        for (student in report.students ?: emptyList()) {
            // Check if need new page
            if (y + rowHeight > pageHeight - 30) {
                pdfDocument.finishPage(currentPage)
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 25f
                y = drawPageHeader(canvas, y)
            }

            var x = 20f

            // Roll number
            canvas.drawRect(x, y, x + rollWidth, y + rowHeight, borderPaint)
            canvas.drawText(student.rollNo.toString(), x + 10, y + 17, textPaint)
            x += rollWidth

            // Student name (truncate if too long)
            val displayName = student.studentName
            canvas.drawRect(x, y, x + nameWidth, y + rowHeight, borderPaint)
            canvas.drawText(displayName, x + 8, y + 18, textPaint)
            x += nameWidth

            // Attendance cells
            for (daily in student.dailyStatus) {
                val bgPaint = if (daily.status == "P") presentPaint else absentPaint
                canvas.drawRect(x, y, x + dateWidth, y + rowHeight, bgPaint)
                canvas.drawRect(x, y, x + dateWidth, y + rowHeight, borderPaint)
                canvas.drawText(
                    daily.status, x + dateWidth / 2 - 5, y + 28, boldPaint
                )
                x += dateWidth
            }

            // Totals
            val totalDays = student.present + student.absent
            val values = listOf(
                totalDays.toString(),
                student.present.toString(),
                student.absent.toString(),
                String.format("%.2f", student.percentage)
            )
            values.forEachIndexed { index, v ->

                val bg = when (index) {
                    1 -> presentPaint
                    2 -> absentPaint
                    else -> null
                }

                bg?.let {
                    canvas.drawRect(x, y, x + totalWidth, y + rowHeight, it)
                }

                canvas.drawRect(x, y, x + totalWidth, y + rowHeight, borderPaint)

                canvas.drawText(
                    v, x + 8, y + 17, if (index == 1 || index == 2) boldPaint else textPaint
                )

                x += totalWidth
            }

            y += rowHeight
        }

        pdfDocument.finishPage(currentPage)

        // Save PDF
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(downloadsDir, "MonthlyAttendance_$timeStamp.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF saved:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()
    }

    // Helper to format date as "dd-MMM" (e.g., "01-May")
    private fun formatDateHeader(dateStr: String): String {
        return try {

            val parts = dateStr.substring(0, 10).split("-")

            val year = parts[0]
            val monthNum = parts[1].toInt()
            val day = parts[2]

            val monthName = when (monthNum) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                else -> "Dec"
            }
            "$day-$monthName-$year"

        } catch (e: Exception) {
            dateStr.take(10)
        }
    }

    private fun formatDate(date: String): String {

        return try {

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val outputFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

            val parsedDate = inputFormat.parse(date.take(10))

            outputFormat.format(parsedDate!!)

        } catch (e: Exception) {

            date.take(10)
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}