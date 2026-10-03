# K08 · 密封类型与状态建模

[Kotlin 目录](README.md) · [上一章](K07-collections.md) · [下一章](K09-generics.md)

## 1. 今天只解决这个问题

用互斥类型表达加载、成功、错误，利用编译器检查分支是否覆盖。

**前置：**先完成 [K07](K07-collections.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K08 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K08.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 多个布尔值会产生歧义

isLoading、hasError、hasData 三个开关能组成八种组合，其中“正在加载且失败且有新结果”是否合理？先定义当前页面允许哪些状态，再选数据结构。本课是互斥的 Loading/Success/Error；真实刷新页面可能允许旧数据与刷新进度共存，届时应明确增加相应模型。

### sealed 限定状态家族

项目已有 `sealed interface KotlinLoad`。它的直接子类型在允许的模块和包范围内定义，编译器知道可能分支。`data object Loading` 没有额外数据；`data class Success(val count: Int)` 携带成功数量；Error 携带原因。普通 interface 并不承诺只有这些实现。

object 声明单例；data object 还提供数据对象语义的生成实现。companion object 是类关联的对象，常承载工厂或常量，不等同 Java static。enum class 适合固定命名值，如 Filter.ALL/PENDING；每个状态需要不同数据时 sealed 类型更贴切。

### when 同时匹配并缩小类型

`is KotlinLoad.Success -> "共 ${state.count} 项"` 中 is 检查类型，分支里 state 被智能转换，所以可以访问 count。Loading 是单例，直接按值匹配。把 when 作为表达式返回 String，需要穷尽；漏掉 Error，编译器会要求补上。随手写 else 可能让新增状态悄悄失去专门处理。

UI 只是把这份模型转换成视觉表现。模型与渲染分开，才能在不启动界面的情况下验证状态规则。本课使用字符串渲染，后面 Compose 17 变为加载提示、内容与重试按钮。

## 4. 暂停：先预测，不要点示范

Loading 能访问 count 吗？删去 when 的 Error 分支会怎样？新增 Empty 后完整处理器需要什么改动？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K08**，点击“运行参考示范”。完整示范的输出是：

```text
加载中 / 共 2 项 / 失败：断网
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

Loading 没有 count。when 表达式不穷尽会编译失败；新增 Empty 需要处理相应分支。不要用 else 掩盖忘记的状态。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise08()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K08，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：先渲染一个状态

调用已有 describeKotlinLoad(KotlinLoad.Loading)，不要再声明同名模型。

### 小步 2：依次体验三种状态

构造 Success(2) 和 Error("断网")，分别传入函数，观察携带数据的使用。

### 小步 3：自己重写处理器

在练习中声明局部 render(state: KotlinLoad): String，以 when 覆盖三种情况，再 joinToString。尝试漏一支观察编译器反馈后恢复。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise08 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
fun render(state: KotlinLoad): String = when (state) {
    KotlinLoad.Loading -> "加载中"
    is KotlinLoad.Success -> "共 ${state.count} 项"
    is KotlinLoad.Error -> "失败：${state.message}"
}
val states = listOf(KotlinLoad.Loading, KotlinLoad.Success(2), KotlinLoad.Error("断网"))
return states.joinToString(" / ") { render(it) }
```

Loading 没有 count。when 表达式不穷尽会编译失败；新增 Empty 需要处理相应分支。不要用 else 掩盖忘记的状态。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| when 不穷尽 | 确认三种状态都覆盖；作为返回值不能漏分支。 |
| count 不可访问 | 只有 Success 分支具有 count；先做 is 检查。 |
| 所有错误都变成空列表 | 保留 Error 类型和原因，成功空结果与失败含义不同。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

把成功 0 项显示为“暂无任务”，成功 2 项仍显示“共 2 项”。无需先修改共享模型。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
fun render(state: KotlinLoad): String = when (state) {
    KotlinLoad.Loading -> "加载中"
    is KotlinLoad.Success -> if (state.count == 0) "暂无任务" else "共 ${state.count} 项"
    is KotlinLoad.Error -> "失败：${state.message}"
}
return render(KotlinLoad.Success(0))
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 17 的异步页面需要互斥状态；08 的 UiState 是一次快照。错误、空结果与取消必须分别定义，不要仅靠几个布尔值猜组合。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 展示三种状态的不同输出。
- [ ] 能解释 object、enum、sealed 的适用条件。
- [ ] 暂时漏掉一支确认编译报错，再恢复。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/sealed-classes.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K07-collections.md) · [下一章](K09-generics.md)
