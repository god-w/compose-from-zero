# 07 · 阶段项目：从一份数据做出任务清单

**本章目标：**把前六章串起来，完成添加、勾选、删除、筛选与旋转恢复，并为后续课程保留清晰接口。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 07 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/07.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 07 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/07.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 动手前，先写出唯一的数据来源

界面有输入、统计、过滤、列表四块，**不能因此保存四份任务列表**。tasks 是完整任务源，visible 是根据 filter 推导的结果，completed 是计数。input 是正在编辑的草稿。

本章文件都放在 `app/src/main/java/dev/learning/compose/`，包名统一 `dev.learning.compose`。新增模型和无状态 UI，最后只接入 `Exercises.kt` 的 `CapstoneExercise`。不要覆盖已有 TaskShowcase；它是可对照的示范。

## 2. 第一步：先把模型与规则写成普通 Kotlin

新建 `TaskModels.kt`：

```kotlin
package dev.learning.compose

data class Task(val id: Int, val title: String, val done: Boolean = false)

// 0 全部；1 未完成；2 已完成
fun visibleTasks(tasks: List<Task>, filter: Int): List<Task> = when (filter) {
    1 -> tasks.filterNot { it.done }
    2 -> tasks.filter { it.done }
    else -> tasks
}

fun normalizedTitle(raw: String): String? = raw.trim().takeIf { it.isNotEmpty() }
```

先在纸上用 A 未完成、B 已完成推导三个结果。再用 Kotlin 表达式核对。`visibleTasks` 不修改传入列表；`normalizedTitle("   ")` 应为 null。

## 3. 第二步：写不拥有业务状态的界面

新建 `TaskContent.kt`，导入如下。本章采用分组导入便于完整复制；Android Studio 可帮助整理。

```kotlin
package dev.learning.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
```

先读函数参数：前四个是当前数据，其余是用户动作。完成一次添加时，UI 只调用 onAdd，不私自修改 tasks。

```kotlin
@Composable
fun TaskContent(
    tasks: List<Task>,
    input: String,
    filter: Int,
    onInput: (String) -> Unit,
    onAdd: () -> Unit,
    onFilter: (Int) -> Unit,
    onToggle: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = visibleTasks(tasks, filter)
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("已完成 ${tasks.count { it.done }} / 总共 ${tasks.size}")
        OutlinedTextField(value = input, onValueChange = onInput,
            label = { Text("新任务") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = onAdd, enabled = input.isNotBlank()) { Text("添加") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("全部", "未完成", "已完成").forEachIndexed { index, label ->
                FilterChip(selected = filter == index, onClick = { onFilter(index) },
                    label = { Text(label) })
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            if (visible.isEmpty()) {
                item { Text(if (tasks.isEmpty()) "还没有任务" else "当前筛选下没有任务") }
            }
            items(visible, key = { it.id }) { task ->
                Row(Modifier.fillMaxWidth()) {
                    Checkbox(checked = task.done, onCheckedChange = { onToggle(task.id) })
                    Text(task.title, Modifier.weight(1f).padding(top = 12.dp))
                    TextButton(onClick = { onDelete(task.id) }) { Text("删除") }
                }
            }
        }
    }
}
```

暂时用固定的两项数据和空回调调用它，确认静态布局。再加入状态，避免把布局错误和业务错误混在一起。

## 4. 第三步：连接状态与动作

先使用 remember 做基本功能。下列是最终含旋转恢复的版本，先写对事件，再补 Saver。

在 `TaskModels.kt` 加入两个 import 与保存器：

```kotlin
import androidx.compose.runtime.saveable.listSaver

val TaskListSaver = listSaver<List<Task>, Any>(
    save = { tasks -> tasks.flatMap { listOf(it.id, it.title, it.done) } },
    restore = { values -> values.chunked(3).map {
        Task(it[0] as Int, it[1] as String, it[2] as Boolean)
    } }
)
```

注意 import 放在 package 后、声明前。Saver 把小练习列表变成可保存的基本值；它不适合无限增长的真实任务库。第 16 章会替换成 Room。

`Exercises.kt` 新增 runtime 的 getValue、setValue、mutableStateOf、mutableIntStateOf 和 saveable.rememberSaveable 导入，替换函数：

```kotlin
@Composable
private fun CapstoneExercise() {
    var tasks by rememberSaveable(stateSaver = TaskListSaver) {
        mutableStateOf(emptyList<Task>())
    }
    var input by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var nextId by rememberSaveable { mutableIntStateOf(1) }
    TaskContent(
        tasks = tasks, input = input, filter = filter,
        onInput = { input = it },
        onAdd = {
            normalizedTitle(input)?.let { title ->
                tasks = tasks + Task(nextId++, title)
                input = ""
            }
        },
        onFilter = { filter = it },
        onToggle = { id -> tasks = tasks.map {
            if (it.id == id) it.copy(done = !it.done) else it
        } },
        onDelete = { id -> tasks = tasks.filterNot { it.id == id } }
    )
}
```

每加入一种动作运行一次。这里 onToggle 接收身份，不接收下标，因此过滤后仍能操作正确项目。

## 5. 看图走一次边界路径

![第 07 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/07-experiment.png)

教学示意：完成 A，再按未完成过滤，A 仍在完整数据里，只是不在 visible 中。删除才移除数据。

![第 7 课真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/07-running.png)

示范里的草稿策略与本章参考不同：示范会清空旋转前的草稿，本章把草稿也保存。恢复行为是产品选择，不是所有状态都必须统一保存。

## 6. 验收脚本：逐行操作，不凭印象

| 操作 | 预期 |
| --- | --- |
| 空格输入 | 按钮禁用；业务规则也拒绝空标题 |
| 添加 A、B | 总共 2，已完成 0 |
| 完成 A | 已完成 1 / 总共 2 |
| 过滤未完成 | 只看见 B，统计仍为 1/2 |
| 过滤已完成并删除 A | 当前为空，统计 0/1 |
| 切回全部 | 只有 B，A 不再出现 |
| 旋转 | B 和当前筛选恢复 |

返回首页后重新进入的恢复行为还与课程导航保存作用域有关；不要把这当作长期存储承诺。

## 7. 理解检查与迁移

1. 为什么不保存 visible 的第二份可变状态？
2. 为什么需要 nextId，而不是 tasks.size + 1？
3. 添加按钮禁用后，为什么业务方法还要检查空标题？

<details><summary>解析</summary>

1. visible 可以从 tasks 和 filter 算出；两份可写列表会在删除、完成时不同步。
2. 删除后数量会回退，数量不是唯一身份；单调序号避免本轮重复。
3. 业务动作未来可能由键盘、测试或别的入口调用；规则应由数据所有者保证。

</details>

**独立挑战：**新增编辑标题动作，先写“空标题拒绝、合法标题去空格、id 保持不变”的三条预期，再实现。不要新增第二份已完成列表。

| 症状 | 检查 |
| --- | --- |
| 切筛选后总数变化 | 是否用 visible 计算总数？ |
| 删除了错误任务 | 是否把过滤列表下标当业务身份？ |
| Saver 崩溃 | 保存与还原的顺序、类型是否一致？ |
| 列表没有空间 | LazyColumn 是否在有界 Column 内使用 weight？ |

下一章保留 TaskContent，只替换状态持有者。官方核对：[状态保存](https://developer.android.com/develop/ui/compose/state-saving)。

---

上一章：[第 06 章](06-effects.md) · 下一章：[第 08 章](08-architecture.md)
