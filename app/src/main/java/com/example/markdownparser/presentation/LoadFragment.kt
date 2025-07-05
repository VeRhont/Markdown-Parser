package com.example.markdownparser.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.markdownparser.databinding.FragmentLoadBinding

class LoadFragment : Fragment() {
    private lateinit var binding: FragmentLoadBinding
    private val viewModel: MarkdownViewModel by activityViewModels()

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

        binding.loadFile.setOnClickListener { openFilePicker() }
        binding.fetchFile.setOnClickListener { downloadFromUrl() }
    }

    override fun onDestroyView() {
        binding.root.removeAllViews()
        super.onDestroyView()
    }

    private fun openFilePicker() {
        handleContentLoaded(testContent)
    }

    private fun downloadFromUrl() {
        handleContentLoaded(testContent)
    }

    private fun handleContentLoaded(content: String) {
        viewModel.loadMarkdownFile(content)
    }

    companion object {
        fun newInstance() = LoadFragment()

        private val testContent = """
# Заголовок 1
## Заголовок 2
Обычный текст
**Жирный текст**
*Курсивный текст*
~~Зачеркнутый текст~~
~~**_Жирный текст_**~~

| Header 1 | Header 2 |
|----------|----------|
| Ячейка 1 | Ячейка 2 |
| Ячейка 3 | Ячейка 4 |

![Описание картинки](https://i.pinimg.com/originals/b2/dc/9c/b2dc9c2cee44e45672ad6e3994563ac2.jpg)

# Заголовок 1
## Заголовок 2
Обычный текст
**Жирный текст**
*Курсивный текст*
~~Зачеркнутый текст~~
~~**_Жирный текст_**~~

# Заголовок 1
## Заголовок 2
Обычный текст
**Жирный текст**
*Курсивный текст*
~~Зачеркнутый текст~~
~~**_Жирный текст_**~~

""".trimIndent()
    }
}
