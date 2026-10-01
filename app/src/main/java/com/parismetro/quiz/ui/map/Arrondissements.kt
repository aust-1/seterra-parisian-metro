package com.parismetro.quiz.ui.map

/**
 * Paris's 20 arrondissement boundaries, for geographic context on the map.
 *
 * Built from the official Paris open data boundaries (opendata.paris.fr, dataset
 * "arrondissements", OSM/IGN-sourced polygons), reprojected into this app's map-space
 * coordinates by fitting an affine transform (independent linear scale per axis, matching
 * [com.parismetro.quiz.domain.model] station generation's own lon/lat normalization) against
 * seven stations whose real-world coordinates and in-dataset x/y are both known - the same
 * projection the station data itself went through, not a separate guess. Each ring is
 * simplified (Douglas-Peucker) down from raw survey detail to the handful of points needed to
 * read clearly at this map's scale.
 */
val ARRONDISSEMENTS: List<Pair<Int, List<Pair<Float, Float>>>> = listOf(
    1 to listOf(575f to 413f, 588f to 421f, 688f to 442f, 657f to 484f, 622f to 464f, 540f to 444f, 561f to 415f, 575f to 413f),
    2 to listOf(691f to 437f, 688f to 442f, 588f to 421f, 575f to 413f, 633f to 404f, 688f to 412f, 704f to 416f, 691f to 437f),
    3 to listOf(751f to 424f, 765f to 443f, 774f to 476f, 684f to 448f, 704f to 416f, 751f to 424f),
    4 to listOf(774f to 476f, 777f to 488f, 764f to 514f, 754f to 519f, 729f to 504f, 657f to 484f, 684f to 448f, 774f to 476f),
    5 to listOf(754f to 519f, 762f to 524f, 741f to 547f, 692f to 561f, 618f to 548f, 657f to 484f, 729f to 504f, 754f to 519f),
    6 to listOf(657f to 484f, 618f to 548f, 519f to 516f, 577f to 494f, 601f to 465f, 599f to 460f, 622f to 464f, 657f to 484f),
    7 to listOf(442f to 497f, 388f to 465f, 425f to 444f, 530f to 440f, 599f to 460f, 601f to 465f, 577f to 494f, 505f to 520f, 490f to 511f, 474f to 515f, 442f to 497f),
    8 to listOf(565f to 415f, 540f to 444f, 446f to 442f, 414f to 395f, 431f to 376f, 571f to 352f, 571f to 396f, 565f to 415f),
    9 to listOf(633f to 359f, 681f to 351f, 673f to 409f, 633f to 404f, 565f to 415f, 571f to 396f, 571f to 352f, 584f to 348f, 633f to 359f),
    10 to listOf(755f to 348f, 781f to 355f, 782f to 377f, 815f to 403f, 751f to 424f, 673f to 409f, 681f to 351f, 755f to 348f),
    11 to listOf(910f to 483f, 924f to 510f, 823f to 499f, 777f to 488f, 765f to 443f, 751f to 424f, 815f to 403f, 866f to 443f, 877f to 464f, 901f to 472f, 910f to 483f),
    12 to listOf(996f to 575f, 1037f to 566f, 1023f to 535f, 1037f to 526f, 1051f to 539f, 1111f to 542f, 1114f to 526f, 1127f to 527f, 1128f to 520f, 1238f to 536f, 1270f to 561f, 1268f to 572f, 1247f to 587f, 1245f to 602f, 1253f to 603f, 1236f to 640f, 1216f to 649f, 1111f to 644f, 1112f to 639f, 1096f to 639f, 1073f to 619f, 984f to 614f, 942f to 593f, 880f to 610f, 754f to 519f, 764f to 514f, 783f to 487f, 823f to 499f, 1007f to 517f, 990f to 568f, 983f to 574f, 996f to 575f),
    13 to listOf(805f to 555f, 880f to 610f, 749f to 653f, 712f to 654f, 695f to 642f, 654f to 654f, 657f to 638f, 641f to 619f, 644f to 554f, 696f to 560f, 741f to 547f, 762f to 524f, 805f to 555f),
    14 to listOf(604f to 544f, 644f to 554f, 641f to 619f, 657f to 638f, 654f to 654f, 594f to 649f, 597f to 644f, 586f to 641f, 444f to 613f, 536f to 544f, 545f to 546f, 543f to 542f, 559f to 530f, 604f to 544f),
    15 to listOf(435f to 492f, 474f to 515f, 490f to 511f, 505f to 520f, 519f to 516f, 559f to 530f, 543f to 542f, 545f to 546f, 536f to 544f, 444f to 613f, 400f to 604f, 336f to 580f, 305f to 600f, 280f to 601f, 278f to 584f, 291f to 577f, 278f to 570f, 256f to 574f, 325f to 510f, 388f to 465f, 435f to 492f),
    16 to listOf(312f to 375f, 339f to 374f, 414f to 396f, 446f to 442f, 425f to 444f, 402f to 455f, 325f to 510f, 256f to 574f, 218f to 570f, 201f to 551f, 199f to 533f, 205f to 522f, 159f to 513f, 141f to 501f, 66f to 486f, 86f to 434f, 104f to 415f, 145f to 405f, 172f to 384f, 219f to 394f, 234f to 367f, 312f to 375f),
    17 to listOf(414f to 395f, 339f to 374f, 345f to 354f, 384f to 331f, 538f to 275f, 586f to 274f, 563f to 335f, 571f to 353f, 431f to 376f, 414f to 395f),
    18 to listOf(761f to 343f, 755f to 348f, 632f to 359f, 584f to 348f, 572f to 352f, 563f to 335f, 586f to 274f, 783f to 270f, 783f to 294f, 790f to 299f, 781f to 305f, 761f to 343f),
    19 to listOf(876f to 273f, 907f to 288f, 919f to 307f, 924f to 344f, 933f to 355f, 974f to 367f, 981f to 375f, 941f to 386f, 881f to 388f, 815f to 403f, 783f to 378f, 782f to 356f, 755f to 348f, 790f to 299f, 783f to 294f, 782f to 271f, 876f to 273f),
    20 to listOf(991f to 388f, 1007f to 517f, 924f to 510f, 921f to 496f, 901f to 472f, 877f to 464f, 866f to 443f, 815f to 403f, 881f to 388f, 941f to 386f, 981f to 375f, 991f to 388f)
)
