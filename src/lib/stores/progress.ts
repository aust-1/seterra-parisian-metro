import { browser } from '$app/environment';

const STORAGE_KEY = 'seterra-parisian-metro-progress-v1';

export type StationProgress = {
  seen: number;
  correct: number;
  revealed: number;
  misses: number;
};

export type LocalProgress = {
  bestScore: number;
  gamesPlayed: number;
  totalScore: number;
  stationStats: Record<string, StationProgress>;
  settings: {
    showStationNames: boolean;
  };
};

export type StationResolution = {
  stationId: string;
  correct: boolean;
  missCount: number;
};

export function createDefaultProgress(): LocalProgress {
  return {
    bestScore: 0,
    gamesPlayed: 0,
    totalScore: 0,
    stationStats: {},
    settings: {
      showStationNames: false
    }
  };
}

export function loadProgress(): LocalProgress {
  if (!browser) {
    return createDefaultProgress();
  }

  const rawProgress = localStorage.getItem(STORAGE_KEY);

  if (!rawProgress) {
    return createDefaultProgress();
  }

  try {
    return {
      ...createDefaultProgress(),
      ...JSON.parse(rawProgress)
    };
  } catch {
    return createDefaultProgress();
  }
}

export function saveProgress(progress: LocalProgress): void {
  if (!browser) {
    return;
  }

  localStorage.setItem(STORAGE_KEY, JSON.stringify(progress));
}

export function recordStationResolution(
  progress: LocalProgress,
  resolution: StationResolution
): LocalProgress {
  const existing = progress.stationStats[resolution.stationId] ?? {
    seen: 0,
    correct: 0,
    revealed: 0,
    misses: 0
  };

  return {
    ...progress,
    stationStats: {
      ...progress.stationStats,
      [resolution.stationId]: {
        seen: existing.seen + 1,
        correct: existing.correct + (resolution.correct ? 1 : 0),
        revealed: existing.revealed + (resolution.correct ? 0 : 1),
        misses: existing.misses + resolution.missCount
      }
    }
  };
}

export function recordGameCompletion(progress: LocalProgress, score: number): LocalProgress {
  return {
    ...progress,
    bestScore: Math.max(progress.bestScore, score),
    gamesPlayed: progress.gamesPlayed + 1,
    totalScore: progress.totalScore + score
  };
}

export function resetProgress(): LocalProgress {
  const progress = createDefaultProgress();
  saveProgress(progress);
  return progress;
}
