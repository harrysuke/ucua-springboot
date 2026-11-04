(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;
    var lookups = null;

    function locationName(id) {
        if (!id) return '—';
        var list = (lookups && lookups.locations) || [];
        for (var i = 0; i < list.length; i++) {
            if (list[i].locationId === id) return list[i].locationName;
        }
        return id;
    }

    function offenceName(code) {
        if (!code) return '';
        var list = (lookups && lookups.offences) || [];
        for (var i = 0; i < list.length; i++) {
            if (list[i].offenceCode === code) return list[i].offence;
        }
        return code;
    }

    function render(reports) {
        var host = $('#reports-table');

        if (!reports.length) {
            host.innerHTML = '<div class="card"><div class="empty">' +
                '<div class="empty-icon">◌</div>' +
                '<h3>No reports match these filters</h3>' +
                '<p>Try clearing the filters, or create a new unsafe condition or unsafe action report.</p>' +
                '<a class="btn btn-primary btn-sm mt-8" href="' + U.ctx + '/pages/reports/new">New report</a>' +
            '</div></div>';
            return;
        }

        var rows = reports.map(function (r) {
            var key = U.statusKey(r.status);
            var isUA = r.formType === 'Unsafe Action';
            var typeCls = isUA ? 'badge-ua' : 'badge-uc';
            var typeLabel = isUA ? 'UA' : 'UC';
            var summary = r.conditionDetails || offenceName(r.offenceCode) || '—';

            return '<tr>' +
                '<td class="cell-strong">#' + r.id + '</td>' +
                '<td><span class="badge ' + typeCls + '">' + typeLabel + '</span></td>' +
                '<td>' + esc(locationName(r.locationId)) + '</td>' +
                '<td><div class="truncate" style="max-width:340px" title="' + esc(summary) + '">' + esc(summary) + '</div></td>' +
                '<td>' + esc(r.observerName || r.staffId || '—') + '</td>' +
                '<td class="nowrap cell-muted">' + esc(U.fmtDate(r.submitDate)) + '</td>' +
                '<td><span class="badge badge-' + key + '">' + esc(r.status || '—') + '</span></td>' +
                '<td class="nowrap"><a class="btn btn-ghost btn-sm" href="' + U.ctx + '/pages/reports/' + r.id + '">Open</a></td>' +
            '</tr>';
        }).join('');

        host.innerHTML = '<div class="card">' +
            '<div class="card-head">' +
                '<div class="card-title">' + reports.length + ' report' + (reports.length === 1 ? '' : 's') + '</div>' +
                '<a class="btn btn-primary btn-sm" href="' + U.ctx + '/pages/reports/new">New report</a>' +
            '</div>' +
            '<div class="table-wrap"><table class="table">' +
                '<thead><tr><th>ID</th><th>Type</th><th>Location</th><th>Summary</th><th>Observer</th><th>Submitted</th><th>Status</th><th></th></tr></thead>' +
                '<tbody>' + rows + '</tbody>' +
            '</table></div>' +
        '</div>';
    }

    async function load() {
        var type = $('#type').value;
        var status = $('#status').value;
        var q = $('#q').value.trim();
        var mine = $('#mine').checked;

        var params = new URLSearchParams();
        if (type) params.set('type', type);
        if (status) params.set('status', status);
        if (q) params.set('q', q);
        if (mine) params.set('mine', '1');
        var qs = params.toString();
        history.replaceState(null, '', location.pathname + (qs ? '?' + qs : ''));

        var host = $('#reports-table');
        host.innerHTML = '<div class="card"><div class="card-body"><div class="skeleton" style="height:200px"></div></div></div>';

        try {
            var data = await U.api('/api/reports' + (qs ? '?' + qs : ''));
            render((data && data.reports) || []);
        } catch (err) {
            host.innerHTML = '<div class="card"><div class="empty">' +
                '<div class="empty-icon">!</div>' +
                '<h3>Could not load reports</h3>' +
                '<p>' + esc(err.message || 'Unexpected error') + '</p>' +
            '</div></div>';
            U.toast(err.message || 'Could not load reports', 'error');
        }
    }

    async function init() {
        var type = U.param('type');
        var status = U.param('status');
        var q = U.param('q');
        var mine = U.param('mine');

        if (type) $('#type').value = type;
        if (status) $('#status').value = status;
        if (q) $('#q').value = q;
        if (mine === '1') $('#mine').checked = true;

        try {
            lookups = await U.api('/api/lookups');
        } catch (e) {
            lookups = null;
        }

        $('#filters').addEventListener('submit', function (e) {
            e.preventDefault();
            load();
        });
        ['#type', '#status', '#mine'].forEach(function (selector) {
            $(selector).addEventListener('change', load);
        });
        $('#clearBtn').addEventListener('click', function () {
            $('#q').value = '';
            $('#type').value = '';
            $('#status').value = '';
            $('#mine').checked = false;
            history.replaceState(null, '', location.pathname);
            load();
        });

        load();
    }

    init();
})();
