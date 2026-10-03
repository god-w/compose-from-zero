# 15 · 绘制与自定义布局：测量、放置、画出来

> **Kotlin 前置回查：** [K01 值、类型与字符串](kotlin/K01-values.md) · [K05 接收者、扩展与作用域函数](kotlin/K05-receivers.md) · [K09 泛型与类型边界](kotlin/K09-generics.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**实现有语义的进度环，再用两个子项实验理解 Layout 的职责。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 15 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/15.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 15 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/15.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 把第一章的三个阶段真正拆开

组合确定内容，布局确定尺寸和位置，绘制产生像素。Canvas 提供绘制空间，不会替你摆放所有其他组件，也不会因为画了圆环就知道它代表进度。

本章分两次做：A 进度环；B 两个标签的自定义布局。常规产品继续优先 Row、Column、FlowRow，自定义布局用于理解和表达特殊约束。

## 2. 实验 A：先定义数学规则

| 输入 | 预期进度 |
| --- | --- |
| done=0,total=10 | 0 |
| done=5,total=10 | 0.5 |
| done=10,total=10 | 1 |
| total=0 或负数 | 0，避免除零 |
| done 超出范围 | 绘制值裁剪到 0..1 |

先使用 Float 除法，整数除法 `5 / 10` 是 0。startAngle=-90 表示从顶部开始，sweepAngle=360×progress。

## 3. 完整进度环代码

新建 `ProgressRing.kt`：

```kotlin
package dev.learning.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun ProgressRing(done: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
    Canvas(modifier.size(80.dp).semantics {
        progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
    }) {
        val stroke = 8.dp.toPx()
        val diameter = (size.minDimension - stroke).coerceAtLeast(0f)
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)
        drawArc(Color.LightGray, -90f, 360f, false,
            topLeft = topLeft, size = arcSize, style = Stroke(stroke))
        drawArc(Color(0xFF356AC3), -90f, 360f * progress, false,
            topLeft = topLeft, size = arcSize, style = Stroke(stroke))
    }
}
```

描边以路径为中心，减掉 stroke 后居中，避免边缘半条线被裁切。dp 到 px 的转换在 DrawScope 中完成。

在 AdvancedExercises.kt 的 DrawingExercise 调用，新增 Column import：

```kotlin
@Composable
private fun DrawingExercise() {
    Column {
        ProgressRing(0, 10)
        ProgressRing(5, 10)
        ProgressRing(10, 10)
        Text("已完成 5 / 总共 10")
    }
}
```

先看三种端点，再试 0 总数和超范围。读屏依赖 progressBarRangeInfo 或清晰文字，不依赖弧线形状。文本与图形可能重复提供信息，产品中应检查朗读是否合适。

![第 15 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/15-experiment.png)

示意比较 0%、50%、100%；实际绘制代码还处理了越界与零总数。

## 4. 实验 B：只布局两个子项

在新文件 TwoLabels.kt 添加如下代码（包含 import）。这个 Layout 接收恰好两项，避免一开始就写完整流式布局算法。

```kotlin
package dev.learning.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.constrainHeight

@Composable
fun TwoLabels(content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        require(measurables.size == 2)
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val first = measurables[0].measure(loose)
        val remaining = if (constraints.hasBoundedWidth)
            (constraints.maxWidth - first.width).coerceAtLeast(0) else Constraints.Infinity
        val second = measurables[1].measure(loose.copy(maxWidth = remaining))
        val width = constraints.constrainWidth(first.width + second.width)
        val height = constraints.constrainHeight(maxOf(first.height, second.height))
        layout(width, height) {
            first.placeRelative(0, 0)
            second.placeRelative(first.width, 0)
        }
    }
}
```

调用 `TwoLabels { Text("A"); Text("第二个标签") }`。测量阶段每个子项只测一次；放置阶段使用测量结果。placeRelative 尊重布局方向。

这是横排教学模型，窄空间第二项可能被压缩，不提供自动换行。和 Row 比较后，产品保留 Row 或 FlowRow；不要把这个两项样本当作成熟布局。

## 5. 理解检查

1. Canvas 画出的字会自动成为可查找的 Text 语义吗？
2. measure 之后还需要 place 吗？
3. 为什么 Layout 要遵守 constraints？

<details><summary>解析</summary>

1. 不会。自绘内容需要显式语义或等价的文字组件。
2. 需要。测量只获得大小，放置确定坐标。
3. 父子要协调可用空间，任意忽略约束可能溢出或破坏布局。

</details>

## 6. 验收、迁移、排错

把 ProgressRing 接到第 7 章真实完成数量，不再维护第二份 progress 状态。修改任务后圆环与统计同步。

| 现象 | 检查 |
| --- | --- |
| 50% 仍画成 0 | 是否整数除法？ |
| 圆环边缘被切掉 | 是否为半条描边留出空间？ |
| 零任务崩溃 | 是否先处理 total<=0？ |
| 标签叠在一起 | 是否测量后遗漏第二项放置？ |

完成标准：五个数学边界正确、读屏有信息、能解释 measure/place/draw。官方核对：[绘制](https://developer.android.com/develop/ui/compose/graphics/draw/overview)、[自定义布局](https://developer.android.com/develop/ui/compose/layouts/custom)。

## 本章代码的真实运行对照

![第 15 章完整样本的真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/15-reference-running.png)

三个圆环分别按 0%、50%、100% 绘制。这是按本章代码在独立验证 app 中运行的截图；课程中的占位练习仍需你亲手完成。

---

上一章：[第 14 章](14-gestures.md) · 下一章：[第 16 章](16-state-persistence.md)
