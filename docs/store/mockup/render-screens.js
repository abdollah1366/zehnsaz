// Renders each mock-up screen in screens.html to a 1080x1920 PNG (360x640 at 3x).
// Usage: node docs/store/mockup/render-screens.js   (needs Playwright + Chromium)
const { chromium } = require('playwright');
const path = require('path');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 420, height: 720 }, deviceScaleFactor: 3 });
  await page.goto('file://' + path.join(__dirname, 'screens.html'));
  await page.evaluate(() => document.fonts.ready);
  const shots = ['home', 'chapters', 'attention', 'profile', 'assessment'];
  for (let i = 0; i < shots.length; i++) {
    const out = path.join(__dirname, '..', 'screenshots', `${i + 1}-${shots[i]}.png`);
    await page.locator('#' + shots[i]).screenshot({ path: out });
    console.log(out);
  }
  await browser.close();
})();
