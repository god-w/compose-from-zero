package dev.learning.compose

internal data class VisualGuide(val question: String, val steps: List<String>)

internal val visualGuides = listOf(
    VisualGuide("为什么 GreetingExercise 没有返回 View，却能显示文字？只新增一个带 @Composable 注解的函数会自动显示吗？", listOf("先读第一章 1–6 节：从 XML 对照理解入口、调用链、组合、布局和绘制。", "按第 7 节改文字、交换两段 Text、把 Column 换成 Row；每次先预测，再运行。", "最后给函数添加 name 参数，同步修改调用处，并完成第 9 节的解释与迁移题。")),
    VisualGuide("如果在 Composable 内直接写 `var count = 0`，点三次按钮后屏幕会稳定显示 3 吗？", listOf("先写普通局部变量和按钮，亲眼观察它为何没有稳定更新。", "换成 `remember { mutableIntStateOf(0) }`，点三次并旋转。", "再换成 `rememberSaveable`，重复点击、旋转和清零。")),
    VisualGuide("输入一个字时，`NameField` 应该自己保存名字，还是只把新值通知父组件？", listOf("先画出父组件持有 `name`、子组件接收 `value` 的两层树。", "给子组件加 `value` 和 `onValueChange` 参数，暂时只显示输入框。", "在父组件回调中更新 `name`；输入、删除、旋转各试一次。")),
    VisualGuide("头像和姓名放在同一个 `Column` 会是什么排列？换成外层 `Row` 呢？", listOf("先只放三个带文字的占位块，观察 `Row` 和 `Column` 的方向。", "按图改成 `Row(头像, Column(姓名, 副标题))`。", "分别交换 `padding` 与 `background` 的顺序，截图比较背景覆盖范围。")),
    VisualGuide("A、B、C 删除 A 后，原来下标 1 的 B 变成下标几？它的身份是否改变？", listOf("先做只显示三项的 `LazyColumn`，每项显示 `id` 和标题。", "用稳定 `id` 作为 `key`，加入删除 A 的按钮。", "给 B 加可观察的局部状态，删除 A 后确认状态仍跟着 B。")),
    VisualGuide("重组和离开组合有什么区别？计时器的 key 改变时，旧协程会怎样？", listOf("先用 LaunchedEffect(Unit) 计时，观察状态变化不会每秒新建任务。", "增加 session 作为 key，用按钮验证旧任务取消、新任务启动。", "返回课程首页再进入，解释计数重置；别把按 Home 当作离开组合。")),
    VisualGuide("过滤成“已完成”时再删除一项，原始列表和可见列表各会怎样变化？", listOf("用纸先写出 A 未完成、B 已完成时三个过滤结果。", "只实现添加和总数；运行后再加入勾选、删除。", "最后从唯一 `tasks` 源推导过滤结果，照验收脚本逐步操作。")),
    VisualGuide("点击一项后，哪一层应该决定新任务列表：界面组件还是状态持有者？", listOf("沿用第 7 章 Task 与无状态 TaskContent，先理解 UiState 与 StateFlow。", "把添加、完成、删除逐个迁入 ViewModel；每迁移一项就验证行为。", "用生命周期收集接回 UI，解释 owner、旋转与进程结束的区别。")),
    VisualGuide("在详情页打开期间任务被删除，详情页还应显示旧对象吗？", listOf("先让列表点击只传任务 `id`，日志或页面标题显示它。", "创建详情目的地，按 `id` 从状态源取得当前任务。", "加入任务不存在的界面，再验证系统返回与旋转。")),
    VisualGuide("一条只检查“按钮存在”的测试，能发现忘记 trim 或添加错误标题吗？", listOf("先给普通 Kotlin 规则写 JVM 测试，验证空标题与去空格。", "配置 UI 测试，输入 A 并添加，断言标题和统计；空输入应断言按钮禁用。", "故意破坏添加结果，确认测试失败，再恢复实现。")),
    VisualGuide("如果屏幕只画了一个勾选图标，TalkBack 能知道它代表“已完成”吗？", listOf("开启 TalkBack，先听当前任务行被读成什么。", "给状态与删除动作补语义描述，重新听读。", "把系统字体放大，检查标题、按钮和列表是否还能操作。")),
    VisualGuide("AndroidView 的 factory 与 update 有什么区别？旧页面必须整页重写吗？", listOf("先在当前项目用 AndroidView 包装 TextView，两处显示同一 count。", "故意只在 factory 设置初值，观察不一致，再用 update 修复。", "用 ComposeView 做传统 Activity 的局部区域，并理解 Fragment View 的清理策略。")),
    VisualGuide("快速连续点“展开/收起”时，动画最后应听从哪个业务状态？", listOf("先完成没有动画的展开/收起，确认状态正确。", "用 AnimatedVisibility 表达内容出现，再给目标背景加颜色动画。", "快速反转并关闭系统动画，确认最终状态与操作仍正确。")),
    VisualGuide("短拖、长拖、取消各应该怎样影响任务？拖动距离就是完成状态吗？", listOf("先用按钮完成任务，明确 done 是业务状态。", "增加以像素表示的临时位移，把 dp 阈值换算后再判断长拖。", "正常松手与取消都复位，保留按钮等价操作，再测试滚动冲突。")),
    VisualGuide("Canvas 画出的 50% 进度环，TalkBack 会自动读出“50%”吗？", listOf("先用普通 `Text` 显示 0%、50%、100% 三个测试值。", "画背景弧和进度弧，再把进度限制在 0..1。", "补语义描述；调大字体观察图与文字是否重叠。")),
    VisualGuide("旋转、杀进程、重新启动，哪种操作会让内存状态消失？", listOf("先对当前任务清单依次做三种操作，记录丢失了什么。", "只把筛选条件改为可恢复的小状态，重复实验。", "最后把任务放入 Room，再次强制停止并启动。")),
    VisualGuide("“请求成功但列表为空”和“请求失败”，用户下一步应该看到同一句话吗？", listOf("先用假仓库固定返回 Loading、Empty、Success、Error 四种结果。", "给每种状态做最小界面；先验证文字，再接真实列表。", "人为延迟旧请求并快速切换筛选，观察是否被旧结果覆盖。")),
    VisualGuide("手机显示详情时把窗口拉宽，当前选中的任务应该消失吗？", listOf("先在窄屏保存一个选中 `id`，确认能进入和返回详情。", "把窗口拉宽，改成列表和详情并排显示，仍使用同一个 `id`。", "删除选中任务，再缩回窄屏，检查空状态和返回路径。")),
    VisualGuide("给每个表达式都加 remember，一定会让列表更快吗？怎样证明收益？", listOf("用固定 500 项实验观察无关更新是否重复排序，先记录基线。", "只把排序改为 remember(tasks)，确认输入变化与行为仍正确。", "在相同设备与代表性构建复测；记录真实数据，没有收益也可撤回。")),
    VisualGuide("换成习惯打卡后，谁定义今天、每日目标和归档规则？怎样证明能交付？", listOf("先独立定义日期与目标规则，完成模型测试和内存 UI 闭环。", "按四个里程碑加入事务持久化、恢复、无障碍与窗口适配。", "交付真实截图、测试与性能证据，用评分表复盘不足。")),
)

internal val lessonDiagrams = intArrayOf(
    R.drawable.lesson_01,
    R.drawable.lesson_02,
    R.drawable.lesson_03,
    R.drawable.lesson_04,
    R.drawable.lesson_05,
    R.drawable.lesson_06,
    R.drawable.lesson_07,
    R.drawable.lesson_08,
    R.drawable.lesson_09,
    R.drawable.lesson_10,
    R.drawable.lesson_11,
    R.drawable.lesson_12,
    R.drawable.lesson_13,
    R.drawable.lesson_14,
    R.drawable.lesson_15,
    R.drawable.lesson_16,
    R.drawable.lesson_17,
    R.drawable.lesson_18,
    R.drawable.lesson_19,
    R.drawable.lesson_20,
)
