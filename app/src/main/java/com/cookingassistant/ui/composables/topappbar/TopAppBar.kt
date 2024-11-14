package com.cookingassistant.ui.composables.topappbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cookingassistant.data.objects.SearchEngine
import com.cookingassistant.data.ShoppingProducts
import com.cookingassistant.ui.composables.ShoppingList.ShoppingList
import com.cookingassistant.ui.screens.FilterScreen.FilterScreen
import com.cookingassistant.ui.screens.FilterScreen.FilterScreenViewModel
import com.cookingassistant.ui.screens.editor.EditorScreen
import com.cookingassistant.ui.screens.editor.EditorScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun DrawerItemContent(text:String, icon : ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon,text, tint = MaterialTheme.colorScheme.secondary)
        Text(text = text, fontSize = 18.sp,
            modifier = Modifier.weight(1f)
                .padding(start = 15.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(topAppBarviewModel : TopAppBarViewModel,
              searchQuery: String = "",
              content: @Composable() () -> Unit
              ) {

    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val searchText by topAppBarviewModel.QuickSearchText.collectAsState()
    val searchResults by topAppBarviewModel.QuickSearchResults.collectAsState()
    val showResults by topAppBarviewModel.ShowSearchResults.collectAsState()
    val selectedTool by topAppBarviewModel.SelectedTool.collectAsState()
    val viewModel by remember {
        mutableStateOf(FilterScreenViewModel(topAppBarviewModel.getService(),topAppBarviewModel.navController, topAppBarviewModel.recipeListViewModel,
            topAppBarviewModel
        ))
    }

    topAppBarviewModel.onSearchTextChanged(searchQuery)

    //---------//
    //Left menu//
    //---------//
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 40.sp,
                        text = "TOOLS",
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }

                HorizontalDivider()
                NavigationDrawerItem(
                    label = {
                        DrawerItemContent("Shopping list",Icons.Outlined.ShoppingCart)
                    },
                    selected = false,
                    onClick = {
                        if(selectedTool == "ShoppingList") {
                            topAppBarviewModel.onDeselctTool()
                            scope.launch {
                                drawerState.apply {
                                    close()
                                }
                            }
                        } else {
                            topAppBarviewModel.onSelectTool("ShoppingList")
                            scope.launch {
                                drawerState.apply {
                                    close()
                                }
                            }
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { DrawerItemContent("Advanced search", Icons.AutoMirrored.Outlined.ManageSearch) },
                    selected = false,
                    onClick = {
                        if(selectedTool == "AdvancedSearch") {
                            topAppBarviewModel.onDeselctTool()
                            scope.launch {
                                drawerState.apply {
                                    close()
                                }
                            }
                        } else {
                            scope.launch {
                                drawerState.apply {
                                    close()
                                }
                            }
                            topAppBarviewModel.onSelectTool("AdvancedSearch")
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { DrawerItemContent("Create recipe", Icons.AutoMirrored.Outlined.Note) },
                    selected = false,
                    onClick = {
                        //Later change
                        topAppBarviewModel.onDeselctTool()
                        topAppBarviewModel.navController.navigate("editor")
                    }
                )
            }
        }
    ) {

        //--------//
        //Top menu//
        //--------//

        androidx.compose.material3.Scaffold(
            topBar = {
                HorizontalDivider(modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                )
                CenterAlignedTopAppBar(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                    ,
                    colors = TopAppBarColors(containerColor = Color.Transparent,
                        actionIconContentColor = TopAppBarDefaults.topAppBarColors().actionIconContentColor,
                        scrolledContainerColor = TopAppBarDefaults.topAppBarColors().scrolledContainerColor,
                        navigationIconContentColor = TopAppBarDefaults.topAppBarColors().navigationIconContentColor,
                        titleContentColor = TopAppBarDefaults.topAppBarColors().titleContentColor
                    ),
                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                    title = {
                        OutlinedTextField(
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
                            textStyle = TextStyle.Default.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondaryContainer),
                            singleLine = true,
                            value = searchText,
                            onValueChange = {topAppBarviewModel.onSearchTextChanged(it)},
                            placeholder = { Text("search") },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Search, contentDescription = "Search",
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                    topAppBarviewModel.onQuickSearch()
                                }
                            )
                        )

                    },
                    navigationIcon = {
                        IconButton(
                            onClick =
                            {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open()
                                    }
                                }
                            },
                        )
                        {
                            Icon(Icons.Filled.Menu, contentDescription = "Left side menu",tint= MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    },

                    actions = {
                        IconButton(onClick = { /* Handle action */ },

                        ) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                )
            },
            content = { padding ->
                padding
                Column {
                    Spacer(Modifier.fillMaxWidth().padding(top=60.dp))
                    when(selectedTool) {
                        "" -> {content()}
                        "ShoppingList" -> {
                            ShoppingProducts.loadProducts(LocalContext.current)
                            ShoppingList()
                        }
                        "AdvancedSearch" -> {
                            FilterScreen(viewModel)
                        }
                    }
                }
            },
        )
        if(showResults) {
            Box(Modifier.fillMaxWidth().padding(top = 75.dp).offset(y=10.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.68f)
                        .clip(RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp))
                        .background(MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.85f))
                        .align(Alignment.Center)
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    for (result in searchResults) {
                        Text(
                            text = result.string,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.clickable {
                                if(result.index == -1) return@clickable;
                                topAppBarviewModel.onSearchTextChanged(result.string)
                                topAppBarviewModel.onResultsHide()
                                topAppBarviewModel.onResultSubmited(SearchEngine.getIndex(result.index))
                            }
                                .padding(vertical = 5.dp, horizontal = 5.dp)
                        )
                        HorizontalDivider()
                    }
                    Row(Modifier.fillMaxWidth().clickable { topAppBarviewModel.onResultsHide() }
                        .background(MaterialTheme.colorScheme.inversePrimary)
                        .wrapContentWidth(Alignment.CenterHorizontally)

                    ) {
                        IconButton(onClick = { topAppBarviewModel.onResultsHide() }) {
                            Icon(
                                Icons.Filled.KeyboardArrowUp,
                                "Hide menu",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
