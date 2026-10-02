"""Generate the editable SVG teaching diagrams used by the lessons.

Run from the repository root with the bundled workspace Python (includes Pillow).
"""

from html import escape
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "assets" / "diagrams"
OUT.mkdir(parents=True, exist_ok=True)
ANDROID_OUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
ANDROID_OUT.mkdir(parents=True, exist_ok=True)

# Each diagram tells one small story. Keep labels short enough for a phone-width preview.
DIAGRAMS = [
    (1, "谁调用我的界面函数？", "入口建立组合，内容调用到练习区", [("Activity", "onCreate → setContent", "Android 启动页面"), ("课程外壳", "MaterialTheme", "内容里调用 Workshop"), ("选中第一课", "PracticeScreen(0)", "选择第 1 个练习"), ("你的代码", "GreetingExercise()", "描述练习区内容")]),
    (2, "状态驱动界面", "点击事件只修改状态", [("起点", "count = 0", "界面显示 0"), ("事件", "点击 +1", "onClick 执行"), ("状态", "count = 1", "Compose 观察到变化"), ("重组", "Text 读到 1", "界面显示 1")]),
    (3, "单向数据流", "数据向下，事件向上", [("父组件", "持有 name", "唯一可信来源"), ("向下传值", "NameField(value)", "子组件只显示"), ("用户输入", "onValueChange", "子组件报告事件"), ("向上更新", "父组件改 name", "新值再次传下")]),
    (4, "布局先看树", "再看 Modifier 的作用顺序", [("外层", "Row", "头像与文字横排"), ("左侧", "头像", "固定大小"), ("右侧", "Column", "姓名和副标题竖排"), ("修饰", "padding → background", "顺序影响可见范围")]),
    (5, "列表项的身份", "删除第一项后，B 还是 B", [("原列表", "A · B · C", "每项有稳定 id"), ("操作", "删除 A", "列表缩短"), ("错误 key", "下标 0 → B", "状态可能错配"), ("稳定 key", "id(B) 不变", "状态跟着 B 走")]),
    (6, "副作用跟随页面", "进入启动，离开取消", [("进入", "页面进入组合", "计时器可见"), ("启动", "LaunchedEffect", "协程每秒更新"), ("离开", "页面退出组合", "协程自动取消"), ("回来", "重新进入", "按设计重新开始")]),
    (7, "任务清单的数据流", "一个状态源，多处使用", [("输入", "trim 后添加", "空白不能入库"), ("任务源", "tasks 列表", "唯一真实列表"), ("派生", "filter(tasks)", "不存第二份列表"), ("界面", "统计 + LazyColumn", "事件回到状态源")]),
    (8, "状态持有者", "把业务更新放在 UI 外", [("用户", "点击完成", "触发事件"), ("回调", "onToggle(id)", "UI 不改业务数据"), ("ViewModel", "更新 StateFlow", "产生新 UiState"), ("Compose", "收集并显示", "状态变化驱动重组")]),
    (9, "导航只传身份", "详情页自己查询最新数据", [("列表", "点击任务 B", "知道 B 的 id"), ("路由", "task/{id}", "传稳定 id"), ("详情", "按 id 取任务", "数据可能已删除"), ("返回", "返回列表", "不复制整份 Task")]),
    (10, "测试用户行为", "输入、动作、可见结果", [("准备", "空任务列表", "每次独立开始"), ("输入", "输入空格", "模拟真实操作"), ("动作", "点击添加", "等待框架同步"), ("断言", "数量仍为 0", "能抓住 trim 回归")]),
    (11, "让所有人都能操作", "视觉、语义、不同字号", [("视觉", "任务行", "看到名称和状态"), ("语义", "完成状态描述", "TalkBack 读出来"), ("字号", "放大文字", "控件仍可触达"), ("语言", "strings 资源", "文案可本地化")]),
    (12, "View 与 Compose 共存", "迁移可以从一个控件开始", [("旧页面", "XML / Fragment", "现有页面照常运行"), ("ComposeView", "setContent", "嵌入新区域"), ("AndroidView", "factory + update", "复用旧控件"), ("生命周期", "进入与退出", "清理监听和组合")]),
    (13, "动画表达状态", "目标值由业务状态决定", [("业务状态", "expanded = false", "先保证行为正确"), ("事件", "点击展开", "expanded = true"), ("动画", "从旧值到目标值", "颜色与尺寸过渡"), ("结果", "内容展开", "最终状态准确")]),
    (14, "手势优先级", "先用语义化操作，再处理原始指针", [("需求", "点按 / 滑动", "先区分意图"), ("高层 API", "clickable", "自带语义与反馈"), ("必要时", "pointerInput", "处理拖动距离"), ("等价入口", "按钮 / TalkBack", "非手势用户也能用")]),
    (15, "绘制与布局是两件事", "先测量放置，再画内容", [("约束", "父组件给尺寸", "可用空间"), ("测量", "子组件求大小", "避免无限制"), ("放置", "决定 x / y", "不遮挡文字"), ("绘制", "Canvas 画进度", "另补语义描述")]),
    (16, "状态该活多久", "从临时交互到长期记录", [("组合内", "remember", "离开页面可消失"), ("重建后", "rememberSaveable", "小型界面状态"), ("屏幕逻辑", "ViewModel", "配置变化后还在"), ("长期数据", "Room", "重启后仍存在")]),
    (17, "异步结果有四种", "让每种结果都有对应 UI", [("开始", "Loading", "显示进度"), ("成功为空", "Empty", "给下一步建议"), ("成功有数据", "Success", "展示列表"), ("失败", "Error", "说明并可重试")]),
    (18, "同一数据适配窗口", "布局变化，选中任务不变", [("窄窗口", "列表 → 详情", "一次看一个页面"), ("窗口变宽", "宽度发生变化", "保留选中 id"), ("宽窗口", "列表 | 详情", "并排显示"), ("任务删除", "详情变为空", "不崩溃")]),
    (19, "性能先测再改", "每次只验证一个假设", [("症状", "滚动卡顿", "写清复现步骤"), ("基线", "记录设备与耗时", "先测量"), ("改动", "稳定 key 等", "只改一个因素"), ("复测", "同场景比较", "无改善就撤回")]),
    (20, "毕业项目交付链", "能运行、能恢复、能验证", [("功能", "创建与打卡", "核心流程完整"), ("持久化", "Room 保存", "重启数据仍在"), ("质量", "测试与无障碍", "边界也能使用"), ("证据", "性能与复盘", "说明取舍")]),
]


def svg(number, title, subtitle, steps):
    colors = ["#E8F0FE", "#E7F6EF", "#FFF1DB", "#F1EAFE"]
    borders = ["#4E74C8", "#288A63", "#B4771B", "#8260A5"]
    parts = [
        '<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="380" viewBox="0 0 1200 380" role="img" aria-labelledby="title desc">',
        f'<title id="title">{escape(title)}</title>',
        f'<desc id="desc">{escape(subtitle)}。' + "；".join(escape(a + "：" + b + "，" + c) for a, b, c in steps) + '</desc>',
        '<rect width="1200" height="380" rx="28" fill="#F8FAFD"/>',
        f'<text x="40" y="60" font-size="32" font-weight="700" fill="#202735" font-family="PingFang SC, Noto Sans CJK SC, sans-serif">{escape(f"{number:02d}  {title}")}</text>',
        f'<text x="40" y="95" font-size="20" fill="#536174" font-family="PingFang SC, Noto Sans CJK SC, sans-serif">{escape(subtitle)}</text>',
    ]
    for i, (heading, main, note) in enumerate(steps):
        x = 40 + i * 295
        parts.extend([
            f'<rect x="{x}" y="130" width="260" height="180" rx="20" fill="{colors[i]}" stroke="{borders[i]}" stroke-width="2"/>',
            f'<circle cx="{x + 30}" cy="163" r="17" fill="{borders[i]}"/>',
            f'<text x="{x + 30}" y="170" text-anchor="middle" font-size="19" font-weight="700" fill="white" font-family="Arial, sans-serif">{i + 1}</text>',
            f'<text x="{x + 20}" y="205" font-size="22" font-weight="700" fill="#202735" font-family="PingFang SC, Noto Sans CJK SC, sans-serif">{escape(heading)}</text>',
            f'<text x="{x + 20}" y="248" font-size="21" fill="#202735" font-family="PingFang SC, Noto Sans CJK SC, sans-serif">{escape(main)}</text>',
            f'<text x="{x + 20}" y="281" font-size="17" fill="#536174" font-family="PingFang SC, Noto Sans CJK SC, sans-serif">{escape(note)}</text>',
        ])
        if i < 3:
            parts.append(f'<path d="M{x + 266} 220 L{x + 288} 220 M{x + 280} 212 L{x + 288} 220 L{x + 280} 228" stroke="#63758A" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" fill="none"/>')
    parts.append('<text x="40" y="350" font-size="16" fill="#607086" font-family="PingFang SC, Noto Sans CJK SC, sans-serif">阅读顺序：从左到右。先预测结果，再到模拟器里验证。</text>')
    parts.append('</svg>')
    return "\n".join(parts) + "\n"


def png(number, title, subtitle, steps):
    scale = 2
    canvas = Image.new("RGB", (1200 * scale, 380 * scale), "#F8FAFD")
    draw = ImageDraw.Draw(canvas)
    font_path = "/System/Library/Fonts/PingFang.ttc"
    heading_font = ImageFont.truetype(font_path, 32 * scale)
    subtitle_font = ImageFont.truetype(font_path, 20 * scale)
    card_heading_font = ImageFont.truetype(font_path, 22 * scale)
    card_main_font = ImageFont.truetype(font_path, 21 * scale)
    card_note_font = ImageFont.truetype(font_path, 17 * scale)
    number_font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial.ttf", 19 * scale)

    def xy(x, y):
        return (x * scale, y * scale)

    draw.text(xy(40, 27), f"{number:02d}  {title}", font=heading_font, fill="#202735")
    draw.text(xy(40, 72), subtitle, font=subtitle_font, fill="#536174")
    colors = ["#E8F0FE", "#E7F6EF", "#FFF1DB", "#F1EAFE"]
    borders = ["#4E74C8", "#288A63", "#B4771B", "#8260A5"]
    for i, (heading, main, note) in enumerate(steps):
        x = 40 + i * 295
        draw.rounded_rectangle((*xy(x, 130), *xy(x + 260, 310)), radius=20 * scale, fill=colors[i], outline=borders[i], width=2 * scale)
        draw.ellipse((*xy(x + 13, 146), *xy(x + 47, 180)), fill=borders[i])
        draw.text(xy(x + 24, 148), str(i + 1), font=number_font, fill="white")
        draw.text(xy(x + 20, 179), heading, font=card_heading_font, fill="#202735")
        draw.text(xy(x + 20, 224), main, font=card_main_font, fill="#202735")
        draw.text(xy(x + 20, 261), note, font=card_note_font, fill="#536174")
        if i < 3:
            draw.line([xy(x + 266, 220), xy(x + 288, 220)], fill="#63758A", width=3 * scale)
            draw.line([xy(x + 280, 212), xy(x + 288, 220), xy(x + 280, 228)], fill="#63758A", width=3 * scale)
    draw.text(xy(40, 326), "阅读顺序：从左到右。先预测结果，再到模拟器里验证。", font=card_note_font, fill="#607086")
    canvas.save(OUT / f"{number:02d}.png", optimize=True)


def android_png(number, title, subtitle, steps):
    """A tall version stays legible in the phone's full-screen diagram dialog."""
    scale = 2
    canvas = Image.new("RGB", (800 * scale, 1050 * scale), "#F8FAFD")
    draw = ImageDraw.Draw(canvas)
    font_path = "/System/Library/Fonts/PingFang.ttc"
    title_font = ImageFont.truetype(font_path, 31 * scale)
    sub_font = ImageFont.truetype(font_path, 20 * scale)
    heading_font = ImageFont.truetype(font_path, 24 * scale)
    main_font = ImageFont.truetype(font_path, 23 * scale)
    note_font = ImageFont.truetype(font_path, 19 * scale)
    num_font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial.ttf", 20 * scale)

    def xy(x, y):
        return (x * scale, y * scale)

    draw.text(xy(38, 22), f"{number:02d}  {title}", font=title_font, fill="#202735")
    draw.text(xy(38, 73), subtitle, font=sub_font, fill="#536174")
    colors = ["#E8F0FE", "#E7F6EF", "#FFF1DB", "#F1EAFE"]
    borders = ["#4E74C8", "#288A63", "#B4771B", "#8260A5"]
    for i, (heading, main, note) in enumerate(steps):
        top = 130 + i * 225
        draw.rounded_rectangle((*xy(38, top), *xy(762, top + 180)), radius=20 * scale, fill=colors[i], outline=borders[i], width=2 * scale)
        draw.ellipse((*xy(58, top + 21), *xy(98, top + 61)), fill=borders[i])
        draw.text(xy(71, top + 24), str(i + 1), font=num_font, fill="white")
        draw.text(xy(115, top + 23), heading, font=heading_font, fill="#202735")
        draw.text(xy(62, top + 83), main, font=main_font, fill="#202735")
        draw.text(xy(62, top + 127), note, font=note_font, fill="#536174")
        if i < 3:
            draw.line([xy(400, top + 184), xy(400, top + 216)], fill="#63758A", width=3 * scale)
            draw.line([xy(392, top + 208), xy(400, top + 216), xy(408, top + 208)], fill="#63758A", width=3 * scale)
    draw.text(xy(38, 1024), "先预测，再运行代码验证。", font=note_font, fill="#607086")
    canvas.save(ANDROID_OUT / f"lesson_{number:02d}.png", optimize=True)


def code_to_screen():
    """Show the exact correspondence between two Text calls and two visible lines."""
    canvas = Image.new("RGB", (1200, 500), "#F8FAFD")
    draw = ImageDraw.Draw(canvas)
    font = "/System/Library/Fonts/PingFang.ttc"
    title = ImageFont.truetype(font, 32)
    label = ImageFont.truetype(font, 20)
    code = ImageFont.truetype(font, 27)
    draw.text((36, 24), "两次 Text 调用，怎样成为屏幕上的两行文字？", font=title, fill="#202735")
    draw.rounded_rectangle((36, 100, 665, 425), radius=18, fill="#FFFFFF", outline="#CFD9E7", width=2)
    draw.rounded_rectangle((785, 100, 1164, 425), radius=18, fill="#FFFFFF", outline="#CFD9E7", width=2)
    draw.text((60, 117), "Exercises.kt · 函数里面", font=label, fill="#536174")
    draw.text((810, 117), "练习区 · 排列示意", font=label, fill="#536174")
    draw.text((65, 170), "Column {", font=code, fill="#202735")
    for y, color, border, source, result, number in [
        (225, "#E8F0FE", "#4E74C8", 'Text("你好，Compose！")', "你好，Compose！", "第 1 行"),
        (305, "#E7F6EF", "#288A63", 'Text("我是小明")', "我是小明", "第 2 行"),
    ]:
        draw.rounded_rectangle((85, y - 8, 640, y + 42), radius=8, fill=color)
        draw.rounded_rectangle((808, y - 8, 1140, y + 42), radius=8, fill=color)
        draw.text((100, y), source, font=code, fill=border)
        draw.text((825, y), result, font=code, fill=border)
        draw.line((660, y + 15, 780, y + 15), fill=border, width=3)
        draw.line(((769, y + 7), (780, y + 15), (769, y + 23)), fill=border, width=3)
        draw.text((678, y - 21), number, font=label, fill=border)
    draw.text((65, 372), "}", font=code, fill="#202735")
    draw.text((36, 452), "Column 按代码顺序上下排列。交换两次 Text，屏幕上的顺序也会交换。", font=label, fill="#536174")
    canvas.save(OUT / "01-code-to-screen.png", optimize=True)


def first_lesson_comparisons():
    """Draw code comparisons with visible UI results, rather than just arrows."""
    font_path = "/System/Library/Fonts/PingFang.ttc"
    title = ImageFont.truetype(font_path, 34)
    heading = ImageFont.truetype(font_path, 27)
    body = ImageFont.truetype(font_path, 23)
    small = ImageFont.truetype(font_path, 20)
    code = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", 22)

    canvas = Image.new("RGB", (1400, 820), "#F2F5FA")
    draw = ImageDraw.Draw(canvas)
    draw.text((40, 28), "同一个问候区域：从 View 写法过渡到 Compose", font=title, fill="#202735")
    draw.text((40, 82), "用途对应 ≠ 内部实现相同。下面是代码与界面的教学示意。", font=small, fill="#536174")
    for left, label, color, lines in [
        (40, "你熟悉的 View", "#4E74C8", ["LinearLayout (vertical)", "    TextView", "    TextView", "", "setContentView(layout)", "nameView.text = value"]),
        (720, "新的 Compose 表达", "#288A63", ["setContent { Greeting() }", "", "@Composable", "fun Greeting() {", "    Column {", "        Text(...)", "        Text(...)", "    }", "}"]),
    ]:
        draw.rounded_rectangle((left, 140, left + 640, 485), radius=22, fill="white", outline="#CFD9E7", width=2)
        draw.text((left + 24, 157), label, font=heading, fill=color)
        for index, line in enumerate(lines):
            draw.text((left + 24, 208 + index * 28), line, font=code, fill="#202735")
    draw.text((40, 510), "两种写法都可以表达这个区域", font=heading, fill="#202735")
    draw.rounded_rectangle((40, 563, 655, 770), radius=20, fill="white", outline="#CFD9E7", width=2)
    draw.text((74, 599), "你好，Compose！", font=title, fill="#345A9C")
    draw.text((74, 666), "我是小明", font=heading, fill="#536174")
    draw.text((720, 576), "View：加载层级，拿对象，设属性", font=body, fill="#4E74C8")
    draw.text((720, 628), "Compose：调用组件，传值，描述内容", font=body, fill="#288A63")
    draw.text((720, 698), "排列交给容器；文字内容交给文字组件。", font=small, fill="#536174")
    canvas.save(OUT / "01-view-compose.png", optimize=True)

    canvas = Image.new("RGB", (1400, 700), "#F2F5FA")
    draw = ImageDraw.Draw(canvas)
    draw.text((40, 28), "两次 Text 不一定是两行：先看父容器", font=title, fill="#202735")
    draw.text((40, 83), "两侧使用同样的文字，没有添加间距；下方是局部布局示意。", font=small, fill="#536174")
    for left, container, color in [(40, "Column", "#4E74C8"), (720, "Row", "#288A63")]:
        draw.rounded_rectangle((left, 140, left + 640, 655), radius=22, fill="white", outline="#CFD9E7", width=2)
        draw.text((left + 28, 161), container + ("：竖排" if container == "Column" else "：横排"), font=heading, fill=color)
        for index, line in enumerate([container + " {", '    Text("你好")', '    Text("小明")', "}"]):
            draw.text((left + 28, 219 + index * 36), line, font=body, fill="#202735")
        draw.rounded_rectangle((left + 28, 405, left + 612, 587), radius=12, fill="#F2F5FA")
        draw.text((left + 52, 430), "你好", font=heading, fill=color)
        x = left + 52 if container == "Column" else left + 52 + int(draw.textlength("你好", font=heading))
        y = 466 if container == "Column" else 430
        draw.text((x, y), "小明", font=heading, fill=color)
        draw.text((left + 28, 610), "先预测 → 运行验证 → 解释容器的作用", font=small, fill="#536174")
    canvas.save(OUT / "01-layout-compare.png", optimize=True)


for number, title, subtitle, steps in DIAGRAMS:
    (OUT / f"{number:02d}.svg").write_text(svg(number, title, subtitle, steps), encoding="utf-8")
    png(number, title, subtitle, steps)
    android_png(number, title, subtitle, steps)

code_to_screen()
first_lesson_comparisons()
print(f"Generated {len(DIAGRAMS)} lesson diagrams for Markdown and Android")
