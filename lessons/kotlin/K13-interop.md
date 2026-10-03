# K13 · 注解与 Java 互操作

[Kotlin 目录](README.md) · [上一章](K12-flow.md) · [下一章](K14-integration.md)

## 1. 今天只解决这个问题

能解释注解的作用边界，安全处理平台类型，并读懂旧 Android/Java API。

**前置：**先完成 [K12](K12-flow.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K13 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K13.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 注解附着元信息

`@Composable`、`@JvmStatic`、`@Deprecated` 都是注解写法。它们本身不是函数调用；具体作用来自编译器、工具或运行时读取者。没有相应处理者，你自己声明的注解不会自动生成日志、线程切换或界面。

自定义注解用 `annotation class`，Target 限定可贴的位置，Retention 指定是否进入二进制、是否运行时可见。先掌握“谁读取这个注解”，再决定是否需要自定义注解。Compose 的注解涉及编译器变换和调用约束，具体机制回到 Compose 01。

### Java 未标明可空性时

本项目的 LegacyNames.java 返回 String，但在 available=false 时返回 null。Kotlin 常把这种未注解 Java 类型展示为 String! 平台类型：编译器允许一些非空操作，可运行时仍可能 null。String! 是 IDE 显示约定，不能直接写进 Kotlin 类型声明。

在边界主动接成 `val name: String? = ...`，再用 ?. 或 ?: 处理；不要看到 Java 的 String 就当作一定非空。带可靠可空注解的 API 会给更准确的 Kotlin 类型。

### 从 Kotlin 暴露给 Java

object 中普通函数由 Java 通过 INSTANCE 调用；@JvmStatic 为合适位置提供静态桥接。例如 Java 可调用 `KotlinGreetings.greet("小明")`。Kotlin 自己也能直接通过对象名调用，与是否写 @JvmStatic 无关。@JvmOverloads 可为默认参数生成部分重载，但不要为所有函数都加；只有 Java 调用需要时考虑。

### SAM 与默认参数边界

Java 单抽象方法接口常可用 lambda 实现，例如 Runnable { ... }。这是一种接口转换，不等于所有多方法接口都能传一个 lambda。Java 方法不能像普通 Kotlin 函数那样随意使用 Kotlin 具名实参；可用 IDE 查看签名，按位置传入。

## 4. 暂停：先预测，不要点示范

LegacyNames.optionalName(false) 返回什么？赋给 String 后马上读 length 是否可靠？加一个自己写的 @Trace 注解会自动打印吗？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K13**，点击“运行参考示范”。完整示范的输出是：

```text
匿名；你好，小明
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

返回 null；应接成 String? 并处理，没有保证时直接非空使用可能崩溃。自定义注解没有自动执行能力，需要读取或处理它的机制。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise13()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K13，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：查真实 Java 源码

打开 LegacyNames.java，确认 false 分支确实返回 null，而不是只看函数签名猜测。

### 小步 2：建立安全边界

val name: String? = LegacyNames.optionalName(false)，用 ?: 显示匿名。

### 小步 3：调用 Kotlin 对象

调用已有 KotlinGreetings.greet，查定义处的 @JvmStatic，说明它主要方便哪一侧调用。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise13 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
val name: String? = LegacyNames.optionalName(false)
return "${name ?: "匿名"}；${KotlinGreetings.greet("小明")}"
```

返回 null；应接成 String? 并处理，没有保证时直接非空使用可能崩溃。自定义注解没有自动执行能力，需要读取或处理它的机制。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| Java 返回 String 仍崩溃 | 平台类型可包含 null；显式可空边界并验证来源。 |
| Java 找不到静态函数 | 查 Kotlin 对象桥接；按需要使用 @JvmStatic 或 INSTANCE。 |
| 注解没执行代码 | 注解是元信息，确认是否存在处理者。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

安全计算旧 API 名字长度。false 返回 0，true 返回 2。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
val name: String? = LegacyNames.optionalName(false)
return "长度=${name?.length ?: 0}"
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 12 的 AndroidView 接入旧 Java/View API；01 的 @Composable 要配合编译器与调用环境理解。可空性要在互操作边界确认。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] false 显示匿名，true 显示小明。
- [ ] 说明 String?、String、IDE String! 的区别。
- [ ] 知道注解不等于立即执行。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/java-interop.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K12-flow.md) · [下一章](K14-integration.md)
