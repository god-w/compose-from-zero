# 17 · 异步、错误与取消：只显示最新请求的结果

**本章目标：**用可控假数据练四种结果和竞争条件，再把模型接到真实数据层。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 17 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/17.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 17 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/17.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 空列表不足以表达整个请求过程

空列表可能是“还没收到”“成功但没有数据”或“失败后没有内容”。这三种情况用户的下一步不同，需要明确状态。

| 状态 | 界面 | 用户动作 |
| --- | --- | --- |
| Loading | 加载中 | 等待或切换条件 |
| Empty | 此条件下暂无任务 | 换条件或添加 |
| Success | 展示数据 | 操作任务 |
| Error | 说明失败 | 重试 |

这章先不接外网，让延迟和故障可控。取消是协程控制信号，不应该被转换成“请求失败”的红色错误。

## 2. 文件和前置知识

沿用第 8 章 lifecycle 依赖和第 7 章 Task。新建 AsyncTasks.kt，导入：

```kotlin
package dev.learning.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
```

### 第一步：先定义四种结果

```kotlin
sealed interface LoadState {
    data object Loading : LoadState
    data object Empty : LoadState
    data class Success(val tasks: List<Task>) : LoadState
    data class Error(val message: String) : LoadState
}
```

### 第二步：写可预测的假数据源

```kotlin
suspend fun fakeLoad(mode: Int): List<Task> {
    delay(if (mode == 0) 1_500 else 300)
    return when (mode) {
        0 -> listOf(Task(1, "慢请求的 A"))
        1 -> emptyList()
        2 -> error("演示加载失败")
        else -> listOf(Task(2, "快请求的 B"))
    }
}
```

这里只模拟四个输入模式，不是真正任务筛选规则。mode=0 故意慢，mode=3 快，便于制造竞争。

### 第三步：管理请求和取消

```kotlin
class AsyncTasksModel : ViewModel() {
    private val mutableState = MutableStateFlow<LoadState>(LoadState.Empty)
    val state = mutableState.asStateFlow()
    private var job: Job? = null
    private var requestNumber = 0
    private var currentMode = 0

    fun load(mode: Int) {
        currentMode = mode
        val request = ++requestNumber
        job?.cancel()
        job = viewModelScope.launch {
            mutableState.value = LoadState.Loading
            try {
                val result = fakeLoad(mode)
                if (request == requestNumber) {
                    mutableState.value = if (result.isEmpty()) LoadState.Empty
                        else LoadState.Success(result)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (request == requestNumber) mutableState.value = LoadState.Error("加载失败，请重试")
            }
        }
    }
    fun retry() = load(currentMode)
}
```

cancel 停止旧的协作式工作；requestNumber 防止过期响应覆盖当前结果。它不能修复不响应取消的阻塞代码，需要真实数据源也支持取消。这里调用来自 UI 主线程，序号没有跨线程修改。

## 3. 先展示状态，再接列表

新建 AsyncTaskScreen.kt，先加入这些导入：

```kotlin
package dev.learning.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
```

再写下面的函数：

```kotlin
@Composable
fun AsyncTaskScreen() {
    val model: AsyncTasksModel = viewModel()
    val state by model.state.collectAsStateWithLifecycle()
    Column {
        Row {
            Button(onClick = { model.load(0) }) { Text("慢 A") }
            Button(onClick = { model.load(3) }) { Text("快 B") }
        }
        Row {
            Button(onClick = { model.load(1) }) { Text("空结果") }
            Button(onClick = { model.load(2) }) { Text("失败") }
        }
        when (val current = state) {
            LoadState.Loading -> Text("加载中…")
            LoadState.Empty -> Text("当前条件下暂无任务")
            is LoadState.Success -> current.tasks.forEach { Text(it.title) }
            is LoadState.Error -> {
                Text(current.message)
                Button(onClick = model::retry) { Text("重试") }
            }
        }
    }
}
```

AdvancedExercises.kt 的 when 新增 `16 -> AsyncTaskScreen()`。代码示范小量数据的状态分支，大量结果换为 LazyColumn。首次加载可由受控入口或 LaunchedEffect 调用，不直接在函数体调用 model.load。

![第 17 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/17-experiment.png)

教学示意强调空结果与失败文案、操作不同。

## 4. 四轮验证

1. 点慢 A：先出现 Loading，之后 A。
2. 点空结果：应出现 Empty，不是错误重试页。
3. 点失败：出现 Error；该模式重试仍会失败。要验证成功恢复，切换快 B；或将假数据源改成首次失败、第二次成功再试。
4. 立即点慢 A → 快 B，等待超过 1.5 秒，最终仍应显示 B。

临时移除取消和序号检查，重复第 4 轮，观察 A 是否迟到覆盖 B；修复后再试。这条验收才证明“只要最新”。

## 5. 页面离开与作用域

viewModelScope 随 ViewModel 清理取消，不等于所有页面退出都取消。课程这里可能使用 Activity owner，返回首页不一定清理它。如果数据应仅在该目的地存在，使用对应导航 owner，或明确停止该工作。不要宣称所有返回都会自动取消。

如果接真实接口，需要把异常映射成合适用户文案，保留诊断信息；不要直接把任意服务器异常原文展示给用户。缓存和重试策略由产品需求决定。

## 6. 理解检查与排错

1. 为什么 catch(Exception) 前处理 CancellationException？
2. Error 和 Empty 能用同一个列表值表示吗？
3. 重试失败后是不是按钮坏了？

<details><summary>解析</summary>

1. 取消用于停止旧工作，应继续传播，不应当作普通失败。
2. 单靠空列表无法说明原因，需要额外状态。
3. 本例失败模式每次都失败。先看数据源设计，再判断按钮是否正确执行。

</details>

独立把输入关键字作为请求参数，快速输入两个关键字，最后只展示最新结果；再为竞争条件写测试。

| 症状 | 检查 |
| --- | --- |
| 旧结果覆盖新结果 | 是否取消旧 Job，并拒绝过期响应？ |
| 切条件显示错误 | 是否把取消当失败？ |
| 每次重组都请求 | load 是否直接放函数体？ |
| 返回后仍请求 | 实际 owner 和作用域是否符合需求？ |

官方核对：[UI 层架构](https://developer.android.com/topic/architecture/ui-layer)、[协程取消](https://kotlinlang.org/docs/cancellation-and-timeouts.html)。

## 本章代码的真实运行对照

![第 17 章完整样本的真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/17-reference-running.png)

点击“失败”后，显示错误文案和重试入口；空结果另有独立分支。这是按本章代码在独立验证 app 中运行的截图；课程中的占位练习仍需你亲手完成。

---

上一章：[第 16 章](16-state-persistence.md) · 下一章：[第 18 章](18-adaptive.md)
