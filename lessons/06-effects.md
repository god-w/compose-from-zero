# 06 · 副作用与生命周期

**目标**：让计时器在页面可见时运行、离开时停止；知道何时用 `LaunchedEffect`。预计 75 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 06 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/06.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 06 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/06.png)

**看图先猜**：离开计时器页面后，它的协程应该继续运行吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先只显示秒数和静态文字，不放循环。
2. 用 `LaunchedEffect(Unit)` 每秒更新一次，停在页面观察 3 秒。
3. 返回首页再进入，记录秒数从哪里开始；再加暂停状态。

**停下来验收**：离开页面后旧协程停止；重新进入的行为与你选定的状态作用域一致。

**若结果不同**：如果每次重组又启动一个循环，检查协程是否放在 Composable 函数体而非 effect 中。
<!-- visual-end -->

## 讲解

Composable 函数体可能反复执行，因此不能直接 `while(true)`、订阅监听器或发请求。`LaunchedEffect(key)` 在进入组合时启动协程；key 变化会取消旧任务并启动新任务；离开组合会取消。这里的 key 要反映任务的生命周期。

把“从用户点击开始的动作”放在事件回调或事件处理协程中；把“页面存在期间需要运行的动作”放在合适的 effect 中。`DisposableEffect` 适合必须注册并注销的监听器。

## 先看计时器，再自己写

![第 6 课在 Android 模拟器中运行四秒后的计时器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/06-running.png)

实际运行截图中计时器已增长到 4 秒。再点暂停、继续，验证你对 effect 生命周期的预测。

在第 6 课可操作示范中停留三秒，确认数字增长；点暂停并等待两秒，确认数字不变；点继续，再等两秒。退出示范并重新进入，观察是否从 0 开始。先用这些现象推断：循环在哪里启动？暂停时旧循环去了哪里？

## 按生命周期逐步实现

1. 在 `EffectExercise` 先写 `var seconds by remember { mutableIntStateOf(0) }` 和一个显示秒数的 `Text`。没有循环时，数字应保持 0。
2. 加 `LaunchedEffect(Unit)`，里面写 `while (true) { delay(1_000); seconds++ }`。运行并观察三秒。如果数字增长过快，检查是否把循环误放在 Composable 函数体。
3. 返回首页，再进入。旧页面离开组合时协程取消，新页面从 0 开始。旋转时 `remember` 与 `rememberSaveable` 对数字的影响不同，但 effect 都会随新组合重新启动。
4. 加 `running` 状态，用 `LaunchedEffect(running)`。只有 `running` 为真时循环；暂停时 key 变化，旧任务取消。恢复后继续当前秒数。

**想一想**：如果每次重组都直接启动一个协程，点暂停时会有多少个循环需要处理？`LaunchedEffect` 把启动和取消都交给组合生命周期。

## 任务

在 `EffectExercise`：

1. 显示秒数，从 0 开始。
2. 用 `LaunchedEffect(Unit)` 每秒加 1，内部 `delay(1_000)`。
3. 离开练习页再回来，观察计时重新开始；旋转设备时观察 `remember` 与 `rememberSaveable` 的区别。

## 验收

- 不点击也会增长；返回主页后不会继续运行；重新进入从 0 开始。
- 能解释为什么不能把 `delay` 放在 Composable 函数体。

## 提示

1. `LaunchedEffect(Unit) { while (true) { delay(1_000); seconds++ } }`。
2. `LaunchedEffect` 被取消时 `delay` 可取消；不需要手工维护 Thread。

<details><summary>参考实现</summary>

```kotlin
@Composable
private fun EffectExercise() {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            seconds++
        }
    }
    Text("已停留 $seconds 秒")
}
```

需要 `import androidx.compose.runtime.LaunchedEffect` 和 `import kotlinx.coroutines.delay`。

</details>

**变式**：加暂停按钮。把 `running` 作为 effect 的 key，暂停时不执行循环。再思考恢复后是否该从当前秒数继续。
