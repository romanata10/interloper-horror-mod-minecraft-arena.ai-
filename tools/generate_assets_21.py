#!/usr/bin/env python3
"""Textures for the 2.1.0 update: xbox 360 dreams, portal panels, the white door room."""
import os
import random
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "interloper", "textures")


def save(img, *parts):
    path = os.path.join(TEX, *parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("texture:", os.path.relpath(path, ROOT))


def noisy(size, color, spread, seed):
    img = Image.new("RGBA", size)
    px = img.load()
    rng = random.Random(seed)
    for y in range(size[1]):
        for x in range(size[0]):
            px[x, y] = tuple(max(0, min(255, c + rng.randint(-spread, spread))) for c in color) + (255,)
    return img


def xbox_grass():
    img = noisy((16, 16), (86, 158, 62), 10, 1)
    d = ImageDraw.Draw(img)
    for i in range(16):
        d.point((i, random.Random(i).randint(0, 3)), fill=(112, 186, 78, 255))
    save(img, "block", "xbox_grass.png")


def xbox_dirt():
    save(noisy((16, 16), (124, 88, 56), 12, 2), "block", "xbox_dirt.png")


def xbox_panel():
    img = Image.new("RGBA", (16, 16), (226, 226, 226, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 1], fill=(200, 200, 200, 255))
    d.rectangle([0, 14, 15, 15], fill=(186, 186, 186, 255))
    d.line([0, 8, 15, 8], fill=(206, 206, 206, 255))
    save(img, "block", "xbox_panel.png")


def xbox_glow():
    img = Image.new("RGBA", (16, 16), (140, 255, 60, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 2, 13, 13], fill=(210, 255, 140, 255))
    d.rectangle([5, 5, 10, 10], fill=(255, 255, 220, 255))
    save(img, "block", "xbox_glow.png")


def portal_panel():
    img = Image.new("RGBA", (16, 16), (238, 238, 234, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 0], fill=(214, 214, 210, 255))
    d.rectangle([0, 15, 15, 15], fill=(222, 222, 218, 255))
    d.rectangle([7, 0, 8, 15], fill=(228, 228, 224, 255))
    d.rectangle([0, 7, 15, 8], fill=(228, 228, 224, 255))
    save(img, "block", "portal_panel.png")


def portal_floor():
    img = noisy((16, 16), (150, 150, 146), 8, 3)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 0], fill=(120, 120, 116, 255))
    d.rectangle([0, 8, 15, 8], fill=(132, 132, 128, 255))
    d.rectangle([8, 0, 8, 15], fill=(132, 132, 128, 255))
    save(img, "block", "portal_floor.png")


def missing_texture():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            on = ((x // 4) + (y // 4)) % 2 == 0
            px[x, y] = (255, 0, 255, 255) if on else (24, 24, 24, 255)
    # the engine's little black cross on the undefined checker
    for i in range(16):
        px[i, i] = (12, 12, 12, 255)
        px[15 - i, i] = (12, 12, 12, 255)
    save(img, "block", "missing_texture.png")


def white_wall():
    save(noisy((16, 16), (246, 246, 246), 3, 4), "block", "white_wall.png")


def white_door():
    img = Image.new("RGBA", (16, 16), (250, 250, 250, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(214, 214, 214, 255))
    d.rectangle([3, 3, 12, 15], outline=(224, 224, 224, 255))
    d.ellipse([10, 8, 11, 9], fill=(180, 180, 180, 255))
    save(img, "block", "white_door.png")


def white_lock():
    img = Image.new("RGBA", (16, 16), (236, 236, 236, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([5, 6, 10, 12], fill=(120, 120, 120, 255))
    d.arc([5, 2, 10, 10], 180, 360, fill=(120, 120, 120, 255))
    d.rectangle([7, 8, 8, 10], fill=(50, 50, 50, 255))
    save(img, "block", "white_lock.png")


def white_door_key():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([3, 3, 8, 8], outline=(240, 240, 240, 255), width=2)
    d.line([8, 8, 13, 13], fill=(240, 240, 240, 255), width=2)
    d.line([11, 11, 13, 9], fill=(240, 240, 240, 255), width=2)
    save(img, "item", "white_door_key.png")


def interloper_umbrella():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(2, 6), (14, 6), (8, 1)], fill=(18, 18, 22, 255))
    d.line([8, 6, 8, 14], fill=(60, 60, 66, 255), width=1)
    d.line([8, 14, 6, 14], fill=(60, 60, 66, 255), width=1)
    d.line([2, 6, 14, 6], fill=(120, 120, 130, 255))
    save(img, "item", "interloper_umbrella.png")


# ---------------------------------------------------------------- broken characters

HEAD = (0, 0, 32, 16)
BODY = (16, 16, 40, 32)
ARMS = ((40, 16, 56, 32), (32, 48, 48, 64))
LEGS = ((0, 16, 16, 32), (16, 48, 32, 64))


def character(name, skin, shirt, pants, eye, seed):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for box, color, s in ((HEAD, skin, seed), (BODY, shirt, seed + 1),
                          (ARMS[0], shirt, seed + 2), (ARMS[1], shirt, seed + 3),
                          (LEGS[0], pants, seed + 4), (LEGS[1], pants, seed + 5)):
        rng = random.Random(s)
        px = img.load()
        for y in range(box[1], box[3]):
            for x in range(box[0], box[2]):
                px[x, y] = tuple(max(0, min(255, c + rng.randint(-6, 6))) for c in color) + (255,)
    px = img.load()
    for x, y in ((9, 10), (10, 10), (13, 10), (14, 10)):
        px[x, y] = eye + (255,)
    # the export broke: a stripe of missing texture across the model
    for y in range(24, 28):
        for x in range(16, 40):
            px[x, y] = (255, 0, 255, 255) if (x + y) % 4 < 2 else (20, 20, 20, 255)
    save(img, "entity", name)


def main():
    xbox_grass(); xbox_dirt(); xbox_panel(); xbox_glow()
    portal_panel(); portal_floor(); missing_texture()
    white_wall(); white_door(); white_lock()
    white_door_key(); interloper_umbrella()
    character("broken_scientist.png", (214, 190, 160), (226, 226, 226), (70, 70, 80), (40, 40, 40), 11)
    character("broken_citizen.png", (198, 160, 130), (60, 90, 140), (50, 50, 60), (20, 20, 20), 21)
    character("broken_soldier.png", (170, 140, 110), (70, 90, 60), (60, 70, 50), (60, 10, 10), 31)
    character("broken_robot.png", (150, 150, 160), (120, 120, 130), (90, 90, 100), (255, 80, 0), 41)


if __name__ == "__main__":
    main()
