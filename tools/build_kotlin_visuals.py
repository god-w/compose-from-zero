"""Render Kotlin teaching panels from editable scene data; these are not screenshots."""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'assets/kotlin'
FONT = '/System/Library/Fonts/PingFang.ttc'
fonts = {size: ImageFont.truetype(FONT, size) for size in (24, 28, 32, 44)}

def wrapped(draw, text, font, width):
    lines, line = [], ''
    for char in text:
        if line and draw.textlength(line + char, font=font) > width:
            lines.append(line)
            line = char
        else:
            line += char
    return lines + [line]

for scene in json.loads((OUT / 'scenes.json').read_text()):
    img = Image.new('RGB', (1440, 1120), '#f3f5fc')
    draw = ImageDraw.Draw(img)
    draw.rounded_rectangle((48, 42, 1392, 1078), radius=30, fill='#ffffff')
    draw.rounded_rectangle((80, 76, 216, 138), radius=16, fill='#3348a4')
    draw.text((101, 85), scene['id'], font=fonts[32], fill='white')
    draw.text((240, 78), scene['title'], font=fonts[44], fill='#172448')
    draw.text((88, 167), '先判断值与调用时机，再预测输出', font=fonts[28], fill='#566581')
    for i, text in enumerate(scene['lines']):
        y = 240 + i * 121
        draw.rounded_rectangle((86, y, 1354, y + 98), radius=16,
                               fill='#edf1ff' if i < 4 else '#e3f5ec')
        draw.ellipse((108, y+25, 153, y+70), fill='#3348a4' if i < 4 else '#21774b')
        draw.text((120, y+28), str(i+1), font=fonts[24], fill='white')
        for j, line in enumerate(wrapped(draw, text, fonts[28], 1120)):
            draw.text((181, y+17+j*36), line, font=fonts[28], fill='#172448')
    draw.text((90, 878), '点击 App「运行参考示范」后，对照实际返回值', font=fonts[24], fill='#566581')
    for j, line in enumerate(wrapped(draw, scene['output'], fonts[28], 1240)):
        draw.text((90, 928+j*42), line, font=fonts[28], fill='#21774b')
    draw.text((90, 1032), '教学图解 · 并非模拟器截图 · 完成后再做一组不同输入', font=fonts[24], fill='#566581')
    img.save(OUT / f"{scene['id']}.png")
print('Rendered 14 Kotlin teaching panels')
