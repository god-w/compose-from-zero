# 05 · 列表与 key

**目标**：写可增删的懒加载列表，理解项目身份与不可变状态更新。预计 75 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 05 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/05.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 05 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/05.png)

**看图先猜**：A、B、C 删除 A 后，原来下标 1 的 B 变成下标几？它的身份是否改变？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先做只显示三项的 `LazyColumn`，每项显示 `id` 和标题。
2. 用稳定 `id` 作为 `key`，加入删除 A 的按钮。
3. 给 B 加可观察的局部状态，删除 A 后确认状态仍跟着 B。

**停下来验收**：B 的位置变了，但 `id` 与它自己的状态没有错配。

**若结果不同**：如果删除后状态跑到别的行，检查是否用了下标当 key。
<!-- visual-end -->

## 讲解

`LazyColumn` 只组合可见区域附近的内容，适合长列表。`items(list, key = { it.id })` 给每项稳定身份；没有稳定 key 时，插入或删除可能让项内部状态跟错位置。状态更新不要原地改普通 `MutableList`；用新的列表值替换，Compose 才容易观察。

## 任务

在 `ListExercise`：

1. 定义 `data class Task(val id: Int, val title: String)`。
2. 保存列表和下一个 id；按钮每次添加“任务 N”。
3. 用 `LazyColumn` 展示；每行有标题和删除按钮。`items(..., key = { it.id })`。
4. 尝试先增加 3 项，再删中间项，检查其他项稳定。

## 验收

- 添加和删除结果正确；列表为空时有空状态文字。
- 能解释为什么不能用列表下标当稳定 key。

## 提示

1. `var tasks by remember { mutableStateOf(emptyList<Task>()) }`；`tasks = tasks + Task(nextId++, ...)`。
2. 删除：`tasks = tasks.filterNot { it.id == task.id }`。

<details><summary>参考实现</summary>

```kotlin
private data class Task(val id: Int, val title: String)

@Composable
private fun ListExercise() {
    var tasks by remember { mutableStateOf(emptyList<Task>()) }
    var nextId by remember { mutableIntStateOf(1) }
    Column {
        Button(onClick = {
            tasks = tasks + Task(nextId, "任务 $nextId")
            nextId++
        }) { Text("添加任务") }
        if (tasks.isEmpty()) Text("暂无任务")
        LazyColumn {
            items(tasks, key = { it.id }) { task ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(task.title, modifier = Modifier.weight(1f))
                    TextButton(onClick = { tasks = tasks.filterNot { it.id == task.id } }) {
                        Text("删除")
                    }
                }
            }
        }
    }
}
```

这里为突出列表行为使用 `remember`。若要在 Activity 重建后保存整个列表，可在后续课程学习 `ViewModel` 或自定义 `Saver`。

</details>

**变式**：给每项加完成状态和 `Checkbox`，用 `map` 生成更新后的列表。
