# Seterra du métro parisien

Jeu mobile-first inspiré de Seterra pour apprendre à placer les stations du métro parisien.

## Stack

- SvelteKit + TypeScript
- Tailwind CSS
- SVG custom interactif
- Données JSON locales
- Progression locale via `localStorage`
- Build statique avec `@sveltejs/adapter-static`

## Gameplay MVP

Une station est tirée au hasard. Le joueur zoome/déplace la carte puis clique sur le point exact.

- 3 points si la station est trouvée sans erreur
- 2 points après une erreur
- 1 point après deux erreurs
- 0 point après trois erreurs, avec révélation de la bonne station

Les mauvais clics restent affichés sur la carte pour montrer où le joueur a répondu.

## Données

Les fichiers versionnés sont dans `src/lib/data/` :

- `stations.json`
- `lines.json`
- `metro-source.json`

Périmètre : lignes `1` à `14`, plus `3bis` et `7bis`.

Source : Île-de-France Mobilités, GTFS Datahub.

Pour régénérer les données après avoir téléchargé le ZIP GTFS officiel :

```bash
python scripts/generate-metro-data.py /path/to/IDFM-gtfs.zip --output-dir src/lib/data
```

## Développement

```bash
corepack enable
pnpm install
pnpm dev
```

## Qualité

```bash
pnpm check
pnpm lint
pnpm test
pnpm build
```
