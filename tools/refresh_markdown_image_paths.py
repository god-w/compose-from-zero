"""Switch every repository image in Markdown between local and GitHub paths."""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
assets = root / 'assets'
github = 'https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/'
if sys.argv[1:] not in ([], ['--github']):
    raise SystemExit('Usage: python3 tools/refresh_markdown_image_paths.py [--github]')
use_github = sys.argv[1:] == ['--github']


def replace_asset(match):
    label, target = match.groups()
    if target.startswith(github):
        relative = target[len(github):]
    elif target.startswith(str(assets) + '/'):
        relative = target[len(str(assets)) + 1:]
    else:
        return match.group(0)
    local = assets / relative
    if not local.is_file() or not local.resolve().is_relative_to(assets.resolve()):
        raise FileNotFoundError(local)
    url = github + relative if use_github else str(local)
    return f'![{label}]({url})'


for document in [root / 'README.md', *sorted((root / 'lessons').glob('*.md'))]:
    text = document.read_text(encoding='utf-8')
    text = re.sub(r'!\[([^\]]*)\]\(([^)]+)\)', replace_asset, text)
    document.write_text(text, encoding='utf-8')
print('Updated every Markdown asset path for ' + ('GitHub' if use_github else 'this computer'))
