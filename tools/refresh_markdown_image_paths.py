"""Switch Markdown image paths between local IDE previews and GitHub URLs."""

from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
github = 'https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets'
if len(sys.argv) != 1 and sys.argv != [sys.argv[0], '--github']:
    raise SystemExit('Usage: python3 tools/refresh_markdown_image_paths.py [--github]')
use_github = len(sys.argv) == 2


def replace_image(text, label, folder, number):
    local = root / 'assets' / folder / f'{number}.png'
    if not local.is_file():
        raise FileNotFoundError(local)
    url = f'{github}/{folder}/{number}.png' if use_github else str(local)
    pattern = r'(!\[' + re.escape(label) + r'\]\()[^)]*(\))'
    updated, count = re.subn(pattern, lambda m: m.group(1) + url + m.group(2), text, count=1)
    if count != 1:
        raise ValueError(f'Expected one image: {label}')
    return updated


for lesson in sorted((root / 'lessons').glob('[0-9][0-9]-*.md')):
    n = lesson.name[:2]
    text = lesson.read_text(encoding='utf-8')
    text = replace_image(text, f'第 {n} 课概念图', 'diagrams', n)
    text = replace_image(text, f'第 {n} 课完成后的目标界面示意', 'effects', n)
    if n == '01':
        text = replace_image(text, 'Column 中两次 Text 调用与两行文字的对应关系', 'diagrams', '01-code-to-screen')
        text = replace_image(text, 'View 与 Compose 描述同一个问候区域', 'diagrams', '01-view-compose')
        text = replace_image(text, 'Column 和 Row 对同样两段文字的排列', 'diagrams', '01-layout-compare')
    lesson.write_text(text, encoding='utf-8')

readme = root / 'README.md'
text = readme.read_text(encoding='utf-8')
text = replace_image(text, '第一课从 Activity 到练习区的调用链图解', 'diagrams', '01')
text = replace_image(text, '第 07 课任务清单的目标界面示意', 'effects', '07')
readme.write_text(text, encoding='utf-8')
print('Updated Markdown image paths for ' + ('GitHub' if use_github else 'this computer'))
