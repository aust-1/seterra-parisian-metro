from __future__ import annotations

import argparse
import csv
import json
import math
import re
import unicodedata
import zipfile
from collections import Counter, defaultdict
from datetime import date
from pathlib import Path
from typing import Iterable

LINE_SHORT_NAMES = [
    "1",
    "2",
    "3",
    "3B",
    "4",
    "5",
    "6",
    "7",
    "7B",
    "8",
    "9",
    "10",
    "11",
    "12",
    "13",
    "14",
]

PATH_SELECTIONS = {
    "1": [("La Défense (Grande Arche)", "Château de Vincennes")],
    "2": [("Porte Dauphine", "Nation")],
    "3": [("Pont de Levallois - Bécon", "Gallieni")],
    "3B": [("Gambetta", "Porte des Lilas")],
    "4": [("Bagneux - Lucie Aubrac", "Porte de Clignancourt")],
    "5": [("Place d'Italie", "Bobigny - Pablo Picasso")],
    "6": [("Charles de Gaulle - Étoile", "Nation")],
    "7": [
        ("Villejuif - Louis Aragon", "La Courneuve - 8 Mai 1945"),
        ("Mairie d'Ivry", "La Courneuve - 8 Mai 1945"),
    ],
    "7B": [
        ("Louis Blanc", "Pré-Saint-Gervais"),
        ("Pré-Saint-Gervais", "Louis Blanc"),
    ],
    "8": [("Pointe du Lac", "Balard")],
    "9": [("Mairie de Montreuil", "Pont de Sèvres")],
    "10": [
        ("Gare d'Austerlitz", "Boulogne Pont de Saint-Cloud"),
        ("Boulogne Pont de Saint-Cloud", "Gare d'Austerlitz"),
    ],
    "11": [("Châtelet", "Rosny-Bois-Perrier")],
    "12": [("Mairie d'Aubervilliers", "Mairie d'Issy")],
    "13": [
        ("Saint-Denis - Université", "Châtillon - Montrouge"),
        ("Asnières - Gennevilliers - Les Courtilles", "Châtillon - Montrouge"),
    ],
    "14": [("Aéroport d'Orly", "Saint-Denis - Pleyel")],
}

SVG_WIDTH = 1400
SVG_HEIGHT = 1120
SVG_MARGIN = 72


def read_csv_from_zip(zip_file: zipfile.ZipFile, filename: str) -> Iterable[dict[str, str]]:
    with zip_file.open(filename) as file:
        yield from csv.DictReader((line.decode("utf-8-sig") for line in file))


def slugify(value: str) -> str:
    normalized = unicodedata.normalize("NFKD", value)
    ascii_value = normalized.encode("ascii", "ignore").decode("ascii")
    slug = re.sub(r"[^a-zA-Z0-9]+", "-", ascii_value.lower()).strip("-")
    return slug


def public_line_id(short_name: str) -> str:
    return short_name.lower()


def line_label(short_name: str) -> str:
    return short_name.replace("B", "bis")


def collect_sequences(zip_file: zipfile.ZipFile) -> tuple[dict[str, dict], dict[str, Counter]]:
    route_by_id = {}
    for row in read_csv_from_zip(zip_file, "routes.txt"):
        if row["route_type"] == "1" and row["route_short_name"] in LINE_SHORT_NAMES:
            route_by_id[row["route_id"]] = {
                "short_name": row["route_short_name"],
                "color": f"#{row['route_color']}",
                "text_color": f"#{row['route_text_color']}",
            }

    trip_to_line = {}
    for row in read_csv_from_zip(zip_file, "trips.txt"):
        if row["route_id"] in route_by_id:
            trip_to_line[row["trip_id"]] = route_by_id[row["route_id"]]["short_name"]

    stop_names = {}
    stop_coordinates = {}
    for row in read_csv_from_zip(zip_file, "stops.txt"):
        stop_names[row["stop_id"]] = row["stop_name"]
        if row["stop_lat"] and row["stop_lon"]:
            stop_coordinates[row["stop_id"]] = (float(row["stop_lon"]), float(row["stop_lat"]))

    trip_stop_ids: dict[str, list[tuple[int, str]]] = defaultdict(list)
    for row in read_csv_from_zip(zip_file, "stop_times.txt"):
        trip_id = row["trip_id"]
        if trip_id in trip_to_line:
            trip_stop_ids[trip_id].append((int(row["stop_sequence"]), row["stop_id"]))

    sequences_by_line: dict[str, Counter] = defaultdict(Counter)
    coordinate_samples: dict[str, list[tuple[float, float]]] = defaultdict(list)

    for trip_id, entries in trip_stop_ids.items():
        line = trip_to_line[trip_id]
        ordered_stop_ids = [stop_id for _, stop_id in sorted(entries)]
        names = tuple(stop_names[stop_id] for stop_id in ordered_stop_ids)
        sequences_by_line[line][names] += 1

        for stop_id in ordered_stop_ids:
            stop_name = stop_names[stop_id]
            if stop_id in stop_coordinates:
                coordinate_samples[stop_name].append(stop_coordinates[stop_id])

    return route_by_id, sequences_by_line, coordinate_samples


def select_path(sequences: Counter, start: str, end: str) -> list[str]:
    candidates = [
        (names, count)
        for names, count in sequences.items()
        if names and names[0] == start and names[-1] == end
    ]
    if not candidates:
        reverse_candidates = [
            (names, count)
            for names, count in sequences.items()
            if names and names[0] == end and names[-1] == start
        ]
        if reverse_candidates:
            names, _ = max(reverse_candidates, key=lambda item: (len(item[0]), item[1]))
            return list(reversed(names))
        raise ValueError(f"No GTFS path found from {start!r} to {end!r}.")

    names, _ = max(candidates, key=lambda item: (len(item[0]), item[1]))
    return list(names)


def project_coordinates(
    samples_by_station: dict[str, list[tuple[float, float]]]
) -> dict[str, tuple[int, int]]:
    averaged = {
        station: (
            sum(lon for lon, _ in samples) / len(samples),
            sum(lat for _, lat in samples) / len(samples),
        )
        for station, samples in samples_by_station.items()
    }

    lons = [lon for lon, _ in averaged.values()]
    lats = [lat for _, lat in averaged.values()]
    min_lon, max_lon = min(lons), max(lons)
    min_lat, max_lat = min(lats), max(lats)
    usable_width = SVG_WIDTH - SVG_MARGIN * 2
    usable_height = SVG_HEIGHT - SVG_MARGIN * 2

    projected = {}
    for station, (lon, lat) in averaged.items():
        normalized_x = (lon - min_lon) / (max_lon - min_lon)
        normalized_y = (max_lat - lat) / (max_lat - min_lat)
        projected[station] = (
            round(SVG_MARGIN + normalized_x * usable_width),
            round(SVG_MARGIN + normalized_y * usable_height),
        )

    return projected


def dedupe_path(path: list[str]) -> list[str]:
    deduped = []
    for station_name in path:
        if not deduped or deduped[-1] != station_name:
            deduped.append(station_name)
    return deduped


def generate(zip_path: Path, output_dir: Path) -> None:
    with zipfile.ZipFile(zip_path) as zip_file:
        routes, sequences_by_line, coordinate_samples = collect_sequences(zip_file)

    selected_paths_by_line: dict[str, list[list[str]]] = {}
    selected_station_names = set()

    for short_name in LINE_SHORT_NAMES:
        selected_paths = []
        for start, end in PATH_SELECTIONS[short_name]:
            path = dedupe_path(select_path(sequences_by_line[short_name], start, end))
            selected_paths.append(path)
            selected_station_names.update(path)
        selected_paths_by_line[short_name] = selected_paths

    selected_coordinate_samples = {
        station: coordinate_samples[station] for station in selected_station_names
    }
    projected = project_coordinates(selected_coordinate_samples)

    station_ids_by_name = {name: slugify(name) for name in sorted(selected_station_names)}
    lines_for_station: dict[str, set[str]] = defaultdict(set)
    for short_name, paths in selected_paths_by_line.items():
        line_id = public_line_id(short_name)
        for path in paths:
            for station_name in path:
                lines_for_station[station_name].add(line_id)

    stations = []
    for station_name in sorted(selected_station_names):
        x, y = projected[station_name]
        stations.append(
            {
                "id": station_ids_by_name[station_name],
                "name": station_name,
                "lineIds": sorted(
                    lines_for_station[station_name],
                    key=lambda line_id: LINE_SHORT_NAMES.index(line_id.upper()),
                ),
                "x": x,
                "y": y,
            }
        )

    lines = []
    for short_name in LINE_SHORT_NAMES:
        route = next(route for route in routes.values() if route["short_name"] == short_name)
        lines.append(
            {
                "id": public_line_id(short_name),
                "name": f"Ligne {line_label(short_name)}",
                "label": line_label(short_name),
                "color": route["color"],
                "textColor": route["text_color"],
                "paths": [
                    [station_ids_by_name[station_name] for station_name in path]
                    for path in selected_paths_by_line[short_name]
                ],
            }
        )

    metadata = {
        "source": "Île-de-France Mobilités — Horaires prévus sur les lignes de transport en commun d'Ile-de-France (GTFS Datahub)",
        "sourceUrl": "https://data.iledefrance-mobilites.fr/explore/dataset/offre-horaires-tc-gtfs-idfm/",
        "downloadUrl": "https://eu.ftp.opendatasoft.com/stif/GTFS/IDFM-gtfs.zip",
        "generatedAt": date.today().isoformat(),
        "svgViewBox": {"width": SVG_WIDTH, "height": SVG_HEIGHT},
        "lineCount": len(lines),
        "stationCount": len(stations),
        "notes": "Les coordonnées x/y sont une projection locale simplifiée des arrêts GTFS pour une carte SVG custom.",
    }

    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "stations.json").write_text(
        json.dumps(stations, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    (output_dir / "lines.json").write_text(
        json.dumps(lines, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    (output_dir / "metro-source.json").write_text(
        json.dumps(metadata, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    print(
        f"Generated {len(stations)} stations and {len(lines)} lines in {output_dir.as_posix()}."
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate local Paris Metro JSON data from IDFM GTFS.")
    parser.add_argument("zip_path", type=Path, help="Path to IDFM-gtfs.zip")
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("src/lib/data"),
        help="Output directory for JSON files.",
    )
    args = parser.parse_args()

    if not args.zip_path.exists():
        raise SystemExit(f"GTFS ZIP not found: {args.zip_path}")

    generate(args.zip_path, args.output_dir)


if __name__ == "__main__":
    main()
