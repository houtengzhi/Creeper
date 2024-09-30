package com.cloud.creeper.ui.converter

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.base.DataState
import com.cloud.creeper.repository.entity.Converter
import com.cloud.creeper.repository.entity.ConverterWithSources

/**
 *
 * Created by cloud on 2024/2/5.
 */
private const val TAG = "ConverterManagePage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterManagePage(viewModel: ConvertViewModel = hiltViewModel(), onUpClick: () -> Unit = {}, onNewClick: () -> Unit, onEditClick: (converter : ConverterWithSources) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState = viewModel.subscribeConverterListState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ConverterManageTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick, onNewClick = onNewClick)
        }
    ) { contentPadding ->
        ConverterPageScreen(uiState.value, modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            onEditClick = onEditClick,
            onDeleteClick = {
                viewModel.deleteConverter(it)
            })

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterManageTopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                                     modifier: Modifier = Modifier, onUpClick: () -> Unit, onNewClick: () -> Unit) {
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.Converter_Manage))
    },
        modifier = modifier,
        navigationIcon = {
             IconButton(onClick = onUpClick) {
                 Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
             }
        },
        actions = {
            IconButton(onClick = {
                      onNewClick()
                  }) {
                      Icon(imageVector = Icons.Filled.Add, contentDescription = "Add converter")
                  }
        },
        scrollBehavior = scrollBehavior)
}

@Composable
private fun MoreMenu(expanded: Boolean, onDismissRequest: () -> Unit, onNewClick: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Add_Converter))
        }, onClick = {
            onDismissRequest()
            onNewClick()
        } )
        DropdownMenuItem(text = {
            Text(text = "About")
        }, onClick = {
            onDismissRequest()
        })
    }
}

@Composable
fun ConverterPageScreen(dataState: DataState<List<ConverterWithSources>>, modifier: Modifier = Modifier,  onEditClick: (converter: ConverterWithSources) -> Unit,
                        onDeleteClick: (converter: ConverterWithSources) -> Unit) {
    when {
        dataState.isLoading -> {
        }

        dataState.throwable != null -> {

        }

        dataState.error != null -> {

        }

        dataState.data != null -> {
            dataState.data.let { dataList ->
                Log.d(TAG, "converter list size = ${dataList.size}")
                LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dataList) {
                        ConverterItem(it, onEditClick, onDeleteClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConverterItem(converter: ConverterWithSources,
                                   onEditClick: (converter: ConverterWithSources) -> Unit,
                                   onDeleteClick: (converter: ConverterWithSources) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable {

        }, verticalAlignment = Alignment.CenterVertically) {

        Image(painter = painterResource(id = converter.converter.getClientIconResId()), contentDescription = "",
            modifier = Modifier.padding(start = 24.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = converter.converter.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 24.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }


        IconButton(onClick = {
            menuExpanded = true
        }, modifier = Modifier.padding(end = 12.dp)) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Menu")

            ConverterMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onDetailsClick = {
                                 showDetailsDialog = true
                },
                onEditClick = { onEditClick(converter) },
                onDeleteClick = { showDeleteDialog = true })
        }
    }

    if (showDeleteDialog) {
        DeleteConverterDialog(
            converter = converter,
            onDismissRequest = { showDeleteDialog = false },
            onDeleteClick = {
                onDeleteClick(it)
            }
        )
    }
    if (showDetailsDialog) {
        ConverterDetailsDialog(converter = converter, onDismissRequest = {
            showDetailsDialog = false
        })
    }
}

@Composable
private fun ConverterMenu(expanded: Boolean, onDismissRequest: () -> Unit, onDetailsClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = Modifier) {
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Details))
        }, onClick = {
            onDismissRequest()
            onDetailsClick()
        } )
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Edit))
        }, onClick = {
            onDismissRequest()
            onEditClick()
        } )
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Delete))
        }, onClick = {
            onDismissRequest()
            onDeleteClick()
        })
    }
}

@Composable
private fun DeleteConverterDialog(converter: ConverterWithSources, onDismissRequest: () -> Unit, onDeleteClick: (converter: ConverterWithSources) -> Unit) {
    AlertDialog(onDismissRequest = onDismissRequest, modifier = Modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onDeleteClick(converter)

            }) {
                Text(text = stringResource(id = R.string.OK))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.Cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.Delete_converter))
        },
        text = {
            Text(text = stringResource(id = R.string.You_wont_be_able_to_recover_it_once_confirmed))
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Composable
private fun ConverterDetailsDialog(converter: ConverterWithSources, onDismissRequest: () -> Unit) {
    Log.d(TAG, "showConverterDetailsDialog(), ${converter.converter}")
    AlertDialog(onDismissRequest = onDismissRequest, modifier = Modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()

            }) {
                Text(text = stringResource(id = R.string.Dismiss))
            }
        },
        title = {
            Text(text = converter.converter.name)
        },
        text = {
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            val clipboardManager = LocalClipboardManager.current
            Column(modifier = Modifier.fillMaxWidth()) {
                ScrollableTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth(), edgePadding = 0.dp) {
                    Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                        Text(text = stringResource(id = R.string.Local_Address))
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically) {
                    val url = converter.converter.getLocalAddress()
                    Text(text = url, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)

                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(url))
                    }) {
                        Text(text = stringResource(id = R.string.Copy))
                    }
                }

            }
            
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Preview
@Composable
fun PreviewDialog() {

    val converter = Converter("", "Test")
    val converterWithSources = ConverterWithSources(converter, emptyList(), emptyList())
    ConverterDetailsDialog(converter = converterWithSources) {

    }
}