# 14 · 手势：拖动距离不是业务状态

**本章目标：**写拖动任务卡，理解像素、事件消费和与按钮等价的操作入口。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 14 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/14.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 14 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/14.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从 OnTouchListener 的认识开始

Compose 也有低层指针事件，但普通按钮/点击优先用 Button、clickable 等高层 API，它们带有语义、反馈与更多输入支持。需要持续拖动距离时才进入 pointerInput。

我们做一个小实验：卡片向左拖动超过阈值表示完成，松手后位置归零；旁边保留“完成”按钮。拖动只是另一种触发方式，业务结果仍是 done。

## 2. 分清单位与三个值

| 值 | 单位/类型 | 作用 |
| --- | --- | --- |
| threshold | dp 转 px | 距离阈值与密度一致 |
| dragX | Float，像素 | 临时拖动位置 |
| done | Boolean | 真正完成状态 |

拖动回调中的距离是像素。把 `80f` 当成 80dp 会在不同密度设备上产生不同体验。

## 3. 到哪里写？先只做按钮

修改 `AdvancedExercises.kt` 的 `GestureExercise`，先用 remember 保存 done，放一个按钮令它变成 true，并显示完成状态。运行确认后才添加拖动。

完整实验需要导入：

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
```

## 4. 第二步：只看拖动，再连接完成动作

```kotlin
@Composable
private fun GestureExercise() {
    var done by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(0f) }
    val limitPx = with(LocalDensity.current) { 120.dp.toPx() }
    val thresholdPx = with(LocalDensity.current) { 80.dp.toPx() }
    Column {
        Text(if (done) "任务已完成" else "向左拖动或点完成")
        Box(
            Modifier.fillMaxWidth().height(80.dp)
                .offset { IntOffset(dragX.roundToInt(), 0) }
                .background(Color(0xFFE1EAF9))
                .pointerInput(limitPx, thresholdPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, delta ->
                            change.consume()
                            dragX = (dragX + delta).coerceIn(-limitPx, 0f)
                        },
                        onDragEnd = {
                            if (dragX <= -thresholdPx) done = true
                            dragX = 0f
                        },
                        onDragCancel = { dragX = 0f }
                    )
                }
        ) { Text("任务 A") }
        Button(onClick = { done = true }, enabled = !done) { Text("完成") }
        Button(onClick = { done = false }) { Text("重置") }
    }
}
```

第一轮可以先只保留拖动更新与归零，不修改 done。确认方向正确后再加阈值判断。负值代表向左，比较应使用 `<= -thresholdPx`。

消费事件表示此手势使用了对应变化，但不等于所有父布局都永远不会收到输入；真实列表中的横向手势与纵向滚动要另做冲突验证。本章先用独立区域观察。

![第 14 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/14-experiment.png)

示意比较短拖动与超过阈值后的最终结果。完成业务状态和临时位移分开保存。

## 5. 取消与等价入口

取消可能来自另一手势接管或输入中断，不应自动完成任务。onDragCancel 将位置复位即可。按钮和拖动执行同一个“完成”意图，TalkBack 用户不必学会拖动才能操作。

如果抽成 `DraggableTask(onComplete)`，长生命周期的 pointerInput 需要拿到最新回调：可以使用 rememberUpdatedState，或为需要重启的输入使用合适 key。不要为解决旧闭包随意把每一帧 dragX 放进 key，否则不断重启检测器。

## 6. 理解检查

1. 为什么不能保存 dragX 当作任务完成进度？
2. 为什么需要 onDragCancel？
3. 为什么拖动已经能完成还保留按钮？

<details><summary>解析</summary>

1. dragX 是短暂输入状态，松手就复位；done 才是稳定业务结果。
2. 取消不是正常松手，应清理临时位移，不误触业务操作。
3. 给键盘、读屏及不方便拖动的人提供等价操作，并便于自动化验证。

</details>

## 7. 迁移、验收与排错

独立加入“向右拖动取消完成”。先定义另一侧阈值与取消规则，再扩展范围；不要用同一个正阈值判断两边。

| 现象 | 检查 |
| --- | --- |
| 不同设备阈值不同 | dp 是否正确转成像素？ |
| 拖动不断中断 | pointerInput key 是否使用 dragX？ |
| 松手后卡片停在半路 | onDragEnd / onDragCancel 是否都复位？ |
| 列表难以纵向滚动 | 是否错误消费所有方向，是否测试了滚动冲突？ |

验收：短拖不完成、长拖完成、取消不完成、按钮等价，旋转后临时位置不需要恢复。官方核对：[手势层级](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures)、[拖动](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling)。

## 本章代码的真实运行对照

![第 14 章完整样本的真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/14-reference-running.png)

在模拟器上向左拖动超过阈值后，任务变成已完成，卡片位置归零。这是按本章代码在独立验证 app 中运行的截图；课程中的占位练习仍需你亲手完成。

---

上一章：[第 13 章](13-animation.md) · 下一章：[第 15 章](15-drawing-layout.md)
