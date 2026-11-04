@param String ctx
@template.auth(ctx = ctx, page = "register", title = "Create your account", subtitle = "Register with your staff details. A PSSD administrator will approve your access.", content = @`
<form id="register-form" class="stack">
    <div class="form-grid">
        <label class="field">
            <span class="label">Staff ID <span class="req">*</span></span>
            <input class="input" id="staffId" placeholder="e.g. 12345" autocomplete="username" required>
        </label>
        <label class="field">
            <span class="label">Full name <span class="req">*</span></span>
            <input class="input" id="name" placeholder="Your full name" autocomplete="name" required>
        </label>
        <label class="field full">
            <span class="label">Password <span class="req">*</span></span>
            <input class="input" id="password" type="password" placeholder="At least 6 characters" autocomplete="new-password" required>
        </label>
        <label class="field">
            <span class="label">Email</span>
            <input class="input" id="email" type="email" placeholder="you@company.com" autocomplete="email">
        </label>
        <label class="field">
            <span class="label">Phone number</span>
            <input class="input" id="phoneNo" placeholder="+60…" autocomplete="tel">
        </label>
        <label class="field">
            <span class="label">Department</span>
            <input class="input" id="department" placeholder="e.g. Operations">
        </label>
        <label class="field">
            <span class="label">Designation</span>
            <input class="input" id="designation" placeholder="e.g. Technician">
        </label>
        <label class="field full">
            <span class="label">Company ID <span class="muted small">(optional)</span></span>
            <input class="input" id="companyId" inputmode="numeric" placeholder="Defaults to 1">
        </label>
    </div>

    <div class="form-error" id="register-error" hidden></div>
    <button class="btn btn-primary btn-block" type="submit" id="register-btn">Create account</button>
</form>

<div class="form-note" id="register-success" hidden>
    <strong>Registration submitted.</strong>
    <p id="register-message" style="margin:6px 0 0">Your account is pending PSSD approval.</p>
    <a class="btn btn-ghost btn-sm mt-12" href="${ctx}/pages/login">Back to sign in</a>
</div>

<div class="auth-alt">Already registered? <a href="${ctx}/pages/login">Sign in</a></div>

<script src="${ctx}/js/pages/register.js" defer></script>
`)
