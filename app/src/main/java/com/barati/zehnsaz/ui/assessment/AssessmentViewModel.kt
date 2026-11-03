package com.barati.zehnsaz.ui.assessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barati.zehnsaz.data.local.dao.AssessmentDao
import com.barati.zehnsaz.data.local.entities.AssessmentMeasureEntity
import com.barati.zehnsaz.domain.assessment.AntCue
import com.barati.zehnsaz.domain.assessment.AntFlanker
import com.barati.zehnsaz.domain.assessment.AntTrial
import com.barati.zehnsaz.domain.assessment.AssessmentMeasures
import com.barati.zehnsaz.domain.assessment.AssessmentScorer
import com.barati.zehnsaz.domain.assessment.AssessmentTask
import com.barati.zehnsaz.domain.assessment.GradCptTrial
import com.barati.zehnsaz.domain.assessment.NBackTrial
import com.barati.zehnsaz.ui.common.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/** What the assessment screen shows at any moment. */
sealed interface AssessmentUiState {
    data object Menu : AssessmentUiState
    /** The instructions for [task], shown before anything is timed. */
    data class Briefing(val task: AssessmentTask) : AssessmentUiState
    data class Running(val task: AssessmentTask, val stimulus: Stimulus, val progress: Progress) : AssessmentUiState
    data class Finished(val task: AssessmentTask, val measures: Map<String, Double>) : AssessmentUiState
}

/** What is on screen for one moment. Empty parts are simply not drawn. */
data class Stimulus(val top: String = "", val middle: String = "", val bottom: String = "")

/** How far the task has got. [remainingMillis] is the time left at the planned pace. */
data class Progress(val completed: Int, val total: Int, val remainingMillis: Long)

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val assessmentDao: AssessmentDao,
    private val soundManager: SoundManager
) : ViewModel() {

    /** When each task was last taken. Null means it never was. */
    val lastTaken: StateFlow<Map<AssessmentTask, Long?>> = combine(
        AssessmentTask.entries.map { task ->
            assessmentDao.getLastTakenAt(PLAYER_ID, task.name).map { task to it }
        }
    ) { results -> results.associate { it.first to it.second } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _state = MutableStateFlow<AssessmentUiState>(AssessmentUiState.Menu)
    val state: StateFlow<AssessmentUiState> = _state.asStateFlow()

    private var job: Job? = null

    /** A response is accepted only while the response window of the current trial is open. */
    private var windowOpen = false
    private var pendingResponse: Pair<String, Long>? = null

    private var completedTrials = 0
    private var totalTrials = 0

    private data class Response(val key: String?, val reactionTimeMillis: Long?)

    /** Opens the instructions for [task]. Nothing is timed until the player starts. */
    fun prepare(task: AssessmentTask) {
        if (job?.isActive == true) return
        _state.value = AssessmentUiState.Briefing(task)
    }

    fun start(task: AssessmentTask) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            completedTrials = 0
            totalTrials = trialCount(task)
            countdown(task)
            val measures = when (task) {
                AssessmentTask.GRAD_CPT -> AssessmentMeasures.of(AssessmentScorer.gradCpt(runGradCpt()))
                AssessmentTask.ANT -> AssessmentMeasures.of(AssessmentScorer.ant(runAnt()))
                AssessmentTask.N_BACK -> AssessmentMeasures.of(AssessmentScorer.nBack(runNBack()))
            }
            val takenAt = System.currentTimeMillis()
            assessmentDao.insertMeasures(
                measures.map { (name, value) ->
                    AssessmentMeasureEntity(
                        playerId = PLAYER_ID,
                        task = task.name,
                        takenAt = takenAt,
                        measure = name,
                        measureValue = value
                    )
                }
            )
            _state.value = AssessmentUiState.Finished(task, measures)
        }
    }

    fun backToMenu() {
        _state.value = AssessmentUiState.Menu
    }

    /** Called by the screen when the player presses a response key. */
    fun respond(key: String) {
        if (!windowOpen || pendingResponse != null) return
        pendingResponse = key to nowMillis()
    }

    /** Three beeps, one per second, so the player knows when the first stimulus comes. */
    private suspend fun countdown(task: AssessmentTask) {
        repeat(COUNTDOWN_SECONDS) { index ->
            soundManager.play(SoundManager.SoundEvent.COUNTDOWN_TICK)
            showStimulus(task, Stimulus(middle = (COUNTDOWN_SECONDS - index).toString()))
            delay(1_000L)
        }
        showStimulus(task, Stimulus())
    }

    private suspend fun runGradCpt(): List<GradCptTrial> {
        val noGoFlags = (List(GRAD_CPT_NO_GO_TRIALS) { true } + List(GRAD_CPT_TRIALS - GRAD_CPT_NO_GO_TRIALS) { false })
            .shuffled()
        val trials = mutableListOf<GradCptTrial>()
        for (isNoGo in noGoFlags) {
            val digit = if (isNoGo) GRAD_CPT_NO_GO_DIGIT else GRAD_CPT_DIGITS.random()
            val response = presentTrial(AssessmentTask.GRAD_CPT, Stimulus(middle = digit), GRAD_CPT_WINDOW_MILLIS)
            trials += GradCptTrial(
                isNoGo = isNoGo,
                responded = response.key != null,
                reactionTimeMillis = response.reactionTimeMillis
            )
        }
        return trials
    }

    private suspend fun runAnt(): List<AntTrial> {
        val trials = mutableListOf<AntTrial>()
        repeat(ANT_TRIALS) {
            val cue = AntCue.entries.random()
            val flanker = AntFlanker.entries.random()
            val pointsLeft = Random.nextBoolean()
            val onTop = Random.nextBoolean()

            val arrow = if (pointsLeft) "←" else "→"
            val opposite = if (pointsLeft) "→" else "←"
            val flankerSymbol = when (flanker) {
                AntFlanker.CONGRUENT -> arrow
                AntFlanker.INCONGRUENT -> opposite
                AntFlanker.NEUTRAL -> "-"
            }
            val row = listOf(flankerSymbol, flankerSymbol, arrow, flankerSymbol, flankerSymbol).joinToString(" ")
            val target = if (onTop) Stimulus(top = row) else Stimulus(bottom = row)

            val cueStimulus = when (cue) {
                AntCue.NONE -> Stimulus()
                AntCue.DOUBLE -> Stimulus(top = "•", bottom = "•")
                AntCue.CENTER -> Stimulus(middle = "+")
                AntCue.SPATIAL -> if (onTop) Stimulus(top = "▲") else Stimulus(bottom = "▲")
            }
            showStimulus(AssessmentTask.ANT, cueStimulus)
            delay(ANT_CUE_MILLIS)
            showStimulus(AssessmentTask.ANT, Stimulus())
            delay(ANT_CUE_TO_TARGET_MILLIS)

            val response = presentTrial(AssessmentTask.ANT, target, ANT_WINDOW_MILLIS)
            val expectedKey = if (pointsLeft) KEY_LEFT else KEY_RIGHT
            trials += AntTrial(
                cue = cue,
                flanker = flanker,
                correct = response.key == expectedKey,
                reactionTimeMillis = response.reactionTimeMillis ?: ANT_WINDOW_MILLIS
            )
        }
        return trials
    }

    private suspend fun runNBack(): List<NBackTrial> {
        val letters = generateNBackLetters()
        val trials = mutableListOf<NBackTrial>()
        letters.forEachIndexed { index, letter ->
            val response = presentTrial(AssessmentTask.N_BACK, Stimulus(middle = letter.toString()), N_BACK_WINDOW_MILLIS)
            // The first two letters have nothing two steps back to compare with, so they are shown but not scored.
            if (index >= 2) {
                trials += NBackTrial(isTarget = letter == letters[index - 2], respondedMatch = response.key != null)
            }
        }
        return trials
    }

    /** About a third of the letters match the one two steps back. */
    private fun generateNBackLetters(): List<Char> {
        val letters = mutableListOf(N_BACK_LETTERS.random(), N_BACK_LETTERS.random())
        while (letters.size < N_BACK_TRIALS) {
            val index = letters.size
            val twoBack = letters[index - 2]
            val next = if (Random.nextDouble() < N_BACK_TARGET_RATE) {
                twoBack
            } else {
                N_BACK_LETTERS.filter { it != twoBack && it != letters[index - 1] }.random()
            }
            letters += next
        }
        return letters
    }

    private fun showStimulus(task: AssessmentTask, stimulus: Stimulus) {
        val left = (totalTrials - completedTrials).coerceAtLeast(0)
        val progress = Progress(
            completed = completedTrials,
            total = totalTrials,
            remainingMillis = left * trialDurationMillis(task)
        )
        _state.value = AssessmentUiState.Running(task, stimulus, progress)
    }

    /** Shows a stimulus for [windowMillis], accepts a response during that time, then leaves a short gap. */
    private suspend fun presentTrial(task: AssessmentTask, stimulus: Stimulus, windowMillis: Long): Response {
        pendingResponse = null
        showStimulus(task, stimulus)
        soundManager.play(SoundManager.SoundEvent.COUNTDOWN_TICK)
        val shownAt = nowMillis()
        windowOpen = true
        delay(windowMillis)
        windowOpen = false
        val given = pendingResponse
        completedTrials++
        showStimulus(task, Stimulus())
        delay(GAP_MILLIS)
        return Response(
            key = given?.first,
            reactionTimeMillis = given?.let { it.second - shownAt }
        )
    }

    private fun trialCount(task: AssessmentTask): Int = when (task) {
        AssessmentTask.GRAD_CPT -> GRAD_CPT_TRIALS
        AssessmentTask.ANT -> ANT_TRIALS
        AssessmentTask.N_BACK -> N_BACK_TRIALS
    }

    /** Planned length of one trial, including its gap, so the timer can count down. */
    private fun trialDurationMillis(task: AssessmentTask): Long = when (task) {
        AssessmentTask.GRAD_CPT -> GRAD_CPT_WINDOW_MILLIS + GAP_MILLIS
        AssessmentTask.ANT -> ANT_CUE_MILLIS + ANT_CUE_TO_TARGET_MILLIS + ANT_WINDOW_MILLIS + GAP_MILLIS
        AssessmentTask.N_BACK -> N_BACK_WINDOW_MILLIS + GAP_MILLIS
    }

    private fun nowMillis(): Long = System.nanoTime() / 1_000_000L

    companion object {
        const val KEY_LEFT = "left"
        const val KEY_RIGHT = "right"
        const val KEY_TAP = "tap"
        const val KEY_MATCH = "match"

        private const val PLAYER_ID = "default_player"
        private const val GAP_MILLIS = 500L
        private const val COUNTDOWN_SECONDS = 3

        private const val GRAD_CPT_TRIALS = 100
        private const val GRAD_CPT_NO_GO_TRIALS = 11
        private const val GRAD_CPT_NO_GO_DIGIT = "3"
        private val GRAD_CPT_DIGITS = listOf("1", "2", "4", "5", "6", "7", "8", "9")
        private const val GRAD_CPT_WINDOW_MILLIS = 800L

        private const val ANT_TRIALS = 48
        private const val ANT_CUE_MILLIS = 150L
        private const val ANT_CUE_TO_TARGET_MILLIS = 400L
        private const val ANT_WINDOW_MILLIS = 1700L

        private const val N_BACK_TRIALS = 32
        private const val N_BACK_WINDOW_MILLIS = 1500L
        private const val N_BACK_TARGET_RATE = 0.3
        private const val N_BACK_LETTERS = "BCDFGHKLMNPRSTVWXZ"
    }
}
