import { describe, expect, it } from 'vitest';
import {
  createQuiz,
  goToNextQuestion,
  submitMapMiss,
  submitStationGuess
} from './quiz-engine';

const fixedRandom = () => 0;

describe('quiz-engine', () => {
  it('creates a shuffled quiz and accepts the target station', () => {
    let quiz = createQuiz(['a', 'b', 'c'], fixedRandom);

    expect(quiz.currentQuestion?.stationId).toBe('b');

    const result = submitStationGuess(quiz, 'b', { x: 10, y: 20 });
    quiz = result.state;

    expect(result.outcome).toBe('correct');
    expect(result.pointsAwarded).toBe(3);
    expect(quiz.score).toBe(3);
    expect(quiz.currentQuestion?.status).toBe('correct');
  });

  it('reveals the answer after three misses', () => {
    let quiz = createQuiz(['station'], fixedRandom);

    quiz = submitMapMiss(quiz, { x: 1, y: 1 }).state;
    quiz = submitStationGuess(quiz, 'wrong-station', { x: 2, y: 2 }).state;
    const result = submitMapMiss(quiz, { x: 3, y: 3 });

    expect(result.outcome).toBe('revealed');
    expect(result.state.currentQuestion?.status).toBe('revealed');
    expect(result.state.currentQuestion?.missCount).toBe(3);
    expect(result.state.currentQuestion?.missMarkers).toHaveLength(3);
    expect(result.state.score).toBe(0);
  });

  it('moves to the next station only after a resolved question', () => {
    let quiz = createQuiz(['a', 'b'], fixedRandom);
    const firstStation = quiz.currentQuestion?.stationId;

    expect(goToNextQuestion(quiz).currentQuestion?.stationId).toBe(firstStation);

    quiz = submitStationGuess(quiz, firstStation ?? '', { x: 0, y: 0 }).state;
    quiz = goToNextQuestion(quiz);

    expect(quiz.completedStationIds).toEqual([firstStation]);
    expect(quiz.status).toBe('playing');
    expect(quiz.currentQuestion?.stationId).not.toBe(firstStation);
  });
});
