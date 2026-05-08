# OpenStream Documentation

## Architecture
```
:app          -> Main application (Compose, ViewModels, Navigation)
:library      -> Core streaming logic (StreamLibrary)
:extensions   -> Plugin/extension system (ExtensionManager)
```

## Module Dependency Graph
```
:app ---> :library
:app ---> :extensions ---> :library
```

## Theme System
| ThemeMode | Background | Surface  |
|-----------|-----------|---------|
| DARK      | #0A0A0F   | #12121A |
| AMOLED    | #000000   | #080808 |
| LIGHT     | #F2F2F8   | #FFFFFF |
| SYSTEM    | system    | system  |

Persisted via DataStore â€” key: `app_theme`

## Glass Components
- `GlassBackground` â€” full-screen glow backdrop
- `GlassCard`       â€” frosted card component
- `GlassButton`     â€” translucent action button
- `GlassTopBar`     â€” frosted app bar
