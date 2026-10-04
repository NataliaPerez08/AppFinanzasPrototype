# PULSO — App Naming & Branding Update

## Objective

Update the application's public-facing identity to:

**PULSO**  
**Patrimonio e inversiones**

This task is primarily a naming and branding update.

Do **not** redesign the application, modify financial calculations, change existing flows, or introduce unrelated refactors.

---

## 1. Brand Identity

Use the following naming consistently throughout the application:

### Primary name

`PULSO`

The brand name should normally be displayed in uppercase when used as a visual wordmark or main application heading.

### Descriptor

`Patrimonio e inversiones`

Use this when additional context is useful, such as:

- Splash screen
- Onboarding
- About screen
- Store listing
- Documentation
- Empty states where appropriate

Do not repeat the descriptor unnecessarily throughout the UI.

### Optional tagline

`El pulso de tu patrimonio`

The tagline is optional and should only be used where it improves the experience.

Do not replace the primary descriptor with the tagline.

---

## 2. Product Positioning

PULSO is a personal finance application focused on providing a consolidated view of the user's wealth and investments across multiple financial institutions and asset types.

The application currently supports or is intended to support concepts such as:

- Stocks
- ETFs
- Mutual/investment funds
- FIBRAs
- CETES
- Fixed-income products
- SOFIPOs
- Cash
- Other manually registered investments
- Multiple financial institutions

The product should not be presented as a brokerage, bank, financial institution, or investment advisor.

PULSO tracks and analyzes financial information entered by the user.

---

## 3. Core Product Message

The product identity should communicate:

> One place to understand the current state, performance, composition, and projected evolution of your wealth.

The branding should emphasize:

- Patrimonio
- Investments
- Consolidation
- Performance
- Visibility
- Projection

Avoid messaging that implies guaranteed returns, investment recommendations, or financial advice.

---

## 4. Android Application Name

Update the Android application label so the installed application appears as:

`PULSO`

Review all relevant resources, including:

- `strings.xml`
- Manifest application labels
- Launcher labels
- Activity labels
- Compose strings
- Hardcoded legacy application names
- Accessibility descriptions where applicable

Prefer string resources instead of hardcoded text.

---

## 5. Existing Project Identifiers

Do **not** automatically rename technical identifiers simply because the public product name changed.

In particular, do not change without a concrete technical reason:

- `applicationId`
- Package names
- Kotlin package structure
- Room database names
- Migration identifiers
- Internal preference keys
- DataStore keys
- Existing persisted-data identifiers

Changing the brand name must not cause users to lose existing local data.

If a technical identifier contains the previous prototype name but changing it could affect persistence, compatibility, signing, migrations, or updates, leave it unchanged and document it.

---

## 6. UI Branding

Integrate PULSO into the existing design system.

Preserve the established visual direction:

- Neo-brutalist
- Financial
- Technological
- Editorial
- High information density
- Flat rectangular panels
- Precise grid alignment
- Minimal corner rounding
- No unnecessary shadows
- No decorative gradients

Existing palette direction:

- Warm light backgrounds: `#E4E3E1`, `#F4F3F1`
- Charcoal/black: `#111110`
- Dividers: `#C6C5C2`
- Primary accent: `#FF641C`

The orange accent should remain intentional and limited to:

- Active states
- Selected elements
- Important financial data
- Primary CTA
- Relevant highlights

Do not redesign existing screens merely to accommodate the new name.

---

## 7. Wordmark

For the initial version, prefer a typography-based wordmark:

`PULSO`

Avoid introducing an elaborate logo unless the project already has infrastructure for one.

The wordmark should feel:

- Technical
- Precise
- Modern
- Financial
- Compact
- Confident

Avoid stereotypical fintech imagery such as:

- Dollar signs
- Coins
- Piggy banks
- Candlestick charts
- Generic upward arrows
- Bank buildings

---

## 8. App Icon

If the project currently uses a placeholder launcher icon, prepare the project so it can later receive the official PULSO icon.

For now:

- Preserve Android adaptive icon compatibility.
- Preserve foreground/background separation.
- Do not introduce a low-resolution raster asset.
- Do not break existing launcher icon resources.

A future PULSO icon may explore concepts such as:

- Pulse
- Signal
- Financial movement
- A minimal `P`
- Data progression

Icon design itself is outside the scope of this task unless explicitly requested.

---

## 9. Splash Screen

Where the current Android architecture supports a splash screen, update the visible branding to:

`PULSO`

Optionally:

`Patrimonio e inversiones`

Keep the splash screen minimal.

Do not introduce artificial delays or animations that slow application startup.

---

## 10. Navigation and Headers

Review existing navigation surfaces and replace obsolete prototype/application names with PULSO where appropriate.

Examples:

- Main header
- Navigation drawer
- Settings
- About
- Splash
- Onboarding
- Empty-state introductory copy

Do not place `PULSO` in every screen header if the screen already has a meaningful title such as:

- Patrimonio
- Inversiones
- Proyección
- Instituciones
- Configuración

Screen hierarchy remains more important than repeating the brand.

---

## 11. Settings / About

If an About section exists, present the product as:

**PULSO**  
Patrimonio e inversiones

Include the application version using the existing build configuration rather than hardcoding it.

If no About screen currently exists, do not create a large new feature solely for this branding task.

---

## 12. Documentation

Update relevant project documentation to use the PULSO name.

Review:

- `README.md`
- Roadmap documents
- Architecture documentation
- Testing documentation
- Release documentation
- Screenshots or textual references
- Developer instructions

Where historical documentation describes an earlier prototype, preserve historical context when appropriate instead of blindly replacing every occurrence.

---

## 13. Store Preparation

Prepare user-facing naming so a future store listing can use:

### App name

**PULSO**

### Short positioning

**Patrimonio e inversiones**

### Longer positioning concept

PULSO centralizes your investments and helps you understand your current wealth, performance, asset allocation, and projected evolution from one place.

Do not publish anything to Google Play as part of this task.

---

## 14. Search for Legacy Branding

Perform a repository-wide search for:

- Previous application names
- Prototype names
- Placeholder branding
- Hardcoded app titles
- Old accessibility labels
- Old documentation references

Classify each occurrence before changing it.

### Replace

User-visible branding that refers to the old product name.

### Keep

Technical identifiers where changing them could affect:

- Persistence
- Database migrations
- Package compatibility
- Existing installations
- Signing
- Build configuration
- Tests that intentionally reference historical identifiers

### Review manually

Anything ambiguous.

Do not perform a blind global find-and-replace.

---

## 15. Regression Requirements

After the branding update, verify that the following existing flows still work:

- Dashboard Empty
- Create investment
- Initial deposit
- Dashboard with real data
- Register purchase
- Reactive Room/Flow updates
- Wealth calculation
- Asset distribution
- Projections and scenarios
- Persistent settings
- Financial institutions
- Add institution
- Main navigation
- Secondary navigation

Branding changes must not alter financial calculations or persistence behavior.

---

## 16. Build Validation

Before completing the task:

1. Build the application successfully.
2. Run existing automated tests.
3. Resolve regressions introduced by the branding changes.
4. Launch the application.
5. Verify the launcher name displays `PULSO`.
6. Verify splash/onboarding branding where applicable.
7. Verify no obvious legacy product name remains in user-facing UI.
8. Verify existing local data remains accessible.
9. Verify navigation and primary financial flows remain functional.

---

## 17. Scope Restrictions

Do not:

- Rewrite the architecture.
- Replace Room.
- Replace Compose components unnecessarily.
- Change financial formulas.
- Change database schemas without necessity.
- Change `applicationId` just for branding.
- Delete existing user data.
- Redesign completed screens.
- Add backend infrastructure.
- Add authentication.
- Add analytics.
- Add advertising.
- Add subscriptions.
- Add unrelated dependencies.

Those should be handled as separate tasks.

---

## 18. Definition of Done

This task is complete when:

- The public product name is **PULSO**.
- The descriptor is **Patrimonio e inversiones**.
- User-facing legacy branding has been removed.
- Technical identifiers that should remain stable have not been unnecessarily renamed.
- Existing user data remains compatible.
- The existing visual system is preserved.
- The project builds successfully.
- Existing tests pass.
- Core application flows continue working.
- Documentation reflects the PULSO identity.
- No unrelated feature or architecture changes were introduced.

---

## Expected Agent Output

When finished, provide:

1. Summary of branding changes.
2. List of modified files.
3. Any legacy technical identifiers intentionally preserved and why.
4. Build result.
5. Test result.
6. Any remaining occurrences of the previous name that were intentionally retained.
7. Any issues that should be addressed before the next release.

Do not report the task as complete if the project does not build successfully.
