# K01 · 值、类型与字符串

[Kotlin 目录](README.md) · [下一章](K02-functions.md)

## 1. 今天只解决这个问题

能逐个解释 val、var、类型、赋值和字符串模板，并独立计算一个正确的完成比例。

**前置：**无 Kotlin 前置；只需知道在 Android Studio 中编辑文件、运行 app。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K01 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K01.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 先把一行拆开

`val name: String = "小明"` 从左到右读：声明一个不可重新赋值的名称 `name`，它的类型是 `String`，把右边字符串作为初始值。`=` 是赋值；判断相等用 `==`。`String` 前的大写字母是类型命名习惯。双引号包住字符串，单引号如 `'A'` 表示 `Char`。

写成 `val name = "小明"` 时，编译器从初始值推断为 String，类型仍然存在。Kotlin 是静态类型语言，不是“省略类型后就能随意改变类型”。`var count = 0` 推断为 Int，之后不能赋值为 `"三"`。

### val 固定的是绑定

`var count = 0` 后可以 `count = count + 1`；`count += 1` 是常见简写。`val` 不允许重新赋值，但如果它引用的是可变对象，对象内容仍可能改变。现在先用数字和字符串，K06–07 再亲手观察这个区别。普通 var 也不自带 Compose 的界面通知。

### 数字运算先看操作数类型

`5 / 10` 的两端都是 Int，得到整数 0。`5.toDouble() / 10` 有 Double 操作数，得到 0.5。转换是在运算**之前**进行；`(5 / 10).toDouble()` 只能得到 0.0。Int、Long、Float、Double 各有用途，`1L` 是 Long，`0.5f` 是 Float；变量赋值通常需要明确的数值转换。

### 把值放进句子

`"$name：$count"` 中 `$name` 会替换为变量值；包含运算或属性访问的表达式要写 `${...}`。字符串可以调用 `.length`、`.trim()`。`;` 可以分隔同一行的语句，但通常每行一句。`//` 后是注释，不参与运算。

## 4. 暂停：先预测，不要点示范

`count` 从 0 加 3；`5 / 10`、`5.toDouble() / 10`、`(5 / 10).toDouble()` 分别是什么？把 count 改成 val 后还允许 += 吗？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K01**，点击“运行参考示范”。完整示范的输出是：

```text
小明：3；比例=0.5
```

![K01 参考示范的真实模拟器运行截图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K01-running.png)

这是本项目 Kotlin 练习页的真实运行截图；图中“参考示范”结果来自示范函数，你的练习按钮读取另一份代码。

<details>
<summary>展开预测解析：先写出自己的答案</summary>

结果依次是 3、0、0.5、0.0；val 不允许重新赋值。先转换和后转换处理的是不同的值。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise01()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K01，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

![K01 尚未修改时的真实练习结果](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K01-practice-start.png)

开始时“我的练习”只显示 `小明：0`，这是正常的起点。完成下面三步后才会得到示范中的数值与比例。

### 小步 1：先改文字

保留起始函数，把 name 改为你自己的名字。点击“运行我的练习”，只观察名字变化。

### 小步 2：再改数值

在 return 前加入 count += 3。先预测数值，重建运行后应从 0 变成 3。

### 小步 3：最后算比例

return 改成下面答案格式。先用 5 / 10 观察 0，再将第一个数转换为 Double。不要同时改名字和计算，以便定位原因。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise01 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
val name = "小明"
var count = 0
count += 3
return "$name：$count；比例=${5.toDouble() / 10}"
```

结果依次是 3、0、0.5、0.0；val 不允许重新赋值。先转换和后转换处理的是不同的值。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| Val cannot be reassigned | 需要改变计数时使用 var；不要为了消除错误把所有声明改成 var。 |
| 比例一直为 0 | 检查除法发生时两端是否都是 Int。 |
| 屏幕没变化 | 修改的是 kotlinExercise01，且已重新构建安装；普通局部变量只在点击执行时运算。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

把固定 5 和 10 改成 done 与 total，要求 total 为 0 时返回 0.0，不能出现无穷大或 NaN。先尝试，再展开。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
val done = 5
val total = 0
val ratio = if (total == 0) 0.0 else done.toDouble() / total
return "比例=$ratio"
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 第 1 课的 Text 接收 String；第 2 课 count 是 Int；第 15 课 Canvas 常用 Float。先判断值的类型，再理解如何显示它。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 名字可以独立修改，数值可以独立修改。
- [ ] 口头解释每个符号：val、var、=、+=、$、${}。
- [ ] 用 0/10、5/10、10/10 三组输入验证比例。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/basic-syntax.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [下一章](K02-functions.md)
