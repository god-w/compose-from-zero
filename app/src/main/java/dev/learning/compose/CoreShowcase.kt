package dev.learning.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Interactive observations for layout, stable list identity, and effect lifecycle. */
@Composable
internal fun CoreShowcase(lesson: Int, modifier: Modifier = Modifier) {
    when (lesson) {
        3 -> LayoutShowcase(modifier)
        4 -> ListShowcase(modifier)
        5 -> EffectShowcase(modifier)
    }
}

@Composable
private fun LayoutShowcase(modifier: Modifier) {
    var backgroundFirst by rememberSaveable { mutableStateOf(true) }
    val color = MaterialTheme.colorScheme.primaryContainer
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("先预测：交换 padding 和 background 后，色块会变大还是变小？", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Column(Modifier.padding(start = 16.dp)) {
                    Text("Compose 学习者", style = MaterialTheme.typography.titleMedium)
                    Text("头像和两行文字：外层 Row，内层 Column")
                }
            }
        }
        Text(
            "同一块内容，不同 Modifier 顺序",
            modifier = if (backgroundFirst) Modifier.background(color).padding(16.dp)
            else Modifier.padding(16.dp).background(color)
        )
        Button(onClick = { backgroundFirst = !backgroundFirst }) {
            Text(if (backgroundFirst) "当前：背景 → 内边距" else "当前：内边距 → 背景")
        }
        Text("观察：Modifier 按书写顺序包裹后续内容，顺序改变了背景覆盖范围。回到练习，亲自交换顺序。")
    }
}

@Composable
private fun ListShowcase(modifier: Modifier) {
    val ids = remember { mutableStateListOf(1, 2, 3) }
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("先勾选 B，再删除 A：勾选会留在 B 身上吗？", style = MaterialTheme.typography.titleMedium)
        Text("每行的勾选是局部状态。列表用任务 ID 作为 key，让状态跟随任务身份。")
        TextButton(onClick = { ids.clear(); ids.addAll(listOf(1, 2, 3)) }) { Text("重置列表") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ids, key = { it }) { id ->
                var checked by rememberSaveable { mutableStateOf(false) }
                Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = checked, onCheckedChange = { checked = it })
                        Text("任务 ${'A' + id - 1}", Modifier.weight(1f))
                        TextButton(onClick = { ids.remove(id) }) { Text("删除") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectShowcase(modifier: Modifier) {
    var seconds by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var running by remember { mutableStateOf(true) }
    LaunchedEffect(running) {
        if (running) {
            while (true) {
                delay(1_000)
                seconds++
            }
        }
    }
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("先预测：暂停后秒数还会变化吗？离开页面时循环应继续吗？", style = MaterialTheme.typography.titleMedium)
        Text("$seconds 秒", style = MaterialTheme.typography.displayMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { running = !running }) { Text(if (running) "暂停" else "继续") }
            TextButton(onClick = { seconds = 0 }) { Text("清零") }
        }
        Text("观察：LaunchedEffect 随当前页面进入组合而启动；离开后自动取消。暂停时 key 改变，旧循环取消。")
    }
}
