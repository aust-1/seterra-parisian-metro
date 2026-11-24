package com.parismetro.quiz.ui.map

/**
 * Paris's 20 arrondissement boundaries, for geographic context on the map.
 *
 * Built from the official Paris open data boundaries (opendata.paris.fr, dataset
 * "arrondissements", OSM/IGN-sourced polygons), reprojected into this app's map-space
 * coordinates by fitting an affine transform (a linear scale per axis, matching the station
 * generation's own lon/lat projection) against seven stations whose real-world coordinates and
 * in-dataset x/y are both known - the same projection the station data itself went through,
 * not a separate guess. Since that projection became isotropic (same scale on both axes, see
 * `scripts/generate-metro-data.py`), the x values were carried over by the same exact
 * per-axis map as the stations' (x' = 73.59 + (x - 72) × 0.59141). Each ring is
 * simplified (Douglas-Peucker) down from raw survey detail to the handful of points needed to
 * read clearly at this map's scale.
 */
val ARRONDISSEMENTS: List<Pair<Int, List<Pair<Float, Float>>>> = listOf(
    1 to listOf(371f to 413f, 379f to 421f, 438f to 442f, 420f to 484f, 399f to 464f, 350f to 444f, 363f to 415f, 371f to 413f),
    2 to listOf(440f to 437f, 438f to 442f, 379f to 421f, 371f to 413f, 405f to 404f, 438f to 412f, 447f to 416f, 440f to 437f),
    3 to listOf(475f to 424f, 483f to 443f, 489f to 476f, 436f to 448f, 447f to 416f, 475f to 424f),
    4 to listOf(489f to 476f, 491f to 488f, 483f to 514f, 477f to 519f, 462f to 504f, 420f to 484f, 436f to 448f, 489f to 476f),
    5 to listOf(477f to 519f, 482f to 524f, 469f to 547f, 440f to 561f, 397f to 548f, 420f to 484f, 462f to 504f, 477f to 519f),
    6 to listOf(420f to 484f, 397f to 548f, 338f to 516f, 372f to 494f, 386f to 465f, 385f to 460f, 399f to 464f, 420f to 484f),
    7 to listOf(292f to 497f, 260f to 465f, 282f to 444f, 344f to 440f, 385f to 460f, 386f to 465f, 372f to 494f, 330f to 520f, 321f to 511f, 311f to 515f, 292f to 497f),
    8 to listOf(365f to 415f, 350f to 444f, 295f to 442f, 276f to 395f, 286f to 376f, 369f to 352f, 369f to 396f, 365f to 415f),
    9 to listOf(405f to 359f, 434f to 351f, 429f to 409f, 405f to 404f, 365f to 415f, 369f to 396f, 369f to 352f, 376f to 348f, 405f to 359f),
    10 to listOf(478f to 348f, 493f to 355f, 493f to 377f, 513f to 403f, 475f to 424f, 429f to 409f, 434f to 351f, 478f to 348f),
    11 to listOf(569f to 483f, 577f to 510f, 518f to 499f, 491f to 488f, 483f to 443f, 475f to 424f, 513f to 403f, 543f to 443f, 550f to 464f, 564f to 472f, 569f to 483f),
    12 to listOf(620f to 575f, 644f to 566f, 636f to 535f, 644f to 526f, 653f to 539f, 688f to 542f, 690f to 526f, 698f to 527f, 698f to 520f, 763f to 536f, 782f to 561f, 781f to 572f, 769f to 587f, 767f to 602f, 772f to 603f, 762f to 640f, 750f to 649f, 688f to 644f, 689f to 639f, 679f to 639f, 666f to 619f, 613f to 614f, 588f to 593f, 551f to 610f, 477f to 519f, 483f to 514f, 494f to 487f, 518f to 499f, 627f to 517f, 617f to 568f, 612f to 574f, 620f to 575f),
    13 to listOf(507f to 555f, 551f to 610f, 474f to 653f, 452f to 654f, 442f to 642f, 418f to 654f, 420f to 638f, 410f to 619f, 412f to 554f, 443f to 560f, 469f to 547f, 482f to 524f, 507f to 555f),
    14 to listOf(388f to 544f, 412f to 554f, 410f to 619f, 420f to 638f, 418f to 654f, 382f to 649f, 384f to 644f, 378f to 641f, 294f to 613f, 348f to 544f, 353f to 546f, 352f to 542f, 362f to 530f, 388f to 544f),
    15 to listOf(288f to 492f, 311f to 515f, 321f to 511f, 330f to 520f, 338f to 516f, 362f to 530f, 352f to 542f, 353f to 546f, 348f to 544f, 294f to 613f, 268f to 604f, 230f to 580f, 211f to 600f, 197f to 601f, 195f to 584f, 203f to 577f, 195f to 570f, 182f to 574f, 223f to 510f, 260f to 465f, 288f to 492f),
    16 to listOf(216f to 375f, 231f to 374f, 276f to 396f, 295f to 442f, 282f to 444f, 269f to 455f, 223f to 510f, 182f to 574f, 160f to 570f, 150f to 551f, 149f to 533f, 152f to 522f, 125f to 513f, 114f to 501f, 70f to 486f, 82f to 434f, 93f to 415f, 117f to 405f, 133f to 384f, 161f to 394f, 169f to 367f, 216f to 375f),
    17 to listOf(276f to 395f, 231f to 374f, 235f to 354f, 258f to 331f, 349f to 275f, 378f to 274f, 364f to 335f, 369f to 353f, 286f to 376f, 276f to 395f),
    18 to listOf(481f to 343f, 478f to 348f, 405f to 359f, 376f to 348f, 369f to 352f, 364f to 335f, 378f to 274f, 494f to 270f, 494f to 294f, 498f to 299f, 493f to 305f, 481f to 343f),
    19 to listOf(549f to 273f, 567f to 288f, 575f to 307f, 577f to 344f, 583f to 355f, 607f to 367f, 611f to 375f, 588f to 386f, 552f to 388f, 513f to 403f, 494f to 378f, 493f to 356f, 478f to 348f, 498f to 299f, 494f to 294f, 493f to 271f, 549f to 273f),
    20 to listOf(617f to 388f, 627f to 517f, 577f to 510f, 576f to 496f, 564f to 472f, 550f to 464f, 543f to 443f, 513f to 403f, 552f to 388f, 588f to 386f, 611f to 375f, 617f to 388f)
)
