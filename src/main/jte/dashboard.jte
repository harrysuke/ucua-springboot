@param String ctx
@template.layout(ctx = ctx, title = "Dashboard", active = "dashboard", content = @`
<div class="page-head">
    <div>
        <h1 class="page-title">Dashboard</h1>
        <p class="page-sub" id="welcome">Loading your workspace…</p>
    </div>
    <div class="page-actions">
        <button type="button" class="btn btn-ghost" id="refreshBtn">Refresh</button>
        <a class="btn btn-primary" href="${ctx}/pages/reports/new">New report</a>
    </div>
</div>

<section class="stat-grid" id="stats">
    @for(int i = 0; i < 6; i++)
    <div class="stat">
        <div class="skeleton" style="height:11px;width:65%"></div>
        <div class="skeleton" style="height:28px;width:42%;margin-top:12px"></div>
        <div class="skeleton" style="height:10px;width:52%;margin-top:10px"></div>
    </div>
    @endfor
</section>

<div class="grid grid-3 mt-16">
    <section class="card span-2">
        <header class="card-head">
            <div>
                <div class="card-title">Six-month trend</div>
                <div class="card-sub">Unsafe conditions vs unsafe actions</div>
            </div>
            <div class="legend">
                <span><i style="background:#4f46e5"></i>Unsafe condition</span>
                <span><i style="background:#f59e0b"></i>Unsafe action</span>
            </div>
        </header>
        <div class="card-body" id="trend">
            <div class="skeleton" style="height:190px"></div>
        </div>
    </section>

    <section class="card">
        <header class="card-head">
            <div>
                <div class="card-title">Approval queues</div>
                <div class="card-sub">Items awaiting your role</div>
            </div>
            <a class="small" href="${ctx}/pages/approvals">Open</a>
        </header>
        <div class="card-body" id="queues">
            <div class="skeleton" style="height:150px"></div>
        </div>
    </section>
</div>

<div class="grid grid-2 mt-16">
    <section class="card">
        <header class="card-head">
            <div class="card-title">Recent activity</div>
            <a class="small" href="${ctx}/pages/reports">All reports</a>
        </header>
        <div class="card-body" id="activity">
            <div class="skeleton" style="height:140px"></div>
        </div>
    </section>

    <section class="card">
        <header class="card-head">
            <div>
                <div class="card-title">Top reporters</div>
                <div class="card-sub">Last six months</div>
            </div>
        </header>
        <div class="card-body" id="leaderboard">
            <div class="skeleton" style="height:140px"></div>
        </div>
    </section>
</div>

<script src="${ctx}/js/pages/dashboard.js" defer></script>
`)
