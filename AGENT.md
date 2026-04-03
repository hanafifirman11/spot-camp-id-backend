# Agent Workflow Rules

## Git Flow (Mandatory)
- Do not push directly to `main`.
- Always create a working branch per task/issue.
- Open a Pull Request for every change and merge via PR.
- Direct push to `main` is allowed only if explicitly requested by the user in the current task.

## Recommended Branch Naming
- `feature/issue-<number>-<short-desc>`
- `fix/issue-<number>-<short-desc>`
- `chore/<short-desc>`

## Minimum PR Checklist
- `mvn test` passes locally (or clearly explain blockers).
- Scope matches the issue/request.
- No unrelated file changes.
