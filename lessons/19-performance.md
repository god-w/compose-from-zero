# 19 · 性能：先定位成本，再证明改善

> **Kotlin 前置回查：** [K07 集合与不可变更新](kotlin/K07-collections.md) · [K10 委托属性与 by](kotlin/K10-delegation.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**建立可复现场景，分清组合、布局、绘制成本，并记录真实测量。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 19 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/19.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 19 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/19.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 从“感觉卡”转成可调查的问题

“页面卡”太宽泛。先写设备、构建、数据量、具体操作和现象。例如“500 项列表从顶部连续滚动 10 次，操作时出现长帧”。不能把重组次数直接等同于卡顿，重组可能便宜且必要。

第一章的三阶段现在用于定位：内容计算贵看组合，频繁测量看布局，复杂阴影/图形看绘制。Android Studio 的检查器帮助形成假设，帧数据帮助验证用户体验。

## 2. 先创建固定数据，不污染用户库

在独立性能实验函数中生成，不写入 Room 的真实任务表。新建 PerformanceExercise.kt：

```kotlin
package dev.learning.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun PerformanceExercise() {
    val tasks = remember { List(500) { index -> Task(index, "任务 ${500 - index}") } }
    var unrelated by remember { mutableIntStateOf(0) }
    val sorted = tasks.sortedBy { it.title }
    Column {
        Button(onClick = { unrelated++ }) { Text("无关更新 $unrelated") }
        LazyColumn {
            items(sorted, key = { it.id }) { task -> Text(task.title) }
        }
    }
}
```

在 `AdvancedExercises.kt` 已预留的 `PerformanceExerciseEntry()` 中，把占位 `Text` 换成 `PerformanceExercise()`；`18 -> PerformanceExerciseEntry()` 分支对应第 19 课，已存在。第 17、18 课的入口应继续可打开。排序 500 项通常不一定慢，此例是观察重复工作，不预先宣布它导致了卡顿。不要为了制造结论，把几秒 sleep 放在 UI 里。

## 3. 第二步：只有一个变量变化

点“无关更新”，内容再次执行时 sorted 会重新计算。记录 Inspector/Profiler 的观察，再只改一行：

```kotlin
val sorted = remember(tasks) { tasks.sortedBy { it.title } }
```

不相关状态变化时可复用排序结果。前提是 tasks 作为新列表值变化；如果修改同一个普通可变列表内部内容，这个 key 不会表达变化，可能得到旧结果。

这项改动减少重复计算，但帧表现是否改善，需要测量。记录没有可感知改善也完全合理。

![第 19 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/19-experiment.png)

教学示意是“同样的显示结果，不同的计算路径”，图里的记录栏不是实测成绩，不可作为性能证据。

## 4. 第三步：固定测量方法

1. 用相同设备、数据量和操作，先完成基线。
2. 检查构建类型。debug 用来定位，最终结论在代表性的 release/benchmark 构建验证；release 优化、真机速度与温度都会影响结果。
3. 使用 Android Studio Profiler / 系统跟踪观察帧与 CPU 开销。先确定具体昂贵调用，再改变一个因素。
4. 多次重复同一操作，记录中位数与波动。不要取最好的一次与最差的一次比较。
5. 改动后检查行为仍正确，包括排序、增删、项目身份。

可先用平台已有统计留一个起点，连接设备后：

```shell
adb shell dumpsys gfxinfo dev.learning.compose reset
# 在设备上执行你记录的固定滚动步骤
adb shell dumpsys gfxinfo dev.learning.compose framestats > /tmp/compose-frames.txt
```

这是辅助记录，不替代专门 benchmark；不同版本统计项可不同。保存完整输出和环境，别只截图一个百分比。

若本项目 release 未配置可安装签名，可在**本地性能实验**给 release 临时使用 debug signingConfig，构建安装；它只为本地运行，正式发布另配签名。性能报告要注明优化是否开启；不要把未优化 release 当成同等条件。

## 5. 先会选工具，再学更多工具

| 工具/方式 | 能回答什么 | 不能直接证明什么 |
| --- | --- | --- |
| Layout Inspector 重组计数 | 哪些区域更新 | 实际长帧一定由它导致 |
| CPU / 系统跟踪 | 哪些调用耗时、帧时序 | 任意设备都有相同结果 |
| Macrobenchmark | 固定用户操作的外部测量 | 未定义场景的全局体验 |
| Baseline Profile | 帮助常用路径提前优化 | 修复阻塞、错误布局或慢网络 |

需要可重复启动/滚动测量时再按官方文档建 benchmark 模块。先完成当前调查，避免同时改架构、动画、列表与编译设置，最后却不知道哪项有效。

## 6. derivedStateOf 解决的是结果变化频率

例如滚动位置频繁变，但“显示回顶部按钮”只在过阈值时改变。可将结果派生：

```kotlin
val showBackToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
```

这个片段假设已有 rememberLazyListState，并传给列表。不要给普通 `name + surname` 全部加 derivedStateOf；它也有成本。列表 key 解决身份，remember 缓存计算，derivedStateOf 控制派生结果更新，三者不是万能“加速开关”。

## 7. 性能记录模板

新建自己的 `notes/performance.md`，填写真实值：

| 项目 | 基线 | 改动后 |
| --- | --- | --- |
| 设备 / 系统 / 刷新率 | 待填写 | 相同 |
| 构建版本 / 优化状态 | 待填写 | 待填写 |
| 数据量 / 操作步骤 | 待填写 | 相同 |
| 重复次数 / 中位数 / 波动 | 待测量 | 待测量 |
| 行为回归检查 | 待填写 | 待填写 |

最后写结论：保留，因为……；或撤回，因为差异不稳定/收益不足。不要把讲义示例数值当实测结果。

## 8. 理解检查、迁移与排错

1. 重组很多，但每次极便宜，是否一定要优化？
2. remember 不给 tasks key 会怎样？
3. 改动后快一次，是否证明有效？

<details><summary>解析</summary>

1. 不一定，先看用户可感知成本与测量。
2. 缓存可能不随输入变化更新，换正确数据时仍显示旧排序。
3. 不足够，设备状态和测量噪声会波动，需要相同条件重复验证。

</details>

独立在任务清单调查一个真实症状，用同一套证据说明是否值得修。没有慢点时记录现状与不优化的理由。

| 症状 | 检查 |
| --- | --- |
| 改完更快但数据旧 | 缓存 key 是否覆盖真正输入？ |
| 测量每次差别很大 | 设备温度、后台工作、数据量、构建是否一致？ |
| debug 慢但 release 正常 | 将定位证据与最终体验区分记录 |
| 优化后状态错位 | 列表身份与行为验收是否仍通过？ |

官方核对：[性能实践](https://developer.android.com/develop/ui/compose/performance/bestpractices)、[Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)、[Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/overview)。

---

上一章：[第 18 章](18-adaptive.md) · 下一章：[第 20 章](20-mastery-project.md)
