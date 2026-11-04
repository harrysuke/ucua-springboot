@param String appName
@param String version
@param String generatedAt
@param boolean showEndpoints
@param java.util.List<String> endpoints
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${appName} · Home</title>
    <style>
        :root { color-scheme: light dark; }
        body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; max-width: 720px; margin: 3rem auto; padding: 0 1rem; line-height: 1.6; }
        h1 { margin-bottom: .25rem; }
        .meta { color: #6b7280; font-size: .9rem; }
        .badge { display: inline-block; background: #2563eb; color: #fff; border-radius: 999px; padding: .1rem .6rem; font-size: .8rem; }
        ul { padding-left: 1.2rem; }
        code { background: rgba(127,127,127,.15); padding: .1rem .35rem; border-radius: 4px; }
        footer { margin-top: 2rem; border-top: 1px solid #e5e7eb; padding-top: 1rem; color: #6b7280; font-size: .85rem; }
    </style>
</head>
<body>
    <h1>Welcome to ${appName} <span class="badge">v${version}</span></h1>
    <p class="meta">Rendered with the JTE template engine at ${generatedAt}</p>

    <p>
        This page is server-side rendered by a JTE <code>.jte</code> template.
        Model values are passed as type-safe <code>@param</code> declarations and are
        HTML-escaped automatically.
    </p>

    @if(showEndpoints)
    <h2>Available API endpoints</h2>
    <ul>
        @for(String endpoint : endpoints)
        <li><code>${endpoint}</code></li>
        @endfor
    </ul>
    @endif

    <footer>
        JTE (Java Template Engine) · context path <code>/ucua</code> · page route <code>/ucua/pages/home</code>
    </footer>
</body>
</html>
