# 13 · 动画：先定义状态，再定义怎样过渡

> **Kotlin 前置回查：** [K01 值、类型与字符串](kotlin/K01-values.md) · [K10 委托属性与 by](kotlin/K10-delegation.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**写可展开任务卡，分清目标值、动画中间值与业务真相。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 13 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/13.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 13 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/13.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从属性动画走到状态动画

View 的属性动画常指定从哪个值走到哪个值。Compose 常根据当前业务状态决定目标，再让动画 API 处理过渡。`expanded` 表示卡片是否展开，颜色只是它的表现，不应该反过来通过颜色猜是否展开。

| 数据 | 角色 | 谁读取 |
| --- | --- | --- |
| expanded | 业务/交互状态 | 按钮、文本、内容可见性 |
| target color | expanded 对应的目标颜色 | animateColorAsState |
| animated color | 每帧的过渡值 | background |

## 2. 文件和导入

修改 `AdvancedExercises.kt` 的 `AnimationExercise`，加入：

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
```

这些 API 由当前 Compose 依赖提供。本章不需要为了一个卡片增加动画库。

### 第一步：先没有动画

```kotlin
@Composable
private fun AnimationExercise() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column {
        Button(onClick = { expanded = !expanded }) {
            Text(if (expanded) "收起" else "展开")
        }
        if (expanded) Text("今天先做一个小练习，再记录理解。")
    }
}
```

先确保展开、收起、旋转行为正确。状态不正确时，加动画只会让错误看起来更平滑。

### 第二步：只给内容出现加过渡

把 `if (expanded)` 换成：

```kotlin
AnimatedVisibility(visible = expanded) {
    Text("今天先做一个小练习，再记录理解。")
}
```

运行，观察内容出现与退出。退出动画结束之前内容可能仍参与过渡，不要靠“立刻离开组合”做清理假设。

### 第三步：给背景加目标颜色

```kotlin
@Composable
private fun AnimationExercise() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val background by animateColorAsState(
        targetValue = if (expanded) Color(0xFFE1F3E8) else Color(0xFFE8EEFA),
        label = "card-background"
    )
    Column(Modifier.background(background).padding(16.dp)) {
        Button(onClick = { expanded = !expanded }) {
            Text(if (expanded) "收起" else "展开")
        }
        AnimatedVisibility(visible = expanded) {
            Text("今天先做一个小练习，再记录理解。")
        }
    }
}
```

先预测：快速展开、收起时，背景是否必须先完整走到旧目标？动画会响应新的目标；不要自己并行开多个协程强行写颜色。

![第 13 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/13-experiment.png)

图是展开前后两个端点，中间过程要在 app 运行观察。静态图无法证明动画的时序。

## 3. 要动画哪个量，选哪个层级

- 单一颜色、数值跟随目标：animate…AsState。
- 内容出现/消失：AnimatedVisibility。
- 多个值必须跟随同一状态协调：可进一步学 updateTransition。
- 需要手动启动、停止或与手势衔接：后续学 Animatable。

不要第一天就把所有场景统一成自建动画管理器。先让动画表达“展开/收起”这个已有状态。

## 4. 可访问性与恢复

内容是否展开要由文本和操作表达，不能只靠颜色。使用系统动画时长设置检查减少/关闭动画后的结果；仍需保持最终业务状态准确和按钮可操作。

旋转后保存 expanded 的目标，通常不需要保存每帧中间色值。不要把动画进度当数据库业务字段。

## 5. 理解检查

1. background 到了绿色，是否适合据此决定任务完成？
2. 为什么先做无动画版本？
3. 暂时关掉动画，用户是否还能知道已展开？

<details><summary>解析</summary>

1. 不适合。颜色是表现，业务状态才是判断依据。
2. 先验证状态、事件与内容逻辑，避免把业务故障和过渡故障混在一起。
3. 应该能，按钮文字和内容本身提供等价信息。

</details>

## 6. 迁移、验收与排错

把同样方式用于第 7 章任务的完成颜色，但 completed 数量仍从 task.done 推导。快速点击 10 次，最终状态与次数奇偶一致；动画没有延迟业务结果。

| 现象 | 检查 |
| --- | --- |
| 颜色不更新 | targetValue 是否读取 expanded？ |
| 内容闪现或重复 | 是否同时保留 if 和 AnimatedVisibility 两套内容？ |
| 重组后总收起 | 状态是否被正确保存？ |
| 退出动画时任务又写回旧值 | 是否用动画回调替代了业务状态更新？ |

完成标准：端点正确、快速反转正确、关闭动画仍能操作，并解释三种值。官方核对：[值动画](https://developer.android.com/develop/ui/compose/animation/value-based)、[显示与消失](https://developer.android.com/develop/ui/compose/animation/composables-modifiers)。

## 本章代码的真实运行对照

![第 13 章完整样本的真实模拟器画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/13-reference-running.png)

展开卡片后，背景与内容都到达展开状态。这是按本章代码在独立验证 app 中运行的截图；课程中的占位练习仍需你亲手完成。

---

上一章：[第 12 章](12-interop.md) · 下一章：[第 14 章](14-gestures.md)
