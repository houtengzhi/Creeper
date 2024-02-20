package com.cloud.spider.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        var showBottomSheet by remember { mutableStateOf(false) }
        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {


            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterTopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                          modifier: Modifier = Modifier, onUpClick: () -> Unit) {
    var moreMenuExpanded by remember { mutableStateOf(false) }
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
                      moreMenuExpanded = true
                  }) {
                      Icon(imageVector = Icons.Filled.Add, contentDescription = "Add converter")
                  }
            MoreMenu(expanded = moreMenuExpanded, onDismissRequest = {
                moreMenuExpanded = false
            })
        },
        scrollBehavior = scrollBehavior)
}

@Composable
private fun MoreMenu(expanded: Boolean, onDismissRequest: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(text = {
            Text(text = "Merge Proxies")
        }, onClick = { })
        DropdownMenuItem(text = {
            Text(text = "About")
        }, onClick = { })
    }
}

@Composable
fun ConverterPageScreen(modifier: Modifier = Modifier) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeProxiesScreen() {

}