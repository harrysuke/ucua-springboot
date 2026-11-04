@param String ctx
@template.layout(ctx = ctx, title = "Lookups", active = "lookups", content = @`
<div class="page-head">
    <div>
        <h1 class="page-title">Reference data</h1>
        <p class="page-sub">Locations, offences, companies and departments used by the reporting form.</p>
    </div>
    <div class="page-actions">
        <button type="button" class="btn btn-ghost" id="lookup-refresh">Refresh</button>
    </div>
</div>

<section class="card">
    <div class="card-body">
        <div class="tabs" id="lookup-tabs">
            <button type="button" class="tab is-active" data-tab="locations">Locations <span class="badge badge-neutral" id="count-locations">0</span></button>
            <button type="button" class="tab" data-tab="offences">Offences <span class="badge badge-neutral" id="count-offences">0</span></button>
            <button type="button" class="tab" data-tab="companies">Companies <span class="badge badge-neutral" id="count-companies">0</span></button>
            <button type="button" class="tab" data-tab="departments">Departments <span class="badge badge-neutral" id="count-departments">0</span></button>
        </div>

        <div class="row wrap mb-12" id="lookup-search-wrap" hidden>
            <input class="input" id="lookup-search" placeholder="Filter offences by code, keyword or description…" style="max-width:420px">
        </div>

        <div id="lookup-panel">
            <div class="skeleton" style="height:260px"></div>
        </div>
    </div>
</section>

<script src="${ctx}/js/pages/lookups.js" defer></script>
`)
