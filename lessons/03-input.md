# 03 · 输入与单向数据流

**目标**：理解受控输入和状态提升，能写无状态子组件。预计 60 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 03 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/03.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 03 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/03.png)

**看图先猜**：输入一个字时，`NameField` 应该自己保存名字，还是只把新值通知父组件？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先画出父组件持有 `name`、子组件接收 `value` 的两层树。
2. 给子组件加 `value` 和 `onValueChange` 参数，暂时只显示输入框。
3. 在父组件回调中更新 `name`；输入、删除、旋转各试一次。

**停下来验收**：子组件里找不到第二份 `name` 状态，屏幕文字始终跟父组件一致。

**若结果不同**：如果输入框无法输入，检查回调是否真的把新值写回父状态。
<!-- visual-end -->

## 讲解

`TextField` 的 `value` 是当前状态，`onValueChange` 把用户输入作为事件交还给调用者。数据向下流，事件向上流。和 `EditText.addTextChangedListener` 相比，这里只有一个可信的状态来源。

一个组件需要复用或由父组件控制时，把状态提升：`NameField(value, onValueChange)` 不自己持有名字；父组件保存并更新名字。这也使它更容易预览和测试。

## 看一次真实的数据流

![第 3 课在 Android 模拟器中输入 Compose 后的实际画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/03-running.png)

实际运行画面里，输入值和下方问候同步变化。下一步亲自删掉文字，观察空提示。

在 App 第 3 课点「先体验可操作示范」。输入“阿青”，观察下方问候；逐字删除，再看空提示。现在在纸上画四个箭头：手指输入 → `onValueChange` → 父组件更新 `name` → 新的 `value` 回到输入框。不要画第二份 `name` 状态。

## 分两次写，避免一下写完整页

1. 先只在 `InputExercise` 里保存 `name`，放一个 `OutlinedTextField(value = name, onValueChange = { name = it })` 和显示问候的 `Text`。每输入一个字就验证下方是否同步变化；清空时显示“请输入名字”。
2. 再把输入框抽成 `NameField(value: String, onValueChange: (String) -> Unit)`。父组件调用 `NameField(name, onValueChange = { name = it })`。子组件里不准再声明 `remember`；它只显示传入的值，并把新输入交还给父组件。

**故意做坏再修好**：暂时把父组件回调改成 `{ }`，输入框会无法保持新字符。原因是事件到达了，却没有更新唯一的状态源。恢复 `{ name = it }`，确认输入恢复正常。

**卡住时检查**：如果输入后下方文字变了、输入框却变空，检查 `OutlinedTextField` 的 `value` 是否仍来自同一个 `name`；如果旋转后丢字，确认父组件使用 `rememberSaveable`。

## 任务

1. 在 `InputExercise` 保存名字，初始为空。
2. 用 `OutlinedTextField` 输入名字，下方实时显示“你好，名字”。为空时显示“请输入名字”。
3. 抽出 `NameField(value: String, onValueChange: (String) -> Unit)`，让父组件持有状态。

## 验收

- 输入、删除、旋转屏幕后文本都正确。
- `NameField` 内没有 `remember`；父组件是唯一修改名字的地方。

## 提示

1. `var name by rememberSaveable { mutableStateOf("") }`。
2. `OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text("名字") })`。

<details><summary>参考实现</summary>

```kotlin
@Composable
private fun InputExercise() {
    var name by rememberSaveable { mutableStateOf("") }
    Column {
        NameField(name, onValueChange = { name = it })
        Text(if (name.isBlank()) "请输入名字" else "你好，$name")
    }
}

@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("名字") }
    )
}
```

</details>

**变式**：只允许最多 12 个字符；把约束放在父组件的事件回调里。想一想为什么不应该让 `NameField` 自己偷偷修改 `value`。
