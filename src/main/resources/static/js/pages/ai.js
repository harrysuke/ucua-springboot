(function () {
    'use strict';
    if (!window.UCUA) return;

    var U = window.UCUA;
    if (!U.requireAuth()) return;

    var $ = function (s) { return document.querySelector(s); };
    var esc = U.escapeHtml;

    function note(message) {
        return '<p class="hint" style="margin:0">' + esc(message) + '</p>';
    }

    /* ---------- header ---------------------------------------------------- */

    U.api('/api/auth/me').then(function (me) {
        U.renderUser(me);
        var badge = $('#ai-status-badge');
        badge.className = 'badge badge-closed';
        badge.textContent = 'AI layer ready';
    }).catch(function () {
        var badge = $('#ai-status-badge');
        badge.className = 'badge badge-neutral';
        badge.textContent = 'Session active';
    });

    /* ---------- suggest offence ------------------------------------------ */

    $('#ai-suggest-btn').addEventListener('click', async function () {
        var text = $('#ai-cond').value.trim();
        var host = $('#ai-suggestions');
        if (!text) {
            U.toast('Enter a condition description', 'error');
            return;
        }

        var btn = this;
        U.setBusy(btn, true, 'Searching…');
        try {
            var res = await U.api('/api/ai/suggest-offence', { method: 'POST', body: { conditionDetails: text } });
            if (res.disabled) { host.innerHTML = note(res.message || 'AI layer is disabled.'); return; }

            var list = res.suggestions || [];
            if (!list.length) { host.innerHTML = note(res.message || 'No matching offence found.'); return; }

            host.innerHTML = list.map(function (s) {
                return '<div class="suggest" style="cursor:default">' +
                    '<strong>' + esc(s.offenceCode) + '</strong>' +
                    '<span>' + esc(s.offence) + '</span>' +
                    '<em>' + Math.round((s.score || 0) * 100) + '%</em>' +
                '</div>';
            }).join('');
        } catch (err) {
            host.innerHTML = note(err.message || 'Suggestion failed');
            U.toast(err.message || 'Suggestion failed', 'error');
        } finally {
            U.setBusy(btn, false);
        }
    });

    /* ---------- similar reports ------------------------------------------ */

    $('#ai-similar-btn').addEventListener('click', async function () {
        var id = parseInt($('#ai-report-id').value, 10);
        var host = $('#ai-similar');
        if (!id) {
            U.toast('Enter a report ID', 'error');
            return;
        }

        var btn = this;
        U.setBusy(btn, true, 'Searching…');
        try {
            var res = await U.api('/api/ai/similar', {
                method: 'POST',
                body: { reportId: id, limit: parseInt($('#ai-limit').value, 10) }
            });
            if (res.disabled) { host.innerHTML = note(res.message || 'AI layer is disabled.'); return; }

            var list = res.results || [];
            if (!list.length) { host.innerHTML = note(res.message || 'No similar reports found.'); return; }

            host.innerHTML = list.map(function (r) {
                return '<a class="suggest" href="' + U.ctx + '/pages/reports/' + r.id + '">' +
                    '<strong>#' + r.id + '</strong>' +
                    '<span>' + esc(r.snippet || r.formType || '') + '</span>' +
                    '<em>' + Math.round((r.score || 0) * 100) + '%</em>' +
                '</a>';
            }).join('');
        } catch (err) {
            host.innerHTML = note(err.message || 'Similarity search failed');
            U.toast(err.message || 'Similarity search failed', 'error');
        } finally {
            U.setBusy(btn, false);
        }
    });

    /* ---------- streamed Q&A --------------------------------------------- */

    function handleEvent(chunk) {
        var lines = chunk.split('\n');
        var event = 'message';
        var data = [];

        lines.forEach(function (line) {
            if (line.indexOf('event:') === 0) event = line.slice(6).trim();
            else if (line.indexOf('data:') === 0) data.push(line.slice(5).replace(/^ /, ''));
        });

        var text = data.join('\n');
        if (!text) return;

        if (event === 'citations') {
            $('#ai-citations').textContent = text;
            return;
        }

        var out = $('#ai-answer');
        out.textContent += (out.textContent ? ' ' : '') + text;
    }

    async function ask() {
        var question = $('#ai-question').value.trim();
        if (!question) {
            U.toast('Enter a question', 'error');
            return;
        }

        var out = $('#ai-answer');
        out.textContent = '';
        $('#ai-citations').textContent = '';

        var btn = $('#ai-ask-btn');
        U.setBusy(btn, true, 'Thinking…');
        try {
            var headers = { 'Content-Type': 'application/json' };
            var token = U.store.token();
            if (token) headers.Authorization = 'Bearer ' + token;

            var res = await fetch(U.ctx + '/api/ai/ask', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    question: question,
                    sourceType: $('#ai-source').value || null
                })
            });

            if (res.status === 401) { U.logout(); return; }
            if (!res.ok) {
                var errText = await res.text();
                throw new Error(errText || ('Request failed with status ' + res.status));
            }
            if (!res.body) {
                out.textContent = await res.text();
                return;
            }

            var reader = res.body.getReader();
            var decoder = new TextDecoder();
            var buffer = '';

            while (true) {
                var step = await reader.read();
                if (step.done) break;
                buffer += decoder.decode(step.value, { stream: true });

                var idx;
                while ((idx = buffer.indexOf('\n\n')) >= 0) {
                    var chunk = buffer.slice(0, idx);
                    buffer = buffer.slice(idx + 2);
                    handleEvent(chunk);
                }
            }
            if (buffer.trim()) handleEvent(buffer);
        } catch (err) {
            U.toast(err.message || 'Assistant failed', 'error');
            out.textContent = 'Assistant unavailable: ' + (err.message || 'unknown error');
        } finally {
            U.setBusy(btn, false);
        }
    }

    $('#ai-ask-btn').addEventListener('click', ask);
    $('#ai-ask-clear').addEventListener('click', function () {
        $('#ai-answer').textContent = '';
        $('#ai-citations').textContent = '';
        $('#ai-question').value = '';
    });

    /* ---------- reindex --------------------------------------------------- */

    $('#ai-reindex-btn').addEventListener('click', async function () {
        var btn = this;
        var out = $('#ai-reindex-result');
        U.setBusy(btn, true, 'Reindexing…');
        out.textContent = 'Rebuilding embeddings…';
        try {
            var res = await U.api('/api/ai/reindex', { method: 'POST' });
            out.textContent = (res.message || 'Reindex complete') +
                ' · offences: ' + (res.indexedOffences || 0) +
                ', reports: ' + (res.indexedReports || 0);
            U.toast('Corpus reindexed', 'success');
        } catch (err) {
            out.textContent = err.message || 'Reindex failed';
            U.toast(err.message || 'Reindex failed', 'error');
        } finally {
            U.setBusy(btn, false);
        }
    });
})();
