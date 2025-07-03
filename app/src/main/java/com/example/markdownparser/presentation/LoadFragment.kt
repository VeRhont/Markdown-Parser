package com.example.markdownparser.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.markdownparser.databinding.FragmentLoadBinding


class LoadFragment : Fragment() {
    private var _binding: FragmentLoadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MarkdownViewModel by activityViewModels()

    companion object {
        fun newInstance() = LoadFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadFile.setOnClickListener { openFilePicker() }
        binding.fetchFile.setOnClickListener { downloadFromUrl() }
    }

    private fun openFilePicker() {
        // ... (код выбора файла)
    }

    private fun downloadFromUrl() {
        // ... (код загрузки по URL)
    }

    private fun handleContentLoaded(content: String) {
        viewModel.loadMarkdownFile(content)
    }

    // ... остальная реализация загрузки
}