package com.example.markdownparser.presentation

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.markdownparser.R
import com.example.markdownparser.databinding.FragmentEditBinding

class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private val viewModel: MarkdownViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ARG_CONTENT)?.let { content ->
            binding.editText.setText(content)
        }

        binding.toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    saveContent()
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        binding.root.removeAllViews()
        super.onDestroyView()
    }

    private fun saveContent() {
        val newContent = binding.editText.text.toString()
        viewModel.saveContent(newContent)
    }

    private fun handleBackPressed() {
        val originalContent = arguments?.getString(ARG_CONTENT) ?: ""
        val currentContent = binding.editText.text.toString()

        if (originalContent != currentContent) {
            showUnsavedChangesDialog()
        } else {
            viewModel.enterReadMode()
        }
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Несохраненные изменения")
            .setMessage("Сохранить изменения?")
            .setPositiveButton("Сохранить") { _, _ ->
                saveContent()
            }
            .setNegativeButton("Не сохранять") { _, _ ->
                viewModel.enterReadMode()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    companion object {
        private const val ARG_CONTENT = "content"

        fun newInstance(content: String) = EditFragment().apply {
            arguments = Bundle().apply { putString(ARG_CONTENT, content) }
        }
    }
}
