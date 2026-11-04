@import gg.jte.Content

@param String ctx
@param String page
@param String title
@param String subtitle
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
<body data-ctx="${ctx}" data-page="${page}">
<div class="auth">
    <aside class="auth-aside">
        <div class="auth-brand">
            <div class="brand-mark">U</div>
            <div class="brand-text">
                <strong>UCUA</strong>
                <span>U-See · U-Act</span>
            </div>
        </div>

        <div class="auth-pitch">
            <h2 class="auth-headline">See something.<br>Say something.<br>Make it safer.</h2>
            <p class="auth-lead">UCUA is the unsafe condition and unsafe action reporting platform for PSSD HSE — capture observations, attach evidence, and route every report through the approval chain.</p>
            <div class="auth-features">
                <div class="auth-feature"><span class="tick">✓</span><span>Report unsafe conditions and unsafe actions in seconds</span></div>
                <div class="auth-feature"><span class="tick">✓</span><span>Photo evidence with a full audit timeline</span></div>
                <div class="auth-feature"><span class="tick">✓</span><span>HSE Officer → HOS → HOD approval workflow</span></div>
                <div class="auth-feature"><span class="tick">✓</span><span>AI-assisted offence suggestions and semantic search</span></div>
            </div>
        </div>

        <p class="auth-foot">PSSD Health, Safety &amp; Environment · Internal use only</p>
    </aside>

    <div class="auth-panel">
        <div class="auth-card">
            <h1>${title}</h1>
            <p class="auth-sub">${subtitle}</p>
            ${content}
        </div>
    </div>
</div>
<div id="toast-host" class="toast-host" aria-live="polite"></div>
</body>
</html>
