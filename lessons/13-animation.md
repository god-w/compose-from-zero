# 13 · 状态驱动动画

**目标**：根据状态变化设计有意义的动效，知道何时用 `animate*AsState`、`AnimatedVisibility`、`updateTransition`。预计 90 分钟。练习文件：`AdvancedExercises.kt` 的 `AnimationExercise`，随后迁移到第 07 课任务清单。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 13 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/13.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 13 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/13.png)

**看图先猜**：快速连续点“展开/收起”时，动画最后应听从点击次数还是当前 `expanded`？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先完成没有动画的展开/收起，确认状态正确。
2. 只给背景颜色加动画，慢点和快点各试三轮。
3. 再加内容显隐与尺寸动画，观察最终界面是否与状态一致。

**停下来验收**：动画可中途转向，最终颜色和内容始终与业务状态一致。

**若结果不同**：如果状态错乱，先删掉动画，检查是否把动画值当作业务状态保存。
<!-- visual-end -->

## 概念

Compose 动画仍遵循“状态 → UI”：你设置目标值，动画 API 负责从旧值过渡。只动一个值时先用 `animate*AsState`；元素出现/消失时用 `AnimatedVisibility`；多个值必须同步时用 `updateTransition`。不要用协程循环直接改每一帧的颜色或尺寸。

动效要表达状态变化，不能拖慢主要操作。支持系统的“移除动画”偏好；用不同动画速度测试时，完成状态应始终正确。

## 跟做

1. 在 `AnimationExercise` 保存 `expanded` 布尔状态。用 `Button` 切换它。
2. 让卡片背景在两种颜色间过渡：`val color by animateColorAsState(targetValue = if (expanded) ... else ..., label = "card color")`。
3. 用 `AnimatedVisibility(visible = expanded)` 显示说明文字。
4. 给卡片添加 `Modifier.animateContentSize()`；观察它与 `AnimatedVisibility` 的职责区别。
5. 把其中一种动效应用到任务清单的“完成/未完成”状态。

## 验收

- 连续快速点击时，最终颜色、文字可见性与 `expanded` 一致。
- 任务完成状态仍由任务数据决定，动画值不被当成业务状态。
- 能说明何时应该删除一个仅为好看的动画。

## 两级提示

1. 先写完全不带动画的版本，确保状态切换正确；再替换静态颜色。
2. 所需导入来自 `androidx.compose.animation.*` 与 `androidx.compose.animation.animateColorAsState`。

<details><summary>最小参考片段</summary>

```kotlin
var expanded by rememberSaveable { mutableStateOf(false) }
val color by animateColorAsState(
    targetValue = if (expanded) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant,
    label = "card color"
)
Column(Modifier.background(color).animateContentSize()) {
    Button(onClick = { expanded = !expanded }) { Text("展开 / 收起") }
    AnimatedVisibility(visible = expanded) { Text("更多内容") }
}
```

</details>

**延伸**：用 `updateTransition(expanded, label = "card")` 同时驱动颜色和透明度。观察两者是否在同一状态切换中保持同步。

官方资料：[Compose 动画](https://developer.android.com/develop/ui/compose/animation/introduction)、[动画快速选择](https://developer.android.com/develop/ui/compose/quick-guides/content/video/animation-in-compose)。
