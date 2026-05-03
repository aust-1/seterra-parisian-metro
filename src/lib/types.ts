export type Station = {
  id: string;
  name: string;
  lineIds: string[];
  x: number;
  y: number;
};

export type MetroLine = {
  id: string;
  name: string;
  label: string;
  color: string;
  textColor: string;
  paths: string[][];
};

export type MapPoint = {
  x: number;
  y: number;
};

export type MissMarker = MapPoint & {
  id: string;
};
