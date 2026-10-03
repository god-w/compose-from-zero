# K14 · 综合练习：纯 Kotlin 任务规则

[Kotlin 目录](README.md) · [上一章](K13-interop.md)

## 1. 今天只解决这个问题

独立写规则、验证边界，再把结果交给 Compose，而不是让业务逻辑依赖屏幕。

**前置：**先完成 [K13](K13-interop.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K14 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K14.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### 先定义可检验的规则

空白标题不添加；正常标题 trim 后保存；id 单调分配，不复用被删除项的 id；切换只改变指定项的 done；删除只移除指定项。对于不存在的 id，本课定义为内容不变。先把约定写下来，代码和测试才有共同目标。

### 一份状态，一种事件，一个转换

KotlinTaskState 保存 tasks 与 nextId；KotlinTaskEvent 的 Add/Toggle/Delete 携带必要参数。`reduceKotlinTasks(old, event): KotlinTaskState` 不读系统时间、不访问网络、不修改旧列表。相同输入得到相同结果，是易验证的普通函数。

Add 清洗标题后用 + 创建新列表，nextId 加一；Toggle 用 map 与 copy；Delete 用 filterNot。所有字段保留在一份快照中，避免列表与 ID 计数各自变化。模型与函数已在 KotlinExamples.kt 中提供用于示范；你的挑战是独立重写，而非只调用参考函数。

### 按时间顺序手算

初始 []/nextId=1；Add A 得到 [1:A□]/2；Add B 得到 [1:A□,2:B□]/3；Toggle 1 得到 [1:A✓,2:B□]/3；Delete 2 得到 [1:A✓]/3。删除不令计数倒退，下次添加得到 id=3。

### 先验规则，再验界面

check 条件不成立会抛异常，适合本课的小型可运行断言。验证输出字符串之外，还应检查原快照未被修改、空白无变化、不存在 id 无变化。测试只有成功路径会错过很多规则。正式项目可以把这些断言移到单元测试，但现在不需要先引入测试框架。

接入 Compose 时，用可观察容器持有 state，事件回调里赋值 `state = reduceKotlinTasks(state, event)`，再把 tasks 交给无状态子组件。不能在 Composable 函数体每次执行时自动 Add。ViewModel/StateFlow 的版本放在 Compose 08 阅读。

## 4. 暂停：先预测，不要点示范

添加 A、添加 B、完成 1、删除 2 后 nextId 是几？旧状态里的 A 会跟着完成吗？再加 C 的 id 是几？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K14**，点击“运行参考示范”。完整示范的输出是：

```text
A：完成；总数=1；下一ID=3
```

![K14 参考示范的真实模拟器运行截图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K14-running.png)

这是本项目 Kotlin 练习页的真实运行截图；图中“参考示范”结果来自示范函数，你的练习按钮读取另一份代码。

<details>
<summary>展开预测解析：先写出自己的答案</summary>

nextId=3；旧快照的 A 仍未完成；C 使用 id=3，nextId 变成4。身份分配不依赖当前位置。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise14()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K14，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：用参考规则体验

建立 KotlinTaskState，依次传入四个事件，记录每一步 tasks 与 nextId。

### 小步 2：把记录变成断言

添加空白、不存在 id、旧快照保持不变三类检查。先运行已有规则，理解检查针对哪个需求。

### 小步 3：独立实现 reducer

在 KotlinExercises.kt 文件顶层另写 reduceMyTasks，参数/返回类型沿用现有模型；用自己的函数替换练习中的参考调用。先实现 Add，通过后再加 Toggle/Delete。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise14 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
var state = KotlinTaskState()
val events = listOf(KotlinTaskEvent.Add(" A "), KotlinTaskEvent.Add("B"),
    KotlinTaskEvent.Toggle(1), KotlinTaskEvent.Delete(2))
for (event in events) state = reduceKotlinTasks(state, event)
check(state.tasks.single() == KotlinTask(1, "A", true))
check(state.nextId == 3)
return "${state.tasks.single().title}：完成；总数=${state.tasks.size}；下一ID=${state.nextId}"
```

nextId=3；旧快照的 A 仍未完成；C 使用 id=3，nextId 变成4。身份分配不依赖当前位置。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| 删除后新 id 重复 | nextId 不能用 tasks.size+1；保留独立单调计数。 |
| 旧快照也改变 | 检查是否原地改 MutableList 或 var 元素字段。 |
| 完成任务丢失标题 | copy 指定 done，保留 id/title；不要手写不完整新对象。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

关闭参考源码，独立实现 reduceMyTasks，并用 Add(" ")、Toggle(999)、Delete(999)、两次 Toggle、删除后再 Add 检查。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
// 放在 KotlinExercises.kt 文件顶层，不要声明在 Composable 里。
fun reduceMyTasks(state: KotlinTaskState, event: KotlinTaskEvent): KotlinTaskState = when (event) {
    is KotlinTaskEvent.Add -> {
        val title = event.title.trim()
        if (title.isEmpty()) state else state.copy(
            tasks = state.tasks + KotlinTask(state.nextId, title),
            nextId = state.nextId + 1
        )
    }
    is KotlinTaskEvent.Toggle -> state.copy(tasks = state.tasks.map { task ->
        if (task.id == event.id) task.copy(done = !task.done) else task
    })
    is KotlinTaskEvent.Delete -> state.copy(tasks = state.tasks.filterNot { it.id == event.id })
}
// 在 kotlinExercise14() 中改为调用 reduceMyTasks。
// 空白验证示例：check(reduceMyTasks(KotlinTaskState(), KotlinTaskEvent.Add(" ")) == KotlinTaskState())
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

把本课能力带回 Compose 07：Kotlin 负责规则和新快照，Compose 负责显示与事件。08 再把相同规则放到状态持有者，不必推翻重写。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 空白标题不改变任务与 nextId。
- [ ] 切换完成两次回到原内容，不改变 id。
- [ ] 删除后添加不会复用被删除项的 id。
- [ ] 自己重写 reducer，通过边界检查，再用不同输入说明结果。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/coding-conventions.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K13-interop.md)
