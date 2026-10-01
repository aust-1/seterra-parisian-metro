package com.parismetro.quiz.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.ui.theme.CorrectGreen
import com.parismetro.quiz.ui.theme.IncorrectRed

/** Score + "station N / total" row shown above every game mode's content. */
@Composable
fun GameHeader(score: Int, progress: Pair<Int, Int>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Station ${progress.first} / ${progress.second}", style = MaterialTheme.typography.bodyMedium)
        Text("Score : $score", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

/**
 * Shown once a question is resolved - a passive color flash, Seterra-style: no button, the
 * [GameViewModel] moves on by itself shortly after (see `scheduleAutoAdvance`).
 */
@Composable
fun ResolutionBanner(
    status: QuestionStatus,
    correctAnswerName: String,
    modifier: Modifier = Modifier
) {
    val isCorrect = status == QuestionStatus.CORRECT
    Surface(
        color = (if (isCorrect) CorrectGreen else IncorrectRed).copy(alpha = 0.15f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(
                text = if (isCorrect) "Bravo !" else "Raté - c'était $correctAnswerName",
                color = if (isCorrect) CorrectGreen else IncorrectRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
