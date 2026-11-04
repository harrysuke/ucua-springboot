(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (U.store.token()) { location.replace(U.ctx + '/pages/dashboard'); return; }

    var $ = function (s) { return document.querySelector(s); };
    var form = $('#register-form');
    var errorBox = $('#register-error');
    var btn = $('#register-btn');

    function showError(message) {
        errorBox.hidden = false;
        errorBox.textContent = message;
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        errorBox.hidden = true;

        var payload = {
            staffId: $('#staffId').value.trim(),
            name: $('#name').value.trim(),
            password: $('#password').value,
            email: $('#email').value.trim() || null,
            phoneNo: $('#phoneNo').value.trim() || null,
            department: $('#department').value.trim() || null,
            designation: $('#designation').value.trim() || null,
            companyId: $('#companyId').value.trim() || null
        };

        if (!payload.staffId || !payload.name) {
            showError('Staff ID and full name are required.');
            return;
        }
        if (payload.password.length < 6) {
            showError('Choose a password of at least 6 characters.');
            return;
        }

        U.setBusy(btn, true, 'Creating account…');
        try {
            var res = await U.api('/api/auth/register', { method: 'POST', body: payload });
            if (!res || !res.registered) throw new Error((res && res.error) || 'Registration failed');

            form.hidden = true;
            var success = $('#register-success');
            success.hidden = false;
            $('#register-message').textContent = res.message || 'Your account is pending PSSD approval.';
        } catch (err) {
            showError(err.message || 'Registration failed');
            U.setBusy(btn, false);
        }
    });
})();
