(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;
    var data = null;
    var tab = 'locations';
    var search = '';

    function count(key) {
        return data && data[key] ? data[key].length : 0;
    }

    function updateCounts() {
        ['locations', 'offences', 'companies', 'departments'].forEach(function (key) {
            var el = document.getElementById('count-' + key);
            if (el) el.textContent = count(key);
        });
    }

    function empty(message) {
        return '<div class="empty"><div class="empty-icon">◌</div><h3>' + esc(message) + '</h3></div>';
    }

    function table(headers, rows) {
        return '<div class="table-wrap"><table class="table"><thead><tr>' +
            headers.map(function (h) { return '<th>' + esc(h) + '</th>'; }).join('') +
        '</tr></thead><tbody>' + rows + '</tbody></table></div>';
    }

    function render() {
        var panel = $('#lookup-panel');
        var searchWrap = $('#lookup-search-wrap');
        if (searchWrap) searchWrap.hidden = tab !== 'offences';

        if (tab === 'locations') {
            var locations = (data && data.locations) || [];
            panel.innerHTML = locations.length
                ? table(['Location ID', 'Location name'], locations.map(function (l) {
                    return '<tr><td class="cell-strong mono">' + esc(l.locationId) + '</td><td>' + esc(l.locationName) + '</td></tr>';
                }).join(''))
                : empty('No locations configured');
            return;
        }

        if (tab === 'offences') {
            var needle = search.toLowerCase();
            var offences = ((data && data.offences) || []).filter(function (o) {
                if (!needle) return true;
                var hay = ((o.offenceCode || '') + ' ' + (o.offKeyword || '') + ' ' + (o.offence || '')).toLowerCase();
                return hay.indexOf(needle) !== -1;
            });
            panel.innerHTML = offences.length
                ? table(['Code', 'Keyword', 'Offence'], offences.map(function (o) {
                    return '<tr>' +
                        '<td class="cell-strong mono">' + esc(o.offenceCode) + '</td>' +
                        '<td><span class="badge badge-neutral">' + esc(o.offKeyword || '—') + '</span></td>' +
                        '<td>' + esc(o.offence) + '</td>' +
                    '</tr>';
                }).join(''))
                : empty(search ? 'No offences match "' + search + '"' : 'No offences configured');
            return;
        }

        if (tab === 'companies') {
            var companies = (data && data.companies) || [];
            panel.innerHTML = companies.length
                ? table(['ID', 'Company'], companies.map(function (c) {
                    return '<tr><td class="cell-strong">' + esc(c.id) + '</td><td>' + esc(c.companyName) + '</td></tr>';
                }).join(''))
                : empty('No companies configured');
            return;
        }

        var departments = (data && data.departments) || [];
        panel.innerHTML = departments.length
            ? table(['ID', 'Code', 'Department'], departments.map(function (d) {
                return '<tr>' +
                    '<td class="cell-strong">' + esc(d.deptId) + '</td>' +
                    '<td class="mono">' + esc(d.dept) + '</td>' +
                    '<td>' + esc(d.deptName) + '</td>' +
                '</tr>';
            }).join(''))
            : empty('No departments configured');
    }

    async function load() {
        try {
            data = await U.api('/api/lookups');
            updateCounts();
            render();
        } catch (err) {
            U.toast(err.message || 'Could not load lookups', 'error');
        }
    }

    $('#lookup-tabs').addEventListener('click', function (e) {
        var b = e.target instanceof Element ? e.target.closest('[data-tab]') : null;
        if (!b) return;
        tab = b.dataset.tab;
        document.querySelectorAll('#lookup-tabs .tab').forEach(function (t) {
            t.classList.toggle('is-active', t === b);
        });
        render();
    });

    $('#lookup-search').addEventListener('input', function (e) {
        search = e.target.value.trim();
        if (tab === 'offences') render();
    });

    $('#lookup-refresh').addEventListener('click', load);

    load();
})();
