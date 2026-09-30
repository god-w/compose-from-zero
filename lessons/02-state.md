# 02 · 状态与重组

**目标**：写出能正确保存和更新状态的计数器，分清局部变量、`remember` 与 `rememberSaveable`。预计 60 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 02 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/02.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 02 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/02.png)

**看图先猜**：如果在 Composable 内直接写 `var count = 0`，点三次按钮后屏幕会稳定显示 3 吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先写普通局部变量和按钮，亲眼观察它为何没有稳定更新。
2. 换成 `remember { mutableIntStateOf(0) }`，点三次并旋转。
3. 再换成 `rememberSaveable`，重复点击、旋转和清零。

**停下来验收**：能分别说出普通变量、`remember`、`rememberSaveable` 在这三个实验中的表现。

**若结果不同**：如果委托属性语法报错，检查 `getValue` 和 `setValue` 导入。
<!-- visual-end -->

## 讲解

Composable 可能因状态变化再次执行。普通 `var count = 0` 每次执行又变成 0，界面也不知道它变了。`mutableIntStateOf(0)` 创建可观察状态；`remember` 把它保留在当前组合中；`rememberSaveable` 还能在旋转屏幕等 Activity 重建时恢复可保存的值。

状态变化 → Compose 安排重组 → 读取该状态的 Composable 更新。不要在绘制过程中写状态，尽量只在点击、输入等事件里写。

## 先玩，再拆解

![第 2 课在 Android 模拟器中点击三次后的实际画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/02-running.png)

这张实际运行截图显示计数为 3；你可以自己再旋转设备验证它是否保留。

在 App 第 2 课点「先体验可操作示范」。不要急着写代码：先点三次 `+1`，读出数字；旋转模拟器，再读一次；点「清零」。真实示范用的是 `rememberSaveable`。接下来在自己的练习里分别试 `remember` 和 `rememberSaveable`，比较结果。

## 三轮小实验

1. **普通变量**：先在 `CounterExercise` 中写 `var count = 0`，显示 `Text("已点击 $count 次")`，按钮里 `count++`。先预测点击后屏幕会不会更新，再运行。局部变量不是可观察状态，Compose 不会因为它被改就刷新界面。
2. **可观察状态**：改成 `var count by remember { mutableIntStateOf(0) }`。连续点击应显示 1、2、3。旋转设备前写下预测；旋转后观察它是否从 0 开始。
3. **可恢复状态**：只把 `remember` 换成 `rememberSaveable`。再次点到 3 并旋转，数字应保持 3。最后加清零按钮，确认事件只在点击时修改状态。

**对照表**：普通 `var` 不会触发更新；`remember` 能在当前组合中保持并触发更新；`rememberSaveable` 还会为可保存的值处理常见的 Activity 重建。它们不是数据库：强制停止后重新启动，不能拿这个计数器证明持久化。

**卡住时检查**：`by` 报错先检查 `getValue`、`setValue` 导入；点击不变，检查按钮的 `onClick` 是否真正写回 `count`；旋转不符合预测，确认你替换的是同一个练习函数里的状态声明。

## 任务

在 `CounterExercise` 中：

1. 建立从 0 开始的整数状态。
2. 显示“已点击 N 次”和一个“+1”按钮。
3. 添加“清零”按钮。
4. 旋转设备，确认数字还在。

## 验收

- 点 3 次显示 3；清零回到 0；旋转屏幕后保留当前值。
- 解释 `remember` 与 `rememberSaveable` 的区别；指出状态写入发生在事件回调中。

## 提示

1. `var count by rememberSaveable { mutableIntStateOf(0) }`。
2. `Button(onClick = { count++ }) { Text("+1") }`。需要导入相应的 Compose API。

<details><summary>参考实现</summary>

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

需要 `import androidx.compose.runtime.getValue`、`setValue`、`mutableIntStateOf` 和 `androidx.compose.runtime.saveable.rememberSaveable`。

</details>

**变式**：加入“−1”，但数字不得小于 0。想一想：禁用按钮还是点击时判断？
