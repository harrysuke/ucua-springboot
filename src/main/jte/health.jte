@param String ctx
@template.layout(ctx = ctx, title = "System health", active = "health", content = @`
<div class="page-head">
    <div>
        <h1 class="page-title">System health</h1>
        <p class="page-sub">Live probe of the public health endpoint, refreshed every 10 seconds.</p>
    </div>
    <div class="page-actions">
        <button type="button" class="btn btn-ghost" id="health-refresh">Check now</button>
    </div>
</div>

<div class="grid grid-3">
    <section class="card span-2">
        <header class="card-head">
            <div class="card-title">API status</div>
            <span class="badge badge-neutral" id="health-badge">Checking…</span>
        </header>
        <div class="card-body" id="health-result">
            <div class="skeleton" style="height:120px"></div>
        </div>
    </section>

    <section class="card">
        <header class="card-head">
            <div class="card-title">Endpoints covered</div>
        </header>
        <div class="card-body">
            <ul class="endpoint-list">
                <li><code>GET</code> /api/health</li>
                <li><code>POST</code> /api/auth/login</li>
                <li><code>GET</code> /api/dashboard</li>
                <li><code>GET</code> /api/lookups</li>
                <li><code>GET</code> /api/reports</li>
                <li><code>POST</code> /api/reports</li>
                <li><code>POST</code> /api/workflow</li>
                <li><code>POST</code> /api/ai/ask</li>
            </ul>
        </div>
    </section>
</div>

<script src="${ctx}/js/pages/health.js" defer></script>
`)
