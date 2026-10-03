# 11 · 质量：让看不见屏幕的人也能完成任务

> **Kotlin 前置回查：** [K02 函数与控制流程](kotlin/K02-functions.md) · [K03 可空类型与安全输入](kotlin/K03-null-safety.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**理解语义树、预览与字符串资源，验证 TalkBack、大字号和本地化。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 11 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/11.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 11 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/11.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从可见画面走向可操作含义

文字画出来了，不等于所有用户都能完成操作。TalkBack 通过语义信息知道节点的名称、角色、状态和动作。视觉树、布局树与语义树不是同一棵树。

标准 Button、Checkbox 已提供许多语义；优先使用它们，再补业务含义。不要把每个节点都塞一遍 contentDescription，造成重复朗读。

## 2. 先听当前版本，不急着加属性

沿用第 7 章 TaskContent。模拟器或设备设置中启用 TalkBack，练习逐项移动焦点、双击激活动作。记下：复选框是否知道任务名称？“删除”是否知道删哪一项？

| 看得到的信息 | 应能听到的信息 | 验证动作 |
| --- | --- | --- |
| 任务 A 与勾选框 | A 的完成状态与可切换动作 | 切换后再次听 |
| 行末删除 | 删除任务 A | 焦点移动到按钮 |
| 彩色完成标记 | 完成状态，不依赖颜色 | 改为灰度仍可理解 |

## 3. 第一步：给动作补明确对象

在 TaskContent.kt 添加 import：

```kotlin
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
```

替换原来行中的 Checkbox 和删除按钮：

```kotlin
Checkbox(
    checked = task.done,
    onCheckedChange = { onToggle(task.id) },
    modifier = Modifier.semantics {
        contentDescription = "任务 ${task.title}"
        stateDescription = if (task.done) "已完成" else "未完成"
    }
)
TextButton(
    onClick = { onDelete(task.id) },
    modifier = Modifier.semantics { contentDescription = "删除任务 ${task.title}" }
) { Text("删除") }
```

先用中文教学文案听读，再按本章资源化。这里保留独立复选框与删除两个动作，没有把整行合成一个节点，否则删除动作可能变得不可达。

装饰 Image 的 contentDescription 可以为 null。真正承担动作的图标需要明确动作名称。不要用“垃圾桶图标”代替“删除任务”。

## 4. 第二步：大字号暴露布局假设

把系统字体调到较大档位，检查长任务名。不要给文字区域固定高度，允许换行；行末删除要能触达。标准 Material 控件通常提供合适触控目标，不要通过缩小交互区域破坏它。

![第 11 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/11-experiment.png)

教学示意比较长标题在固定高度下被截断与允许换行后的区别。最终验收仍要在设备上放大字体和使用读屏。

## 5. 第三步：预览是无状态接口的回报

新建 `TaskPreviews.kt`，使用第 7 章 TaskContent：

```kotlin
package dev.learning.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, widthDp = 360, heightDp = 640, fontScale = 2f)
@Composable
fun LongTaskPreview() {
    MaterialTheme {
        TaskContent(
            tasks = listOf(Task(1, "这是一个很长的任务标题，用来检查文字是否能够换行")),
            input = "", filter = 0,
            onInput = {}, onAdd = {}, onFilter = {}, onToggle = {}, onDelete = {}
        )
    }
}
```

再复制为 EmptyTaskPreview，传 emptyList；第三个预览传一项完成、一项未完成。空回调只用于静态预览，不证明交互正确。预览不应该强迫连接真实数据库。

## 6. 第四步：资源化不只是翻译单词

在 `app/src/main/res/values/strings.xml` 新增资源（已有 resources 时合并，不创建嵌套）：

```xml
<resources>
    <string name="task_summary">已完成 %1$d / 总共 %2$d</string>
    <string name="delete_task">删除任务 %1$s</string>
    <string name="task_done">已完成</string>
    <string name="task_pending">未完成</string>
</resources>
```

导入 `androidx.compose.ui.res.stringResource`。在 Composable 中：

```kotlin
val summary = stringResource(R.string.task_summary, tasks.count { it.done }, tasks.size)
Text(summary, modifier = Modifier.testTag("task-summary"))
```

这里替换第 10 课的统计文字时必须保留 `Modifier.testTag("task-summary")`；输入框的 `Modifier.fillMaxWidth().testTag("task-input")` 也继续保留。改完后重新运行第 10 课的 UI 测试，确认它仍能按这两个标签找到节点。

第 10 课断言使用固定中文；在本章接入英文资源后，把断言改为读取当前测试设备的资源，避免切换语言时误报。先导入 `androidx.test.platform.app.InstrumentationRegistry`，将原来的统计断言替换为：

```kotlin
val context = InstrumentationRegistry.getInstrumentation().targetContext
val expected = context.getString(R.string.task_summary, 0, 1)
compose.onNodeWithTag("task-summary").assertTextEquals(expected)
```

semantics lambda 不是 Composable 上下文，先在外面计算 `val deleteLabel = stringResource(R.string.delete_task, task.title)`，再在 lambda 设置 description。不要在里面直接调用 stringResource。

新建 values-en/strings.xml，把 summary 写成 `Completed %1$d / Total %2$d`。切系统语言，检查占位符顺序与长文字，继续把其余用户文案迁入资源。

## 7. 理解检查、迁移与排错

1. 图片看起来像勾，是否自动含完成语义？
2. 预览成功，是否证明 TalkBack 可操作？
3. 为什么不能把删除按钮和整行随意合并成一个节点？

<details><summary>解析</summary>

1. 不能依赖视觉形状。需相应角色、状态与描述，标准 Checkbox 是合适起点。
2. 不能。预览主要验证静态布局，读屏和交互要真机或模拟器执行。
3. 合并可能改变动作与焦点结构，必须确保独立动作仍可访问。

</details>

独立迁移：把资料卡中的“头像”作为装饰或有意义图片作一次明确判断，写出理由；把所有删除文案资源化。

| 症状 | 检查 |
| --- | --- |
| 每项被朗读两遍 | 父子 description 是否重复？是否需要重新设计合并？ |
| 编译说 stringResource 上下文错误 | 是否在普通 semantics lambda 内调用？ |
| 大字号按钮不可见 | 是否固定高度或长文字未分配弹性宽度？ |
| 切英文仍部分中文 | 是否还有硬编码文案？ |

验收：三种预览、两种语言、大字号、TalkBack 添加/勾选/删除都可用。官方核对：[语义](https://developer.android.com/develop/ui/compose/accessibility/semantics)、[预览](https://developer.android.com/develop/ui/compose/tooling/previews)、[资源](https://developer.android.com/develop/ui/compose/resources)。

---

上一章：[第 10 章](10-testing.md) · 下一章：[第 12 章](12-interop.md)
