package com.elykia.octopus.feature.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField

/**
 * Group Item 编辑器行组件
 */
@Composable
fun GroupItemEditorRow(
    item: GroupItem,
    channel: Channel?,
    onChange: (GroupItem) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    var priorityText by remember(item.id, item.groupId, item.channelId, item.modelName) {
        mutableStateOf(item.priority.toString())
    }
    var weightText by remember(item.id, item.groupId, item.channelId, item.modelName) {
        mutableStateOf(item.weight.toString())
    }

    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppInfoChip(
                text = stringResource(
                    R.string.group_item_channel_summary,
                    item.channelId,
                    channel?.name ?: stringResource(R.string.group_item_channel_fallback, item.channelId),
                ),
                icon = AppMiuixIcons.Channel,
            )
            TextField(
                value = item.modelName,
                onValueChange = { onChange(item.copy(modelName = it)) },
                label = stringResource(R.string.group_item_model_name_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = priorityText,
                    onValueChange = { value ->
                        parseGroupItemNumberInput(value)?.let { input ->
                            priorityText = input.displayValue
                            onChange(item.copy(priority = input.value))
                        }
                    },
                    label = stringResource(R.string.group_item_priority_hint),
                    singleLine = true,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                TextField(
                    value = weightText,
                    onValueChange = { value ->
                        parseGroupItemNumberInput(value)?.let { input ->
                            weightText = input.displayValue
                            onChange(item.copy(weight = input.value))
                        }
                    },
                    label = stringResource(R.string.group_item_weight_hint),
                    singleLine = true,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            TextButton(text = stringResource(R.string.common_delete), enabled = enabled, onClick = onDelete)
        }
    }
}
