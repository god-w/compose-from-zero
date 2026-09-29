# 08 · 状态持有者与架构

**目标**：把阶段项目的界面状态与业务操作分开，理解 `ViewModel`、`StateFlow`、生命周期收集。预计 90–120 分钟。直接扩展第 07 课的任务清单。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 08 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/08.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 08 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/08.png)

**看图先猜**：点击一项后，哪一层应该决定新任务列表：行组件还是状态持有者？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先保留现有 UI，只把 `TaskRow` 改为接收值与事件。
2. 把添加、完成、删除逻辑逐个移入 ViewModel；每迁移一项就运行。
3. 观察一次点击如何经过回调、StateFlow，再回到 UI。

**停下来验收**：`TaskRow` 不知道 ViewModel；业务方法可单独调用和测试。

**若结果不同**：如果 UI 没更新，检查是否发出了新的状态值并在界面按生命周期收集。
<!-- visual-end -->

## 为什么要迁移

前几课把状态放在 Composable 里，适合局部界面状态。任务列表属于屏幕级数据：旋转后要保留、将来可能来自数据库。此时让 `ViewModel` 持有状态，Composable 只负责读取状态、发送事件。`ViewModel` 可跨配置更改存活，但**不能自动跨进程死亡**；持久保存请用数据库，少量可恢复值可用 `SavedStateHandle`。

## 动手步骤

1. 在 `app/build.gradle.kts` 的 `dependencies` 中增加以下两行，Sync：

   ```kotlin
   implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
   implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
   ```
2. 新建 `TaskViewModel`，用 `MutableStateFlow(TaskUiState())` 私有保存状态，只暴露 `StateFlow<TaskUiState>`。
3. 把 `add`、`toggle`、`delete`、`setFilter` 写成 ViewModel 方法；每次生成新的状态值。
4. Composable 中 `val state by viewModel.uiState.collectAsStateWithLifecycle()`，把事件回调传给无状态子组件。
5. 旋转设备，再通过系统开发者选项触发“不要保留活动”，分辨配置更改和进程恢复。
6. 把任务行提成 `TaskRow(task, onToggle, onDelete)`；检查它不导入 ViewModel，也不知道任务数据来自内存还是数据库。

## 验收

- `TaskList` 不导入 ViewModel，也不持有任务列表。
- ViewModel 的方法无需 Compose UI 就能测试。
- 能画出：用户点击 → 回调 → ViewModel 更新 StateFlow → UI 重组。
- 快速点击同一任务两次，最终状态正确；没有在 `TaskRow` 中留第二份完成状态。

## 提示

```kotlin
data class TaskUiState(val tasks: List<Task> = emptyList(), val filter: Filter = Filter.All)
class TaskViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState = _uiState.asStateFlow()
    fun add(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        _uiState.update { old ->
            old.copy(tasks = old.tasks + Task(nextId(), clean))
        }
    }
}
```

`nextId()` 由你实现，并保证不会产生重复 ID；这个片段只示意状态更新。需要导入 `kotlinx.coroutines.flow.update`。先保持单模块与内存数据。第 16 课才把数据迁到 Room；不要为了练架构凭空堆 Repository 接口。

## 自检

在纸上或 README 画出 3 条路径：添加成功、空标题被拒绝、删除任务。每条路径标出事件发起者、规则执行者、状态所有者和 UI 消费者。若有两处同时可修改任务列表，回到本课整理。

**变式**：把输入框内容仍放在 Composable，解释为什么它不一定要进入 ViewModel。

官方资料：[Compose 与 ViewModel](https://developer.android.com/develop/ui/compose/state#viewmodel-state)、[生命周期安全地收集 Flow](https://developer.android.com/develop/ui/compose/state#use-other-types-of-state-in-jetpack-compose)。
