package com.example.ict2_project

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ict2_project.api.ApiService
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.databinding.ActivityMarksEntryBinding
import com.example.ict2_project.models.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MarksEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarksEntryBinding
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences

    // Data lists
    private var sessionList = listOf<Session>()
    private var classList = listOf<Class>()
    private var sectionList = listOf<Section>()
    private var examsList = listOf<Exam>()
    private var examSubjectsList = listOf<ExamSubject>()

    // Selected items
    private var selectedSessionId: Int? = null
    private var selectedMedium: String? = null
    private var selectedClassId: Int? = null
    private var selectedSectionId: Int? = null
    private var selectedExam: Exam? = null
    private var selectedSubject: ExamSubject? = null

    private var currentStudents = listOf<StudentMarksData>()
    private lateinit var studentAdapter: StudentMarksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarksEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        sharedPref = getSharedPreferences("TeacherApp", MODE_PRIVATE)
        apiService = RetrofitClient.instance

        loadSessions()
        setupListeners()
        binding.spinnerExam.setTextColor(getColor(R.color.text_primary))
        binding.spinnerSubject.setTextColor(getColor(R.color.text_primary))
    }

    private fun loadSessions() {
        apiService.getSessions().enqueue(object : Callback<List<Session>> {
            override fun onResponse(call: Call<List<Session>>, response: Response<List<Session>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    sessionList = response.body()!!
                    val sessionNames = sessionList.map { it.sessionName }
                    val adapter = ArrayAdapter(
                        this@MarksEntryActivity,
                        R.layout.dropdown_item,
                        sessionNames
                    )
                    binding.filterDropdowns.spinnerSession.setAdapter(adapter)

                    if (sessionList.isNotEmpty()) {
                        binding.filterDropdowns.spinnerSession.setText(sessionNames[0], false)
                        selectedSessionId = sessionList[0].sessionId
                        loadMediums()
                    }

                    binding.filterDropdowns.spinnerSession.setOnItemClickListener { _, _, position, _ ->
                        selectedSessionId = sessionList[position].sessionId
                        clearClassAndSection()
                        loadMediums()
                    }
                } else {
                    Toast.makeText(this@MarksEntryActivity, "No sessions found", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Session>>, t: Throwable) {
                Toast.makeText(this@MarksEntryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun loadMediums() {
        val mediums = listOf("Gujarati", "English")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, mediums)
        binding.filterDropdowns.spinnerMedium.setAdapter(adapter)
        binding.filterDropdowns.spinnerMedium.setText(mediums[0], false)
        selectedMedium = mediums[0]

        loadClasses()

        binding.filterDropdowns.spinnerMedium.setOnItemClickListener { _, _, position, _ ->
            selectedMedium = mediums[position]
            clearClassAndSection()
            loadClasses()
        }
    }

    private fun clearClassAndSection() {
        selectedClassId = null
        selectedSectionId = null
        selectedExam = null
        selectedSubject = null
        classList = emptyList()
        sectionList = emptyList()
        examsList = emptyList()
        examSubjectsList = emptyList()

        binding.filterDropdowns.spinnerClass.setText("", false)
        binding.filterDropdowns.spinnerSection.setText("", false)
        binding.spinnerExam.setText("", false)
        binding.spinnerExam.setAdapter(null)
        binding.spinnerSubject.setText("", false)
        binding.spinnerSubject.setAdapter(null)
        binding.subjectLayout.visibility = android.view.View.GONE
        binding.rvStudents.visibility = android.view.View.GONE
        binding.tvStudentsHeader.visibility = android.view.View.GONE
        binding.btnSubmit.isEnabled = false

        binding.filterDropdowns.spinnerClass.setAdapter(
            ArrayAdapter(this, R.layout.dropdown_item, emptyList<String>())
        )
        binding.filterDropdowns.spinnerSection.setAdapter(
            ArrayAdapter(this, R.layout.dropdown_item, emptyList<String>())
        )
    }

    private fun loadClasses() {
        if (selectedSessionId == null || selectedMedium.isNullOrEmpty()) return

        showProgress(true)
        apiService.getClasses(selectedMedium!!).enqueue(object : Callback<List<Class>> {
            override fun onResponse(call: Call<List<Class>>, response: Response<List<Class>>) {
                showProgress(false)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val allClasses = response.body()!!
                    classList = allClasses.filter {
                        it.sessionId == selectedSessionId &&
                                it.medium.equals(selectedMedium, ignoreCase = true)
                    }

                    val classNames = classList.map { it.className }
                    val adapter = ArrayAdapter(
                        this@MarksEntryActivity,
                        R.layout.dropdown_item,
                        classNames
                    )
                    binding.filterDropdowns.spinnerClass.setAdapter(adapter)

                    if (classNames.isEmpty()) {
                        Toast.makeText(
                            this@MarksEntryActivity,
                            "No classes found for this session & medium",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.filterDropdowns.spinnerClass.setOnItemClickListener { _, _, position, _ ->
                        selectedClassId = classList[position].classId
                        clearSectionAndExam()
                        loadSections()
                    }
                } else {
                    Toast.makeText(
                        this@MarksEntryActivity,
                        "No classes found for $selectedMedium",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Class>>, t: Throwable) {
                showProgress(false)
                Toast.makeText(this@MarksEntryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun clearSectionAndExam() {
        selectedSectionId = null
        selectedExam = null
        selectedSubject = null
        sectionList = emptyList()
        examsList = emptyList()
        examSubjectsList = emptyList()

        binding.filterDropdowns.spinnerSection.setText("", false)
        binding.spinnerExam.setText("", false)
        binding.spinnerExam.setAdapter(null)
        binding.spinnerSubject.setText("", false)
        binding.spinnerSubject.setAdapter(null)
        binding.subjectLayout.visibility = android.view.View.GONE
        binding.rvStudents.visibility = android.view.View.GONE
        binding.tvStudentsHeader.visibility = android.view.View.GONE
        binding.btnSubmit.isEnabled = false

        binding.filterDropdowns.spinnerSection.setAdapter(
            ArrayAdapter(this, R.layout.dropdown_item, emptyList<String>())
        )
    }

    private fun loadSections() {
        if (selectedClassId == null) return

        showProgress(true)
        apiService.getSectionsByClass(selectedClassId!!).enqueue(object : Callback<List<Section>> {
            override fun onResponse(call: Call<List<Section>>, response: Response<List<Section>>) {
                showProgress(false)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    sectionList = response.body()!!
                    val sectionNames = sectionList.map { it.sectionName }
                    val adapter = ArrayAdapter(
                        this@MarksEntryActivity,
                        R.layout.dropdown_item,
                        sectionNames
                    )
                    binding.filterDropdowns.spinnerSection.setAdapter(adapter)

                    binding.filterDropdowns.spinnerSection.setOnItemClickListener { _, _, position, _ ->
                        selectedSectionId = sectionList[position].sectionId
                        clearExamAndSubject()
                        loadExams()
                    }
                } else {
                    Toast.makeText(this@MarksEntryActivity, "No sections found", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Section>>, t: Throwable) {
                showProgress(false)
                Toast.makeText(this@MarksEntryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun clearExamAndSubject() {
        selectedExam = null
        selectedSubject = null
        examsList = emptyList()
        examSubjectsList = emptyList()

        binding.spinnerExam.setText("", false)
        binding.spinnerExam.setAdapter(null)
        binding.spinnerSubject.setText("", false)
        binding.spinnerSubject.setAdapter(null)
        binding.subjectLayout.visibility = android.view.View.GONE
        binding.rvStudents.visibility = android.view.View.GONE
        binding.tvStudentsHeader.visibility = android.view.View.GONE
        binding.btnSubmit.isEnabled = false
    }

    private fun loadExams() {
        if (selectedSessionId == null || selectedMedium.isNullOrEmpty() ||
            selectedClassId == null || selectedSectionId == null
        ) return

        showProgress(true)
        lifecycleScope.launch {
            try {
                val exams = apiService.getExamsByFilters(
                    sessionId = selectedSessionId!!,
                    medium = selectedMedium!!,
                    classId = selectedClassId!!,
                    sectionId = selectedSectionId!!,
                    publishedOnly = false
                )

                examsList = exams
                setupExamSpinner()
                showProgress(false)
            } catch (e: Exception) {
                showProgress(false)
                Toast.makeText(
                    this@MarksEntryActivity,
                    "Error loading exams: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupExamSpinner() {
        if (examsList.isEmpty()) {
            binding.tvNoData.visibility = android.view.View.VISIBLE
            binding.tvNoData.text = "No exams found for selected filters"
            binding.spinnerExam.setText("", false)
            binding.spinnerExam.setAdapter(null)
            binding.subjectLayout.visibility = android.view.View.GONE
            return
        }

        binding.tvNoData.visibility = android.view.View.GONE

        // ✅ Simple exam names only in dropdown
        val examNames = examsList.map { "${it.examName} (${it.examType})" }
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, examNames)
        binding.spinnerExam.setAdapter(adapter)

        binding.spinnerExam.setText(examNames[0], false)
        binding.spinnerExam.setTextColor(getColor(R.color.text_primary))
        selectedExam = examsList[0]

        // ✅ Show status below dropdown
        showExamStatus()
        loadExamSubjects()

        binding.spinnerExam.setOnItemClickListener { _, _, position, _ ->
            selectedExam = examsList[position]
            showExamStatus()  // Update status when exam changes
            loadExamSubjects()
        }
    }

    private fun showExamStatus() {
        val exam = selectedExam ?: return
        val statusText = if (exam.isPublished) {
            "Results have been declared. Marks cannot be edited."
        } else {
            "Marks entry is open. You can enter/edit marks."
        }

        // Show status in a TextView below the exam dropdown
        binding.tvExamStatus.text = statusText
        binding.tvExamStatus.visibility = android.view.View.VISIBLE
        binding.tvExamStatus.setTextColor(
            if (exam.isPublished) getColor(R.color.status_absent_text)
            else getColor(R.color.status_present_text)
        )
    }

    private fun loadExamSubjects() {
        val exam = selectedExam ?: return
        val staffId = sharedPref.getInt("StaffId", 0)

        android.util.Log.d("MarksEntry", "========== LOADING SUBJECTS ==========")
        android.util.Log.d("MarksEntry", "StaffId: $staffId")
        android.util.Log.d("MarksEntry", "Exam ClassId: ${exam.classId}")
        android.util.Log.d("MarksEntry", "Exam SessionId: ${exam.sessionId}")
        android.util.Log.d("MarksEntry", "ExamId: ${exam.examId}")

        showProgress(true)
        lifecycleScope.launch {
            try {
                val allTeacherSubjects = apiService.getTeacherSubjects()
                android.util.Log.d("MarksEntry", "All teacher subjects: ${allTeacherSubjects.size}")

                val mySubjects = allTeacherSubjects.filter {
                    it.staffId == staffId &&
                            it.classId == exam.classId &&
                            it.sessionId == exam.sessionId
                }

                val mySubjectIds = mySubjects.map { it.subjectId }.toSet()
                android.util.Log.d("MarksEntry", "Teacher's subjects: $mySubjectIds")

                val allExamSubjects = apiService.getExamSubjects(exam.examId)
                android.util.Log.d("MarksEntry", "Exam subjects: ${allExamSubjects.size}")

                examSubjectsList = allExamSubjects.filter { it.subjectId in mySubjectIds }

                android.util.Log.d("MarksEntry", "Final subjects: ${examSubjectsList.size}")

                if (examSubjectsList.isEmpty()) {
                    Toast.makeText(
                        this@MarksEntryActivity,
                        "No subjects assigned to you for this exam",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.subjectLayout.visibility = android.view.View.GONE
                } else {
                    setupSubjectSpinner()
                    binding.subjectLayout.visibility = android.view.View.VISIBLE
                }
                showProgress(false)
            } catch (e: Exception) {
                showProgress(false)
                android.util.Log.e("MarksEntry", "Error: ${e.message}")
                Toast.makeText(
                    this@MarksEntryActivity,
                    "Error loading subjects: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupSubjectSpinner() {
        android.util.Log.d("MarksEntry", "========== SUBJECT LIST ==========")

        val subjectNames = examSubjectsList.map { examSubject ->
            val name = examSubject.subject?.subjectName ?: "Unknown"
            android.util.Log.d(
                "MarksEntry",
                "Subject: $name, Total Marks: ${examSubject.totalMarks}"
            )
            "${name} (Max: ${examSubject.totalMarks})"
        }

        if (subjectNames.isEmpty()) {
            android.util.Log.d("MarksEntry", "No subjects found!")
            binding.tvNoData.visibility = android.view.View.VISIBLE
            binding.tvNoData.text = "No subjects found for this exam"
            return
        }

        val adapter = ArrayAdapter(this, R.layout.dropdown_item, subjectNames)
        binding.spinnerSubject.setAdapter(adapter)
        binding.spinnerSubject.setTextColor(getColor(R.color.text_primary))

        binding.spinnerSubject.setText(subjectNames[0], false)
        selectedSubject = examSubjectsList[0]
        loadStudentsAndMarks()

        binding.spinnerSubject.setOnItemClickListener { _, _, position, _ ->
            selectedSubject = examSubjectsList[position]
            loadStudentsAndMarks()
        }
    }

    private fun loadStudentsAndMarks() {
        val exam = selectedExam ?: return
        val subject = selectedSubject ?: return

        showProgress(true)
        lifecycleScope.launch {
            try {
                val response = apiService.getMarksData(exam.examId, exam.classId, exam.sectionId)
                currentStudents = response.students.map { student ->
                    val subjectMark =
                        student.marks.find { it.examSubjectId == subject.examSubjectId }
                    StudentMarksData(
                        studentId = student.studentId,
                        rollNo = student.rollNo,
                        studentName = student.studentName,
                        grno = student.grno,
                        marks = listOf(subjectMark ?: SubjectMark(subject.examSubjectId, null))
                    )
                }

                if (currentStudents.isEmpty()) {

                    binding.tvNoData.visibility = android.view.View.VISIBLE
                    binding.tvNoData.text = "No students found"

                    binding.headerLayout.visibility = android.view.View.GONE
                    binding.rvStudents.visibility = android.view.View.GONE
                    binding.tvStudentsHeader.visibility = android.view.View.GONE

                    binding.btnSubmit.isEnabled = false

                } else {

                    binding.tvNoData.visibility = android.view.View.GONE

                    setupRecyclerView()

                    binding.headerLayout.visibility = android.view.View.VISIBLE
                    binding.tvStudentsHeader.visibility = android.view.View.VISIBLE
                    binding.rvStudents.visibility = android.view.View.VISIBLE
                    binding.btnSubmit.isEnabled = true
                }
                showProgress(false)
            } catch (e: Exception) {
                showProgress(false)
                Toast.makeText(this@MarksEntryActivity, "Error: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun setupRecyclerView() {
        val isEditable = selectedExam?.isPublished == false

        studentAdapter = StudentMarksAdapter(
            currentStudents,
            selectedSubject!!.totalMarks,
            isEditable
        )

        binding.rvStudents.apply {
            layoutManager = LinearLayoutManager(this@MarksEntryActivity)
            adapter = studentAdapter
            isNestedScrollingEnabled = false  // ✅ Important for NestedScrollView
            setHasFixedSize(false)
        }

        // ✅ Scroll to top when data loads
        binding.rvStudents.post {
            binding.mainScrollView.fullScroll(android.view.View.FOCUS_UP)
        }

        binding.btnSubmit.visibility =
            if (isEditable) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun setupListeners() {
        binding.btnSubmit.setOnClickListener {
            submitMarks()
        }
    }

    private fun submitMarks() {
        val userId = sharedPref.getInt("UserId", 0)
        val marksToSave =
            studentAdapter.getMarksList().mapNotNull { (examSubjectId, studentId, obtained) ->
                if (obtained != null && obtained >= 0f) {
                    SaveMarksRequest(
                        examSubjectId = examSubjectId,
                        studentId = studentId,
                        obtainedMarks = obtained,
                        enteredBy = userId
                    )
                } else null
            }

        if (marksToSave.isEmpty()) {
            Toast.makeText(this, "No marks entered", Toast.LENGTH_SHORT).show()
            return
        }

        showProgress(true)
        lifecycleScope.launch {
            try {
                val response = apiService.saveMarks(marksToSave)
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MarksEntryActivity,
                        "Marks saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadStudentsAndMarks()
                } else {
                    Toast.makeText(
                        this@MarksEntryActivity,
                        "Failed to save marks",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                showProgress(false)
            } catch (e: Exception) {
                showProgress(false)
                Toast.makeText(this@MarksEntryActivity, "Error: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility =
            if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnSubmit.isEnabled = !show
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}