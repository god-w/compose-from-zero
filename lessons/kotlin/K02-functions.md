# K02 · 函数与控制流程

[Kotlin 目录](README.md) · [上一章](K01-values.md) · [下一章](K03-null-safety.md)

## 1. 今天只解决这个问题

能读懂函数签名、传入实参、返回结果，并用 if、when 和循环表达规则。

**前置：**先完成 [K01](K01-values.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K02 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K02.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 函数先看输入与输出

`fun greet(name: String): String`：`fun` 声明函数，括号内的 name 是形参，冒号后的 String 是返回类型。调用 `greet("小明")` 传入实参。块函数用 `return` 交回结果；只有一个表达式时可写 `fun greet(name: String) = "你好，$name"`。

不需要返回有用值的函数通常返回 Unit，例如打印日志。Unit 是一种类型；它不表示“可能返回 null”。函数可以写在文件顶层，不必塞进一个工具类，也能在函数内部声明局部函数供当前练习使用。

### 默认参数与具名实参

`prefix: String = "你好"` 给参数默认值；调用不传 prefix 时使用它。`greet(name = "小明", prefix = "欢迎")` 用参数名表达意思。名字写在**调用处**表示具名实参，写在**声明处**定义形参；两处必须对应。

### if 和 when 可以产生值

`val message = if (count == 0) "空" else "有内容"`：选择的分支就是表达式结果，因此作为值使用时需要覆盖两条路。`when (count) { 0 -> "空"; 1 -> "一项"; else -> "多项" }` 用箭头把匹配条件与结果连接。别把 `->` 与赋值符号混淆。

### 循环按顺序执行

`for (number in 1..3)` 遍历包含 1 和 3 的区间；`1 until 3` 只包含 1、2。`while` 先检查条件再执行，必须有改变条件的途径。break 退出循环，continue 跳过本次。计算 1+2+3 时，sum 的变化是 0→1→3→6。

## 4. 暂停：先预测，不要点示范

默认 prefix 是“你好”。greet(name = "小明") 返回什么？for 的范围从 1..3 改为 1 until 3，总和如何变化？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K02**，点击“运行参考示范”。完整示范的输出是：

```text
你好，小明；总和=6
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

问候为“你好，小明”。包含端点时总和 6，排除上端点时总和 3。函数声明不执行函数体，调用才执行。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise02()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K02，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：声明并调用

先在练习内声明 greet 局部函数，只 return greet(name = "小明")。验证默认值后显式传 prefix。

### 小步 2：累加三次

加入 sum 和 for。每次循环用 number 更新 sum，先手写三轮变化。

### 小步 3：组合返回值

用字符串模板同时显示 greet 的结果与 sum。练习是普通函数，不需要写 Composable。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise02 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
fun greet(name: String, prefix: String = "你好") = "$prefix，$name"
var sum = 0
for (number in 1..3) sum += number
return "${greet(name = "小明")}；总和=$sum"
```

问候为“你好，小明”。包含端点时总和 6，排除上端点时总和 3。函数声明不执行函数体，调用才执行。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| 函数没有返回值 | 块函数返回 String 时要使用 return；表达式函数用 =。 |
| 具名参数找不到 | 参数名要与函数声明对应；不是给当前函数创建一个新变量。 |
| 总和多一项 | .. 包含上端点；按需求选择 until。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

写任务摘要：0 项显示“空列表”，1 项显示“1 项”，其他值显示“共 n 项”；负数明确拒绝。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
fun summary(count: Int): String {
    require(count >= 0) { "数量不能为负" }
    return when (count) {
        0 -> "空列表"
        1 -> "1 项"
        else -> "共 $count 项"
    }
}
return summary(2)
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 01 中 Greeting(name = ...) 是函数调用；Modifier 的默认参数也用相同语法。读函数签名时先找参数、返回类型，再读注解。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 不看答案，写出一个有默认参数的问候函数。
- [ ] 比较 1..3 与 1 until 3 的输出。
- [ ] 把 if 写成产生 String 的表达式，并解释为何需要 else。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/functions.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K01-values.md) · [下一章](K03-null-safety.md)
