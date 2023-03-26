package com.example.quizgame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.quizgame.data.QUIZZES
import com.example.quizgame.data.INCREASE
import com.example.quizgame.data.all_Quizzes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    // Game UI state
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")
        private set

    // Set of quiz used in the game
    private var usedWords: MutableSet<List<String>> = mutableSetOf()
    private lateinit var currentWord: List<String>

    init {
        resetGame()
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentQuiz = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord[1])) {
            val updatedScore = _uiState.value.score.plus(INCREASE)
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)

            }
            skipWord()
        }
        // Reset user guess
        updateUserGuess("")
    }


     //Skip to next question

    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }


    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == QUIZZES){
            //Last round in the game, update isGameOver to true, don't pick a new question
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else{
            // Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentQuiz = pickRandomWordAndShuffle(),
                    currentQuizCount = currentState.currentQuizCount.inc(),
                    score = updatedScore
                )
            }
        }
    }

    private fun shuffleCurrentWord(word: List<String>): List<String> {
        val tempquiz = word
        val tempWord = word[0].toCharArray()
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return tempquiz
    }

    private fun pickRandomWordAndShuffle(): List<String> {
        // Continue picking up a new random question until you get one that hasn't been used before
        currentWord = all_Quizzes.random()
        currentWord = all_Quizzes.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }
}