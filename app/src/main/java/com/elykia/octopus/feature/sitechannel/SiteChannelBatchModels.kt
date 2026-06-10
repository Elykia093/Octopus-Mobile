package com.elykia.octopus.feature.sitechannel

import com.elykia.octopus.core.data.model.SiteChannelModel
import com.elykia.octopus.core.data.model.SiteModelDisableUpdateRequest
import com.elykia.octopus.core.data.model.SiteModelRouteUpdateRequest

internal fun buildBulkModelRouteRequests(
    groupKey: String,
    models: List<SiteChannelModel>,
    routeType: String,
): List<SiteModelRouteUpdateRequest> {
    val normalizedGroupKey = groupKey.trim()
    val normalizedRouteType = routeType.trim()
    if (normalizedGroupKey.isBlank() || normalizedRouteType.isBlank()) return emptyList()

    return models
        .asSequence()
        .map { model -> model to model.modelName.trim() }
        .filter { (model, modelName) -> modelName.isNotBlank() && !model.disabled && model.routeType != normalizedRouteType }
        .distinctBy { (_, modelName) -> modelName }
        .map { (_, modelName) ->
            SiteModelRouteUpdateRequest(
                groupKey = normalizedGroupKey,
                modelName = modelName,
                routeType = normalizedRouteType,
            )
        }
        .toList()
}

internal fun buildBulkModelDisabledRequests(
    groupKey: String,
    models: List<SiteChannelModel>,
    disabled: Boolean,
): List<SiteModelDisableUpdateRequest> {
    val normalizedGroupKey = groupKey.trim()
    if (normalizedGroupKey.isBlank()) return emptyList()

    return models
        .asSequence()
        .map { model -> model to model.modelName.trim() }
        .filter { (model, modelName) -> modelName.isNotBlank() && model.disabled != disabled }
        .distinctBy { (_, modelName) -> modelName }
        .map { (_, modelName) ->
            SiteModelDisableUpdateRequest(
                groupKey = normalizedGroupKey,
                modelName = modelName,
                disabled = disabled,
            )
        }
        .toList()
}
