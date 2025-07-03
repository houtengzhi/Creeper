package com.cloud.creeper.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.ui.integration.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(viewModel: SettingsViewModel = hiltViewModel(), onUpClick: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick)
        }
    ) { contentPadding ->
        SettingsScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()), viewModel)

    }

}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel) {

    Column(modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight()) {
        TextInputItem(title = stringResource(id = R.string.Http_Port), description = "", value = viewModel.getServerPort().toString(), onClick = { /*TODO*/ }, modifier = Modifier)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                      modifier: Modifier = Modifier, onUpClick: () -> Unit) {
    androidx.compose.material3.TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.Settings))
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
private fun TextInputItem(title: String, description: String?, value: String, onClick: () -> Unit, modifier: Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 24.dp, end = 24.dp)
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {

        Column {
            Text(text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp),
                textAlign = TextAlign.Center)

            if (!description.isNullOrBlank()) {
                Text(text = description,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 24.dp))
            }

        }

        Text(text = value,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 24.dp),
            textAlign = TextAlign.Center)


    }
}


@Composable
private fun SwitchButtonItem(@DrawableRes iconResId: Int, title: String, onClick: () -> Unit, modifier: Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 24.dp, top = 12.dp, end = 24.dp)
            .clickable(onClick = onClick)) {
        Icon(painter = painterResource(iconResId),
            contentDescription = "stopped icon",
            modifier = Modifier
                .padding(start = 24.dp, top = 12.dp, bottom = 12.dp)
                .size(24.dp, 24.dp),
            tint = LocalContentColor.current
        )

        Text(text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp),
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}