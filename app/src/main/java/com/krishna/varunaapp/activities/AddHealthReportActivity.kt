package com.krishna.varunaapp.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.R
import com.krishna.varunaapp.models.HealthReport
import java.text.SimpleDateFormat
import java.util.*

class AddHealthReportActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Views
    private lateinit var tvVillageName: TextView
    private lateinit var etPatientName: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var spinnerGender: Spinner
    private lateinit var listViewSymptoms: ListView
    private lateinit var etSymptomStartDate: TextInputEditText
    private lateinit var spinnerSeverity: Spinner
    private lateinit var etWaterSource: TextInputEditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var etAdditionalNotes: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var btnViewReports: Button
    private lateinit var toolbar: Toolbar

    private var villageName = ""
    private var symptomStartDate = 0L
    private val selectedSymptoms = mutableListOf<String>()

    // Water-related diseases and symptoms
    private val waterRelatedSymptoms = listOf(
        "Diarrhea",
        "Vomiting",
        "Abdominal Pain",
        "Nausea",
        "Fever",
        "Dehydration",
        "Cholera Symptoms",
        "Typhoid Symptoms",
        "Dysentery",
        "Hepatitis A Symptoms",
        "Skin Rash",
        "Eye Infection",
        "Jaundice",
        "Fatigue",
        "Loss of Appetite"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_health_report)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        villageName = intent.getStringExtra("villageName") ?: ""

        initViews()
        setupToolbar()
        setupSpinners()
        setupDatePicker()
        setupSymptomsList()
        setupButtons()
    }

    private fun initViews() {
        tvVillageName = findViewById(R.id.tvVillageName)
        etPatientName = findViewById(R.id.etPatientName)
        etAge = findViewById(R.id.etAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        listViewSymptoms = findViewById(R.id.listViewSymptoms)
        etSymptomStartDate = findViewById(R.id.etSymptomStartDate)
        spinnerSeverity = findViewById(R.id.spinnerSeverity)
        etWaterSource = findViewById(R.id.etWaterSource)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnViewReports = findViewById(R.id.btnViewReports)
        toolbar = findViewById(R.id.toolbarHealthReport)

        tvVillageName.text = "Health Report - $villageName"
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        btnSubmit.setOnClickListener { submitReport() }
        btnViewReports.setOnClickListener { finish() }
    }

    private fun setupSpinners() {
        // Gender Spinner
        val genderAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Male", "Female", "Other")
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        // Severity Spinner
        val severityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Mild", "Moderate", "Severe")
        )
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeverity.adapter = severityAdapter

        // Status Spinner
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Not Cured", "Cured")
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter
    }

    private fun setupDatePicker() {
        etSymptomStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    symptomStartDate = calendar.timeInMillis
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etSymptomStartDate.setText(sdf.format(Date(symptomStartDate)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSymptomsList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            waterRelatedSymptoms
        )
        listViewSymptoms.adapter = adapter
        listViewSymptoms.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    private fun submitReport() {
        val patientName = etPatientName.text.toString().trim()
        val ageStr = etAge.text.toString().trim()
        val waterSource = etWaterSource.text.toString().trim()
        val additionalNotes = etAdditionalNotes.text.toString().trim()

        // Validation
        if (patientName.isEmpty()) {
            Toast.makeText(this, "Please enter patient name", Toast.LENGTH_SHORT).show()
            return
        }

        if (ageStr.isEmpty()) {
            Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull()
        if (age == null || age <= 0) {
            Toast.makeText(this, "Please enter valid age", Toast.LENGTH_SHORT).show()
            return
        }

        if (symptomStartDate == 0L) {
            Toast.makeText(this, "Please select symptom start date", Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected symptoms
        selectedSymptoms.clear()
        val checkedPositions = listViewSymptoms.checkedItemPositions
        for (i in 0 until checkedPositions.size()) {
            if (checkedPositions.valueAt(i)) {
                selectedSymptoms.add(waterRelatedSymptoms[checkedPositions.keyAt(i)])
            }
        }

        if (selectedSymptoms.isEmpty()) {
            Toast.makeText(this, "Please select at least one symptom", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = spinnerGender.selectedItem.toString()
        val severity = spinnerSeverity.selectedItem.toString()
        val status = spinnerStatus.selectedItem.toString()

        // Create report
        val reportId = db.collection("villages")
            .document(villageName)
            .collection("health_reports")
            .document().id

        val report = HealthReport(
            id = reportId,
            patientName = patientName,
            age = age,
            gender = gender,
            symptoms = selectedSymptoms,
            symptomStartDate = symptomStartDate,
            severity = severity,
            waterSource = waterSource,
            additionalNotes = additionalNotes,
            status = status,
            createdAt = System.currentTimeMillis(),
            villageName = villageName,
            reportedBy = auth.currentUser?.uid ?: ""
        )

        // Save to Firebase
        db.collection("villages")
            .document(villageName)
            .collection("health_reports")
            .document(reportId)
            .set(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Health report submitted successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        etPatientName.text?.clear()
        etAge.text?.clear()
        etSymptomStartDate.text?.clear()
        etWaterSource.text?.clear()
        etAdditionalNotes.text?.clear()
        spinnerGender.setSelection(0)
        spinnerSeverity.setSelection(0)
        spinnerStatus.setSelection(0)
        listViewSymptoms.clearChoices()
        symptomStartDate = 0L
        selectedSymptoms.clear()
    }
}