#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# OuterTune — Interactive Build Script
# Pilihan: ABI · Flavor · Build Type · GitHub Release tag (opsional)
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

# ── Warna & helper ────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

info()    { echo -e "${CYAN}${BOLD}[i]${RESET} $*"; }
success() { echo -e "${GREEN}${BOLD}[✓]${RESET} $*"; }
warn()    { echo -e "${YELLOW}${BOLD}[!]${RESET} $*"; }
error()   { echo -e "${RED}${BOLD}[✗]${RESET} $*" >&2; exit 1; }

menu() {
    local prompt="$1"; shift
    local options=("$@")
    echo ""
    echo -e "${BOLD}${CYAN}$prompt${RESET}"
    local i=1
    for opt in "${options[@]}"; do
        echo -e "  ${YELLOW}$i)${RESET} $opt"
        ((i++))
    done
    local choice
    while true; do
        printf "${BOLD}Pilihan [1-${#options[@]}]: ${RESET}"
        read -r choice
        if [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#options[@]} )); then
            MENU_RESULT="${options[$((choice-1))]}"
            return
        fi
        warn "Masukkan angka antara 1 dan ${#options[@]}"
    done
}

yn() {
    local prompt="$1"
    local answer
    while true; do
        printf "${BOLD}${CYAN}$prompt ${YELLOW}[y/n]${RESET}: "
        read -r answer
        case "${answer,,}" in
            y|yes) return 0 ;;
            n|no)  return 1 ;;
            *)     warn "Ketik y atau n" ;;
        esac
    done
}

# ── Root project ──────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}${CYAN}║      OuterTune — Build Script  v1.0      ║${RESET}"
echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════╝${RESET}"

# ─────────────────────────────────────────────────────────────────────────────
# 1. BUILD TYPE
# ─────────────────────────────────────────────────────────────────────────────
menu "Pilih Build Type:" \
    "Release (signed, minified)" \
    "Debug (unsigned, debuggable)" \
    "Userdebug (signed, no minify)"

case "$MENU_RESULT" in
    "Release"*)     BUILD_TYPE="release";    IS_RELEASE=true  ;;
    "Debug"*)       BUILD_TYPE="debug";      IS_RELEASE=false ;;
    "Userdebug"*)   BUILD_TYPE="userdebug";  IS_RELEASE=true  ;;
esac
info "Build type   : ${BOLD}$BUILD_TYPE${RESET}"

# ─────────────────────────────────────────────────────────────────────────────
# 2. FLAVOR (Build Variant)
# ─────────────────────────────────────────────────────────────────────────────
menu "Pilih Flavor / Variant:" \
    "Core (standard, ukuran kecil)" \
    "Full (semua fitur, FFmpeg, ukuran besar)" \
    "Mix (Core + Full — build dua-duanya)"

case "$MENU_RESULT" in
    "Core"*) FLAVORS=("core") ;;
    "Full"*) FLAVORS=("full") ;;
    "Mix"*)  FLAVORS=("core" "full") ;;
esac
info "Flavor       : ${BOLD}${FLAVORS[*]}${RESET}"

# ─────────────────────────────────────────────────────────────────────────────
# 3. ABI
# ─────────────────────────────────────────────────────────────────────────────
menu "Pilih ABI (arsitektur CPU):" \
    "arm64-v8a  (64-bit, sebagian besar HP modern)" \
    "armeabi-v7a (32-bit, HP lama)" \
    "Mix         (arm64-v8a + armeabi-v7a — build dua-duanya)"

case "$MENU_RESULT" in
    "arm64-v8a"*)   ABI_LIST=("arm64-v8a") ;;
    "armeabi-v7a"*) ABI_LIST=("armeabi-v7a") ;;
    "Mix"*)         ABI_LIST=("arm64-v8a" "armeabi-v7a") ;;
esac
info "ABI          : ${BOLD}${ABI_LIST[*]}${RESET}"

# ─────────────────────────────────────────────────────────────────────────────
# 4. RELEASE TAG (hanya untuk release & userdebug)
# ─────────────────────────────────────────────────────────────────────────────
PUSH_TAG=false
TAG_NAME=""

if $IS_RELEASE; then
    echo ""
    if yn "Push GitHub Release Tag setelah build?"; then
        PUSH_TAG=true

        # Ambil versionName dari build.gradle.kts
        RAW_VERSION=$(grep 'versionName\s*=' app/build.gradle.kts | head -1 \
            | sed 's/.*versionName\s*=\s*"\(.*\)".*/\1/')
        info "versi saat ini : ${BOLD}$RAW_VERSION${RESET}"

        printf "${BOLD}Tag name ${YELLOW}[default: v${RAW_VERSION}]${RESET}: "
        read -r TAG_INPUT
        TAG_NAME="${TAG_INPUT:-v${RAW_VERSION}}"

        printf "${BOLD}Release title ${YELLOW}[default: OuterTune ${TAG_NAME}]${RESET}: "
        read -r TITLE_INPUT
        RELEASE_TITLE="${TITLE_INPUT:-OuterTune ${TAG_NAME}}"

        printf "${BOLD}Release notes (kosongkan untuk default): ${RESET}"
        read -r RELEASE_NOTES
        RELEASE_NOTES="${RELEASE_NOTES:-Release ${TAG_NAME}}"

        IS_PRERELEASE=false
        if yn "Tandai sebagai Pre-release?"; then
            IS_PRERELEASE=true
        fi

        info "Tag          : ${BOLD}$TAG_NAME${RESET}"
        info "Title        : ${BOLD}$RELEASE_TITLE${RESET}"
        info "Pre-release  : ${BOLD}$IS_PRERELEASE${RESET}"
    fi
fi

# ─────────────────────────────────────────────────────────────────────────────
# 5. KONFIRMASI
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}${CYAN}─── Ringkasan Build ───────────────────────${RESET}"
echo -e "  Build Type  : ${BOLD}$BUILD_TYPE${RESET}"
echo -e "  Flavor(s)   : ${BOLD}${FLAVORS[*]}${RESET}"
echo -e "  ABI(s)      : ${BOLD}${ABI_LIST[*]}${RESET}"
if $PUSH_TAG; then
    echo -e "  Release Tag : ${BOLD}$TAG_NAME${RESET} (pre-release: $IS_PRERELEASE)"
fi
echo -e "${BOLD}${CYAN}───────────────────────────────────────────${RESET}"
echo ""
yn "Lanjut build?" || { warn "Build dibatalkan."; exit 0; }

# ─────────────────────────────────────────────────────────────────────────────
# 6. MODIFIKASI build.gradle.kts SESUAI ABI PILIHAN
# ─────────────────────────────────────────────────────────────────────────────
GRADLE_FILE="app/build.gradle.kts"

patch_abi() {
    local include_line
    if [[ ${#ABI_LIST[@]} -eq 1 ]]; then
        include_line="            include(\"${ABI_LIST[0]}\")"
    else
        include_line="            include(\"arm64-v8a\", \"armeabi-v7a\")"
    fi

    # Replace baris include(...) dalam blok splits { abi { ... } }
    python3 - "$GRADLE_FILE" "$include_line" <<'PYEOF'
import sys, re
path, new_include = sys.argv[1], sys.argv[2]
content = open(path).read()
# Replace the include(...) line inside splits { abi { block
content = re.sub(
    r'(splits\s*\{[^}]*abi\s*\{[^}]*?)([ \t]*include\([^)]*\))',
    lambda m: m.group(1) + new_include,
    content, flags=re.DOTALL
)
open(path, 'w').write(content)
print(f"  ABI include updated: {new_include.strip()}")
PYEOF
}

info "Mengatur ABI di build.gradle.kts..."
patch_abi

# ─────────────────────────────────────────────────────────────────────────────
# 7. BUILD
# ─────────────────────────────────────────────────────────────────────────────
APK_OUTPUTS=()

BUILD_TYPE_CAPITALIZED="$(tr '[:lower:]' '[:upper:]' <<< "${BUILD_TYPE:0:1}")${BUILD_TYPE:1}"

for flavor in "${FLAVORS[@]}"; do
    FLAVOR_CAP="$(tr '[:lower:]' '[:upper:]' <<< "${flavor:0:1}")${flavor:1}"
    TASK="assemble${FLAVOR_CAP}${BUILD_TYPE_CAPITALIZED}"

    echo ""
    info "Building: ${BOLD}$TASK${RESET}"
    ./gradlew "$TASK" --stacktrace 2>&1 | tee /tmp/outertune_build_${flavor}.log | \
        grep -E "BUILD|FAILED|error:|warning:|\.apk" || true

    # Cek hasil
    if grep -q "BUILD SUCCESSFUL" /tmp/outertune_build_${flavor}.log; then
        success "Build ${flavor}/${BUILD_TYPE} berhasil!"

        # Kumpulkan APK yang dihasilkan
        while IFS= read -r apk; do
            APK_OUTPUTS+=("$apk")
            success "  APK: $apk"
        done < <(find app/build/outputs/apk/${flavor}/${BUILD_TYPE}/ -name "*.apk" 2>/dev/null)
    else
        error "Build ${flavor}/${BUILD_TYPE} GAGAL! Cek /tmp/outertune_build_${flavor}.log"
    fi
done

# ─────────────────────────────────────────────────────────────────────────────
# 8. GITHUB RELEASE TAG (opsional)
# ─────────────────────────────────────────────────────────────────────────────
if $PUSH_TAG; then
    echo ""
    info "Membuat GitHub Release: ${BOLD}$TAG_NAME${RESET}..."

    # Pastikan gh CLI ada
    if ! command -v gh &>/dev/null; then
        warn "GitHub CLI (gh) tidak ditemukan. Install dulu: https://cli.github.com/"
        warn "APK sudah siap di: ${APK_OUTPUTS[*]}"
        exit 0
    fi

    # Buat git tag jika belum ada
    if ! git tag | grep -q "^${TAG_NAME}$"; then
        git tag -a "$TAG_NAME" -m "$RELEASE_TITLE"
        git push origin "$TAG_NAME"
        success "Git tag ${TAG_NAME} dipush ke origin"
    else
        warn "Tag ${TAG_NAME} sudah ada, skip pembuatan tag"
    fi

    # Build argumen gh release
    GH_ARGS=(
        release create "$TAG_NAME"
        --title "$RELEASE_TITLE"
        --notes "$RELEASE_NOTES"
    )
    $IS_PRERELEASE && GH_ARGS+=(--prerelease)

    # Lampirkan semua APK
    for apk in "${APK_OUTPUTS[@]}"; do
        GH_ARGS+=("$apk")
    done

    gh "${GH_ARGS[@]}" && success "GitHub Release ${TAG_NAME} berhasil dibuat!" \
        || warn "gh release gagal. Coba buat manual di GitHub."
fi

# ─────────────────────────────────────────────────────────────────────────────
echo ""
success "Selesai! APK output:"
for apk in "${APK_OUTPUTS[@]}"; do
    echo -e "  ${GREEN}→${RESET} $apk"
done
echo ""
