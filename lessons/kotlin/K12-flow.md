# K12 · Flow 与 StateFlow

[Kotlin 目录](README.md) · [上一章](K11-coroutines.md) · [下一章](K13-interop.md)

## 1. 今天只解决这个问题

能区分流的创建、收集、冷流与当前状态，避免无限等待与重复工作。

**前置：**先完成 [K11](K11-coroutines.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K12 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K12.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 多次产生值

suspend 函数通常交回一次结果；Flow<T> 可以随着时间产生多个 T。`flow { emit(1); emit(2) }` 创建一个流对象，不立即执行块。终端操作 collect/toList 才开始收集；map 返回转换后的流，本身也不启动收集。

### 冷流：每次收集执行一遍

本例 starts 放在 flow 块里，所以建立 source 时仍是 0。第一次 toList 令 starts=1，第二次令 starts=2，两次都得到 [10,20]。如果块里请求网络，重复收集就可能重复请求。这不是所有 Flow 实现的统一规则，而是本例 flow 构建器的冷流行为。[flow 构建器官方契约](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flow.html) 说明每次终端收集会启动代码块。

### StateFlow 保存当前值

MutableStateFlow(initial) 需要初始状态，value 可立即读取。它是热状态流，后来的收集者可得到当前状态，通常持续存在而不是自动结束。对 StateFlow 直接 toList 会一直等待完成，不能用冷流有限结果的习惯对待它。

相等的新值通常不会产生新通知；快速连续更新还可能合并，中间值不保证逐一送到慢收集者。StateFlow 适合“现在是什么”，若每个事件都必须处理，应明确事件交付机制，不能假设状态流会保留完整事件历史。

### 更新与生命周期

`state.value = state.value + 1` 在本课单次执行中足够；并发修改一般用 `update { it + 1 }` 等原子更新。update 的函数可能被重试，因此不要在里面发送邮件或累加外部副作用。MutableList 原地修改可能仍与旧 value 相等，优先发布新状态。

Android UI 常用 collectAsStateWithLifecycle 观察状态，与可见生命周期协调；对应依赖在 Compose 08 讲义。冷流的收集取消由作用域控制，flowOn 改变上游上下文而不是所有代码。网络、重试和共享策略应随具体需求选择。

## 4. 暂停：先预测，不要点示范

仅创建 source 时 starts 是几？两次 toList 后是几？为什么不能把 StateFlow.toList() 当作读取当前值？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K12**，点击“运行参考示范”。完整示范的输出是：

```text
[10, 20] / [10, 20]；启动=2；state=1
```

![K12 参考示范的真实模拟器运行截图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K12-running.png)

这是本项目 Kotlin 练习页的真实运行截图；图中“参考示范”结果来自示范函数，你的练习按钮读取另一份代码。

<details>
<summary>展开预测解析：先写出自己的答案</summary>

创建时 0，两次收集后 2。StateFlow 通常不会完成，当前值直接读 value，UI 使用生命周期感知的收集。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise12()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K12，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：建立有限冷流

在 suspend 练习中用 flow 发出 1 与 2，toList 后返回列表字符串。导入 flow、map、toList。

### 小步 2：收集两次

在 flow 块里增加 starts++。第一次收集之后再收集一次，观察两份结果与启动次数。

### 小步 3：对照当前状态

声明 MutableStateFlow(0)，更新 value=1，直接读取 value。不要对它无期限 toList。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise12 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
var starts = 0
val source = kotlinx.coroutines.flow.flow { starts++; emit(1); emit(2) }
val first = source.map { it * 10 }.toList()
val second = source.map { it * 10 }.toList()
val state = kotlinx.coroutines.flow.MutableStateFlow(0)
state.value = 1
return "$first / $second；启动=$starts；state=${state.value}"
```

创建时 0，两次收集后 2。StateFlow 通常不会完成，当前值直接读 value，UI 使用生命周期感知的收集。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| 点击后一直检查中 | 可能对 StateFlow 用无限 collect/toList；返回页面取消，再读取 value 或有界收集。 |
| 同一请求重复执行 | 冷流每次收集启动上游；先查有多少收集者。 |
| 列表改了却没收到状态 | 原地修改同一对象可能未产生相等性变化；发布新快照。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

把转换链改成只保留偶数再乘 10；发出 1、2、3、4，结果应该 [20,40]。导入 filter。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
val source = kotlinx.coroutines.flow.flow {
    for (number in 1..4) emit(number)
}
val result = source.filter { it % 2 == 0 }.map { it * 10 }.toList()
return result.toString()
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 08 从 StateFlow 得到 UiState；16 Room 查询常产生 Flow；17 取消旧请求避免旧结果覆盖新状态。不要直接在 Composable 函数体无限 collect。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 两份结果都是 [10,20]，starts=2。
- [ ] 能解释 flow/map/toList 的执行时机。
- [ ] 知道 StateFlow 不保证交付每个快速中间值。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K11-coroutines.md) · [下一章](K13-interop.md)
