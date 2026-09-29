# 15 · 自定义绘制与布局

**目标**：用 Canvas 画进度，用 `Layout` 理解测量与放置；知道何时标准布局已足够。预计 2 小时。练习文件：`AdvancedExercises.kt` 的 `DrawingExercise`。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 15 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/15.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 15 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/15.png)

**看图先猜**：Canvas 画出的 50% 进度环，TalkBack 会自动读出“50%”吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先用普通 `Text` 显示 0%、50%、100% 三个测试值。
2. 画背景弧和进度弧，再把进度限制在 0..1。
3. 补语义描述；调大字体观察图与文字是否重叠。

**停下来验收**：三个进度端点准确；读屏能获取同等信息。

**若结果不同**：如果弧线越界，检查角度计算和输入值裁剪。
<!-- visual-end -->

## 概念

Compose 一帧分为组合、布局、绘制。`Canvas` 只负责画，不能替代交互语义；`Layout` 的测量过程决定子项大小和位置。先尝试 `Row`、`Column`、`FlowRow` 等现有布局，仅在它们表达不了需求时才手写测量逻辑。

## 跟做 A：进度环

1. 定义 `ProgressRing(done: Int, total: Int)`；`total == 0` 时进度为 0，避免除零。
2. 用 `Canvas(Modifier.size(80.dp))` 画灰色底环与彩色进度弧，`sweepAngle = 360f * done / total`。
3. 将可读文本“已完成 X / Y”放在画布旁，并提供合适语义；不要只靠颜色或弧长表达进度。
4. 试 `done = 0`、`done = total`、`total = 0`、`done > total`；为非法输入定义明确处理方式。

## 跟做 B：布局

1. 用 `FlowRow` 显示 8 个不同宽度的标签，记录小屏换行效果。
2. 再做一个只放 2 个元素的小型 `Layout`：测量两个 `measurable`，把第二个元素放在第一个右侧。比较代码量与 `Row`。
3. 删除手写 `Layout` 实验，保留更清晰的标准布局；写下何种特殊约束才值得自定义。

## 验收

- 进度 0%、50%、100% 的绘制正确；零任务不崩溃。
- 大字号下文字仍可读，图形不遮挡操作。
- 能解释为什么 `Canvas` 不会自动让 TalkBack 读出进度。

## 提示

`drawArc(color, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 8.dp.toPx()))`。`progress` 先用 `(done.toFloat() / total).coerceIn(0f, 1f)` 推导；零总数单独处理。

官方资料：[自定义绘制](https://developer.android.com/develop/ui/compose/graphics/draw/overview)、[自定义布局](https://developer.android.com/develop/ui/compose/layouts/custom)、[渲染阶段](https://developer.android.com/develop/ui/compose/phases)。
