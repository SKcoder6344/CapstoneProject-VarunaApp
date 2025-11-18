    package com.krishna.varunaapp.fragments

    import android.app.Activity
    import android.content.Intent
    import android.net.Uri
    import android.graphics.Color
    import android.os.Bundle
    import android.view.*
    import android.widget.ArrayAdapter
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.github.mikephil.charting.charts.BarChart
    import com.github.mikephil.charting.charts.LineChart
    import com.github.mikephil.charting.components.Legend
    import com.github.mikephil.charting.components.XAxis
    import com.github.mikephil.charting.data.*
    import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FieldPath
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.Query
    import com.krishna.varunaapp.activities.VillageActivity
    import com.krishna.varunaapp.adapters.SimpleRowsAdapter
    import com.krishna.varunaapp.databinding.FragmentWaterQualityBinding
    import com.krishna.varunaapp.models.WaterParameter
    import com.krishna.varunaapp.models.WaterTestTable

        class WaterQualityFragment : Fragment() {

        private var _binding: FragmentWaterQualityBinding? = null
        private val binding get() = _binding!!

        private lateinit var auth: FirebaseAuth
        private lateinit var db: FirebaseFirestore

        private val csvPicker = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) parseCSV(uri)
            }
        }

        private val tables = mutableListOf<WaterTestTable>()
        private val villages = mutableListOf<String>()

        private var userRole = "GeneralUser"
        private var selectedVillage = ""

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentWaterQualityBinding.inflate(inflater, container, false)

            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            setupUi()
            loadVillages()
            loadUserRole()

            return binding.root
        }

        private fun setupUi() {
            binding.btnUploadCSV.setOnClickListener {
                if (userRole == "GeneralUser") {
                    Toast.makeText(requireContext(), "Only Asha/Health Workers can upload", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "text/*"
                csvPicker.launch(intent)
            }

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        private fun loadVillages() {
            db.collection("villages")
                .orderBy(FieldPath.documentId())
                .get()
                .addOnSuccessListener { snap ->
                    villages.clear()
                    for (doc in snap.documents) {
                        villages.add(doc.id)
                    }
                    if (villages.isEmpty()) villages.add("DefaultVillage")

                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, villages)
                    binding.spinnerVillage.adapter = adapter

                    selectedVillage = villages[0]
                    fetchTablesForVillage(selectedVillage)

                    binding.spinnerVillage.setOnItemSelectedListener(object :
                        android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, pos: Int, id: Long) {
                            selectedVillage = villages[pos]
                            fetchTablesForVillage(selectedVillage)
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
                    })
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load villages", Toast.LENGTH_SHORT).show()
                }
        }

        private fun loadUserRole() {
            val uid = auth.currentUser?.uid ?: return
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    userRole = it.getString("role") ?: "GeneralUser"
                    binding.btnUploadCSV.visibility = if (userRole == "GeneralUser") View.GONE else View.VISIBLE
                }
        }

        private fun fetchTablesForVillage(villageId: String) {
            db.collection("villages")
                .document(villageId)
                .collection("water_tables")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { snap ->
                    tables.clear()
                    for (doc in snap.documents) {
                        val table = doc.toObject(WaterTestTable::class.java)
                        if (table != null) tables.add(table)
                    }

                    if (tables.isEmpty()) {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.chartComparison.visibility = View.GONE
                        binding.chartTrend.visibility = View.GONE
                        binding.recyclerView.adapter = null
                        return@addOnSuccessListener
                    } else {
                        binding.tvNoData.visibility = View.GONE
                        binding.chartComparison.visibility = View.VISIBLE
                        binding.chartTrend.visibility = View.VISIBLE
                    }

                    val latestTable = tables.last()
                    val displayRows = latestTable.rows

                    binding.recyclerView.adapter = SimpleRowsAdapter(displayRows)

                    buildComparisonChart()
                    buildTrendChart()
                }
        }

        private fun parseCSV(uri: Uri) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val lines = inputStream?.bufferedReader()?.readLines() ?: return

                val rows = mutableListOf<WaterParameter>()

                for (i in 1 until lines.size) {
                    val row = lines[i].split(",")
                    if (row.size < 4) continue
                    rows.add(WaterParameter(row[0], row[1], row[2], row[3]))
                }

                if (rows.isEmpty()) {
                    Toast.makeText(requireContext(), "CSV contains no valid rows", Toast.LENGTH_SHORT).show()
                    return
                }

                val table = WaterTestTable(
                    createdAt = System.currentTimeMillis(),
                    rows = rows
                )

                db.collection("villages")
                    .document(selectedVillage)
                    .collection("water_tables")
                    .add(table)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "CSV Uploaded", Toast.LENGTH_SHORT).show()
                        fetchTablesForVillage(selectedVillage)
                    }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error reading CSV", Toast.LENGTH_SHORT).show()
            }
        }

        private fun buildComparisonChart() {
            if (tables.isEmpty()) return

            val latest = tables.last()
            val previous = if (tables.size > 1) tables[tables.size - 2] else null

            val labels = latest.rows.map { it.parameter }

            val latestEntries = ArrayList<BarEntry>()
            val prevEntries = ArrayList<BarEntry>()

            for (i in labels.indices) {
                val param = labels[i]

                val rowLatest = latest.rows.find { it.parameter == param }
                val rowPrev = previous?.rows?.find { it.parameter == param }

                latestEntries.add(BarEntry(i.toFloat(), averageSamples(rowLatest)))
                prevEntries.add(BarEntry(i.toFloat(), averageSamples(rowPrev)))
            }

            val setLatest = BarDataSet(latestEntries, "Latest")
            val setPrev = BarDataSet(prevEntries, "Previous")

            // Dynamic colors
            setLatest.color = Color.parseColor("#3F51B5")
            setPrev.color = Color.parseColor("#FFC107")

            val barData = BarData(setPrev, setLatest)
            barData.barWidth = 0.35f

            val chart = binding.chartComparison
            chart.data = barData

            val groupSpace = 0.25f
            val barSpace = 0.05f

            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelRotationAngle = -25f
            }

            chart.axisRight.isEnabled = false
            chart.description.isEnabled = false

            // Enable grouping of previous + latest
            chart.xAxis.axisMinimum = 0f
            val groupWidth = barData.getGroupWidth(groupSpace, barSpace)
            chart.xAxis.axisMaximum = groupWidth * labels.size
            chart.groupBars(0f, groupSpace, barSpace)

            chart.animateY(600)
            chart.invalidate()
        }

//        private fun buildComparisonChart() {
//            if (tables.isEmpty()) return
//
//            val latest = tables.last()
//            val previous = if (tables.size > 1) tables[tables.size - 2] else null
//
//            val labels = latest.rows.map { it.parameter }
//
//            val latestEntries = ArrayList<BarEntry>()
//            val prevEntries = ArrayList<BarEntry>()
//
//            for (i in labels.indices) {
//                val param = labels[i]
//
//                val rowLatest = latest.rows.find { it.parameter == param }
//                val rowPrev = previous?.rows?.find { it.parameter == param }
//
//                val avgLatest = averageSamples(rowLatest)
//                val avgPrev = averageSamples(rowPrev)
//
//                latestEntries.add(BarEntry(i.toFloat(), avgLatest))
//                prevEntries.add(BarEntry(i.toFloat(), avgPrev))
//            }
//
//            val setLatest = BarDataSet(latestEntries, "Latest")
//            val setPrev = BarDataSet(prevEntries, "Previous")
//
//            val barData = BarData(setPrev, setLatest)
//            barData.barWidth = 0.4f
//
//            val chart = binding.chartComparison
//            chart.data = barData
//
//            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
//            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//            chart.xAxis.labelRotationAngle = -20f
//            chart.axisRight.isEnabled = false
//            chart.description.isEnabled = false
//
//            chart.invalidate()
//        }


        private fun buildTrendChart() {
            if (tables.isEmpty()) return

            // Unique list of parameters across all tables
            val params = linkedSetOf<String>()
            tables.forEach { table ->
                table.rows.forEach { params.add(it.parameter) }
            }

            val dateLabels = tables.map {
                val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                sdf.format(java.util.Date(it.createdAt))
            }

            val lineData = LineData()

            var colorIndex = 0
            val colorPalette = listOf(
                Color.parseColor("#3F51B5"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#009688"),
                Color.parseColor("#9C27B0")
            )

            for (param in params) {
                val entries = ArrayList<Entry>()
                for (i in tables.indices) {
                    val row = tables[i].rows.find { it.parameter == param }
                    entries.add(Entry(i.toFloat(), averageSamples(row)))
                }

                val set = LineDataSet(entries, param)

                set.color = colorPalette[colorIndex % colorPalette.size]
                set.setCircleColor(set.color)
                set.lineWidth = 2.5f
                set.circleRadius = 4f
                set.valueTextSize = 8f

                lineData.addDataSet(set)
                colorIndex++
            }

            val chart = binding.chartTrend
            chart.data = lineData

            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
            }

            chart.axisRight.isEnabled = false
            chart.description.isEnabled = false

            chart.animateX(800)
            chart.invalidate()
        }


//        private fun buildTrendChart() {
//            if (tables.isEmpty()) return
//
//            val params = linkedSetOf<String>()
//            tables.forEach { t -> t.rows.forEach { params.add(it.parameter) } }
//
//            val labels = tables.map {
//                val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
//                sdf.format(java.util.Date(it.createdAt))
//            }
//
//            val lineData = LineData()
//
//            params.forEach { param ->
//                val entries = ArrayList<Entry>()
//
//                for (i in tables.indices) {
//                    val row = tables[i].rows.find { it.parameter == param }
//                    entries.add(Entry(i.toFloat(), averageSamples(row)))
//                }
//
//                val set = LineDataSet(entries, param)
//                set.lineWidth = 2f
//                set.circleRadius = 3f
//                lineData.addDataSet(set)
//            }
//
//            val chart = binding.chartTrend
//            chart.data = lineData
//            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
//            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//            chart.axisRight.isEnabled = false
//            chart.description.isEnabled = false
//
//            chart.invalidate()
//        }

        private fun averageSamples(row: WaterParameter?): Float {
            if (row == null) return 0f
            val s1 = row.sample1.toFloatOrNull()
            val s2 = row.sample2.toFloatOrNull()
            return when {
                s1 != null && s2 != null -> (s1 + s2) / 2f
                s1 != null -> s1
                s2 != null -> s2
                else -> 0f
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
