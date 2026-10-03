# K05 · 接收者、扩展与作用域函数

[Kotlin 目录](README.md) · [上一章](K04-lambdas.md) · [下一章](K06-classes.md)

## 1. 今天只解决这个问题

能辨认 this 与 it，按返回值选择作用域函数，并读懂 Compose 的接收者作用域。

**前置：**先完成 [K04](K04-lambdas.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K05 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K05.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 点号前是谁

`fun String.cleanTitle() = trim().uppercase()` 是扩展函数。String 是接收者类型，`" a ".cleanTitle()` 的接收者就是字符串。函数内可写 `this.trim()`，或省略 this。扩展没有修改 String 类源码，也不能越过 private 边界；同名成员优先于扩展。

### 接收者函数与普通参数

`(String) -> Int` 用参数接收 String；`String.() -> Int` 让 String 在函数体里成为 this。先分清这两种写法，Compose 的 RowScope.() -> Unit 就不会像魔法。接收者是一种访问对象的方式，不自动改变调用次数。

### 返回对象，还是返回计算结果

let/also 使用 it；apply/run/with 使用 this。let、run、with 返回 lambda 最后一个表达式；apply、also 返回原来的对象。`"A".let { it.length }` 得到 Int 1；`"A".also { it.length }` 得到 String "A"。不要仅凭代码更短来选函数。

| 函数 | 对象怎么引用 | 表达式最终返回 |
| --- | --- | --- |
| let | it 或命名参数 | lambda 结果 |
| run（接收者形式） | this | lambda 结果 |
| with(object) | this | lambda 结果 |
| apply | this | 原对象 |
| also | it 或命名参数 | 原对象 |

### 作用域没有赋予新能力

`StringBuilder().apply { append("A") }` 是配置对象的便捷写法；它没有线程调度或 null 校验。`nullable?.let { ... }` 的非空限制来自 ?.，不是 let 本身。`return@let` 退出当前 lambda；在可内联 lambda 里直接 return 可能退出外层函数。遇到多层嵌套就拆成有名字的局部值。

## 4. 暂停：先预测，不要点示范

" A ".trim().let { it.length } 返回几？若 let 改成 also，返回类型和结果是什么？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K05**，点击“运行参考示范”。完整示范的输出是：

```text
标题=A；长度=1
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

let 得到 Int 1；also 返回原字符串 "A"。它们都执行代码块，但表达式的最终返回值不同。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise05()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K05，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：写扩展

在练习内部声明 String.cleanTitle，调用一次，只返回清洗后的 title。

### 小步 2：求一个新值

用 title.let { it.length } 得到 length，先明确它是 Int。

### 小步 3：对照配置对象

独立写 StringBuilder().apply { append(title) }，比较 apply 返回对象与 let 返回长度。主练习保持下方格式。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise05 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
fun String.cleanTitle() = trim().uppercase()
val title = " a ".cleanTitle()
val length = title.let { it.length }
return "标题=$title；长度=$length"
```

let 得到 Int 1；also 返回原字符串 "A"。它们都执行代码块，但表达式的最终返回值不同。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| apply 后得到的不是长度 | apply 返回原对象；求长度用 let/run 或直接 .length。 |
| 嵌套 it 看不懂 | 写明确参数名，如 title ->，或拆开表达式。 |
| Modifier.weight 在任何地方都能用吗 | 它依赖特定 Compose 接收者作用域；Kotlin 的接收者决定可调用扩展范围。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

用 StringBuilder 接收者 lambda 构造“你好，小明”，写出函数签名和调用。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
fun buildGreeting(block: StringBuilder.() -> Unit): String {
    val builder = StringBuilder()
    builder.block()
    return builder.toString()
}
return buildGreeting { append("你好，"); append("小明") }
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 04 中 Row/Column 的 content 带接收者；weight 等 API 在对应作用域可用。先用 StringBuilder 理解 this，再去读布局作用域。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 对照表不看答案说出五种返回值。
- [ ] 用普通局部值改写 let 链，保持结果一致。
- [ ] 写一段接收者 lambda，明确 this 的类型。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/scope-functions.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K04-lambdas.md) · [下一章](K06-classes.md)
