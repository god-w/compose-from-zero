# 02 · 状态与重组：点击后谁通知界面？

**本章目标：**接着第一章，解释一次点击如何改变界面；区分可观察、记忆和恢复三个职责。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 02 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/02.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 02 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/02.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从你熟悉的点击监听开始

View 中常见写法是 `count++; textView.text = count.toString()`。你同时改数据和控件。Compose 中按钮回调只改数据，Text 读取数据；框架安排相关内容更新。

**先别把“重组”理解为整个 Activity 重建。**它是 Compose 重新执行需要更新的内容描述。Activity 重建是另一件事：旋转时旧 Activity 被销毁并创建新的实例。

## 2. 拆开三个问题，避免只背 API

| 问题 | API 的职责 | 单独使用是否足够？ |
| --- | --- | --- |
| 这个值改变，Compose 能发现吗？ | `mutableIntStateOf` / `mutableStateOf` 提供可观察状态 | 不负责跨重组记住新建的对象 |
| 内容再次执行时，还是同一份状态吗？ | `remember` 把对象保留在当前组合位置 | 普通对象被 remember 后也不会自动可观察 |
| Activity 重建后，小值能恢复吗？ | `rememberSaveable` 保存可保存的恢复信息 | 不是长期存储，不能替代数据库 |

`remember { mutableIntStateOf(0) }` 把前两个职责组合起来。`rememberSaveable` 再补恢复能力。本章只保存一个 Int，适合这种机制；大量任务记录留到第 16 章。

## 3. 先读不使用 by 的版本

```kotlin
val countState = remember { mutableIntStateOf(0) }
Text("已点击 ${countState.intValue} 次")
Button(onClick = { countState.intValue++ }) { Text("+1") }
```

`countState` 是状态容器，`intValue` 是容器里的数。Text 所在内容读取它；点击修改它；这条读写关系让 Compose 知道相关内容需要更新。

再看等价的委托写法：

```kotlin
var count by remember { mutableIntStateOf(0) }
```

这里 `count` 读写的是容器中的值。`by` 没有提供额外的保存能力，它只是让你不用每次写 `intValue`。导入 `getValue` 和 `setValue` 才能使用这个委托语法。

## 4. 到哪里写？先准备导入

修改 `app/src/main/java/dev/learning/compose/Exercises.kt` 中的 `CounterExercise`。文件已有 Column、Text、Composable；新增以下导入，重复的不用再加：

```kotlin
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
```

### 实验 A：故意用普通变量

```kotlin
@Composable
private fun CounterExercise() {
    var count = 0
    Column {
        Text("已点击 $count 次")
        Button(onClick = { count++ }) { Text("+1") }
    }
}
```

先预测再运行：连续点三次，Text 是否稳定显示 3？**不能依赖它更新。**普通变量被修改不通知 Compose；内容若因其他原因重新执行，局部初始化还会重来。不要以偶然刷新证明它正确。

### 实验 B：只改状态声明

```kotlin
var count by remember { mutableIntStateOf(0) }
```

再次运行，点到 3，应该依次看到 1、2、3。现在旋转模拟器；如果系统确实重建了 Activity，状态回到初始值。无需旋转代码，只使用模拟器旋转按钮。

**解释：**同一次组合中的记忆还在，但新 Activity 建立的是新的组合。

### 实验 C：让小状态可以恢复

把声明中的 `remember` 换成 `rememberSaveable`，再加清零按钮。完整结果：

```kotlin
@Composable
private fun CounterExercise() {
    var count by rememberSaveable { mutableIntStateOf(0) }
    Column {
        Text("已点击 $count 次")
        Button(onClick = { count++ }) { Text("+1") }
        Button(onClick = { count = 0 }) { Text("清零") }
    }
}
```

点到 3 → 旋转 → 应仍是 3 → 清零 → 应为 0。强制停止再启动是否保留，不是这个 API 的验收承诺。

## 5. 读图复盘一次点击

![第 02 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/02-experiment.png)

这是教学示意，比较同一次“点三次”操作的结果。普通变量没有自动驱动 Text，状态版本才建立读写关系。

![第 2 课真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/02-running.png)

项目示范使用可恢复计数。完成自己的三轮实验后，用它核对行为。

## 6. 检查理解，先作答再展开

1. `remember { mutableListOf<String>() }` 的列表 add 后一定通知 UI 吗？
2. `mutableIntStateOf(0)` 不放在 remember 中，问题是什么？
3. 点一下按钮后，是 onCreate 必须重跑吗？

<details><summary>答案与原因</summary>

1. 不会。remember 负责记忆对象，普通 MutableList 不提供 Compose 的内容变化通知。
2. 内容再次执行时可能重新创建状态容器，丢掉之前的值。要保持容器身份。
3. 不是。状态变化安排相关内容重组；Activity 重建是生命周期事件。

</details>

## 7. 迁移练习与排错

自己加一个“−1”按钮，并保证不能小于 0。先写行为表：0 时不可减；1 时减到 0。再决定使用 `enabled = count > 0` 和回调保护。不要把 `count++` 写在 Composable 函数体里，否则执行内容描述时就修改了状态。

| 现象 | 回到哪里检查 |
| --- | --- |
| `by` 报错 | getValue、setValue 导入 |
| 每次都回到 0 | 状态容器是否被 remember |
| 点击没变 | onClick 是否写回可观察的同一个值 |
| 数字不停增加 | 是否在内容描述中直接写了 count++ |
| 返回首页后丢失 | 当前函数已离开组合；本章没有承诺跨页面长期保存 |

**完成标准：**三种声明各实验一次，能解释结果；不看答案独立写加一、减一、清零。下一章把同一条数据流用于输入框。

官方核对：[状态](https://developer.android.com/develop/ui/compose/state)、[状态恢复](https://developer.android.com/develop/ui/compose/state-saving)。

---

上一章：[第 01 章](01-first-composable.md) · 下一章：[第 03 章](03-input.md)
