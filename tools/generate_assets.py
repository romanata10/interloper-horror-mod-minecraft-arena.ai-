#!/usr/bin/env python3
"""
Generates all textures and sounds for the INTERLOPER mod.

Everything is procedural: no external assets, no third party art.
Run from the repository root:

    python3 tools/generate_assets.py

Requires: pillow, numpy, soundfile
"""
import math
import os
import random
import struct

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "interloper")
TEX = os.path.join(ASSETS, "textures")
SOUNDS = os.path.join(ASSETS, "sounds")

SR = 44100


def ensure(*paths):
    for path in paths:
        os.makedirs(path, exist_ok=True)


# --------------------------------------------------------------------------------------
# helpers
# --------------------------------------------------------------------------------------

def noise_layer(size, seed, low=0, high=255, alpha=(200, 255)):
    rng = random.Random(seed)
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    px = img.load()
    for y in range(size[1]):
        for x in range(size[0]):
            v = rng.randint(low, high)
            a = rng.randint(*alpha)
            px[x, y] = (v, v, v, a)
    return img


def save(img, *parts):
    path = os.path.join(*parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("texture:", os.path.relpath(path, ROOT))


def tint_noise(size, base, seed, spread=14, alpha=255):
    rng = random.Random(seed)
    img = Image.new("RGBA", size, base + (alpha,))
    px = img.load()
    for y in range(size[1]):
        for x in range(size[0]):
            n = rng.randint(-spread, spread)
            px[x, y] = (
                max(0, min(255, base[0] + n)),
                max(0, min(255, base[1] + n)),
                max(0, min(255, base[2] + n)),
                alpha,
            )
    return img


# --------------------------------------------------------------------------------------
# entity textures
# --------------------------------------------------------------------------------------

def interloper_texture():
    img = tint_noise((64, 64), (9, 9, 12), 1337, 12)
    d = ImageDraw.Draw(img)
    # faint vertical ooze on the body region
    for x in range(0, 32, 3):
        d.line([(x, 16), (x, 34)], fill=(20, 20, 26, 255))
    # umbrella cloth
    d.rectangle([26, 16, 49, 32], fill=(6, 6, 9, 255))
    d.line([(26, 16), (49, 16)], fill=(24, 24, 30, 255))
    # hair/head top
    d.rectangle([0, 0, 31, 8], fill=(5, 5, 7, 255))
    save(img, TEX, "entity", "interloper.png")


def interloper_eyes_texture():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # eye boxes use texOffs (32,0) and (38,0), 2x1x1 -> 6x2 uv block each
    d.rectangle([33, 0, 37, 1], fill=(255, 255, 255, 255))
    d.rectangle([39, 0, 43, 1], fill=(255, 255, 255, 255))
    img = img.filter(ImageFilter.GaussianBlur(0.4))
    save(img, TEX, "entity", "interloper_eyes.png")


def skyghost_texture():
    img = tint_noise((64, 64), (14, 14, 18), 4242, 10)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 31, 8], fill=(8, 8, 10, 255))
    # glasses
    d.rectangle([33, 0, 37, 2], fill=(240, 240, 245, 255))
    d.rectangle([43, 0, 47, 2], fill=(240, 240, 245, 255))
    # shoulders fade
    d.rectangle([0, 20, 21, 26], fill=(18, 18, 24, 255))
    save(img, TEX, "entity", "skyghost.png")


# --------------------------------------------------------------------------------------
# item textures (16x16)
# --------------------------------------------------------------------------------------

def demo_tape_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 3, 14, 12], fill=(28, 28, 32, 255), outline=(90, 90, 100, 255))
    d.rectangle([1, 3, 14, 5], fill=(120, 24, 24, 255))
    d.rectangle([3, 7, 12, 10], fill=(14, 14, 16, 255), outline=(70, 70, 78, 255))
    d.ellipse([4, 7, 6, 10], fill=(160, 160, 170, 255))
    d.ellipse([9, 7, 11, 10], fill=(160, 160, 170, 255))
    d.rectangle([3, 11, 12, 11], fill=(203, 190, 120, 255))
    save(img, TEX, "item", "demo_tape.png")


def skybox_fragment_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 1), (14, 7), (11, 14), (5, 14), (2, 7)], fill=(88, 190, 210, 255))
    d.polygon([(8, 3), (11, 7), (9, 12), (6, 12), (5, 7)], fill=(160, 235, 245, 255))
    d.line([(8, 3), (8, 12)], fill=(230, 255, 255, 255))
    d.point((4, 5), fill=(255, 255, 255, 255))
    save(img, TEX, "item", "skybox_fragment.png")


def anomaly_scanner_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 4, 12, 14], fill=(38, 40, 44, 255), outline=(96, 100, 110, 255))
    d.rectangle([5, 6, 10, 9], fill=(36, 120, 80, 255))
    d.line([(5, 8), (6, 7), (7, 8), (8, 6), (9, 8), (10, 7)], fill=(150, 255, 180, 255))
    d.line([(8, 4), (8, 1)], fill=(120, 124, 130, 255))
    d.rectangle([7, 0, 8, 1], fill=(200, 60, 60, 255))
    d.rectangle([6, 11, 9, 12], fill=(200, 200, 210, 255))
    save(img, TEX, "item", "anomaly_scanner.png")


def umbrella_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 2), (15, 9), (1, 9)], fill=(20, 20, 26, 255), outline=(70, 70, 80, 255))
    d.line([(1, 9), (15, 9)], fill=(120, 24, 24, 255))
    d.line([(8, 2), (8, 9)], fill=(70, 70, 80, 255))
    d.line([(8, 9), (8, 13)], fill=(110, 90, 60, 255))
    d.line([(8, 13), (6, 14)], fill=(110, 90, 60, 255))
    save(img, TEX, "item", "umbrella.png")


def engine_terminal_texture():
    img = tint_noise((16, 16), (44, 44, 50), 777, 12)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(24, 24, 28, 255))
    d.rectangle([2, 2, 13, 10], fill=(10, 10, 12, 255), outline=(80, 20, 20, 255))
    d.rectangle([3, 4, 12, 8], fill=(40, 12, 12, 255))
    d.line([(4, 5), (6, 5)], fill=(220, 60, 60, 255))
    d.line([(4, 7), (9, 7)], fill=(160, 40, 40, 255))
    d.rectangle([3, 12, 12, 13], fill=(30, 30, 34, 255))
    d.point((4, 12), fill=(220, 120, 60, 255))
    save(img, TEX, "block", "engine_terminal.png")


# --------------------------------------------------------------------------------------
# gui textures
# --------------------------------------------------------------------------------------

def noise_texture():
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    px = img.load()
    rng = random.Random(90210)
    for y in range(128):
        for x in range(128):
            v = rng.randint(0, 255)
            a = 255 if rng.random() < 0.75 else rng.randint(0, 120)
            px[x, y] = (v, v, v, a)
    save(img, TEX, "gui", "noise.png")


def vignette_texture():
    size = 256
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    cx = cy = size / 2.0
    maxd = math.hypot(cx, cy)
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - cx, y - cy) / maxd
            a = int(max(0.0, min(1.0, (d - 0.45) / 0.55)) ** 1.6 * 255)
            px[x, y] = (0, 0, 0, a)
    save(img, TEX, "gui", "vignette.png")


def logo_texture():
    w, h = 256, 256
    img = Image.new("RGBA", (w, h), (6, 6, 8, 255))
    static = noise_layer((w, h), 5, 0, 120, (40, 160))
    img.alpha_composite(static)
    d = ImageDraw.Draw(img)
    # umbrella silhouette
    d.polygon([(128, 58), (182, 96), (74, 96)], fill=(10, 10, 14, 255))
    d.line([(74, 96), (182, 96)], fill=(120, 20, 20, 255))
    # figure
    d.rectangle([124, 96, 132, 192], fill=(12, 12, 16, 255))
    d.rectangle([104, 192, 112, 216], fill=(12, 12, 16, 255))
    d.rectangle([144, 192, 152, 216], fill=(12, 12, 16, 255))
    # eyes
    d.rectangle([121, 74, 124, 77], fill=(240, 240, 245, 255))
    d.rectangle([132, 74, 135, 77], fill=(240, 240, 245, 255))
    d.text((16, 232), "INTERLOPER", fill=(180, 30, 30, 255))
    img = img.filter(ImageFilter.GaussianBlur(0.3))
    save(img, ROOT, "src", "main", "resources", "interloper_logo.png")


# --------------------------------------------------------------------------------------
# audio
# --------------------------------------------------------------------------------------

def norm(x, peak=0.85):
    m = np.max(np.abs(x)) if x.size else 1.0
    if m < 1e-9:
        return x
    return x / m * peak


def lowpass(x, alpha):
    y = np.empty_like(x)
    acc = 0.0
    for i in range(x.size):
        acc += alpha * (x[i] - acc)
        y[i] = acc
    return y


def hp(x):
    return x - lowpass(x, 0.02)


def env(n, attack, decay):
    a = int(SR * attack)
    d = int(SR * decay)
    e = np.zeros(n)
    a = min(a, n)
    e[:a] = np.linspace(0, 1, a)
    rest = n - a
    if rest > 0:
        e[a:] = np.exp(-np.linspace(0, 6.0, rest) * (n / max(d, 1)) * 0.05)
    return e


def write_sound(name, data, peak=0.85):
    data = norm(np.asarray(data, dtype=np.float64), peak)
    # small fade in/out to avoid clicks
    fade = min(256, data.size // 8)
    if fade > 0:
        data[:fade] *= np.linspace(0, 1, fade)
        data[-fade:] *= np.linspace(1, 0, fade)
    path = os.path.join(SOUNDS, name + ".ogg")
    os.makedirs(SOUNDS, exist_ok=True)
    import soundfile as sf
    sf.write(path, data.astype(np.float32), SR, format="OGG", subtype="VORBIS")
    print("sound:", os.path.relpath(path, ROOT))


def gen_whisper():
    rng = np.random.default_rng(11)
    dur = 3.0
    n = int(SR * dur)
    base = rng.standard_normal(n)
    # crude formant sweep to imitate speech
    out = np.zeros(n)
    for k in range(6):
        seg = np.zeros(n)
        start = int(n * (0.06 + 0.14 * k))
        length = int(SR * (0.25 + 0.1 * rng.random()))
        if start + length >= n:
            break
        noise = rng.standard_normal(length)
        cutoff = 0.03 + 0.02 * rng.random()
        seg[start:start + length] = lowpass(noise, cutoff) * env(length, 0.05, 0.4)
        out += seg
    out = hp(out) * 0.5 + out * 0.4
    mod = 0.6 + 0.4 * np.sin(np.linspace(0, 18 * np.pi, n))
    write_sound("whisper", out * mod, 0.6)


def gen_static():
    rng = np.random.default_rng(12)
    n = int(SR * 2.5)
    x = rng.standard_normal(n) * 0.5
    crack = (rng.random(n) < 0.0015) * rng.standard_normal(n) * 3.0
    x = x + crack
    # bitcrush
    x = np.round(x * 12) / 12
    write_sound("static", x, 0.65)


def gen_stinger():
    rng = np.random.default_rng(13)
    dur = 3.2
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    x = np.zeros(n)
    for f in (52.0, 55.5, 58.0, 110.0):
        x += np.sin(2 * np.pi * f * t) * 0.3
    sweep = np.sin(2 * np.pi * (900 * np.exp(-t / 1.2)) * t) * 0.25
    x += sweep
    noise = rng.standard_normal(n) * 0.15
    swell = np.exp(-((t - 0.15) ** 2) / 0.05)
    x = x * np.exp(-t / 1.6) + noise * swell
    write_sound("stinger", x, 0.9)


def gen_heartbeat():
    n = int(SR * 1.5)
    t = np.linspace(0, 1.5, n)
    x = np.zeros(n)
    for start, amp in ((0.05, 1.0), (0.32, 0.65)):
        tt = t - start
        beat = np.sin(2 * np.pi * 48 * tt) * np.exp(-np.clip(tt, 0, None) * 18) * (tt > 0)
        x += beat * amp
    x += np.sin(2 * np.pi * 96 * t) * np.exp(-np.clip(t, 0, None) * 24) * 0.2
    write_sound("heartbeat", x, 0.7)


def gen_cubemap():
    rng = np.random.default_rng(15)
    dur = 3.5
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    x = np.sin(2 * np.pi * 41 * t) * 0.5 + np.sin(2 * np.pi * 61.5 * t) * 0.3
    x *= 0.4 + 0.6 * (np.sin(2 * np.pi * 2.2 * t) > 0)
    blips = np.zeros(n)
    for i in range(14):
        idx = int(n * rng.random() * 0.9)
        length = int(SR * 0.04)
        if idx + length < n:
            blips[idx:idx + length] += np.sin(2 * np.pi * (1200 + 800 * rng.random()) *
                                              np.linspace(0, 0.04, length)) * 0.25
    x = x + blips + rng.standard_normal(n) * 0.03
    write_sound("cubemap", x, 0.7)


def gen_terminal():
    dur = 1.6
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    x = np.sin(2 * np.pi * 60 * t) * 0.4 + np.sin(2 * np.pi * 120 * t) * 0.2
    x += np.sin(2 * np.pi * 180 * t) * 0.1
    x *= 0.7 + 0.3 * np.sin(2 * np.pi * 3 * t)
    write_sound("terminal", x, 0.5)


def gen_umbrella():
    rng = np.random.default_rng(17)
    dur = 1.8
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    noise = rng.standard_normal(n)
    swoosh = lowpass(noise, 0.08) * np.exp(-((t - 0.5) ** 2) / 0.12)
    click = np.sin(2 * np.pi * 320 * t) * np.exp(-((t - 0.95) ** 2) / 0.0015)
    x = swoosh * 1.2 + click * 0.5
    write_sound("umbrella", x, 0.7)


def gen_interloper_ambient():
    rng = np.random.default_rng(18)
    dur = 2.4
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    breath = lowpass(rng.standard_normal(n), 0.02) * (0.5 + 0.5 * np.sin(2 * np.pi * 0.8 * t))
    x = breath * 1.4 + np.sin(2 * np.pi * 38 * t) * 0.2
    write_sound("interloper_ambient", x, 0.6)


def gen_interloper_hurt():
    rng = np.random.default_rng(19)
    dur = 1.1
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    growl = np.sin(2 * np.pi * (70 - 30 * t) * t) * 0.6
    x = np.tanh((growl + rng.standard_normal(n) * 0.3) * 3.0) * np.exp(-t / 0.6)
    write_sound("interloper_hurt", x, 0.8)


def gen_interloper_death():
    rng = np.random.default_rng(20)
    dur = 3.4
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    sweep = np.sin(2 * np.pi * (300 * np.exp(-t / 1.1)) * t) * 0.4
    noise = lowpass(rng.standard_normal(n), 0.05)
    x = (sweep + noise) * np.exp(-t / 1.8)
    write_sound("interloper_death", x, 0.8)


def gen_capture():
    rng = np.random.default_rng(21)
    dur = 2.6
    n = int(SR * dur)
    t = np.linspace(0, dur, n)
    x = np.sin(2 * np.pi * (110 - 70 * t / dur) * t) * 0.5
    x += np.sin(2 * np.pi * 233 * t) * 0.25
    x *= np.exp(-t / 1.4)
    hit = rng.standard_normal(n) * np.exp(-((t - 0.02) ** 2) / 0.002) * 0.8
    x = np.tanh((x + hit) * 2.2)
    write_sound("capture", x, 0.95)


def main():
    ensure(TEX, SOUNDS)
    interloper_texture()
    interloper_eyes_texture()
    skyghost_texture()
    demo_tape_texture()
    skybox_fragment_texture()
    anomaly_scanner_texture()
    umbrella_texture()
    engine_terminal_texture()
    noise_texture()
    vignette_texture()
    logo_texture()

    gen_whisper()
    gen_static()
    gen_stinger()
    gen_heartbeat()
    gen_cubemap()
    gen_terminal()
    gen_umbrella()
    gen_interloper_ambient()
    gen_interloper_hurt()
    gen_interloper_death()
    gen_capture()
    print("done")


if __name__ == "__main__":
    main()
