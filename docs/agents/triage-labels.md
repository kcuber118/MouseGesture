# Triage Labels

The skills speak in terms of five canonical triage roles. This file maps those roles to the actual label strings used in this repo's GitHub issue tracker.

Because this repo uses **GitHub Issues**, a role's "label" is a real GitHub label applied via `gh issue edit <number> --add-label "<label>"` (and removed with `--remove-label`).

| Label in mattpocock/skills | Label in our tracker | Meaning                                  |
| -------------------------- | -------------------- | ---------------------------------------- |
| `needs-triage`             | `needs-triage`       | Maintainer needs to evaluate this issue  |
| `needs-info`               | `needs-info`         | Waiting on reporter for more information |
| `ready-for-agent`          | `ready-for-agent`    | Fully specified, ready for an AFK agent  |
| `ready-for-human`          | `ready-for-human`    | Requires human implementation            |
| `wontfix`                  | `wontfix`            | Will not be actioned                     |

When a skill mentions a role (e.g. "apply the AFK-ready triage label"), apply the corresponding label from the right-hand column with `gh issue edit`.

## Ensure the labels exist

These five labels must exist on the GitHub repo before `/triage` runs. If any is missing, create it (color is your choice — the values below are suggestions):

```sh
gh label create needs-triage    --color BFD4F2 || true
gh label create needs-info      --color FBCA04 || true
gh label create ready-for-agent --color 0E8A16 || true
gh label create ready-for-human --color 1D76DB || true
gh label create wontfix         --color FFFFFF || true
```

Edit the right-hand column above to match whatever vocabulary you actually use — and update the `gh label create` names to match.
