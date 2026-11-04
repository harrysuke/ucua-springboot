@param String ctx
@template.layout(ctx = ctx, title = "Approvals", active = "approvals", content = @`
<div class="page-head">
    <div>
        <h1 class="page-title">Approvals</h1>
        <p class="page-sub">Reports routed to your role, grouped by approval stage.</p>
    </div>
    <div class="page-actions">
        <button type="button" class="btn btn-ghost" id="approvals-refresh">Refresh</button>
    </div>
</div>

<div id="approvals">
    <div class="card">
        <div class="card-body">
            <div class="skeleton" style="height:240px"></div>
        </div>
    </div>
</div>

<script src="${ctx}/js/pages/approvals.js" defer></script>
`)
