/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.unscramble.ui.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.unscramble.R
import com.example.android.unscramble.databinding.GameFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


/**
 *  Fragment responsible for drawing views and data to the screen
 */
class GameFragment : Fragment() {

    //使viewModel跟UI controller(GameFragment)綁定
    private val viewModel: GameViewModel by viewModels()

    // Binding object instance with access to the views in the game_fragment.xml layout
    private lateinit var binding: GameFragmentBinding

    // Create a ViewModel the first time the fragment is created.
    // If the fragment is re-created, it receives the same GameViewModel instance created by the
    // first fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout XML file and return a binding object instance
        binding = GameFragmentBinding.inflate(inflater, container, false)

        Log.d("GameFragment", "GameFragment created/re-created!")

        Log.d("GameFragment","Word: ${viewModel.currentScrambledWord}"+
                " Score: ${viewModel.score} WordCount: ${viewModel.currentWordCount}")

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("GameFragment", "GameFragment destroyed!")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup a click listener for the Submit and Skip buttons.
        binding.submit.setOnClickListener { onSubmitWord() }
        binding.skip.setOnClickListener { onSkipWord() }
        // Update the UI
        updateNextWordOnScreen()
        binding.score.text = getString(R.string.score, 0)
        binding.wordCount.text = getString(
            R.string.word_count, 0, MAX_NO_OF_WORDS
        )
    }

    /*
    * Checks the user's word, and updates the score accordingly.
    * Displays the next scrambled word.
    */
    private fun onSubmitWord() {
        val playerWord = binding.textInputEditText.text.toString()

        //判斷玩家填入字串是否正確，如正確true->不彈出error訊息&往下判斷回合；如不正確false，彈出error訊息請玩家再次嘗試
        if(viewModel.isUserWordCorrect(playerWord)){
            setErrorTextField(false)

            //判斷回合，true->更新UI；false->彈出alertDialog
            if (viewModel.nextWord()){
                updateNextWordOnScreen()

            }else{
                showFinalScoreDialog()
            }
        }else{
            setErrorTextField(true)
        }
    }

    /*
     * Skips the current word without changing the score.
     * Increases the word count.
     */
    private fun onSkipWord() {
        if (viewModel.nextWord()){
            setErrorTextField(false)
            updateNextWordOnScreen()
        }else{
            showFinalScoreDialog()
        }
    }

/*
* Creates and shows an AlertDialogwith the final score.
* (Material Design Components library內建功能)
*/

    private fun showFinalScoreDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.congratulations))
            .setMessage(getString(R.string.you_scored,viewModel.score))
            .setCancelable(false) //讓user不能以倒退鍵取消此AlertDialog視窗
            .setNegativeButton(getString(R.string.exit)){_,_ ->
                exitGame()
            } // trailing lambda syntax.
            .setPositiveButton(getString(R.string.play_again)){_,_ ->
                restartGame()
            } // trailing lambda syntax.
            .show()
    }


    /*
     * Re-initializes the data in the ViewModel and updates the views with the new data, to
     * restart the game.
     */
    private fun restartGame() {
        viewModel.reinitializeData()
        setErrorTextField(false)
        updateNextWordOnScreen()
    }

    /*
     * Exits the game.
     */
    private fun exitGame() {
        activity?.finish()
    }

    /*
    * Sets and resets the text field error status.
    */
    private fun setErrorTextField(error: Boolean) {
        if (error) {
            binding.textField.isErrorEnabled = true
            binding.textField.error = getString(R.string.try_again)
        } else {
            binding.textField.isErrorEnabled = false
            binding.textInputEditText.text = null
        }
    }

    /*
     * Displays the next scrambled word on screen.
     */
    private fun updateNextWordOnScreen() {
        binding.textViewUnscrambledWord.text = viewModel.currentScrambledWord
    }
}