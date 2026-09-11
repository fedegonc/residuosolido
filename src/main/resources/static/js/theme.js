(function () {
  'use strict';
  var theme;
  try { theme = localStorage.getItem('theme'); } catch (e) {}
  if (theme !== 'light' && theme !== 'dark') {
    theme = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  document.documentElement.setAttribute('data-theme', theme);
  var meta = document.getElementById('meta-theme-color');
  if (meta) meta.content = theme === 'dark' ? '#0f1419' : '#2d6a4f';
})();
