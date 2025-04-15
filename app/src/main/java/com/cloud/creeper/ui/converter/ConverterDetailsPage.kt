package com.cloud.creeper.ui.converter

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.ui.integration.AuthViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterDetailsPage(
    converterWithSources: ConverterWithSources,
    onUpClick: () -> Unit = {},
    viewModel: ConvertViewModel = hiltViewModel<ConvertViewModel, ConvertViewModel.ConvertViewModelFactory> { factory ->
        factory.create(converterWithSources)
    },
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val getAuthState = authViewModel.getTokenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ConverterDetailsTopAppBar(
                scrollBehavior = scrollBehavior,
                onUpClick = onUpClick)
        }
    ) { contentPadding ->

        ConverterDetailsScreen(
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            converterWithSources = converterWithSources,
            viewModel,
            getAuthState.value.data
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterDetailsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier, onUpClick: () -> Unit
) {

    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.Converter_Details))
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onUpClick) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        },
        actions = {
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterDetailsScreen(
    modifier: Modifier = Modifier,
    converterWithSources: ConverterWithSources,
    viewModel: ConvertViewModel,
    auth: ServiceAuth?
) {

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex,) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Text(text = stringResource(id = R.string.Statistics))
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Text(text = stringResource(id = R.string.Subscription_Source))
            }
        }
        if (selectedTabIndex == 0) {
            StatisticsTabContent(converterWithSources = converterWithSources)

        } else if (selectedTabIndex == 1) {
            SubscriptionSourceTabContent(converterWithSources = converterWithSources)
        }
    }

}

@Composable
private fun StatisticsTabContent(converterWithSources: ConverterWithSources) {
    val clipboardManager = LocalClipboardManager.current
    Column(modifier = Modifier.fillMaxWidth()) {

        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp)) {
            Text(text = stringResource(id = R.string.Address))
            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                val url = converterWithSources.converter.getLocalAddress()
                Text(text = stringResource(id = R.string.Local))
                Text(text = url, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)

                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(url))
                }) {
                    Text(text = stringResource(id = R.string.Copy))
                }
            }

            converterWithSources.cloudRepositoryList?.forEach {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    val url = it.url
                    Text(text = stringResource(id = R.string.Gists))
                    Text(text = url!!, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)

                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(url))
                    }) {
                        Text(text = stringResource(id = R.string.Copy))
                    }
                }
            }
        }

    }
}

@Composable
private fun SubscriptionSourceTabContent(converterWithSources: ConverterWithSources) {

}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}