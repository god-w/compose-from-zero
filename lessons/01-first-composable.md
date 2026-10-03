# 01 · Kotlin 函数为什么能变成界面？

> **Kotlin 前置回查：** [K01 值、类型与字符串](kotlin/K01-values.md) · [K02 函数与控制流程](kotlin/K02-functions.md) · [K04 函数类型与 lambda](kotlin/K04-lambdas.md) · [K13 注解与 Java 互操作](kotlin/K13-interop.md)。语法卡住时先做对应小步练习，再继续本章。

你已经会 Android 和 Kotlin。这一章从你熟悉的 `Activity → XML → View` 出发，把 Compose 接到已有知识上。

**本章目标：**能说清楚谁调用界面函数、`@Composable` 做什么、`Column` 和 `Text` 如何协作；然后独立写出一个接收名字的问候区域。暂时不涉及状态、点击事件和编译器源码。

## 怎么读这一章

分三次学，每次做到检查点再继续，时间只是参考：

| 一次学习 | 阅读位置 | 你应该能解释什么 |
| --- | --- | --- |
| 第一次，约 20 分钟 | 1–3 节 | XML 与 Compose 分别怎样描述同一个页面；谁调用函数 |
| 第二次，约 20 分钟 | 4–6 节 | 为什么不返回 View；谁决定排列；注解为什么不等于自动显示 |
| 第三次，约 30 分钟 | 7–9 节 | 写出来、预测改动结果、用自己的话解释 |

不要用“看着懂了”判断完成。每个检查点先遮住答案，用自己的话回答；答不出就回到指定的小节。

## 1. 先用你熟悉的方式做一件小事

我们只做这个界面：第一行“你好，Compose！”，第二行“我是小明”。

用 View 实现，你可能会写这样的 XML。**这一节是对照阅读，不需要在项目中创建 XML 文件。**

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <TextView
        android:id="@+id/greeting"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="我是小明" />
</LinearLayout>
```

然后在 Activity 中：

```kotlin
setContentView(R.layout.greeting)
findViewById<TextView>(R.id.greeting).text = "你好，Compose！"
```

这段你已经知道怎么工作：XML 描述层级，加载后得到 View 对象；你拿到其中一个 TextView，设置它的文字。垂直 LinearLayout 决定两个子 View 上下排列。

Compose 可以描述同一个内容：

```kotlin
@Composable
fun Greeting() {
    Column {
        Text("你好，Compose！")
        Text("我是小明")
    }
}
```

现在先把它读成一句话：**问候区域里有一个竖排容器，容器里依次放两段文字。**暂时不要把 `Text(...)` 想成 `new TextView(...)`。

![View 与 Compose 描述同一个问候区域](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01-view-compose.png)

图里的“对应”是用途对应：`Column` 负责竖排，`Text` 负责文字。它们的内部实现不是 LinearLayout 和 TextView 的一一替换。

**检查点 A：**若第二段文字出现在第一段下面，是 `@Composable` 决定的，还是 `Column` 决定的？

<details>
<summary>回答后再展开</summary>

是 `Column` 的布局规则。注解不决定方向。你在 XML 中用 `orientation="vertical"` 表达的排列意图，在这里交给 `Column` 表达。

</details>

## 2. 谁调用 Greeting？它不是自动出现的

先从一个最小 Activity 看入口。**这是解释用的缩小示例，不要替换项目中的 MainActivity。**完整示例需要这些 import：

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
```

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Greeting()
            }
        }
    }
}

@Composable
fun Greeting() {
    Column {
        Text("你好，Compose！")
        Text("我是小明")
    }
}
```

从外往里读，不要一下子盯住所有括号：

1. Android 创建 Activity，调用 `onCreate`。这一点没变。
2. Activity 的 `setContent` 建立 Compose 内容入口，接收一段描述界面的代码。这个入口让 Compose 可以执行、管理这段内容。
3. 内容里调用 `MaterialTheme`，为里面的组件提供主题信息，例如文字样式和颜色。它不是竖排容器。
4. 主题内容里调用你写的 `Greeting()`；函数里面再调用 `Column` 和 `Text`。

**关键：定义函数和调用函数依然是两件事。**只写一个带注解的 `Greeting`，没有任何地方调用它，屏幕不会自己出现问候语。

`setContent { ... }` 里的大括号是一段交给框架管理的内容，不是“函数只执行一次”的承诺。后续状态变化时，框架可能再次执行相关内容；第二章再实验这一点。

### 在本项目中，调用路径稍微长一点

实际入口在 `app/src/main/java/dev/learning/compose/MainActivity.kt`：

```kotlin
setContent { MaterialTheme { Workshop() } }
```

`Workshop` 负责课程首页和课程页面。当你进入第一课的**练习页面**，它选择第 1 课（下标 `0`），调用 `PracticeScreen(0)`。`PracticeScreen` 在 `Exercises.kt` 中选择：

```kotlin
0 -> GreetingExercise()
```

于是你修改 `GreetingExercise`，就能改变第一课练习区。我们把课程外壳提前写好了，你不需要先理解整个课程列表。

![第 01 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01.png)

图省略了课程页面的外层布局，只追踪通往你练习函数的路径。**“可操作示范”使用另一个实现；修改练习函数不会修改示范页。**

**检查点 B：**你在 `Exercises.kt` 新增一个 `@Composable fun MyTitle()`，但任何地方都没有调用它。运行后是否能看到它？为什么？

<details>
<summary>回答后再展开</summary>

看不到。注解标记函数的用途，没有替你接入界面。可以在 `GreetingExercise` 的 `Column` 中调用 `MyTitle()`，让它成为当前内容的一部分。

</details>

## 3. @Composable 到底做了什么？

你知道普通 Kotlin 函数怎么互相调用。Compose 多了一个要求：**描述 Compose 内容的调用，要处在 Compose 管理的上下文中。**

`@Composable` 告诉 Compose 编译器：这个函数需要按 Compose 的调用机制处理。编译器会对它进行转换，让运行时能够管理组合中的这些调用。你仍然写正常的参数、条件判断和函数调用，不需要手动传内部管理信息。

这里“组合（composition）”可以先理解为：**框架执行这些内容描述，建立和维护界面结构的过程。**“运行时”就是 app 运行时负责这件事的 Compose 代码。

所以，`Greeting` 调用了本身就是 Composable 的 `Column` 和 `Text`，自己也需要注解。如果删掉 `GreetingExercise` 上面的注解，编译器会提示 Composable 调用需要 Composable 上下文。Activity 的 `onCreate` 是普通函数，它通过 `setContent` 提供的内容入口进入这个上下文。

三个边界要分清：

| 你可能形成的印象 | 本章应建立的理解 |
| --- | --- |
| 加了注解，函数自动显示 | 还要从内容入口的调用链中被调用 |
| 注解负责把文字画上去 | 注解参与编译处理；组件、运行时、布局与绘制共同完成界面 |
| 所有辅助函数都要注解 | 普通字符串处理函数照常写；需要调用 Composable 的内容函数才需要相应上下文 |

例如：

```kotlin
fun greetingText(name: String): String = "你好，$name！"

@Composable
fun Greeting(name: String) {
    Text(greetingText(name))
}
```

`greetingText` 只计算字符串，可以在普通 Kotlin 代码中调用。`Greeting` 调用 `Text` 描述 UI，才需要 `@Composable`。

这里了解的是工作原理，没有要求你阅读转换后的代码。官方背景：[Compose 思维模型](https://developer.android.com/develop/ui/compose/mental-model)、[Compose 编译器配置](https://developer.android.com/develop/ui/compose/compiler)。

## 4. 没有返回 View，界面从哪里来？

看这个函数：

```kotlin
@Composable
fun Greeting(name: String) {
    Text("你好，$name！")
}
```

它没有返回 `TextView`，也没拿到 `Text` 对象再调用 setter。因为它的任务是**在当前组合中描述文字内容**，框架负责维护对应的 UI 结构。

把“代码运行”和“像素出现”分开看，就容易理解了：

| 阶段 | 主要回答的问题 | 本例发生什么 |
| --- | --- | --- |
| 组合 | 界面有哪些内容？ | `Greeting` 调用组件，描述一个 Column 和两段文字 |
| 布局：测量、放置 | 多大？放在哪里？ | 确定文字尺寸，按 Column 规则安排位置 |
| 绘制 | 用什么像素表现？ | 绘出文字等可见内容 |

不是执行到 `Text` 那一行就立刻把像素画到屏幕。也不是“函数返回 Unit，所以没有产生界面”：它通过当前组合参与 UI 描述。

**Compose 内部仍有维护布局和绘制的对象。**你通常不直接持有一个 TextView 来改文字；也不能把每一个 Composable 函数都当作一个独立布局节点。例如 `Greeting` 只是组织了里面的组件，不会因为函数名就额外产生一层容器。

这三个阶段的正式说明见[官方 Compose 阶段文档](https://developer.android.com/develop/ui/compose/phases)。后续更新时，框架可以只执行需要的阶段；本章先理解首次显示的路径。

### 为什么不再 findViewById？

对照同一件事：显示名字。

```kotlin
// View：找到对象，设置对象的属性
nameView.text = "小明"

// Compose：把要显示的值交给组件
Text("小明")
```

如果写 `Greeting("小明")`，数据通过参数流到 `Text`。你不需要先找一个文字对象再设置它。

但**把普通变量改成另一个值，并不保证 Compose 自动发现和更新界面**。第二章会补上“可观察状态 → 相关内容重新组合”的机制。本章只改源码、重新构建运行，先做静态界面。

## 5. Column 的大括号里为什么能放 Text？

你熟悉 Kotlin 的尾随 lambda。`Column { ... }` 的 `{ ... }` 正是传给 Column 的内容 lambda；这里的内容允许调用 Composable。

可以把接口含义读作：**“Column，我把子内容交给你，请按竖排规则布局。”**内容中两次 `Text` 调用描述两个文字组件。Column 决定这些子内容怎样测量、放置；Text 负责文字内容与表现。

![Column 中两次 Text 调用与两行文字的对应关系](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01-code-to-screen.png)

这张图是局部排列示意，不是整个 app 的截图。练习区上方还会有课程标题、引导和其他外层布局。

这里的“顺序”指子内容在布局中的顺序。不要由此推断每次重组时所有函数都按同样的先后顺序执行；UI 代码不应依赖这种执行顺序来修改外部数据。

### 把 Column 换成 Row，会发生什么？

```kotlin
Row {
    Text("你好")
    Text("小明")
}
```

默认横向放置，两段文字会靠在一起。先不要添加间距，观察容器方向本身造成的变化。

![Column 和 Row 对同样两段文字的排列](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01-layout-compare.png)

**检查点 C：**写两次 `Text` 就一定会显示两行吗？

<details>
<summary>回答后再展开</summary>

不一定。内容和布局共同决定结果。Column 竖排，Row 横排；移除内部 Column 后，还要看外层布局。本项目的练习区域本身也在 Column 中，所以不能用“删掉内部 Column 后仍有两行”证明容器不重要。

</details>

## 6. 动手前，把这四句话串起来

先不用背术语，试着补全：

> Android 调用 ______。Activity 用 ______ 设置 Compose 内容入口。内容调用我的 ______ 函数。函数中的 ______ 决定文字竖排，______ 描述文字。

<details>
<summary>核对，再用自己的话解释一遍</summary>

`onCreate` → `setContent` → `Greeting` / `GreetingExercise` → `Column` → `Text`。

更完整一点：Compose 执行内容描述并维护 UI 结构，经过布局和绘制，才产生看到的画面。能解释这条链之后，再继续下面的练习。

</details>

## 7. 亲手写：每一步先预测，再运行

打开 `app/src/main/java/dev/learning/compose/Exercises.kt`。用搜索找到 `private fun GreetingExercise`。**只修改这个函数和指定的调用位置；其他课程的 TODO 留到对应章节。**

![第一课练习页修改前的真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/01-practice-start.png)

这是修改前的真实练习页。“01 · 在这里写第一个 Composable”所在区域，就是下面实验要改变的区域；上方课程外壳由其他代码提供。

### 第 1 步：确认你修改的代码真的进入调用链

初始函数是：

```kotlin
@Composable
private fun GreetingExercise() {
    // 这里有练习提示注释
    Text("01 · 在这里写第一个 Composable")
}
```

先只替换引号里的内容，得到：

```kotlin
@Composable
private fun GreetingExercise() {
    Text("你好，Compose！")
}
```

**先预测：**课程标题会变，还是练习区会变？

在 Android Studio 点击 Run，运行 `app`，进入第 1 课，向下找到练习区。应看到“你好，Compose！”，课程标题保持原来的内容。

如果仍是旧文字，先确认修改已保存、Run 已完成，并且打开的是练习页。不要进入“可操作示范”检查自己的改动。

**解释结果：**你修改的是调用链末端 `GreetingExercise` 的 Text 参数，所以改变了这个区域的内容。

### 第 2 步：添加第二段文字，并指定排列

把整个函数替换为：

```kotlin
@Composable
private fun GreetingExercise() {
    Column {
        Text("你好，Compose！")
        Text("我是小明")
    }
}
```

这个文件已经导入 `Column`、`Text` 和 `Composable`，无需再添加同名 import。

**先预测：**第二句放在哪里？运行检查，应该在第一句下面。

接着亲手交换两次 Text 的位置，再运行。应该先显示“我是小明”，再显示“你好，Compose！”。解释时要说出：改变了 Column 子内容的顺序。

**变式实验：**把 `Column` 改成 `Row`，并添加：

```kotlin
import androidx.compose.foundation.layout.Row
```

重新运行，观察横排结果。验证后改回 Column。两句靠在一起是这一步的预期；第 4 课再学间距。

### 第 3 步：让界面由参数决定

现在把硬编码的名字改成参数。**两处一起修改。**

第一处，函数定义：

```kotlin
@Composable
private fun GreetingExercise(name: String) {
    Column {
        Text("你好，$name！")
        Text("这是我写的第一个 Compose 界面")
    }
}
```

第二处，仍在同一个文件中的 `PracticeScreen`，把：

```kotlin
0 -> GreetingExercise()
```

改成：

```kotlin
0 -> GreetingExercise("小明")
```

**先预测：**第一行应该是“你好，小明！”。运行确认后，只把调用处参数改成你自己的名字，再运行。函数本体不用跟着改，文字应该随参数变化。

这一步已经体现出声明式 UI 的一个基础：**组件接收数据，描述这个数据对应的界面。**当前是源码中的固定参数，还没有加入运行中的输入与状态。

**若提示 `No value passed for parameter 'name'`：**函数签名已经改了，但有调用处忘记传名字。搜索 `GreetingExercise(`，同时核对定义与调用。

### 第 4 步：可选，只改变样式

前面三步完成后再做。文件顶部添加：

```kotlin
import androidx.compose.material3.MaterialTheme
```

把第一行 Text 改成：

```kotlin
Text("你好，$name！", style = MaterialTheme.typography.headlineMedium)
```

重新运行，第一句应更醒目。这里仍然是一个 Text，只是多接收一个样式参数；`MaterialTheme.typography` 提供主题中的文字样式。

## 8. 看成果，但分清示意与真实运行

![第 01 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/01.png)

这是带主题样式的目标效果示意。你这章主要验收文字、参数与排列；字体和课程外壳不必与图逐像素一致。

![第 01 课可操作示范的真实模拟器截图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/01-running.png)

这是项目已有示范的真实运行画面，额外带了切换名字的按钮。按钮依赖第二章的状态知识，本章练习不要求实现它。完成静态练习后再体验示范，下一章会解释怎样让名字在运行中改变。

## 9. 验收：能写出来，也能解释

先不看前面的代码，完成下面五项。遇到不会的地方，按右列回读。

| 检查任务 | 回读位置 |
| --- | --- |
| 说出从 Activity 到第一课练习函数的调用路径 | 第 2 节 |
| 解释为什么只新增带注解的函数不会显示 | 第 2–3 节 |
| 解释 Text 为什么不需要返回 TextView | 第 4 节 |
| 独立写出接收 `name`、竖排两句文字的函数，并正确调用 | 第 5、7 节 |
| 预测改名、交换顺序、改为 Row 三种变化，再运行核对 | 第 7 节 |

### 两道迁移题

**题 1：**下面在 Column 中调用同一个函数两次，会显示什么？`GreetingExercise` 是第 3 步的实现。

```kotlin
Column {
    GreetingExercise("小明")
    GreetingExercise("小红")
}
```

**题 2：**你想把“你好，小明！”改成“早上好，小明！”。应该改调用处的名字参数，还是修改组件中的问候模板？为什么？

<details>
<summary>先作答，再看解析</summary>

题 1：依次显示两组问候，每组两句，一共四段文字，上下排列。两次调用分别传入不同数据。`GreetingExercise` 没有全局唯一的“控件身份”，也不是只能用一次。

题 2：改模板为 `Text("早上好，$name！")`。名字参数代表“对谁问候”，模板代表“怎么问候”。把名字参数写成“早上好，小明”会拼出错误内容。这是设计组件参数时要分清的职责。

</details>

### 卡住时按现象排查

| 现象 | 检查与原因 |
| --- | --- |
| 新写的函数没有出现在界面 | 是否被当前内容调用？注解不会自动挂到页面上 |
| Composable 调用上下文报错 | 是否删除了练习函数上的注解，或在普通函数里直接调用了 Text？ |
| Row 显示红色 | 是否添加 `androidx.compose.foundation.layout.Row` import？ |
| 要求传 `name` 参数 | 是否同步修改了 `PracticeScreen` 中的调用？ |
| Run 后练习没变 | 是否保存并运行成功，进入的是第 1 课练习页，查看的是练习区？ |
| 没有内部 Column 也能竖排 | 检查外层：本项目的 PracticeScreen 已经有 Column |

到这里，你应该能自己回答“函数为什么能成为界面”，而不只是记住三个 API 名字。如果只能复述代码，先回到检查点 A–C，再做一次预测实验。

下一章：[02 · 状态与重组](02-state.md)。我们将在已经理解的调用与界面描述之上，回答一个新问题：**点击之后数据变了，谁通知 Compose 更新？**
