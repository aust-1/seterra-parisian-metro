export const MAX_MISSES = 3;

export function scoreForCorrectGuess(missCount: number): number {
  return Math.max(0, MAX_MISSES - missCount);
}

export function scoreForReveal(): number {
  return 0;
}
