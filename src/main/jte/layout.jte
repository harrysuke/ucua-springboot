@import gg.jte.Content

@param String ctx
@param String title
@param String active
@param Content content
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="color-scheme" content="light">
    <title>${title} · UCUA</title>
    <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'%3E%3Crect width='64' height='64' rx='14' fill='%234f46e5'/%3E%3Ctext x='32' y='45' font-family='Arial' font-size='34' font-weight='bold' fill='white' text-anchor='middle'%3EU%3C/text%3E%3C/svg%3E">
    <link rel="stylesheet" href="${ctx}/css/app.css">
    <script src="${ctx}/js/app.js" defer></script>
</head>
<body data-ctx="${ctx}" data-page="${active}">
<div class="shell">
    <aside class="sidebar" id="sidebar">
        <div class="brand">
            <div class="brand-mark">U</div>
            <div class="brand-text">
                <strong>UCUA</strong>
                <span>U-See · U-Act</span>
            </div>
        </div>

        <nav class="nav" aria-label="Primary">
            <div class="nav-label">Workspace</div>
            <a class="nav-link" data-nav="dashboard" href="${ctx}/pages/dashboard">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1.6"/><rect x="14" y="3" width="7" height="7" rx="1.6"/><rect x="3" y="14" width="7" height="7" rx="1.6"/><rect x="14" y="14" width="7" height="7" rx="1.6"/></svg>
                <span>Dashboard</span>
            </a>
            <a class="nav-link" data-nav="reports" href="${ctx}/pages/reports">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M8 13h8M8 17h6"/></svg>
                <span>Reports</span>
            </a>
            <a class="nav-link" data-nav="new" href="${ctx}/pages/reports/new">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/></svg>
                <span>New report</span>
            </a>
            <a class="nav-link" data-nav="approvals" href="${ctx}/pages/approvals">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
                <span>Approvals</span>
            </a>

            <div class="nav-label">Reference</div>
            <a class="nav-link" data-nav="lookups" href="${ctx}/pages/lookups">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M8 6h13M8 12h13M8 18h13"/><circle cx="3.5" cy="6" r="1"/><circle cx="3.5" cy="12" r="1"/><circle cx="3.5" cy="18" r="1"/></svg>
                <span>Lookups</span>
            </a>
            <a class="nav-link" data-nav="ai" href="${ctx}/pages/ai">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l1.9 4.8L18.7 9.7l-4.8 1.9L12 16.4l-1.9-4.8L5.3 9.7l4.8-1.9z"/><path d="M19 15l.9 2.1L22 18l-2.1.9L19 21l-.9-2.1L16 18l2.1-.9z"/></svg>
                <span>AI assistant</span>
            </a>
            <a class="nav-link" data-nav="health" href="${ctx}/pages/health">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
                <span>System health</span>
            </a>
        </nav>

        <div class="sidebar-foot">
            <div class="user-chip">
                <div class="avatar js-avatar">–</div>
                <div class="meta">
                    <div class="name js-user-name">Loading…</div>
                    <div class="role js-user-role">Signed in</div>
                </div>
            </div>
            <button type="button" class="btn btn-signout" data-action="logout">Sign out</button>
        </div>
    </aside>

    <div class="main">
        <header class="topbar">
            <button type="button" class="icon-btn menu-btn" id="menuBtn" aria-label="Toggle navigation">☰</button>
            <div class="topbar-title">${title}</div>
            <div class="topbar-actions">
                <a class="btn btn-ghost btn-sm" href="${ctx}/pages/reports/new">New report</a>
                <div class="topbar-user">
                    <div class="avatar avatar-sm js-avatar">–</div>
                    <div class="topbar-user-meta">
                        <span class="js-user-name">Loading…</span>
                        <span class="js-user-role muted">Signed in</span>
                    </div>
                </div>
            </div>
        </header>
        <main class="content">
            ${content}
        </main>
    </div>
</div>
<div class="scrim" id="scrim"></div>
<div id="toast-host" class="toast-host" aria-live="polite"></div>
</body>
</html>
