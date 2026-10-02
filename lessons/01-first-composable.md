# 01 · 从改一行文字开始

这节课陪你做一个很小的页面：上面写“你好，Compose！”，下面写“我是小明”。你已经会一些 Android 开发，我们就借助熟悉的“修改代码 → 运行 → 看屏幕”来开始。

**先只做第 1 步。看见屏幕上的文字变了，再往下读。** 不必一次读完整章，也不必先理解整个项目。

今天会遇到三个名字：`Text` 显示文字，`Column` 把内容上下排列，`@Composable` 标记可以描述 Compose 界面的函数。每遇到一个，我们都会马上用它做出看得见的结果。

<!-- visual-start -->
## 0 · 找到你要改的地方

在 Android Studio 左侧项目窗口中，找到这个文件。若当前视图把目录合并成 `dev.learning.compose`，直接展开这个包即可。

```text
app
└── src/main/java
    └── dev/learning/compose
        └── Exercises.kt    ← 打开它
```

也可以使用「Navigate → File…」，输入 `Exercises.kt` 打开它。在文件中搜索 `private fun GreetingExercise`，找到：

```kotlin
@Composable
private fun GreetingExercise() {
    // 这里是第 1 课的练习位置。
    Text("01 · 在这里写第一个 Composable")
}
```

`//` 开头的是注释，只是给人看的说明，不会显示在屏幕上。你的文件里注释文字可能更详细，找到这个函数就对了。

**先在这个函数里改代码。** `MainActivity.kt` 已经接好了页面入口，你暂时不用修改它。

![第 01 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01.png)

看这张定位图：文件里的一次 `Text(...)` 调用，对应练习区的一段文字。

### 先运行原始页面

1. Android Studio 顶部运行配置选择 `app`，设备选择模拟器或真机。
2. 点绿色运行按钮，等应用打开。
3. 在 App 首页点「01 第一个 Composable」，停留在练习页。
4. 在页面下方找到「01 · 在这里写第一个 Composable」。

这个位置就是你将要改变的练习区。页面上方的课程标题、图解和按钮是教程外壳，由项目中其他代码显示。

暂时不用点「先体验可操作示范」：本次先直接修改自己的练习区，跟上下面的小步骤。
<!-- visual-end -->

## 1 · 只改引号里的文字

找到这一行：

```kotlin
Text("01 · 在这里写第一个 Composable")
```

只把双引号里面的内容改成：

```kotlin
Text("你好，Compose！")
```

括号、双引号和函数外面的花括号都保留。修改后，整个函数应当是：

```kotlin
@Composable
private fun GreetingExercise() {
    Text("你好，Compose！")
}
```

再点一次绿色运行按钮。若 App 回到首页，重新进入第 1 课；看练习区，而不是只看讲义图片。

**你应该看到：** 原来的占位文字变成了“你好，Compose！”。

### 看到结果后，再理解这一行

`Text("你好，Compose！")` 可以读成：“在这里显示一段文字，内容是你好，Compose！”

你熟悉的 Android 中，`TextView` 可以显示文字。Compose 中，我们调用 `Text(...)` 来描述一段文字应该显示什么。这个函数现在只有一次 `Text` 调用，所以练习区只有一段文字。

`@Composable` 是写在函数上方的标记。先记住：**要在自己写的函数里调用 `Text` 这类 Compose UI 函数，就把这个函数标记为 `@Composable`。** 练习已经帮你写好了这个标记。

### 轮到你做一个小改动

把文字改成“我已经写出第一行 Compose 了”，运行一次。看到它出现在屏幕上后，再改回“你好，Compose！”。

- [ ] 我知道要打开 `Exercises.kt`。
- [ ] 我知道修改的是 `GreetingExercise` 里的文字。
- [ ] 我重新运行后，在练习区看到了自己写的文字。

做到这里，就已经完成了今天的第一个小目标。

## 2 · 用 Column 放两行文字

接下来，在问候下方显示“我是小明”。除了写两个 `Text`，还需要告诉 Compose 它们怎样排列。这里选择上下排列。

把整个 `GreetingExercise` 替换成下面这段，函数外面的其他代码先不改：

```kotlin
@Composable
private fun GreetingExercise() {
    Column {
        Text("你好，Compose！")
        Text("我是小明")
    }
}
```

`Column` 的意思是“一列”：花括号里的内容按照代码顺序，从上往下排列。

读代码时，把它拆成三句话：

1. `Column { ... }`：接下来这些内容上下排列。
2. 第一个 `Text`：第一行显示“你好，Compose！”。
3. 第二个 `Text`：第二行显示“我是小明”。

![Column 中两次 Text 调用与两行文字的对应关系](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01-code-to-screen.png)

图里左侧是代码，右侧是练习区的排列；相同颜色代表对应的那一行。

重新运行，进入第 1 课。你应该看到上下两行文字。

### 花括号到底包住了什么？

```kotlin
private fun GreetingExercise() {  // 函数开始
    Column {                     // 上下排列的区域开始
        Text("你好，Compose！")
        Text("我是小明")
    }                            // 排列区域结束
}                                // 函数结束
```

外面一对花括号属于函数，里面一对属于 `Column`。把光标放在其中一个花括号旁边，Android Studio 会帮助你找到配对的另一个。

### 轮到你验证排列顺序

先预测：如果把两个 `Text` 的顺序交换，哪句话会在上面？交换、运行，亲眼确认后再恢复原顺序。

- [ ] 我看到两行文字，没有叠在一起。
- [ ] 我交换两个 `Text` 后，屏幕上的顺序也随之交换。
- [ ] 我能说出：`Column` 负责上下排列，`Text` 负责显示文字。

`Column` 和 `Text` 在这个项目中已经导入好了。如果它们变红，先看本章最后的排错表。

## 3 · 让调用者决定名字

完成前两步再做这一步。我们把固定的“小明”变成一个可传入的名字。

你可能写过这样的普通 Kotlin 函数：

```kotlin
fun greeting(name: String): String {
    return "我是 $name"
}
```

调用 `greeting("小明")`，参数 `name` 收到“小明”。Compose 函数也能接收参数，语法还是熟悉的 Kotlin。

### 这次需要一起修改两处

**第一处：修改函数定义。** 把 `GreetingExercise` 替换成：

```kotlin
@Composable
private fun GreetingExercise(name: String) {
    Column {
        Text("你好，Compose！")
        Text("我是 $name")
    }
}
```

比上一步只有两个变化：

| 变化 | 可以怎样读 |
| --- | --- |
| `name: String` | 函数需要接收一个字符串，名字叫 `name` |
| `"我是 $name"` | 把收到的名字放到这段文字里 |

`$name` 是 Kotlin 的字符串模板。比如 `name` 是“小明”，最终文字就是“我是 小明”。引号里的空格也会显示出来。

**第二处：修改调用位置。** 在同一个文件中搜索：

```kotlin
0 -> GreetingExercise()
```

它在上方的 `PracticeScreen` 函数里。把这一行改成：

```kotlin
0 -> GreetingExercise("小明")
```

`0` 在这里表示第 1 课的编号。现在只需要认出这一行是在“打开第 1 课时，调用这个练习函数”。其他分支继续保留。

改完函数定义、还没改调用处时会出现红线：函数现在需要名字，调用处却还没提供。把两处都改完，再运行。

### 把整个过程连起来看

```text
GreetingExercise("小明")   ← 调用者提供“小明”
          ↓
name 收到“小明”
          ↓
Text("我是 $name")         ← 用收到的名字描述文字
          ↓
屏幕第二行：我是 小明
```

运行，确认第二行显示“我是 小明”。然后只把调用处的“小明”改成你的名字，再运行一次。

**你应该看到：** 第一行不变，第二行变成你的名字。

这里是在修改源代码后重新构建运行，验证参数。下一章才学习“用户点击按钮后，如何在应用运行期间更新屏幕”。这两种实验先分开做，会更容易理解。

- [ ] 我修改了函数定义和调用位置两处。
- [ ] 我只改变调用处的名字，就改变了第二行文字。
- [ ] 我能指出 `name` 在哪里收到值、在哪里被显示。

## 4 · 可选：把标题放大一点

如果前三步已经理解，可以再做一个小的外观调整。

在文件顶部的 `import` 区域加入：

```kotlin
import androidx.compose.material3.MaterialTheme
```

`import` 表示使用某个库提供的名字。这一行让这个文件能够使用 `MaterialTheme`。

接着把第一个 `Text` 改成：

```kotlin
Text(
    text = "你好，Compose！",
    style = MaterialTheme.typography.headlineMedium
)
```

`text =` 指定内容，`style =` 指定文字样式。这里选用项目主题提供的标题样式。主题系统会在后续课程详细解释。

运行后，第一行应比第二行更像标题。

## 做完后对照一下

参数版的完整练习函数如下。先自己完成，再对照括号、参数和字符串模板。

```kotlin
@Composable
private fun GreetingExercise(name: String) {
    Column {
        Text("你好，Compose！")
        Text("我是 $name")
    }
}
```

对应的调用是：

```kotlin
0 -> GreetingExercise("小明")
```

若完成了可选的第 4 步，第一个 `Text` 还会带 `style` 参数，文件顶部会多一条 `MaterialTheme` 的导入。

### 给自己的三道小题

1. 想把问候放在名字下方，应该改变哪里？
2. 想把“小明”改成“小红”，参数版中只需要修改哪里？
3. `Text` 和 `Column` 各负责什么？

<details><summary>做完后再看答案</summary>

1. 交换 `Column` 中两次 `Text` 调用的顺序。
2. 修改 `0 -> GreetingExercise("小明")` 中传入的字符串。
3. `Text` 描述要显示的文字；`Column` 把里面的内容上下排列。

</details>

回答时可以看自己的代码。能找到相应位置并解释，比背诵术语更有用。

## 卡住了，按现象找原因

| 你遇到的现象 | 先做这一件事 |
| --- | --- |
| 找不到练习函数 | 在 `Exercises.kt` 搜索 `private fun GreetingExercise` |
| 修改代码后，屏幕还是旧文字 | 点运行按钮，等安装完成，再进入第 1 课练习页 |
| 看到能切换名字的按钮，却看不到自己的改动 | 你在可操作示范页；点返回，查看练习页下方 |
| `Text` 或 `Column` 变红 | 对照下面的导入，确认它们位于文件顶部 |
| 提示缺少参数 `name` | 把调用改成 `GreetingExercise("小明")` |
| 提示引号或括号错误 | 对照当前步骤的完整函数，检查引号与括号是否成对 |
| 提示只能在 Composable 中调用 | 确认函数上方保留了 `@Composable` |

这个文件本来已经有以下导入，无需重复添加：

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
```

构建报错时，先看最上方指向你修改位置的错误。修好它再运行；每次只改一个原因，容易判断结果。

## 以后再回头理解页面入口

完成前三步后，你可能会好奇：这个函数为什么能出现在 Activity 里？

显示路径是：`MainActivity` 提供 Compose 内容区域 → `Workshop` 显示课程外壳 → `PracticeScreen` 选择第 1 课 → `GreetingExercise` 描述你的练习内容。入口里的 `setContent` 提供了放 Compose UI 的位置，功能上可以先类比熟悉的 `setContentView`。

这条路径已由项目接好。你现在能找到最末端的 `GreetingExercise` 并修改它就可以；熟悉更多组件后，再沿路径阅读源码。

### 想看可操作示范时

![第 1 课在 Android 模拟器中运行的可操作示范](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/01-running.png)

这是真实模拟器截图。App 中的「先体验可操作示范」可以切换名字，用到了下一章的状态知识。你可以体验它，先只观察“哪行文字发生变化”。

![第 01 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/01.png)

这是设计示意，展示后续还能怎样美化问候页面。本节先完成两行文字即可。

**到下一章前，只确认一件事：你能自己用 `Column` 和两个 `Text`，显示问候与你的名字。** 完成后再读 [第 02 课：状态与重组](02-state.md)。
