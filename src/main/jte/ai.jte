@param String ctx
@template.layout(ctx = ctx, title = "AI assistant", active = "ai", content = @`
<div class="page-head">
    <div>
        <h1 class="page-title">AI assistant</h1>
        <p class="page-sub">Retrieval-augmented helpers backed by the project's offence catalogue and report corpus.</p>
    </div>
    <div class="page-actions">
        <span class="badge badge-neutral" id="ai-status-badge">Checking…</span>
    </div>
</div>

<div class="grid grid-2">
    <section class="card">
        <header class="card-head">
            <div>
                <div class="card-title">Suggest an offence</div>
                <div class="card-sub">Describe the condition, get ranked offence codes</div>
            </div>
        </header>
        <div class="card-body">
            <div class="stack">
                <label class="field">
                    <span class="label">Condition description</span>
                    <textarea class="textarea" id="ai-cond" placeholder="e.g. Trailing cable across the walkway near the compressor…"></textarea>
                </label>
                <button type="button" class="btn btn-primary" id="ai-suggest-btn">Suggest offence</button>
                <div id="ai-suggestions"></div>
            </div>
        </div>
    </section>

    <section class="card">
        <header class="card-head">
            <div>
                <div class="card-title">Find similar reports</div>
                <div class="card-sub">Semantic search across past reports</div>
            </div>
        </header>
        <div class="card-body">
            <div class="stack">
                <div class="form-grid">
                    <label class="field">
                        <span class="label">Report ID</span>
                        <input class="input" id="ai-report-id" inputmode="numeric" placeholder="e.g. 42">
                    </label>
                    <label class="field">
                        <span class="label">Results</span>
                        <select class="select" id="ai-limit">
                            <option value="3">3</option>
                            <option value="5" selected>5</option>
                            <option value="10">10</option>
                        </select>
                    </label>
                </div>
                <button type="button" class="btn btn-primary" id="ai-similar-btn">Find similar</button>
                <div id="ai-similar"></div>
            </div>
        </div>
    </section>

    <section class="card span-2">
        <header class="card-head">
            <div>
                <div class="card-title">Ask the assistant</div>
                <div class="card-sub">Streamed answer grounded in indexed offences and reports</div>
            </div>
        </header>
        <div class="card-body">
            <div class="stack">
                <div class="form-grid">
                    <label class="field">
                        <span class="label">Question</span>
                        <input class="input" id="ai-question" placeholder="What controls apply to working at height?">
                    </label>
                    <label class="field">
                        <span class="label">Source filter</span>
                        <select class="select" id="ai-source">
                            <option value="">Everything</option>
                            <option value="OFFENCE">Offence catalogue</option>
                            <option value="REPORT">Past reports</option>
                        </select>
                    </label>
                </div>
                <div class="row wrap">
                    <button type="button" class="btn btn-primary" id="ai-ask-btn">Ask assistant</button>
                    <button type="button" class="btn btn-ghost" id="ai-ask-clear">Clear</button>
                </div>
                <div class="ai-answer" id="ai-answer-wrap">
                    <div class="ai-answer-text" id="ai-answer"></div>
                    <div class="ai-citations" id="ai-citations"></div>
                </div>
            </div>
        </div>
    </section>

    <section class="card span-2">
        <header class="card-head">
            <div>
                <div class="card-title">Reindex corpus</div>
                <div class="card-sub">Rebuild embeddings for offences and reports · administrators only</div>
            </div>
        </header>
        <div class="card-body">
            <div class="row wrap">
                <button type="button" class="btn btn-ghost" id="ai-reindex-btn">Reindex now</button>
                <span class="hint" id="ai-reindex-result"></span>
            </div>
        </div>
    </section>
</div>

<script src="${ctx}/js/pages/ai.js" defer></script>
`)
