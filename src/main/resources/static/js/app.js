/* ==========================================================================
   UCUA web console — shared runtime
   Auth guard, JWT fetch wrapper, toasts, modals and formatting helpers.
   ========================================================================== */

(function () {
    'use strict';

    var body = document.body;
    var ctx = body.dataset.ctx || '';
    var page = body.dataset.page || '';

    var TOKEN_KEY = 'ucua_token';
    var USER_KEY = 'ucua_user';
    var PUBLIC_PAGES = { login: true, register: true };

    /* ---------- storage -------------------------------------------------- */

    var store = {
        token: function () { return localStorage.getItem(TOKEN_KEY); },
        user: function () {
            try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); } catch (e) { return null; }
        },
        set: function (token, user) {
            if (token) localStorage.setItem(TOKEN_KEY, token);
            if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
        },
        clear: function () {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
        }
    };

    /* ---------- api ------------------------------------------------------ */

    function ApiError(message, status, payload) {
        this.name = 'ApiError';
        this.message = message;
        this.status = status;
        this.payload = payload;
        if (Error.captureStackTrace) Error.captureStackTrace(this, ApiError);
    }
    ApiError.prototype = Object.create(Error.prototype);
    ApiError.prototype.constructor = ApiError;

    async function api(path, opts) {
        var options = opts || {};
        var method = options.method || 'GET';
        var headers = Object.assign({}, options.headers || {});
        var token = store.token();
        if (token) headers.Authorization = 'Bearer ' + token;

        var payload;
        if (options.form) {
            payload = options.body;
        } else if (options.body !== undefined && options.body !== null) {
            headers['Content-Type'] = 'application/json';
            payload = JSON.stringify(options.body);
        }

        var res = await fetch(ctx + path, { method: method, headers: headers, body: payload });

        if (res.status === 401 && !PUBLIC_PAGES[page]) {
            store.clear();
            location.replace(ctx + '/pages/login');
            throw new ApiError('Your session has expired. Please sign in again.', 401, null);
        }

        var text = await res.text();
        var data = null;
        if (text) {
            try { data = JSON.parse(text); } catch (e) { data = text; }
        }

        if (!res.ok) {
            var message = (data && (data.error || data.message)) || ('Request failed with status ' + res.status);
            throw new ApiError(message, res.status, data);
        }
        return data;
    }

    /* ---------- formatting ---------------------------------------------- */

    var ESCAPE_MAP = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };

    function escapeHtml(value) {
        return String(value === undefined || value === null ? '' : value)
            .replace(/[&<>"']/g, function (ch) { return ESCAPE_MAP[ch]; });
    }

    function fmtDate(value, withTime) {
        if (!value) return '—';
        var d = new Date(value);
        if (isNaN(d.getTime())) return String(value);
        var opts = { year: 'numeric', month: 'short', day: '2-digit' };
        if (withTime !== false) { opts.hour = '2-digit'; opts.minute = '2-digit'; }
        return d.toLocaleString(undefined, opts);
    }

    function statusKey(status) {
        var s = String(status || '').toLowerCase();
        if (!s) return 'neutral';
        if (s.indexOf('draft') !== -1) return 'draft';
        if (s.indexOf('reject') !== -1) return 'rejected';
        if (s.indexOf('approved by hod') !== -1 || s === 'closed') return 'closed';
        if (s.indexOf('approved') === 0) return 'approved';
        if (s.indexOf('pending') === 0 || s.indexOf('hse rep') !== -1) return 'pending';
        return 'neutral';
    }

    function initials(name) {
        var parts = String(name || '').trim().split(/\s+/).filter(Boolean);
        if (!parts.length) return '?';
        if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
        return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    function roleLabel(level) {
        var map = {
            1: 'Staff', 2: 'Staff', 3: 'PSSD HSE Officer', 4: 'HSE Representative',
            6: 'Head of Section', 7: 'Head of Department', 8: 'HSE Reviewer', 9: 'Administrator'
        };
        return map[level] || 'Staff';
    }

    function renderUser(user) {
        if (!user) return;
        var name = user.name || user.staffId || 'User';
        var role = user.roleName || roleLabel(user.accessLevel);
        document.querySelectorAll('.js-user-name').forEach(function (el) { el.textContent = name; });
        document.querySelectorAll('.js-user-role').forEach(function (el) { el.textContent = role; });
        document.querySelectorAll('.js-avatar').forEach(function (el) { el.textContent = initials(name); });
    }

    /* ---------- feedback ------------------------------------------------ */

    function toast(message, type, timeout) {
        var host = document.getElementById('toast-host') || body;
        var el = document.createElement('div');
        el.className = 'toast ' + (type || 'info');
        el.textContent = message;
        host.appendChild(el);
        setTimeout(function () {
            el.style.transition = 'opacity .2s ease, transform .2s ease';
            el.style.opacity = '0';
            el.style.transform = 'translateY(6px)';
            setTimeout(function () { el.remove(); }, 220);
        }, timeout || 3800);
    }

    function setBusy(btn, busy, busyText) {
        if (!btn) return;
        if (busy) {
            if (!btn.dataset.label) btn.dataset.label = btn.textContent;
            btn.disabled = true;
            if (busyText) btn.textContent = busyText;
        } else {
            btn.disabled = false;
            if (btn.dataset.label) {
                btn.textContent = btn.dataset.label;
                delete btn.dataset.label;
            }
        }
    }

    /* ---------- modals -------------------------------------------------- */

    function modal(opts) {
        var options = opts || {};
        var backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop';
        backdrop.innerHTML =
            '<div class="modal" role="dialog" aria-modal="true">' +
                '<div class="modal-head">' +
                    '<div class="card-title">' + escapeHtml(options.title || '') + '</div>' +
                    '<button type="button" class="icon-btn" data-close aria-label="Close">✕</button>' +
                '</div>' +
                '<div class="modal-body">' + (options.body || '') + '</div>' +
                '<div class="modal-foot"></div>' +
            '</div>';

        var foot = backdrop.querySelector('.modal-foot');
        var closed = false;

        function close() {
            if (closed) return;
            closed = true;
            backdrop.remove();
            document.removeEventListener('keydown', onKey);
            if (options.onClose) options.onClose();
        }
        function onKey(e) { if (e.key === 'Escape') close(); }

        (options.actions || []).forEach(function (action) {
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn ' + (action.cls || 'btn-ghost');
            btn.textContent = action.label;
            btn.addEventListener('click', function () {
                if (action.onClick) action.onClick(close, backdrop);
            });
            foot.appendChild(btn);
        });

        backdrop.querySelector('[data-close]').addEventListener('click', close);
        backdrop.addEventListener('click', function (e) { if (e.target === backdrop) close(); });
        document.addEventListener('keydown', onKey);
        document.body.appendChild(backdrop);

        if (options.onMount) options.onMount(backdrop, close);
        return close;
    }

    function confirmDialog(title, message, confirmLabel, danger) {
        return new Promise(function (resolve) {
            var settled = false;
            modal({
                title: title,
                body: '<p class="muted" style="margin:0">' + escapeHtml(message) + '</p>',
                onClose: function () { if (!settled) resolve(false); },
                actions: [
                    {
                        label: 'Cancel', cls: 'btn-ghost',
                        onClick: function (close) { settled = true; close(); resolve(false); }
                    },
                    {
                        label: confirmLabel || 'Confirm', cls: danger ? 'btn-danger' : 'btn-primary',
                        onClick: function (close) { settled = true; close(); resolve(true); }
                    }
                ]
            });
        });
    }

    /* ---------- session ------------------------------------------------- */

    function requireAuth() {
        if (!store.token()) {
            location.replace(ctx + '/pages/login');
            return false;
        }
        return true;
    }

    async function loadMe() {
        var me = await api('/api/auth/me');
        store.set(store.token(), me);
        renderUser(me);
        return me;
    }

    function logout() {
        store.clear();
        location.replace(ctx + '/pages/login');
    }

    function param(name) {
        return new URLSearchParams(location.search).get(name);
    }

    /* ---------- boot ---------------------------------------------------- */

    document.querySelectorAll('[data-nav]').forEach(function (link) {
        if (link.dataset.nav === page) link.classList.add('is-active');
    });

    var sidebar = document.getElementById('sidebar');
    var scrim = document.getElementById('scrim');
    var menuBtn = document.getElementById('menuBtn');

    function closeNav() {
        if (sidebar) sidebar.classList.remove('open');
        if (scrim) scrim.classList.remove('show');
    }

    if (menuBtn && sidebar) {
        menuBtn.addEventListener('click', function () {
            sidebar.classList.toggle('open');
            if (scrim) scrim.classList.toggle('show');
        });
    }
    if (scrim) scrim.addEventListener('click', closeNav);

    document.addEventListener('click', function (e) {
        var el = e.target instanceof Element ? e.target.closest('[data-action="logout"]') : null;
        if (el) { e.preventDefault(); logout(); }
    });

    if (!PUBLIC_PAGES[page] && !store.token()) {
        location.replace(ctx + '/pages/login');
    } else {
        var cachedUser = store.user();
        if (cachedUser) renderUser(cachedUser);
    }

    window.UCUA = {
        ctx: ctx,
        page: page,
        api: api,
        ApiError: ApiError,
        store: store,
        toast: toast,
        modal: modal,
        confirmDialog: confirmDialog,
        escapeHtml: escapeHtml,
        fmtDate: fmtDate,
        statusKey: statusKey,
        initials: initials,
        roleLabel: roleLabel,
        renderUser: renderUser,
        requireAuth: requireAuth,
        loadMe: loadMe,
        setBusy: setBusy,
        logout: logout,
        param: param
    };
})();
