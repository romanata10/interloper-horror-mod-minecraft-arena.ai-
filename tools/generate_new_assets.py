#!/usr/bin/env python3
"""
Generates the textures used by the Source Void update.

Everything is procedural: no external art, no copyright victims.
    python3 tools/generate_new_assets.py
"""
import os
import random

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "interloper", "textures")


def save(img, *parts):
    path = os.path.join(ASSETS, *parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("texture:", os.path.relpath(path, ROOT))


def noise_fill(img, box, rgb, spread=6, seed=1):
    rng = random.Random(seed)
    px = img.load()
    for y in range(box[1], box[3]):
        for x in range(box[0], box[2]):
            d = rng.randint(-spread, spread)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in rgb) + (255,)


def flat(color, size=(16, 16)):
    return Image.new("RGBA", size, color)


# ---------------------------------------------------------------- block / item

def source_floor():
    img = flat((126, 126, 126, 255), (16, 16))
    px = img.load()
    rng = random.Random(4)
    for y in range(16):
        for x in range(16):
            v = 124 + rng.randint(-1, 1)
            px[x, y] = (v, v, v, 255)
    save(img, "block", "source_floor.png")


def console_item():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(2, 15):
        for x in range(1, 15):
            px[x, y] = (18, 20, 22, 255)
    for x in range(1, 15):
        px[x, 2] = (60, 66, 70, 255)
        px[x, 14] = (40, 44, 46, 255)
    for y in (4, 6, 8, 10):
        x0 = 3
        x1 = 3 + random.Random(y).randint(3, 9)
        for x in range(x0, x1):
            px[x, y] = (200, 210, 210, 255) if y != 8 else (255, 150, 40, 255)
    save(img, "item", "console.png")


# ---------------------------------------------------------------- humanoid skins

HEAD = (0, 0, 32, 16)
BODY = (16, 16, 40, 32)
RIGHT_ARM = (40, 16, 56, 32)
LEFT_ARM = (32, 48, 48, 64)
RIGHT_LEG = (0, 16, 16, 32)
LEFT_LEG = (16, 48, 32, 64)


def humanoid_skin(name, skin, shirt, pants, eye, seed=1, extra=None):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    noise_fill(img, HEAD, skin, seed=seed)
    noise_fill(img, BODY, shirt, seed=seed + 1)
    noise_fill(img, RIGHT_ARM, shirt, seed=seed + 2)
    noise_fill(img, LEFT_ARM, shirt, seed=seed + 3)
    noise_fill(img, RIGHT_LEG, pants, seed=seed + 4)
    noise_fill(img, LEFT_LEG, pants, seed=seed + 5)

    px = img.load()
    # front face of the head is (8, 8) - (16, 16)
    for y in range(8, 16):
        for x in range(8, 16):
            px[x, y] = tuple(max(0, min(255, c + random.Random(x * 31 + y).randint(-4, 4)))
                             for c in skin) + (255,)
    for x, y in ((9, 10), (10, 10), (13, 10), (14, 10), (9, 11), (14, 11)):
        px[x, y] = eye + (255,)
    for x in range(10, 14):
        px[x, 13] = (20, 20, 20, 255)

    if extra:
        extra(img, px)
    save(img, "entity", name)


def extra_zombie(img, px):
    rng = random.Random(9)
    for _ in range(70):
        x = rng.randint(0, 63)
        y = rng.randint(0, 63)
        px[x, y] = rng.choice([(40, 52, 36, 255), (90, 100, 80, 255), (24, 30, 24, 255)])


def extra_checker(img, px):
    for y in range(64):
        for x in range(64):
            if y >= 38 or (x >= 30 and y < 38):
                on = ((x // 4) + (y // 4)) % 2 == 0
                px[x, y] = (255, 0, 255, 255) if on else (12, 12, 12, 255)


def corrupted_npc():
    humanoid_skin("corrupted_npc.png", (86, 104, 78), (52, 66, 50), (38, 46, 40),
                  (12, 12, 12), seed=11, extra=extra_zombie)


def crasher():
    def extra(img, px):
        for y in range(64):
            for x in range(64):
                if (x + y) % 17 == 0:
                    px[x, y] = (20, 14, 10, 255)

    humanoid_skin("crasher.png", (150, 120, 96), (104, 74, 44), (70, 52, 36),
                  (10, 10, 10), seed=21, extra=extra)


def dmnpc():
    def extra(img, px):
        # long glowing eyes, twice as wide as a human's
        for x in range(8, 16):
            px[x, 10] = (170, 90, 255, 255)
            px[x, 11] = (120, 40, 220, 255)
        px[8, 12] = (90, 20, 200, 255)
        px[15, 12] = (90, 20, 200, 255)

    humanoid_skin("dmnpc.png", (16, 16, 22), (10, 10, 14), (8, 8, 10),
                  (150, 70, 255), seed=31, extra=extra)


def skyghost():
    def extra(img, px):
        for y in range(8, 16):
            for x in range(8, 16):
                px[x, y] = (228, 220, 210, 255)
        for x, y in ((9, 10), (10, 10), (13, 10), (14, 10), (9, 11), (10, 11), (13, 11), (14, 11)):
            px[x, y] = (4, 4, 4, 255)
        # smile that is a little too wide
        for x in range(9, 15):
            px[x, 13] = (20, 20, 20, 255)

    humanoid_skin("skyghost_skin.png", (222, 214, 204), (56, 74, 116), (32, 40, 62),
                  (4, 4, 4), seed=41, extra=extra)


def interloper_corrupt():
    img = Image.new("RGBA", (64, 64), (10, 10, 12, 255))
    px = img.load()
    extra_checker(img, px)
    # head stays black, only the lower body and the umbrella are missing
    for y in range(0, 16):
        for x in range(0, 32):
            px[x, y] = (8, 8, 10, 255)
    for x in range(0, 32):
        px[x, 8] = (200, 200, 200, 255)
    save(img, "entity", "interloper_corrupt.png")


def vrad():
    img = Image.new("RGBA", (32, 32), (255, 0, 0, 255))
    px = img.load()
    for y in range(32):
        for x in range(32):
            on = ((x // 4) + (y // 4)) % 2 == 0
            px[x, y] = (220, 0, 0, 255) if on else (0, 0, 0, 255)
    for x in range(4, 28):
        px[x, 8] = (255, 255, 255, 255)
        px[x, 23] = (255, 210, 0, 255)
    for y in range(10, 22):
        px[4, y] = (255, 140, 0, 255)
        px[27, y] = (255, 140, 0, 255)
    save(img, "entity", "vrad.png")


def cone():
    img = Image.new("RGBA", (32, 32), (128, 128, 128, 255))
    px = img.load()
    for y in range(32):
        for x in range(32):
            v = 126 + random.Random(x * 7 + y * 13).randint(-3, 3)
            px[x, y] = (v, v, v, 255)
    for x in range(32):
        for y in (0, 1, 16, 17):
            px[x, y] = (96, 96, 96, 255)
    save(img, "entity", "cone.png")


def main():
    source_floor()
    console_item()
    corrupted_npc()
    crasher()
    dmnpc()
    skyghost()
    interloper_corrupt()
    vrad()
    cone()


if __name__ == "__main__":
    main()
