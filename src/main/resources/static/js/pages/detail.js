(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;
    var root = $('#report-root');
    var reportId = root ? parseInt(root.dataset.id, 10) : NaN;

    var report = null;
    var me = null;
    var lookups = null;
    var photos = [];

    /* ---------- lookups -------------------------------------------------- */

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
        return '';
    }

    function photoUrl(url) {
        if (!url) return '';
        if (/^https?:/i.test(url)) return url;
        return U.ctx + url;
    }

    function isEditable() {
        return report && (report.status === 'Draft' || /^rejected/i.test(report.status || ''));
    }

    function stageLabel(status) {
        var s = status || '';
        if (s === 'Draft') return 'Draft';
        if (/^rejected/i.test(s)) return 'Rejected — awaiting resubmission';
        if (s.indexOf('Pending') === 0) return 'PSSD HSE Officer review';
        if (s.indexOf('Approved by PSSD HSE') === 0 || s.indexOf('Approved by PSSD Officer') === 0) return 'Head of Section review';
        if (s === 'Approved by HOS') return 'Head of Department review';
        if (s === 'Approved by HOD' || s === 'Approved by HODs') return 'Closed';
        return s || 'Unknown';
    }

    /* ---------- render --------------------------------------------------- */

    function kv(label, value, isHtml) {
        return '<dt>' + esc(label) + '</dt><dd>' + (isHtml ? value : esc(value || '—')) + '</dd>';
    }

    function renderActions() {
        var isAdmin = me && me.accessLevel >= 3;
        var editable = isEditable();
        var html = [];

        if ((report.isOwner || isAdmin) && (editable || isAdmin)) {
            html.push('<a class="btn btn-ghost" href="' + U.ctx + '/pages/reports/' + report.id + '/edit">Edit</a>');
        }
        if (report.isOwner && editable) {
            html.push('<button type="button" class="btn btn-primary" data-act="resubmit">Resubmit</button>');
        }
        if (report.canApprove) {
            html.push('<button type="button" class="btn btn-success" data-act="approve">Approve</button>');
            html.push('<button type="button" class="btn btn-danger" data-act="reject">Reject</button>');
        }
        if (report.status === 'Approved by HOS' && report.canApprove) {
            html.push('<button type="button" class="btn btn-primary" data-act="close">Close</button>');
        }
        if (report.isOwner || isAdmin) {
            html.push('<button type="button" class="btn btn-danger" data-act="delete">Delete</button>');
        }

        $('#report-actions').innerHTML = html.join('');
    }

    function renderKv() {
        var isUA = report.formType === 'Unsafe Action';
        var rows = [];

        rows.push(kv('Status', '<span class="badge badge-' + U.statusKey(report.status) + '">' + esc(report.status) + '</span>', true));
        rows.push(kv('Location', locationName(report.locationId) + (report.locationId ? ' (' + report.locationId + ')' : '')));
        if (isUA || report.offenceCode) {
            var off = offenceName(report.offenceCode);
            rows.push(kv('Offence', report.offenceCode ? (report.offenceCode + (off ? ' · ' + off : '')) : '—'));
        }
        rows.push(kv('Violator', report.violatorName || '—'));
        if (isUA) {
            rows.push(kv('Company', report.violatorCompany || '—'));
            rows.push(kv('Department', report.violatorDept || '—'));
        }
        rows.push(kv('Action taken', report.actionTaken || '—'));
        rows.push(kv('Submitted', U.fmtDate(report.submitDate)));
        rows.push(kv('Staff ID', report.staffId || '—'));

        $('#report-kv').innerHTML = rows.join('');
    }

    function renderNarrative() {
        $('#report-narrative').innerHTML = report.conditionDetails
            ? '<p style="margin:0;white-space:pre-wrap">' + esc(report.conditionDetails) + '</p>'
            : '<p class="muted" style="margin:0">No narrative provided.</p>';
    }

    function renderPhotos() {
        $('#photo-count').textContent = photos.length + ' photo' + (photos.length === 1 ? '' : 's');

        if (!photos.length) {
            $('#photos').innerHTML = '<div class="empty" style="padding:28px 12px">' +
                '<div class="empty-icon">▣</div>' +
                '<h3>No evidence attached</h3>' +
                '<p>Upload a photo using the panel on the right.</p>' +
            '</div>';
            return;
        }

        $('#photos').innerHTML = '<div class="photo-grid">' + photos.map(function (p) {
            var url = photoUrl(p.url);
            return '<a class="photo" href="' + esc(url) + '" target="_blank" rel="noopener">' +
                '<img src="' + esc(url) + '" alt="' + esc(p.photoType || 'Evidence') + '" loading="lazy">' +
                '<span class="tag">' + esc(p.photoType || 'Evidence') + '</span>' +
            '</a>';
        }).join('') + '</div>';
    }

    function renderTimeline() {
        var items = report.history || [];
        if (!items.length) {
            $('#timeline').innerHTML = '<li><div class="tl-title">No history recorded</div><div class="tl-meta">Events appear as the report moves through workflow.</div></li>';
            return;
        }
        $('#timeline').innerHTML = items.map(function (h) {
            return '<li>' +
                '<div class="tl-title">' + esc(h.action || 'Event') + '</div>' +
                '<div class="tl-meta">' + esc(h.staffName || h.staffId || '') + ' · ' + esc(U.fmtDate(h.createdAt)) + '</div>' +
                (h.remarks ? '<div class="tl-remarks">' + esc(h.remarks) + '</div>' : '') +
            '</li>';
        }).join('');
    }

    function renderStatus() {
        var html = '<div class="row between">' +
            '<span class="badge badge-' + U.statusKey(report.status) + '">' + esc(report.status) + '</span>' +
            (report.canApprove ? '<span class="small muted">Action available</span>' : '') +
        '</div>';
        if (report.rejectionReason) {
            html += '<div class="form-error mt-12">Rejected: ' + esc(report.rejectionReason) + '</div>';
        }
        if (report.isOwner) {
            html += '<p class="hint mt-12" style="margin-bottom:0">You submitted this report.</p>';
        }
        $('#status-card').innerHTML = html;
    }

    function renderObserver() {
        $('#observer-card').innerHTML =
            '<div class="row">' +
                '<div class="avatar">' + esc(U.initials(report.observerName || report.staffId)) + '</div>' +
                '<div>' +
                    '<div class="cell-strong">' + esc(report.observerName || report.staffId || '—') + '</div>' +
                    '<div class="small muted">' + esc(report.observerDepartment || '—') + '</div>' +
                '</div>' +
            '</div>' +
            '<div class="divider"></div>' +
            '<dl class="kv" style="grid-template-columns:88px 1fr;gap:8px 12px">' +
                '<dt>Staff ID</dt><dd>' + esc(report.staffId || '—') + '</dd>' +
                '<dt>Stage</dt><dd>' + esc(stageLabel(report.status)) + '</dd>' +
            '</dl>';
    }

    function renderWorkflow() {
        var wf = [];
        if (report.canApprove) {
            wf.push('<button type="button" class="btn btn-success btn-sm" data-act="approve">Approve</button>');
            wf.push('<button type="button" class="btn btn-danger btn-sm" data-act="reject">Reject</button>');
        }
        if (report.status === 'Approved by HOS' && report.canApprove) {
            wf.push('<button type="button" class="btn btn-primary btn-sm" data-act="close">Close</button>');
        }
        if (report.isOwner && isEditable()) {
            wf.push('<button type="button" class="btn btn-primary btn-sm" data-act="resubmit">Resubmit</button>');
        }

        $('#workflow-card').innerHTML =
            '<p class="small muted" style="margin:0 0 10px">Current stage: <strong>' + esc(stageLabel(report.status)) + '</strong></p>' +
            '<div class="row wrap">' + (wf.join('') || '<span class="small muted">No workflow action available for your role.</span>') + '</div>' +
            '<div class="divider"></div>' +
            '<label class="field">' +
                '<span class="label">Quick update · action taken</span>' +
                '<input class="input" id="patch-action" value="' + esc(report.actionTaken || '') + '" placeholder="Describe the action taken">' +
            '</label>' +
            '<button type="button" class="btn btn-ghost btn-sm mt-8" data-act="patch">Save quick update (PATCH)</button>';
    }

    function toggleUpload() {
        var isAdmin = me && me.accessLevel >= 3;
        var card = $('#upload-card');
        if (card) card.classList.toggle('hidden', !(report.isOwner || isAdmin));
    }

    function render() {
        var isUA = report.formType === 'Unsafe Action';
        $('#report-title').textContent = 'Report #' + report.id;
        $('#report-sub').textContent = report.formType + ' · submitted ' + U.fmtDate(report.submitDate) + ' by ' + (report.observerName || report.staffId || '—');
        $('#report-type').innerHTML = '<span class="badge ' + (isUA ? 'badge-ua' : 'badge-uc') + '">' + (isUA ? 'Unsafe action' : 'Unsafe condition') + '</span>';

        renderActions();
        renderKv();
        renderNarrative();
        renderPhotos();
        renderTimeline();
        renderStatus();
        renderObserver();
        renderWorkflow();
        toggleUpload();
    }

    /* ---------- data ----------------------------------------------------- */

    async function load() {
        try {
            var results = await Promise.all([
                U.api('/api/reports/' + reportId),
                U.api('/api/auth/me').catch(function () { return null; }),
                U.api('/api/lookups').catch(function () { return null; }),
                U.api('/api/reports/' + reportId + '/photos').catch(function () { return null; })
            ]);

            report = results[0].report;
            me = results[1];
            lookups = results[2];
            photos = (results[3] && results[3].photos) || report.photos || [];

            if (me) U.renderUser(me);
            render();
        } catch (err) {
            $('#report-sub').textContent = err.message || 'Report unavailable';
            U.toast(err.message || 'Failed to load report', 'error');
            $('#report-actions').innerHTML = '<a class="btn btn-ghost" href="' + U.ctx + '/pages/reports">Back to reports</a>';
        }
    }

    /* ---------- actions -------------------------------------------------- */

    var DONE = { resubmit: 'resubmitted', approve: 'approved', close: 'closed' };

    async function onWorkflow(action, btn) {
        var questions = {
            resubmit: 'Resubmit this report for PSSD HSE review?',
            approve: 'Approve this report at the current stage?',
            close: 'Close this report as fully approved?'
        };
        var ok = await U.confirmDialog('Confirm ' + action, questions[action] || 'Continue?', action === 'resubmit' ? 'Resubmit' : 'Confirm');
        if (!ok) return;

        U.setBusy(btn, true, 'Working…');
        try {
            var res = await U.api('/api/workflow', { method: 'POST', body: { id: report.id, action: action } });
            U.toast('Report ' + DONE[action] + ' · status: ' + res.status, 'success');
            await load();
        } catch (err) {
            U.toast(err.message || 'Workflow action failed', 'error');
            U.setBusy(btn, false);
        }
    }

    function onReject() {
        U.modal({
            title: 'Reject report #' + report.id,
            body: '<label class="field">' +
                    '<span class="label">Rejection reason <span class="req">*</span></span>' +
                    '<textarea class="textarea" id="reject-remarks" placeholder="Explain what needs to change…"></textarea>' +
                '</label>' +
                '<div class="form-error hidden" id="reject-error"></div>',
            actions: [
                { label: 'Cancel', cls: 'btn-ghost', onClick: function (close) { close(); } },
                { label: 'Reject report', cls: 'btn-danger', onClick: function (close) { submitReject(close); } }
            ]
        });

        async function submitReject(close) {
            var remarks = document.getElementById('reject-remarks').value.trim();
            var errBox = document.getElementById('reject-error');
            if (!remarks) {
                errBox.hidden = false;
                errBox.textContent = 'A rejection reason is required.';
                return;
            }
            try {
                var res = await U.api('/api/workflow', { method: 'POST', body: { id: report.id, action: 'reject', remarks: remarks } });
                close();
                U.toast('Report rejected · status: ' + res.status, 'success');
                await load();
            } catch (err) {
                errBox.hidden = false;
                errBox.textContent = err.message || 'Reject failed';
            }
        }
    }

    async function onDelete(btn) {
        var ok = await U.confirmDialog(
            'Delete report #' + report.id,
            'This permanently removes the report, its photos and its timeline. This cannot be undone.',
            'Delete report',
            true
        );
        if (!ok) return;

        U.setBusy(btn, true, 'Deleting…');
        try {
            await U.api('/api/reports/' + report.id, { method: 'DELETE' });
            U.toast('Report deleted', 'success');
            setTimeout(function () { location.href = U.ctx + '/pages/reports'; }, 450);
        } catch (err) {
            U.toast(err.message || 'Delete failed', 'error');
            U.setBusy(btn, false);
        }
    }

    async function onPatch(btn) {
        var input = document.getElementById('patch-action');
        var value = input ? input.value.trim() : '';
        U.setBusy(btn, true, 'Saving…');
        try {
            await U.api('/api/reports/' + report.id, { method: 'PATCH', body: { actionTaken: value } });
            U.toast('Action taken updated', 'success');
            await load();
        } catch (err) {
            U.toast(err.message || 'Update failed', 'error');
            U.setBusy(btn, false);
        }
    }

    document.addEventListener('click', function (e) {
        var el = e.target instanceof Element ? e.target.closest('[data-act]') : null;
        if (!el || !report) return;
        var act = el.dataset.act;
        if (act === 'delete') { e.preventDefault(); onDelete(el); }
        else if (act === 'reject') { e.preventDefault(); onReject(); }
        else if (act === 'patch') { e.preventDefault(); onPatch(el); }
        else if (act === 'resubmit' || act === 'approve' || act === 'close') { e.preventDefault(); onWorkflow(act, el); }
    });

    /* ---------- upload --------------------------------------------------- */

    var uploadForm = $('#upload-form');
    if (uploadForm) {
        uploadForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            if (!report) return;

            var fileInput = $('#photo-file');
            var file = fileInput && fileInput.files ? fileInput.files[0] : null;
            if (!file) {
                U.toast('Choose an image file first', 'error');
                return;
            }

            var fd = new FormData();
            fd.append('reportId', String(report.id));
            fd.append('photoType', $('#photo-type').value);
            fd.append('file', file);

            var btn = uploadForm.querySelector('button[type="submit"]');
            U.setBusy(btn, true, 'Uploading…');
            try {
                await U.api('/api/reports/photos', { method: 'POST', form: true, body: fd });
                U.toast('Photo uploaded', 'success');
                uploadForm.reset();
                await load();
            } catch (err) {
                U.toast(err.message || 'Upload failed', 'error');
                U.setBusy(btn, false);
            }
        });
    }

    load();
})();
