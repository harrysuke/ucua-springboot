(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;

    var root = $('#form-root');
    var mode = root ? root.dataset.mode : 'new';
    var id = root ? (parseInt(root.dataset.id, 10) || 0) : 0;
    var lookups = null;

    /* ---------- lookups -------------------------------------------------- */

    function ensureOption(select, value, label) {
        if (!value) return;
        for (var i = 0; i < select.options.length; i++) {
            if (select.options[i].value === value) { select.value = value; return; }
        }
        var opt = document.createElement('option');
        opt.value = value;
        opt.textContent = label || value;
        select.appendChild(opt);
        select.value = value;
    }

    function fillLookups() {
        var locations = (lookups && lookups.locations) || [];
        $('#locationId').innerHTML = '<option value="">Select a location…</option>' + locations.map(function (l) {
            return '<option value="' + esc(l.locationId) + '">' + esc(l.locationName) + ' (' + esc(l.locationId) + ')</option>';
        }).join('');

        var offences = (lookups && lookups.offences) || [];
        $('#offenceCode').innerHTML = '<option value="">Select an offence…</option>' + offences.map(function (o) {
            return '<option value="' + esc(o.offenceCode) + '">' + esc(o.offenceCode) + ' · ' + esc(o.offence) + '</option>';
        }).join('');

        var companies = (lookups && lookups.companies) || [];
        $('#company-list').innerHTML = companies.map(function (c) {
            return '<option value="' + esc(c.companyName) + '"></option>';
        }).join('');

        var departments = (lookups && lookups.departments) || [];
        $('#dept-list').innerHTML = departments.map(function (d) {
            return '<option value="' + esc(d.deptName) + '"></option>';
        }).join('');
    }

    /* ---------- type toggle ---------------------------------------------- */

    function setType(type) {
        $('#formType').value = type;
        document.querySelectorAll('#type-toggle button').forEach(function (b) {
            b.classList.toggle('is-active', b.dataset.type === type);
        });
        updateRequirements();
    }

    function updateRequirements() {
        var isUA = $('#formType').value === 'Unsafe Action';
        document.querySelectorAll('[data-req="ua"]').forEach(function (el) { el.classList.toggle('hidden', !isUA); });
        document.querySelectorAll('[data-req="uc"]').forEach(function (el) { el.classList.toggle('hidden', isUA); });
    }

    /* ---------- actions --------------------------------------------------- */

    function renderActions() {
        var html;
        if (mode === 'edit') {
            html =
                '<button type="button" class="btn btn-ghost" data-submit="save">Save changes</button>' +
                '<button type="submit" class="btn btn-primary">Submit for review</button>' +
                '<a class="btn btn-ghost" href="' + U.ctx + '/pages/reports/' + id + '">Cancel</a>';
        } else {
            html =
                '<button type="button" class="btn btn-ghost" data-submit="draft">Save draft</button>' +
                '<button type="submit" class="btn btn-primary">Submit for review</button>' +
                '<a class="btn btn-ghost" href="' + U.ctx + '/pages/reports">Cancel</a>';
        }
        $('#form-actions').innerHTML = html;
    }

    /* ---------- prefill --------------------------------------------------- */

    function prefill(r) {
        setType(r.formType || 'Unsafe Condition');
        ensureOption($('#locationId'), r.locationId || '', r.locationId || '');
        ensureOption($('#offenceCode'), r.offenceCode || '', r.offenceCode || '');
        $('#conditionDetails').value = r.conditionDetails || '';
        $('#violatorName').value = r.violatorName || '';
        $('#violatorCompany').value = r.violatorCompany || '';
        $('#violatorDept').value = r.violatorDept || '';
        $('#actionTaken').value = r.actionTaken || '';
    }

    /* ---------- submit ---------------------------------------------------- */

    async function submitForm(kind) {
        var errorBox = $('#form-error');
        errorBox.hidden = true;

        function fail(message) {
            errorBox.hidden = false;
            errorBox.textContent = message;
            errorBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        var payload = {
            formType: $('#formType').value,
            locationId: $('#locationId').value,
            conditionDetails: $('#conditionDetails').value.trim() || null,
            offenceCode: $('#offenceCode').value || null,
            violatorName: $('#violatorName').value.trim() || null,
            violatorCompany: $('#violatorCompany').value.trim() || null,
            violatorDept: $('#violatorDept').value.trim() || null,
            actionTaken: $('#actionTaken').value.trim() || null
        };

        if (kind !== 'draft') {
            if (!payload.locationId) return fail('Select a location.');
            if (payload.formType === 'Unsafe Condition' && !payload.conditionDetails) return fail('Describe the unsafe condition.');
            if (payload.formType === 'Unsafe Action' && (!payload.offenceCode || !payload.violatorCompany)) {
                return fail('Select an offence code and enter the violator company.');
            }
        }

        var btn = document.querySelector('#form-actions .btn-primary') || document.querySelector('#form-actions .btn-ghost');
        U.setBusy(btn, true, kind === 'draft' ? 'Saving…' : 'Submitting…');

        try {
            if (mode === 'edit') {
                await U.api('/api/reports/' + id, {
                    method: 'PUT',
                    body: Object.assign({}, payload, { submit: kind === 'submit' })
                });
                U.toast(kind === 'submit' ? 'Report resubmitted for review' : 'Changes saved', 'success');
                location.href = U.ctx + '/pages/reports/' + id;
            } else {
                var res = await U.api('/api/reports', {
                    method: 'POST',
                    body: Object.assign({}, payload, { draft: kind === 'draft', submit: kind === 'submit' })
                });
                U.toast(kind === 'draft' ? 'Draft saved' : 'Report submitted', 'success');
                location.href = U.ctx + '/pages/reports/' + (res && res.id ? res.id : '');
            }
        } catch (err) {
            fail(err.message || 'Could not save the report');
            U.setBusy(btn, false);
        }
    }

    /* ---------- AI suggestion -------------------------------------------- */

    function renderSuggestions(res) {
        var host = $('#suggestions');
        if (!res || res.disabled) {
            host.innerHTML = '<p class="hint" style="margin:0">' + esc((res && res.message) || 'AI layer is disabled.') + '</p>';
            return;
        }
        var list = res.suggestions || [];
        if (!list.length) {
            host.innerHTML = '<p class="hint" style="margin:0">' + esc(res.message || 'No matching offence found.') + '</p>';
            return;
        }

        host.innerHTML = list.map(function (s) {
            var pct = Math.round((s.score || 0) * 100);
            return '<button type="button" class="suggest" data-code="' + esc(s.offenceCode) + '">' +
                '<strong>' + esc(s.offenceCode) + '</strong>' +
                '<span>' + esc(s.offence) + '</span>' +
                '<em>' + pct + '%</em>' +
            '</button>';
        }).join('');

        host.querySelectorAll('.suggest').forEach(function (b) {
            b.addEventListener('click', function () {
                var code = b.dataset.code;
                ensureOption($('#offenceCode'), code, code);
                $('#suggest-hint').textContent = 'Selected ' + code;
                U.toast('Offence code ' + code + ' selected', 'success');
            });
        });
    }

    async function suggest() {
        var text = $('#conditionDetails').value.trim();
        var hint = $('#suggest-hint');
        if (!text) {
            U.toast('Describe the condition first', 'error');
            return;
        }

        var btn = $('#suggestBtn');
        U.setBusy(btn, true, 'Thinking…');
        hint.textContent = '';
        try {
            var res = await U.api('/api/ai/suggest-offence', {
                method: 'POST',
                body: { conditionDetails: text }
            });
            renderSuggestions(res);
        } catch (err) {
            hint.textContent = err.message || 'Suggestion failed';
            U.toast(err.message || 'Suggestion failed', 'error');
        } finally {
            U.setBusy(btn, false);
        }
    }

    /* ---------- init ------------------------------------------------------ */

    async function init() {
        try {
            lookups = await U.api('/api/lookups');
            fillLookups();

            if (mode === 'edit') {
                var res = await U.api('/api/reports/' + id);
                prefill(res.report);
                $('#form-title').textContent = 'Edit report #' + id;
                $('#form-sub').textContent = 'Update the report, then save or resubmit for review.';
                $('#form-mode-badge').textContent = res.report.status;
            } else {
                $('#form-title').textContent = 'New report';
                $('#form-sub').textContent = 'Capture an unsafe condition or unsafe action.';
                $('#form-mode-badge').textContent = 'new';
            }

            renderActions();
            updateRequirements();
        } catch (err) {
            U.toast(err.message || 'Could not load the form', 'error');
        }
    }

    $('#type-toggle').addEventListener('click', function (e) {
        var b = e.target instanceof Element ? e.target.closest('button[data-type]') : null;
        if (b) setType(b.dataset.type);
    });

    $('#report-form').addEventListener('submit', function (e) {
        e.preventDefault();
        submitForm('submit');
    });

    $('#form-actions').addEventListener('click', function (e) {
        var b = e.target instanceof Element ? e.target.closest('[data-submit]') : null;
        if (b) {
            e.preventDefault();
            submitForm(b.dataset.submit);
        }
    });

    $('#suggestBtn').addEventListener('click', suggest);

    init();
})();
