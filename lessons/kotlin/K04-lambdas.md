# K04 · 函数类型与 lambda

[Kotlin 目录](README.md) · [上一章](K03-null-safety.md) · [下一章](K05-receivers.md)

## 1. 今天只解决这个问题

把回调还原成普通函数值，解释花括号、箭头、it 和尾随 lambda 的执行时机。

**前置：**先完成 [K03](K03-null-safety.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K04 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K04.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 把一个动作当作参数

之前函数参数是 Int 或 String，现在参数可以是一种函数：`action: (Int) -> Int` 表示接收一个 Int，返回一个 Int。`(String) -> Unit` 接收文本、没有有用返回值；`() -> Unit` 不接收参数。箭头左边是输入，右边是输出。

`fun applyOperation(value: Int, action: (Int) -> Int) = action(value)` 里，声明 action 不会执行动作；函数体中 `action(value)` 才调用它。事件回调什么时候执行，取决于持有回调的那段代码。

### lambda 是一种函数值写法

`val double: (Int) -> Int = { number -> number * 2 }`：花括号包住函数体，number 是参数，箭头后是运算。最后一个表达式作为返回值。只有一个参数且类型可推断时可写 `{ it * 2 }`；这里 it 是当前 lambda 的参数，不是任何外层对象。

### 括号外的花括号仍然是参数

当最后一个参数是函数类型时，`applyOperation(3, { n -> n * 2 })` 可写 `applyOperation(3) { n -> n * 2 }`。这是尾随 lambda，不是“函数调用后自动执行的另一个代码块”。只有 lambda 参数时，括号也可省略。

### 闭包与函数引用

lambda 能读取或修改外层变量，这种捕获称为闭包。例子里 calls 是同一局部计数，不是副本；action 被执行一次才增长一次。`::functionName` 引用已有函数，不是立即调用它。学习初期给嵌套参数明确命名，避免两个 it 混淆。

## 4. 暂停：先预测，不要点示范

仅创建 `{ calls++; number * 2 }` 会增加 calls 吗？applyOperation 调用一次后 result 与 calls 是什么？把 action(value) 改成 action(action(value)) 呢？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K04**，点击“运行参考示范”。完整示范的输出是：

```text
结果=6；调用=1
```

![K04 参考示范的真实模拟器运行截图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K04-running.png)

这是本项目 Kotlin 练习页的真实运行截图；图中“参考示范”结果来自示范函数，你的练习按钮读取另一份代码。

<details>
<summary>展开预测解析：先写出自己的答案</summary>

创建不会增加。调用一次得到 6 和 1；调用两次得到 12 和 2。回调交给谁执行比花括号在哪里更重要。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise04()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K04，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：先保存函数值

在练习里写 val double: (Int) -> Int = { n -> n * 2 }，return double(3).toString()。

### 小步 2：交给别的函数调用

声明 applyOperation，把 double 作为 action 传入。与直接调用比较结果。

### 小步 3：再用尾随形式

将 double 的函数体写到调用末尾，并捕获 calls。先解释调用时机，再运行。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise04 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
fun applyOperation(value: Int, action: (Int) -> Int): Int = action(value)
var calls = 0
val result = applyOperation(3) { number ->
    calls++
    number * 2
}
return "结果=$result；调用=$calls"
```

创建不会增加。调用一次得到 6 和 1；调用两次得到 12 和 2。回调交给谁执行比花括号在哪里更重要。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| Int 与 Unit 类型不匹配 | lambda 最后一行是不是 println？它返回 Unit，不能代替 Int。 |
| it 未定义 | 零参数 lambda 没有 it；多个参数时必须显式声明。 |
| 回调提前执行 | 传函数值如 ::greet，而不是 greet() 的执行结果。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

做 repeatAction(times: Int, action: () -> Unit)，执行三次令 calls 增加 3。不要在参数声明处执行 action。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
fun repeatAction(times: Int, action: () -> Unit) {
    repeat(times) { action() }
}
var calls = 0
repeatAction(3) { calls++ }
return "调用=$calls"
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 03 的 onValueChange: (String)->Unit 是候选文本事件；Button 的 onClick: ()->Unit 是点击动作。传回调本身不会点击按钮。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 用括号内形式和尾随形式实现同一结果。
- [ ] 口头指出谁调用 action、调用几次。
- [ ] 把 (Int)->Int 改为 (String)->Unit 并写一个匹配回调。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/lambdas.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K03-null-safety.md) · [下一章](K05-receivers.md)
