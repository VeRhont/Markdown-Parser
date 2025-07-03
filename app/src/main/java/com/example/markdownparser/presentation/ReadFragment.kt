package com.example.markdownparser.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.markdownparser.databinding.FragmentReadBinding
import com.example.markdownparser.parser.MarkdownParser
import com.example.markdownparser.renderer.MarkdownRenderer

class ReadFragment : Fragment() {
    private var _binding: FragmentReadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MarkdownViewModel by activityViewModels()
    private lateinit var markdownRenderer: MarkdownRenderer

    companion object {
        private const val ARG_CONTENT = "content"

        fun newInstance(content: String) = ReadFragment().apply {
            arguments = Bundle().apply { putString(ARG_CONTENT, content) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        markdownRenderer = MarkdownRenderer(requireContext())

        arguments?.getString(ARG_CONTENT)?.let { content ->
            renderMarkdown(content)
        }

        binding.editMarkdown.setOnClickListener {
            viewModel.enterEditMode()
        }
    }

    private fun renderMarkdown(content: String) {
        val elements = MarkdownParser.parse(content)
        markdownRenderer.render(elements, binding.markdownContainer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}