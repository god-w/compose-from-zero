# 14 · 手势与交互冲突

**目标**：先用高层语义化组件，再在必要时使用 `pointerInput`；能处理点击与拖动的冲突。预计 90 分钟。练习文件：`AdvancedExercises.kt` 的 `GestureExercise`。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 14 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/14.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 14 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/14.png)

**看图先猜**：轻点和水平拖动从同一个位置开始，怎样避免把拖动当成点击？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先用普通 `clickable` 完成可访问的点击操作。
2. 给拖动增加可视化位移，观察轻点时位移是否为零。
3. 最后添加按钮形式的等价操作，用键盘或 TalkBack 试一遍。

**停下来验收**：轻点只触发点击，拖动不误触删除，非触屏操作仍可完成任务。

**若结果不同**：如果两个动作都触发，先缩小手势区域并检查检测器的消费顺序。
<!-- visual-end -->

## 概念

`Button`、`clickable`、`combinedClickable` 已处理许多交互、焦点和无障碍语义。`pointerInput` 适合确实需要自定义拖动等手势时；它不像 `clickable` 自动提供完整的按钮语义。给同一元素叠加点击与拖动时，要考虑触摸竞争与可访问性替代操作。

## 跟做

1. 做一张任务卡：点击切换完成状态，长按显示“编辑”提示。
2. 增加水平拖动，只让卡片在本行内移动，松手后回到原位；不要先实现“滑动即删除”。
3. 拖动使用 `pointerInput(Unit) { detectHorizontalDragGestures(...) }`；偏移用 `Modifier.offset { IntOffset(offsetX.roundToInt(), 0) }`。
4. 保留可点击的“删除”按钮，确保不使用手势也能完成任务。
5. 开启 TalkBack，验证卡片标题与操作仍能读出。

## 验收

- 轻点不会被误判为拖动；拖动不会意外触发删除。
- 拖动值在横向可控范围内；离开页面后没有残留手势任务。
- 键盘和 TalkBack 用户有等价的操作入口。

## 提示

先实现点击/长按，再加拖动；每完成一步就在模拟器上验证。偏移值在 `onHorizontalDrag` 中累加 `dragAmount`，松手在 `onDragEnd` 中归零。需要导入 `androidx.compose.foundation.gestures.detectHorizontalDragGestures` 与 `androidx.compose.ui.input.pointer.pointerInput`。

**延伸**：尝试 `anchoredDraggable` 做两个稳定位置的操作菜单。比较它与自己累加像素偏移的边界处理和恢复行为。

官方资料：[理解手势](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures)。
