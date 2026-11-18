# Quiz Métro Paris

Application Android native (Kotlin + Jetpack Compose), **100% hors connexion**, pour apprendre
par cœur les stations du métro parisien - dans l'esprit de Seterra : plusieurs modes de jeu,
plusieurs niveaux de difficulté, style visuel simple.

## Fonctionnalités

- **3 modes de jeu** : cliquer sur la carte, QCM, taper le nom de la station.
- **Difficulté réglable** : toutes les stations / stations "essentielles" (correspondances) /
  une sélection de lignes précises, avec ou sans les lignes affichées sur la carte.
- **Carte** avec zoom/pan tactile, posée sur la vraie géographie :
  - la **Seine** d'après ses berges réelles, divisée autour de ses îles (Cité, Saint-Louis, la
    Jatte, Saint-Denis…), sur toute l'emprise de la carte ;
  - le **boulevard périphérique** selon son tracé exact ;
  - les **20 arrondissements**, avec leur numéro.
- **Progression locale** : meilleur score par configuration, stations les plus ratées.
- **Aucune permission réseau** : les données (321 stations, 16 lignes) sont embarquées dans
  l'APK, rien n'est jamais envoyé ni téléchargé.

## Prérequis pour builder le projet

- [Android Studio](https://developer.android.com/studio) (Ladybug ou plus récent).
- JDK 17 (fourni avec Android Studio).
- Android SDK **35** (Android Studio te proposera de l'installer à la première ouverture si
  besoin - Outils > SDK Manager).

## Ouvrir le projet

1. Android Studio → **Open** → sélectionner le dossier du projet.
2. Attendre le Gradle Sync (télécharge les dépendances, nécessite une connexion internet
   *seulement pour builder* - l'app compilée, elle, n'a besoin de rien).

## Lancer les tests unitaires

Toute la logique de jeu (moteurs de quiz, scoring, normalisation des réponses, sélection des
stations) est en Kotlin pur, testée sans dépendance Android :

```bash
./gradlew test
```

Ou, dans Android Studio : clic droit sur `app/src/test` → **Run Tests**.

## Installer l'app sur ton téléphone (usage personnel)

Pas de Play Store : on génère un APK et on l'installe directement.

1. Android Studio → **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
2. Une fois le build terminé, cliquer sur **locate** dans la notification (le fichier est dans
   `app/build/outputs/apk/debug/app-debug.apk`).
3. Transférer cet APK sur le téléphone (câble, Drive, etc.) et l'ouvrir pour l'installer -
   Android demandera d'autoriser l'installation depuis cette source la première fois.

Alternative en une commande, téléphone branché en USB avec le débogage USB activé :

```bash
./gradlew installDebug
```

L'APK debug n'est pas destiné au Play Store mais convient très bien à une installation
manuelle sur ton propre appareil. Si tu veux un jour publier l'app ou distribuer un APK signé
"release", il faudra générer un keystore (Build → Generate Signed App Bundle / APK) - inutile
pour un usage strictement personnel.

## Structure du projet

```text
app/src/main/java/com/parismetro/quiz/
├── domain/        logique métier pure (moteurs de quiz, scoring, modèles) - sans Android
├── data/          assets JSON + persistance Room (scores, stats)
└── ui/            écrans Compose, ViewModels, navigation
app/src/main/assets/metro-data/   données embarquées : stations.json, lines.json, geography.json
app/src/test/                      tests unitaires (domaine + géographie embarquée)
scripts/                           génération des données (Python, stdlib uniquement)
```

## Origine des données

`stations.json` / `lines.json` (dans `app/src/main/assets/metro-data/`) proviennent du jeu de
données GTFS d'Île-de-France Mobilités - voir `scripts/metro-source.json` pour la source exacte
et `scripts/generate-metro-data.py` (Python, stdlib uniquement) pour le script qui les a générés.
Elles ne changent pas à l'exécution : il n'y a pas d'appel réseau pour les récupérer. Pour les
régénérer un jour (nouvelle station, ligne prolongée...), retélécharger le GTFS depuis
`downloadUrl` dans `metro-source.json` et relancer le script.

### Seine et périphérique

`geography.json` contient la Seine et le boulevard périphérique, tracés d'après les coordonnées
réelles d'[OpenStreetMap](https://www.openstreetmap.org/copyright) (© contributeurs
OpenStreetMap, licence ODbL). Il est généré par `scripts/generate-geography.py` :

- **Seine** : les surfaces d'eau nommées « La Seine », avec leurs îles en anneaux intérieurs.
  Le dessin suit donc les berges, et le fleuve se divise de lui-même autour de chaque île.
- **Périphérique** : les voies « Boulevard Périphérique Intérieur », raboutées en un anneau
  fermé.
- **Projection** : le script la recale sur les stations elles-mêmes. Il apparie environ 280
  stations de `stations.json` à leurs coordonnées OSM, puis ajuste par moindres carrés le
  `x = a·lon + b, y = c·lat + d` du script GTFS. L'écart médian est d'environ 1,6 unité de
  carte. Le tracé tombe ainsi pile sous les stations et le long des arrondissements.
- **Allègement** : les tracés sont découpés au cadre de la carte, puis simplifiés
  (Douglas-Peucker), pour environ 1 200 points en tout.

```bash
python3 scripts/generate-geography.py                 # télécharge via l'API Overpass
python3 scripts/generate-geography.py --cache osm/    # garde / réutilise les réponses
```

L'écran d'accueil affiche l'attribution des données, comme l'exige l'ODbL.

### Arrondissements

`ui/map/Arrondissements.kt` vient des limites officielles d'opendata.paris.fr, reprojetées
dans le même repère et simplifiées.

## Intégration continue

`.github/workflows/android.yml` lance les tests unitaires, puis construit l'APK debug, à chaque
push sur `main` et sur chaque PR. L'APK est téléchargeable dans les artefacts du run pendant 7
jours.

## Problèmes courants

**"The project's Gradle version X is incompatible with the Gradle JVM version Y"** - Android
Studio utilise par défaut le JDK le plus récent trouvé sur la machine pour *exécuter* Gradle
lui-même (pas pour compiler l'appli - ça, c'est réglé par `compileOptions` dans
`app/build.gradle.kts` et reste en Java 17 quoi qu'il arrive). Si ce JDK est trop récent pour la
version de Gradle du projet, corrige-le dans Android Studio, pas dans le code :

1. **File → Settings** (ou **Android Studio → Preferences** sur Mac) →
   **Build, Execution, Deployment → Build Tools → Gradle**.
2. Champ **Gradle JDK** → choisir le JDK 17 installé (ou **Add JDK...** et pointer vers son
   dossier d'installation s'il n'apparaît pas dans la liste).
3. **OK**, puis relancer le Gradle Sync (icône éléphant, ou le bandeau qui propose de resync).

## Check-list au premier build

- [x] Le Gradle Sync se termine sans erreur de résolution de dépendances.
- [x] `./gradlew test` passe (37/37 : logique de jeu et géographie embarquée).
- [x] `./gradlew assembleDebug` compile l'appli et produit `app-debug.apk`.
- [ ] L'appli s'installe sur un téléphone et le zoom/pan de la carte se sent naturel au
      toucher (c'est la partie la plus délicate à valider sans appareil physique - ajuste les
      bornes de zoom dans `MetroMapCanvas.kt` si besoin).
- [ ] Une partie complète dans chacun des 3 modes se joue sans accroc.
