import rawLines from './lines.json';
import rawMetadata from './metro-source.json';
import rawStations from './stations.json';
import type { MetroLine, Station } from '$lib/types';

export const stations = rawStations as Station[];
export const lines = rawLines as MetroLine[];
export const metroDataMetadata = rawMetadata;

export const stationsById = new Map(stations.map((station) => [station.id, station]));
export const linesById = new Map(lines.map((line) => [line.id, line]));

export function getStationById(stationId: string): Station {
  const station = stationsById.get(stationId);

  if (!station) {
    throw new Error(`Unknown station: ${stationId}`);
  }

  return station;
}

export function getLineById(lineId: string): MetroLine {
  const line = linesById.get(lineId);

  if (!line) {
    throw new Error(`Unknown metro line: ${lineId}`);
  }

  return line;
}
