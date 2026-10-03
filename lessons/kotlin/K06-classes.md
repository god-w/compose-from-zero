# K06 · 类、属性与数据类

[Kotlin 目录](README.md) · [上一章](K05-receivers.md) · [下一章](K07-collections.md)

## 1. 今天只解决这个问题

能定义一个有身份的任务对象，理解 copy、相等与共享可变属性。

**前置：**先完成 [K05](K05-receivers.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K06 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K06.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 类描述对象拥有的数据与动作

`class Person(val name: String)` 同时声明主构造函数参数和只读属性；`Person("小明")` 创建实例，不写 new。如果只写 `class Person(name: String)`，参数不会自动成为外部可读的属性。`init` 可验证构造参数；成员函数访问本对象时 this 常可省略。

属性不一定就是一个字段：`val label get() = "任务：$title"` 每次读取执行 getter。var 可有 setter。private 限制到声明范围，internal 限制到模块；类默认不能继承，需要 open。接口描述契约，类用 `: InterfaceName` 实现，并用 override 标明实现成员。

### 数据类生成便利操作

`data class KotlinTask(val id: Int, val title: String, val done: Boolean = false)` 用主构造属性参与默认 equals/hashCode/toString/componentN/copy。写 `task.copy(done = true)` 得到新任务，同时保留 id/title。放在类体内的其他属性不自动参加这些生成操作。

### 两种相等问的是不同问题

`a == b` 问结构是否相等，一般调用 equals；`a === b` 问是否同一个对象引用。普通类默认相等行为与数据类不同，别仅根据屏幕文字判定对象身份。任务业务身份用 id，既不是列表位置，也不是对象引用。

### copy 是浅复制

本课故意用 MutableList 标签：copy 复制了标签列表的引用，没有递归复制列表。副本 tags.add 后，原对象也会看到变化。生产界面状态更适合 val + 不暴露可变引用的结构；如果确实需要独立标签容器，可以显式 toMutableList，但里面的可变元素仍可能共享。

## 4. 暂停：先预测，不要点示范

original.done=false，copy(done=true) 后原对象 done 会变吗？副本 tags.add 后两者标签相同吗？original === copy 呢？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K06**，点击“运行参考示范”。完整示范的输出是：

```text
原来=false；副本=true；共享标签=true
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

原 done 仍 false，副本 true；tags 引用共享，所以两者看到同一列表；original 与 copy 本身是不同对象。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise06()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K06，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：先只放标量

定义局部 data class Draft(val done: Boolean)，创建 original 和 copy，显示两者 done。

### 小步 2：加入可变标签

增加 tags: MutableList<String>，创建 mutableListOf("学习")，比较 original.tags === copy.tags。

### 小步 3：亲手证明浅复制

copy.tags.add("Kotlin")，观察 original.tags 同样增长。主输出记录共享引用结果，不将这种模型直接当作生产状态。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise06 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
data class Draft(val done: Boolean, val tags: MutableList<String>)
val original = Draft(false, mutableListOf("学习"))
val copy = original.copy(done = true)
copy.tags.add("Kotlin")
return "原来=${original.done}；副本=${copy.done}；共享标签=${original.tags === copy.tags}"
```

原 done 仍 false，副本 true；tags 引用共享，所以两者看到同一列表；original 与 copy 本身是不同对象。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| 无法访问构造参数 | 要成为属性需在主构造参数前加 val/var。 |
| copy 后原标签也变 | 共享了可变列表；按需求显式复制或使用不变更新。 |
| 两个任务标题一样就视为同一个 | 业务身份要看 id；结构相等不能取代身份规则。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

让副本的标签容器独立，再加“Kotlin”；要求原列表只有“学习”。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
data class Draft(val done: Boolean, val tags: MutableList<String>)
val original = Draft(false, mutableListOf("学习"))
val copy = original.copy(done = true, tags = original.tags.toMutableList())
copy.tags.add("Kotlin")
check(original.tags == listOf("学习"))
return "原标签=${original.tags}；副本标签=${copy.tags}"
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 07/08 的任务状态用 data class.copy 表达变化。不要在 copy 后偷偷修改共享列表，再期待状态系统一定通知。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 分别比较对象的 == 与 ===。
- [ ] 能解释为什么 done 独立而 tags 共享。
- [ ] 给 KotlinTask 添加一个计算属性，观察 copy 不额外复制该计算结果。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/data-classes.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K05-receivers.md) · [下一章](K07-collections.md)
