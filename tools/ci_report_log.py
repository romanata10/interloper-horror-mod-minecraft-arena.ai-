#!/usr/bin/env python3
"""Prints the tail of a build log as GitHub Actions error annotations.

Used by CI so build failures are visible through the API without downloading
the (large) raw log archive.
"""
import pathlib
import re
import sys

LOG = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "build.log")
CHUNK = 5000


def emit(text: str) -> None:
    text = text.replace("\r", "").replace("\n", "%0A")
    print(f"::error::{text}")


def main() -> int:
    if not LOG.exists():
        emit(f"no build log at {LOG}")
        return 0
    data = LOG.read_text(errors="replace")
    lines = data.splitlines()

    interesting = [l for l in lines if re.search(r"(?i)error|fail|exception|caused by|could not|what went wrong", l)]
    if interesting:
        emit("MATCHED LINES:%0A" + "\n".join(interesting[:60]))
    else:
        emit("NO MATCHED LINES")

    tail = "\n".join(lines[-120:])
    for i in range(0, len(tail), CHUNK):
        emit(f"TAIL PART {i // CHUNK + 1}:%0A" + tail[i:i + CHUNK])
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
