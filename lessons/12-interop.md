# 12 · 在已有 Android 项目中使用 Compose

**目标**：掌握渐进迁移：在 View 页面里放 Compose，在 Compose 里复用现有 View。预计 60–90 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 12 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/12.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 12 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/12.png)

**看图先猜**：已有 Fragment 只想迁移一小块 UI，必须整页重写吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 在旧页面找一个边界清楚的区域，先画出 View 与 Compose 的交界。
2. 用 `ComposeView` 放一个静态文本，确认能显示。
3. 再用 `AndroidView` 包装一个旧 View，并在状态变化时验证 `update` 被调用。

**停下来验收**：新旧区域都能更新，退出页面后没有残留监听器或计时任务。

**若结果不同**：如果旧 View 只显示初值，检查后续变化是否写在 `update` 而非只写在 `factory`。
<!-- visual-end -->

## 两个方向

- **View → Compose**：在旧 XML 页面放 `ComposeView`，调用 `setContent { ... }`。Fragment 中应设置与 View 生命周期匹配的 composition strategy，避免销毁 View 后组合仍在。
- **Compose → View**：用 `AndroidView(factory = { context -> LegacyView(context) }, update = { view -> ... })` 包装尚未迁移的控件。`factory` 创建，`update` 响应 Compose 状态变化。

## 动手步骤

1. 新建一个传统 `Activity` 或 `Fragment`，只把资料卡改成 `ComposeView`，其他 UI 保持 View。
2. 从旧页面传 `name` 给 Composable；改变名字时确认界面更新。
3. 在毕业项目中用 `AndroidView` 嵌入一个简单 `TextView`，让它显示任务总数；之后再替换回 `Text`。
4. 给旧页面加一个 `EditText`，把输入值传给 `ComposeView`；更新后确认两个界面读的是同一份状态。

## 验收

- 旧页面可局部使用 Compose；关闭页面后没有继续运行的计时器或监听器。
- `AndroidView` 中更新文本写在 `update`，而不是只在 `factory` 设置一次。
- 能解释何时保留已有 View 更划算，何时迁移整个页面更简单。
- 反复进入退出 Fragment 10 次，没有重复注册监听器或持续运行的副作用。

## 提示

```kotlin
AndroidView(
    factory = { context -> TextView(context) },
    update = { view -> view.text = "任务总数：$count" }
)
```

在 Fragment 内创建 `ComposeView` 时按官方文档设置适合 View 生命周期的 `ViewCompositionStrategy`。迁移时先保持现有数据来源不变，只换一小块 UI，便于比较行为。

官方资料：[Compose 与 View 互操作](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis)。

第 20 课会用一个新的“习惯打卡”应用检验独立交付能力。
