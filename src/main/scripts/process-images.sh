#!/usr/bin/env bash
# ============================================================
#  process_images.sh
#  Recursively finds PNG files in a directory, auto-straightens
#  each one, and applies a contrast correction (whitens the page
#  background, darkens text) to clean up scanned pages, then
#  replaces the original file.
#
#  Dependencies: imagemagick
#  Install on macOS:  brew install imagemagick
#  Install on Ubuntu: sudo apt install imagemagick
#
#  Usage:
#    chmod +x process_images.sh
#    ./process_images.sh /path/to/images
# ============================================================

set -euo pipefail

# ── Arguments ────────────────────────────────────────────────
INPUT_DIR="${1:-}"

# ── Validation ───────────────────────────────────────────────
if [[ -z "$INPUT_DIR" ]]; then
  echo "Usage: $0 <input_folder>"
  exit 1
fi

if [[ ! -d "$INPUT_DIR" ]]; then
  echo "Error: Input folder not found: $INPUT_DIR"
  exit 1
fi

# ── Check dependencies ────────────────────────────────────────
# Detect ImageMagick version: prefer 'magick' (IMv7), fall back to 'convert' (IMv6)
if command -v magick &>/dev/null; then
  IM_CMD="magick"
elif command -v convert &>/dev/null; then
  IM_CMD="convert"
else
  echo "Error: ImageMagick is not installed. Please install it first."
  exit 1
fi
echo "Using ImageMagick command: $IM_CMD"

# ── Deskew settings ───────────────────────────────────────────
# Threshold: 40% works well for most scanned/photographed docs.
# Raise it (e.g. 80%) if only severely skewed images should be corrected.
DESKEW_THRESHOLD=40

# ── Contrast settings ──────────────────────────────────────────
# -sigmoidal-contrast applies a smooth S-curve (not a hard clip): tones
# get pulled toward black or white based on how far they already are from
# SIGMOID_MIDPOINT, with SIGMOID_STRENGTH controlling how aggressively.
# This whitens page background and darkens text like a linear -level
# would, but preserves the gray gradient at glyph edges instead of
# flattening it — a plain -level (or a .cube LUT, which barely moves
# these images at all since most pixels sit near its near-identity
# black/white corners) hard-clips that gradient and makes antialiased
# text look bold/pixelated.
#
# Applied to the Lightness channel only (via HSL), not R/G/B directly:
# some pages are plain grayscale scans, others are color photos where
# independent per-channel contrast would shift the color balance (e.g. a
# warm lighting cast). Lightness-only adjustment affects grayscale content
# exactly the same way (identical result, since H/S carry no information
# there) while leaving hue and saturation untouched on pages that do carry
# real color (diagrams, highlighter, colored markings).
#
# SIGMOID_STRENGTH — how aggressively tones are pushed toward black/white.
#   Range: 0 and up (unitless). 0 = no change (identity, e.g. matches the
#   unprocessed scan exactly). Practical usable range is ~3-10:
#     3  -> mild, background/text barely more separated than the original
#     7  -> current default, clearly darkens text while keeping edges
#           looking hand-scanned rather than printed
#     10 -> visibly stronger blacks, edges just starting to feel bolder
#   Above ~12-15 the curve gets steep enough that it starts to look like
#   the old hard -level clip again — glyphs get visibly bolder/heavier
#   and antialiasing softness is lost, even though the curve is
#   technically still smooth. Don't go past ~15 for this kind of content.
#
# SIGMOID_MIDPOINT — the tone (as a %, 0-100) that the curve pivots
#   around; pixels darker than this get pulled toward black, pixels
#   lighter get pulled toward white, and the pivot tone itself is left
#   unchanged. 50 (the default) is the symmetric/typical choice.
#     lower (e.g. 30)  -> pivot sits down in the shadows, so most of the
#                         page (which is light) falls on the "push toward
#                         white" side: background gets brighter/cleaner,
#                         but text darkens less (weaker effect on text)
#     higher (e.g. 90) -> pivot sits up in the midtones/highlights, so
#                         more of the range falls on the "push toward
#                         black" side: text gets noticeably darker/bolder,
#                         at the cost of some risk of also dimming
#                         near-white background if pushed too high
SIGMOID_STRENGTH=8
SIGMOID_MIDPOINT=90

# ── Temp file ────────────────────────────────────────────────
TMP_OUT="$(mktemp /tmp/img_proc_XXXXXX.png)"
trap 'rm -f "$TMP_OUT"' EXIT

# ── Collect all PNG files recursively ─────────────────────────
FILES=()
while IFS= read -r -d '' f; do
  FILES+=("$f")
done < <(find "$INPUT_DIR" -type f \( -iname "*.png" \) -print0)

if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "No PNG files found in: $INPUT_DIR"
  exit 0
fi

TOTAL=${#FILES[@]}
COUNT=0
ERRORS=0

echo "Found $TOTAL PNG file(s). Processing in-place..."
echo "────────────────────────────────────────"

for INPUT_FILE in "${FILES[@]}"; do
  COUNT=$((COUNT + 1))
  # Show path relative to input dir for readability
  REL_PATH="${INPUT_FILE#${INPUT_DIR%/}/}"
  echo "[$COUNT/$TOTAL] $REL_PATH"

  # ── Auto-orient + Deskew + Contrast (lightness-only) ─────────
  # -auto-orient              : corrects rotation from camera EXIF metadata
  # -deskew %                  : detects and corrects document/photo tilt
  # +repage                    : resets canvas offsets after deskew crop
  # -colorspace HSL            : split into Hue/Saturation/Lightness
  # -channel B -sigmoidal-... : smooth contrast curve on Lightness (B slot)
  # -colorspace sRGB           : convert back for saving
  # -strip                     : drop old ICC/EXIF profiles (see note below)
  #
  # -strip matters here, not just for file size: some source scans carry
  # a grayscale ICC profile (e.g. "Generic Gray Gamma 2.2"), and on some
  # images the HSL round-trip's floating-point rounding leaves a handful
  # of pixels with R/G/B off by 1, which flips ImageMagick's PNG writer
  # from Grayscale to Truecolor mode while still copying over the old
  # grayscale-tagged profile — an invalid combination that libpng warns
  # about on every future read of that file ("iCCP ... Gray color space
  # not permitted on RGB PNG"). -strip removes the stale profile so the
  # output is always clean; verified this doesn't touch pixel data.
  if ! $IM_CMD "$INPUT_FILE" \
      -auto-orient \
      -deskew "${DESKEW_THRESHOLD}%" \
      +repage \
      -colorspace HSL \
      -channel B -sigmoidal-contrast "${SIGMOID_STRENGTH}x${SIGMOID_MIDPOINT}%" +channel \
      -colorspace sRGB \
      -strip \
      "$TMP_OUT"; then
    echo "  ✗ Processing failed, skipping."
    ERRORS=$((ERRORS + 1))
    continue
  fi

  # ── Replace original file ────────────────────────────────────
  mv "$TMP_OUT" "$INPUT_FILE"

done

echo "────────────────────────────────────────"
echo "Done! $((TOTAL - ERRORS))/$TOTAL file(s) processed in-place."
[[ $ERRORS -gt 0 ]] && echo "Skipped $ERRORS file(s) due to errors."
