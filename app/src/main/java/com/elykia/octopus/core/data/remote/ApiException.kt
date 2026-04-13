package com.elykia.octopus.core.data.remote

class ApiException(
    val code: Int,
    override val message: String,
) : RuntimeException(message)
