# 12 · View 互操作：旧页面怎样逐块迁移？

> **Kotlin 前置回查：** [K05 接收者、扩展与作用域函数](kotlin/K05-receivers.md) · [K13 注解与 Java 互操作](kotlin/K13-interop.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**用 AndroidView 与 ComposeView 理解边界、更新与清理，而不是整页重写。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 12 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/12.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 12 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/12.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 回到第一章的两种界面世界

你已经会 TextView，也知道 Text 接收数据。旧项目可能还有成熟的地图、广告或自定义 View。迁移可以先保持数据逻辑，只替换一块界面。

| 宿主是谁？ | 接入工具 | 核心职责 |
| --- | --- | --- |
| Compose 页面要放 View | AndroidView | 创建 View，并把新状态应用到它 |
| 传统 View 页面要放 Compose | ComposeView | 为局部区域建立组合入口 |

数据只能有一个可信来源。不要让 EditText 和 Compose 输入框各自保存业务名字后再互相监听同步。

## 2. 实验 A：在当前项目放一个 TextView

无需新依赖。在包目录新建 `InteropExercise.kt`：

```kotlin
package dev.learning.compose

import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun InteropExercise() {
    var count by remember { mutableIntStateOf(0) }
    Column {
        Button(onClick = { count++ }) { Text("+1") }
        Text("Compose：$count")
        AndroidView(
            factory = { context -> TextView(context).apply { textSize = 20f } },
            update = { view -> view.text = "View：$count" }
        )
    }
}
```

在 `AdvancedExercises.kt` 已预留的 `InteropExerciseEntry()` 中，把占位 `Text` 换成 `InteropExercise()`；第 12 课的 `11 -> InteropExerciseEntry()` 分支已经存在。只替换这一处函数体内容，再从 App 首页进入第 12 课。

先预测再点击：两处都更新，因为它们读取同一个 count。factory 负责创建，update 负责应用当前数据，update 可以多次发生。

### 故意做坏

把赋文本移到 factory，并把 update 留空：

```kotlin
factory = { context -> TextView(context).apply { text = "View：$count" } },
update = { }
```

点击后 Compose 文字改变，旧 View 可能仍是创建时的文本。恢复 update，解释为什么不能把每次更新的工作只写到创建阶段。

![第 12 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/12-experiment.png)

教学示意显示相同 count 的两个界面；更新断开后会产生分歧。

## 3. 实验 B：在传统 Activity 中建立 Compose 区域

新建 `LegacyHostActivity.kt`。本例用代码建立 LinearLayout，不需要 XML：

```kotlin
package dev.learning.compose

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView

class LegacyHostActivity : ComponentActivity() {
    private val name = mutableStateOf("小明")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(Button(this).apply {
            text = "从旧 View 改名字"
            setOnClickListener { name.value = "小红" }
        })
        root.addView(ComposeView(this).apply {
            setContent { MaterialTheme { Text("你好，${name.value}") } }
        })
        setContentView(root)
    }
}
```

在 AndroidManifest.xml 的 application 内声明 `<activity android:name=".LegacyHostActivity" android:exported="false" />`。从已有 Compose 按钮启动它时，在 Composable 中取得 `LocalContext.current`，事件里 `context.startActivity(Intent(context, LegacyHostActivity::class.java))`。需要 android.content.Intent、androidx.compose.ui.platform.LocalContext 导入。

先确认旧按钮与 Compose 文字共存，再点击检查更新。这个小样本的 name 是 Activity 内存状态，旋转会重建；若需恢复，用前几章学到的状态持有方案。

## 4. Fragment 多一层 View 生命周期

Activity 与 Fragment 的 View 生命周期不能混为一谈。在已有 Fragment 的 onCreateView / onViewCreated 中配置 ComposeView 时，常用：

```kotlin
composeView.setViewCompositionStrategy(
    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
)
```

导入 `androidx.compose.ui.platform.ViewCompositionStrategy`。这段作用于你已有的 composeView，不是要本项目额外引入 Fragment 框架。销毁 Fragment View 时组合应被清理；重新创建 View 时重新建立。

涉及 View 监听器时，应明确注册与注销；若使用 AndroidView 的 onRelease 清理回调，按该 overload 文档选择，别把整个组合的清理和 View 复用混成一件事。

## 5. 理解检查

1. AndroidView 的 update 会创建新 TextView 吗？
2. 旧按钮修改普通 String 变量，Compose 一定感知吗？
3. 为什么 Fragment 不能只考虑 Fragment 实例是否还在？

<details><summary>解析</summary>

1. 创建主要在 factory；update 把数据应用给已有实例。
2. 不一定。本例使用可观察状态，普通变量没有同等通知能力。
3. Fragment 实例可能仍在，但它的 View 已销毁；组合资源要匹配 View 生命周期。

</details>

## 6. 验收、迁移、排错

独立把传统 TextView 换成你熟悉的一个自定义 View，保持 factory 创建、update 更新的边界。若注册监听，记录退出后如何清理。

| 现象 | 检查 |
| --- | --- |
| View 只显示初值 | 状态更新是否只写在 factory？ |
| 旧按钮点击后 Compose 不变 | 修改的值是否可观察，是否同一份数据？ |
| Activity 无法打开 | Manifest 是否声明，Intent 是否指向正确类？ |
| 多次进入出现重复回调 | 监听注册与注销是否成对？ |

完成标准：两个方向都能解释，至少 AndroidView 实验亲手运行；已有 Fragment 场景按 View 生命周期清理。官方核对：[Compose 中放 View](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose)、[View 中放 Compose](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views)。

---

上一章：[第 11 章](11-quality.md) · 下一章：[第 13 章](13-animation.md)
