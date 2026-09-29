"""Draw target UI previews for the 20 Compose lessons (illustrations, not screenshots)."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'assets' / 'effects'
OUT.mkdir(parents=True, exist_ok=True)
FONT = '/System/Library/Fonts/PingFang.ttc'

# title, screen title, scenario, observation, code bridge, style
DATA = [
 ('第一个 Composable','欢迎页','把传入的名字放进欢迎卡片','改 name 后，哪一行会更新？','GreetingExercise(name)','welcome'),
 ('状态与重组','习惯计数','连续点击三次，数字来到 3','按钮改的是状态，还是 Text？','rememberSaveable + Button','counter'),
 ('输入与单向数据流','个人信息','输入“小雨”，下方实时预览','输入值由谁保存？','TextField(value, onValueChange)','input'),
 ('布局与 Modifier','个人资料','头像、姓名和标签排成一张卡','先看 Row，再看内部 Column','Row + Column + Modifier','profile'),
 ('列表与 key','今日计划','删除第一项后，其他任务仍保持身份','B 变成第一项，id 变了吗？','LazyColumn(items, key)','list'),
 ('副作用','专注计时','页面可见时秒数持续增加','离开页面后计时会怎样？','LaunchedEffect','timer'),
 ('阶段项目','我的任务','添加、勾选、筛选一条任务','总数和已完成数来自哪里？','State + LazyColumn','tasks'),
 ('状态持有者','今日任务','状态持有者更新后，界面同步刷新','点击事件交给哪一层？','ViewModel + StateFlow','tasks'),
 ('导航与返回栈','任务详情','从任务列表打开选中的详情','详情页只需要传什么？','NavHost + taskId','detail'),
 ('UI 测试','行为测试','输入空格再添加，任务数仍是 0','这条断言能抓住什么回归？','Compose UI test','test'),
 ('质量与无障碍','无障碍任务行','大字号下仍看得清、点得到','读屏会如何描述完成状态？','semantics + fontScale','access'),
 ('View 互操作','渐进式迁移','旧页面中嵌入新的 Compose 卡片','哪一块仍由 View 管理？','ComposeView / AndroidView','interop'),
 ('状态驱动动画','展开任务','点击后卡片平滑展开说明','快速连点时最终状态是什么？','AnimatedVisibility','expanded'),
 ('手势与交互冲突','滑动操作','向左滑任务露出操作按钮','点按与拖动如何区分？','clickable + pointerInput','gesture'),
 ('自定义绘制与布局','学习进度','用 Canvas 画完成进度环','屏幕阅读器知道 75% 吗？','Canvas + semantics','progress'),
 ('状态恢复与持久化','重启后的任务','旋转或重启后任务仍在','哪一种状态需要长期保存？','rememberSaveable + Room','restore'),
 ('异步数据','同步任务','加载、空、成功、错误各有画面','失败和空列表的建议一样吗？','Loading / Empty / Error','async'),
 ('自适应布局','宽屏任务','平板左侧列表、右侧详情','窗口变宽后选中项去哪了？','WindowSizeClass','tablet'),
 ('性能实战','滚动分析','记录优化前后的帧耗时','先测量哪个可复现场景？','Macrobenchmark + tracing','perf'),
 ('综合毕业项目','完整任务空间','任务、统计、详情与恢复闭环','你能拿出哪些验收证据？','测试 + 无障碍 + 交付','mastery'),
]

INK='#18243B'; MUTED='#66748A'; BLUE='#4263E8'; PURPLE='#7A55DC'; BG='#EEF3FF'; WHITE='#FFFFFF'; GREEN='#178A68'; BORDER='#DDE5F2'

def font(size, bold=False):
    return ImageFont.truetype(FONT, size, index=0)

def rr(d, box, fill=WHITE, radius=22, outline=None, width=1):
    d.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)

def txt(d, xy, value, size=24, color=INK):
    d.text(xy, value, font=font(size), fill=color)

def line(d, xy1, xy2, color=BORDER, width=2):
    d.line([xy1,xy2], fill=color, width=width)

def wrap(s, n):
    return [s[i:i+n] for i in range(0,len(s),n)]

def pill(d, x,y,label,fill='#E9EEFF',fg=BLUE,w=None):
    w=w or max(88, len(label)*22+32)
    rr(d,(x,y,x+w,y+43),fill,21)
    txt(d,(x+16,y+8),label,18,fg)
    return w

def card(d,y,h=90, x=77,w=397,fill=WHITE):
    rr(d,(x,y,x+w,y+h),fill,22)

def row(d,y,label,sub='',checked=False,accent=BLUE):
    card(d,y,78)
    rr(d,(97,y+23,127,y+53),accent if checked else WHITE,10,accent,3)
    if checked: txt(d,(103,y+20),'✓',23,WHITE)
    txt(d,(145,y+13),label,23,INK)
    if sub: txt(d,(145,y+44),sub,16,MUTED)

def phone_ui(d,n,kind,title):
    # Phone viewport: 57..493 × 47..710.
    rr(d,(57,47,493,713),'#182744',43)
    rr(d,(65,56,485,705),'#F7F9FF',36)
    rr(d,(214,63,336,83),'#182744',12)
    txt(d,(87,105),'9:41',16,INK)
    txt(d,(399,105),'●  ▰',16,INK)
    txt(d,(86,151),title,32,INK)
    txt(d,(87,194),'COMPOSE LAB  /  '+f'{n:02d}',15,PURPLE)
    if kind=='welcome':
        rr(d,(78,250,472,553),'#DDE8FF',30)
        d.ellipse((208,280,340,412),fill='#A8BDFB')
        txt(d,(253,313),'✦',46,WHITE)
        txt(d,(110,430),'你好，Compose！',32,INK)
        txt(d,(113,487),'我是小雨，今天开始创作。',20,MUTED)
        pill(d,124,590,'继续学习',BLUE,WHITE,290)
    elif kind=='counter':
        rr(d,(78,248,472,554),'#E8E5FF',30)
        txt(d,(123,286),'今天已经完成',23,PURPLE)
        txt(d,(226,337),'3',92,INK)
        txt(d,(216,465),'次练习',24,MUTED)
        pill(d,112,593,'−  减少',WHITE,INK,145); pill(d,278,593,'＋  增加',BLUE,WHITE,145)
    elif kind=='input':
        txt(d,(88,260),'你的名字',21,MUTED)
        rr(d,(80,302,470,380),WHITE,18,BLUE,3); txt(d,(101,321),'小雨',27,INK)
        rr(d,(80,421,470,578),'#E8EFFF',25)
        txt(d,(105,451),'实时预览',18,BLUE); txt(d,(104,505),'你好，小雨 👋',32,INK)
        txt(d,(91,615),'每输入一个字，预览立即改变',18,MUTED)
    elif kind=='profile':
        rr(d,(78,250,472,594),WHITE,27)
        d.ellipse((110,284,225,399),fill='#B7C5F9')
        txt(d,(143,310),'雨',42,BLUE)
        txt(d,(244,291),'小雨',30,INK); txt(d,(244,339),'Android 开发者',18,MUTED)
        line(d,(104,426),(447,426))
        pill(d,104,450,'Jetpack Compose','#EBE7FF',PURPLE,236)
        txt(d,(107,526),'让界面跟着状态变化。',20,INK)
    elif kind=='timer':
        d.arc((130,260,421,551),20,320,fill=BLUE,width=20)
        txt(d,(200,343),'00:18',53,INK)
        txt(d,(189,421),'专注进行中',19,MUTED)
        pill(d,113,593,'暂停',WHITE,INK,146); pill(d,278,593,'完成',BLUE,WHITE,146)
    elif kind=='detail':
        pill(d,84,242,'← 返回列表',WHITE,BLUE,180)
        rr(d,(79,310,471,607),WHITE,24)
        txt(d,(105,343),'准备 Compose 分享',27,INK)
        txt(d,(105,395),'今天 · 18:00',18,MUTED)
        line(d,(105,441),(444,441))
        txt(d,(105,468),'说明',18,PURPLE)
        txt(d,(105,506),'整理状态提升的例子',21,INK)
        pill(d,105,551,'✓ 标记完成','#DEF5E9',GREEN,229)
    elif kind=='test':
        rr(d,(80,244,471,331),'#E1F7E9',20)
        txt(d,(103,261),'✓  3 个测试已通过',25,GREEN)
        txt(d,(89,368),'输入空格',21,INK); pill(d,326,355,'添加',BLUE,WHITE,118)
        rr(d,(82,416,469,510),WHITE,18)
        txt(d,(105,438),'任务数量',18,MUTED); txt(d,(401,430),'0',37,INK)
        rr(d,(82,543,469,648),'#E9EEFF',18)
        txt(d,(103,565),'断言：列表保持为空',21,BLUE)
    elif kind=='access':
        row(d,260,'整理学习笔记','已完成 · 可双击切换',True)
        rr(d,(80,381,471,519),'#E9EEFF',22)
        txt(d,(107,403),'读屏提示',18,BLUE)
        txt(d,(107,446),'“整理学习笔记，已完成”',20,INK)
        txt(d,(91,581),'大字号下按钮仍完整可见',19,MUTED)
    elif kind=='interop':
        rr(d,(80,245,470,346),'#E8EDF5',20)
        txt(d,(103,267),'旧版 View 页面',22,INK)
        txt(d,(103,306),'原有导航与工具栏继续工作',17,MUTED)
        rr(d,(80,375,470,568),'#E9E6FF',22)
        txt(d,(105,397),'✦  Compose 新卡片',23,PURPLE)
        txt(d,(105,454),'任务完成率',19,MUTED); txt(d,(105,489),'75%',48,INK)
    elif kind=='expanded':
        row(d,249,'准备 Compose 分享','点击卡片后展开',False)
        rr(d,(80,347,470,568),'#ECE9FF',22)
        txt(d,(106,376),'任务说明  ⌃',23,PURPLE)
        txt(d,(106,427),'1. 整理状态提升示例',19,INK)
        txt(d,(106,474),'2. 做一次现场演示',19,INK)
        pill(d,107,519,'设为已完成',BLUE,WHITE,228)
    elif kind=='gesture':
        row(d,256,'读完 Compose 文档','向左拖动这张任务卡')
        rr(d,(316,365,470,451),'#FCE4E4',18)
        txt(d,(348,385),'删除',23,'#C64D4D')
        rr(d,(80,365,402,451),WHITE,18)
        txt(d,(107,388),'准备分享资料  ←',21,INK)
        txt(d,(91,533),'也可以点击按钮完成相同操作',19,MUTED)
    elif kind=='progress':
        d.arc((123,256,433,566),0,360,fill='#DDE6FA',width=24)
        d.arc((123,256,433,566),-90,180,fill=BLUE,width=24)
        txt(d,(206,358),'75%',56,INK)
        txt(d,(194,426),'本周完成',20,MUTED)
        pill(d,122,605,'已完成 6 / 8','#E4F5EB',GREEN,305)
    elif kind=='restore':
        rr(d,(80,242,470,332),'#E3F5E9',20)
        txt(d,(102,266),'✓  已从本地恢复',23,GREEN)
        row(d,362,'写 Compose 练习','昨天保存',True)
        row(d,454,'准备分享资料','今天继续',False)
        txt(d,(92,609),'关闭应用后再次打开，数据还在',18,MUTED)
    elif kind=='async':
        rr(d,(80,244,470,488),'#FFF0DE',24)
        txt(d,(108,276),'!  同步暂时失败',27,'#B27021')
        txt(d,(108,341),'检查网络后可以重试，',20,INK)
        txt(d,(108,379),'本地任务仍可查看。',20,INK)
        pill(d,107,427,'重试同步','#B27021',WHITE,192)
        txt(d,(92,561),'错误状态有明确下一步',19,MUTED)
    elif kind=='perf':
        rr(d,(80,242,470,536),WHITE,23)
        txt(d,(103,263),'任务列表滚动',22,INK)
        txt(d,(102,323),'优化前',19,MUTED); txt(d,(356,316),'24 ms',27,'#C45A5A')
        rr(d,(105,365,420,384),'#F9D7D7',10)
        txt(d,(102,418),'优化后',19,MUTED); txt(d,(356,411),'13 ms',27,GREEN)
        rr(d,(105,461,275,480),'#C6EEDD',10)
        txt(d,(91,581),'同一设备 · 同一场景 · 重复测量',18,MUTED)
    elif kind=='tablet':
        # In-phone teaser; full wide layout shown separately by inset.
        row(d,262,'准备 Compose 分享','已选中',True)
        row(d,355,'整理学习笔记','待完成')
        rr(d,(80,469,470,594),'#E9EEFF',22)
        txt(d,(105,488),'宽屏时',18,BLUE)
        txt(d,(105,529),'列表 + 详情并排',25,INK)
    elif kind=='mastery':
        rr(d,(79,241,472,363),'#E7EBFF',23)
        txt(d,(105,262),'本周完成',19,PURPLE); txt(d,(105,302),'8 / 12',42,INK)
        row(d,391,'写完状态管理笔记','已完成',True)
        row(d,484,'实现离线任务清单','正在进行')
        pill(d,91,605,'＋ 新建任务',BLUE,WHITE,365)
    else: # list, tasks
        if kind=='tasks':
            rr(d,(80,236,470,319),'#E5EAFF',18)
            txt(d,(101,255),'今日完成  2 / 3',24,BLUE)
            pill(d,88,337,'全部',BLUE,WHITE,103); pill(d,200,337,'待办',WHITE,INK,103); pill(d,312,337,'完成',WHITE,INK,103)
            row(d,403,'阅读 Compose 文档','已完成',True)
            row(d,495,'写一个状态练习','待完成')
        else:
            row(d,255,'A · 阅读教程','id = 101',True)
            row(d,347,'B · 写练习','id = 102')
            row(d,439,'C · 做验证','id = 103')
            pill(d,100,580,'＋ 添加任务',BLUE,WHITE,350)
    # navigation indicator
    rr(d,(214,680,336,687),'#25385B',4)

def tablet_ui(d):
    rr(d,(29,147,530,618),'#182744',27)
    rr(d,(39,157,520,608),'#F7F9FF',21)
    txt(d,(59,177),'我的任务',28,INK)
    txt(d,(364,184),'宽屏模式',16,PURPLE)
    line(d,(55,226),(504,226))
    rr(d,(55,247,249,360),'#E8EEFF',15)
    txt(d,(69,266),'准备 Compose 分享',17,BLUE)
    txt(d,(69,313),'已选中',14,PURPLE)
    rr(d,(55,373,249,470),WHITE,15)
    txt(d,(69,394),'整理学习笔记',17,INK)
    txt(d,(69,431),'待完成',14,MUTED)
    line(d,(263,249),(263,577))
    txt(d,(281,260),'任务详情',20,INK)
    txt(d,(281,315),'准备 Compose 分享',18,BLUE)
    for y,t in [(364,'今天 · 18:00'),(405,'整理状态提升示例'),(446,'做一次现场演示')]:
        txt(d,(281,y),t,15,MUTED if y==364 else INK)
    rr(d,(280,507,487,559),'#DDF4E7',15)
    txt(d,(294,519),'✓  标记完成',17,GREEN)
    txt(d,(59,640),'选中任务保持不变，布局随窗口宽度改变',17,MUTED)


def make(n, data):
    title,screen,scenario,question,bridge,kind=data
    im=Image.new('RGB',(1200,760),'#EEF3FF'); d=ImageDraw.Draw(im)
    # Decorative accent shapes and white editorial card.
    d.ellipse((1015,-105,1300,180),fill='#DDE4FF')
    d.ellipse((-120,615,150,885),fill='#E1E9FF')
    rr(d,(545,47,1150,713),WHITE,34)
    tablet_ui(d) if n==18 else phone_ui(d,n,kind,screen)
    pill(d,586,91,f'第 {n:02d} 课 · 目标效果示意','#E9EEFF',BLUE,279)
    txt(d,(586,165),title,43,INK)
    line(d,(586,236),(1108,236))
    txt(d,(586,269),'做完后，你会看到',21,PURPLE)
    for i,l in enumerate(wrap(scenario,18)):
        txt(d,(586,312+i*40),l,29,INK)
    rr(d,(585,428,1111,548),'#F3F0FF',23)
    txt(d,(609,447),'先观察，再动手',20,PURPLE)
    for i,l in enumerate(wrap(question,22)):
        txt(d,(609,485+i*30),l,21,INK)
    txt(d,(587,586),'代码入口',19,MUTED)
    rr(d,(585,625,1111,680),'#EAF0FF',15)
    txt(d,(603,635),bridge,22,BLUE)
    im.save(OUT/f'{n:02d}.png',optimize=True)

for number, data in enumerate(DATA,1):
    make(number,data)
print(f'Created {len(DATA)} target UI previews in {OUT}')
