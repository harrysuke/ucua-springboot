@param String ctx
@param String mode
@param String reportId
@param String active
@template.layout(ctx = ctx, title = "New report", active = active, content = @`
<div id="form-root" data-mode="${mode}" data-id="${reportId}">
    <div class="page-head">
        <div>
            <a class="small" href="${ctx}/pages/reports">← Back to reports</a>
            <h1 class="page-title" id="form-title">New report</h1>
            <p class="page-sub" id="form-sub">Capture an unsafe condition or unsafe action.</p>
        </div>
        <div class="page-actions">
            <span class="badge badge-neutral" id="form-mode-badge">${mode}</span>
        </div>
    </div>

    <form id="report-form" class="stack-gap" novalidate>
        <section class="card">
            <header class="card-head">
                <div>
                    <div class="card-title">Report type</div>
                    <div class="card-sub">What are you reporting?</div>
                </div>
            </header>
            <div class="card-body">
                <div class="segmented" id="type-toggle">
                    <button type="button" data-type="Unsafe Condition" class="is-active">Unsafe condition</button>
                    <button type="button" data-type="Unsafe Action">Unsafe action</button>
                </div>
                <input type="hidden" id="formType" value="Unsafe Condition">
                <p class="hint mt-8">Unsafe conditions describe hazardous situations. Unsafe actions describe at-risk behaviour.</p>
            </div>
        </section>

        <section class="card">
            <header class="card-head">
                <div class="card-title">Observation</div>
            </header>
            <div class="card-body">
                <div class="form-grid">
                    <label class="field">
                        <span class="label">Location <span class="req">*</span></span>
                        <select class="select" id="locationId">
                            <option value="">Loading locations…</option>
                        </select>
                    </label>
                    <label class="field">
                        <span class="label">Offence code <span class="req" data-req="ua">*</span></span>
                        <select class="select" id="offenceCode">
                            <option value="">Loading offences…</option>
                        </select>
                    </label>
                    <label class="field full">
                        <span class="label">Condition details <span class="req" data-req="uc">*</span></span>
                        <textarea class="textarea" id="conditionDetails" placeholder="Describe what you observed, where it was, and why it is unsafe…"></textarea>
                        <div class="row wrap mt-8">
                            <button type="button" class="btn btn-ghost btn-sm" id="suggestBtn">✦ Suggest offence with AI</button>
                            <span class="hint" id="suggest-hint"></span>
                        </div>
                        <div id="suggestions" class="mt-12"></div>
                    </label>
                </div>
            </div>
        </section>

        <section class="card">
            <header class="card-head">
                <div>
                    <div class="card-title">Violator &amp; action</div>
                    <div class="card-sub">Who was involved, and what did you do?</div>
                </div>
            </header>
            <div class="card-body">
                <div class="form-grid">
                    <label class="field">
                        <span class="label">Violator name</span>
                        <input class="input" id="violatorName" placeholder="Person involved (optional)">
                    </label>
                    <label class="field">
                        <span class="label">Violator company <span class="req" data-req="ua">*</span></span>
                        <input class="input" id="violatorCompany" list="company-list" placeholder="Company or contractor">
                        <datalist id="company-list"></datalist>
                    </label>
                    <label class="field">
                        <span class="label">Violator department</span>
                        <input class="input" id="violatorDept" list="dept-list" placeholder="Department">
                        <datalist id="dept-list"></datalist>
                    </label>
                    <label class="field">
                        <span class="label">Action taken</span>
                        <input class="input" id="actionTaken" placeholder="e.g. Stopped the job and briefed the crew">
                    </label>
                </div>
            </div>
        </section>

        <div class="form-error" id="form-error" hidden></div>

        <div class="row wrap" id="form-actions">
            <div class="skeleton" style="height:38px;width:260px"></div>
        </div>
    </form>
</div>

<script src="${ctx}/js/pages/form.js" defer></script>
`)
