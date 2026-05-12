---
name: skill-creator
description: Scaffolds, validates, and creates new OpenCode agent skills based on user requests.
license: MIT
compatibility: opencode
metadata:
  audience: developers
  workflow: agent-customization
---

## What I do

I help users create valid, well-structured OpenCode agent skills. When a user asks to create a new skill, I will guide them through gathering requirements, validating constraints, and generating the final `SKILL.md` file in the correct directory.

## When to use me

Use me whenever the user says things like:

- "Create a new skill for..."
- "I want to teach you how to..."
- "Make a skill that..."
- "Let's add a custom agent tool to do..."

## Process for Creating a New Skill

Whenever you are invoked to create a skill, follow these steps strictly:

### 1. Gather Information

If the user hasn't provided them, ask for:

- The **purpose** of the skill (what it should do).
- The preferred **name** for the skill.
- Whether it should be **project-local** or **global**.

### 2. Validate Constraints

Before generating any files, validate the metadata against OpenCode's strict rules:

**Name Rules:**

- Must be 1–64 characters.
- Must be lowercase alphanumeric with single hyphen separators (`^[a-z0-9]+(-[a-z0-9]+)*$`).
- Cannot start or end with `-`, or contain `--`.
- _Crucial:_ The folder name must exactly match the skill name.

**Description Rules:**

- Must be 1-1024 characters.
- Must be highly specific so agents know exactly when to load it via the `skill` tool.

### 3. Determine the File Path

Based on the user's preference, prepare the destination path:

- **Project-local:** `.opencode/skills/<name>/SKILL.md` (Use this as the default if unspecified)
- **Global:** `~/.config/opencode/skills/<name>/SKILL.md`
- _(Note: Also support `.claude/skills/` or `.agents/skills/` if requested)._

### 4. Scaffold the Content

Draft the content of the `SKILL.md` file. It **must** include the YAML frontmatter.

**Frontmatter Template:**

```yaml
---
name: <validated-name>
description: <validated-description>
license: MIT
compatibility: opencode
---
```

_(Only `name` and `description` are required. Do not add unknown fields. `metadata` is an optional string-to-string map)._

**Body Template:**

Include clear, concise markdown sections. Recommended structure:

- `## What I do`: Bulleted list of the skill's capabilities.
- `## When to use me`: Context for the agent on when to trigger this skill.
- `## Instructions`: Step-by-step rules, commands, or context the agent needs to follow when executing the skill.

### 5. Write the File

Use your file system tools to:

1. Create the directory: e.g., `mkdir -p .opencode/skills/<name>`
2. Write the file: e.g., `.opencode/skills/<name>/SKILL.md`

### 6. Advise on Permissions

After successful creation, briefly remind the user that if the skill does not load, they should check their `opencode.json` permissions (`allow`, `ask`, `deny`). Give them a quick example if they want to restrict access to it.
