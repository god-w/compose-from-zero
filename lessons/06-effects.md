# 06 · 副作用：计时任务为什么不能写在函数体里？

**本章目标：**理解组合管理的协程、key 和取消；写一个离开页面会停止的计时器。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 06 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/06.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 06 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/06.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 接着重组知识思考

第二章知道 Composable 内容可能再次执行。如果你在函数体里直接启动计时任务，每次执行都可能多启动一个，速度越来越快。界面描述不应靠“只执行一次”的假设注册监听、发网络请求或启动计时。

“副作用”是界面描述之外的工作，例如协程、监听器与资源操作。本章用计时器把生命周期看得见。

## 2. 三个工具的责任

| 需求 | 使用方式 | 由谁决定生命周期 |
| --- | --- | --- |
| 内容出现时启动一项挂起工作 | LaunchedEffect(key) | 当前组合位置与 key |
| 用户点击才启动协程 | rememberCoroutineScope().launch | 用户触发，作用域随组合结束取消 |
| 注册并注销监听器 | DisposableEffect(key) / onDispose | 当前组合位置与 key |

今天先写 LaunchedEffect；后两种用小片段认识，不需要同时塞入计时器。

## 3. 文件位置和导入

修改 `Exercises.kt` 的 `EffectExercise`，加入：

```kotlin
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
```

### 第一步：只建立计时器

```kotlin
@Composable
private fun EffectExercise() {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000)
            seconds++
        }
    }
    Text("已进入 $seconds 秒")
}
```

先预测：更新 seconds 引起重组，会不会每秒新建一个计时协程？不会。当前位置仍在组合中且 key 不变，原 effect 持续工作。

`Unit` 在这里是不会变的 key，表示不用因为参数变化重启；它不代表“整个 app 永远只运行一次”。退出本页，函数离开组合，协程取消；回来是新的进入，会再次启动。

运行：停留约 3 秒 → 返回首页 → 等 3 秒 → 再进。这个版本使用 remember，回来从 0 附近重新开始。它是展示任务生命周期的计数，不是精确计时器；真实计时应依据时钟差值计算。

## 4. 第二步：让 key 改变能被观察

```kotlin
@Composable
private fun EffectExercise() {
    var seconds by remember { mutableIntStateOf(0) }
    var session by remember { mutableIntStateOf(0) }
    LaunchedEffect(session) {
        seconds = 0
        while (isActive) {
            delay(1000)
            seconds++
        }
    }
    Column {
        Text("第 $session 轮：$seconds 秒")
        Button(onClick = { session++ }) { Text("重新开始") }
    }
}
```

点“重新开始”，key 从 0 到 1：旧协程取消，新协程启动，并重置 seconds。不要把 seconds 自己作为 key，否则每次加一都会取消和重启工作。

![第 06 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/06-experiment.png)

示意比较持续运行与重新进入；页面退出是实验动作的一部分。

![第 6 课真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/06-running.png)

## 5. 把“取消”讲清楚

协程取消是合作式的；delay 会响应取消。阻塞操作、吞掉取消异常，可能阻碍及时停止。不要用 Thread.sleep 替换 delay 来模拟同一行为。

LaunchedEffect 随组合生命周期，并不自动等同于 Activity 的 STARTED/STOPPED。按 Home 后内容可能仍在组合中；**本章验证返回课程首页导致内容移除，不承诺按 Home 就停止。**如果需求是仅在前台运行，需结合生命周期 APIs。

注册传统监听器的模式如下，阅读即可：

```kotlin
DisposableEffect(source) {
    source.addListener(listener)
    onDispose { source.removeListener(listener) }
}
```

这里 `source` 与 `listener` 是你的现有监听对象，片段不是计时器可直接复制的实现。关键是一进一出使用同一监听器。

## 6. 理解检查

1. 重组与退出组合有什么区别？
2. 为什么不把 seconds 放进 key？
3. 点击按钮才启动的下载适合直接放函数体吗？

<details><summary>解析</summary>

1. 重组更新当前内容；退出组合移除这部分内容，其 effect 会结束。
2. seconds 每次改变都重启 effect，形成不必要的反馈循环。
3. 不适合。应由事件启动协程，并明确下载应归属界面作用域还是更长寿命的状态持有者。

</details>

## 7. 迁移练习与排错

把显示改为倒计时 5、4、3、2、1、0，到 0 不继续减；重新开始仍能启动新一轮。写下每一步的 key 和预期结果。

| 现象 | 检查 |
| --- | --- |
| 计数加速 | 是否在函数体每次启动一个 Job？ |
| 一直重置 | key 是否使用了不断变化的 seconds？ |
| 离开后仍在跑 | 是否移除了内容？任务是否实际属于全局 scope？ |
| 页面卡住 | 是否阻塞主线程或用 Thread.sleep？ |

完成标准：能展示进入、持续、重启、退出四个行为，并解释作用域。官方核对：[副作用](https://developer.android.com/develop/ui/compose/side-effects)、[协程取消](https://kotlinlang.org/docs/cancellation-and-timeouts.html)。

---

上一章：[第 05 章](05-list.md) · 下一章：[第 07 章](07-capstone.md)
