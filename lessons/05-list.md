# 05 · 列表与 key：位置变了，身份变了吗？

**本章目标：**用 LazyColumn 显示可删除列表，理解可观察列表和稳定身份。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 05 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/05.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 05 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/05.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从 RecyclerView 迁移已有认识

RecyclerView 让可见区域按需展示项目。LazyColumn 也用于按需组合和布局列表内容。你描述 items，不再写 Adapter 和 ViewHolder；但“列表数据”“项目身份”“项目状态”仍需自己设计。

普通 Column 加循环适合小量内容；大量可滚动条目用 LazyColumn。不要给 LazyColumn 再套同方向、无限高度的滚动容器。

## 2. 先辨认三种编号

| 值 | 含义 | 删除第一项后 |
| --- | --- | --- |
| 下标 index | 现在位于第几个位置 | 后面的项目会移动 |
| 业务 id | 哪个任务 | 幸存项目保持不变 |
| Lazy key | 组合追踪项目使用的身份 | 应与稳定业务身份对应 |

A、B、C 的 id 分别是 10、20、30。删除 A，B 位置从 1 到 0，id 仍为 20。稳定 key 帮助列表把状态与项目对应。它不是持久化，也不保证业务 id 自动生成。

## 3. 写一个只显示、不修改的列表

修改 `Exercises.kt` 的 `ListExercise`，新增：

```kotlin
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
```

在文件顶层添加本章模型，不要放到 Composable 函数体里：

```kotlin
private data class ListTask(val id: Int, val title: String)
```

```kotlin
@Composable
private fun ListExercise() {
    val tasks = listOf(ListTask(10, "A"), ListTask(20, "B"), ListTask(30, "C"))
    LazyColumn {
        items(tasks, key = { it.id }) { task ->
            Text("${task.id} · ${task.title}")
        }
    }
}
```

先运行并确认三项，不急着添加按钮。这里的 items 来自 `foundation.lazy.items`，不是 Row/Column 的普通内容 API。

## 4. 第二步：只加入删除

```kotlin
@Composable
private fun ListExercise() {
    var tasks by remember {
        mutableStateOf(listOf(ListTask(10, "A"), ListTask(20, "B"), ListTask(30, "C")))
    }
    LazyColumn {
        items(tasks, key = { it.id }) { task ->
            Column {
                Text("${task.id} · ${task.title}")
                Button(onClick = { tasks = tasks.filterNot { it.id == task.id } }) {
                    Text("删除 ${task.title}")
                }
            }
        }
    }
}
```

先预测：删 A 后，B 的文字会变成 `10 · B` 吗？不会。你更换列表值，Compose 观察该值；每项数据里的 id 不随位置变。

不要使用 `remember { mutableListOf(...) }` 后直接 remove 来期待 UI 自动更新。本例使用 `State<List<...>>` 配合新的列表值；另一种方案是可观察的 `mutableStateListOf`，不要混淆普通列表与可观察列表。

## 5. 第三步：让身份问题变得可见

给每项加一个独立计数。需新增 `mutableIntStateOf` 导入，在 items 内容里先写：

```kotlin
var taps by remember { mutableIntStateOf(0) }
Column {
    Text("${task.title}：局部点击 $taps 次")
    Button(onClick = { taps++ }) { Text("点 ${task.title}") }
    Button(onClick = { tasks = tasks.filterNot { it.id == task.id } }) { Text("删除") }
}
```

把 B 点到 2，再删 A，B 应仍为 2。现在临时移除 key，从初始数据重新运行重复操作，观察局部状态是否按位置错配；具体可见表现还与组合结构有关。恢复稳定 key，不依赖无 key 版本的偶然表现。

![第 05 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/05-experiment.png)

这是身份实验示意，关注 B 移动后的归属。key 需要在同一列表里唯一；本例 Int 也适合后续可保存状态的身份使用。

![第 5 课真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/05-running.png)

## 6. 先回答再看解析

1. 用标题作 key 会有什么风险？
2. `tasks = tasks.filterNot { ... }` 为什么会更新？
3. 稳定 key 能让任务在强制停止后还存在吗？

<details><summary>答案</summary>

1. 标题可能重复，也可能被编辑；身份会冲突或变化。用稳定、唯一的业务 id。
2. tasks 是可观察状态容器的值；赋新列表值触发相应更新。
3. 不能。key 解决列表内容的身份对应，磁盘持久化另有机制。

</details>

## 7. 迁移任务与排错

增加“添加”按钮，每次生成新的、未被使用的 id。不要用 `tasks.size` 当 id：删除后再添加可能重复。内存实验可以单调递增计数；长期数据交给数据库或合适的 ID 策略。

| 现象 | 检查 |
| --- | --- |
| 删除后界面不变 | 是否改了普通可变列表而没有可观察更新？ |
| 项目状态跑到别人身上 | key 是否用下标或发生重复？ |
| 无限高度约束报错 | LazyColumn 上层是否同方向无限滚动？ |
| 旋转后列表回到初始值 | 本章使用 remember；第 7、16 章补恢复和持久化 |

完成标准：删除、添加、身份实验都通过；能分别解释位置、id、key。官方核对：[Lazy 列表与 key](https://developer.android.com/develop/ui/compose/lists)。

---

上一章：[第 04 章](04-layout.md) · 下一章：[第 06 章](06-effects.md)
