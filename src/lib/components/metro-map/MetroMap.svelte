<script lang="ts">
  import type { MapPoint, MetroLine, MissMarker, Station } from '$lib/types';

  export let stations: Station[] = [];
  export let lines: MetroLine[] = [];
  export let revealedStationId: string | null = null;
  export let missMarkers: MissMarker[] = [];
  export let showStationNames = false;
  export let onStationClick: (stationId: string, point: MapPoint) => void = () => {};
  export let onMapMiss: (point: MapPoint) => void = () => {};

  const baseViewBox = {
    x: 0,
    y: 0,
    width: 1400,
    height: 1120
  };
  const minViewBoxWidth = 260;

  let svgElement: SVGSVGElement;
  let viewBox = { ...baseViewBox };
  let isPanning = false;
  let lastClientPoint: MapPoint | null = null;
  let dragDistance = 0;

  $: stationById = new Map(stations.map((station) => [station.id, station]));
  $: viewBoxValue = `${viewBox.x} ${viewBox.y} ${viewBox.width} ${viewBox.height}`;

  function pathPoints(path: string[]): string {
    return path
      .map((stationId) => stationById.get(stationId))
      .filter((station): station is Station => Boolean(station))
      .map((station) => `${station.x},${station.y}`)
      .join(' ');
  }

  function stationColor(station: Station): string {
    const line = lines.find((candidate) => candidate.id === station.lineIds[0]);
    return line?.color ?? '#14213d';
  }

  function svgPointFromClient(clientX: number, clientY: number): MapPoint {
    const point = svgElement.createSVGPoint();
    point.x = clientX;
    point.y = clientY;

    const matrix = svgElement.getScreenCTM();

    if (!matrix) {
      return { x: 0, y: 0 };
    }

    const transformed = point.matrixTransform(matrix.inverse());

    return {
      x: Math.round(transformed.x),
      y: Math.round(transformed.y)
    };
  }

  function handleStationClick(event: MouseEvent, station: Station): void {
    event.stopPropagation();

    if (dragDistance > 6) {
      return;
    }

    onStationClick(station.id, { x: station.x, y: station.y });
  }

  function handleMapClick(event: MouseEvent): void {
    if (dragDistance > 6) {
      return;
    }

    const target = event.target as Element;

    if (target.closest('[data-station-id]')) {
      return;
    }

    onMapMiss(svgPointFromClient(event.clientX, event.clientY));
  }

  function handlePointerDown(event: PointerEvent): void {
    isPanning = true;
    dragDistance = 0;
    lastClientPoint = { x: event.clientX, y: event.clientY };
    svgElement.setPointerCapture(event.pointerId);
  }

  function handlePointerMove(event: PointerEvent): void {
    if (!isPanning || !lastClientPoint) {
      return;
    }

    const rect = svgElement.getBoundingClientRect();
    const deltaX = event.clientX - lastClientPoint.x;
    const deltaY = event.clientY - lastClientPoint.y;
    const scaleX = viewBox.width / rect.width;
    const scaleY = viewBox.height / rect.height;

    dragDistance += Math.abs(deltaX) + Math.abs(deltaY);
    viewBox = clampViewBox({
      ...viewBox,
      x: viewBox.x - deltaX * scaleX,
      y: viewBox.y - deltaY * scaleY
    });
    lastClientPoint = { x: event.clientX, y: event.clientY };
  }

  function handlePointerUp(event: PointerEvent): void {
    isPanning = false;
    lastClientPoint = null;

    if (svgElement.hasPointerCapture(event.pointerId)) {
      svgElement.releasePointerCapture(event.pointerId);
    }

    setTimeout(() => {
      dragDistance = 0;
    }, 0);
  }

  function handleWheel(event: WheelEvent): void {
    const factor = event.deltaY < 0 ? 0.82 : 1.18;
    zoomAt(factor, svgPointFromClient(event.clientX, event.clientY));
  }

  function zoomAt(factor: number, center?: MapPoint): void {
    const zoomCenter = center ?? {
      x: viewBox.x + viewBox.width / 2,
      y: viewBox.y + viewBox.height / 2
    };
    const nextWidth = clamp(viewBox.width * factor, minViewBoxWidth, baseViewBox.width);
    const nextHeight = nextWidth * (baseViewBox.height / baseViewBox.width);
    const widthRatio = nextWidth / viewBox.width;
    const heightRatio = nextHeight / viewBox.height;

    viewBox = clampViewBox({
      x: zoomCenter.x - (zoomCenter.x - viewBox.x) * widthRatio,
      y: zoomCenter.y - (zoomCenter.y - viewBox.y) * heightRatio,
      width: nextWidth,
      height: nextHeight
    });
  }

  function resetZoom(): void {
    viewBox = { ...baseViewBox };
  }

  function clampViewBox(nextViewBox: typeof viewBox): typeof viewBox {
    const width = clamp(nextViewBox.width, minViewBoxWidth, baseViewBox.width);
    const height = width * (baseViewBox.height / baseViewBox.width);
    const maxX = baseViewBox.width - width;
    const maxY = baseViewBox.height - height;

    return {
      x: clamp(nextViewBox.x, 0, Math.max(0, maxX)),
      y: clamp(nextViewBox.y, 0, Math.max(0, maxY)),
      width,
      height
    };
  }

  function clamp(value: number, minimum: number, maximum: number): number {
    return Math.min(Math.max(value, minimum), maximum);
  }
</script>

<div class="relative h-full min-h-[62vh] overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
  <svg
    bind:this={svgElement}
    class="h-full min-h-[62vh] w-full touch-none select-none"
    viewBox={viewBoxValue}
    role="img"
    aria-label="Carte interactive du métro parisien"
    on:click={handleMapClick}
    on:pointerdown={handlePointerDown}
    on:pointermove={handlePointerMove}
    on:pointerup={handlePointerUp}
    on:pointercancel={handlePointerUp}
    on:wheel|preventDefault={handleWheel}
  >
    <rect
      x="0"
      y="0"
      width={baseViewBox.width}
      height={baseViewBox.height}
      fill="#f8fafc"
    />

    <g opacity="0.16">
      <circle cx="700" cy="560" r="365" fill="none" stroke="#94a3b8" stroke-width="2" />
      <circle cx="700" cy="560" r="230" fill="none" stroke="#cbd5e1" stroke-width="2" />
      <line x1="72" y1="560" x2="1328" y2="560" stroke="#e2e8f0" stroke-width="2" />
      <line x1="700" y1="72" x2="700" y2="1048" stroke="#e2e8f0" stroke-width="2" />
    </g>

    <g fill="none" stroke-linecap="round" stroke-linejoin="round">
      {#each lines as line}
        <g>
          {#each line.paths as path}
            <polyline
              points={pathPoints(path)}
              stroke={line.color}
              stroke-width="10"
              opacity="0.88"
            />
            <polyline
              points={pathPoints(path)}
              stroke="#ffffff"
              stroke-width="3"
              opacity="0.62"
            />
          {/each}
        </g>
      {/each}
    </g>

    <g>
      {#each stations as station}
        <g data-station-id={station.id}>
          <circle
            cx={station.x}
            cy={station.y}
            r={revealedStationId === station.id ? 12 : 8}
            fill={stationColor(station)}
            stroke={revealedStationId === station.id ? '#16a34a' : '#ffffff'}
            stroke-width={revealedStationId === station.id ? 5 : 3}
            class="cursor-pointer"
            on:click={(event) => handleStationClick(event, station)}
          >
            <title>{station.name}</title>
          </circle>

          {#if showStationNames || revealedStationId === station.id}
            <text
              x={station.x + 12}
              y={station.y - 12}
              fill="#0f172a"
              stroke="#ffffff"
              stroke-width="5"
              paint-order="stroke"
              font-size="18"
              font-weight="700"
            >
              {station.name}
            </text>
          {/if}
        </g>
      {/each}
    </g>

    <g>
      {#each missMarkers as marker}
        <g transform={`translate(${marker.x} ${marker.y})`}>
          <circle r="12" fill="#fee2e2" stroke="#dc2626" stroke-width="4" />
          <line x1="-7" y1="-7" x2="7" y2="7" stroke="#dc2626" stroke-width="4" />
          <line x1="-7" y1="7" x2="7" y2="-7" stroke="#dc2626" stroke-width="4" />
        </g>
      {/each}
    </g>
  </svg>

  <div class="absolute bottom-4 right-4 flex gap-2 rounded-full bg-white/90 p-2 shadow-lg backdrop-blur">
    <button
      class="grid h-10 w-10 place-items-center rounded-full bg-slate-900 text-lg font-bold text-white"
      type="button"
      aria-label="Zoomer"
      on:click={() => zoomAt(0.76)}
    >
      +
    </button>
    <button
      class="grid h-10 w-10 place-items-center rounded-full bg-slate-100 text-lg font-bold text-slate-900"
      type="button"
      aria-label="Dézoomer"
      on:click={() => zoomAt(1.24)}
    >
      −
    </button>
    <button
      class="rounded-full bg-slate-100 px-4 text-sm font-semibold text-slate-900"
      type="button"
      on:click={resetZoom}
    >
      Reset
    </button>
  </div>
</div>
