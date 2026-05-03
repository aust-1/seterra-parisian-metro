<script lang="ts">
  import { onMount } from 'svelte';
  import MetroMap from '$lib/components/metro-map/MetroMap.svelte';
  import { lines, metroDataMetadata, stations } from '$lib/data/metro';
  import {
    createQuiz,
    goToNextQuestion,
    submitMapMiss,
    submitStationGuess,
    type GuessOutcome,
    type QuizState
  } from '$lib/game/quiz-engine';
  import { MAX_MISSES } from '$lib/game/scoring';
  import {
    createDefaultProgress,
    loadProgress,
    recordGameCompletion,
    recordStationResolution,
    resetProgress,
    saveProgress,
    type LocalProgress
  } from '$lib/stores/progress';
  import type { MapPoint, Station } from '$lib/types';

  const stationIds = stations.map((station) => station.id);
  const stationById = new Map(stations.map((station) => [station.id, station]));
  const maxScore = stations.length * MAX_MISSES;

  let quiz: QuizState = createQuiz(stationIds);
  let progress: LocalProgress = createDefaultProgress();
  let feedback = 'Clique sur le point correspondant à la station demandée.';

  $: question = quiz.currentQuestion;
  $: targetStation = question ? stationById.get(question.stationId) : null;
  $: revealedStationId =
    question && question.status !== 'guessing' ? question.stationId : null;
  $: missMarkers = question?.missMarkers ?? [];
  $: completedCount = quiz.completedStationIds.length;
  $: progressPercent = Math.round((completedCount / stations.length) * 100);

  onMount(() => {
    progress = loadProgress();
  });

  function handleStationClick(stationId: string, point: MapPoint): void {
    const result = submitStationGuess(quiz, stationId, point);
    quiz = result.state;
    feedback = feedbackForOutcome(
      result.outcome,
      result.pointsAwarded,
      result.state.currentQuestion?.missCount ?? 0
    );
    recordResolutionIfNeeded(result.outcome);
  }

  function handleMapMiss(point: MapPoint): void {
    const result = submitMapMiss(quiz, point);
    quiz = result.state;
    feedback = feedbackForOutcome(
      result.outcome,
      result.pointsAwarded,
      result.state.currentQuestion?.missCount ?? 0
    );
    recordResolutionIfNeeded(result.outcome);
  }

  function recordResolutionIfNeeded(outcome: GuessOutcome): void {
    if (outcome !== 'correct' && outcome !== 'revealed') {
      return;
    }

    const resolvedQuestion = quiz.currentQuestion;

    if (!resolvedQuestion) {
      return;
    }

    progress = recordStationResolution(progress, {
      stationId: resolvedQuestion.stationId,
      correct: outcome === 'correct',
      missCount: resolvedQuestion.missCount
    });
    saveProgress(progress);
  }

  function handleNextQuestion(): void {
    const previousScore = quiz.score;
    quiz = goToNextQuestion(quiz);

    if (quiz.status === 'completed') {
      progress = recordGameCompletion(progress, previousScore);
      saveProgress(progress);
      feedback = `Partie terminée : ${previousScore} / ${maxScore} points.`;
      return;
    }

    feedback = 'Nouvelle station : clique sur son point exact.';
  }

  function handleRestart(): void {
    quiz = createQuiz(stationIds);
    feedback = 'Nouvelle partie lancée.';
  }

  function handleResetProgress(): void {
    progress = resetProgress();
  }

  function feedbackForOutcome(
    outcome: GuessOutcome,
    pointsAwarded: number,
    missCount: number
  ): string {
    if (outcome === 'correct') {
      return `Correct : +${pointsAwarded} point${pointsAwarded > 1 ? 's' : ''}.`;
    }

    if (outcome === 'revealed') {
      return "Trois échecs : la bonne station est révélée.";
    }

    if (outcome === 'incorrect') {
      const remaining = Math.max(0, MAX_MISSES - missCount);
      return `Mauvais emplacement. ${remaining} essai${remaining > 1 ? 's' : ''} restant${remaining > 1 ? 's' : ''}.`;
    }

    return feedback;
  }

  function stationLineLabels(station: Station): string {
    return station.lineIds.map((lineId) => lineId.replace('b', 'bis')).join(', ');
  }
</script>

<svelte:head>
  <title>Seterra du métro parisien</title>
</svelte:head>

<main class="min-h-screen bg-slate-100 px-4 py-4 text-slate-900 md:px-8 md:py-6">
  <section class="mx-auto grid max-w-[1500px] gap-4 lg:grid-cols-[390px_minmax(0,1fr)]">
    <aside class="order-2 rounded-[2rem] border border-slate-200 bg-white p-5 shadow-sm lg:order-1">
      <div class="mb-5">
        <p class="text-sm font-semibold uppercase tracking-[0.22em] text-metro">
          Seterra métro
        </p>
        <h1 class="mt-2 text-3xl font-black tracking-tight text-slate-950">
          Place la station
        </h1>
        <p class="mt-2 text-sm leading-6 text-slate-600">
          Zoom, déplace la carte, puis clique sur le point exact. Après trois erreurs,
          la réponse est affichée.
        </p>
      </div>

      <div class="rounded-3xl bg-slate-950 p-5 text-white">
        <p class="text-sm text-slate-300">Station à trouver</p>
        <p class="mt-2 text-3xl font-black leading-tight">
          {targetStation?.name ?? 'Partie terminée'}
        </p>

        {#if targetStation && question?.status !== 'guessing'}
          <p class="mt-3 text-sm text-slate-300">
            Ligne{targetStation.lineIds.length > 1 ? 's' : ''} :
            {stationLineLabels(targetStation)}
          </p>
        {/if}
      </div>

      <div class="mt-4 grid grid-cols-2 gap-3">
        <div class="rounded-2xl bg-slate-100 p-4">
          <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Score</p>
          <p class="mt-1 text-2xl font-black">{quiz.score}</p>
          <p class="text-xs text-slate-500">sur {maxScore}</p>
        </div>
        <div class="rounded-2xl bg-slate-100 p-4">
          <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Progression</p>
          <p class="mt-1 text-2xl font-black">{completedCount}/{stations.length}</p>
          <p class="text-xs text-slate-500">{progressPercent}% terminé</p>
        </div>
        <div class="rounded-2xl bg-slate-100 p-4">
          <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Échecs</p>
          <p class="mt-1 text-2xl font-black">{question?.missCount ?? 0}/{MAX_MISSES}</p>
        </div>
        <div class="rounded-2xl bg-slate-100 p-4">
          <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Record</p>
          <p class="mt-1 text-2xl font-black">{progress.bestScore}</p>
          <p class="text-xs text-slate-500">{progress.gamesPlayed} partie(s)</p>
        </div>
      </div>

      <div class="mt-4 rounded-2xl border border-slate-200 p-4 text-sm leading-6 text-slate-700">
        {feedback}
      </div>

      <div class="mt-4 flex flex-col gap-3">
        {#if question?.status !== 'guessing' && quiz.status !== 'completed'}
          <button
            class="rounded-2xl bg-slate-950 px-5 py-3 font-bold text-white"
            type="button"
            on:click={handleNextQuestion}
          >
            Station suivante
          </button>
        {/if}

        {#if quiz.status === 'completed'}
          <button
            class="rounded-2xl bg-slate-950 px-5 py-3 font-bold text-white"
            type="button"
            on:click={handleRestart}
          >
            Rejouer
          </button>
        {/if}

        <button
          class="rounded-2xl bg-slate-100 px-5 py-3 font-bold text-slate-900"
          type="button"
          on:click={handleRestart}
        >
          Nouvelle partie
        </button>

        <button
          class="rounded-2xl border border-red-200 px-5 py-3 font-bold text-red-700"
          type="button"
          on:click={handleResetProgress}
        >
          Réinitialiser la progression locale
        </button>
      </div>

      <div class="mt-5 text-xs leading-5 text-slate-500">
        Données : {metroDataMetadata.stationCount} stations, {metroDataMetadata.lineCount}
        lignes. Source IDFM GTFS, projection SVG locale.
      </div>
    </aside>

    <section class="order-1 min-h-[62vh] lg:order-2">
      <MetroMap
        {stations}
        {lines}
        {revealedStationId}
        {missMarkers}
        onStationClick={handleStationClick}
        onMapMiss={handleMapMiss}
      />
    </section>
  </section>
</main>
