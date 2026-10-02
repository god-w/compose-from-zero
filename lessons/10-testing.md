# 10 · 测试：怎样证明行为真的正确？

**本章目标：**为第 7 章规则和界面写能发现回归的测试，理解语义查找和同步。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 10 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/10.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 10 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/10.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从手工验收变成可重复动作

前几章一直“输入 → 点击 → 看结果”。测试把同一件事记录成代码。它不替你决定预期，预期必须先来自需求。

“按钮存在”不能证明添加正确；“添加后总数增加、空标题被拒绝”才对应业务行为。测试看到的语义树也不是第一章的函数调用树。

## 2. 先做最小 JVM 测试

app dependencies 加入 `testImplementation("junit:junit:4.13.2")`。新建 `app/src/test/java/dev/learning/compose/TaskRulesTest.kt`：

```kotlin
package dev.learning.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskRulesTest {
    @Test fun blankTitleIsRejected() {
        assertNull(normalizedTitle("   "))
        assertEquals("A", normalizedTitle(" A "))
    }
    @Test fun unfinishedFilterKeepsOnlyUnfinished() {
        val tasks = listOf(Task(1, "A", true), Task(2, "B", false))
        assertEquals(listOf(2), visibleTasks(tasks, 1).map { it.id })
    }
}
```

运行 `./gradlew :app:testDebugUnitTest`。这些测试使用第 7 章 TaskModels.kt，不需要启动模拟器。临时删掉 trim，第一条应失败；恢复后通过。这比复制实现逻辑再测试它更有价值。

## 3. 准备 UI 测试环境

app 的 defaultConfig 添加：

```kotlin
testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
```

dependencies 使用已有 composeBom 变量：

```kotlin
androidTestImplementation(composeBom)
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test:runner:1.6.2")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

这是固定的可复现 runner 版本；升级需查看发布页。启动模拟器，再运行 connectedDebugAndroidTest。

## 4. 查找控件：先语义，再标签

文字按钮可按可见文字查找。输入框和统计可加稳定 testTag，标签只用于测试，不应读给用户。

在 TaskContent.kt 导入 `androidx.compose.ui.platform.testTag`，做这两处修改：

```kotlin
Text("已完成 ${tasks.count { it.done }} / 总共 ${tasks.size}",
    modifier = Modifier.testTag("task-summary"))

// 保留原有其他参数
OutlinedTextField(value = input, onValueChange = onInput,
    label = { Text("新任务") },
    modifier = Modifier.fillMaxWidth().testTag("task-input"))
```

## 5. 测无状态 UI，也要给它真实的状态闭环

新建 `app/src/androidTest/java/dev/learning/compose/TaskUiTest.kt`。测试每次创建独立内存状态；它不依赖课程菜单，也不复用前一条测试的数据。

```kotlin
package dev.learning.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test

class TaskUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun addingTaskUpdatesSummary() {
        compose.setContent {
            var tasks by remember { mutableStateOf(emptyList<Task>()) }
            var input by remember { mutableStateOf("") }
            MaterialTheme {
                TaskContent(tasks = tasks, input = input, filter = 0,
                    onInput = { input = it },
                    onAdd = { normalizedTitle(input)?.let {
                        tasks = tasks + Task(1, it); input = ""
                    } },
                    onFilter = {}, onToggle = {}, onDelete = {})
            }
        }
        compose.onNodeWithTag("task-input").performTextInput(" A ")
        compose.onNodeWithText("添加").performClick()
        compose.onNodeWithText("A").assertExists()
        compose.onNodeWithTag("task-summary")
            .assertTextEquals("已完成 0 / 总共 1")
    }
}
```

这是添加行为的独立测试，不声称覆盖 ViewModel。第 8 章规则可再用单元测试调用 model.add，形成另一层检查。

## 6. 故意制造一次失败

把添加动作改成总是添加“错误”，运行测试，应找不到 A。修复后再运行。**如果故障存在但测试仍通过，先改断言，而不是宣布功能正确。**

空白输入时按钮禁用，所以 UI 测试应断言 disabled；不要对禁用按钮执行点击来假装测试了规则。业务规则拒绝空白由 JVM 测试验证。

![第 10 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/10-experiment.png)

示意比较正确和故意错误的添加结果。验收断言应区分它们。

## 7. 同步、理解检查与扩展

Compose 测试框架同步许多 UI 工作，不要用固定 sleep 猜等待。外部网络或未纳入框架的异步工作需使用条件等待、测试替身等明确机制；第 17 章练习异步时再扩展。

1. UI 测试通过，是否证明 Room 恢复正确？
2. 为什么不用 contentDescription="test-input" 来定位？
3. 两条测试顺序一换就失败，说明什么？

<details><summary>解析</summary>

1. 本例没有数据库，不能推断持久化。测试范围由它实际执行的路径决定。
2. contentDescription 是用户可听的无障碍信息，测试内部标识应用 testTag。
3. 测试可能共享了未清理的数据或外部资源；每条应准备自己的初始条件。

</details>

独立补一条完成、过滤、删除流程，并临时把删除 id 改错，确认失败。不要为每行 Text 都写镜像断言。

| 现象 | 检查 |
| --- | --- |
| 找到多个同名节点 | 用更明确的语义或 tag，不用随意取第一个 |
| 找不到输入框 | tag 是否设置在真正的控件 Modifier 上？ |
| 测试任务没设备 | 启动模拟器，adb devices 确認连接 |
| 测试依赖无法解析 | BOM 是否添加到 androidTest，runner 是否 Sync？ |

完成标准：逻辑和 UI 测试通过，故意回归后失败，恢复后再通过。官方核对：[测试环境](https://developer.android.com/develop/ui/compose/testing)、[测试同步](https://developer.android.com/develop/ui/compose/testing/synchronization)、[AndroidX Test 发布版本](https://developer.android.com/jetpack/androidx/releases/test)。

---

上一章：[第 09 章](09-navigation.md) · 下一章：[第 11 章](11-quality.md)
