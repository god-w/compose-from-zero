# K11 · 协程与取消

[Kotlin 目录](README.md) · [上一章](K10-delegation.md) · [下一章](K12-flow.md)

## 1. 今天只解决这个问题

能解释挂起、作用域、等待、取消和调度，并写一段不会阻塞界面的并发练习。

**前置：**先完成 [K10](K10-delegation.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K11 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K11.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### suspend 表示允许挂起

挂起让当前协程等待而不必占住执行线程，例如 delay。它不是启动协程，也不承诺把代码移到后台。普通 CPU 运算放在 suspend 函数里仍可能阻塞当前线程；需要时用 withContext(Dispatchers.Default)，阻塞 I/O 可放 Dispatchers.IO，具体 API 若已自行调度则遵循其契约。

### 谁启动、谁拥有

App 的按钮用 rememberCoroutineScope().launch 启动练习。这个 scope 属于当前页面的组合，离开页面会取消其子任务。练习内 coroutineScope 为子任务建立结构化关系：正常返回前等待子任务完成，取消会传播。不要为了让代码“随时能运行”使用 GlobalScope 丢失生命周期。

### launch 与 async

launch 返回 Job，适合没有返回结果的任务；async 返回 Deferred<T>，用 await 获取结果。示例先启动 A/B，再依次 await，所以它们可以重叠等待。代码输出 A+B 是我们读取结果的顺序，不是任务完成顺序。若先 async().await() 再启动另一个，就可能失去想要的重叠。

### 取消需要合作

cancel 发出请求；cancelAndJoin 还等待任务退出。delay 等可挂起函数响应取消；长 CPU 循环应定期 ensureActive/yield，不能假设任意代码都会立刻停。协程取消常用 CancellationException 表示，catch(Exception) 时要优先重新抛出取消，避免把离开页面变成“请求失败”。

### 不要用时间巧合证明正确

本课 delay 只模拟等待，不比较设备耗时。我们验证组合结果与 Job.isCancelled。并发共享可变数据需要同步；本例两个任务各自返回值，再在父协程组合。普通协程不自动拥有 Compose 界面状态。

## 4. 暂停：先预测，不要点示范

B 的 delay 更短，最后字符串一定变成 B+A 吗？suspend 函数里加一个很长的 CPU 循环，能保证后台执行吗？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K11**，点击“运行参考示范”。完整示范的输出是：

```text
A+B；cancelled=true
```

![K11 参考示范的真实模拟器运行截图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K11-running.png)

这是本项目 Kotlin 练习页的真实运行截图；图中“参考示范”结果来自示范函数，你的练习按钮读取另一份代码。

<details>
<summary>展开预测解析：先写出自己的答案</summary>

输出按 await 的拼接顺序是 A+B；挂起不会自动改线程，长循环仍需调度与取消检查。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise11()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K11，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：先挂起一次

在 suspend 练习中 delay(50)，返回“完成”。按 IDE 提示导入 kotlinx.coroutines.delay。不要用 Thread.sleep。

### 小步 2：同时启动两个任务

用 coroutineScope 包住代码，先声明两个 async，再 await 组合结果。

### 小步 3：增加取消实验

launch 一个等待任务，cancelAndJoin 后读取 isCancelled。导入 async、launch、coroutineScope、cancelAndJoin。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise11 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
return kotlinx.coroutines.coroutineScope {
    val a = async { kotlinx.coroutines.delay(50); "A" }
    val b = async { kotlinx.coroutines.delay(20); "B" }
    val result = "${a.await()}+${b.await()}"
    val job = launch { kotlinx.coroutines.delay(1000) }
    job.cancelAndJoin()
    "$result；cancelled=${job.isCancelled}"
}
```

输出按 await 的拼接顺序是 A+B；挂起不会自动改线程，长循环仍需调度与取消检查。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| Suspend function should be called only... | 调用位于 suspend 函数或协程内部；不要在普通 Composable 函数体直接执行。 |
| 界面卡住 | 检查 Thread.sleep、阻塞调用和主线程 CPU 循环。 |
| 离开页面显示网络错误 | catch 中不要吞 CancellationException。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

用 try/finally 记录取消后的清理；先等子任务确实启动，再取消，不依赖猜测 delay 长短。需要导入 CompletableDeferred。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
return kotlinx.coroutines.coroutineScope {
    val started = kotlinx.coroutines.CompletableDeferred<Unit>()
    var cleaned = false
    val job = launch {
        try {
            started.complete(Unit)
            kotlinx.coroutines.delay(5000)
        } finally { cleaned = true }
    }
    started.await()
    job.cancelAndJoin()
    "清理=$cleaned"
}
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 06 的 LaunchedEffect 与页面生命周期相连；17 的请求需要正确取消。先理解结构化并发，再添加网络或数据库调用。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 示范结果 A+B；cancelled=true。
- [ ] 临时增大 delay 到 5000，运行后返回 Kotlin 目录，页面任务应被取消。
- [ ] 明确区分 launch、async、await、coroutineScope。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/coroutines-overview.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K10-delegation.md) · [下一章](K12-flow.md)
