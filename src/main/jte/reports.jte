@param String ctx
@template.layout(ctx = ctx, title = "Reports", active = "reports", content = @`
<div class="page-head">
    <div>
        <h1 class="page-title">Reports</h1>
        <p class="page-sub">Every unsafe condition and unsafe action you can see, with live filters.</p>
    </div>
    <div class="page-actions">
        <a class="btn btn-primary" href="${ctx}/pages/reports/new">New report</a>
    </div>
</div>

<form class="filters" id="filters">
    <label class="field grow">
        <span class="label">Search</span>
        <input class="input" id="q" placeholder="Search description, violator, observer…">
    </label>
    <label class="field">
        <span class="label">Type</span>
        <select class="select" id="type">
            <option value="">All types</option>
            <option value="UC">Unsafe condition</option>
            <option value="UA">Unsafe action</option>
        </select>
    </label>
    <label class="field">
        <span class="label">Status</span>
        <select class="select" id="status">
            <option value="">All statuses</option>
            <option value="Draft">Draft</option>
            <option value="Pending PSSD HSE Officer">Pending PSSD HSE Officer</option>
            <option value="Approved by PSSD HSE Officer">Approved by PSSD HSE Officer</option>
            <option value="Approved by HOS">Approved by HOS</option>
            <option value="Approved by HOD">Approved by HOD</option>
            <option value="Rejected by PSSD HSE Officer">Rejected by PSSD HSE Officer</option>
            <option value="Rejected by HOS">Rejected by HOS</option>
            <option value="Rejected by HOD">Rejected by HOD</option>
            <option value="Rejected">Rejected</option>
        </select>
    </label>
    <label class="check" style="padding-bottom:10px">
        <input type="checkbox" id="mine"> Only mine
    </label>
    <button class="btn btn-primary" type="submit">Apply filters</button>
    <button class="btn btn-ghost" type="button" id="clearBtn">Clear</button>
</form>

<div id="reports-table">
    <div class="card">
        <div class="card-body">
            <div class="skeleton" style="height:220px"></div>
        </div>
    </div>
</div>

<script src="${ctx}/js/pages/reports.js" defer></script>
`)
