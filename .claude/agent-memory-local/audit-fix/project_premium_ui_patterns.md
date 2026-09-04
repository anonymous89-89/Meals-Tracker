---
name: project-premium-ui-patterns
description: Recurring bug patterns found in the Meal Tracker premium UI files added under ui/components, ui/screens, ui/utils, ui/integration
metadata:
  type: project
---

Premium UI files added to this project had several recurring issues fixed in May 2026:

**Why:** A batch of "premium UI" files was added without being compiled first. They had missing imports, a wrong API call, and duplicate top-level declarations.

**How to apply:** When reviewing new premium UI files in this project, check for these patterns first:

1. **Duplicate top-level declarations** — `Typography.kt` duplicated `AppTypography` and `PoppinsFamily` from `Type.kt`, and referenced non-existent `R.font.*` resources. `Type.kt` is canonical (Google Fonts, no bundled TTFs). Delete any file that re-declares `AppTypography`.

2. **Invalid TextField parameter** — `onFocusChanged` is not a `TextField` parameter in Material3. Focus tracking must use `Modifier.onFocusChanged { }` on the modifier chain instead.

3. **Missing `BorderStroke` import** — `BorderStroke` requires `import androidx.compose.foundation.BorderStroke`; it is not pulled in by `material3.*`.

4. **Experimental Slider thumb** — Using a custom `thumb` lambda in `Slider` requires `@OptIn(ExperimentalMaterial3Api::class)`.

5. **`InfiniteTransition.animateFloat` requires `infiniteRepeatable`** — Passing a bare `tween(...)` spec to `InfiniteTransition.animateFloat` is a type error. Always wrap: `infiniteRepeatable(animation = tween(...), repeatMode = RepeatMode.*)`.

6. **`by` delegate on `InfiniteTransition.animateFloat` needs `import androidx.compose.runtime.getValue`** — The star import `animation.core.*` does not cover this.

7. **`graphicsLayer` as Modifier extension** — Needs `import androidx.compose.ui.graphics.graphicsLayer` explicitly; not covered by other ui.* imports.

8. **`CycleProgressBar` / `RemainingDaysBar` live in `com.mealcycle.app.ui.home`** — Must be explicitly imported in files outside that package; wildcard `components.*` does not cover them.
