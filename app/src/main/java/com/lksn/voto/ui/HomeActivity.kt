package com.lksn.voto.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.lksn.voto.R
import com.lksn.voto.data.ApiClient
import com.lksn.voto.data.ApiResult
import com.lksn.voto.data.Category
import com.lksn.voto.data.apiCall
import com.lksn.voto.databinding.ActivityHomeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: CameraAdapter
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: Int? = null

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupList()
        setupFilters()
        loadCategories()
    }

    private fun setupHeader() {
        binding.btnSpin.setOnClickListener { startActivity(Intent(this, LuckySpinActivity::class.java)) }
        binding.btnCart.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }
        binding.btnHistory.setOnClickListener { startActivity(Intent(this, OrderHistoryActivity::class.java)) }
    }

    private fun setupList() {
        adapter = CameraAdapter { camera ->
            startActivity(
                Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_CAMERA_ID, camera.id)
            )
        }
        binding.rvCameras.layoutManager = GridLayoutManager(this, 2)
        binding.rvCameras.adapter = adapter
    }

    private fun setupFilters() {
        // The first entry clears the category filter.
        spinnerAdapter = ArrayAdapter(this, R.layout.item_spinner, mutableListOf(getString(R.string.all_categories)))
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spCategory.adapter = spinnerAdapter
        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newId = if (position == 0) null else categories.getOrNull(position - 1)?.id
                // Fires once at start-up as well, which performs the initial load.
                if (newId != selectedCategoryId || adapter.itemCount == 0) {
                    selectedCategoryId = newId
                    loadCameras()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.etSearch.doAfterTextChanged {
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(400) // debounce typing
                loadCameras()
            }
        }
        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchJob?.cancel()
                v.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                loadCameras()
                true
            } else false
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            when (val result = apiCall(this@HomeActivity) { ApiClient.api.getCategories() }) {
                is ApiResult.Success -> {
                    categories = result.data
                    spinnerAdapter.clear()
                    spinnerAdapter.add(getString(R.string.all_categories))
                    spinnerAdapter.addAll(categories.map { it.name })
                    spinnerAdapter.notifyDataSetChanged()
                }
                is ApiResult.Failure -> showFailure(result)
            }
        }
    }

    private fun loadCameras() {
        loadJob?.cancel() // a newer request always wins over an older, slower one
        val search = binding.etSearch.text.toString().trim().ifEmpty { null }
        val categoryId = selectedCategoryId

        loadJob = lifecycleScope.launch {
            when (val result = apiCall(this@HomeActivity) { ApiClient.api.getCameras(categoryId, search) }) {
                is ApiResult.Success -> {
                    adapter.submit(result.data)
                    binding.tvEmpty.text = getString(R.string.no_camera_found)
                    binding.tvEmpty.visibility = if (result.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is ApiResult.Failure -> {
                    adapter.submit(emptyList())
                    binding.tvEmpty.text = getString(R.string.err_load_failed)
                    binding.tvEmpty.visibility = View.VISIBLE
                    showFailure(result)
                }
            }
        }
    }
}