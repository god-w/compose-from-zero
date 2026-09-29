package dev.learning.compose

internal data class VisualGuide(val question: String, val steps: List<String>)

internal val visualGuides = listOf(
    VisualGuide("把传入的 `name` 从“小明”改成“你自己的名字”，你认为要不要找到 `TextView` 才能更新？", listOf("先只运行原始练习，记下屏幕上已有的文字。", "给 `GreetingExercise` 增加 `name` 参数；先只让第二行显示参数，运行一次。", "再加标题和排版。改两次传入的名字，每次都预测第二行会显示什么。")),
    VisualGuide("如果在 Composable 内直接写 `var count = 0`，点三次按钮后屏幕会稳定显示 3 吗？", listOf("先写普通局部变量和按钮，亲眼观察它为何没有稳定更新。", "换成 `remember { mutableIntStateOf(0) }`，点三次并旋转。", "再换成 `rememberSaveable`，重复点击、旋转和清零。")),
    VisualGuide("输入一个字时，`NameField` 应该自己保存名字，还是只把新值通知父组件？", listOf("先画出父组件持有 `name`、子组件接收 `value` 的两层树。", "给子组件加 `value` 和 `onValueChange` 参数，暂时只显示输入框。", "在父组件回调中更新 `name`；输入、删除、旋转各试一次。")),
    VisualGuide("头像和姓名放在同一个 `Column` 会是什么排列？换成外层 `Row` 呢？", listOf("先只放三个带文字的占位块，观察 `Row` 和 `Column` 的方向。", "按图改成 `Row(头像, Column(姓名, 副标题))`。", "分别交换 `padding` 与 `background` 的顺序，截图比较背景覆盖范围。")),
    VisualGuide("A、B、C 删除 A 后，原来下标 1 的 B 变成下标几？它的身份是否改变？", listOf("先做只显示三项的 `LazyColumn`，每项显示 `id` 和标题。", "用稳定 `id` 作为 `key`，加入删除 A 的按钮。", "给 B 加可观察的局部状态，删除 A 后确认状态仍跟着 B。")),
    VisualGuide("离开计时器页面后，它的协程应该继续运行吗？", listOf("先只显示秒数和静态文字，不放循环。", "用 `LaunchedEffect(Unit)` 每秒更新一次，停在页面观察 3 秒。", "返回首页再进入，记录秒数从哪里开始；再加暂停状态。")),
    VisualGuide("过滤成“已完成”时再删除一项，原始列表和可见列表各会怎样变化？", listOf("用纸先写出 A 未完成、B 已完成时三个过滤结果。", "只实现添加和总数；运行后再加入勾选、删除。", "最后从唯一 `tasks` 源推导过滤结果，照验收脚本逐步操作。")),
    VisualGuide("点击一项后，哪一层应该决定新任务列表：行组件还是状态持有者？", listOf("先保留现有 UI，只把 `TaskRow` 改为接收值与事件。", "把添加、完成、删除逻辑逐个移入 ViewModel；每迁移一项就运行。", "观察一次点击如何经过回调、StateFlow，再回到 UI。")),
    VisualGuide("在详情页打开期间任务被删除，详情页还应显示旧对象吗？", listOf("先让列表点击只传任务 `id`，日志或页面标题显示它。", "创建详情目的地，按 `id` 从状态源取得当前任务。", "加入任务不存在的界面，再验证系统返回与旋转。")),
    VisualGuide("一条只检查“按钮存在”的测试，能发现忘记 `trim()` 吗？", listOf("写下用户步骤：输入空格 → 点击添加 → 数量仍为零。", "用测试 API 对输入框执行输入和点击；先让断言通过。", "故意临时删掉 `trim()`，确认测试失败，再恢复实现。")),
    VisualGuide("如果屏幕只画了一个勾选图标，TalkBack 能知道它代表“已完成”吗？", listOf("开启 TalkBack，先听当前任务行被读成什么。", "给状态与删除动作补语义描述，重新听读。", "把系统字体放大，检查标题、按钮和列表是否还能操作。")),
    VisualGuide("已有 Fragment 只想迁移一小块 UI，必须整页重写吗？", listOf("在旧页面找一个边界清楚的区域，先画出 View 与 Compose 的交界。", "用 `ComposeView` 放一个静态文本，确认能显示。", "再用 `AndroidView` 包装一个旧 View，并在状态变化时验证 `update` 被调用。")),
    VisualGuide("快速连续点“展开/收起”时，动画最后应听从点击次数还是当前 `expanded`？", listOf("先完成没有动画的展开/收起，确认状态正确。", "只给背景颜色加动画，慢点和快点各试三轮。", "再加内容显隐与尺寸动画，观察最终界面是否与状态一致。")),
    VisualGuide("轻点和水平拖动从同一个位置开始，怎样避免把拖动当成点击？", listOf("先用普通 `clickable` 完成可访问的点击操作。", "给拖动增加可视化位移，观察轻点时位移是否为零。", "最后添加按钮形式的等价操作，用键盘或 TalkBack 试一遍。")),
    VisualGuide("Canvas 画出的 50% 进度环，TalkBack 会自动读出“50%”吗？", listOf("先用普通 `Text` 显示 0%、50%、100% 三个测试值。", "画背景弧和进度弧，再把进度限制在 0..1。", "补语义描述；调大字体观察图与文字是否重叠。")),
    VisualGuide("旋转、杀进程、重新启动，哪种操作会让内存状态消失？", listOf("先对当前任务清单依次做三种操作，记录丢失了什么。", "只把筛选条件改为可恢复的小状态，重复实验。", "最后把任务放入 Room，再次强制停止并启动。")),
    VisualGuide("“请求成功但列表为空”和“请求失败”，用户下一步应该看到同一句话吗？", listOf("先用假仓库固定返回 Loading、Empty、Success、Error 四种结果。", "给每种状态做最小界面；先验证文字，再接真实列表。", "人为延迟旧请求并快速切换筛选，观察是否被旧结果覆盖。")),
    VisualGuide("手机显示详情时把窗口拉宽，当前选中的任务应该消失吗？", listOf("先在窄屏保存一个选中 `id`，确认能进入和返回详情。", "把窗口拉宽，改成列表和详情并排显示，仍使用同一个 `id`。", "删除选中任务，再缩回窄屏，检查空状态和返回路径。")),
    VisualGuide("给每个表达式都加 `remember`，一定会让列表更快吗？", listOf("选一个可重复的慢场景，记录设备、构建类型和操作。", "先测基线，再只改一个候选因素，例如列表 key。", "用同样操作复测；没有稳定改善就撤回改动。")),
    VisualGuide("一个功能能在自己手机上点通，是否足以证明它可交付？", listOf("先用第 20 课评分表评估当前阶段项目，每项写一条证据。", "按最低分维度补功能、测试或恢复能力，每次只改一个目标。", "拿一台不同尺寸设备或模拟器，按交付清单完整走一遍。")),
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
