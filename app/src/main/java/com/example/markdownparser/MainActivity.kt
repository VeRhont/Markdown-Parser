package com.example.markdownparser

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.markdownparser.presentation.EditFragment
import com.example.markdownparser.presentation.LoadFragment
import com.example.markdownparser.presentation.MarkdownViewModel
import com.example.markdownparser.presentation.ReadFragment
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val viewModel: MarkdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: AppState) {
        val fragment: Fragment = when (state) {
            is AppState.LoadMode -> LoadFragment.newInstance()
            is AppState.EditMode -> EditFragment.newInstance(state.content)
            is AppState.ReadMode -> ReadFragment.newInstance(state.content)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
