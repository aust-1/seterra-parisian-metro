import { describe, expect, it } from 'vitest';
import { lines, stations } from './metro';

const expectedLineIds = [
  '1',
  '2',
  '3',
  '3b',
  '4',
  '5',
  '6',
  '7',
  '7b',
  '8',
  '9',
  '10',
  '11',
  '12',
  '13',
  '14'
];

describe('metro data', () => {
  it('contains the complete MVP metro line scope', () => {
    expect(lines.map((line) => line.id)).toEqual(expectedLineIds);
    expect(stations.length).toBeGreaterThan(300);
  });

  it('keeps all line path references valid', () => {
    const stationIds = new Set(stations.map((station) => station.id));

    for (const line of lines) {
      expect(line.paths.length).toBeGreaterThan(0);

      for (const path of line.paths) {
        expect(path.length).toBeGreaterThan(1);

        for (const stationId of path) {
          expect(stationIds.has(stationId)).toBe(true);
        }
      }
    }
  });

  it('stores click coordinates for every station', () => {
    for (const station of stations) {
      expect(station.name).not.toHaveLength(0);
      expect(station.lineIds.length).toBeGreaterThan(0);
      expect(station.x).toBeGreaterThanOrEqual(0);
      expect(station.y).toBeGreaterThanOrEqual(0);
    }
  });
});
