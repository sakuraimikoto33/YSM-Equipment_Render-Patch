# Branch ownership

Read this reference only for ownership questions, mixed root files, or work spanning main and mc branches.

## Main

Commit repository instructions, `.agents/`, and `.gitignore` on `main`. In particular, edit and commit `.agents/active-minecraft-branches.txt` only on `main`; version branches receive it only through a main merge.

## Minecraft branches

Commit `src/`, Gradle wrapper files, Minecraft-specific documentation, loader integration, resources, and Minecraft-specific configuration on the matching `mc/<minecraft-version>` branch.

## Mixed files

Treat `README.md`, localized root README files, root `build.gradle`, `settings.gradle`, and `gradle.properties` by semantic hunk. Repository-wide guidance belongs to `main`; Minecraft, loader, runtime distribution, dependency, or build hunks belong to the target `mc/*`.

If path classification and semantic ownership disagree, treat the result as mixed and inspect it manually. Scripts must not decide semantic hunk ownership or resolve conflicts.
