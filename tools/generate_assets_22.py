#!/usr/bin/env python3
"""Textures for the 2.2.0 update: the street, the graffiti, and everything you
can craft out of the pieces of a broken npc."""
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


def brick():
    """The wall the street is built from."""
    img = noisy((16, 16), (118, 112, 104), 8, 71)
    d = ImageDraw.Draw(img)
    for y in range(0, 16, 4):
        d.line([0, y, 15, y], fill=(92, 88, 82, 255))
        off = 0 if (y // 4) % 2 == 0 else 4
        for x in range(off, 16, 8):
            d.line([x, y, x, y + 3], fill=(96, 92, 86, 255))
    return img


def graffiti():
    """A man with an umbrella, sprayed on the bricks."""
    base = brick()
    img = base.copy()
    d = ImageDraw.Draw(img)
    red = (168, 32, 32, 255)
    dark = (72, 12, 12, 255)
    # umbrella canopy
    d.polygon([(4, 7), (12, 7), (8, 2)], fill=red)
    d.line([(4, 7), (12, 7)], fill=dark)
    d.line([(8, 7), (8, 12)], fill=dark)
    d.point((8, 13), fill=dark)
    # body and legs
    d.line([(8, 8), (8, 11)], fill=dark)
    d.line([(6, 15), (8, 11)], fill=dark)
    d.line([(10, 15), (8, 11)], fill=dark)
    d.point((7, 8), fill=red)
    d.point((9, 8), fill=red)
    # a stroke of the person doing it
    d.line([(1, 14), (3, 15)], fill=(20, 20, 20, 255))
    save(img, "block", "graffiti.png")


def ravencheck():
    """A small terminal with one line of green text."""
    img = noisy((16, 16), (44, 44, 48), 6, 84)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(26, 26, 28, 255))
    d.rectangle([2, 3, 13, 10], fill=(12, 18, 12, 255))
    for i, w in enumerate((8, 5, 9, 4)):
        d.line([(3, 4 + i), (3 + w, 4 + i)], fill=(70, 220, 90, 255))
    d.rectangle([6, 11, 9, 14], fill=(120, 120, 120, 255))
    d.point((7, 12), fill=(200, 60, 40, 255))
    save(img, "block", "ravencheck.png")


def void_probe_entity():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 4, 13, 11], fill=(96, 104, 112, 255))
    d.rectangle([4, 6, 11, 9], fill=(38, 42, 46, 255))
    d.rectangle([5, 7, 7, 8], fill=(220, 60, 40, 255))
    d.point((2, 7), fill=(150, 150, 150, 255))
    d.point((13, 7), fill=(150, 150, 150, 255))
    save(img, "entity", "void_probe.png")


def white_cube_entity():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 2, 13, 13], fill=(250, 250, 250, 255))
    d.rectangle([2, 2, 13, 13], outline=(206, 206, 210, 255))
    save(img, "entity", "white_cube.png")


def item_void_probe():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 5, 12, 10], fill=(104, 112, 120, 255))
    d.rectangle([5, 6, 9, 8], fill=(40, 44, 48, 255))
    d.point((6, 7), fill=(230, 70, 50, 255))
    for x in (2, 13):
        d.line([(x, 7), (x, 8)], fill=(190, 190, 190, 255))
    save(img, "item", "void_probe.png")


def item_dm_editor():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 3, 13, 12], fill=(28, 30, 34, 255))
    d.rectangle([3, 4, 12, 6], fill=(20, 60, 90, 255))
    d.line([(4, 8), (11, 8)], fill=(90, 210, 240, 255))
    d.line([(4, 10), (9, 10)], fill=(90, 210, 240, 255))
    d.point((11, 11), fill=(240, 200, 60, 255))
    save(img, "item", "dm_editor.png")


def item_dm_code_fragment():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for i in range(4):
        y = 3 + i * 3
        w = 5 + (i * 3) % 7
        d.line([(3, y), (3 + w, y)], fill=(90, 210, 240, 255))
    d.line([(3, 2), (3, 13)], fill=(40, 80, 100, 255))
    save(img, "item", "dm_code_fragment.png")


def item_broken_npc_debris():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(2, 9), (6, 4), (8, 10), (5, 12)], fill=(148, 148, 154, 255))
    d.polygon([(8, 3), (13, 5), (11, 9), (8, 8)], fill=(112, 112, 118, 255))
    d.point((12, 11), fill=(240, 80, 60, 255))
    save(img, "item", "broken_npc_debris.png")


def item_flashlight_nuke():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 6, 9, 10], fill=(60, 64, 70, 255))
    d.rectangle([9, 4, 13, 12], fill=(220, 200, 80, 255))
    d.rectangle([13, 6, 14, 10], fill=(255, 250, 200, 255))
    d.point((4, 5), fill=(240, 240, 240, 255))
    d.point((6, 5), fill=(240, 240, 240, 255))
    save(img, "item", "flashlight_nuke.png")


def item_source_box():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 4, 13, 12], fill=(58, 60, 66, 255))
    d.rectangle([2, 4, 13, 6], fill=(84, 88, 96, 255))
    d.rectangle([5, 8, 10, 11], fill=(20, 22, 24, 255))
    d.point((7, 9), fill=(80, 240, 120, 255))
    save(img, "item", "source_box.png")


def item_skybox_shard():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 2), (13, 7), (9, 14), (4, 9)], fill=(150, 215, 245, 255))
    d.polygon([(8, 4), (11, 7), (9, 11), (6, 8)], fill=(214, 242, 255, 255))
    save(img, "item", "skybox_shard.png")


def item_skybox_exe():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 2, 14, 13], fill=(28, 34, 44, 255))
    d.rectangle([1, 2, 14, 4], fill=(64, 82, 104, 255))
    d.rectangle([3, 6, 9, 11], fill=(150, 215, 245, 255))
    d.line([(4, 7), (8, 7)], fill=(240, 252, 255, 255))
    d.line([(4, 9), (7, 9)], fill=(240, 252, 255, 255))
    d.rectangle([10, 6, 12, 8], fill=(90, 210, 120, 255))
    save(img, "item", "skybox_exe.png")


def bread_note_book():
    """The chest book in the normal world."""
    img = noisy((16, 16), (150, 120, 78), 6, 21)
    d = ImageDraw.Draw(img)
    d.line([(0, 7), (15, 7)], fill=(116, 90, 56, 255))
    d.rectangle([2, 2, 5, 4], fill=(220, 210, 170, 255))
    return img


def main():
    graffiti()
    ravencheck()
    void_probe_entity()
    white_cube_entity()
    item_void_probe()
    item_dm_editor()
    item_dm_code_fragment()
    item_broken_npc_debris()
    item_flashlight_nuke()
    item_source_box()
    item_skybox_shard()
    item_skybox_exe()


if __name__ == "__main__":
    main()
