package com.cloud.spider.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.cloud.spider.R

/**
 *
 * Created by cloud on 2024/2/5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterPage(onUpClick: () -> Unit = {}) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ConverterTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick)
        }
    ) { contentPadding ->
        ConverterPageScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterTopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                          modifier: Modifier = Modifier, onUpClick: () -> Unit) {
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.Convert))
    },
        modifier = modifier,
        navigationIcon = {
             IconButton(onClick = onUpClick) {
                 Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
             }
        },
        actions = {
                  IconButton(onClick = {

                  }) {
                      Icon(imageVector = Icons.Filled.Add, contentDescription = "Add converter")
                  }
        },
        scrollBehavior = scrollBehavior)
}

@Composable
fun ConverterPageScreen(modifier: Modifier = Modifier) {

}