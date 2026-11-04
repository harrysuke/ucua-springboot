(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;

    function emptyState(title, text) {
        return '<div class="empty"><div class="empty-icon">◌</div><h3>' + esc(title) + '</h3><p>' + esc(text) + '</p></div>';
    }

    function statCard(label, value, color, hint) {
        return '<div class="stat" style="--accent:' + color + '">' +
            '<div class="stat-label">' + esc(label) + '</div>' +
            '<div class="stat-value">' + (value == null ? 0 : value) + '</div>' +
            '<div class="stat-hint">' + esc(hint) + '</div>' +
        '</div>';
    }

    function renderWelcome(me) {
        var name = me && me.name ? me.name.split(' ')[0] : 'there';
        var role = me ? (me.roleName || U.roleLabel(me.accessLevel)) : '';
        $('#welcome').textContent = 'Welcome back, ' + name + (role ? ' · ' + role : '') + '. Here is the latest safety picture.';
    }

    function renderStats(counts) {
        var c = counts || {};
        $('#stats').innerHTML = [
            statCard('Unsafe conditions', c.uc || 0, '#4f46e5', 'UC reports'),
            statCard('Unsafe actions', c.ua || 0, '#f59e0b', 'UA reports'),
            statCard('Drafts', c.drafts || 0, '#64748b', 'Not submitted'),
            statCard('Pending', c.pending || 0, '#2563eb', 'In review'),
            statCard('Closed', c.closed || 0, '#15803d', 'Fully approved'),
            statCard('Rejected', c.rejected || 0, '#dc2626', 'Needs attention')
        ].join('');
    }

    function renderTrend(trend) {
        var list = trend || [];
        if (!list.length) {
            $('#trend').innerHTML = emptyState('No trend data yet', 'Submit a report to start the six-month chart.');
            return;
        }

        var max = 1;
        list.forEach(function (t) { max = Math.max(max, t.uc || 0, t.ua || 0); });

        var cols = list.map(function (t) {
            var ucH = Math.max(2, Math.round((t.uc || 0) / max * 100));
            var uaH = Math.max(2, Math.round((t.ua || 0) / max * 100));
            return '<div class="chart-col">' +
                '<div class="chart-bars">' +
                    '<div class="chart-bar" style="height:' + ucH + '%" title="Unsafe conditions: ' + (t.uc || 0) + '"></div>' +
                    '<div class="chart-bar ua" style="height:' + uaH + '%" title="Unsafe actions: ' + (t.ua || 0) + '"></div>' +
                '</div>' +
                '<div class="chart-label">' + esc(t.label || t.month || '') + '</div>' +
            '</div>';
        }).join('');

        $('#trend').innerHTML = '<div class="chart">' + cols + '</div>';
    }

    function renderQueues(queues) {
        var q = queues || {};
        var rows = [
            ['PSSD HSE Officer', q.hse || 0, 'First-line review'],
            ['Head of Section', q.hos || 0, 'Second-line review'],
            ['Head of Department', q.hod || 0, 'Final approval']
        ];
        $('#queues').innerHTML = rows.map(function (r) {
            return '<div class="queue">' +
                '<div><div class="queue-name">' + esc(r[0]) + '</div><div class="queue-sub">' + esc(r[2]) + '</div></div>' +
                '<div class="queue-count">' + r[1] + '</div>' +
            '</div>';
        }).join('');
    }

    function renderActivity(items) {
        var list = items || [];
        if (!list.length) {
            $('#activity').innerHTML = emptyState('No activity yet', 'Your recent report actions will appear here.');
            return;
        }
        $('#activity').innerHTML = '<ul class="activity">' + list.map(function (a) {
            return '<li>' +
                '<div class="ico">' + esc(String(a.action || 'ACT').slice(0, 4)) + '</div>' +
                '<div class="body">' +
                    '<div class="txt">' + esc(a.activity || a.action || '') + '</div>' +
                    '<div class="time">' + esc(U.fmtDate(a.date)) + '</div>' +
                '</div>' +
            '</li>';
        }).join('') + '</ul>';
    }

    function renderLeaderboard(items) {
        var list = items || [];
        if (!list.length) {
            $('#leaderboard').innerHTML = emptyState('No submissions yet', 'The top reporters will be ranked here.');
            return;
        }
        $('#leaderboard').innerHTML = list.map(function (u, i) {
            var cls = 'rank' + (i < 3 ? ' top' + (i + 1) : '');
            return '<div class="' + cls + '">' +
                '<div class="num">' + (i + 1) + '</div>' +
                '<div><div class="name">' + esc(u.name || u.staffId) + '</div><div class="sub">' + esc(u.staffId) + '</div></div>' +
                '<div class="total">' + (u.total || 0) + '</div>' +
            '</div>';
        }).join('');
    }

    async function load() {
        var btn = $('#refreshBtn');
        U.setBusy(btn, true, 'Refreshing…');
        try {
            var results = await Promise.all([U.loadMe(), U.api('/api/dashboard')]);
            var me = results[0];
            var data = results[1] || {};
            renderWelcome(me);
            renderStats(data.counts);
            renderTrend(data.trend);
            renderQueues(data.queues);
            renderActivity(data.activity);
            renderLeaderboard(data.leaderboard);
        } catch (err) {
            U.toast(err.message || 'Failed to load dashboard', 'error');
        } finally {
            U.setBusy(btn, false);
        }
    }

    $('#refreshBtn').addEventListener('click', load);
    load();
})();
