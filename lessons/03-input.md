# 03 · 输入与状态提升：输入框的值属于谁？

**本章目标：**理解输入事件为何必须写回状态；拆出由父组件控制的输入框。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 03 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/03.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 03 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/03.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 先从 EditText 的直觉过来

EditText 内部维护文本，你监听变化再读它。这里使用的 Compose `OutlinedTextField(value, onValueChange)` 由调用者提供当前文本。用户输入后，回调报告候选新文本；父组件更新状态，新 value 再传回输入框。

先把这个闭环读熟：**当前值向下传 → 输入事件向上报 → 所有者接受并更新 → 新值再次向下传。**这里的“上下”指组件调用层级，不是屏幕坐标。

## 2. 在代码之前，走一次具体事件

| 时刻 | 父组件 name | 传入 value | 回调收到 |
| --- | --- | --- | --- |
| 初始 | 空字符串 | 空字符串 | 尚无事件 |
| 输入“明” | 还未处理时仍为空 | 当前仍为空 | “明” |
| 父组件接受 | “明” | 下次更新为“明” | 本次结束 |
| 再输入“天” | “明” | “明” | “明天” |

`onValueChange` 给出的是新文本，不只是新输入的那一个字符。把 `name += it` 写进回调会重复拼接旧文字。

## 3. 文件位置和导入

修改 `Exercises.kt` 中的 `InputExercise`。加入：

```kotlin
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
```

### 第一步：先不拆组件

```kotlin
@Composable
private fun InputExercise() {
    var name by rememberSaveable { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = { newText -> name = newText },
            label = { Text("名字") }
        )
        Text(if (name.isBlank()) "请输入名字" else "你好，$name")
    }
}
```

运行：输入小明、逐字删除、旋转。下方问候和输入框应该读取同一个 name；清空后显示空提示。

### 第二步：故意断开闭环

临时把回调改成 `onValueChange = { }`。先预测再试输入：候选文本不会被父组件接受，输入框不能保持新内容。恢复原回调。

这不是“键盘坏了”。它说明 value 的来源才决定显示内容。

## 4. 为什么要状态提升？

现在输入框和问候都需要 name。如果各自保存一份，两处会有不同的真相。把共享值放到最低的共同父组件，两个孩子只接收数据，才能一起更新。

“无状态组件”不是说屏幕完全没有状态，也不是禁止框架内部状态；这里指这个组件不拥有业务名字的另一份可变副本。

### 第三步：抽出输入组件

先新增函数，再替换父组件里的输入框：

```kotlin
@Composable
private fun NameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("名字") }
    )
}

@Composable
private fun InputExercise() {
    var name by rememberSaveable { mutableStateOf("") }
    Column {
        NameField(value = name, onValueChange = { name = it })
        Text(if (name.isBlank()) "请输入名字" else "你好，$name")
    }
}
```

子函数的 `onValueChange` 参数是“告诉我发生了什么”的通道。父函数传入的 lambda 才执行写状态。抽取前后行为应完全相同。

## 5. 用图检查有没有两份真相

![第 03 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/03-experiment.png)

教学示意展示接受输入和忽略事件的区别。请沿图找到唯一 name，不能在 NameField 再声明一份 `rememberSaveable` 来同步。

![第 3 课真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/03-running.png)

真实示范可以验证同步问候与空提示。

## 6. 让你作一次设计决定

需求：最多允许 12 个字符。先想在哪里拦截，再写：

```kotlin
NameField(
    value = name,
    onValueChange = { proposed ->
        if (proposed.length <= 12) name = proposed
    }
)
```

规则放在业务值所有者处。通用 NameField 不需要知道这次表单的长度限制。这里按 Kotlin String 长度教学；若产品按用户可见字符计数，需要额外处理 emoji 等情况。

不要在输入时每次 `trim()`：输入过程中的空格可能是用户尚未写完的内容。提交时规范化是另一条业务规则，第七章会处理。

## 7. 先回答，再展开

1. 把回调改成 `{ name = "固定" }` 后，用户输入什么都显示什么？
2. 两个 NameField 接收同一个 name 会怎样？
3. 提升状态时，需要把所有变量都搬进 ViewModel 吗？

<details><summary>解析</summary>

1. 父组件接受的始终是“固定”，所以 value 最终是“固定”。
2. 两个输入框显示同一个值，任一修改会经父组件同步到另一处。这可作为迁移实验。
3. 不需要。状态提升先确定合适的共同所有者；ViewModel 是后续屏幕逻辑的选择。

</details>

## 8. 验收与排错

不看参考实现，写一个“城市”输入框和下方预览，复用受控组件；输入、删除、旋转后正确。说出事件和值的两个方向。

| 现象 | 原因与修复 |
| --- | --- |
| 输入不能保持 | 回调没有更新 value 的来源 |
| 字符反复重复 | 把整个候选文本追加了，应直接赋值 |
| 输入框和问候不一致 | 两处是否各存了一份状态？ |
| 抽出后无法预览 | 子组件依赖外部状态；用参数与空回调提供静态输入 |

下一章解释同一份数据如何组织成资料卡。官方核对：[受控输入与状态提升](https://developer.android.com/develop/ui/compose/state#state-hoisting)。

---

上一章：[第 02 章](02-state.md) · 下一章：[第 04 章](04-layout.md)
