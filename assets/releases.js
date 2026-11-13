/*
 * Populates the "Stable" and "Beta" cards on the CanReg5 GitHub Pages site
 * using the public GitHub Releases API. If the API is unreachable (offline,
 * rate-limited), the cards keep their static fallback links.
 */
(function () {
  'use strict';

  var REPO = 'IARC-CSU/CanReg5';
  var API = 'https://api.github.com/repos/' + REPO + '/releases?per_page=50';

  // Known release assets, in display order. Matched case-insensitively by exact file name.
  var ASSETS = [
    { name: 'CanReg5-Setup.zip', label: 'Windows installer' },
    { name: 'CanReg5-with-R-Setup.zip', label: 'Windows installer (with R)' },
    { name: 'CanReg5-Windows.zip', label: 'Windows (portable zip)' },
    { name: 'CanReg5-macOS.zip', label: 'macOS' },
    { name: 'CanReg5-Linux.zip', label: 'Linux' },
    { name: 'CanReg5-Instructions.pdf', label: 'Instructions (PDF)' }
  ];

  document.getElementById('year').textContent = new Date().getFullYear();

  function field(card, name) {
    return card.querySelector('[data-field="' + name + '"]');
  }

  function formatDate(iso) {
    try {
      return new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
    } catch (e) {
      return iso.substring(0, 10);
    }
  }

  function formatSize(bytes) {
    if (!bytes) { return ''; }
    var mb = bytes / (1024 * 1024);
    return mb >= 1 ? mb.toFixed(0) + ' MB' : Math.max(1, Math.round(bytes / 1024)) + ' kB';
  }

  function fill(card, release) {
    field(card, 'version').textContent = release.name || release.tag_name;
    field(card, 'date').textContent = 'Published ' + formatDate(release.published_at);
    field(card, 'page').href = release.html_url;

    var container = field(card, 'downloads');
    container.textContent = '';
    var byName = {};
    (release.assets || []).forEach(function (a) { byName[a.name.toLowerCase()] = a; });

    ASSETS.forEach(function (spec) {
      var asset = byName[spec.name.toLowerCase()];
      if (!asset) { return; }
      var a = document.createElement('a');
      a.className = 'download';
      a.href = asset.browser_download_url;
      a.textContent = spec.label;
      var size = document.createElement('span');
      size.className = 'size';
      size.textContent = formatSize(asset.size);
      a.appendChild(size);
      container.appendChild(a);
    });

    if (!container.children.length) {
      var p = document.createElement('p');
      p.className = 'muted';
      p.textContent = 'Downloads are being built – see the release page.';
      container.appendChild(p);
    }
  }

  function unavailable(card, message) {
    field(card, 'version').textContent = message;
    field(card, 'date').textContent = '';
  }

  var stableCard = document.getElementById('stable');
  var betaCard = document.getElementById('beta');

  fetch(API, { headers: { 'Accept': 'application/vnd.github+json' } })
    .then(function (r) {
      if (!r.ok) { throw new Error('HTTP ' + r.status); }
      return r.json();
    })
    .then(function (releases) {
      // Only consider CanReg5 application releases (tags starting with "v"), not helper tags such as r-runtime-v1.
      releases = releases.filter(function (r) { return !r.draft && /^v\d/.test(r.tag_name); });
      releases.sort(function (a, b) { return new Date(b.published_at) - new Date(a.published_at); });

      var stable = releases.filter(function (r) { return !r.prerelease; })[0];
      var beta = releases.filter(function (r) { return r.prerelease; })[0];

      if (stable) { fill(stableCard, stable); } else { unavailable(stableCard, 'No stable release found'); }

      if (beta && (!stable || new Date(beta.published_at) > new Date(stable.published_at))) {
        fill(betaCard, beta);
      } else {
        unavailable(betaCard, 'No newer pre-release available');
        betaCard.classList.add('inactive');
      }
    })
    .catch(function () {
      unavailable(stableCard, 'See latest release on GitHub');
      unavailable(betaCard, 'See all releases on GitHub');
    });
})();

