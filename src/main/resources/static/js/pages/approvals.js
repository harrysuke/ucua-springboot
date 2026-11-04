(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;
    var lookups = null;
    var me = null;

    var STAGES = [
        {
            key: 'hse', title: 'PSSD HSE Officer', sub: 'First-line review',
            statuses: ['Pending PSSD HSE Officer', 'Pending HSE Reps', 'Updated by HSE Reps'],
            approvers: [3, 4, 8, 9]
        },
        {
            key: 'hos', title: 'Head of Section', sub: 'Second-line review',
            statuses: ['Approved by PSSD HSE Officer', 'Approved by PSSD Officer'],
            approvers: [3, 6, 9]
        },
        {
            key: 'hod', title: 'Head of Department', sub: 'Final approval',
            statuses: ['Approved by HOS'],
            approvers: [3, 7, 9]
        }
    ];

    function locationName(id) {
        if (!id) return '—';
        var list = (lookups && lookups.locations) || [];
        for (var i = 0; i < list.length; i++) {
            if (list[i].locationId === id) return list[i].locationName;
        }
        return id;
    }

    function canAct(stage) {
        return !!me && stage.approvers.indexOf(me.accessLevel) !== -1;
    }

    function emptyRow(message) {
        return '<div class="empty" style="padding:34px 16px">' +
            '<div class="empty-icon">✓</div>' +
            '<h3>' + esc(message) + '</h3>' +
        '</div>';
    }

    function table(stage, list, allowed) {
        var rows = list.map(function (r) {
            var isUA = r.formType === 'Unsafe Action';
            var actions;

            if (allowed) {
                actions = '<div class="row" style="gap:6px">' +
                    '<button type="button" class="btn btn-success btn-sm" data-act="approve" data-id="' + r.id + '">Approve</button>' +
                    (stage.key === 'hod'
                        ? '<button type="button" class="btn btn-primary btn-sm" data-act="close" data-id="' + r.id + '">Close</button>'
                        : '<button type="button" class="btn btn-danger btn-sm" data-act="reject" data-id="' + r.id + '">Reject</button>') +
                '</div>';
            } else {
                actions = '<a class="btn btn-ghost btn-sm" href="' + U.ctx + '/pages/reports/' + r.id + '">Open</a>';
            }

            return '<tr>' +
                '<td class="cell-strong">#' + r.id + '</td>' +
                '<td><span class="badge ' + (isUA ? 'badge-ua' : 'badge-uc') + '">' + (isUA ? 'UA' : 'UC') + '</span></td>' +
                '<td>' + esc(locationName(r.locationId)) + '</td>' +
                '<td><div class="truncate" style="max-width:300px" title="' + esc(r.conditionDetails || r.offenceCode || '') + '">' + esc(r.conditionDetails || r.offenceCode || '—') + '</div></td>' +
                '<td class="nowrap cell-muted">' + esc(U.fmtDate(r.submitDate)) + '</td>' +
                '<td><span class="badge badge-' + U.statusKey(r.status) + '">' + esc(r.status) + '</span></td>' +
                '<td class="nowrap">' + actions + '</td>' +
            '</tr>';
        }).join('');

        return '<div class="table-wrap"><table class="table">' +
            '<thead><tr><th>ID</th><th>Type</th><th>Location</th><th>Summary</th><th>Submitted</th><th>Status</th><th></th></tr></thead>' +
            '<tbody>' + rows + '</tbody>' +
        '</table></div>';
    }

    function render(reports) {
        $('#approvals').innerHTML = STAGES.map(function (stage) {
            var list = reports.filter(function (r) { return stage.statuses.indexOf(r.status) !== -1; });
            var allowed = canAct(stage);

            return '<section class="card mb-16">' +
                '<header class="card-head">' +
                    '<div>' +
                        '<div class="card-title">' + esc(stage.title) + '</div>' +
                        '<div class="card-sub">' + esc(stage.sub) + '</div>' +
                    '</div>' +
                    '<div class="row">' +
                        '<span class="badge badge-' + (list.length ? 'pending' : 'neutral') + '">' + list.length + ' waiting</span>' +
                        (allowed ? '' : '<span class="small muted">Read-only for your role</span>') +
                    '</div>' +
                '</header>' +
                (list.length ? table(stage, list, allowed) : emptyRow('Nothing waiting at this stage')) +
            '</section>';
        }).join('');
    }

    async function load() {
        var btn = $('#approvals-refresh');
        U.setBusy(btn, true, 'Refreshing…');
        try {
            var results = await Promise.all([
                U.api('/api/auth/me'),
                U.api('/api/reports'),
                U.api('/api/lookups').catch(function () { return null; })
            ]);
            me = results[0];
            lookups = results[2];
            if (me) U.renderUser(me);
            render((results[1] && results[1].reports) || []);
        } catch (err) {
            U.toast(err.message || 'Could not load approvals', 'error');
        } finally {
            U.setBusy(btn, false);
        }
    }

    async function doWorkflow(id, action, btn) {
        var question = action === 'approve'
            ? 'Approve report #' + id + ' at its current stage?'
            : 'Close report #' + id + ' as fully approved?';
        var ok = await U.confirmDialog('Confirm ' + action, question, action === 'approve' ? 'Approve' : 'Close');
        if (!ok) return;

        U.setBusy(btn, true, 'Working…');
        try {
            var res = await U.api('/api/workflow', { method: 'POST', body: { id: id, action: action } });
            U.toast('Report ' + (action === 'approve' ? 'approved' : 'closed') + ' · status: ' + res.status, 'success');
            await load();
        } catch (err) {
            U.toast(err.message || 'Action failed', 'error');
            U.setBusy(btn, false);
        }
    }

    function openReject(id) {
        U.modal({
            title: 'Reject report #' + id,
            body: '<label class="field">' +
                    '<span class="label">Rejection reason <span class="req">*</span></span>' +
                    '<textarea class="textarea" id="reject-remarks" placeholder="Explain what needs to change…"></textarea>' +
                '</label>' +
                '<div class="form-error hidden" id="reject-error"></div>',
            actions: [
                { label: 'Cancel', cls: 'btn-ghost', onClick: function (close) { close(); } },
                { label: 'Reject report', cls: 'btn-danger', onClick: function (close) { submit(close); } }
            ]
        });

        async function submit(close) {
            var remarks = document.getElementById('reject-remarks').value.trim();
            var errBox = document.getElementById('reject-error');
            if (!remarks) {
                errBox.hidden = false;
                errBox.textContent = 'A rejection reason is required.';
                return;
            }
            try {
                var res = await U.api('/api/workflow', { method: 'POST', body: { id: id, action: 'reject', remarks: remarks } });
                close();
                U.toast('Report rejected · status: ' + res.status, 'success');
                await load();
            } catch (err) {
                errBox.hidden = false;
                errBox.textContent = err.message || 'Reject failed';
            }
        }
    }

    document.addEventListener('click', function (e) {
        var el = e.target instanceof Element ? e.target.closest('[data-act]') : null;
        if (!el) return;
        var id = parseInt(el.dataset.id, 10);
        if (!id) return;
        var act = el.dataset.act;
        if (act === 'approve' || act === 'close') {
            e.preventDefault();
            doWorkflow(id, act, el);
        } else if (act === 'reject') {
            e.preventDefault();
            openReject(id);
        }
    });

    $('#approvals-refresh').addEventListener('click', load);

    load();
})();
