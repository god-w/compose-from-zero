# 09 · 导航与返回栈：详情为什么只接收 ID？

> **Kotlin 前置回查：** [K02 函数与控制流程](kotlin/K02-functions.md) · [K03 可空类型与安全输入](kotlin/K03-null-safety.md) · [K06 类、属性与数据类](kotlin/K06-classes.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**把列表与详情接入 Navigation Compose，保持同一数据来源并处理失效身份。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 09 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/09.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 09 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/09.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 用你熟悉的页面跳转理解返回栈

Activity/Fragment 导航会记录去过的目的地。Compose 函数调用本身并不等于导航；NavHost 用当前返回栈选择显示哪个目的地。`navigate` 压入新目的地，`popBackStack` 回到上一项。

本章使用固定版本的 Navigation Compose 2.x 字符串路由，先把返回栈原理讲透。类型安全路由和 Navigation 3 是后续可替换方案，不能混用它们的 API。

## 2. 为什么传 ID 而不是整个 Task？

假设详情打开后标题被编辑或任务被删除。若传的是对象副本，详情可能继续显示旧信息；若传 id，再从唯一状态源查询，就能看到当前值或“任务已不存在”。

| 路由数据 | 屏幕数据 | 更新方式 |
| --- | --- | --- |
| task/20 | 当前 uiState 中 id=20 的 Task | 读取当前状态再查找 |
| task/999 | 没有对应对象 | 显示明确空态 |
| task/abc | 非法参数 | 校验后显示错误，不崩溃 |

## 3. 依赖与文件

在 app 的 dependencies 增加并 Sync：

```kotlin
implementation("androidx.navigation:navigation-compose:2.10.2")
```

沿用第 8 章 TaskViewModel 和第 7 章模型。新建 `TaskNavigation.kt`：

```kotlin
package dev.learning.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
```

### 第一步：先只做两页最小跳转

下面列表页只用文本按钮展示当前任务，便于隔离导航。添加任务仍可先用第 8 章界面；或在实验中调用 model.add 创建两项数据。

```kotlin
@Composable
fun TaskNavigation() {
    val nav = rememberNavController()
    val model: TaskViewModel = viewModel() // 在 NavHost 外创建，两个目的地共享
    val state by model.uiState.collectAsStateWithLifecycle()
    NavHost(navController = nav, startDestination = "tasks") {
        composable("tasks") {
            Column {
                Text("任务列表")
                TextButton(onClick = { model.add("导航实验") }) { Text("添加实验任务") }
                state.tasks.forEach { task ->
                    TextButton(onClick = { nav.navigate("task/${task.id}") }) { Text(task.title) }
                }
            }
        }
        composable(
            route = "task/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id")?.toIntOrNull()
            val task = state.tasks.firstOrNull { it.id == id }
            TaskDetail(task = task,
                onDelete = { if (id != null) model.delete(id) },
                onBack = { nav.popBackStack() })
        }
    }
}

@Composable
fun TaskDetail(task: Task?, onDelete: () -> Unit, onBack: () -> Unit) {
    Column {
        Text(task?.title ?: "任务已不存在或 ID 无效")
        if (task != null) TextButton(onClick = onDelete) { Text("删除当前任务") }
        TextButton(onClick = onBack) { Text("返回") }
    }
}
```

在 Exercises.kt 将 `CapstoneExercise` 的函数体临时替换为 `TaskNavigation()`。课程原有外层 Scaffold 保留。后续可将列表目的地换成你已有 TaskContent，并给任务标题新增 `onOpen(id)` 回调。

### 第二步：测试数据更新，不抢先返回

打开任务 → 点击删除 → 应在当前详情显示空态。这个实验刻意不立即返回，便于证明详情重新查询同一状态源。产品之后可以选择删除后返回列表。

### 第三步：观察返回栈

运行两页跳转并旋转，系统返回应先返回任务列表。已经在任务列表时，再返回才交给课程外壳。返回按钮用 popBackStack，别通过 navigate("tasks") 创建第二份列表。

![第 09 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/09-experiment.png)

示意比较“有效 id”与“同一 id 已删除”的详情。标题来自数据源，路由只提供查询身份。

## 4. 认识作用域的陷阱

在各个 composable 目的地内部调用 viewModel()，默认 owner 可能是对应的返回栈项，得到不同实例。本例在 NavHost 上层取得共享实例。真实项目可以用导航图作用域或仓库共享数据，但不要靠全局单例掩盖作用域不清楚。

路由参数也属于输入边界。toIntOrNull 处理错误格式；查询结果的 null 处理不存在数据。二者都要有界面分支。

## 5. 理解检查与迁移

1. navigate("tasks") 与 popBackStack 为什么不同？
2. id 合法是否等于任务存在？
3. 两个详情页各创建 ViewModel 能保证共享任务吗？

<details><summary>解析</summary>

1. 前者可以压入一个新目的地，后者移除当前项回到已有上一项。
2. 不等于。合法整数也可能已删除或从未存在。
3. 不能保证。必须明确 owner 和状态来源，本例在上层共享。

</details>

独立新增“编辑”目的地：只传 id，保存时 model.rename，然后 popBackStack。先画 `[列表, 详情, 编辑] → [列表, 详情] → [列表]`，再验证系统返回。

| 现象 | 检查 |
| --- | --- |
| 详情总为空 | 是否在两页创建了不同状态持有者？ |
| 返回叠出列表 | 是否使用 navigate 代替返回？ |
| 删除后仍显示旧标题 | 是否缓存 Task 副本，而不是按 id 读取当前状态？ |
| 参数格式错误时崩溃 | 是否用了 toInt 而不是安全校验？ |

完成标准：两页可进退，旋转正确，删除当前任务与非法 id 不崩溃。官方核对：[Compose Navigation 2](https://developer.android.com/guide/navigation/navigation-2/compose)、[Navigation 发布版本](https://developer.android.com/jetpack/androidx/releases/navigation)。

---

上一章：[第 08 章](08-architecture.md) · 下一章：[第 10 章](10-testing.md)
