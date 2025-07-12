package gaku.original.myapplication.ui.view.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.main.NotCategorizedViewModel

@Composable
fun NotCategorizedView(
    viewModel: NotCategorizedViewModel = hiltViewModel(),
    navController: NavController
) {
    val expenses = viewModel.notCategorizedExpenses.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopBarView("カテゴリー未割り当て")
        },

        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        /**
         * 新たに抽出したら通知したいな
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                "The App will never be completed.\n" +
                        " It will continue to grow as long as there is imagination left in the world."
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth(),
                    userScrollEnabled = true
                ) {
                    items(expenses.value) { expense ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("${expense.datetime}")
                            Text("${expense.amount}")
                        }
                    }
                }
            }
        }
    }
}