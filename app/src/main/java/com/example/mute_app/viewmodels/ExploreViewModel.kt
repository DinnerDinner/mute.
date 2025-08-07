package com.example.mute_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val color: Long,
    val isEnabled: Boolean = true
)

data class ExploreUiState(
    val isLoading: Boolean = false,
    val cards: List<ExploreCard> = emptyList(),
    val selectedCard: ExploreCard? = null,
    val error: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadExploreCards()
    }

    private fun loadExploreCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simulate loading delay for smooth animations
            kotlinx.coroutines.delay(500)

            val cards = listOf(
                ExploreCard(
                    id = "break",
                    title = "break.",
                    subtitle = "Digital Discipline Core",
                    description = "Your anchor feature — combines Strict Mode, Detox Sessions, App & Website Blocking, and digital wellness rituals.",
                    color = 0xFF00BCD4 // Cyan
                ),
                ExploreCard(
                    id = "doc",
                    title = "doc.",
                    subtitle = "Personal Medical & Mental Health Bot",
                    description = "Not just a therapy chatbot — it evolves into a wellness navigator for both emotional and physical health.",
                    color = 0xFF4CAF50 // Green
                ),
                ExploreCard(
                    id = "stats",
                    title = "stats.",
                    subtitle = "Personal Analytics & Emotional Pulse",
                    description = "Your app's self-awareness engine. Tracks mood swings, usage patterns, detox success rates, and emotional spikes.",
                    color = 0xFF9C27B0 // Purple
                ),
                ExploreCard(
                    id = "eat",
                    title = "eat.",
                    subtitle = "AI-Powered Smart Cooking Assistant",
                    description = "Not a basic recipe library — this is a real-time cooking experience that feels like a chef walking you through your own kitchen.",
                    color = 0xFFFF5722 // Deep Orange
                ),
                ExploreCard(
                    id = "fit",
                    title = "fit.",
                    subtitle = "Personalized Fitness Companion",
                    description = "A daily movement guide that prioritizes functional movement over hardcore regimens. Think mindful fitness, not punishment routines.",
                    color = 0xFF2196F3 // Blue
                ),
                ExploreCard(
                    id = "day",
                    title = "day.",
                    subtitle = "Lifestyle Routine & Micro Habits Manager",
                    description = "Your daily flow controller, keeping you on top of micro-routines that often get ignored.",
                    color = 0xFFFF9800 // Orange
                )
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                cards = cards
            )
        }
    }

    fun onCardSelected(card: ExploreCard) {
        _uiState.value = _uiState.value.copy(selectedCard = card)
        // TODO: Navigate to specific card's navigation graph
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedCard = null)
    }

    fun refreshCards() {
        loadExploreCards()
    }
}