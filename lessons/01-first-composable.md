# 01 · 第一个 Composable

**目标**：理解 `setContent`、`@Composable`、组合与重组，能把一个 XML/TextView 小页面改写为 Compose。预计 45 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 01 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/01.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 01 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01.png)

**看图先猜**：把传入的 `name` 从“小明”改成“你自己的名字”，你认为要不要找到 `TextView` 才能更新？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先只运行原始练习，记下屏幕上已有的文字。
2. 给 `GreetingExercise` 增加 `name` 参数；先只让第二行显示参数，运行一次。
3. 再加标题和排版。改两次传入的名字，每次都预测第二行会显示什么。

**停下来验收**：第二行总与参数相同；解释函数输入和屏幕输出的关系。

**若结果不同**：如果改了参数却看不到变化，先确认 `PracticeScreen` 调用的是带参数的新函数。
<!-- visual-end -->

## 先建立心智模型

传统 View：`findViewById` 找到对象，再调用 `text = ...` 改它。Compose：传入数据，函数描述界面。数据变化时，Compose 会重新执行需要更新的部分。这叫**重组**。不要在 Composable 函数体里直接发网络请求或修改数据库，因为它可能运行多次。

`MainActivity.kt` 的 `setContent { MaterialTheme { Workshop() } }` 是入口。`Text` 是 Composable；`Column` 把子项竖排。每个 Composable 都可以拆成更小的 Composable。

## 先在 App 里看真实效果

![第 1 课在 Android 模拟器中运行的可操作示范](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/01-running.png)

这是从运行中的 Android 模拟器截取的实际画面；上面的蓝色手机图是目标设计示意。

进入第 1 课，点「先体验可操作示范」。先盯着“我是小明”，预测点「切换传入的名字」后哪一行会变；点两次验证。这个页面是真正运行的 Compose 界面，目标图片只是设计示意。回到练习页时，你仍会看到 `TODO`，因为示范和你的代码互不代写。

## 跟写实验：每次只改一点

1. 在 `Exercises.kt` 找到 `GreetingExercise()` 和上方的 `0 -> GreetingExercise()`。先运行一次，确认它只显示占位文字。
2. 把占位文字改成 `Text("你好，Compose！")`，运行。你刚写的 `Text` 会出现在练习区；如果仍是旧文字，确认运行的是 `app` 而不是只看设计图。
3. 在函数参数处加入 `name: String`。此时调用处会报缺少参数；这是预期的编译提示。把调用改为 `GreetingExercise("小明")`，再运行。
4. 加第二个 `Text("我是 $name")`。若要显示两行，把两个 `Text` 包在 `Column { ... }` 里。最后把“小明”换成你的名字，先预测，再运行。

**你应该看到**：改调用处的名字后，只改变第二行；没有任何查找或修改 `TextView` 的代码。这里调用者给出输入，`GreetingExercise` 描述这个输入对应的 UI。

**自己解释一次**：`@Composable` 标注的函数与普通返回字符串的函数有何不同？当 `name` 改变时，哪段代码决定了第二行的内容？

## 任务

打开 `Exercises.kt` 的 `GreetingExercise`：

1. 给函数加 `name: String` 参数，并在 `PracticeScreen` 中传入你的名字。
2. 用 `Column` 放两个 `Text`：标题“你好，Compose！”、副标题“我是 <你的名字>”。
3. 标题使用 `MaterialTheme.typography.headlineMedium`。

## 验收

- 首页进入第 1 课后看到两行文字；改动传入的名字并重新运行，第二行随之变化。
- 可以口头解释：`GreetingExercise(name)` 的输入是什么，输出是什么，为什么不需要 `findViewById`。

## 提示

1. 参数写在函数名后：`private fun GreetingExercise(name: String)`；调用处也必须提供参数。
2. 文本模板：`Text("我是 $name")`。

<details><summary>参考实现（完成后再展开）</summary>

```kotlin
@Composable
private fun GreetingExercise(name: String) {
    Column {
        Text("你好，Compose！", style = MaterialTheme.typography.headlineMedium)
        Text("我是 $name")
    }
}
// PracticeScreen 的分支：0 -> GreetingExercise("小明")
```

</details>

**变式**：添加 `isNewStudent: Boolean`，为 `true` 时显示“欢迎开始学习”。完成后进入下一课。
