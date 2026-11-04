(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;
    var timer = null;

    function render(ok, data, ms) {
        var badge = $('#health-badge');
        badge.className = 'badge ' + (ok ? 'badge-closed' : 'badge-rejected');
        badge.textContent = ok ? 'Online' : 'Offline';

        if (!ok) {
            $('#health-result').innerHTML = '<div class="empty" style="padding:24px 12px">' +
                '<div class="empty-icon">!</div>' +
                '<h3>API unreachable</h3>' +
                '<p>' + esc((data && data.message) || 'Unknown error') + '</p>' +
            '</div>';
            return;
        }

        var time = (data && data.time) || '';
        $('#health-result').innerHTML =
            '<div class="row between" style="align-items:flex-start">' +
                '<div>' +
                    '<div class="stat-value" style="font-size:22px">' + esc((data && data.status) || 'ok') + '</div>' +
                    '<div class="hint">GET /api/health</div>' +
                '</div>' +
                '<span class="badge badge-closed">Responding</span>' +
            '</div>' +
            '<div class="divider"></div>' +
            '<dl class="kv" style="grid-template-columns:120px 1fr;gap:8px 12px">' +
                '<dt>Server time</dt><dd>' + esc(U.fmtDate(time)) + '</dd>' +
                '<dt>Latency</dt><dd>' + (ms == null ? '—' : ms + ' ms') + '</dd>' +
                '<dt>Checked at</dt><dd>' + esc(new Date().toLocaleTimeString()) + '</dd>' +
            '</dl>';
    }

    async function check(manual) {
        var btn = $('#health-refresh');
        if (manual) U.setBusy(btn, true, 'Checking…');

        var started = performance.now();
        try {
            var health = await U.api('/api/health');
            render(true, health, Math.round(performance.now() - started));
        } catch (err) {
            render(false, err, null);
        } finally {
            if (manual) U.setBusy(btn, false);
        }
    }

    $('#health-refresh').addEventListener('click', function () { check(true); });

    check(false);
    timer = setInterval(function () { check(false); }, 10000);
    window.addEventListener('beforeunload', function () { if (timer) clearInterval(timer); });
})();
