package com.example.android.unscramble.ui.game

import android.text.Spannable
import android.text.SpannableString
import android.text.style.TtsSpan
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

/**
 *  ViewModel responsible for holding and processing all the data needed for the UI
 */
class GameViewModel : ViewModel() {

    private val _score = MutableLiveData(0)
    val score: LiveData<Int>
        get() = _score

    private val _currentWordCount = MutableLiveData(0)
    val currentWordCount: LiveData<Int>
        get() = _currentWordCount

    private val _currentScrambledWord = MutableLiveData<String>()

    //為了使talkback功能可以正常朗讀，改寫String type為 Spannable string
    val currentScrambledWord: LiveData<Spannable> = Transformations.map(_currentScrambledWord) {
        if (it == null) {
            SpannableString("")
        } else {
            val scrambledWord = it.toString()
            val spannable: Spannable = SpannableString(scrambledWord)
            spannable.setSpan(
                TtsSpan.VerbatimBuilder(scrambledWord).build(),
                0,
                scrambledWord.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            spannable
        }
    }


    private var wordsList: MutableList<String> = mutableListOf()
    private lateinit var currentWord: String


    //initializer block (init {}) as a place for initial setup code needed during the initialization of an object instance.
    init {
        Log.d("GameFragment", "GameViewModel is created!")
        //app遊戲開始先抓一單字
        getNextWord()
    }

    private fun getNextWord() {
        currentWord = allWordsList.random()
        val tempWord = currentWord.toCharArray()  //把string拆解成char陣列
        tempWord.shuffle()  //洗陣列順續,目前tempWord狀態仍是char陣列

        //確保洗完的順序的字串不跟原始字串一樣
        while (String(tempWord).equals(currentWord, false)) {
            tempWord.shuffle()
        }

        //確保新抓出的currentWord沒有在遊戲中10場的wordsList出現過;如有,重做一次getNextWord()；如沒有，更新亂序單字&將其加入wordsList清單
        if (wordsList.contains(currentWord)) {
            getNextWord()
        } else {
            _currentScrambledWord.value = String(tempWord)
            _currentWordCount.value = (_currentWordCount.value)?.inc()
            /* inc() Kotlin function to increment the value by one*/
            wordsList.add(currentWord)
        }
    }


    //判斷目前遊戲進行到第幾回合，如回合未滿，則繼續遊戲&回傳true；如回合已滿則=回傳false
    fun nextWord(): Boolean{
        return if (_currentWordCount.value!! < MAX_NO_OF_WORDS){
            getNextWord()
            true
        }else false
    }

    //加分流程
    private fun increaseScore(){
        _score.value =(_score.value)?.plus(SCORE_INCREASE)
    }

    fun isUserWordCorrect(playerWord:String):Boolean{
        if (playerWord.equals(currentWord,true)){
            increaseScore()
            return true
        }else{
            return false
        }
    }


    fun reinitializeData(){
        _score.value = 0
        _currentWordCount.value = 0
        wordsList.clear()
        getNextWord()
    }


    override fun onCleared() {
        super.onCleared()
        Log.d("GameFragment", "GameViewModel is destroyed!")
    }


}