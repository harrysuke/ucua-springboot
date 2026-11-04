@param String ctx
@template.auth(ctx = ctx, page = "login", title = "Welcome back", subtitle = "Sign in with your UCUA staff credentials to continue.", content = @`
<div class="auth-tabs" role="tablist">
    <button type="button" class="auth-tab is-active" data-tab="login">Sign in</button>
    <button type="button" class="auth-tab" data-tab="forgot">Forgot password</button>
</div>

<form id="login-form" class="stack" autocomplete="on">
    <label class="field">
        <span class="label">Staff ID</span>
        <input class="input" id="staffId" name="staffId" autocomplete="username" placeholder="e.g. 12345" required>
    </label>
    <label class="field">
        <span class="label">Password</span>
        <input class="input" id="password" name="password" type="password" autocomplete="current-password" placeholder="Enter your password" required>
    </label>
    <div class="form-error" id="login-error" hidden></div>
    <button class="btn btn-primary btn-block" type="submit" id="login-btn">Sign in</button>
</form>

<form id="forgot-form" class="stack" hidden>
    <p class="hint" style="margin:0">Enter your staff ID and PSSD/ICT will contact you to reset your password.</p>
    <label class="field">
        <span class="label">Staff ID</span>
        <input class="input" id="forgot-staffId" placeholder="e.g. 12345" required>
    </label>
    <label class="field">
        <span class="label">Email <span class="muted small">(optional)</span></span>
        <input class="input" id="forgot-email" type="email" placeholder="you@company.com">
    </label>
    <div class="form-note" id="forgot-note" hidden></div>
    <button class="btn btn-primary btn-block" type="submit">Request reset</button>
</form>

<div class="auth-alt">New to UCUA? <a href="${ctx}/pages/register">Create an account</a></div>
<div class="api-status" id="api-status"><span class="dot"></span><span class="txt">Checking API…</span></div>

<script src="${ctx}/js/pages/login.js" defer></script>
`)
