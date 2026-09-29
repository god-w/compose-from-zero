# 19 · 性能实战：先测再改

**目标**：在真实慢点出现时定位组合、布局或绘制的成本；用证据验证优化。预计 2–3 小时。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 19 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/19.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 19 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/19.png)

**看图先猜**：给每个表达式都加 `remember`，一定会让列表更快吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 选一个可重复的慢场景，记录设备、构建类型和操作。
2. 先测基线，再只改一个候选因素，例如列表 key。
3. 用同样操作复测；没有稳定改善就撤回改动。

**停下来验收**：能拿出同一场景前后的记录，而不是凭体感宣布优化成功。

**若结果不同**：如果结果波动大，先固定数据量和设备状态，多次测量再比较。
<!-- visual-end -->

## 观察模型

Compose 有组合、布局、绘制三个阶段。状态在不同阶段被读取，会触发不同范围的工作。重组本身不是错误；卡顿、长帧和用户感知延迟才是要解决的问题。调试版速度不能代表发布版，最终结论应在有代表性的设备和构建上验证。

## 跟做

1. 在任务清单生成 500 条测试数据，不要手工逐条输入；给列表项稳定 `key`。
2. 用 Layout Inspector 观察滚动与勾选时哪些 Composable 重组；记录一个可疑点。
3. 用 Android Studio Profiler/系统帧时间工具记录滚动前的慢帧和操作步骤。不要只写“感觉有点卡”。
4. 只修改一个原因，例如把昂贵过滤移出高频重组、给列表稳定 key、把只用于位置的状态读延后到 `Modifier.offset { ... }`。
5. 重复相同测量，保留前后结果。如果差异不稳定或不可见，撤销这次优化。

## 验收

- 有一份简短记录：设备、构建类型、场景、基线、改动、复测结果。
- 列表增删后项目状态不会错位；滚动时没有明显卡顿。
- 能解释 `remember`、`derivedStateOf`、稳定 key 各自解决什么问题，以及什么情况下不该加。

## 常见误区

- 为了减少重组，把所有状态塞进一个全局单例。
- 没有测量就给每个值加 `remember` 或 `derivedStateOf`。
- 只在 debug 构建测流畅度；或者改了多个因素却声称知道是哪一个有效。

**延伸**：对启动和长列表滚动加 Macrobenchmark/Baseline Profile；记录首次与预热后的差异。

官方资料：[Compose 性能](https://developer.android.com/develop/ui/compose/performance)、[渲染阶段](https://developer.android.com/develop/ui/compose/phases)、[性能实践](https://developer.android.com/develop/ui/compose/performance/bestpractices)。
