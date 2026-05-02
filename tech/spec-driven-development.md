# Spec-Driven Development Standard

This document defines the default workflow for building and changing features in this repository and in future projects that copy this standard.

## Principles

- Write the spec before writing code.
- Keep each change small and tied to one spec.
- Make rollback easy by isolating work into a branch and a small commit set.
- Keep implementation, verification, and decision records linked together.

## Workflow

1. Draft a spec in `tech/specs/`.
2. Review scope, data, and acceptance criteria before implementation.
3. Create a feature branch for the spec.
4. Implement the smallest useful slice.
5. Verify the result with tests, screenshots, or API calls.
6. Merge only when the spec and the implementation match.

## Required Spec Contents

Every spec should include:

- Goal
- Non-goals
- Scope
- User flow or API flow
- Data changes
- Acceptance criteria
- Risks and rollback notes

## Branching

- `main`: stable, release-ready state.
- `develop`: integration branch for active work.
- `feature/<name>`: one feature or spec.
- `fix/<name>`: one bug fix.

## Commit Rules

- Prefer one logical change per commit.
- Use a message that names the scope, for example `feat(admin): add intake form`.
- Do not mix unrelated refactors, formatting, and feature work in the same commit.

## Rollback Rules

- If a branch is not merged, drop the branch.
- If a change is merged, revert the commit or merge commit.
- Do not rewrite shared history unless there is a strong reason.

## Review Checklist

- The implementation matches the spec.
- The change is limited to the agreed scope.
- The verification steps are reproducible.
- The rollback path is clear.

## Reference Files

- Spec template: `tech/spec-template.md`
- Design decisions: `tech/decisions/`
- Feature specs: `tech/specs/`

