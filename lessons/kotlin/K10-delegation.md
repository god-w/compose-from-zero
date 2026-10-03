# K10 · 委托属性与 by

[Kotlin 目录](README.md) · [上一章](K09-generics.md) · [下一章](K11-coroutines.md)

## 1. 今天只解决这个问题

能把委托语法展开成读写调用，并区分 Kotlin 语法与 Compose 状态行为。

**前置：**先完成 [K09](K09-generics.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K10 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K10.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 把属性读写交给另一个对象

`var score by KotlinScore()` 表示创建一个委托对象，score 的读取调用 getValue，赋值调用 setValue。本项目的 KotlinScore 用 ReadWriteProperty 实现二者，并在写入时将数值限制到 0..10。`score = 99` 最终保存 10，规则来自委托实现，不是 by 关键字自带。

getValue 的 thisRef 是属性所属对象，property 带有属性名等元数据。局部属性在本例中不需要所属对象，所以用 Any?。也能通过 operator getValue/setValue 按约定实现，不一定要实现接口。

### 展开语法再看简写

概念上先创建 delegate；读 score 相当于向 delegate 请求值；写 score 相当于将新值交给 delegate。实际编译转换还涉及属性元数据。知道读写去了哪里，就能查到校验、缓存、通知的来源。

### lazy 解决初始化时机

`val label by lazy { ... }` 第一次读取才执行初始化块，之后使用缓存值。本例两次读取，calls 只增加一次。默认 lazy 有线程安全策略，但不是所有自定义委托都有；lazy 的 val 不允许重新赋值。它也不是异步加载工具。

### Compose 的 by 仍然是 Kotlin 委托

`var count by remember { mutableStateOf(0) }` 可拆为记住容器、建立可观察状态、通过委托访问 value 三部分。remember 负责组合中的记忆，MutableState 的实现负责可观察读写，by 负责语法。普通 `var score by KotlinScore()` 没有建立 Compose 状态通知。Compose 中 getValue/setValue 扩展还需要对应 import。

类委托 `class Wrapper(service: Service) : Service by service` 是转发接口成员，与本课的属性委托相关但不是相同语法位置；别把两者误认成继承。

## 4. 暂停：先预测，不要点示范

写 score=99 后读取为何是 10？label 读取两次 calls 为何只有 1？去掉 by 会发生什么变化？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K10**，点击“运行参考示范”。完整示范的输出是：

```text
分数=10；初始化=1
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

KotlinScore.setValue 限制范围；lazy 缓存首次结果。去掉 by 时 score 变成委托对象变量，不能再按 Int 属性那样赋值。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise10()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K10，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：先体验自定义委托

声明 var score by KotlinScore()，赋值 99 后 return score.toString()。

### 小步 2：加入延迟初始化

声明 calls 与 label by lazy，先不读取 label，确认 calls 为 0。

### 小步 3：再读取两次

读取 label 两次，再返回分数与初始化次数。查 KotlinExamples.kt 的 getValue/setValue，说明保存了什么。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise10 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
var score by KotlinScore()
score = 99
var calls = 0
val label by lazy { calls++; "已初始化" }
check(label == label)
return "分数=$score；初始化=$calls"
```

KotlinScore.setValue 限制范围；lazy 缓存首次结果。去掉 by 时 score 变成委托对象变量，不能再按 Int 属性那样赋值。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| getValue/setValue 编译错误 | 确认导入 Compose 委托扩展或实现正确的 Kotlin 委托约定。 |
| 普通 by 属性更新但 UI 不刷新 | 委托没有自动提供 Compose 可观察状态。 |
| lazy 始终没初始化 | 代码尚未读取被委托属性；创建委托不是读取值。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

不用 by 的读取简写，展示 Compose 状态容器的 value；再写等价委托形式。此题读代码，无需把 Composable 放进 Kotlin 练习函数。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
// 以下放在 @Composable 函数中；需要 runtime 的 getValue/setValue 导入。
// val state = remember { mutableStateOf(0) }
// Text("${state.value}")
// Button(onClick = { state.value += 1 }) { Text("+1") }
// 等价属性访问：var count by remember { mutableStateOf(0) }
// 然后读 count、写 count += 1。
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 02 最常见的 by 来自语言委托；阅读状态代码时逐层识别“容器”“记忆”“可观察性”“属性访问”，不要只背整句。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 输入 -1、5、99 分别读到 0、5、10。
- [ ] 第一次读取前 calls=0，两次读取后 calls=1。
- [ ] 能分别说明 remember、mutableStateOf、by 的职责。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/delegated-properties.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K09-generics.md) · [下一章](K11-coroutines.md)
