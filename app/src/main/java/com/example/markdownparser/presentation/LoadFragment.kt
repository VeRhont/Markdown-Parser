package com.example.markdownparser.presentation

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.markdownparser.databinding.FragmentLoadBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoadFragment : Fragment() {
    private lateinit var binding: FragmentLoadBinding
    private val viewModel: MarkdownViewModel by activityViewModels()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadFileFromUri(requireContext().contentResolver, uri) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadFile.setOnClickListener {
            openFilePicker()
        }
        binding.fetchFile.setOnClickListener {
            if (viewModel.fileState.value !is FileResult.Loading) {
                downloadFromUrl()
            }
        }

        viewModel.fileState.onEach { result ->
            when (result) {
                is FileResult.Loading -> showLoading()
                is FileResult.Success -> hideLoading()
                is FileResult.Error -> showError(result.errorMessage)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        binding.root.removeAllViews()
        super.onDestroyView()
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("text/markdown"))
    }

    private fun downloadFromUrl() {
        val url = binding.enterUrl.text

        if (url.isNotBlank()) {
            viewModel.loadFileFromUrl(url.toString())
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        hideLoading()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = LoadFragment()
    }
}
