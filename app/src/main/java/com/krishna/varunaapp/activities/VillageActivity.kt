package com.krishna.varunaapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.R
import com.krishna.varunaapp.adapters.WaterTableAdapter
import com.krishna.varunaapp.databinding.ActivityVillageBinding
import com.krishna.varunaapp.databinding.DialogTableInputBinding
import com.krishna.varunaapp.models.WaterParameter
import com.krishna.varunaapp.models.WaterTestTable
import android.view.View

class VillageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVillageBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: WaterTableAdapter
    private val tables = mutableListOf<WaterTestTable>()

    private var villageName = ""
    private var userRole = "GeneralUser"

    // ---------------- CSV PICKER ----------------
    private val csvPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) parseCsvFile(uri)
        }
    }
    // ----------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVillageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        villageName = intent.getStringExtra("villageName") ?: "Unknown Village"
        binding.tvVillageName.text = villageName

        setSupportActionBar(binding.toolbarVillage)
        supportActionBar?.title = villageName
        binding.toolbarVillage.setNavigationOnClickListener { finish() }

        setupRecycler()
        loadUserRole()
        fetchTables()

        binding.btnAddParameter.setOnClickListener { openTableDialog() }

        binding.btnAddHealthReport.setOnClickListener {
            val i = Intent(this, AddHealthReportActivity::class.java)
            i.putExtra("villageName", villageName)
            startActivity(i)
        }

        binding.btnViewHealthReports.setOnClickListener {
            val i = Intent(this, HealthReportsListActivity::class.java)
            i.putExtra("villageName", villageName)
            startActivity(i)
        }

        binding.btnUploadCsv.setOnClickListener {
            if (userRole == "GeneralUser") {
                Toast.makeText(this, "Only Asha/Health Workers can upload CSV", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/*"
            csvPicker.launch(intent)
        }
    }

    private fun setupRecycler() {
        adapter = WaterTableAdapter(tables)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                userRole = it.getString("role") ?: "GeneralUser"

                if (userRole == "GeneralUser") {
                    binding.btnAddParameter.visibility = View.GONE
                    binding.btnAddHealthReport.visibility = View.GONE
                    binding.btnUploadCsv.visibility = View.GONE
                }
            }
    }

    private fun fetchTables() {
        db.collection("villages")
            .document(villageName)
            .collection("water_tables")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                tables.clear()
                for (doc in snap.documents) {
                    val table = doc.toObject(WaterTestTable::class.java)
                    if (table != null) tables.add(table)
                }
                adapter.notifyDataSetChanged()
            }
    }

    // ------------------------- CSV PARSING ------------------------------

    private fun parseCsvFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val lines = inputStream?.bufferedReader()?.readLines() ?: return

            if (lines.size <= 1) {
                Toast.makeText(this, "CSV file is empty", Toast.LENGTH_SHORT).show()
                return
            }

            val rows = mutableListOf<WaterParameter>()

            // Skip header
            for (i in 1 until lines.size) {
                val row = lines[i].split(",")

                if (row.size < 4) continue

                val parameter = row[0].trim()
                val s1 = row[1].trim()
                val s2 = row[2].trim()
                val epa = row[3].trim()

                rows.add(WaterParameter(parameter, s1, s2, epa))
            }

            if (rows.isEmpty()) {
                Toast.makeText(this, "Invalid CSV format", Toast.LENGTH_SHORT).show()
                return
            }

            val table = WaterTestTable(
                createdAt = System.currentTimeMillis(),
                rows = rows
            )

            db.collection("villages")
                .document(villageName)
                .collection("water_tables")
                .add(table)
                .addOnSuccessListener {
                    tables.add(0, table)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "CSV Uploaded Successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error reading CSV", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------------------

    private fun openTableDialog() {
        val dialogBinding = DialogTableInputBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Water Test Table")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialogBinding.btnAddRow.setOnClickListener {
                addNewRow(dialogBinding)
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                saveTable(dialogBinding, dialog)
            }
        }

        dialog.show()
    }

    private fun addNewRow(binding: DialogTableInputBinding) {
        val rowView = LayoutInflater.from(this)
            .inflate(R.layout.item_parameter_row, binding.containerRows, false)

        binding.containerRows.addView(rowView)
    }

    private fun saveTable(binding: DialogTableInputBinding, dialog: AlertDialog) {
        val rows = mutableListOf<WaterParameter>()

        for (i in 0 until binding.containerRows.childCount) {
            val row = binding.containerRows.getChildAt(i)
            val p = row.findViewById<EditText>(R.id.etParam).text.toString().trim()
            val s1 = row.findViewById<EditText>(R.id.etS1).text.toString().trim()
            val s2 = row.findViewById<EditText>(R.id.etS2).text.toString().trim()
            val epa = row.findViewById<EditText>(R.id.etEpa).text.toString().trim()

            if (p.isNotEmpty()) rows.add(WaterParameter(p, s1, s2, epa))
        }

        if (rows.isEmpty()) {
            Toast.makeText(this, "Add at least 1 row", Toast.LENGTH_SHORT).show()
            return
        }

        val table = WaterTestTable(System.currentTimeMillis(), rows)

        db.collection("villages")
            .document(villageName)
            .collection("water_tables")
            .add(table)
            .addOnSuccessListener {
                tables.add(0, table)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Table Added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }
}


//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.adapters.WaterTableAdapter
//import com.krishna.varunaapp.databinding.ActivityVillageBinding
//import com.krishna.varunaapp.databinding.DialogTableInputBinding
//import com.krishna.varunaapp.models.WaterParameter
//import com.krishna.varunaapp.models.WaterTestTable
//import com.krishna.varunaapp.R
//import android.view.View
//import android.content.Intent
//
//class VillageActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityVillageBinding
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    private lateinit var adapter: WaterTableAdapter
//    private val tables = mutableListOf<WaterTestTable>()
//
//    private var villageName = ""
//    private var userRole = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityVillageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        db = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        // Get village name passed from list screen
//        villageName = intent.getStringExtra("villageName") ?: "Unknown Village"
//        binding.tvVillageName.text = villageName
//
//        setSupportActionBar(binding.toolbarVillage)
//        supportActionBar?.title = villageName
//        binding.toolbarVillage.setNavigationOnClickListener { finish() }
//
//        setupRecycler()
//        loadUserRole()
//        fetchTables()
//
//        binding.btnAddParameter.setOnClickListener { openTableDialog() }
//
//        // Add Health Report Button
//        binding.btnAddHealthReport.setOnClickListener {
//            val intent = Intent(this, AddHealthReportActivity::class.java)
//            intent.putExtra("villageName", villageName)
//            startActivity(intent)
//        }
//
//        // View Health Reports Button
//        binding.btnViewHealthReports.setOnClickListener {
//            val intent = Intent(this, HealthReportsListActivity::class.java)
//            intent.putExtra("villageName", villageName)
//            startActivity(intent)
//        }
//
//        // Upload CSV Button
//        binding.btnUploadCsv.setOnClickListener {
//            // TODO: Implement CSV upload functionality
//            Toast.makeText(this, "CSV Upload feature - Coming soon", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun setupRecycler() {
//        adapter = WaterTableAdapter(tables)
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        binding.recyclerView.adapter = adapter
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener {
//                userRole = it.getString("role") ?: "GeneralUser"
//
//                // Hide admin-only buttons for general users
//                if (userRole == "GeneralUser") {
//                    binding.btnAddParameter.visibility = View.GONE
//                    binding.btnAddHealthReport.visibility = View.GONE
//                    binding.btnUploadCsv.visibility = View.GONE
//                } else {
//                    binding.btnAddParameter.visibility = View.VISIBLE
//                    binding.btnAddHealthReport.visibility = View.VISIBLE
//                    binding.btnUploadCsv.visibility = View.VISIBLE
//                }
//            }
//    }
//
//    private fun fetchTables() {
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { snap ->
//                tables.clear()
//                for (doc in snap.documents) {
//                    val table = doc.toObject(WaterTestTable::class.java)
//                    if (table != null) tables.add(table)
//                }
//                adapter.notifyDataSetChanged()
//            }
//    }
//
//    private fun openTableDialog() {
//        val dialogBinding = DialogTableInputBinding.inflate(layoutInflater)
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("Add Water Test Table")
//            .setView(dialogBinding.root)
//            .setPositiveButton("Save", null)
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.setOnShowListener {
//            dialogBinding.btnAddRow.setOnClickListener {
//                addNewRow(dialogBinding)
//            }
//
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//                saveTable(dialogBinding, dialog)
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun addNewRow(binding: DialogTableInputBinding) {
//        val rowView = LayoutInflater.from(this)
//            .inflate(R.layout.item_parameter_row, binding.containerRows, false)
//
//        binding.containerRows.addView(rowView)
//    }
//
//    private fun saveTable(binding: DialogTableInputBinding, dialog: AlertDialog) {
//        val rows = mutableListOf<WaterParameter>()
//
//        for (i in 0 until binding.containerRows.childCount) {
//            val row = binding.containerRows.getChildAt(i)
//            val p = row.findViewById<EditText>(R.id.etParam).text.toString().trim()
//            val s1 = row.findViewById<EditText>(R.id.etS1).text.toString().trim()
//            val s2 = row.findViewById<EditText>(R.id.etS2).text.toString().trim()
//            val epa = row.findViewById<EditText>(R.id.etEpa).text.toString().trim()
//
//            if (p.isNotEmpty()) rows.add(WaterParameter(p, s1, s2, epa))
//        }
//
//        if (rows.isEmpty()) {
//            Toast.makeText(this, "Add at least 1 row", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val table = WaterTestTable(System.currentTimeMillis(), rows)
//
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .add(table)
//            .addOnSuccessListener {
//                tables.add(0, table)
//                adapter.notifyDataSetChanged()
//                Toast.makeText(this, "Table Saved", Toast.LENGTH_SHORT).show()
//                dialog.dismiss()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error saving table", Toast.LENGTH_SHORT).show()
//            }
//    }
//}

//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.adapters.WaterTableAdapter
//import com.krishna.varunaapp.databinding.ActivityVillageBinding
//import com.krishna.varunaapp.databinding.DialogTableInputBinding
//import com.krishna.varunaapp.models.WaterParameter
//import com.krishna.varunaapp.models.WaterTestTable
//import com.krishna.varunaapp.R
//import android.view.View
//import android.content.Intent
//
//class VillageActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityVillageBinding
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    private lateinit var adapter: WaterTableAdapter
//    private val tables = mutableListOf<WaterTestTable>()
//
//    private var villageName = ""
//    private var userRole = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityVillageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        db = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        // Get village name passed from list screen
//        villageName = intent.getStringExtra("villageName") ?: "Unknown Village"
//        binding.tvVillageName.text = "Village: $villageName"
//
//        setupRecycler()
//        loadUserRole()
//        fetchTables()
//
//        binding.btnAddParameter.setOnClickListener { openTableDialog() }
//
//        // New: Add Health Report Button
//        binding.btnAddHealthReport.setOnClickListener {
//            val intent = Intent(this, AddHealthReportActivity::class.java)
//            intent.putExtra("villageName", villageName)
//            startActivity(intent)
//        }
//
//        // New: View Health Reports Button
//        binding.btnViewHealthReports.setOnClickListener {
//            val intent = Intent(this, HealthReportsListActivity::class.java)
//            intent.putExtra("villageName", villageName)
//            startActivity(intent)
//        }
//
//        // New: Upload CSV Button
//        binding.btnUploadCsv.setOnClickListener {
//            // Implement CSV upload functionality
//            Toast.makeText(this, "CSV Upload feature - Coming soon", Toast.LENGTH_SHORT).show()
//        }
//
//        setSupportActionBar(binding.toolbarVillage)
//        binding.toolbarVillage.setNavigationOnClickListener { finish() }
//    }
//
//    private fun setupRecycler() {
//        adapter = WaterTableAdapter(tables)
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        binding.recyclerView.adapter = adapter
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener {
//                userRole = it.getString("role") ?: "GeneralUser"
//                if (userRole == "GeneralUser") {
//                    binding.btnAddParameter.visibility = View.GONE
//                    binding.btnAddHealthReport.visibility = View.GONE
//                    binding.btnUploadCsv.visibility = View.GONE
//                }
//            }
//    }
//
//    private fun fetchTables() {
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { snap ->
//                tables.clear()
//                for (doc in snap.documents) {
//                    val table = doc.toObject(WaterTestTable::class.java)
//                    if (table != null) tables.add(table)
//                }
//                adapter.notifyDataSetChanged()
//            }
//    }
//
//    private fun openTableDialog() {
//        val dialogBinding = DialogTableInputBinding.inflate(layoutInflater)
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("Add Water Test Table")
//            .setView(dialogBinding.root)
//            .setPositiveButton("Save", null)
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.setOnShowListener {
//            dialogBinding.btnAddRow.setOnClickListener {
//                addNewRow(dialogBinding)
//            }
//
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//                saveTable(dialogBinding, dialog)
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun addNewRow(binding: DialogTableInputBinding) {
//        val rowView = LayoutInflater.from(this)
//            .inflate(R.layout.item_parameter_row, binding.containerRows, false)
//
//        binding.containerRows.addView(rowView)
//    }
//
//    private fun saveTable(binding: DialogTableInputBinding, dialog: AlertDialog) {
//        val rows = mutableListOf<WaterParameter>()
//
//        for (i in 0 until binding.containerRows.childCount) {
//            val row = binding.containerRows.getChildAt(i)
//            val p = row.findViewById<EditText>(R.id.etParam).text.toString().trim()
//            val s1 = row.findViewById<EditText>(R.id.etS1).text.toString().trim()
//            val s2 = row.findViewById<EditText>(R.id.etS2).text.toString().trim()
//            val epa = row.findViewById<EditText>(R.id.etEpa).text.toString().trim()
//
//            if (p.isNotEmpty()) rows.add(WaterParameter(p, s1, s2, epa))
//        }
//
//        if (rows.isEmpty()) {
//            Toast.makeText(this, "Add at least 1 row", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val table = WaterTestTable(System.currentTimeMillis(), rows)
//
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .add(table)
//            .addOnSuccessListener {
//                tables.add(0, table)
//                adapter.notifyDataSetChanged()
//                Toast.makeText(this, "Table Saved", Toast.LENGTH_SHORT).show()
//                dialog.dismiss()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error saving table", Toast.LENGTH_SHORT).show()
//            }
//    }
//}

//package com.krishna.varunaapp.activities
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.databinding.ActivityVillageBinding
//
//
//class VillageActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityVillageBinding
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    private var userRole = "GeneralUser"
//    private var villageName = ""
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityVillageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        db = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        villageName = intent.getStringExtra("villageName") ?: "Unknown Village"
//        binding.tvVillageName.text = "Village: $villageName"
//
//        loadUserRole()
//
//        binding.btnAddWaterData.setOnClickListener {
//            val i = Intent(this, WaterDataActivity::class.java)
//            i.putExtra("villageName", villageName)
//            startActivity(i)
//        }
//
//        binding.btnUploadCsv.setOnClickListener {
//            val i = Intent(this, UploadCsvActivity::class.java)
//            i.putExtra("villageName", villageName)
//            startActivity(i)
//        }
//
//        binding.btnAddHealthReport.setOnClickListener {
//            val i = Intent(this, AddHealthReportActivity::class.java)
//            i.putExtra("villageName", villageName)
//            startActivity(i)
//        }
//
//        binding.btnViewReports.setOnClickListener {
//            val i = Intent(this, ViewReportsActivity::class.java)
//            i.putExtra("villageName", villageName)
//            startActivity(i)
//        }
//
//        setSupportActionBar(binding.toolbarVillage)
//        binding.toolbarVillage.setNavigationOnClickListener { finish() }
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener {
//                userRole = it.getString("role") ?: "GeneralUser"
//
//                if (userRole == "GeneralUser") {
//                    binding.btnAddWaterData.visibility = View.GONE
//                    binding.btnUploadCsv.visibility = View.GONE
//                    binding.btnAddHealthReport.visibility = View.GONE
//                }
//            }
//    }
//}


//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.adapters.WaterTableAdapter
//import com.krishna.varunaapp.databinding.ActivityVillageBinding
//import com.krishna.varunaapp.databinding.DialogTableInputBinding
//import com.krishna.varunaapp.models.WaterParameter
//import com.krishna.varunaapp.models.WaterTestTable
//import com.krishna.varunaapp.R
//import android.view.View
//
//class VillageActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityVillageBinding
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    private lateinit var adapter: WaterTableAdapter
//    private val tables = mutableListOf<WaterTestTable>()
//
//    private var villageName = ""
//    private var userRole = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityVillageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        db = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        // Get village name passed from list screen
//        villageName = intent.getStringExtra("villageName") ?: "Unknown Village"
//        binding.tvVillageName.text = "Village: $villageName"
//
//        setupRecycler()
//        loadUserRole()
//        fetchTables()
//
//        binding.btnAddParameter.setOnClickListener { openTableDialog() }
//
//        setSupportActionBar(binding.toolbarVillage)
//        binding.toolbarVillage.setNavigationOnClickListener { finish() }
//    }
//
//    private fun setupRecycler() {
//        adapter = WaterTableAdapter(tables)
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        binding.recyclerView.adapter = adapter
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener {
//                userRole = it.getString("role") ?: "GeneralUser"
//                if (userRole == "GeneralUser") binding.btnAddParameter.visibility = View.GONE
//            }
//    }
//
//    private fun fetchTables() {
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { snap ->
//                tables.clear()
//                for (doc in snap.documents) {
//                    val table = doc.toObject(WaterTestTable::class.java)
//                    if (table != null) tables.add(table)
//                }
//                adapter.notifyDataSetChanged()
//            }
//    }
//
//    private fun openTableDialog() {
//        val dialogBinding = DialogTableInputBinding.inflate(layoutInflater)
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("Add Water Test Table")
//            .setView(dialogBinding.root)
//            .setPositiveButton("Save", null)
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.setOnShowListener {
//            dialogBinding.btnAddRow.setOnClickListener {
//                addNewRow(dialogBinding)
//            }
//
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//                saveTable(dialogBinding, dialog)
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun addNewRow(binding: DialogTableInputBinding) {
//        val rowView = LayoutInflater.from(this)
//            .inflate(R.layout.item_parameter_row, binding.containerRows, false)
//
//        binding.containerRows.addView(rowView)
//    }
//
//    private fun saveTable(binding: DialogTableInputBinding, dialog: AlertDialog) {
//        val rows = mutableListOf<WaterParameter>()
//
//        for (i in 0 until binding.containerRows.childCount) {
//            val row = binding.containerRows.getChildAt(i)
//            val p = row.findViewById<EditText>(R.id.etParam).text.toString().trim()
//            val s1 = row.findViewById<EditText>(R.id.etS1).text.toString().trim()
//            val s2 = row.findViewById<EditText>(R.id.etS2).text.toString().trim()
//            val epa = row.findViewById<EditText>(R.id.etEpa).text.toString().trim()
//
//            if (p.isNotEmpty()) rows.add(WaterParameter(p, s1, s2, epa))
//        }
//
//        if (rows.isEmpty()) {
//            Toast.makeText(this, "Add at least 1 row", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val table = WaterTestTable(System.currentTimeMillis(), rows)
//
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .add(table)
//            .addOnSuccessListener {
//                tables.add(0, table)
//                adapter.notifyDataSetChanged()
//                Toast.makeText(this, "Table Saved", Toast.LENGTH_SHORT).show()
//                dialog.dismiss()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error saving table", Toast.LENGTH_SHORT).show()
//            }
//    }
//}


//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.adapters.WaterTableAdapter
//import com.krishna.varunaapp.databinding.ActivityVillageBinding
//import com.krishna.varunaapp.databinding.DialogTableInputBinding
//import com.krishna.varunaapp.models.WaterParameter
//import com.krishna.varunaapp.models.WaterTestTable
//import com.krishna.varunaapp.R
//import android.view.View
//
//class VillageActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityVillageBinding
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    private lateinit var adapter: WaterTableAdapter
//    private val tables = mutableListOf<WaterTestTable>()
//
//    private var villageName = ""
//    private var userRole = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityVillageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        db = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        villageName = intent.getStringExtra("villageName") ?: "Unknown Village"
//        binding.tvVillageName.text = "Village: $villageName"
//
//        setupRecycler()
//        loadUserRole()
//        fetchTables()
//
//        binding.btnAddParameter.setOnClickListener { openTableDialog() }
//
//        setSupportActionBar(binding.toolbarVillage)
//        binding.toolbarVillage.setNavigationOnClickListener { finish() }
//
//    }
//
//    private fun setupRecycler() {
//        adapter = WaterTableAdapter(tables)
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        binding.recyclerView.adapter = adapter
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener {
//                userRole = it.getString("role") ?: "GeneralUser"
//                if (userRole == "GeneralUser") binding.btnAddParameter.visibility = View.GONE
//            }
//    }
//
//    private fun fetchTables() {
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { snap ->
//                tables.clear()
//                for (doc in snap.documents) {
//                    val table = doc.toObject(WaterTestTable::class.java)
//                    if (table != null) tables.add(table)
//                }
//                adapter.notifyDataSetChanged()
//            }
//    }
//
//    private fun openTableDialog() {
//        val dialogBinding = DialogTableInputBinding.inflate(layoutInflater)
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("Add Water Test Table")
//            .setView(dialogBinding.root)
//            .setPositiveButton("Save", null)
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.setOnShowListener {
//            dialogBinding.btnAddRow.setOnClickListener {
//                addNewRow(dialogBinding)
//            }
//
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//                saveTable(dialogBinding, dialog)
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun addNewRow(binding: DialogTableInputBinding) {
//        val rowView = LayoutInflater.from(this)
//            .inflate(R.layout.item_parameter_row, binding.containerRows, false)
//
//        binding.containerRows.addView(rowView)
//    }
//
//    private fun saveTable(binding: DialogTableInputBinding, dialog: AlertDialog) {
//        val rows = mutableListOf<WaterParameter>()
//
//        for (i in 0 until binding.containerRows.childCount) {
//            val row = binding.containerRows.getChildAt(i)
//            val p = row.findViewById<EditText>(R.id.etParam).text.toString().trim()
//            val s1 = row.findViewById<EditText>(R.id.etS1).text.toString().trim()
//            val s2 = row.findViewById<EditText>(R.id.etS2).text.toString().trim()
//            val epa = row.findViewById<EditText>(R.id.etEpa).text.toString().trim()
//
//            if (p.isNotEmpty()) rows.add(WaterParameter(p, s1, s2, epa))
//        }
//
//        if (rows.isEmpty()) {
//            Toast.makeText(this, "Add at least 1 row", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val table = WaterTestTable(System.currentTimeMillis(), rows)
//
//        db.collection("villages")
//            .document(villageName)
//            .collection("water_tables")
//            .add(table)
//            .addOnSuccessListener {
//                tables.add(0, table)
//                adapter.notifyDataSetChanged()
//                Toast.makeText(this, "Table Saved", Toast.LENGTH_SHORT).show()
//                dialog.dismiss()
//            }
//    }
//}
