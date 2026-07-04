# Issue tracker: GitHub

Issues and PRDs for this repo live as GitHub issues at **kcuber118/MouseGesture**. Use the `gh` CLI for all operations.

## Conventions

- **Create an issue**: `gh issue create --title "..." --body "..."`. Use a heredoc for multi-line bodies.
- **Read an issue**: `gh issue view <number> --comments`, filtering comments by `jq` and also fetching labels.
- **List issues**: `gh issue list --state open --json number,title,body,labels,comments --jq '[.[] | {number, title, body, labels: [.labels[].name], comments: [.comments[].body]}]'` with appropriate `--label` and `--state` filters.
- **Comment on an issue**: `gh issue comment <number> --body "..."`
- **Apply / remove labels**: `gh issue edit <number> --add-label "..."` / `--remove-label "..."`
- **Close**: `gh issue close <number> --comment "..."`

`gh` infers the repo from the clone's `origin` remote automatically.

## Pull requests as a triage surface

**PRs as a request surface: no.** `/triage` only processes GitHub issues; it does not pull in external PRs.

If this ever flips to `yes`, PRs would run through the same labels and states as issues using the `gh pr` equivalents (`gh pr view`, `gh pr list` filtered to `authorAssociation` of `CONTRIBUTOR` / `FIRST_TIME_CONTRIBUTOR` / `NONE`, `gh pr comment`, `gh pr edit --add-label`, `gh pr close`). GitHub shares one number space across issues and PRs, so a bare `#42` may be either — resolve with `gh pr view 42`, then fall back to `gh issue view 42`.

## When a skill says "publish to the issue tracker"

Create a GitHub issue.

## When a skill says "fetch the relevant ticket"

Run `gh issue view <number> --comments`.

## History

This repo previously used a local-markdown tracker under `.scratch/<feature-slug>/`. Its contents (PRD + 5 issues for `touchpad-overlay`) were migrated to GitHub Issues on 2026-07-04 and the local files removed. All work now lives in GitHub Issues.
