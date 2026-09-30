# Quiz Métro Paris

Application Android native (Kotlin + Jetpack Compose), **100% hors connexion**, pour apprendre
par cœur les stations du métro parisien - dans l'esprit de Seterra : plusieurs modes de jeu,
plusieurs niveaux de difficulté, style visuel simple.

## Fonctionnalités

- **3 modes de jeu** : cliquer sur la carte, QCM, taper le nom de la station.
- **Difficulté réglable** : toutes les stations / stations "essentielles" (correspondances) /
  une sélection de lignes précises, avec ou sans les lignes affichées sur la carte.
- **Carte schématique** avec zoom/pan tactile.
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
app/src/main/assets/metro-data/   données stations.json / lines.json embarquées
app/src/test/                      tests unitaires de la couche domaine
```

## Origine des données

`stations.json` / `lines.json` (dans `app/src/main/assets/metro-data/`) proviennent du jeu de
données GTFS d'Île-de-France Mobilités - voir `scripts/metro-source.json` pour la source exacte
et `scripts/generate-metro-data.py` (Python, stdlib uniquement) pour le script qui les a générés.
Elles ne changent pas à l'exécution : il n'y a pas d'appel réseau pour les récupérer. Pour les
régénérer un jour (nouvelle station, ligne prolongée...), retélécharger le GTFS depuis
`downloadUrl` dans `metro-source.json` et relancer le script.

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

Le code a été écrit avec soin mais jamais compilé sur cette machine. À la première tentative :

- [ ] Le Gradle Sync se termine sans erreur de résolution de dépendances.
- [ ] `./gradlew test` passe (logique de jeu).
- [ ] `./gradlew assembleDebug` compile l'appli.
- [ ] L'appli s'installe et le zoom/pan de la carte se sent naturel au toucher (c'est la partie
      la plus délicate à valider sans appareil physique - ajuste les bornes de zoom dans
      `MetroMapCanvas.kt` si besoin).
