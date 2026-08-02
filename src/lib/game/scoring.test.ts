import { describe, expect, it } from 'vitest';
import { scoreForCorrectGuess, scoreForReveal } from './scoring';

describe('scoring', () => {
	it('awards fewer points after each miss', () => {
		expect(scoreForCorrectGuess(0)).toBe(3);
		expect(scoreForCorrectGuess(1)).toBe(2);
		expect(scoreForCorrectGuess(2)).toBe(1);
	});

	it('never awards points after reveal', () => {
		expect(scoreForCorrectGuess(3)).toBe(0);
		expect(scoreForReveal()).toBe(0);
	});
});
