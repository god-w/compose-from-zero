# K07 · 集合与不可变更新

[Kotlin 目录](README.md) · [上一章](K06-classes.md) · [下一章](K08-sealed-state.md)

## 1. 今天只解决这个问题

能读懂 filter/map/count，保留完整源数据，并用新列表表达修改。

**前置：**先完成 [K06](K06-classes.md) 的理解检查；卡住时返回相应章节。

建议留 45–90 分钟，K11–14 可以分两次完成。能说明原因再继续，不以阅读速度为标准。

## 2. 先看代码与结果的关系

![K07 代码与结果对照图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/kotlin/K07.png)

这是教学对照图，表示程序值与执行关系；真实 App 运行结果通过下面按钮验证。先沿图说一遍，不需要把每个符号立即记住。

## 3. 一点一点读懂语法

### List、Set 与 Map

List 有顺序、可以重复；Set 强调不重复；Map 保存键到值的对应。`listOf` 暴露只读 List 接口，`mutableListOf` 暴露添加、删除等操作。只读不等于深度不可变：若底层被其他引用修改，读者仍可能看到变化；元素内部也可能可变。

### 逐个读集合操作

`filterNot { it.done }` 保留 done 为 false 的任务；map 把每个任务转换成 title；count 统计符合条件的数量。filter/map 返回新列表，不替你删除原始元素。firstOrNull 找不到元素时返回 null；first 在空列表会抛异常。

读 `tasks.filterNot { it.done }.map { it.title }` 时先给每个 it 标类型：第一个 KotlinTask，第二个仍是 KotlinTask，最后结果为 List<String>。逐步拆成 visible 和 titles 会比立即堆在一行更易理解。

### 原数据与可见结果分开

完成数量从完整 tasks 计算；只把 visible 用于展示。若从筛选后的列表计算总数，过滤“未完成”会令统计看起来消失。不要为了隐藏一个完成任务就从源数据删除它。

### 用新值表达业务变化

添加：`tasks + KotlinTask(...)`；切换：`tasks.map { if (it.id == id) it.copy(done = !it.done) else it }`；删除：`filterNot { it.id == id }`。这个策略让旧快照保持可读。它仍要求元素内避免共享可变引用，K06 的结论继续适用。

Sequence 让部分链式操作延迟执行，适合某些大数据/提前结束场景，也有开销；不是越长的链越应该改成 Sequence。先测量，普通课程列表优先用易读的 List 操作。

## 4. 暂停：先预测，不要点示范

任务 A 已完成，B 未完成。过滤未完成后源列表还有几项？titles 是什么？完成数应从哪个列表算？

把答案写在纸上，并在每个结论旁写一句理由。然后在 App 首页进入 **Kotlin 从基础到 Compose → K07**，点击“运行参考示范”。完整示范的输出是：

```text
未完成=[B]；完成=1
```

<details>
<summary>展开预测解析：先写出自己的答案</summary>

源列表仍两项；titles 为 [B]；完成数从完整 tasks 算，得到 1。

如果预测不同，回到相关表达式，标出其输入类型、调用时机和返回值；不要只记最终字符串。

</details>

## 5. 动手：每一步都运行

打开 [`KotlinExercises.kt`](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt)，找到 **kotlinExercise07()**。只改这个函数的函数体，保留函数名与签名；其他练习不会受影响。K11、K12 是 suspend 函数。

修改后点击 Android Studio 的 **Run app** 重建安装，重新进入 K07，点击“运行我的练习”。仅切换 App 页面不会编译新源码。“运行参考示范”读取另一个文件，不会使用你的练习结果。

### 小步 1：建立两项数据

使用项目已有 KotlinTask 类型，不再声明同名类。只 return tasks.size.toString()。

### 小步 2：分步筛选转换

建立 visible = tasks.filterNot，再建立 titles = visible.map。分别想清两者元素类型。

### 小步 3：加入完整统计

用 tasks.count { it.done }，与只对 visible 统计比较。保持源列表不变。

### 小步完成参考：先自己写，再对照

<details>
<summary>完整函数体与解释</summary>

下面代码替换 kotlinExercise07 的函数体（外层函数签名已经存在，不要套一层同名函数）。需要的 import 按 IDE 提示补到文件顶部。

```kotlin
val tasks = listOf(KotlinTask(1, "A", true), KotlinTask(2, "B"))
val visible = tasks.filterNot { it.done }
val titles = visible.map { it.title }
return "未完成=$titles；完成=${tasks.count { it.done }}"
```

源列表仍两项；titles 为 [B]；完成数从完整 tasks 算，得到 1。 主练习返回 String，App 外壳负责显示；没有要求你先写 Compose 界面。输出与示范一致只能证明这组输入，下面变式会检查你是否理解规则。

</details>

## 6. 按现象排错

| 现象 | 回查位置与处理 |
| --- | --- |
| 筛选后总数变小 | 总数从 tasks 计算，不从 visible 计算。 |
| 修改 val 列表仍成功 | val 固定绑定；MutableList 内容仍可改变。 |
| 空列表 first 崩溃 | 根据业务使用 firstOrNull 或先验证非空。 |

遇到编译错误，先读 Build 窗口中第一条指向自己文件的错误。遇到运行异常，练习区会显示异常类型与消息；回到产生异常的表达式检查输入，不要盲目删掉类型约束。

## 7. 换一组输入，检查是否吃透

从原列表把 B 完成，保留两项和同样 id，再计算完成数 2。

<details>
<summary>变式答案：完成一次尝试后再打开</summary>

```kotlin
val tasks = listOf(KotlinTask(1, "A", true), KotlinTask(2, "B"))
val updated = tasks.map { task ->
    if (task.id == 2) task.copy(done = true) else task
}
check(!tasks[1].done && updated[1].done)
return "完成=${updated.count { it.done }}；ID=${updated.map { it.id }}"
```

这一变式是在改变输入或使用方式后再次应用本章规则。请指出与主练习不同的地方，再用一组新输入验证；不要只复制运行。

</details>

## 8. 带回 Compose

Compose 05 的列表 key 依赖 id；07 的 visible 和任务源数据分开；08 的 StateFlow 新状态常由 map/copy 产生。

看到陌生语法时，先问：这是值、函数、类型还是语言约定？再问 Compose 库在这一约定里提供了什么行为。

## 9. 验收与学习记录

- [ ] 说明 filter、map、count 的输入与返回类型。
- [ ] 筛选后仍验证 tasks.size 为 2。
- [ ] 用任务 id 修改 B，验证 A 和旧列表都没被改变。
- [ ] 不看答案，重新实现主练习，并完成至少一组不同输入。
- [ ] 写下“我原先预测 ___，实际 ___，原因 ___”，或说明为何预测正确。

如果未达标，只回做没通过的那一步。通过后在 [Kotlin 学习检查表](README.md#学习检查表) 标记“解释 / 独写 / 变式”。

本章语言规则可回查 [Kotlin 官方资料](https://kotlinlang.org/docs/collections-overview.html)；本项目例子围绕课程任务重新编写。

[Kotlin 目录](README.md) · [上一章](K06-classes.md) · [下一章](K08-sealed-state.md)
