# Powerk UI guidelines

These rules are mandatory for web dashboard changes. The implementation lives in
`powerk.py` (`PAGE`); design tokens are declared in its two `:root` CSS blocks.
Keep the dashboard self-contained, offline-capable, and free of external fonts,
icon downloads, or frontend build dependencies.

## Visual direction

- Use neutral white/slate surfaces in light mode and charcoal surfaces in dark
  mode. Keep the page, metric panels, and outlet cards neutral in both states.
- Use blue sparingly for interactive state, keyboard focus, and the primary live
  metric. Do not introduce green, additional brand accents, saturated card fills,
  decorative charts, gradients, or ornamental rings.
- Reserve red for errors and destructive actions. Turning an outlet off is a
  routine action: its button uses the standard neutral treatment.
- Show success with explicit confirmation text on a neutral surface. Never
  communicate state only through color: retain labels and switch positions.
- Preserve functional parity when redesigning. Keep every outlet control, strip
  bulk action, and existing measurement available through the real server API.
  Show model, firmware, watts, cumulative energy, voltage, current, and Wi-Fi
  signal in the strip view. Keep provisioning instructions and advanced device
  identity details expandable. Use direct functional copy, not marketing text.
- Preserve measurement precision from the API: power uses up to two decimals,
  energy three, and current two. Do not round small nonzero power readings to zero.
- Separate physical ports from confirmed control/metering channels. Show the two
  USB charging ports for the verified stock model/firmware as capability
  information, with no fake switch or zero-valued readings. Always-powered design
  is not measured live state. Label totals and history as AC-only; never count
  USB as extra metered outlets or infer USB data from an unidentified channel.

## Color tokens

Use these semantic variables. Never put literal colors inside component rules,
HTML style attributes, or JavaScript. Change tokens and both themes together;
update this document and the contrast checks when changing the palette.

| Token | Light | Dark | Purpose |
|---|---|---|---|
| `--bg` | `#f6f7f9` | `#111318` | Page background |
| `--surface` | `#ffffff` | `#191d25` | Panels and active outlet surfaces |
| `--subtle` | `#f1f3f6` | `#202631` | Inactive outlets and neutral feedback |
| `--ink` | `#17202e` | `#edf1f7` | Primary text |
| `--muted` | `#566276` | `#a9b4c4` | Secondary text |
| `--line` | `#d6dbe3` | `#343c4a` | Decorative panel dividers |
| `--control-border` | `#788496` | `#8190a6` | Control boundaries and off switch track |
| `--accent` | `#2457d6` | `#8aacff` | Focus, on switches, live metric, on-state text |
| `--accent-soft` | `#edf2ff` | `#202e4f` | Reserved for small selected controls |
| `--accent-ink` | `#2448a5` | `#bed0ff` | Text on accent-soft |
| `--danger` | `#b42318` | `#ffb4ac` | Error text |
| `--danger-bg` | `#fff1f0` | `#3b2224` | Error feedback surface |
| `--danger-border` | `#d92d20` | `#ff8b80` | Error boundary |
| `--thumb` | `#ffffff` | `#111318` | Switch thumb |

`--shadow` and `--thumb-shadow` are the only shadow tokens. Keep shadows subtle;
never use a colored glow. The muted divider `--line` is not a control boundary.

## Typography, spacing, and shape

- Use the system font stack. Use tabular numbers for live measurements, and show
  units next to values. Do not animate changing numbers.
- Type scale: **12, 14, 16, 20, 24, 28, 32, 40, 48 px**, through `--text-*`
  tokens. The minimum text size is 12 px, including metadata and mobile views.
  Default body text is 14 px; headings use 20/32 px; primary metrics use 48 px
  on desktop and 32 px on mobile.
- Weights: **400, 500, 600, 700**, through `--weight-*` tokens. Body line height
  is 1.5; headings 1.2; standalone metric values 1.1. Decorative icons may use 1.
- Spacing scale: **4, 8, 12, 16, 20, 24, 32, 40, 48, 64 px**, through
  `--space-*` tokens. Do not introduce one-off margins, padding, or gaps.
- Radii: **8 px controls, 12 px outlet cards, 16 px panels, 999 px pills**,
  through `--radius-*` tokens. Circles may use 50%.
- Literal dimensions are allowed for borders, focus outlines, breakpoints,
  content bounds, and icon/switch geometry. The socket's small holes may use
  2 px corner radii. These are geometry exceptions, not additional spacing or
  text-size tokens.
- Do not add emoji icons. Keep socket and power symbols consistent; new icons
  must use a shared inline SVG style and be hidden from assistive technology
  when adjacent text supplies the meaning.

## Controls and accessibility

- Buttons, switches, and expandable summaries have at least **44 px height**.
  Buttons have at least **44 px width**. Switch hit areas are 52 × 44 px;
  their visible tracks can be smaller. Do not shrink targets on mobile.
- Every control needs a visible label or accessible name. Switches use
  `role="switch"` and `aria-checked`. Keep separate All on and All off actions.
- Maintain a visible 3 px keyboard focus outline with 4 px offset. Do not
  replace focused controls during polling or collapse open panels.
- Text must achieve at least **4.5:1** contrast, even where large text could use
  a lower threshold. Essential control boundaries, focus rings, and switch
  thumb/track pairs must achieve at least **3:1**. Decorative dividers and
  disabled controls are exceptions; do not reduce enabled text opacity.
- Respect reduced-motion preferences. Use only `--duration-fast` (160 ms) for
  simple state transitions; no perpetual animation, pulsing, or count-up effects.
- Use semantic headings, polite live announcements, and real disabled states.
  Offline readings must say they are stale and must not count toward live power.
  Distinguish pending, confirmed, and failed commands in text.
- Keep history separate from device control. Use labeled period/device/outlet
  selects with 44 px minimum height, explicit recording/error status, and a
  semantic table. A history table may scroll inside its labeled container on
  narrow screens; it must not cause page-level overflow.
- Select options must explain the choice: include UTC bucket granularity for
  periods, a full MAC identifier for strips with potentially identical names,
  and combined versus individual AC scope. Keep these details in the closed
  control too. Do not imply live status from historical device metadata.
- Use native `select`, option values, keyboard handling, and form semantics.
  Rich option layout is progressive enhancement: gate `appearance:base-select`
  behind feature support and `(hover:hover) and (pointer:fine)`. Touch devices
  retain their native picker. Stack history filters in one column at 900 px and
  below so native labels have room. Fallback option text must read independently, with
  separators hidden only in enhanced mode. Use shared theme/spacing tokens for
  the picker, readable wrapping, a selected checkmark, and visible focus.
- Keep option nodes and selection stable during polling. Queue device metadata
  updates while the picker is open; apply them after selection, blur, or the
  next closed-picker refresh. Historical strips stay available.
- Label history buckets as UTC. Do not present the strip's lifetime counter as
  newly consumed energy, fill gaps with invented readings, or hide reset/gap
  flags. Report intervals crossing bucket boundaries separately. Historical
  devices remain selectable even when offline; history failures must not
  disable live outlet controls.

Contrast thresholds follow W3C's
[text contrast guidance](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html)
and [non-text contrast guidance](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html).
The 44 px hit target and universal 4.5:1 text rule are Powerk's own stricter
defaults; passing these checks alone does not establish full WCAG conformance.

## Responsive behavior and review

- Desktop content is at most 1120 px wide, with four outlet columns. At 760 px
  and below, use two outlet columns and stack the main power metric above the
  two secondary metrics. At 380 px and below, reduce spacing, not hit areas.
- Check 320 px, 390 px, 768 px, and desktop widths. No page-level horizontal
  overflow, overlapping labels, clipped units, or unreadable metadata.
- Check light and dark modes, no-device and offline states, mixed outlet states,
  long values/names, command progress, confirmation, and failure. Verify keyboard
  focus survives a live refresh.
- Use clearly labeled sample data for visual QA; do not toggle real devices to
  review styling. Leave existing device servers running unless their restart is
  part of the authorized task.
- Run `node --test tests/*.test.cjs` and `git diff --check`. The CI web job runs
  the interaction tests and design-system checks. For server/API changes, also
  run the Python suite and protocol selftest.
- Design-system checks enforce token usage, documented palette and scales,
  contrast pairs, minimum control sizes, reduced motion, and neutral panel
  rules. Visual review remains required; tests cannot judge hierarchy or taste.
- If a UI change needs a new design rule, update this document and the relevant
  guardrails in the same commit. Do not suppress checks to bypass the rules.

## Android implementation

- Match the dashboard's information hierarchy as well as its colors: a brand
  bar with theme/refresh/settings controls; an eyebrow, title and sync time;
  a full-width AC power card above separate active-outlet and cumulative-energy
  cards; then the strip list with a count badge. Keep settings reachable from
  the top bar instead of adding a different dashboard navigation layout.
- Strip cards use a power mark, name/model/firmware, online badge and separate
  All on / All off actions. Keep W/kWh/V/A/Wi-Fi in a compact wrapping summary
  above the outlet grid, rather than tall independent metric rows.
- At normal phone text sizes show two outlet columns, with a socket symbol and
  native switch together at the top, followed by label/state, prominent watts,
  and compact temperature/energy metadata. Use four columns when there is room,
  and one column for larger accessibility text. Off cards use the subtle surface;
  powered cards use the neutral surface and a meaningful control border.
- Dashboard action buttons explicitly use 8 dp corners and neutral text; do not
  inherit Material's default pill buttons. Numbers and smaller unit labels share
  a baseline. Keep history as a clearly labeled browser entry until native
  analytics are implemented; do not claim that shortcut is a native history view.
- Android follows the same palette through `PowerkColors` in `Theme.kt`. Do not
  use dynamic wallpaper colors or literal colors in screen components. Launch
  backgrounds and the launcher accent must match the shared palette too.
- Use system fonts and the same numeric type scale in sp. Use `Space` tokens in
  dp for layout and the 8/12/16 dp control/card/panel radii. Respect font scaling;
  outlet cards become one column on narrow layouts or with larger text.
- Native Material switches, buttons, navigation, text fields, focus behavior,
  and accessibility semantics remain native. Android touch targets are at least
  48 dp. Do not imitate HTML controls or replace switches with clickable cards.
- Keep surfaces neutral, use blue for small selected controls and live power,
  and red for errors. Keep separate All on / All off actions. Show accurate
  small readings and explicit units, device identity, firmware and online state.
- Remove decorative power rings, pulsing dots, animated numbers, splash delays,
  and screen movement. Native control feedback follows Android motion settings.
- On connection failure retain the last readings with stale labels, exclude
  them from live totals, and disable power controls. Announce command pending,
  confirmed, and failed states politely. Key strip/outlet identity across refreshes.
- Keep English/Arabic translations and RTL layout, masked tokens, HTTPS settings,
  connection testing and provisioning instructions. Saved history and CSV are
  available through the labeled browser shortcut to the dashboard.
- USB capability disclosure follows the same verified firmware restriction as
  the dashboard; never draw fake USB switches or measurements.
- Run the Android unit tests, lint and debug build plus the shared Node design
  checks. Review phone layouts, large text, RTL and both themes; never toggle
  real outlets just to test appearance.
