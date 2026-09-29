# 10 · 测试行为，而不是实现

**目标**：给任务清单写能发现真实回归的 UI 测试，再给纯逻辑写单元测试。预计 90–120 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 10 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/10.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 10 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/10.png)

**看图先猜**：一条只检查“按钮存在”的测试，能发现忘记 `trim()` 吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 写下用户步骤：输入空格 → 点击添加 → 数量仍为零。
2. 用测试 API 对输入框执行输入和点击；先让断言通过。
3. 故意临时删掉 `trim()`，确认测试失败，再恢复实现。

**停下来验收**：测试能因真实行为回归而失败，恢复后再次通过。

**若结果不同**：如果测试偶尔失败，先去掉固定 `delay`，使用框架等待界面空闲。
<!-- visual-end -->

## 讲解

Compose 测试通过语义树查找控件，而不是 View ID。测试应像用户一样输入、点击、断言结果。按钮文本在本地化后可能变化，关键控件可设置稳定 `testTag`；无障碍标签不是专门为测试而生，不要用无意义文案污染它。

## 动手步骤

1. 先在 `app/build.gradle.kts` 加入 Compose BOM 对应的 `androidTestImplementation(composeBom)`、`androidTestImplementation("androidx.compose.ui:ui-test-junit4")` 和 `debugImplementation("androidx.compose.ui:ui-test-manifest")`；再按官方测试文档加入 AndroidX test runner，并在 `defaultConfig` 设置 `testInstrumentationRunner`。
2. 给输入框、添加按钮、统计文字设置语义标签或可查找文本。
3. 写一条流程：输入 A → 点击添加 → 勾选 A → 断言“已完成 1 / 总共 1”。
4. 将 `trim()`、空标题拦截、过滤逻辑提成普通 Kotlin 函数；给边界条件写 JVM 单元测试。
5. 增加第二条 UI 测试：添加 A、B → 完成 A → 过滤未完成 → 删除 B → 验证过滤空状态。运行 `./gradlew :app:connectedDebugAndroidTest`（需要已启动设备）。

## 验收

- 测试在模拟器上通过；故意删掉 `trim()` 后有测试失败。
- 测试不依赖 `delay(1000)` 猜 UI 何时稳定；使用 Compose 测试框架的同步能力。
- 两条 UI 测试可重复运行；顺序颠倒也能通过，说明测试数据互不污染。

## 提示

```kotlin
composeRule.onNodeWithTag("task-input").performTextInput("A")
composeRule.onNodeWithText("添加").performClick()
composeRule.onNodeWithText("A").assertExists()
```

不要为每个 `Text` 写一条镜像式测试。优先覆盖添加、过滤、删除这类关键行为。

## 调试练习

故意把删除回调改成“删除错误的 ID”，确认第二条测试失败；修复后重新运行。若测试仍通过，说明断言没有覆盖真实结果，需要改进测试。

官方资料：[Compose 测试](https://developer.android.com/develop/ui/compose/testing)。
