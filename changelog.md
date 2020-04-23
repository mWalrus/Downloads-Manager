# Changelog
## [1.5] - 2020-04-23
### Added
- Handling for resources being downloaded when a website's source is downloaded
### Changed
- Category list under "Open" menu is updated every time a new category is created

## [1.4.3] - 2020-04-22
### Added
- Version tag in System Tray menu

## [1.4.2] - 2020-04-22
### Changed
- Improved speed for detecting when download is finished (3s to 1s, had unnecessarily long pause in file size comparison)
- Disposing of JFrame moved to method inside UI class
### Removed
- Unused dependencies

## [1.4] - 2020-04-20
### Added
- New download handling system that is reliable across browsers
- New and improved UI using JFrames
- New UI color scheme
### Removed
- Removed old UI using JOptionPanes

## [1.3.1] - 2020-04-14
### Added
- "Open" submenu with options to open specific folders
- Tray icon tooltip
- JavaDoc comments
### Removed
- Duplicate method for opening files
