# K09 · 泛型与类型边界

[Kotlin 目录](README.md) · [上一章](K08-sealed-state.md) · [下一章](K10-delegation.md)

## 1. 今天只解决这个问题

能辨认 T 与具体类型、可空返回值、协变与逆变，并知道 reified 的使用边界。

**前置：**先完成 [K08](K08-sealed-state.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K09 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K09.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 把类型留给调用处

`fun <T> kotlinFirst(values: List<T>): T?` 的 T 是类型参数，不是变量。传入 List<String> 时，返回 String?；传入 List<Int> 时，返回 Int?。具体 T 由调用上下文推断，也可显式写 `kotlinFirst<String>(...)`。泛型保留类型关系，避免每种类型复制一份实现。

### 边界决定函数能做什么

没有非空上界时 T 可以是可空类型。`<T : Any>` 限制 T 非空，但返回 T? 仍可表示没有元素。`<T : Comparable<T>>` 允许实现依赖比较的函数。不要为“更通用”添加用不到的多个边界。

### out 与 in 从读写方向理解

只生产 T 的容器可用 out T，让 List<String> 作为 List<Any> 读取；只消费 T 的类型可用 in T。可读写的 MutableList 通常不能这样转换：若把 MutableList<String> 当 MutableList<Any>，别人就可能塞进 Int。`List<*>` 表示元素具体类型未知，读出只能根据上界处理，并不等于可以任意写入。

### JVM 类型擦除与 reified

普通泛型函数体不能直接做 `value is T`，JVM 通常没有完整保留 T 的运行时信息。`inline fun <reified T> ...` 在内联调用处保留可用类型信息，本项目已有 kotlinFirstOfType。此类函数放文件顶层或合适成员位置，不能把本课 inline 示例声明成局部函数。

reified 不是任意深层类型检查器：`List<String>` 的元素类型不会因此自动全部验证。先掌握 List<T> 的静态关系，再阅读内联实现。inline 常用于高阶函数，也允许某些非局部 return；不是给所有函数自动提速的按钮。

## 4. 暂停：先预测，不要点示范

kotlinFirst(listOf("A", "B")) 的类型是什么？空列表呢？List<String> 可只读作 List<Any>，为何不能把 MutableList<String> 任意当成 MutableList<Any>？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K09**，点击“运行参考示范”。完整示范的输出是：

```text
首项=A；首个数字=7
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

结果 String?，值 A；空列表返回 null。可变容器若允许这个转换就能写入错误类型，破坏原来的 String 承诺。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise09()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K09，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：调用已有泛型

先调用 kotlinFirst(listOf("A", "B"))，用 IDE 悬浮查看返回类型。

### 小步 2：检查缺少元素

把输入换为空 List<String>，观察 null，不用 !!。

### 小步 3：体验运行时类型筛选

调用 kotlinFirstOfType<Int>(listOf("A", 7))。泛型帮助函数位于 KotlinExamples.kt，不要在练习内部再写 inline 声明。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise09 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
val first = kotlinFirst(listOf("A", "B"))
val number = kotlinFirstOfType<Int>(listOf("A", 7))
return "首项=$first；首个数字=$number"
```

结果 String?，值 A；空列表返回 null。可变容器若允许这个转换就能写入错误类型，破坏原来的 String 承诺。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| Cannot check for instance of erased type | 普通 T 不能直接 is T；使用已有 reified 函数或显式类型策略。 |
| Local inline functions are not supported | 声明移到文件顶层；练习函数内只调用已有实现。 |
| 泛型推断失败 | 空集合缺乏上下文时显式 emptyList<String>()。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

寻找混合列表中的首个 String 和首个 Double；没有匹配时用“没有”兜底。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
val values: List<Any> = listOf(7, "A", true)
val text = kotlinFirstOfType<String>(values) ?: "没有"
val decimal = kotlinFirstOfType<Double>(values)?.toString() ?: "没有"
return "文本=$text；小数=$decimal"
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose MutableState<Int>、StateFlow<TaskUiState>、List<Task> 的尖括号描述容器中的类型。读懂泛型签名比记住尖括号更有用。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 给 String、Int、空列表三种调用标注返回类型。
- [ ] 解释 MutableList 为何不能随意改变类型。
- [ ] 能说明 reified 并不能验证任意嵌套泛型元素。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/generics.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K08-sealed-state.md) · [下一章](K10-delegation.md)
