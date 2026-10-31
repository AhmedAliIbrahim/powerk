const assert = require('node:assert/strict');
const fs = require('node:fs');
const {test} = require('node:test');
const root = 'android/app/src/main/';
const theme = fs.readFileSync(root + 'java/com/powerk/app/Theme.kt', 'utf8');
const ui = fs.readFileSync(root + 'java/com/powerk/app/Ui.kt', 'utf8');
const web = fs.readFileSync('powerk.py', 'utf8');
const roots = [...web.matchAll(/:root(?:\[data-theme=dark\])?\s*\{([^}]+)\}/g)].map(match => match[1]);

test('Android shares the dashboard semantic palette in both themes', () => {
  const mapping = {Background:'bg', Surface:'surface', Subtle:'subtle', Ink:'ink', Muted:'muted', Line:'line', Border:'control-border', Accent:'accent', AccentSoft:'accent-soft', AccentInk:'accent-ink', Danger:'danger', DangerBackground:'danger-bg'};
  for (const [index, mode] of ['Light', 'Dark'].entries()) {
    for (const [android, token] of Object.entries(mapping)) {
      const color = roots[index].match(new RegExp(`--${token}:\\s*#([a-f0-9]{6})`, 'i'))[1];
      assert.match(theme, new RegExp(`val ${mode}${android} = Color\\(0xFF${color}\\)`, 'i'));
    }
  }
  assert.doesNotMatch(ui, /Color\(|0xFF[\da-f]{6}/i, 'Screens must use theme colors');
  assert.doesNotMatch(theme, /dynamic(?:Dark|Light)ColorScheme|MaterialExpressiveTheme/);
});

test('Android uses shared scales and native targets without decorative animation', () => {
  assert.match(theme, /val target = 48\.dp/);
  assert.doesNotMatch(ui, /\b\d+(?:\.\d+)?\.dp\b/, 'Screen spacing must use Space tokens');
  assert.doesNotMatch(ui, /rememberInfiniteTransition|animateFloatAsState|Animatable|animateItem|Gradient|PowerRing|MiniSwitch/);
  for (const size of [...theme.matchAll(/RoundedCornerShape\((\d+)\.dp\)/g)]) {
    assert.ok([8, 12, 16].includes(Number(size[1])));
  }
  assert.match(ui, /Switch\(checked =/);
  assert.match(ui, /contentDescription = label; stateDescription = state/);
  assert.match(ui, /liveRegion = LiveRegionMode.Polite/);
  assert.match(ui, /items\(strips, key = \{ it.mac \}\)/);
});

test('Android launch surfaces and icon match the shared neutral and blue palette', () => {
  assert.match(fs.readFileSync(root + 'res/values/colors.xml','utf8'), /window_background">#F6F7F9/);
  assert.match(fs.readFileSync(root + 'res/values-night/colors.xml','utf8'), /window_background">#111318/);
  assert.match(fs.readFileSync(root + 'res/drawable/ic_launcher_foreground.xml','utf8'), /fillColor="@color\/launcher_accent"/);
});

test('Android preserves the dashboard hierarchy and outlet card composition', () => {
  assert.match(ui, /topBar = \{\s*DashboardTopBar/);
  assert.doesNotMatch(ui, /NavigationBarItem|bottomBar =/);
  for (const label of ['ac_power', 'active_ac_label', 'ac_energy']) {
    assert.match(ui, new RegExp(`SummaryMetric\\(stringResource\\(R.string.${label}\\)`));
  }
  assert.match(ui, /DashboardGlyph\(Glyph.Socket/);
  assert.match(ui, /maxWidth >= DashboardLayout.outletPairWidth -> 2/);
  assert.match(ui, /maxWidth >= DashboardLayout.outletFourWidth -> 4/);
  assert.match(ui, /fontScale > 1.2f -> 1/);
  assert.match(ui, /color = if \(outlet.on\) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant/);
  assert.match(ui, /private fun DashboardButton[\s\S]*?shape = MaterialTheme.shapes.small/);
  assert.match(ui, /modifier = modifier.heightIn\(min = Space.target\)/);
  assert.match(ui, /Modifier.alignByBaseline\(\)/);
});
