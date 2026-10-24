"""Generate high-fidelity UI design mockups for the NovaChat / 星语 AI Android app.

Outputs PNG design boards into /workspace/design.
"""
import os
import math
from PIL import Image, ImageDraw, ImageFilter, ImageFont

OUT = os.path.dirname(os.path.abspath(__file__))
FONT_PATH = "/workspace/tools/fonts/NotoSansSC.ttf"
S = 2                       # 2x density rendering
W, H = 390 * S, 844 * S

# ---------------- design tokens ----------------
BG_TOP = (9, 9, 20)
BG_BOT = (20, 16, 44)
SURFACE = (26, 24, 48)
STROKE = (52, 48, 88)
PRIMARY = (124, 92, 255)
PRIMARY_2 = (59, 130, 246)
ACCENT = (34, 211, 238)
TEXT = (242, 241, 250)
TEXT_2 = (154, 152, 184)
TEXT_3 = (110, 107, 140)
WHITE = (255, 255, 255)
BUBBLE_AI = (30, 28, 54)
GREEN = (52, 211, 153)


def font(size, weight=400):
    f = ImageFont.truetype(FONT_PATH, int(size * S))
    try:
        f.set_variation_by_axes([weight])
    except Exception:
        pass
    return f


def linear(size, top, bottom, horizontal=False):
    w, h = (8, size[1]) if horizontal else (size[0], 8)
    img = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(img)
    n = w if horizontal else h
    for i in range(n):
        t = i / max(1, n - 1)
        c = tuple(int(top[k] + (bottom[k] - top[k]) * t) for k in range(3))
        if horizontal:
            d.line([(i, 0), (i, h)], fill=c)
        else:
            d.line([(0, i), (w, i)], fill=c)
    return img.resize(size, Image.BICUBIC)


def gradient_circle(diam, c1, c2, c3=None):
    img = Image.new("RGB", (diam, diam))
    d = ImageDraw.Draw(img)
    for y in range(diam):
        t = y / max(1, diam - 1)
        c = tuple(int(c1[k] + (c2[k] - c1[k]) * t) for k in range(3))
        d.line([(0, y), (diam, y)], fill=c)
    if c3:
        over = Image.new("RGB", (diam, diam))
        od = ImageDraw.Draw(over)
        for y in range(diam):
            t = y / max(1, diam - 1)
            c = tuple(int(c2[k] + (c3[k] - c2[k]) * t) for k in range(3))
            od.line([(0, y), (diam, y)], fill=c)
        mask = Image.new("L", (diam, diam), 0)
        ImageDraw.Draw(mask).polygon([(0, 0), (diam, 0), (diam, diam)], fill=255)
        img.paste(over, (0, 0), mask)
    mask = Image.new("L", (diam, diam), 0)
    ImageDraw.Draw(mask).ellipse([0, 0, diam - 1, diam - 1], fill=255)
    out = Image.new("RGBA", (diam, diam), (0, 0, 0, 0))
    out.paste(img, (0, 0), mask)
    return out


def glow(base, center, radius, color, alpha=90, blur=40):
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.ellipse([center[0] - radius, center[1] - radius,
               center[0] + radius, center[1] + radius],
              fill=color + (alpha,))
    layer = layer.filter(ImageFilter.GaussianBlur(blur))
    base.alpha_composite(layer)


def mix(c, bg, a):
    """Pre-blend an alpha color over a solid background (avoids PNG alpha artefacts)."""
    return tuple(int(bg[k] + (c[k] - bg[k]) * a) for k in range(3))


def rrect(base, box, radius, fill=None, outline=None, width=1):
    ImageDraw.Draw(base).rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def grad_rrect(base, box, radius, c1, c2, horizontal=False):
    w = int(box[2] - box[0])
    h = int(box[3] - box[1])
    g = linear((w, h), c1, c2, horizontal).convert("RGBA")
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, w - 1, h - 1], radius=radius, fill=255)
    base.paste(g, (int(box[0]), int(box[1])), mask)


def text(base, xy, s, size=14, weight=400, color=TEXT, anchor="la"):
    ImageDraw.Draw(base).text(xy, s, font=font(size, weight), fill=color, anchor=anchor)


def tw(base, s, size, weight=400):
    return ImageDraw.Draw(base).textlength(s, font=font(size, weight))



def phone_frame(screen):
    pad = 18 * S
    frame = Image.new("RGBA", (screen.width + pad * 2, screen.height + pad * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(frame)
    d.rounded_rectangle([0, 0, frame.width - 1, frame.height - 1], radius=44 * S, fill=(16, 15, 26, 255))
    d.rounded_rectangle([3 * S, 3 * S, frame.width - 3 * S, frame.height - 3 * S],
                        radius=41 * S, outline=(70, 66, 104, 255), width=2 * S)
    mask = Image.new("L", screen.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, screen.width - 1, screen.height - 1],
                                           radius=36 * S, fill=255)
    frame.paste(screen, (pad, pad), mask)
    return frame


def status_bar(img, color=TEXT):
    text(img, (28 * S, 16 * S), "9:41", 13, 600, color)
    d = ImageDraw.Draw(img)
    x = W - 86 * S
    for i, hh in enumerate([4, 6, 8, 10]):
        d.rounded_rectangle([x + i * 5 * S, 22 * S - hh * S, x + i * 5 * S + 3 * S, 22 * S],
                            radius=1 * S, fill=color)
    wx = W - 60 * S
    for r in (9, 6, 3):
        d.arc([wx - r * S, 24 * S - r * S, wx + r * S, 24 * S + r * S], 200, 340,
              fill=color, width=int(1.6 * S))
    bx = W - 44 * S
    d.rounded_rectangle([bx, 13 * S, bx + 22 * S, 26 * S], radius=4 * S, outline=color, width=int(1.4 * S))
    d.rounded_rectangle([bx + 2 * S, 15 * S, bx + 17 * S, 24 * S], radius=2 * S, fill=color)
    d.rounded_rectangle([bx + 23 * S, 17 * S, bx + 25 * S, 22 * S], radius=1 * S, fill=color)


def nav_bar(img):
    ImageDraw.Draw(img).rounded_rectangle(
        [W // 2 - 34 * S, H - 9 * S, W // 2 + 34 * S, H - 5 * S], radius=3 * S, fill=(228, 227, 240))


def base_screen():
    img = linear((W, H), BG_TOP, BG_BOT).convert("RGBA")
    glow(img, (int(W * 0.85), int(H * 0.02)), 190 * S, PRIMARY, 70, 90)
    glow(img, (int(W * 0.05), int(H * 0.55)), 160 * S, PRIMARY_2, 45, 100)
    return img


def icon_gear(img, cx, cy, r, color=TEXT_2):
    d = ImageDraw.Draw(img)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=color, width=int(1.8 * S))
    d.ellipse([cx - r * 0.38, cy - r * 0.38, cx + r * 0.38, cy + r * 0.38], outline=color, width=int(1.8 * S))
    for i in range(8):
        a = math.pi / 4 * i
        d.line([cx + math.cos(a) * r, cy + math.sin(a) * r,
                cx + math.cos(a) * (r + 3.2 * S), cy + math.sin(a) * (r + 3.2 * S)],
               fill=color, width=int(2.2 * S))


def icon_chevron(img, cx, cy, size, color=TEXT, direction="left"):
    d = ImageDraw.Draw(img)
    s = size
    if direction == "left":
        d.line([(cx + s * 0.35, cy - s), (cx - s * 0.35, cy), (cx + s * 0.35, cy + s)],
               fill=color, width=int(2.4 * S), joint="curve")
    else:
        d.line([(cx - s * 0.35, cy - s), (cx + s * 0.35, cy), (cx - s * 0.35, cy + s)],
               fill=color, width=int(2.4 * S), joint="curve")


def icon_send(img, cx, cy, s, color=WHITE):
    d = ImageDraw.Draw(img)
    d.line([(cx - s * 0.75, cy - s * 0.75), (cx + s * 0.85, cy), (cx - s * 0.75, cy + s * 0.75)],
           fill=color, width=int(2.2 * S), joint="curve")
    d.line([(cx - s * 0.75, cy - s * 0.75), (cx - s * 0.2, cy)], fill=color, width=int(2.2 * S))
    d.line([(cx - s * 0.75, cy + s * 0.75), (cx - s * 0.2, cy)], fill=color, width=int(2.2 * S))


def icon_eye(img, cx, cy, s, color=TEXT_3):
    d = ImageDraw.Draw(img)
    d.arc([cx - s, cy - s * 0.75, cx + s, cy + s * 0.75], 0, 360, fill=color, width=int(1.5 * S))
    d.ellipse([cx - s * 0.3, cy - s * 0.3, cx + s * 0.3, cy + s * 0.3], fill=color)


def icon_check(img, cx, cy, s, color=WHITE):
    ImageDraw.Draw(img).line([(cx - s * 0.6, cy), (cx - s * 0.15, cy + s * 0.45), (cx + s * 0.65, cy - s * 0.5)],
                             fill=color, width=int(2 * S), joint="curve")


def orb(img, cx, cy, r):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(layer).ellipse([cx - r * 1.9, cy - r * 1.9, cx + r * 1.9, cy + r * 1.9],
                                  fill=PRIMARY + (70,))
    layer = layer.filter(ImageFilter.GaussianBlur(r * 0.9))
    img.alpha_composite(layer)
    img.alpha_composite(gradient_circle(int(r * 2), (124, 92, 255), (59, 130, 246), (34, 211, 238)),
                        (int(cx - r), int(cy - r)))


def input_bar(img, bar_y, show_chips=False):
    rrect(img, [0, bar_y - 10 * S, W, H], 0, fill=(12, 11, 25))
    ImageDraw.Draw(img).line([(0, bar_y - 10 * S), (W, bar_y - 10 * S)], fill=(44, 41, 72, 180), width=1 * S)
    if show_chips:
        cx0 = 22 * S
        for c in ("优化文案", "深度思考"):
            w = tw(img, c, 10, 500)
            rrect(img, [cx0, bar_y, cx0 + w + 20 * S, bar_y + 22 * S], 11 * S,
                  fill=(49, 42, 92), outline=(96, 76, 170), width=1 * S)
            text(img, (cx0 + 10 * S, bar_y + 3.5 * S), c, 10, 500, (190, 178, 255))
            cx0 += w + 28 * S
        iy = bar_y + 34 * S
    else:
        iy = bar_y + 14 * S
    rrect(img, [16 * S, iy, W - 16 * S, iy + 46 * S], 23 * S, fill=SURFACE, outline=STROKE, width=1 * S)
    d = ImageDraw.Draw(img)
    d.ellipse([30 * S, iy + 15 * S, 44 * S, iy + 29 * S], outline=TEXT_2, width=int(1.8 * S))
    d.line([(37 * S, iy + 18 * S), (37 * S, iy + 26 * S)], fill=TEXT_2, width=int(1.8 * S))
    d.line([(33 * S, iy + 22 * S), (41 * S, iy + 22 * S)], fill=TEXT_2, width=int(1.8 * S))
    text(img, (56 * S, iy + 14 * S), "输入消息…", 12.5, 400, TEXT_3)
    grad_rrect(img, [W - 68 * S, iy + 7 * S, W - 24 * S, iy + 39 * S], 16 * S, PRIMARY, PRIMARY_2, True)
    icon_send(img, W - 46 * S, iy + 23 * S, 7.5 * S, WHITE)

# ================= SCREEN 1: CHAT =================
def screen_chat():
    img = base_screen()
    status_bar(img)
    hdr_y = 54 * S
    rrect(img, [0, hdr_y - 6 * S, W, hdr_y + 52 * S], 0, fill=(11, 10, 22))
    ImageDraw.Draw(img).line([(0, hdr_y + 52 * S), (W, hdr_y + 52 * S)],
                             fill=(44, 41, 72, 160), width=1 * S)
    orb(img, 40 * S, hdr_y + 23 * S, 17 * S)
    text(img, (66 * S, hdr_y + 11 * S), "星语 AI", 15, 700, TEXT)
    ImageDraw.Draw(img).ellipse([66 * S, hdr_y + 33 * S, 72 * S, hdr_y + 39 * S], fill=GREEN)
    text(img, (78 * S, hdr_y + 30 * S), "在线 · nova-4o", 11, 400, TEXT_2)
    icon_gear(img, W - 34 * S, hdr_y + 23 * S, 9 * S, TEXT_2)

    cy = hdr_y + 76 * S
    label = "今天 09:41"
    w = tw(img, label, 10.5, 500)
    rrect(img, [W / 2 - w / 2 - 12 * S, cy, W / 2 + w / 2 + 12 * S, cy + 20 * S],
          10 * S, fill=(38, 35, 64))
    text(img, (W / 2, cy + 10 * S), label, 10.5, 500, TEXT_3, anchor="mm")

    y = cy + 38 * S
    rrect(img, [56 * S, y, W - 22 * S, y + 62 * S], 18 * S, fill=BUBBLE_AI)
    rrect(img, [56 * S, y, W - 22 * S, y + 62 * S], 18 * S, outline=(58, 54, 92, 150), width=1 * S)
    text(img, (72 * S, y + 14 * S), "早上好！我是星语，你的 AI 对话助手。", 12.5, 400, TEXT)
    text(img, (72 * S, y + 36 * S), "今天想让我帮你做点什么？", 12.5, 400, TEXT)

    y2 = y + 78 * S
    ux1 = W - 22 * S
    ux0 = ux1 - 268 * S
    grad_rrect(img, [ux0, y2, ux1, y2 + 62 * S], 18 * S, (109, 74, 255), (139, 92, 246))
    text(img, (ux0 + 16 * S, y2 + 14 * S), "帮我把这段产品介绍改写得更有吸引力，", 12.5, 400, WHITE)
    text(img, (ux0 + 16 * S, y2 + 36 * S), "控制在 100 字以内。", 12.5, 400, WHITE)

    y3 = y2 + 80 * S
    orb(img, 34 * S, y3 + 12 * S, 11 * S)
    text(img, (52 * S, y3 + 2 * S), "星语 AI", 10.5, 600, TEXT_2)
    box_h = 150 * S
    rrect(img, [52 * S, y3 + 22 * S, W - 22 * S, y3 + 22 * S + box_h], 18 * S, fill=BUBBLE_AI)
    text(img, (68 * S, y3 + 38 * S), "改写后的版本", 12.5, 500, TEXT)
    rrect(img, [68 * S, y3 + 62 * S, W - 38 * S, y3 + 118 * S], 12 * S,
          fill=(49, 42, 92), outline=(124, 92, 255), width=1 * S)
    text(img, (80 * S, y3 + 72 * S), "让每一次灵感都有回应。", 12, 500, (198, 186, 255))
    text(img, (80 * S, y3 + 92 * S), "轻触即达的智能对话体验。", 12, 500, (198, 186, 255))
    text(img, (68 * S, y3 + 130 * S), "已精简 42 字 · 语气更主动", 10.5, 400, TEXT_3)

    y4 = y3 + 22 * S + box_h + 10 * S
    for i, lbl in enumerate(("复制", "重新生成")):
        w2 = tw(img, lbl, 10, 500)
        bx = 52 * S + i * (w2 + 26 * S)
        rrect(img, [bx, y4, bx + w2 + 16 * S, y4 + 20 * S], 10 * S, fill=(38, 36, 64))
        text(img, (bx + w2 / 2 + 8 * S, y4 + 10 * S), lbl, 10, 500, TEXT_2, anchor="mm")

    y5 = y4 + 34 * S
    rrect(img, [52 * S, y5, 118 * S, y5 + 34 * S], 14 * S, fill=BUBBLE_AI)
    for i in range(3):
        cx = 70 * S + i * 14 * S
        ImageDraw.Draw(img).ellipse([cx - 3 * S, y5 + 14 * S, cx + 3 * S, y5 + 20 * S],
                                    fill=(168, 160, 200 - i * 40))

    input_bar(img, H - 118 * S, show_chips=True)
    nav_bar(img)
    return img


# ================= SCREEN 2: SETTINGS =================
def field(img, y, label, value, hint=None, masked=False, height=56):
    text(img, (22 * S, y), label, 11, 600, TEXT_2)
    top = y + 18 * S
    rrect(img, [22 * S, top, W - 22 * S, top + height * S], 14 * S, fill=SURFACE, outline=STROKE, width=1 * S)
    if masked:
        text(img, (38 * S, top + height * S / 2), "••••••••••••••••4f2a", 13, 500, TEXT, anchor="lm")
        icon_eye(img, W - 46 * S, top + height * S / 2, 8 * S)
    else:
        text(img, (38 * S, top + height * S / 2), value, 13, 500, TEXT, anchor="lm")
    if hint:
        text(img, (24 * S, top + height * S + 6 * S), hint, 9.5, 400, TEXT_3)
    return top + height * S + (20 * S if hint else 0)


def screen_settings():
    img = base_screen()
    status_bar(img)
    rrect(img, [0, 46 * S, W, 100 * S], 0, fill=(13, 12, 26))
    icon_chevron(img, 34 * S, 73 * S, 8 * S, TEXT)
    text(img, (W / 2, 73 * S), "设置", 15, 700, TEXT, anchor="mm")
    text(img, (W - 22 * S, 73 * S), "保存", 12.5, 600, (167, 139, 250), anchor="rm")

    y = 112 * S
    text(img, (22 * S, y), "服务提供商", 11, 600, TEXT_2)
    x, cy = 22 * S, y + 18 * S
    for name, active in (("OpenAI", False), ("DeepSeek", True), ("自定义", False)):
        w = tw(img, name, 11.5, 600)
        cw = w + 30 * S
        if active:
            grad_rrect(img, [x, cy, x + cw, cy + 32 * S], 16 * S, PRIMARY, PRIMARY_2, True)
            text(img, (x + cw / 2, cy + 16 * S), name, 11.5, 600, WHITE, anchor="mm")
        else:
            rrect(img, [x, cy, x + cw, cy + 32 * S], 16 * S, fill=SURFACE, outline=STROKE, width=1 * S)
            text(img, (x + cw / 2, cy + 16 * S), name, 11.5, 600, TEXT_2, anchor="mm")
        x += cw + 8 * S

    y = field(img, cy + 52 * S, "API BASE URL", "https://api.deepseek.com/v1",
              hint="兼容 OpenAI Chat Completions 协议的地址均可")
    y = field(img, y + 10 * S, "API KEY", "", masked=True,
              hint="仅保存在本机，EncryptedSharedPreferences 加密")
    y = field(img, y + 10 * S, "模型名称", "deepseek-chat", hint="填写服务商支持的模型 ID")
    y = field(img, y + 10 * S, "系统提示词", "你是一个乐于助人的中文 AI 助手。", height=64)

    text(img, (22 * S, y + 12 * S), "温度", 11, 600, TEXT_2)
    text(img, (W - 22 * S, y + 12 * S), "0.7", 11, 700, (167, 139, 250), anchor="ra")
    sy = y + 42 * S
    rrect(img, [22 * S, sy, W - 22 * S, sy + 5 * S], 3 * S, fill=(58, 54, 92))
    tw2 = (W - 44 * S) * 0.7
    grad_rrect(img, [22 * S, sy, 22 * S + tw2, sy + 5 * S], 3 * S, PRIMARY, ACCENT, True)
    cxs = 22 * S + tw2
    ImageDraw.Draw(img).ellipse([cxs - 8 * S, sy - 5.5 * S, cxs + 8 * S, sy + 10.5 * S], fill=WHITE)

    ty = sy + 34 * S
    rrect(img, [22 * S, ty, W - 22 * S, ty + 44 * S], 14 * S, fill=(35, 31, 62),
          outline=(124, 92, 255, 120), width=1 * S)
    ImageDraw.Draw(img).ellipse([40 * S, ty + 14 * S, 56 * S, ty + 30 * S], fill=(52, 211, 153, 40))
    icon_check(img, 48 * S, ty + 22 * S, 4.5 * S, GREEN)
    text(img, (68 * S, ty + 15 * S), "连接正常", 12, 600, (167, 240, 214))
    text(img, (68 * S, ty + 30 * S), "deepseek-chat · 延迟 312 ms", 10, 400, TEXT_3)
    text(img, (W - 40 * S, ty + 22 * S), "重新测试", 10.5, 600, (167, 139, 250), anchor="rm")

    by = ty + 62 * S
    grad_rrect(img, [22 * S, by, W - 22 * S, by + 48 * S], 18 * S, PRIMARY, (139, 92, 246), True)
    text(img, (W / 2, by + 24 * S), "保存并开始对话", 13.5, 700, WHITE, anchor="mm")
    text(img, (W / 2, by + 66 * S), "数据仅保存在设备本地 · 支持主流 OpenAI 兼容接口", 9.5, 400, TEXT_3, anchor="mm")
    nav_bar(img)
    return img


def card_icon(img, kind, cx, cy):
    """Small monochrome line icons for the welcome suggestion cards."""
    d = ImageDraw.Draw(img)
    c = (176, 164, 245)
    w = int(1.7 * S)
    if kind == "mail":
        d.rounded_rectangle([cx - 11 * S, cy - 8 * S, cx + 11 * S, cy + 8 * S], 3 * S, outline=c, width=w)
        d.line([(cx - 10 * S, cy - 6 * S), (cx, cy + 2 * S), (cx + 10 * S, cy - 6 * S)], fill=c, width=w, joint="curve")
    elif kind == "code":
        d.line([(cx - 4 * S, cy - 8 * S), (cx - 11 * S, cy), (cx - 4 * S, cy + 8 * S)], fill=c, width=w, joint="curve")
        d.line([(cx + 4 * S, cy - 8 * S), (cx + 11 * S, cy), (cx + 4 * S, cy + 8 * S)], fill=c, width=w, joint="curve")
        d.line([(cx + 2 * S, cy - 9 * S), (cx - 2 * S, cy + 9 * S)], fill=c, width=w)
    elif kind == "fit":
        d.line([(cx - 5 * S, cy), (cx + 5 * S, cy)], fill=c, width=w)
        d.rounded_rectangle([cx - 11 * S, cy - 6 * S, cx - 5 * S, cy + 6 * S], 2 * S, outline=c, width=w)
        d.rounded_rectangle([cx + 5 * S, cy - 6 * S, cx + 11 * S, cy + 6 * S], 2 * S, outline=c, width=w)
        d.line([(cx - 8 * S, cy - 9 * S), (cx - 8 * S, cy + 9 * S)], fill=c, width=w)
        d.line([(cx + 8 * S, cy - 9 * S), (cx + 8 * S, cy + 9 * S)], fill=c, width=w)
    elif kind == "globe":
        d.ellipse([cx - 9 * S, cy - 9 * S, cx + 9 * S, cy + 9 * S], outline=c, width=w)
        d.line([(cx - 9 * S, cy), (cx + 9 * S, cy)], fill=c, width=w)
        d.ellipse([cx - 4 * S, cy - 9 * S, cx + 4 * S, cy + 9 * S], outline=c, width=w)


# ================= SCREEN 3: WELCOME =================
def screen_welcome():
    img = base_screen()
    status_bar(img)
    icon_gear(img, W - 34 * S, 66 * S, 9 * S, TEXT_2)
    text(img, (22 * S, 58 * S), "星语 AI", 14, 700, TEXT)

    orb(img, W / 2, 230 * S, 52 * S)
    d = ImageDraw.Draw(img)
    d.line([(W / 2 - 12 * S, 230 * S), (W / 2 + 12 * S, 230 * S)], fill=WHITE, width=int(1.6 * S))
    d.line([(W / 2, 230 * S - 12 * S), (W / 2, 230 * S + 12 * S)], fill=WHITE, width=int(1.6 * S))

    text(img, (W / 2, 320 * S), "今天想聊点什么？", 23, 700, TEXT, anchor="mm")
    text(img, (W / 2, 352 * S), "随时提问、写作、翻译或一起头脑风暴", 12, 400, TEXT_2, anchor="mm")

    cards = [("mail", "帮我写一封邮件", "正式、简洁，面向客户"),
             ("code", "解释这段代码", "逐步拆解核心逻辑"),
             ("fit", "制定健身计划", "每周 4 次，居家训练"),
             ("globe", "翻译并润色", "中英互译，语气自然")]
    cw = (W - 22 * S * 2 - 12 * S) / 2
    ch = 82 * S
    y0 = 384 * S
    for i, (ic, t1, t2) in enumerate(cards):
        cx = 22 * S + (i % 2) * (cw + 12 * S)
        cyy = y0 + (i // 2) * (ch + 12 * S)
        rrect(img, [cx, cyy, cx + cw, cyy + ch], 16 * S, fill=SURFACE, outline=STROKE, width=1 * S)
        card_icon(img, ic, cx + 24 * S, cyy + 22 * S)
        text(img, (cx + 14 * S, cyy + 40 * S), t1, 11.5, 600, TEXT)
        text(img, (cx + 14 * S, cyy + 58 * S), t2, 9.5, 400, TEXT_3)

    input_bar(img, H - 108 * S, show_chips=False)
    nav_bar(img)
    return img


# ================= SPEC SHEET =================
def spec_sheet():
    cw, chh = 540, 590
    img = linear((cw, chh), (12, 12, 24), (22, 18, 44)).convert("RGBA")
    glow(img, (cw - 60, 40), 240, PRIMARY, 60, 90)
    text(img, (44, 36), "星语 AI · NovaChat", 23, 700, TEXT)
    text(img, (44, 70), "Android Design Spec v1.0  ·  Material 3 + Jetpack Compose  ·  390×844 dp", 10.5, 400, TEXT_2)

    text(img, (44, 108), "COLOR TOKENS", 9, 700, TEXT_3)
    swatches = [("Bg", BG_TOP, "#090914"), ("Surface", SURFACE, "#1A1830"),
                ("Primary", PRIMARY, "#7C5CFF"), ("Blue", PRIMARY_2, "#3B82F6"),
                ("Accent", ACCENT, "#22D3EE"), ("Text", TEXT, "#F2F1FA"),
                ("Text2", TEXT_2, "#9A98B8"), ("Stroke", STROKE, "#343058")]
    for i, (n, c, hx) in enumerate(swatches):
        x = 44 + (i % 4) * 124
        y = 128 + (i // 4) * 74
        rrect(img, [x, y, x + 104, y + 40], 10, fill=c, outline=(255, 255, 255, 30), width=1)
        text(img, (x, y + 46), n, 9, 600, TEXT)
        text(img, (x, y + 58), hx, 8, 400, TEXT_3)

    text(img, (44, 288), "TYPOGRAPHY", 9, 700, TEXT_3)
    rows = [("H1 标题", 23, 700, "今天想聊点什么？"),
            ("H2 会话名", 15, 700, "星语 AI"),
            ("Body 正文", 12.5, 400, "随时提问、写作、翻译或头脑风暴"),
            ("Caption 辅助", 10.5, 400, "在线 · nova-4o")]
    y = 308
    for n, s, w, sample in rows:
        text(img, (44, y), n, 8.5, 400, TEXT_3)
        text(img, (150, y - 8), sample, s * 0.8, w, TEXT)
        y += 38

    text(img, (44, 464), "COMPONENTS", 9, 700, TEXT_3)
    grad_rrect(img, [44, 484, 244, 522], 18, PRIMARY, (139, 92, 246), True)
    text(img, (60, 495), "用户消息气泡", 11, 500, WHITE)
    rrect(img, [44, 532, 264, 570], 18, fill=BUBBLE_AI, outline=(58, 54, 92, 255), width=1)
    text(img, (60, 543), "AI 消息气泡", 11, 400, TEXT)
    grad_rrect(img, [292, 484, 352, 518], 16, PRIMARY, PRIMARY_2, True)
    icon_send(img, 322, 501, 6.5, WHITE)
    rrect(img, [292, 528, 448, 564], 18, fill=SURFACE, outline=STROKE, width=1)
    text(img, (306, 538), "输入框 · 圆角 23", 9.5, 400, TEXT_3)
    rrect(img, [292, 574, 470, 606], 14, fill=(124, 92, 255, 40), outline=(124, 92, 255, 140), width=1)
    text(img, (306, 583), "功能卡片 · 主色 12% 叠加", 9.5, 500, (190, 178, 255))

    text(img, (44, 628), "SPACING · 8pt grid", 9, 700, TEXT_3)
    for i, sp in enumerate((4, 8, 12, 16, 22, 32)):
        x = 44 + i * 62
        rrect(img, [x, 648, x + sp + 10, 662], 4, fill=PRIMARY)
        text(img, (x, 668), f"{sp}dp", 8.5, 400, TEXT_2)

    text(img, (44, 700), "BEHAVIOR", 9, 700, TEXT_3)
    behaviors = ["流式输出（SSE）逐字渲染 + 光标呼吸动效",
                 "API Key 加密存储，可一键清除，首次启动引导",
                 "失败态内联错误气泡 + 重试，附 HTTP 状态码",
                 "复制 / 重新生成 / 停止生成 / 多会话草稿"]
    y = 720
    for b in behaviors:
        ImageDraw.Draw(img).ellipse([46, y + 4, 52, y + 10], fill=ACCENT)
        text(img, (60, y), b, 9, 400, TEXT_2)
        y += 21
    return img


def main():
    chat = phone_frame(screen_chat())
    settings = phone_frame(screen_settings())
    welcome = phone_frame(screen_welcome())
    chat.save(os.path.join(OUT, "screen_chat.png"))
    settings.save(os.path.join(OUT, "screen_settings.png"))
    welcome.save(os.path.join(OUT, "screen_welcome.png"))
    spec_sheet().save(os.path.join(OUT, "design_spec.png"))

    pad, title_h = 56, 130
    tw_ = chat.width + settings.width + welcome.width + pad * 4
    th_ = max(chat.height, settings.height, welcome.height) + title_h + pad
    board = linear((tw_, th_), (8, 8, 18), (18, 14, 38)).convert("RGBA")
    glow(board, (tw_ // 2, 0), 520, PRIMARY, 60, 140)
    text(board, (pad, 26), "星语 AI · NovaChat — Android AI 对话 Agent 设计稿", 27, 700, TEXT)
    text(board, (pad, 92), "核心流程：欢迎页 → 对话页 → API Key 配置页   |   深空紫渐变 + Material 3 圆角语言", 13, 400, TEXT_2)
    x = pad
    for im, label in ((welcome, "01 欢迎 / 快捷入口"), (chat, "02 对话主界面"), (settings, "03 API Key 配置")):
        board.alpha_composite(im, (x, title_h))
        text(board, (x + 18, title_h + im.height + 14), label, 12.5, 600, TEXT_2)
        x += im.width + pad
    board.save(os.path.join(OUT, "design_mockup.png"))
    print("mockups written to", OUT)


if __name__ == "__main__":
    main()
