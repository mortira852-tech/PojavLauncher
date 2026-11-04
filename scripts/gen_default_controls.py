#!/usr/bin/env python3
"""Generates app_pojavlauncher/src/main/assets/default.json
Touch layout (landscape), modeled on a mobile-style scheme:
  - tap anywhere: look / attack / use (handled by the touch pad, no button needed)
  - bottom-left: movement joystick
  - bottom-center: hotbar (tap handled by HotbarView) + inventory button at its right end
  - right: jump / sprint / sneak
  - top-center: camera (F5), chat, menu (Esc)
"""
import json, os

OUT = os.path.join(os.path.dirname(__file__), "..", "app_pojavlauncher", "src", "main", "assets", "default.json")

KEY_SPACE, KEY_T, KEY_E = 32, 84, 69
KEY_ESC, KEY_LSHIFT, KEY_LCTRL, KEY_F5 = 256, 340, 341, 294

def base(name, key, x, y, w, h, toggle=False, radius=25):
    return {
        "bgColor": 1711276032,  # ~40% black
        "cornerRadius": radius,
        "dynamicX": x, "dynamicY": y,
        "height": h, "width": w,
        "isDynamicBtn": False, "isHideable": False,
        "isSwipeable": False, "isToggle": toggle,
        "keycodes": [key, 0, 0, 0],
        "name": name, "opacity": 1,
        "passThruEnabled": False,
        "strokeColor": -1426063361,  # translucent white
        "strokeWidth": 2,
    }

TOP_Y = "${margin}"
CX = "${screen_width} / 2"
buttons = [
    # top-center: camera / chat / menu
    base("F5",   KEY_F5,  f"{CX} - ${{width}} * 1.5 - ${{margin}}", TOP_Y, 36, 36),
    base("Chat", KEY_T,   f"{CX} - ${{width}} / 2",                  TOP_Y, 36, 36),
    base("Menu", KEY_ESC, f"{CX} + ${{width}} / 2 + ${{margin}}",    TOP_Y, 36, 36),
    # right side: jump (top), sprint (middle-left), sneak (bottom)
    base("▲", KEY_SPACE,  "${right} - ${margin} * 3 - ${width}", "${screen_height} / 2 - ${height} - ${margin}", 44, 44),
    base("»", KEY_LCTRL,  "${right} - ${margin} * 6 - ${width} * 2", "${screen_height} / 2 - ${height} / 2", 44, 44, toggle=True),
    base("▼", KEY_LSHIFT, "${right} - ${margin} * 3 - ${width}", "${screen_height} / 2 + ${margin}", 44, 44, toggle=True),
    # inventory at the right end of the hotbar
    base("...", KEY_E, f"{CX} + px(115)", "${bottom} - ${margin}", 40, 40),
]

joystick = base("Move", 0, "${margin} * 2", "${bottom} - ${margin} * 2 - ${height}", 110, 110, radius=50)
joystick.update({"forwardLock": True, "absolute": False, "keycodes": [0, 0, 0, 0]})

data = {
    "mControlDataList": buttons,
    "mDrawerDataList": [],
    "mJoystickDataList": [joystick],
    "scaledAt": 100.0,
    "version": 8,
}
with open(OUT, "w", encoding="utf-8") as f:
    json.dump(data, f, indent=2, ensure_ascii=False)
print("wrote", os.path.normpath(OUT))
