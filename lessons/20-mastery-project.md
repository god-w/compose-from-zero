# 20 · 毕业项目：独立交付习惯打卡应用

> **Kotlin 前置回查：** [K06 类、属性与数据类](kotlin/K06-classes.md) · [K07 集合与不可变更新](kotlin/K07-collections.md) · [K08 密封类型与状态建模](kotlin/K08-sealed-state.md) · [K11 协程与取消](kotlin/K11-coroutines.md) · [K12 Flow 与 StateFlow](kotlin/K12-flow.md) · [K14 综合练习：纯 Kotlin 任务规则](kotlin/K14-integration.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**把所有章节迁移到新问题，留下功能、恢复、质量和性能证据，而不是复制任务清单。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 20 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/20.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 20 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/20.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 为什么换一个产品问题？

如果只复制第 7 章，你可能记住了答案却没有理解。习惯打卡保留列表、详情、状态、持久化，又新增日期与每日目标，迫使你重新决定模型和规则。

不要求一口气写完。分四个可演示里程碑，每阶段先完成最小闭环，再加入后面的能力。完成约需 2–4 周，以实际学习节奏为准。

## 2. 先把需求变成明确规则

最低产品范围：创建与编辑习惯、今日打卡、最近 7 天记录、归档筛选、本地持久化、手机/宽窗口、读屏与大字号。加载、失败、空数据都有明确界面。

| 问题 | 本章推荐的第一版决定 | 你要验证的边界 |
| --- | --- | --- |
| 一天如何定义？ | 设备当前本地日期，用可替换日期来源 | 跨午夜和时区变化策略 |
| 每日目标是什么？ | 正整数，可多次打卡至目标 | 0/负数拒绝；超过目标不再增加 |
| 重复动作怎么处理？ | 同一天 count 增加，最多 target | 快速点击、并发写入 |
| 归档后能打卡吗？ | 保留历史，但不接受新打卡 | 从旧详情入口操作 |
| 删除还是归档？ | 第一版先归档 | 历史记录不丢失 |

日期策略需要写进 README。不要直接用毫秒除以一天当所有地区的本地日期。

## 3. 里程碑 A：先写纯模型与规则

新建习惯相关文件，不修改 Task 的业务含义。普通 Kotlin 样本：

```kotlin
data class Habit(val id: Long, val title: String, val target: Int, val archived: Boolean = false)
data class CheckIn(val habitId: Long, val day: String, val count: Int)

fun incrementToday(habit: Habit, day: String, records: List<CheckIn>): List<CheckIn> {
    require(habit.target > 0)
    if (habit.archived) return records
    val current = records.firstOrNull { it.habitId == habit.id && it.day == day }
    val oldCount = current?.count ?: 0
    if (oldCount >= habit.target) return records
    val nextCount = (oldCount + 1).coerceAtMost(habit.target)
    val next = CheckIn(habit.id, day, nextCount)
    return records.filterNot { it.habitId == habit.id && it.day == day } + next
}
```

这里 day 是注入的 `YYYY-MM-DD` 实验输入，不在函数中读取真实时钟。已有打卡达到目标后保持原记录；即使目标降低，也不会因为再次点击而减少历史次数。它是内存规则，不保证数据库并发安全；里程碑 B 再把更新放事务。

先自己写 4 个 JVM 测试：首次打卡、达到目标、归档拒绝、不同日期互不覆盖。示例一条：

```kotlin
@Test fun targetCannotBeExceeded() {
    val habit = Habit(1, "阅读", 2)
    val result = incrementToday(habit, "2026-10-02",
        listOf(CheckIn(1, "2026-10-02", 2)))
    assertEquals(2, result.single().count)
}
```

使用第 10 章 JUnit 设置。日期为固定测试值，不是 app 的真实“今天”。

## 4. 再把规则接到最小 UI

先画这棵树，参数和回调按第三章设计：

```text
HabitRoute：收集 UI 状态与提交事件
  ├── HabitList(items, selectedId, onSelect)
  ├── TodayProgress(done, target)
  ├── CheckInButton(enabled, onCheckIn)
  └── HabitDetail(habit, lastSevenDays, onEdit, onArchive)
```

先固定“阅读，目标 2”一项：点击从 0/2 到 1/2 再到 2/2，按钮不可再加；换日期从该日记录查询。模型正确后才加输入表单、列表和详情。

**A 的完成证据：**录下创建、两次打卡、跨测试日期、归档四条路径；没有数据库也能解释全部状态归属。

## 5. 里程碑 B：Room 与恢复

沿用第 16 章的 Room / KSP 设置，改成 HabitEntity 和 CheckInEntity。记录表使用联合身份 `(habitId, day)`，防止同日产生重复记录：

```kotlin
@Entity(primaryKeys = ["habitId", "day"])
data class CheckInEntity(val habitId: Long, val day: String, val count: Int)
```

建议 foreign key 维护习惯与记录关联。打卡更新放 DAO 事务或原子 SQL 中；不要“读取旧 count → 两个协程各写 count+1”导致丢失一次打卡。数据库事务内查询当前 count、检查目标，再写入。

观察 Flow 把记录投影成 UI 状态。输入草稿/选中 id 用小状态恢复；习惯与打卡记录放数据库。删除和归档策略应明确区分。

**B 的完成证据：**打卡到 2/2 → 强制停止 → 启动仍为 2/2；旧 schema 升级保留记录；快速点击与事务规则测试通过。

## 6. 里程碑 C：可靠交互与窗口

- 第 9 章：详情只传 habitId，习惯不存在或已归档时有明确分支。
- 第 11 章：打卡动作包含习惯名和目标状态，大字号按钮可达，字符串资源化。
- 第 13 章：进度变化可有适度动效，但最终业务结果不等待动画。
- 第 15 章：圆环同时提供可读进度，不仅依赖颜色。
- 第 17 章：数据库或加载失败可以重试；取消不显示错误。
- 第 18 章：宽窗口两栏、窄窗口导航，选择不因窗口变化丢失。

![第 20 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/20-experiment.png)

教学示意是当日第一次打卡前后；成果效果图是设计参考，真实交付截图由你运行后生成。

**C 的完成证据：**空数据、长标题、TalkBack、200% 字体、窄/宽窗口均可操作；至少一条完整打卡 UI 测试通过。

## 7. 里程碑 D：性能与交付说明

沿第 19 章固定数据、设备、构建和操作做一次基线。测量后可以选择不优化，只要证据支持。记录设备与实际数字，不制造优化百分比。

README 必须写：运行依赖、功能范围、数据流、状态寿命、日期策略、schema 迁移、测试命令、测量结果与已知限制。提供真实截图；用“示意图”标明设计参考。

**D 的完成证据：**别人按照 README 能构建运行；所有关键路径可复现；测试与恢复证据可核对。

## 8. 每阶段卡住，回哪一章？

| 问题 | 回读课程 |
| --- | --- |
| 输入框不保持内容 | 03 输入闭环 |
| 统计和列表不同步 | 07 唯一数据源 |
| 两页显示不同数据 | 08 owner、09 导航作用域 |
| 强制停止记录没了 | 16 持久化链 |
| 旧请求覆盖新选择 | 17 取消与过期响应 |
| 分屏后选择变了 | 18 保存选择、推导布局 |
| 滚动慢但不知道原因 | 19 测量与定位 |

不要同时为同一个故障改七处代码。先用最小复现证明根因，再修改共享规则或正确层级。

## 9. 验收评分与最后的理解检查

| 维度 | 0 分 | 1 分 | 2 分 |
| --- | --- | --- | --- |
| 状态与恢复 | 数据错/丢 | 常规路径正确 | 快速操作、重启、升级都正确 |
| 架构 | UI 与存储强耦合 | 有明确持有者 | 数据流清晰、规则可独立测试 |
| 可操作性 | 关键动作不可达 | 普通触控可用 | 读屏、大字号、窗口都可用 |
| 可靠性 | 无验证 | 手工脚本 | 关键规则/UI/持久化自动检查 |
| 性能 | 明显卡顿 | 使用流畅 | 可复现测量与取舍说明 |

达标至少 8/10，且恢复不能为 0；这只是本项目评价，不等于行业认证。0 分先修，1 分给下一步计划。

先独立回答：一次打卡经过哪些层？谁决定今天？哪里保证不超过目标？旋转、进程回收和强停分别如何处理？读屏用户怎么知道完成？卡顿怎么定位？

<details><summary>核对方向，不提供整套毕业答案</summary>

事件从 UI 到状态持有者，再到普通业务规则与持久层；数据库观察结果回到 UI。日期来自明确且可测试的策略，目标规则在数据更新边界保证，状态恢复按寿命划分。语义提供等价信息，性能用固定场景测量。能结合你自己的文件和实验解释这些路径，才证明已经迁移了知识。

</details>

**毕业之后：**用新功能继续练，例如按周目标、云同步、多账号。每次先写规则与边界，再选已有 Compose 能力实现；不必靠继续堆 API 名单判断精通。

官方核对：[UI 架构](https://developer.android.com/topic/architecture/ui-layer)、[Room 事务 API](https://developer.android.com/reference/androidx/room/Transaction)、[应用质量](https://developer.android.com/docs/quality-guidelines/core-app-quality)。

---

上一章：[第 19 章](19-performance.md)
