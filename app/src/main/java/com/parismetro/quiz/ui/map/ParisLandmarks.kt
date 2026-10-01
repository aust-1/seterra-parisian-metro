package com.parismetro.quiz.ui.map

/**
 * Waypoints (in the same map-space coordinates as station x/y) tracing the Seine and the
 * Périphérique, so the schematic map reads as "Paris" instead of an abstract graph.
 *
 * Both are built from real stations that sit directly on them - the Seine from stations named
 * after its bridges and quais ("Pont Neuf", "Cité", "Bir-Hakeim"...), the ring road from the
 * 23 "Porte de ..." stations that line it - rather than from separately sourced geographic
 * data. That keeps them perfectly aligned with the station layout with zero calibration: same
 * source, same projection, by construction.
 */
val SEINE_WAYPOINTS: List<Pair<Float, Float>> = listOf(
    996f to 629f, // Charenton - Écoles
    823f to 545f, // Bercy
    753f to 535f, // Gare d'Austerlitz
    737f to 495f, // Sully - Morland
    664f to 479f, // Cité
    664f to 461f, // Châtelet
    637f to 463f, // Pont Neuf
    629f to 451f, // Louvre - Rivoli
    574f to 435f, // Tuileries
    546f to 428f, // Concorde
    501f to 455f, // Invalides
    434f to 435f, // Alma - Marceau
    365f to 441f, // Trocadéro
    376f to 483f, // Bir-Hakeim
    319f to 518f, // Javel - André Citroën
    295f to 514f, // Mirabeau
    275f to 514f, // Église d'Auteuil
    249f to 511f, // Michel-Ange - Auteuil
    83f to 592f  // Pont de Sèvres
)

/** The 23 "Porte de ..." stations, in angular order around their centroid (a closed loop). */
val PERIPHERIQUE_WAYPOINTS: List<Pair<Float, Float>> = listOf(
    318f to 404f, // Porte Dauphine
    341f to 376f, // Porte Maillot
    389f to 341f, // Porte de Champerret
    496f to 302f, // Porte de Clichy
    573f to 288f, // Porte de Saint-Ouen
    651f to 288f, // Porte de Clignancourt
    723f to 289f, // Porte de la Chapelle
    856f to 287f, // Porte de la Villette
    887f to 329f, // Porte de Pantin
    960f to 380f, // Porte des Lilas
    970f to 436f, // Porte de Bagnolet
    980f to 485f, // Porte de Montreuil
    966f to 513f, // Porte de Vincennes
    956f to 568f, // Porte Dorée
    939f to 574f, // Porte de Charenton
    774f to 629f, // Porte d'Ivry
    751f to 635f, // Porte de Choisy
    725f to 639f, // Porte d'Italie
    556f to 620f, // Porte d'Orléans
    453f to 602f, // Porte de Vanves
    369f to 579f, // Porte de Versailles
    214f to 555f, // Porte de Saint-Cloud
    221f to 509f  // Porte d'Auteuil
)
