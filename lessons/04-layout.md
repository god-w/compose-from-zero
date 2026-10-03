# 04 · 布局与 Modifier：谁决定大小和位置？

> **Kotlin 前置回查：** [K05 接收者、扩展与作用域函数](kotlin/K05-receivers.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**从 ViewGroup 的直觉理解约束、排列与修饰顺序，做一个资料卡。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 04 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/04.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 04 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/04.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从布局树开始，不先背 Modifier

View 页面由 ViewGroup 测量和放置孩子。Compose 的布局也要回答尺寸和位置问题。父节点给约束，子节点在约束内测量，然后父节点放置孩子。

我们的资料卡只有一棵小树：

```text
Row：左右排列
  ├── 头像占位 Box
  └── Column：上下排列
        ├── 姓名 Text
        └── 简介 Text
```

先确定树，再加大小、间距、背景。不要靠许多空格模拟布局。

## 2. 三个常用容器各回答什么？

| 容器 | 默认布局意图 | 本章用途 |
| --- | --- | --- |
| Row | 横向安排孩子 | 头像左、文字右 |
| Column | 纵向安排孩子 | 姓名上、简介下 |
| Box | 允许孩子在同一区域叠放、对齐 | 放头像占位文字 |

Row 的 verticalAlignment 是纵向对齐，Column 的 horizontalAlignment 是横向对齐。Arrangement 控制主轴上的安排与间距。不要只看参数名字里的 horizontal/vertical，把它放回当前容器理解。

## 3. 文件与导入

修改 `Exercises.kt` 的 `LayoutExercise`，新增：

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
```

文件已有 Column、Text、Modifier、dp。

### 第一步：只验证树

```kotlin
@Composable
private fun LayoutExercise() {
    Row {
        Text("头像")
        Column {
            Text("小明")
            Text("Android 开发者")
        }
    }
}
```

先预测：姓名与简介是否在头像下面？运行，应该是文字组在头像右边，组内上下排列。

### 第二步：把尺寸与间距加到对应层

```kotlin
@Composable
private fun LayoutExercise() {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(Color(0xFFDCE8FF)),
            contentAlignment = Alignment.Center
        ) { Text("明") }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("小明")
            Text("Android 开发者")
        }
    }
}
```

运行前指出：16dp 作用在整个资料区域，12dp 在 Row 的两个孩子之间，4dp 在 Column 的文字之间。dp 是尺寸单位，Compose 会根据密度换算；不是“16 个物理像素”。

## 4. Modifier 是有顺序的处理链

比较两个写法，先别运行：

```kotlin
// A：外侧留白，里面上色
Modifier.padding(16.dp).background(Color.Yellow)

// B：外层背景包含里面的留白
Modifier.background(Color.Yellow).padding(16.dp)
```

从外到内理解：A 的 padding 位于背景外面；B 的背景包住 padding。可以给 Text 先使用 A，截图；再使用 B，截图。改变的是背景覆盖范围，不能当成可任意交换的属性集合。

![第 04 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/04-experiment.png)

教学示意标出背景和留白；两张图的文字内容相同。

## 5. 常见疑惑：为什么 size 不总是强制大小？

孩子受父布局约束。`size(56.dp)` 表达希望尺寸，也要与上层给的约束协调。`fillMaxWidth()` 是利用可用宽度，不是“永远等于屏幕宽度”；外层已经留了 padding 时，可用宽度更小。

先不使用 `requiredSize` 绕开约束；本章的目标是看懂父子协作。第 15 章会亲手测量孩子。

![第 4 课真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/04-running.png)

示范页可切换修饰顺序。完成自己的静态资料卡再比较。

## 6. 理解检查

1. 把 Row 改成 Column，而内部 Column 不变，会出现怎样的树？
2. 头像和文字的间距，应写到头像里的 Text 上，还是 Row 的 Arrangement？
3. `fillMaxWidth` 为什么可能没有充满整个屏幕？

<details><summary>答案</summary>

1. 头像上，文字组下；文字组仍是姓名上、简介下。
2. 两个直接孩子的间距适合由 Row 的 Arrangement 表达，职责更清楚。
3. 它使用父布局提供的可用宽度，外层 padding、分栏等都可能缩小这一宽度。

</details>

## 7. 迁移、验收、排错

不用复制资料卡，改成“商品缩略图 + 商品名 + 价格”。先画树，再写容器，然后加尺寸和间距。最后把名字换成长文字，并放大系统字体。

| 现象 | 检查 |
| --- | --- |
| 两段文字横着挤在一起 | 是否遗漏内部 Column？ |
| 背景包括不想上色的留白 | background 和 padding 的顺序 |
| `weight` 无法调用 | 它需要对应的 RowScope / ColumnScope，不是任何位置都能使用 |
| 长标题把按钮挤走 | 在 Row 内给文字区域 weight，允许换行；第 11 章继续验收 |

完成标准：能画出树、解释三种间距、预测交换 Modifier 后的画面。官方核对：[布局基础](https://developer.android.com/develop/ui/compose/layouts/basics)、[Modifier](https://developer.android.com/develop/ui/compose/modifiers)。

---

上一章：[第 03 章](03-input.md) · 下一章：[第 05 章](05-list.md)
