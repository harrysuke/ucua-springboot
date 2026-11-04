(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (U.store.token()) { location.replace(U.ctx + '/pages/dashboard'); return; }

    var $ = function (s) { return document.querySelector(s); };
    var loginForm = $('#login-form');
    var forgotForm = $('#forgot-form');
    var errorBox = $('#login-error');

    function showError(message) {
        errorBox.hidden = false;
        errorBox.textContent = message;
    }

    document.querySelectorAll('.auth-tab').forEach(function (tab) {
        tab.addEventListener('click', function () {
            document.querySelectorAll('.auth-tab').forEach(function (t) {
                t.classList.toggle('is-active', t === tab);
            });
            var which = tab.dataset.tab;
            loginForm.hidden = which !== 'login';
            forgotForm.hidden = which !== 'forgot';
        });
    });

    // Public health probe so the sign-in card can show API reachability.
    U.api('/api/health').then(function (health) {
        var el = $('#api-status');
        if (!el) return;
        el.classList.add('online');
        var txt = el.querySelector('.txt');
        if (txt) txt.textContent = 'API online · ' + (health && health.time ? U.fmtDate(health.time) : 'ready');
    }).catch(function () {
        var el = $('#api-status');
        if (!el) return;
        el.classList.add('offline');
        var txt = el.querySelector('.txt');
        if (txt) txt.textContent = 'API unreachable';
    });

    loginForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        errorBox.hidden = true;

        var staffId = $('#staffId').value.trim();
        var password = $('#password').value;
        if (!staffId || !password) {
            showError('Enter your staff ID and password.');
            return;
        }

        var btn = $('#login-btn');
        U.setBusy(btn, true, 'Signing in…');
        try {
            var res = await U.api('/api/auth/login', {
                method: 'POST',
                body: { staffId: staffId, password: password }
            });
            if (!res || !res.valid) throw new Error((res && res.error) || 'Login failed');
            U.store.set(res.token, res.user);
            location.replace(U.ctx + '/pages/dashboard');
        } catch (err) {
            showError(err.message || 'Login failed');
            U.setBusy(btn, false);
        }
    });

    forgotForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        var note = $('#forgot-note');
        note.hidden = false;
        note.className = 'form-note';
        note.textContent = 'Sending request…';

        try {
            var res = await U.api('/api/auth/forgot', {
                method: 'POST',
                body: {
                    staffId: $('#forgot-staffId').value.trim(),
                    email: $('#forgot-email').value.trim() || null
                }
            });
            note.textContent = (res && res.message) || 'Recovery request received.';
        } catch (err) {
            note.className = 'form-error';
            note.textContent = err.message || 'Request failed';
        }
    });
})();
