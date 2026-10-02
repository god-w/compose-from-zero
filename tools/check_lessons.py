"""Check local lesson navigation, image assets and teaching gates without network access."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
GITHUB = 'https://raw.githubusercontent.com/god-w/compose-from-zero/main/'
lessons = sorted((ROOT / 'lessons').glob('[0-9][0-9]-*.md'))
assert [int(p.name[:2]) for p in lessons] == list(range(1, 21))
image_count = 0
for path in [ROOT / 'README.md', *sorted((ROOT / 'lessons').glob('*.md'))]:
    text = path.read_text(encoding='utf-8')
    assert text.count('```') % 2 == 0, f'Unclosed code block: {path}'
    assert text.count('<details>') == text.count('</details>'), f'Unclosed answer: {path}'
    for image, label, target in re.findall(r'(!?)\[([^\]]*)\]\(([^)]+)\)', text):
        if target.startswith(GITHUB):
            local = ROOT / target[len(GITHUB):]
        elif target.startswith(('https://', 'http://', '#')):
            continue
        elif target.startswith('/'):
            local = Path(target)
        else:
            local = path.parent / target.split('#')[0]
        assert local.is_file(), f'Missing target in {path.name}: {target}'
        image_count += bool(image)
    if path in lessons:
        n = path.name[:2]
        assert f'第 {n} 课概念图' in text
        assert f'第 {n} 课完成后的目标界面示意' in text
        assert '检查' in text and ('验收' in text or '完成标准' in text)
        assert '<details>' in text, f'Missing explained answer: {path}'
        if n != '01':
            assert f'第 {n} 课实验界面对照' in text
            assert '```kotlin' in text
print(f'PASS: {len(lessons)} lessons, {image_count} image references, local links and answer blocks')
