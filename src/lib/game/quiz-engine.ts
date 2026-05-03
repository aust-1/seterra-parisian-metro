import { MAX_MISSES, scoreForCorrectGuess, scoreForReveal } from './scoring';
import type { MapPoint, MissMarker } from '$lib/types';

export type QuestionStatus = 'guessing' | 'correct' | 'revealed';
export type GameStatus = 'playing' | 'completed';
export type GuessOutcome = 'correct' | 'incorrect' | 'revealed' | 'ignored';

export type QuizQuestion = {
  stationId: string;
  status: QuestionStatus;
  missCount: number;
  missMarkers: MissMarker[];
  pointsAwarded: number;
};

export type QuizState = {
  stationQueue: string[];
  currentIndex: number;
  score: number;
  status: GameStatus;
  currentQuestion: QuizQuestion | null;
  completedStationIds: string[];
};

export type GuessResult = {
  state: QuizState;
  outcome: GuessOutcome;
  pointsAwarded: number;
};

type RandomSource = () => number;

export function createQuiz(stationIds: string[], random: RandomSource = Math.random): QuizState {
  const stationQueue = shuffle([...stationIds], random);

  return {
    stationQueue,
    currentIndex: 0,
    score: 0,
    status: stationQueue.length > 0 ? 'playing' : 'completed',
    currentQuestion:
      stationQueue.length > 0 ? createQuestion(stationQueue[0]) : null,
    completedStationIds: []
  };
}

export function submitStationGuess(
  state: QuizState,
  stationId: string,
  point: MapPoint
): GuessResult {
  const question = state.currentQuestion;

  if (!question || state.status === 'completed' || question.status !== 'guessing') {
    return { state, outcome: 'ignored', pointsAwarded: 0 };
  }

  if (stationId === question.stationId) {
    const pointsAwarded = scoreForCorrectGuess(question.missCount);
    const nextQuestion = {
      ...question,
      status: 'correct' as const,
      pointsAwarded
    };

    return {
      state: {
        ...state,
        score: state.score + pointsAwarded,
        currentQuestion: nextQuestion
      },
      outcome: 'correct',
      pointsAwarded
    };
  }

  return registerMiss(state, point);
}

export function submitMapMiss(state: QuizState, point: MapPoint): GuessResult {
  const question = state.currentQuestion;

  if (!question || state.status === 'completed' || question.status !== 'guessing') {
    return { state, outcome: 'ignored', pointsAwarded: 0 };
  }

  return registerMiss(state, point);
}

export function goToNextQuestion(state: QuizState): QuizState {
  const question = state.currentQuestion;

  if (!question || question.status === 'guessing') {
    return state;
  }

  const completedStationIds = [...state.completedStationIds, question.stationId];
  const nextIndex = state.currentIndex + 1;
  const nextStationId = state.stationQueue[nextIndex];

  if (!nextStationId) {
    return {
      ...state,
      currentIndex: nextIndex,
      status: 'completed',
      currentQuestion: null,
      completedStationIds
    };
  }

  return {
    ...state,
    currentIndex: nextIndex,
    currentQuestion: createQuestion(nextStationId),
    completedStationIds
  };
}

export function restartQuiz(state: QuizState, random: RandomSource = Math.random): QuizState {
  return createQuiz(state.stationQueue, random);
}

function registerMiss(state: QuizState, point: MapPoint): GuessResult {
  const question = state.currentQuestion;

  if (!question) {
    return { state, outcome: 'ignored', pointsAwarded: 0 };
  }

  const missCount = question.missCount + 1;
  const missMarkers = [
    ...question.missMarkers,
    {
      ...point,
      id: `${question.stationId}-${missCount}`
    }
  ];
  const shouldReveal = missCount >= MAX_MISSES;
  const nextQuestion = {
    ...question,
    missCount,
    missMarkers,
    status: shouldReveal ? ('revealed' as const) : question.status,
    pointsAwarded: shouldReveal ? scoreForReveal() : question.pointsAwarded
  };

  return {
    state: {
      ...state,
      currentQuestion: nextQuestion
    },
    outcome: shouldReveal ? 'revealed' : 'incorrect',
    pointsAwarded: 0
  };
}

function createQuestion(stationId: string): QuizQuestion {
  return {
    stationId,
    status: 'guessing',
    missCount: 0,
    missMarkers: [],
    pointsAwarded: 0
  };
}

function shuffle<T>(items: T[], random: RandomSource): T[] {
  for (let index = items.length - 1; index > 0; index -= 1) {
    const nextIndex = Math.floor(random() * (index + 1));
    [items[index], items[nextIndex]] = [items[nextIndex], items[index]];
  }

  return items;
}
