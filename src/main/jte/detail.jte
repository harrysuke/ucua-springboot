@param String ctx
@param String reportId
@template.layout(ctx = ctx, title = "Report detail", active = "reports", content = @`
<div id="report-root" data-id="${reportId}">
    <div class="page-head">
        <div>
            <a class="small" href="${ctx}/pages/reports">← Back to reports</a>
            <h1 class="page-title" id="report-title">Report #${reportId}</h1>
            <p class="page-sub" id="report-sub">Loading report…</p>
        </div>
        <div class="page-actions" id="report-actions"></div>
    </div>

    <div class="detail-grid">
        <div class="stack-gap">
            <section class="card">
                <header class="card-head">
                    <div class="card-title">Details</div>
                    <span id="report-type"></span>
                </header>
                <div class="card-body">
                    <dl class="kv" id="report-kv">
                        <div class="skeleton" style="height:120px;grid-column:1/-1"></div>
                    </dl>
                </div>
            </section>

            <section class="card">
                <header class="card-head">
                    <div class="card-title">Observation narrative</div>
                </header>
                <div class="card-body" id="report-narrative">
                    <div class="skeleton" style="height:70px"></div>
                </div>
            </section>

            <section class="card">
                <header class="card-head">
                    <div>
                        <div class="card-title">Photo evidence</div>
                        <div class="card-sub" id="photo-count">Loading…</div>
                    </div>
                </header>
                <div class="card-body" id="photos">
                    <div class="skeleton" style="height:120px"></div>
                </div>
            </section>

            <section class="card">
                <header class="card-head">
                    <div class="card-title">Timeline</div>
                </header>
                <div class="card-body">
                    <ul class="timeline" id="timeline">
                        <li><div class="skeleton" style="height:60px"></div></li>
                    </ul>
                </div>
            </section>
        </div>

        <div class="stack-gap">
            <section class="card">
                <header class="card-head"><div class="card-title">Status</div></header>
                <div class="card-body" id="status-card">
                    <div class="skeleton" style="height:60px"></div>
                </div>
            </section>

            <section class="card">
                <header class="card-head"><div class="card-title">Observer</div></header>
                <div class="card-body" id="observer-card">
                    <div class="skeleton" style="height:60px"></div>
                </div>
            </section>

            <section class="card">
                <header class="card-head"><div class="card-title">Workflow</div></header>
                <div class="card-body" id="workflow-card">
                    <div class="skeleton" style="height:80px"></div>
                </div>
            </section>

            <section class="card" id="upload-card">
                <header class="card-head">
                    <div>
                        <div class="card-title">Attach evidence</div>
                        <div class="card-sub">Upload a photo to this report</div>
                    </div>
                </header>
                <div class="card-body">
                    <form id="upload-form" class="stack">
                        <label class="field">
                            <span class="label">Photo type</span>
                            <select class="select" id="photo-type">
                                <option value="evidence">Photo evidence</option>
                                <option value="violator">Violator evidence</option>
                                <option value="closed">Closing evidence</option>
                            </select>
                        </label>
                        <label class="field">
                            <span class="label">Image file</span>
                            <input class="input" type="file" id="photo-file" accept="image/jpeg,image/png,image/webp,image/gif">
                        </label>
                        <button class="btn btn-primary" type="submit">Upload photo</button>
                        <p class="hint" style="margin:0">JPG, PNG, WEBP or GIF · max 8 MB</p>
                    </form>
                </div>
            </section>
        </div>
    </div>
</div>

<script src="${ctx}/js/pages/detail.js" defer></script>
`)
