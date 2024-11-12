package com.cookingassistant.ui.composables.topappbar

import com.cookingassistant.data.Models.Result
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.cookingassistant.data.objects.ScreenControlManager
import com.cookingassistant.data.objects.SearchEngine
import com.cookingassistant.services.RecipeService
import com.cookingassistant.ui.screens.recipescreen.RecipeScreenViewModel
import com.frosch2010.fuzzywuzzy_kotlin.model.ExtractedResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TopAppBarViewModel(private val _service : RecipeService, private val _recipeScreenViewModel : RecipeScreenViewModel, private val _navController: NavHostController) : ViewModel() {
    private val _quickSearchText = MutableStateFlow("")
    private val _quickSearchResults = MutableStateFlow(listOf<ExtractedResult>())
    private val _showSearchResults = MutableStateFlow(false)
    private val _selectedTool = MutableStateFlow("")

    val QuickSearchText : StateFlow<String> = _quickSearchText
    val QuickSearchResults : StateFlow<List<ExtractedResult>> = _quickSearchResults
    val ShowSearchResults : StateFlow<Boolean> = _showSearchResults
    val SelectedTool: StateFlow<String> = _selectedTool

    init {
        updateLists()
    }

    fun updateLists() {
        _quickSearchResults.value = listOf<ExtractedResult>(ExtractedResult("Propositions are loading...",0,-1))
        viewModelScope.launch {
            try {
                val result = _service.getRecipeNames()
                if(result is Result.Success) {
                    if(result.data != null)
                        SearchEngine.updateRecipesList(result.data)
                    else
                        Log.e("TopAppBarViewModel", "_service.getRecipeNames().body is empty", )
                }
                else if (result is Result.Error){
                    Log.e("TopAppBarViewModel", "Result is error")
                }

                else{
                    Log.e("TopAppBarViewModel", "Unexpected error occurred getRecipeNames()")
                }
            } catch (e: Exception) {
                Log.e("TopAppBarViewModel", e.message ?: "couldn't get message names", )
            }
        }
        viewModelScope.launch {
            val tag = "TopAppBarViewModel"
            try {
                val result = _service.getAllIngredientsList()
                when(result) {
                    is Result.Success -> {
                        if(result.data != null)
                            SearchEngine.updateIngredientsList(result.data)
                        else {
                            Log.w(tag, "_service.getAllIngredientsList().body is empty", )
                        }
                    }
                    is Result.Error -> {
                        Log.e(tag, result.message)
                    }
                    else -> {
                        Log.e(tag, "Unexpected error occurred ${tag}")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, e.message ?: "couldn't get ingredients", )
            }
        }
    }

    fun onSearchTextChanged(newText : String) {
        if(newText.trim() != "" && newText.length > _quickSearchText.value.length) {
            _proposeResults(newText)
            _showSearchResults.value = true
        }
        else if (newText.trim() == "") {
            _showSearchResults.value = false
        }
        _quickSearchText.value = newText
    }

    fun onResultsHide() {
        _showSearchResults.value = false;
    }

    fun onResultSubmited(id : Int) {
        viewModelScope.launch {
            try {
                val result = _service.getRecipeDetails(id)
                if(result is Result.Success) {
                    if(result.data != null) {
                        _recipeScreenViewModel.loadRecipe(result.data)
                        if(_navController.currentDestination?.route == "recipeScreen") {
                            _navController.popBackStack()
                        }
                        _navController.navigate("recipeScreen")
                        onDeselctTool()
                    }
                }
                else if(result is Result.Error){
                    Log.e("onResultSubmitted", "Result is error in onResultSubmitted")
                }
                else{
                    Log.e("onResultSubmitted", "Unexpected error in onResultSubmitted")
                }

            } catch(e: Exception) {
                Log.e("onResultSubmitted", e.message ?: "failed to submit result")
            }
        }
    }

    private fun _proposeResults(newText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _quickSearchResults.value = SearchEngine.suggestRecipeNames(newText)
        }
    }

    fun onDeselctTool() {
        _selectedTool.value = ""
        ScreenControlManager.activeTool = ""
    }

    fun onSelectTool(tool : String) {
        ScreenControlManager.activeTool=tool
        _selectedTool.value = tool
    }

    fun onAppTryExit() : Boolean {
        if(_navController.currentDestination?.route == "home") {
            return true
        }
        return false
    }
}