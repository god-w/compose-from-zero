# 18 · 自适应：同一任务，随着窗口改变布局

**本章目标：**保持选中 ID 与数据不变，验证窄窗口单页和宽窗口两栏。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 18 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/18.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 18 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/18.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从“手机还是平板”换成“当前有多少空间”

手机也能横屏、分屏，平板也能缩成窄窗口。设备型号不是当前布局宽度。先根据当前容器约束安排界面，再用正式窗口与自适应组件扩展。

本章做教学原型：可用宽度至少 600dp 时两栏，否则一次显示一页。600dp 是实验阈值，不代表所有产品的内容都适合这个断点；最后按长文本与操作可达性调整。

## 2. 先分清布局状态与业务选择

| 值 | 保存还是推导？ | 原因 |
| --- | --- | --- |
| selectedId | 保存小状态 | 窗口变化不应改变用户选择 |
| 当前可用宽度 | 布局提供 | 窗口变化会重新给出 |
| wide 布尔值 | 从宽度推导 | 保存会过期 |
| selected Task | 从 tasks 与 id 查询 | 删除后及时变成 null |

不要窄屏复制一份任务，宽屏再复制另一份；那样窗口变化会变成数据迁移。

## 3. 文件、导入与最小代码

新建 AdaptiveTasks.kt，使用第 7 章 Task、第 8 章 TaskViewModel：

```kotlin
package dev.learning.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
```

先写两个无状态区域：

```kotlin
@Composable
private fun AdaptiveList(tasks: List<Task>, onSelect: (Int) -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text("任务列表")
        tasks.forEach { task ->
            TextButton(onClick = { onSelect(task.id) }) { Text(task.title) }
        }
    }
}

@Composable
private fun AdaptiveDetail(task: Task?, onDelete: (Int) -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text(task?.title ?: "选择一项任务，或当前任务已删除")
        if (task != null) TextButton(onClick = { onDelete(task.id) }) { Text("删除当前任务") }
    }
}
```

本例少量任务用 Column，正式长列表换 LazyColumn。再写布局选择：

```kotlin
@Composable
fun AdaptiveTasks() {
    val model: TaskViewModel = viewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize()) {
        TextButton(onClick = { model.add("窗口实验任务") }) { Text("添加实验任务") }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val wide = maxWidth >= 600.dp
            val selected = state.tasks.firstOrNull { it.id == selectedId }
            BackHandler(enabled = !wide && selectedId != null) { selectedId = null }
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    AdaptiveList(state.tasks, { selectedId = it }, Modifier.weight(1f))
                    AdaptiveDetail(selected, model::delete, Modifier.weight(1f))
                }
            } else if (selectedId == null) {
                AdaptiveList(state.tasks, { selectedId = it }, Modifier.fillMaxSize())
            } else {
                Column {
                    TextButton(onClick = { selectedId = null }) { Text("返回列表") }
                    AdaptiveDetail(selected, model::delete, Modifier.fillMaxWidth())
                }
            }
        }
    }
}
```

在 AdvancedExercises.kt 新增 `17 -> AdaptiveTasks()`。先窄窗口运行，再在大屏 AVD 或可调尺寸窗口验证 wide 分支；课程外壳留白也影响可用宽度。

## 4. 预测再做四种操作

| 操作 | 预期 |
| --- | --- |
| 窄窗口选择 B | 只展示 B 的详情 |
| 扩到宽窗口 | 列表和同一 B 详情并排 |
| 缩回窄窗口 | 仍是 B 的详情 |
| 删除 B | 详情显示不存在/选择提示，不引用旧对象 |

![第 18 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/18-experiment.png)

示意图的两种布局来自同一份数据、同一个 selectedId。

## 5. 原型之后再上正式组件

BoxWithConstraints 是为了看懂约束触发布局选择。产品里可使用 Material 3 Adaptive 的窗口自适应信息与列表详情 scaffold，进一步处理分栏导航、折叠姿态和系统布局要求。按官方版本添加对应依赖，别把本例的 600dp 简化规则误当完整设备适配。

第 9 章的返回栈与本章选中状态需要明确整合：产品选一种主导航来源，避免 navController 和 selectedId 同时各自控制不同页面。本实验独立理解布局变化。

## 6. 理解检查

1. 为什么保存 isTablet 不能表达当前窗口？
2. 宽窗口变窄后重新选择第一项合适吗？
3. 删除任务后为何按 id 查询比保存 Task 更可靠？

<details><summary>解析</summary>

1. 设备分类不会随分屏和窗口大小变化；布局需要当前空间。
2. 不合适。布局改变应保留用户当前选择，除非产品明确要求重置。
3. 查询反映当前数据，删除后返回 null，不留下过时副本。

</details>

## 7. 迁移、验收与排错

把列表换成 LazyColumn，并给详情添加滚动；字体放大后，删除和返回仍可达。测试分屏，记录断点两侧的截图。

| 症状 | 检查 |
| --- | --- |
| 改窗口后不变布局 | 是否把 wide 存成了 remember 中的初始值？ |
| 选择丢失 | selectedId 是否在每个分支里分别声明？ |
| 宽屏仍无两栏 | 看实际容器宽度，不只看设备型号 |
| 删除后崩溃 | 详情是否接受 nullable 当前数据？ |

完成标准：四轮行为、大字号、返回都正确；能指出原型的限制。官方核对：[自适应入门](https://developer.android.com/develop/ui/compose/layouts/adaptive/get-started-with-adaptive-apps)、[列表详情](https://developer.android.com/develop/adaptive-apps/guides/list-detail)。

## 本章代码的真实运行对照

![第 18 章完整样本的真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/18-reference-running.png)

窄窗口中选择任务后，展示该任务详情并保留返回入口。这是按本章代码在独立验证 app 中运行的截图；课程中的占位练习仍需你亲手完成。

![第 18 章宽窗口保留选择的真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/18-reference-wide-running.png)

同一实验拉宽窗口后，列表与当前详情并排。再缩回窄窗口，仍是同一任务的详情；这是实际验证过的窗口变化，没有重新选择任务。

---

上一章：[第 17 章](17-async-data.md) · 下一章：[第 19 章](19-performance.md)
