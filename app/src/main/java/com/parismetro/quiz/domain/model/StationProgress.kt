package com.parismetro.quiz.domain.model

/**
 * A player's lifetime record on one station, as far as the domain needs it: how many times it
 * was asked ([seen]) and how many of those were answered right on the first try ([firstTry]).
 */
data class StationProgress(val stationId: String, val seen: Int, val firstTry: Int)
