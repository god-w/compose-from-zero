# 08 · ViewModel 与 StateFlow：让界面只负责呈现

**本章目标：**沿用第 7 章 Task 与 TaskContent，将屏幕业务规则迁入 ViewModel。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 08 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/08.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 08 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/08.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 先判断为什么需要迁移

任务数据会被列表、详情和持久层共同使用。把业务更新集中在屏幕状态持有者里，便于复用和测试。不是因为“Composable 不能持有任何状态”：局部草稿、展开状态仍可留在合适的 UI 层。

本章先保持单模块、内存数据。ViewModel 能跨配置变化存活，不能自动跨进程结束；第 16 章解决后者。

## 2. 把新名词接到已有概念

| 名词 | 可以先这样理解 | 不负责什么 |
| --- | --- | --- |
| UiState | 一份当前屏幕数据快照 | 不直接画界面 |
| MutableStateFlow | 状态持有者可写的数据流 | 不自动成为 Compose State |
| StateFlow | UI 读取的只读视图 | 不让调用者任意改业务状态 |
| collectAsStateWithLifecycle | 把 Flow 的值接入 UI，并按生命周期收集 | 不把数据库自动保存起来 |

改变列表时发出新值。修改同一个普通 List 内部对象，再重复发相等状态，可能无法产生预期更新。

## 3. 文件与依赖

沿用第 7 章的 TaskModels.kt、TaskContent.kt。`app/build.gradle.kts` 的 dependencies 加入下列稳定版本；已存在时不重复添加。它们是本项目已有缓存的 2.9.4 系列，课程固定版本便于复现，升级另看官方发布页。

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
```

Sync 后新建 `TaskViewModel.kt`：

```kotlin
package dev.learning.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TaskUiState(val tasks: List<Task> = emptyList(), val filter: Int = 0)

class TaskViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(TaskUiState())
    val uiState = mutableUiState.asStateFlow()
    private var nextId = 1

    fun add(raw: String) {
        val title = normalizedTitle(raw) ?: return
        val task = Task(nextId++, title)
        mutableUiState.update { old -> old.copy(tasks = old.tasks + task) }
    }
    fun toggle(id: Int) {
        mutableUiState.update { old -> old.copy(tasks = old.tasks.map {
            if (it.id == id) it.copy(done = !it.done) else it
        }) }
    }
    fun delete(id: Int) {
        mutableUiState.update { old -> old.copy(tasks = old.tasks.filterNot { it.id == id }) }
    }
    fun setFilter(filter: Int) {
        if (filter in 0..2) mutableUiState.update { it.copy(filter = filter) }
    }
}
```

这里的调用由主线程 UI 发起，nextId 在 update lambda 外生成，避免 update 在竞争时重试导致重复副作用。并发写入、数据库 ID 在后续章扩展。

## 4. 一步一步迁移，保持行为不变

先只迁移添加动作，验证空格仍不能进入。再迁移完成和删除。最后迁移 filter。每次对照第 7 章验收脚本。

在 `Exercises.kt` 加入 `androidx.lifecycle.viewmodel.compose.viewModel` 和 `androidx.lifecycle.compose.collectAsStateWithLifecycle` 导入，替换第 7 课入口：

```kotlin
@Composable
private fun CapstoneExercise() {
    val model: TaskViewModel = viewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    TaskContent(
        tasks = state.tasks, input = input, filter = state.filter,
        onInput = { input = it },
        onAdd = { if (normalizedTitle(input) != null) { model.add(input); input = "" } },
        onFilter = model::setFilter,
        onToggle = model::toggle,
        onDelete = model::delete
    )
}
```

本课程高级章节继续修改第 7 课入口；第 8 课菜单是讲义提醒，不会自动接入你新写的类。**不要在函数体用 `TaskViewModel()` 每次手工创建实例。**

## 5. 用图跟踪一次动作

![第 08 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/08-experiment.png)

教学示意：点击只上报事件，ViewModel 发出新的 TaskUiState，UI 才显示新统计。TaskContent 不导入 ViewModel，可接静态数据、测试数据或别的持有者。

## 6. 理解作用域，别误判生命周期

此处 viewModel() 默认取得当前 ViewModelStoreOwner 的实例，课程外壳通常提供 Activity owner。所以返回课程首页后，它可能仍在；这与把状态放在练习 Composable 的 remember 不同。

“不要保留活动”主要用于活动销毁实验，不能等同于进程死亡实验。要测试后者，另按第 16 章脚本执行并记录实际操作。

## 7. 理解检查

1. UI 不再持有 tasks，是否就不能持有 input？
2. ViewModel 新增接口后，TaskContent 是否必须知道数据来自网络？
3. 旋转任务还在，是否证明关机也还在？

<details><summary>解析</summary>

1. 可以持有。草稿的范围由需求决定，它不必与业务任务同寿命。
2. 不需要，它只收值和回调。数据来源在上层整合。
3. 没有。内存存活、系统恢复、磁盘持久化是不同机制。

</details>

## 8. 迁移任务、验收与排错

独立加入 `rename(id, rawTitle)`，UI 只报告 id 与候选标题；空标题拒绝、id 不变。用普通 Kotlin 调用 ViewModel 方法检查输出，再接编辑界面。

| 现象 | 检查 |
| --- | --- |
| 每次输入任务都被清空 | 是否每次新建 ViewModel？ |
| UI 只看见初值 | 是否只读取 uiState.value，而没有收集为 Compose State？ |
| 两屏任务不一致 | 两个 ViewModel 是否由不同 owner 创建？ |
| 多处修改 tasks | 是否保留了旧 Composable 列表状态？ |

验收：第 7 章所有动作仍正确，旋转仍保留，TaskContent 与业务类分离；能画出完整数据流。

官方核对：[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)、[生命周期收集](https://developer.android.com/develop/ui/compose/state#use-other-types-of-state-in-jetpack-compose)、[Lifecycle 发布版本](https://developer.android.com/jetpack/androidx/releases/lifecycle)。

---

上一章：[第 07 章](07-capstone.md) · 下一章：[第 09 章](09-navigation.md)
