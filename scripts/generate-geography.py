#!/usr/bin/env python3
"""Génère le tracé réel de la Seine et du boulevard périphérique pour la carte de l'app.

Sortie : app/src/main/assets/metro-data/geography.json, dans le même repère que les x/y des
stations (viewBox 1400 x 1120).

Données : OpenStreetMap (© contributeurs OpenStreetMap, licence ODbL), via l'API Overpass.
- Seine : surfaces d'eau (natural=water + water=river) nommées « La Seine », avec leurs îles
  (anneaux intérieurs). La Seine se divise donc d'elle-même autour des îles de la Cité,
  Saint-Louis, aux Cygnes, de la Jatte, Saint-Denis, Seguin…
- Périphérique : voies « Boulevard Périphérique Intérieur » (highway=trunk), raboutées en un
  anneau fermé.

Projection : celle de scripts/generate-metro-data.py est linéaire par axe (x = a·lon + b,
y = c·lat + d), mais ses bornes dépendaient du GTFS du moment. Plutôt que de la reconstituer,
ce script la recale par moindres carrés sur les stations de stations.json, appariées par nom
aux stations de métro d'OpenStreetMap : le tracé tombe exactement sous les points de la carte.

Usage (stdlib uniquement) :
    python3 scripts/generate-geography.py                  # télécharge depuis Overpass
    python3 scripts/generate-geography.py --cache osm/     # réutilise / garde les réponses
"""

from __future__ import annotations

import argparse
import json
import re
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from datetime import date
from pathlib import Path
from statistics import median

ROOT = Path(__file__).resolve().parent.parent
STATIONS = ROOT / "app/src/main/assets/metro-data/stations.json"
OUTPUT = ROOT / "app/src/main/assets/metro-data/geography.json"

MAP_WIDTH, MAP_HEIGHT = 1400, 1120
CLIP_MARGIN = 40  # au-delà, rien n'est visible même en dézoomant au maximum
SEINE_TOLERANCE = 0.2  # simplification Douglas-Peucker, en unités de carte
PERIPHERIQUE_TOLERANCE = 0.3
MAX_MEDIAN_RESIDUAL = 4.0  # au-delà, l'appariement des stations est suspect

# Emprise large de la carte (le recalage donne l'emprise exacte, ~2,21-2,50 E / 48,71-48,96 N).
BBOX = "48.69,2.19,48.98,2.52"
PERIPHERIQUE_BBOX = "48.80,2.22,48.91,2.43"
OVERPASS_MIRRORS = (
    "https://overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
    "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
)
QUERIES = {
    "stations": f"""[out:json][timeout:120];(
        node["railway"="station"]["station"="subway"]({BBOX});
        node["public_transport"="station"]["subway"="yes"]({BBOX}););out tags center;""",
    "water": f"""[out:json][timeout:180];(
        relation["natural"="water"]["water"="river"]({BBOX});
        way["natural"="water"]["water"="river"]({BBOX}););out geom;""",
    "peripherique": f"""[out:json][timeout:180];
        way["highway"]["name"~"Boulevard Périphérique"]({PERIPHERIQUE_BBOX});out geom;""",
}
SEINE_NAME = "La Seine"
# Tronçons de la Seine sans nom dans OSM, vérifiés à la main (sans eux, le tracé a un trou).
# Un rattachement automatique par contiguïté embarquerait aussi la Marne et l'Orge.
SEINE_UNNAMED_IDS = {("relation", 152887)}  # bras autour de l'île de la Jatte
PERIPHERIQUE_NAME = "Boulevard Périphérique Intérieur"

Point = tuple[float, float]


# --- Téléchargement ---------------------------------------------------------------------


def overpass(name: str, cache: Path | None) -> list[dict]:
    if cache and (cache / f"{name}.json").exists():
        return json.loads((cache / f"{name}.json").read_text())["elements"]

    body = urllib.parse.urlencode({"data": QUERIES[name]}).encode()
    for attempt in range(5):
        for url in OVERPASS_MIRRORS:
            request = urllib.request.Request(
                url, data=body, headers={"User-Agent": "seterra-parisian-metro/geography"}
            )
            try:
                with urllib.request.urlopen(request, timeout=240) as response:
                    raw = response.read()
            except (urllib.error.URLError, TimeoutError):
                continue
            if raw.lstrip().startswith(b"{"):
                if cache:
                    cache.mkdir(parents=True, exist_ok=True)
                    (cache / f"{name}.json").write_bytes(raw)
                return json.loads(raw)["elements"]
        time.sleep(10 * 2**attempt)  # serveurs saturés : on attend de plus en plus longtemps
    raise SystemExit(f"Overpass injoignable pour « {name} »")


# --- Projection -------------------------------------------------------------------------


def normalize(name: str) -> str:
    ascii_name = unicodedata.normalize("NFKD", name).encode("ascii", "ignore").decode()
    return re.sub(r"[^a-z0-9]+", " ", ascii_name.lower()).strip()


def linear_fit(xs: list[float], ys: list[float]) -> tuple[float, float]:
    mean_x, mean_y = sum(xs) / len(xs), sum(ys) / len(ys)
    slope = sum((x - mean_x) * (y - mean_y) for x, y in zip(xs, ys)) / sum(
        (x - mean_x) ** 2 for x in xs
    )
    return slope, mean_y - slope * mean_x


def fit_projection(osm_stations: list[dict]):
    """Recale x = ax·lon + bx et y = ay·lat + by sur les stations appariées par nom."""
    by_name: dict[str, list[Point]] = {}
    for element in osm_stations:
        if name := element.get("tags", {}).get("name"):
            by_name.setdefault(normalize(name), []).append((element["lon"], element["lat"]))

    pairs = []
    for station in json.loads(STATIONS.read_text()):
        if points := by_name.get(normalize(station["name"])):
            lon = sum(p[0] for p in points) / len(points)
            lat = sum(p[1] for p in points) / len(points)
            pairs.append((station["x"], station["y"], lon, lat))

    for _ in range(3):  # écarte les homonymes mal appariés, puis recale
        ax, bx = linear_fit([p[2] for p in pairs], [p[0] for p in pairs])
        ay, by = linear_fit([p[3] for p in pairs], [p[1] for p in pairs])
        residuals = [abs(ax * lon + bx - x) + abs(ay * lat + by - y) for x, y, lon, lat in pairs]
        pairs = [p for p, r in zip(pairs, residuals) if r < 3 * max(median(residuals), 1)]

    residual = median(residuals)
    print(f"projection recalée sur {len(pairs)} stations, écart médian {residual:.2f} unités")
    if residual > MAX_MEDIAN_RESIDUAL:
        raise SystemExit("recalage douteux : vérifier l'appariement des stations")
    return lambda lon, lat: (ax * lon + bx, ay * lat + by)


# --- Géométrie --------------------------------------------------------------------------


def node_key(point: Point) -> Point:
    return (round(point[0], 7), round(point[1], 7))


def stitch(segments: list[list[Point]]) -> list[list[Point]]:
    """Raboute des polylignes par leurs extrémités communes (dans un sens ou dans l'autre)."""
    pending = [list(s) for s in segments if len(s) > 1]
    chains = []
    while pending:
        chain = pending.pop(0)
        grew = True
        while grew and node_key(chain[0]) != node_key(chain[-1]):
            grew = False
            for i, segment in enumerate(pending):
                head, tail = node_key(chain[0]), node_key(chain[-1])
                if node_key(segment[0]) == tail:
                    chain += segment[1:]
                elif node_key(segment[-1]) == tail:
                    chain += segment[::-1][1:]
                elif node_key(segment[-1]) == head:
                    chain = segment + chain[1:]
                elif node_key(segment[0]) == head:
                    chain = segment[::-1] + chain[1:]
                else:
                    continue
                pending.pop(i)
                grew = True
                break
        chains.append(chain)
    return chains


def signed_area(ring: list[Point]) -> float:
    return sum(
        x1 * y2 - x2 * y1 for (x1, y1), (x2, y2) in zip(ring, ring[1:] + ring[:1])
    ) / 2


def clip_ring(ring: list[Point]) -> list[Point]:
    """Sutherland-Hodgman sur le rectangle de la carte élargi de CLIP_MARGIN."""
    lo_x, lo_y = -CLIP_MARGIN, -CLIP_MARGIN
    hi_x, hi_y = MAP_WIDTH + CLIP_MARGIN, MAP_HEIGHT + CLIP_MARGIN
    edges = (
        (lambda p: p[0] >= lo_x, lambda a, b: _cross_x(a, b, lo_x)),
        (lambda p: p[0] <= hi_x, lambda a, b: _cross_x(a, b, hi_x)),
        (lambda p: p[1] >= lo_y, lambda a, b: _cross_y(a, b, lo_y)),
        (lambda p: p[1] <= hi_y, lambda a, b: _cross_y(a, b, hi_y)),
    )
    points = ring
    for inside, cross in edges:
        if not points:
            break
        result = []
        for current, previous in zip(points, points[-1:] + points[:-1]):
            if inside(current):
                if not inside(previous):
                    result.append(cross(previous, current))
                result.append(current)
            elif inside(previous):
                result.append(cross(previous, current))
        points = result
    return points


def _cross_x(a: Point, b: Point, x: float) -> Point:
    t = (x - a[0]) / (b[0] - a[0])
    return (x, a[1] + t * (b[1] - a[1]))


def _cross_y(a: Point, b: Point, y: float) -> Point:
    t = (y - a[1]) / (b[1] - a[1])
    return (a[0] + t * (b[0] - a[0]), y)


def simplify(points: list[Point], tolerance: float) -> list[Point]:
    """Douglas-Peucker (itératif) sur une polyligne ouverte."""
    if len(points) < 3:
        return points
    keep = [False] * len(points)
    keep[0] = keep[-1] = True
    stack = [(0, len(points) - 1)]
    while stack:
        start, end = stack.pop()
        (x1, y1), (x2, y2) = points[start], points[end]
        length = ((x2 - x1) ** 2 + (y2 - y1) ** 2) ** 0.5 or 1e-9
        farthest, index = 0.0, None
        for i in range(start + 1, end):
            x0, y0 = points[i]
            distance = abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) / length
            if distance > farthest:
                farthest, index = distance, i
        if index is not None and farthest > tolerance:
            keep[index] = True
            stack += [(start, index), (index, end)]
    return [p for p, kept in zip(points, keep) if kept]


def simplify_ring(ring: list[Point], tolerance: float) -> list[Point]:
    if ring and node_key(ring[0]) == node_key(ring[-1]):
        ring = ring[:-1]
    if len(ring) < 4:
        return ring
    # Coupe l'anneau au point le plus éloigné du premier : deux polylignes ouvertes.
    far = max(range(len(ring)), key=lambda i: (ring[i][0] - ring[0][0]) ** 2 + (ring[i][1] - ring[0][1]) ** 2)
    first = simplify(ring[: far + 1], tolerance)
    second = simplify(ring[far:] + ring[:1], tolerance)
    return first[:-1] + second[:-1]


def flat(ring: list[Point]) -> list[float]:
    return [round(v, 1) for point in ring for v in point]


# --- Seine et périphérique ------------------------------------------------------------


def seine_rings(water: list[dict], project) -> list[list[Point]]:
    """Anneaux de la Seine, orientés pour un remplissage « non-zero » : berges dans un sens,
    îles dans l'autre. Les tronçons qui se chevauchent s'additionnent au lieu de se percer."""
    outer, inner = [], []
    for element in water:
        named = element.get("tags", {}).get("name") == SEINE_NAME
        if not named and (element["type"], element["id"]) not in SEINE_UNNAMED_IDS:
            continue
        if element["type"] == "way":
            outer.append([(p["lon"], p["lat"]) for p in element["geometry"]])
            continue
        for member in element["members"]:
            if member["type"] == "way" and member.get("geometry"):
                way = [(p["lon"], p["lat"]) for p in member["geometry"]]
                (inner if member["role"] == "inner" else outer).append(way)

    rings = []
    for chains, is_island in ((stitch(outer), False), (stitch(inner), True)):
        for chain in chains:
            ring = simplify_ring(clip_ring([project(*p) for p in chain]), SEINE_TOLERANCE)
            if len(ring) < 3 or abs(signed_area(ring)) < 1:
                continue  # hors de la carte, ou trop petit pour être vu
            if (signed_area(ring) > 0) == is_island:
                ring.reverse()
            rings.append(ring)
    return rings


def peripherique_ring(ways: list[dict], project) -> list[Point]:
    segments = [
        [(p["lon"], p["lat"]) for p in way["geometry"]]
        for way in ways
        if way.get("tags", {}).get("name") == PERIPHERIQUE_NAME
        and way["tags"].get("highway") == "trunk"
    ]
    ring = max(stitch(segments), key=len)
    if node_key(ring[0]) != node_key(ring[-1]):
        raise SystemExit("le périphérique ne se referme pas : vérifier les données OSM")
    return simplify_ring([project(*p) for p in ring], PERIPHERIQUE_TOLERANCE)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.split("\n", 1)[0])
    parser.add_argument("--cache", type=Path, help="dossier où garder/relire les réponses Overpass")
    args = parser.parse_args()

    project = fit_projection(overpass("stations", args.cache))
    seine = seine_rings(overpass("water", args.cache), project)
    peripherique = peripherique_ring(overpass("peripherique", args.cache), project)

    OUTPUT.write_text(
        json.dumps(
            {
                "source": "© contributeurs OpenStreetMap (ODbL) — openstreetmap.org/copyright",
                "generatedAt": date.today().isoformat(),
                "notes": "Anneaux à plat [x0, y0, x1, y1, …] dans le repère des stations. Seine : "
                "berges et îles en sens opposés, à remplir en « non-zero ».",
                "seine": [flat(r) for r in seine],
                "peripherique": flat(peripherique),
            },
            ensure_ascii=False,
            separators=(",", ":"),
        )
        + "\n"
    )
    points = sum(len(r) for r in seine)
    print(f"{OUTPUT.relative_to(ROOT)} : Seine {len(seine)} anneaux / {points} points, "
          f"périphérique {len(peripherique)} points")


if __name__ == "__main__":
    main()
