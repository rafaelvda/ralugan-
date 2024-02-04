package com.ralugan.raluganplus.dataclass

data class RaluganPlus(
    val title: String,
    val imageUrl: String
) {

    constructor() : this("", "")
}