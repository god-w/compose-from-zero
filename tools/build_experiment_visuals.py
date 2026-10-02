"""Draw lesson-specific UI comparisons. These are teaching illustrations, not screenshots."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'assets/diagrams'
FONT = '/System/Library/Fonts/PingFang.ttc'
FONTS = {n: ImageFont.truetype(FONT, n) for n in (18, 21, 24, 28, 34, 64)}
# title, explanation, scenarios: heading, state note, visible contents, buttons, bottom explanation
SCENES = {
2: ('点击三次，什么能驱动文字更新？', '相同操作；先区分可观察状态与普通变量。', [
('普通 var', '没有建立状态通知', ['metric:0', '已点击 0 次'], ['+1'], '不能依赖点击后自动刷新'),
('remember + state', '可观察且保留同一容器', ['metric:3', '已点击 3 次'], ['+1', '清零'], '状态变化驱动相关内容更新')]),
3: ('输入事件是否被父组件接受？', 'value 来自同一份 name；图中显示事件处理后的结果。', [
('回调写回 name', 'name = proposed', ['field:小明', '你好，小明'], ['输入事件已接受'], '候选文本 → 状态 → 新 value'),
('空回调', 'onValueChange = { }', ['field:名字（仍为空）', '请输入名字'], ['事件未写回'], '键盘输入不会替你修改父状态')]),
4: ('相同文字，不同 Modifier 顺序', '蓝色是背景，浅灰色是外围；观察留白是否被上色。', [
('padding → background', '留白在背景外', ['padding:outside'], [], '先留白，内部区域才有背景'),
('background → padding', '背景包住留白', ['padding:inside'], [], '外层先上色，内部再留白')]),
5: ('删 A 后，B 移动了但还是 B', '稳定 key 帮助状态继续对应业务身份。', [
('删除前', 'B 的局部计数是 2', ['A · id 10 · 点击 0', 'B · id 20 · 点击 2', 'C · id 30 · 点击 0'], ['删除 A'], '位置：A 0、B 1、C 2'),
('删除后', 'key = task.id', ['B · id 20 · 点击 2', 'C · id 30 · 点击 0'], ['点 B'], 'B 变成位置 0，身份仍为 20')]),
6: ('进入、离开、再进入计时页', '返回课程首页会移除这部分组合；不是按 Home 的实验。', [
('停留在页面', '同一 effect 持续运行', ['metric:3 秒', 'session = 0'], ['重新开始'], 'seconds 改变不重启 effect'),
('退出后重新进入', '新的组合与新的 effect', ['metric:0 秒', 'remember 从初始值开始'], ['重新开始'], '旧任务已取消，新进入重新启动')]),
7: ('过滤改变可见内容，不改变任务总源', '统计来自完整 tasks；visible 只是一份推导结果。', [
('全部', '已完成 1 / 总共 2', ['✓ A · 已完成', '□ B · 未完成'], ['全部', '未完成'], '完整数据含 A 与 B'),
('未完成', '已完成 1 / 总共 2', ['□ B · 未完成'], ['全部', '未完成'], 'A 仍在数据里，只是不可见')]),
8: ('同一个界面，业务更新移到 ViewModel', 'TaskContent 只接收数据和事件，不再修改自己的 tasks。', [
('动作前', 'TaskUiState：0 / 1', ['□ A · 未完成', 'UI：发送 toggle(id)'], ['完成 A'], '事件上行到状态持有者'),
('动作后', '新的 TaskUiState：1 / 1', ['✓ A · 已完成', 'UI：读取最新 StateFlow'], ['取消完成'], '新快照下行到界面')]),
9: ('详情只保存查询身份', 'task/20 的路由不变；当前数据可能发生变化。', [
('任务存在', '按 id = 20 查询', ['任务 B', '状态：未完成'], ['删除当前任务', '返回'], '详情内容来自当前状态源'),
('任务已删除', '同一 id 查到 null', ['任务已不存在或 ID 无效'], ['返回'], '不继续显示传来的旧对象副本')]),
10: ('测试要区分正确结果与真实回归', '输入 A 后点击添加；不只断言按钮存在。', [
('正确添加', '期望 A 与 0 / 1', ['A', '已完成 0 / 总共 1', '断言：通过'], ['添加'], '结果和需求一致'),
('故意写坏', '实际添加了“错误”', ['错误', '已完成 0 / 总共 1', '断言：找不到 A'], ['添加'], '能失败的测试才抓得住回归')]),
11: ('放大字体，操作仍然需要可达', '教学示意：真实验收还需设备字体设置和 TalkBack。', [
('固定高度', '长标题被截断', ['这是很长的任务标题…', '状态只用颜色表达'], ['删除'], '读屏还缺明确任务对象'),
('允许换行与清晰语义', '任务与动作能被理解', ['这是很长的任务标题', '换行后内容仍可读', '读屏：删除任务 A'], ['删除'], '保留独立可访问操作')]),
12: ('一份 count 同时显示在两个世界', '先创建 View，再在 update 中应用当前数据。', [
('只有 factory 初值', '点击到 count = 3', ['Compose：3', 'View：0'], ['+1'], '旧 View 没收到后续属性更新'),
('factory + update', '点击到 count = 3', ['Compose：3', 'View：3'], ['+1'], '两个区域来自同一数据来源')]),
13: ('状态决定动画的目标', '图展示端点；中间过程需要运行 app 观察。', [
('expanded = false', '冷色背景，内容收起', ['任务练习卡'], ['展开'], '业务状态为收起'),
('expanded = true', '暖绿背景，内容展开', ['任务练习卡', '今天先做一个小练习', '再记录理解。'], ['收起'], '过渡最终听从当前 expanded')]),
14: ('拖动距离与完成状态分开', '向左短拖不完成；超过阈值后完成；松手位置归零。', [
('短拖并松手', '未达到 80dp 阈值', ['□ 任务 A · 未完成', '卡片位置已复位'], ['完成'], '不把取消或短拖当完成'),
('长拖并松手', '达到 80dp 阈值', ['✓ 任务 A · 已完成', '卡片位置已复位'], ['重置'], '按钮提供同等完成动作')]),
15: ('先检查进度端点，再讨论样式', '弧线是表现；数值与语义提供等价信息。', [
('零与半完成', 'Float 除法，零总数单独处理', ['ring:0', 'ring:0.5'], [], '0 / 10 → 0%；5 / 10 → 50%'),
('全部完成', '绘制值裁剪到 0..1', ['ring:1', '已完成 10 / 总共 10'], [], '100% 是满环，越界不多画')]),
16: ('强制停止后，任务记录从哪里回来？', '磁盘持久化与小型屏幕状态恢复是不同实验。', [
('只有内存列表', '重新启动后的默认状态', ['还没有任务'], ['添加'], '进程结束后内存记录消失'),
('Room 保存记录', '从数据库重新查询', ['✓ A · 已完成', '□ B · 未完成'], ['添加'], '长期任务与完成状态恢复')]),
17: ('空结果与失败，用户下一步不同', '不能用同一个空 List 隐藏加载过程与失败原因。', [
('Empty', '请求成功，但没有匹配项', ['当前条件下暂无任务'], ['换条件'], '不是网络或存储失败'),
('Error', '请求未成功完成', ['加载失败，请重试'], ['重试'], '取消旧请求不要转成错误')]),
18: ('窗口改变，选择不改变', '同一 selectedId；布局由当前空间推导。', [
('窄窗口', 'selectedId = 20', ['任务 B 的详情', '状态：未完成'], ['返回列表'], '一次展示一个区域'),
('宽窗口', 'selectedId 仍为 20', ['panes:列表 | 详情 B'], ['删除 B'], '列表和同一详情并排')]),
19: ('优化计算路径，保持可见行为', '这里没有实测数字；真实性能报告由你测量填写。', [
('每次重新排序', '无关计数变化也计算', ['任务 A', '任务 B', 'sortedBy(tasks)'], ['无关更新 3'], '基线：记录真实耗时与波动'),
('按 tasks 记忆结果', '输入不变时复用排序', ['任务 A', '任务 B', 'remember(tasks)'], ['无关更新 3'], '复测：没有收益也可以撤回')]),
20: ('把知识迁移到每日习惯', '每日记录按 habitId + day 区分，目标达到后不再增加。', [
('今日未打卡', '阅读 · 每日目标 2', ['ring:0', '今日 0 / 2'], ['打卡'], '先明确今天的日期策略'),
('完成一次打卡', '同一习惯、同一日期', ['ring:0.5', '今日 1 / 2'], ['再次打卡'], '数据库更新后 UI 读到新结果')]),
}


def wrap(draw, text, width, font):
    lines, line = [], ''
    for char in text:
        if line and draw.textlength(line + char, font=font) > width:
            lines.append(line); line = char
        else:
            line += char
    return lines + ([line] if line else [])


def render(n, title, subtitle, scenes):
    im = Image.new('RGB', (1400, 840), '#F2F5FA')
    d = ImageDraw.Draw(im)
    d.text((38, 24), f'{n:02d}  {title}', font=FONTS[34], fill='#202735')
    d.text((38, 82), subtitle, font=FONTS[21], fill='#536174')
    for i, (heading, state, contents, buttons, footer) in enumerate(scenes):
        x = 38 + i * 684
        accent = '#4169AD' if i == 0 else '#23825F'
        d.rounded_rectangle((x, 137, x + 640, 755), radius=24, fill='white', outline='#CCD7E6', width=2)
        d.text((x + 26, 157), heading, font=FONTS[28], fill=accent)
        d.text((x + 26, 201), state, font=FONTS[21], fill='#536174')
        d.rounded_rectangle((x + 26, 254, x + 614, 617), radius=20, fill='#F7F9FD' if n != 13 or i == 0 else '#E1F3E8')
        y = 282
        for content in contents:
            kind, sep, val = content.partition(':')
            if kind == 'metric':
                d.text((x + 54, y), val, font=FONTS[64], fill=accent); y += 100
            elif kind == 'field':
                d.rounded_rectangle((x + 50, y, x + 590, y + 72), radius=9, outline=accent, width=2)
                d.text((x + 68, y + 18), val, font=FONTS[24], fill=accent); y += 100
            elif kind == 'padding':
                outer = '#E1EAFE' if val == 'inside' else '#E8EDF5'
                d.rectangle((x + 60, y + 12, x + 580, y + 245), fill=outer)
                d.rectangle((x + 120, y + 65, x + 520, y + 190), fill='#BCD2F5')
                d.text((x + 185, y + 105), '同一段文字', font=FONTS[28], fill='#27466E')
                d.text((x + 62, y + 260), '外围留白是否有背景？', font=FONTS[21], fill='#536174'); y += 320
            elif kind == 'ring':
                p = float(val); box = (x + 70, y, x + 170, y + 100)
                d.arc(box, 0, 360, fill='#D5DEEB', width=10)
                if p: d.arc(box, -90, -90 + 360*p, fill=accent, width=10)
                d.text((x + 197, y + 31), f'{round(p*100)}%', font=FONTS[34], fill=accent); y += 132
            elif kind == 'panes':
                d.rounded_rectangle((x + 48, y, x + 270, y + 210), radius=12, fill='#E8EEFA')
                d.rounded_rectangle((x + 285, y, x + 590, y + 210), radius=12, fill='#E1F3E8')
                for dx, dy, text in [(65, 20, '列表'), (65, 76, 'A'), (65, 125, 'B · 已选'), (304, 20, '详情 B'), (304, 85, '未完成')]:
                    d.text((x + dx, y + dy), text, font=FONTS[24], fill=accent)
                y += 230
            else:
                for line in wrap(d, content, 530, FONTS[24]):
                    d.text((x + 52, y), line, font=FONTS[24], fill='#202735'); y += 40
                y += 12
        bx = x + 30
        for label in buttons:
            width = int(d.textlength(label, font=FONTS[21])) + 30
            d.rounded_rectangle((bx, 642, bx + width, 687), radius=20, fill=accent)
            d.text((bx + 15, 651), label, font=FONTS[21], fill='white'); bx += width + 12
        for j, line in enumerate(wrap(d, footer, 580, FONTS[18])):
            d.text((x + 28, 704 + j*25), line, font=FONTS[18], fill='#536174')
    d.text((38, 786), '教学示意图 · 先预测，再运行验证；不是模拟器截图或实测报告。', font=FONTS[21], fill='#536174')
    im.save(OUT / f'{n:02d}-experiment.png', optimize=True)


if __name__ == '__main__':
    OUT.mkdir(parents=True, exist_ok=True)
    for n, data in SCENES.items(): render(n, *data)
    print(f'Generated {len(SCENES)} UI comparison illustrations')
