package com.cloud.creeper.ui.gists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.base.DataState
import com.cloud.creeper.repository.Gist
import com.cloud.creeper.repository.GistFile

/**
 *
 * Created by cloud on 2024/7/18.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GistsPage(viewModel: GistsViewModel = hiltViewModel(), onUpClick: () -> Unit, onResultSet: (gistFile: GistFile) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val gistListState = viewModel.fetchGistsState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick)
        }
    ) { contentPadding ->
        GistsScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()), gistListState.value) {
            onResultSet(it)
        }

    }

}

@Composable
fun GistsScreen(modifier: Modifier = Modifier, dataState: DataState<List<Gist>>, onItemClick: (gist: GistFile) -> Unit) {
    when {
        dataState.isLoading -> {

        }
        dataState.throwable != null -> {

        }
        dataState.error != null -> {

        }
        dataState.data != null -> {
            dataState.data.let { dataList ->
                LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dataList) {
                        GistItem(it, onItemClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun GistItem(gist: Gist, onItemClick: (gistFile: GistFile) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showSubItems by remember { mutableStateOf(false) }

    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable {
            showSubItems = !showSubItems
        }, verticalAlignment = Alignment.CenterVertically) {


        Column(modifier = Modifier.weight(1f)) {
            Text(text = gist.description, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 6.dp), maxLines = 1)
            Text(text = gist.url, modifier = Modifier.padding(start = 24.dp, bottom = 12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }


        IconButton(onClick = {
            menuExpanded = true
        }, modifier = Modifier.padding(end = 12.dp)) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Menu")

        }
    }

    AnimatedVisibility(visible = showSubItems) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
           gist.files.values.forEach {
               GistFileItem(it, onItemClick)
           }
        }
    }


    if (showEditDialog) {

    }

}

@Composable
private fun GistFileItem(gistFile: GistFile, onItemClick: (gistFile: GistFile) -> Unit) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable {
            onItemClick(gistFile)
        }, verticalAlignment = Alignment.CenterVertically) {


        Column(modifier = Modifier.weight(1f)) {
            Text(text = gistFile.fileName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 6.dp), maxLines = 1)
        }

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                      modifier: Modifier = Modifier, onUpClick: () -> Unit) {
    androidx.compose.material3.TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.Gists))
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onUpClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}