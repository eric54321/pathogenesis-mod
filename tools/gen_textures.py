"""
Generates detailed entity textures for Pathogenesis mod.
Each texture is painted face-by-face using the UV layout derived from the Java model files.

UV layout formula for a cuboid (W wide, H tall, D deep) at UV offset (u, v):
  Top:    x=[u+D, u+D+W),  y=[v,   v+D)
  Bottom: x=[u+D+W, u+D+2W), y=[v, v+D)
  Right:  x=[u,    u+D),   y=[v+D, v+D+H)
  Front:  x=[u+D,  u+D+W), y=[v+D, v+D+H)
  Left:   x=[u+D+W, u+2D+W), y=[v+D, v+D+H)
  Back:   x=[u+2D+W, u+2D+2W), y=[v+D, v+D+H)
"""
import sys, os, random
try:
    from PIL import Image
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image

random.seed(42)
OUT = r"C:\Eric-new\code\github\pathogenesis-mod\src\main\resources\assets\pathogenesis\textures\entity"

# ── helpers ────────────────────────────────────────────────────────────────

def new(w, h):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))

def fill(im, x1, y1, x2, y2, r, g, b, noise=10):
    px = im.load()
    for x in range(x1, x2):
        for y in range(y1, y2):
            px[x, y] = (
                max(0, min(255, r + random.randint(-noise, noise))),
                max(0, min(255, g + random.randint(-noise, noise))),
                max(0, min(255, b + random.randint(-noise, noise))),
                255
            )

def radial(im, x1, y1, x2, y2, hi=35, shadow=20):
    """Add highlight at centre and shadow at edges."""
    px = im.load()
    cx, cy = (x1 + x2) / 2.0, (y1 + y2) / 2.0
    mr = max(x2 - x1, y2 - y1) / 2.0
    for x in range(x1, x2):
        for y in range(y1, y2):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            f = d / mr if mr else 0
            r, g, b, a = px[x, y]
            adj = int(hi * (1 - f)) - int(shadow * f)
            px[x, y] = (max(0, min(255, r+adj)), max(0, min(255, g+adj)), max(0, min(255, b+adj)), a)

def hlines(im, x1, y1, x2, y2, r, g, b, step=3):
    """Dark horizontal segment lines."""
    px = im.load()
    for y in range(y1, y2):
        if (y - y1) % step == 0:
            for x in range(x1, x2):
                px[x, y] = (r, g, b, 255)

def dots(im, x1, y1, x2, y2, r, g, b, freq=0.08):
    px = im.load()
    for x in range(x1, x2):
        for y in range(y1, y2):
            if random.random() < freq:
                px[x, y] = (r, g, b, 255)

def poke(im, x, y, r, g, b):
    """Set single pixel if in bounds."""
    if 0 <= x < im.width and 0 <= y < im.height:
        im.load()[x, y] = (r, g, b, 255)

# shorthand: paint a face with a colour shift for lighting
def face(im, x1, y1, x2, y2, r, g, b, lr=0, noise=10):
    fill(im, x1, y1, x2, y2, r+lr, g+lr, b+lr, noise)

# ── 1. STAPH — golden sphere, 32×32  ───────────────────────────────────────
# Body 8×8×8 at UV(0,0)
def make_staph():
    im = new(32, 32)
    R, G, B = 228, 175, 42
    # Top  [8,16)×[0,8)   lighter
    face(im,  8, 0, 16,  8, R, G, B, lr=25)
    # Bottom [16,24)×[0,8) darker
    face(im, 16, 0, 24,  8, R, G, B, lr=-35)
    # Right [0,8)×[8,16)
    face(im,  0, 8,  8, 16, R, G, B, lr=-12)
    radial(im, 0, 8, 8, 16, hi=15, shadow=15)
    # Front [8,16)×[8,16)  — most visible, full radial shading + speckle
    face(im,  8, 8, 16, 16, R, G, B)
    radial(im, 8, 8, 16, 16, hi=40, shadow=20)
    dots(im,  8, 8, 16, 16, R-65, G-55, B-20, freq=0.06)
    # Left [16,24)×[8,16)
    face(im, 16, 8, 24, 16, R, G, B, lr=-12)
    radial(im, 16, 8, 24, 16, hi=15, shadow=15)
    # Back [24,32)×[8,16)
    face(im, 24, 8, 32, 16, R, G, B, lr=-18)
    im.save(os.path.join(OUT, "staph.png"))
    print("staph.png done")

# ── 2. STREPTOCOCCUS — crimson diplococcus, 32×32  ────────────────────────
# Both spheres 6×6×6 at UV(0,0) — shared UV
def make_strep():
    im = new(32, 32)
    R, G, B = 188, 38, 42
    # Top  [6,12)×[0,6)
    face(im,  6, 0, 12,  6, R, G, B, lr=28)
    # Bottom [12,18)×[0,6)
    face(im, 12, 0, 18,  6, R, G, B, lr=-40)
    # Right [0,6)×[6,12)
    face(im,  0, 6,  6, 12, R, G, B, lr=-12)
    radial(im, 0, 6, 6, 12, hi=15, shadow=15)
    # Front [6,12)×[6,12)
    face(im,  6, 6, 12, 12, R, G, B)
    radial(im, 6, 6, 12, 12, hi=50, shadow=20)
    # Chain division line down column 9 — shows the two-cell chain
    for y in range(6, 12):
        poke(im, 9, y, max(0, R-65), max(0, G-20), max(0, B-20))
    # Left [12,18)×[6,12)
    face(im, 12, 6, 18, 12, R, G, B, lr=-12)
    radial(im, 12, 6, 18, 12, hi=15, shadow=15)
    # Back [18,24)×[6,12)
    face(im, 18, 6, 24, 12, R, G, B, lr=-18)
    im.save(os.path.join(OUT, "streptococcus.png"))
    print("streptococcus.png done")

# ── 3. DERMATOPHYTE — fungal hyphae + cap, 32×32  ─────────────────────────
# Shaft 4×12×4 at UV(0,0) — pale beige with ring marks
# Cap   6×2×6  at UV(0,16) — dark olive/brown
def make_dermatophyte():
    im = new(32, 32)
    SR, SG, SB = 205, 182, 138   # pale tan shaft
    CR, CG, CB = 105,  88,  48   # dark brown cap

    # SHAFT
    face(im,  4, 0,  8,  4, SR, SG, SB, lr=20)   # Top
    face(im,  8, 0, 12,  4, SR, SG, SB, lr=-30)  # Bottom
    face(im,  0, 4,  4, 16, SR, SG, SB, lr=-10)  # Right
    hlines(im, 0, 4,  4, 16, SR-55, SG-45, SB-35, step=4)
    face(im,  4, 4,  8, 16, SR, SG, SB)           # Front
    hlines(im, 4, 4,  8, 16, SR-55, SG-45, SB-35, step=4)
    face(im,  8, 4, 12, 16, SR, SG, SB, lr=-10)  # Left
    hlines(im, 8, 4, 12, 16, SR-55, SG-45, SB-35, step=4)
    face(im, 12, 4, 16, 16, SR, SG, SB, lr=-15)  # Back
    hlines(im, 12, 4, 16, 16, SR-55, SG-45, SB-35, step=4)

    # CAP
    face(im,  6, 16, 12, 22, CR, CG, CB, lr=18)   # Top — lighter (spore surface)
    dots(im,  6, 16, 12, 22, CR-30, CG-25, CB-15, freq=0.08)
    face(im, 12, 16, 18, 22, CR, CG, CB, lr=-22)  # Bottom
    face(im,  0, 22,  6, 24, CR, CG, CB, lr=8)    # Right
    face(im,  6, 22, 12, 24, CR, CG, CB, lr=12)   # Front
    face(im, 12, 22, 18, 24, CR, CG, CB, lr=8)    # Left
    face(im, 18, 22, 24, 24, CR, CG, CB, lr=5)    # Back

    im.save(os.path.join(OUT, "dermatophyte.png"))
    print("dermatophyte.png done")

# ── 4. ASCARI — flesh roundworm, 64×32  ────────────────────────────────────
# Head  8×6×8 at UV(0,0)
# Rings 6×5×6 at UV(0,14)  — shared
# Tail  4×4×4 at UV(24,14)
def make_ascari():
    im = new(64, 32)
    HR, HG, HB = 195, 148, 108   # head — slightly darker
    RR, RG, RB = 215, 172, 128   # rings — lighter
    TR, TG, TB = 185, 140,  98   # tail — darkest

    # HEAD
    face(im,  8, 0, 16,  8, HR, HG, HB, lr=20)  # Top
    face(im, 16, 0, 24,  8, HR, HG, HB, lr=-32) # Bottom
    face(im,  0, 8,  8, 14, HR, HG, HB, lr=-10) # Right
    radial(im, 0, 8, 8, 14, hi=15, shadow=15)
    face(im,  8, 8, 16, 14, HR, HG, HB)          # Front
    radial(im, 8, 8, 16, 14, hi=20, shadow=15)
    for x in range(9, 15):                        # mouth bar
        poke(im, x, 13, 45, 28, 18)
    poke(im, 10, 12, 75, 48, 30)
    poke(im, 13, 12, 75, 48, 30)
    face(im, 16, 8, 24, 14, HR, HG, HB, lr=-10) # Left
    radial(im, 16, 8, 24, 14, hi=15, shadow=15)
    face(im, 24, 8, 32, 14, HR, HG, HB, lr=-15) # Back

    # RINGS (shared UV — both rings look the same)
    face(im,  6, 14, 12, 20, RR, RG, RB, lr=18)  # Top
    face(im, 12, 14, 18, 20, RR, RG, RB, lr=-28) # Bottom
    face(im,  0, 20,  6, 25, RR, RG, RB, lr=-8)  # Right
    hlines(im, 0, 20,  6, 25, RR-45, RG-35, RB-28, step=3)
    face(im,  6, 20, 12, 25, RR, RG, RB)          # Front
    hlines(im, 6, 20, 12, 25, RR-45, RG-35, RB-28, step=3)
    radial(im, 6, 20, 12, 25, hi=15, shadow=12)
    face(im, 12, 20, 18, 25, RR, RG, RB, lr=-8)  # Left
    hlines(im, 12, 20, 18, 25, RR-45, RG-35, RB-28, step=3)
    face(im, 18, 20, 24, 25, RR, RG, RB, lr=-12) # Back

    # TAIL
    face(im, 28, 14, 32, 18, TR, TG, TB, lr=18)  # Top
    face(im, 32, 14, 36, 18, TR, TG, TB, lr=-22) # Bottom
    face(im, 24, 18, 28, 22, TR, TG, TB, lr=-5)  # Right
    face(im, 28, 18, 32, 22, TR, TG, TB)          # Front
    face(im, 32, 18, 36, 22, TR, TG, TB, lr=-5)  # Left
    face(im, 36, 18, 40, 22, TR, TG, TB, lr=-10) # Back

    im.save(os.path.join(OUT, "ascari.png"))
    print("ascari.png done")

# ── 5. TAENIA — cream tapeworm, 64×32  ─────────────────────────────────────
# Scolex      8×5×6  at UV(0,0)
# Neck        6×3×5  at UV(0,11)
# Proglottids 8×4×5  at UV(0,19)  — shared
# Gravid      10×4×6 at UV(26,19)
def make_taenia():
    im = new(64, 32)
    SR, SG, SB = 232, 188, 162   # scolex — pinkish head
    NR, NG, NB = 238, 202, 178   # neck — pale
    PR, PG, PB = 242, 220, 193   # proglottids — ivory
    GR, GG, GB = 228, 205, 152   # gravid — yellower (egg-filled)

    # SCOLEX
    face(im,  6,  0, 14,  6, SR, SG, SB, lr=18)  # Top
    face(im, 14,  0, 22,  6, SR, SG, SB, lr=-32) # Bottom
    face(im,  0,  6,  6, 11, SR, SG, SB, lr=-10) # Right
    face(im,  6,  6, 14, 11, SR, SG, SB)          # Front
    radial(im, 6,  6, 14, 11, hi=22, shadow=15)
    poke(im,  7,  6, 90, 55, 40)                   # hook left
    poke(im,  7,  7, 115, 72, 55)
    poke(im, 12,  6, 90, 55, 40)                   # hook right
    poke(im, 12,  7, 115, 72, 55)
    face(im, 14,  6, 20, 11, SR, SG, SB, lr=-10) # Left
    face(im, 20,  6, 28, 11, SR, SG, SB, lr=-15) # Back

    # NECK
    face(im,  5, 11, 11, 16, NR, NG, NB, lr=12)  # Top
    face(im, 11, 11, 17, 16, NR, NG, NB, lr=-20) # Bottom
    face(im,  0, 16,  5, 19, NR, NG, NB, lr=-8)  # Right
    face(im,  5, 16, 11, 19, NR, NG, NB)          # Front
    face(im, 11, 16, 16, 19, NR, NG, NB, lr=-8)  # Left
    face(im, 16, 16, 22, 19, NR, NG, NB, lr=-12) # Back

    # PROGLOTTIDS (shared UV)
    face(im,  5, 19, 13, 24, PR, PG, PB, lr=12)  # Top
    face(im, 13, 19, 21, 24, PR, PG, PB, lr=-22) # Bottom
    face(im,  0, 24,  5, 28, PR, PG, PB, lr=-8)  # Right
    face(im,  5, 24, 13, 28, PR, PG, PB)          # Front
    # segment border line between proglottid 1 and 2
    for x in range(5, 13):
        poke(im, x, 24, PR-65, PG-55, PB-45)
    face(im, 13, 24, 18, 28, PR, PG, PB, lr=-8)  # Left
    face(im, 18, 24, 26, 28, PR, PG, PB, lr=-12) # Back

    # GRAVID
    face(im, 32, 19, 42, 25, GR, GG, GB, lr=15)  # Top
    face(im, 42, 19, 52, 25, GR, GG, GB, lr=-25) # Bottom
    face(im, 26, 25, 32, 29, GR, GG, GB, lr=-10) # Right
    face(im, 32, 25, 42, 29, GR, GG, GB)          # Front — egg dots
    dots(im, 32, 25, 42, 29, GR-65, GG-55, GB-42, freq=0.12)
    face(im, 42, 25, 48, 29, GR, GG, GB, lr=-10) # Left
    face(im, 48, 25, 58, 29, GR, GG, GB, lr=-15) # Back

    im.save(os.path.join(OUT, "taenia.png"))
    print("taenia.png done")

# ── 6. STRONGYLOIDES — threadworm, 32×32  ──────────────────────────────────
# Head 6×5×6 at UV(0,0)
# Body 3×14×3 at UV(0,11)
def make_strongyloides():
    im = new(32, 32)
    HR, HG, HB = 188, 128, 100   # head — pinkish
    BR, BG, BB = 172, 136, 106   # body — tan

    # HEAD
    face(im,  6,  0, 12,  6, HR, HG, HB, lr=22)  # Top
    face(im, 12,  0, 18,  6, HR, HG, HB, lr=-35) # Bottom
    face(im,  0,  6,  6, 11, HR, HG, HB, lr=-10) # Right
    radial(im, 0,  6,  6, 11, hi=12, shadow=15)
    face(im,  6,  6, 12, 11, HR, HG, HB)          # Front
    radial(im, 6,  6, 12, 11, hi=28, shadow=18)
    poke(im,  9, 10, 55, 32, 22)                   # mouth
    face(im, 12,  6, 18, 11, HR, HG, HB, lr=-10) # Left
    radial(im, 12, 6, 18, 11, hi=12, shadow=15)
    face(im, 18,  6, 24, 11, HR, HG, HB, lr=-15) # Back

    # BODY — narrow thread, heavy segment striping
    face(im,  3, 11,  6, 14, BR, BG, BB, lr=20)   # Top
    face(im,  6, 11,  9, 14, BR, BG, BB, lr=-22)  # Bottom
    face(im,  0, 14,  3, 28, BR, BG, BB, lr=-8)   # Right
    hlines(im, 0, 14,  3, 28, BR-38, BG-28, BB-22, step=2)
    face(im,  3, 14,  6, 28, BR, BG, BB)           # Front
    hlines(im, 3, 14,  6, 28, BR-38, BG-28, BB-22, step=2)
    face(im,  6, 14,  9, 28, BR, BG, BB, lr=-8)   # Left
    hlines(im, 6, 14,  9, 28, BR-38, BG-28, BB-22, step=2)
    face(im,  9, 14, 12, 28, BR, BG, BB, lr=-12)  # Back
    hlines(im, 9, 14, 12, 28, BR-38, BG-28, BB-22, step=2)

    im.save(os.path.join(OUT, "strongyloide.png"))
    print("strongyloide.png done")

make_staph()
make_strep()
make_dermatophyte()
make_ascari()
make_taenia()
make_strongyloides()
print("\nAll 6 textures done.")
