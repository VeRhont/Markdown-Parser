package com.example.markdownparser.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.markdownparser.databinding.FragmentEditBinding

class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MarkdownViewModel by activityViewModels()

    companion object {
        private const val ARG_CONTENT = "content"

        fun newInstance(content: String) = EditFragment().apply {
            arguments = Bundle().apply { putString(ARG_CONTENT, content) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ARG_CONTENT)?.let { content ->
            binding.editText.setText(content)
        }

        binding.saveButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val newContent = binding.editText.text.toString()
        viewModel.saveContent(newContent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}