# K03 · 可空类型与安全输入

[Kotlin 目录](README.md) · [上一章](K02-functions.md) · [下一章](K04-lambdas.md)

## 1. 今天只解决这个问题

能区分 null、空与空白，并把不可信文本转换为有明确兜底的数据。

**前置：**先完成 [K02](K02-functions.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K03 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K03.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 为什么类型里多一个问号

`String` 承诺非空；`String?` 允许字符串或 null。null 表示没有值，`""` 是一个长度为 0 的字符串，`"   "` 是有三个空格的字符串。它们是三种输入，不能只测其中一种。

### 先写长形式，再读简写

对于局部稳定的 `val raw: String?`，`if (raw != null) raw.trim()` 的非空分支里编译器能够进行智能转换。对可能随时改变的属性，编译器未必能保证两次读取相同；先保存为局部 val 再判断。

`raw?.trim()` 只在 raw 非空时调用 trim，否则整个表达式是 null。链式 `?.` 把空值继续向后传递。它不会把空字符串自动变成 null，也不会拦截所有异常。

### 给结果一条兜底路

`value ?: "匿名"` 在左边为 null 时使用右边，称为 Elvis 运算符。`takeIf { it.isNotEmpty() }` 在条件满足时保留字符串，不满足时变成 null。先 trim 再 takeIf，才能把纯空格视为没有可用名字。

### 不要强迫输入兑现承诺

`"42".toIntOrNull()` 解析成功返回 Int，否则返回 null；`toInt()` 对非法输入会抛异常。`!!` 表示你强行断言非空，断言错了会崩溃。它适合你能证明的条件，不适合处理用户输入。require 用来拒绝不符合函数契约的参数，check 检查程序内部状态；UI 输入应尽量转成用户能理解的校验结果。

## 4. 暂停：先预测，不要点示范

raw=null 时 raw?.trim() 是什么？raw="   " 时只写 raw?.trim() ?: "匿名" 会显示什么？"x".toIntOrNull() 是什么？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K03**，点击“运行参考示范”。完整示范的输出是：

```text
匿名；42
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

依次为 null、空字符串、null。Elvis 只兜底 null；空白要先清洗再筛掉。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise03()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K03，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：允许没有名字

写 val raw: String? = null，只返回 raw ?: "匿名"，观察 null 情况。

### 小步 2：清洗空白

加入 ?.trim()?.takeIf。分别把 raw 改成 null、""、"   "、" 小明 "，每次只改输入。

### 小步 3：安全解析年龄

用 toIntOrNull 为年龄字符串兜底。错误输入应得到默认值，而不是点击后崩溃。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise03 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
val raw: String? = null
val name = raw?.trim()?.takeIf { it.isNotEmpty() } ?: "匿名"
val age = "42".toIntOrNull() ?: 0
return "$name；$age"
```

依次为 null、空字符串、null。Elvis 只兜底 null；空白要先清洗再筛掉。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| 画面名字仍然空白 | Elvis 不处理空字符串；确认 takeIf 在 trim 后调用。 |
| 点击出现 NumberFormatException | 检查是否用了 toInt 而不是 toIntOrNull。 |
| NullPointerException | 检查 !! 和 Java 平台类型；回到可空类型和安全访问。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

年龄只有 0..150 才有效。无效时显示“年龄无效”，而非默默当成 0。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
val age = "151".toIntOrNull()?.takeIf { it in 0..150 }
return age?.let { "年龄=$it" } ?: "年龄无效"
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 03 的输入先清洗再验证；09/18 的 selectedId 常是 Int?。09 中找不到任务是数据条件，不该靠 !! 把它变成崩溃。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] null、空串、空格、正常名字四种输入分别运行。
- [ ] 对 42、x、空串做安全数字解析。
- [ ] 不用 !! 完成整个练习。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/null-safety.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K02-functions.md) · [下一章](K04-lambdas.md)
