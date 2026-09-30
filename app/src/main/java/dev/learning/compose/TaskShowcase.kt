package dev.learning.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private data class DemoTask(val id: Int, val title: String, val done: Boolean)

private val taskSaver = listSaver<SnapshotStateList<DemoTask>, Any>(
    save = { tasks -> tasks.flatMap { listOf(it.id, it.title, it.done) } },
    restore = { values ->
        mutableStateListOf<DemoTask>().apply {
            values.chunked(3).forEach { add(DemoTask(it[0] as Int, it[1] as String, it[2] as Boolean)) }
        }
    }
)

/** A working result to explore before writing the lesson 07 exercise. */
@Composable
internal fun TaskShowcase(modifier: Modifier = Modifier) {
    val tasks = rememberSaveable(saver = taskSaver) {
        mutableStateListOf(
            DemoTask(1, "阅读 Compose 文档", true),
            DemoTask(2, "写一个状态练习", false)
        )
    }
    var nextId by rememberSaveable { mutableIntStateOf(3) }
    var input by remember { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val visible = tasks.filter { filter == 0 || it.done == (filter == 2) }
    val completed = tasks.count { it.done }

    fun addTask() {
        val title = input.trim()
        if (title.isNotEmpty()) {
            tasks.add(DemoTask(nextId++, title, false))
            input = ""
        }
    }

    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("我的任务", style = MaterialTheme.typography.headlineMedium)
        Text("试着添加 A、B，勾选 A，再切换筛选并删除它。观察统计和列表怎样一起变化。")
        Text("已完成 $completed / 总共 ${tasks.size}", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("新任务") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { addTask() })
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { addTask() }, enabled = input.isNotBlank()) { Text("添加任务") }
            TextButton(onClick = {
                tasks.clear()
                input = ""
                filter = 0
                nextId = 1
            }) { Text("清空示范") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("全部", "未完成", "已完成").forEachIndexed { index, label ->
                FilterChip(selected = filter == index, onClick = { filter = index }, label = { Text(label) })
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (visible.isEmpty()) {
                item {
                    Text(
                        if (tasks.isEmpty()) "还没有任务，试着添加第一条。" else "当前筛选下没有任务。",
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }
            items(visible, key = { it.id }) { task ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(8.dp)) {
                        Checkbox(
                            checked = task.done,
                            onCheckedChange = { done ->
                                val position = tasks.indexOfFirst { it.id == task.id }
                                if (position >= 0) tasks[position] = task.copy(done = done)
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "${task.title}，${if (task.done) "已完成" else "未完成"}"
                            }
                        )
                        Text(task.title, Modifier.weight(1f).padding(top = 12.dp))
                        TextButton(
                            onClick = { tasks.removeAll { it.id == task.id } },
                            modifier = Modifier.semantics { contentDescription = "删除任务 ${task.title}" }
                        ) { Text("删除") }
                    }
                }
            }
        }
    }
}
