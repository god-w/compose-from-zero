# 04 · 布局与 Modifier

**目标**：用 `Column`、`Row`、`Spacer` 和 `Modifier` 拼出个人资料卡，并理解修饰符顺序。预计 60 分钟。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 04 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/04.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 04 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/04.png)

**看图先猜**：头像和姓名放在同一个 `Column` 会是什么排列？换成外层 `Row` 呢？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先只放三个带文字的占位块，观察 `Row` 和 `Column` 的方向。
2. 按图改成 `Row(头像, Column(姓名, 副标题))`。
3. 分别交换 `padding` 与 `background` 的顺序，截图比较背景覆盖范围。

**停下来验收**：能指出哪一层控制方向，哪一个 Modifier 顺序改变了可见范围。

**若结果不同**：如果长名字挤走操作按钮，先给文字列分配剩余宽度。
<!-- visual-end -->

## 讲解

`Column` 竖排，`Row` 横排，`Box` 可以层叠。布局先接收父级约束，再测量子项，最后放置。`Modifier` 是从左到右串起来的修饰链：`padding(8.dp).background(color)` 和 `background(color).padding(8.dp)` 的着色范围不同。尺寸和间距尽量由外层控制，避免子组件假设自己总会占满屏幕。

## 先动手观察真实界面

![第 4 课在 Android 模拟器中运行的资料卡与 Modifier 实验](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/04-running.png)

这是实际运行画面。先预测按钮切换后色块的边界，再动手验证。

第 4 课的可操作示范有一张资料卡和一块可以切换修饰符顺序的色块。先看资料卡，指出哪个 `Row` 负责头像与文字左右排列、哪个 `Column` 负责姓名与副标题上下排列。再点切换按钮，观察色块背景覆盖范围。两次画面只有 `Modifier` 的顺序不同。

## 分层跟写

1. 在 `LayoutExercise` 先只写 `Column { Text("姓名"); Text("Android 开发者") }`，运行确认两行上下排列。
2. 在外面包 `Row`，最左侧加一个 48dp 的圆形 `Box` 作为头像。先不给间距，观察元素是否贴在一起；然后在 `Row` 使用 `Arrangement.spacedBy(12.dp)`。
3. 外层再加 `Card` 与 16dp 内边距，最后加底部标签。每加一层都运行一次；如果布局和预期不同，先查父子层级，暂时不要继续加 Modifier。
4. 单独放一块有背景的 `Text`，试 `Modifier.background(color).padding(16.dp)` 与 `Modifier.padding(16.dp).background(color)`。在纸上画出两者的着色区域，再对照屏幕。

**自检**：把姓名改成长字符串，观察是否挤出屏幕。先给文本列 `weight(1f)` 再考虑截断；解释剩余宽度由谁分配。

## 任务

在 `LayoutExercise` 做资料卡：一行圆形首字母头像和“姓名 / Android 开发者”，下方有“Compose 学习中”标签。给整张卡 16dp 内边距，行内元素间距 12dp。试着交换 `background` 与 `padding` 的顺序观察差异。

## 验收

- 小屏也不会横向溢出；姓名与副标题上下排列，头像和文字左右排列。
- 能说出 `fillMaxWidth`、`padding`、`background` 分别作用于谁。

## 提示

1. `Row(verticalAlignment = Alignment.CenterVertically)`；内部再放 `Column`。
2. 头像可用 `Box(Modifier.size(48.dp).clip(CircleShape).background(...), contentAlignment = Alignment.Center)`。

<details><summary>参考实现</summary>

```kotlin
@Composable
private fun LayoutExercise() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Text("安") }
                Column {
                    Text("安卓同学", style = MaterialTheme.typography.titleMedium)
                    Text("Android 开发者")
                }
            }
            Text("Compose 学习中")
        }
    }
}
```

</details>

**变式**：用 `weight(1f)` 让长姓名占据剩余宽度，在右侧加“编辑”按钮。
