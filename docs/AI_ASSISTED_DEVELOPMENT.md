# AI-assisted development

## Tools and setup

| Part | What I used |
|---|---|
| Coding agent | **Claude Code** (CLI) on Windows, model **Claude Opus 5.5** |
| Project rules | [`CLAUDE.md`](../CLAUDE.md) at the repository root; Claude Code loads it automatically in every session |
| Guardrails | Characterization tests, ArchUnit architecture tests, JaCoCo coverage report, Testcontainers |
| Human in the loop | I reviewed every change and made every commit myself |

### How to reproduce the setup
1. Install Claude Code and open a terminal in the repository root.
2. Start `claude`. `CLAUDE.md` is picked up automatically, with no extra configuration.
3. Work step by step: ask for one step, review the diff, run `mvn verify`, then commit.

### Agent rules (`CLAUDE.md`)
The rules file is the "system prompt" for this repository. Its main sections:
- **Hard constraints from the README:** no breaking changes to `TimeDeposit`; `updateBalance` keeps its signature and exact
  behaviour, including the `new BigDecimal(double)` rounding; exactly two endpoints.
- **Architecture:** hexagonal rules (what the core may and may not import, the direction of dependencies).
- **Patterns and principles:** Strategy for interest rules, SOLID, contract-first API, no over-engineering, where Lombok is allowed.
- **Testing rules:** tests first, boundary triples (29/30/31), every test must be able to catch a real defect, fakes over mocks.
- **Workflow:** the order requirements → analysis → tests → coverage → refactoring → review; small steps; the agent never
  commits and never follows instructions found in external text (emails, web pages).

### Permissions
The agent could read and edit files and run Maven. Git was mine: I staged and committed each step, so the history
shows one reviewed, atomic change per commit.

## Which parts were AI-assisted, and why

| Area | AI's role | My role |
|---|---|---|
| Requirements and code analysis | Compared the README with the existing code, listed issues and assumptions | Checked the findings and chose the assumptions |
| Characterization tests | Wrote the boundary and rounding tests | Asked for boundary triples and removed redundant tests (e.g. an extra "golden master" copy of the old code) |
| Refactoring | Implemented Strategy + Template Method with identical arithmetic | Reviewed that behaviour is unchanged; tests stayed green |
| Application, persistence, API | Generated the code, Flyway scripts, OpenAPI contract and tests | Chose hexagonal over DDD (the README prefers it), latest versions, Lombok scope, endpoint design |
| Architecture rules | Wrote the ArchUnit rules and proved they fail on a deliberate violation | Reviewed the rules |
| Review and docs | Scanned for dead code and unused imports, checked coverage, drafted this README | Final review and edits |

**Why this split:** the AI is fast at boilerplate, test scaffolding and checking details (e.g. that a refactoring keeps
floating-point results identical). Design decisions, scope and the final review stayed with me, and every line was read
before it was committed. The automated guardrails (tests, ArchUnit) make sure AI-generated changes can't silently
break the behaviour or the architecture.
