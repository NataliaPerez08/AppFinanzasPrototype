#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACKAGE="com.appfinanzas.prototype"
ACTIVITY="${PACKAGE}/.MainActivity"
RESULTS_DIR="${ROOT_DIR}/build/device-test-results/$(date +%Y%m%d-%H%M%S)"
ADB=(adb)

mkdir -p "$RESULTS_DIR"

log() { printf '[device-test] %s\n' "$*"; }
fail() { log "FAIL: $*"; collect_diagnostics; exit 1; }

collect_diagnostics() {
    "${ADB[@]}" logcat -d > "$RESULTS_DIR/logcat.txt" 2>/dev/null || true
    "${ADB[@]}" exec-out screencap -p > "$RESULTS_DIR/failure.png" 2>/dev/null || true
    "${ADB[@]}" shell uiautomator dump /sdcard/device_smoke_failure.xml >/dev/null 2>&1 || true
    "${ADB[@]}" exec-out cat /sdcard/device_smoke_failure.xml > "$RESULTS_DIR/failure.xml" 2>/dev/null || true
}

device_count="$(adb devices | awk 'NR > 1 && $2 == "device" {print $1}' | wc -l | tr -d ' ')"
[[ "$device_count" == "1" ]] || fail "se requiere exactamente un dispositivo físico conectado"

SERIAL="$(adb devices | awk 'NR > 1 && $2 == "device" {print $1}')"
[[ "$SERIAL" != emulator-* ]] || fail "se detectó un emulador: $SERIAL"
ADB=(adb -s "$SERIAL")

transport="$("${ADB[@]}" get-state 2>/dev/null)"
[[ "$transport" == "device" ]] || fail "ADB no está listo"
model="$("${ADB[@]}" shell getprop ro.product.model | tr -d '\r')"
sdk="$("${ADB[@]}" shell getprop ro.build.version.sdk | tr -d '\r')"
log "dispositivo físico: $model, API $sdk, serial $SERIAL"

dump_ui() {
    local name="$1"
    "${ADB[@]}" shell uiautomator dump /sdcard/device_smoke.xml >/dev/null 2>&1 || fail "no se pudo obtener UI: $name"
    "${ADB[@]}" exec-out cat /sdcard/device_smoke.xml > "$RESULTS_DIR/${name}.xml" 2>/dev/null || fail "no se pudo guardar UI: $name"
}

has_text() {
    local text="$1"
    grep -Fq "text=\"$text\"" "$RESULTS_DIR/current.xml"
}

require_text() {
    local text="$1"
    has_text "$text" || fail "no se encontró texto: $text"
}

tap_text() {
    local text="$1"
    local bounds
    bounds="$(python3 - "$RESULTS_DIR/current.xml" "$text" <<'PY'
import re, sys
path, wanted = sys.argv[1:]
xml = open(path, encoding="utf-8").read()
match = re.search(r'text="' + re.escape(wanted) + r'"[^>]*bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]"', xml)
if not match:
    match = re.search(r'bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]"[^>]*text="' + re.escape(wanted) + r'"', xml)
if not match:
    sys.exit(1)
x1, y1, x2, y2 = map(int, match.groups())
print((x1 + x2) // 2, (y1 + y2) // 2)
PY
)" || fail "no se pudo tocar: $text"
    read -r x y <<< "$bounds"
    "${ADB[@]}" shell input tap "$x" "$y"
    sleep 0.7
}

tap_label_field() {
    local label="$1"
    local bounds
bounds="$(python3 - "$RESULTS_DIR/current.xml" "$label" <<'PY'
import re, sys
xml = open(sys.argv[1], encoding="utf-8").read()
wanted = sys.argv[2].strip().lower()
for node in re.findall(r'<node[^>]+>', xml):
    content = re.search(r'content-desc="([^"]*)"', node)
    clazz = re.search(r'class="([^"]*)"', node)
    bounds = re.search(r'bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]"', node)
    if content and clazz and bounds and clazz.group(1) == "android.widget.EditText" and content.group(1).lower() == wanted:
        x1, y1, x2, y2 = map(int, bounds.groups())
        print((x1 + x2) // 2, (y1 + y2) // 2)
        sys.exit(0)
label = re.escape(sys.argv[2])
match = re.search(r'text="' + label + r'"[^>]*bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]"', xml)
if not match:
    sys.exit(1)
x1, y1, x2, y2 = map(int, match.groups())
print((x1 + x2) // 2, y2 + 45)
PY
)" || fail "no se pudo localizar campo: $label"
    read -r x y <<< "$bounds"
    "${ADB[@]}" shell input tap "$x" "$y"
    sleep 0.3
    "${ADB[@]}" shell input keyevent 123
    for _ in {1..40}; do "${ADB[@]}" shell input keyevent 67; done
    "${ADB[@]}" shell input text "$2"
    "${ADB[@]}" shell input keyevent 4
    sleep 0.5
}

refresh_ui() {
    dump_ui "$1"
    cp "$RESULTS_DIR/$1.xml" "$RESULTS_DIR/current.xml"
}

step() {
    log "$1"
}

step "build final"
./gradlew :app:assembleDebug --console=plain -q
step "tests unitarios"
./gradlew :app:testDebugUnitTest --console=plain -q
step "tests instrumentados en dispositivo físico"
./gradlew :app:connectedDebugAndroidTest --console=plain -q

step "instalación limpia"
"${ADB[@]}" install -r "$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk" >/dev/null
"${ADB[@]}" shell pm clear "$PACKAGE" >/dev/null
"${ADB[@]}" logcat -c
"${ADB[@]}" shell am start -n "$ACTIVITY" >/dev/null
sleep 3

step "Dashboard Empty"
refresh_ui dashboard_empty
require_text "NO HAY INVERSIONES"
require_text "+ AGREGAR INVERSIÓN"

step "crear inversión"
tap_text "+ AGREGAR INVERSIÓN"
refresh_ui add_investment
tap_text "ACCIÓN"
tap_text "GBM"
tap_label_field "SÍMBOLO / NOMBRE" "VOO"
refresh_ui add_investment_symbol
tap_label_field "SÍMBOLO" "VOO"
refresh_ui add_investment_currency
"${ADB[@]}" shell input swipe 360 1200 360 500 400
sleep 0.8
refresh_ui add_investment_currency_options
tap_text "MXN"
refresh_ui add_investment_value
tap_label_field "VALOR INICIAL" "10000"
"${ADB[@]}" shell input swipe 360 1200 360 500 400
sleep 0.8
refresh_ui add_investment_save
tap_text "GUARDAR INVERSIÓN"
sleep 2
refresh_ui investment_detail
require_text "VOO"
require_text "DEPOSITO"

step "Dashboard Content"
tap_text "INICIO"
refresh_ui dashboard_content
require_text 'VALOR ACTUAL'
require_text '$10,000.00 MXN'

step "registrar compra y verificar reacción Room"
tap_text "INVERSIONES"
refresh_ui investments
tap_text "VOO"
refresh_ui detail_before_transaction
tap_text "REGISTRAR MOVIMIENTO"
refresh_ui transaction_form
tap_text "COMPRA"
refresh_ui transaction_compra
tap_label_field "CANTIDAD" "2"
refresh_ui transaction_price
tap_label_field "PRECIO MXN" "100"
refresh_ui transaction_commission
tap_label_field "COMISIÓN MXN" "1"
"${ADB[@]}" shell input swipe 360 1200 360 500 400
sleep 0.8
refresh_ui transaction_save
tap_text "GUARDAR MOVIMIENTO"
sleep 2
refresh_ui detail_after_transaction
require_text "3.00"
require_text '$201.00 MXN'

step "Patrimonio y distribuciones"
tap_text "PATRIMONIO"
refresh_ui portfolio
require_text "POR TIPO"
require_text "POR INSTITUCIÓN"
require_text "POR MONEDA"

step "Proyección y escenarios"
tap_text "PROYECCIÓN"
refresh_ui projection
require_text "VALOR ACTUAL"
require_text "VALOR NOMINAL"
require_text "DESPUÉS DE ISR"
require_text "VALOR REAL"
require_text "CONSERVADOR"
require_text "BASE"
require_text "OPTIMISTA"

step "Configuración persistente"
tap_text "MÁS"
refresh_ui more
tap_text "CONFIGURACIÓN"
refresh_ui settings
require_text "RENDIMIENTO ESPERADO"
require_text "GUARDAR"

step "Instituciones administrables"
tap_text "MÁS"
refresh_ui more_institutions
tap_text "INSTITUCIONES"
refresh_ui institutions
require_text "+ AGREGAR INSTITUCIÓN"
tap_text "+ AGREGAR INSTITUCIÓN"
refresh_ui institution_form
tap_label_field "NOMBRE" "Klar"
refresh_ui institution_kind
tap_label_field "TIPO" "SOFIPO"
refresh_ui institution_save
tap_text "GUARDAR INSTITUCIÓN"
sleep 1
refresh_ui institutions_after_add
require_text "Klar"

step "revisión de crashes y ANR"
if "${ADB[@]}" logcat -d | grep -qE "FATAL EXCEPTION|ANR in ${PACKAGE}|Process .* has died"; then
    collect_diagnostics
    fail "se detectó crash o ANR"
fi
"${ADB[@]}" shell pidof "$PACKAGE" >/dev/null || fail "el proceso de la app no está vivo"

"${ADB[@]}" logcat -d > "$RESULTS_DIR/logcat.txt"
log "PASS: smoke test completo. Evidencias: $RESULTS_DIR"
