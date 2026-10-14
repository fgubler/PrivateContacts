name: code-review
description: >
This skill should be used when the user asks to 'review my code',
'review this PR', 'do a code review', 'check my changes for issues',
'review the diff', 'audit my changes', 'security review', 'look for
bugs in my changes', 'review the codebase', 'run a full review',
mentions 'code quality check', 'pre-merge review', or wants a
structured review of changes before merging.
version: 1.0.0

# Code Review

Perform structured, persistent code reviews with branch-scoped findings tracking.
Use plain Git-Commands rather than Github or Gitlab CLI.

**Important context:** The code under review may have been written by another AI model (e.g. GitHub Copilot, Codex, GPT-4) or by a more junior developer — not necessarily by the person invoking this skill. Apply a higher level of scrutiny than you would for code from a trusted senior engineer. AI-generated code in particular can look plausible while containing subtle logic errors, misuse of APIs, incomplete error handling, copy-paste inconsistencies, or stub implementations that appear complete but are not. Be thorough and do not discount issues as nitpicks unless they genuinely are.
## Invocation Modes

- **Default (diff mode)**: Review the diff that would go into a PR against `origin/main` (`git diff origin/main...HEAD`)

- **Full mode**: Review the entire tracked codebase — invoke by passing `full` as a parameter (e.g. `/review-code full`)

If the diff is empty and no mode was explicitly specified, stop and ask the user whether to run a full codebase review or wait until changes exist.
## Severity Classifications
Classify every finding as one of:
- **Blocking** — must be resolved before merge
- **Suggestion** — should consider; not required for merge
- **Nitpick** — minor style, naming, or formatting; low impact

**Strict bias**: when severity is ambiguous, be more strict.
## Review Focus
Assess findings in this priority order, but do **not** limit the review to these areas — raise any other relevant finding (API design, testing gaps, architecture, documentation, etc.):
1. **Security** — identify both vulnerabilities present in the code AND missing security controls (absent auth, CSRF protection, rate limiting, input validation, error sanitization, insecure defaults). Flag omissions, not just commissions.
2. **Correctness** — bugs, logic errors, null-safety issues, edge cases, race conditions
3. **Maintainability** — readability, coupling, mixed concerns, missing documentation on public API
4. **Performance** — classify as `Blocking` only when the fix does not reduce maintainability; otherwise `Suggestion`
## Findings File
Findings are written to `aiTools/review/<branch-name>.md`. On subsequent runs on the same branch, the file is updated — not replaced. Findings marked `ignored` or `explained` by the user are not re-raised unless the code near the cited location has changed since acknowledgment.