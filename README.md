<div align="center">

![Lucid Advancements](https://cdn.modrinth.com/data/cached_images/240930d0420c6d769f1d90835582819310b4d6bb.png)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21%2B-blue?style=for-the-badge)
![Side](https://img.shields.io/badge/Side-Client-green?style=for-the-badge)

![NeoForge](https://img.shields.io/badge/NeoForge-red?style=for-the-badge)
![Forge](https://img.shields.io/badge/Forge-orange?style=for-the-badge)
![Fabric](https://img.shields.io/badge/Fabric-gray?style=for-the-badge)

**Browse, track, and complete your goals without the visual chaos.**

</div>

***

**Lucid Advancements** is a complete, client-side overhaul of Minecraft's default Advancements screen. Designed with large modpacks in mind, it replaces the traditional, cluttered advancement tree with a clean, modern, and highly responsive list-based interface. 

Now completely rebuilt as a multiloader mod, it natively supports **Fabric, Forge, and NeoForge**. Browse, track, and complete your goals without the visual chaos.

***

## VISUAL OVERHAUL & ORGANIZATION

The vanilla advancement tree can become entirely unreadable when playing with multiple mods. Lucid Advancements solves this by introducing a standardized, accessible layout:

*   **Global Category View:** Want to see everything at once? Use the Global View to browse advancements from every category in one unified, easily scrollable list.
*   **Categorical Sidebar:** Quickly navigate between different advancement tabs (Minecraft, Nether, Adventure, etc.) using the sleek left-hand sidebar. Categories are intelligently sorted using **vanilla priority**, keeping your layout familiar and logical.
*   **Card-Based Layout:** Advancements are displayed as clean, uniform cards with **interactive hover highlights**, massively improving readability for long descriptions.
*   **Expandable Requirements:** No more guessing what you are missing. Clicking on an advancement card expands it to reveal a detailed, checked list of its specific requirements.

## ![Expandable Requirements](https://cdn.modrinth.com/data/cached_images/934b83ce11c53cee25542c6ccc6d6aed020b6f5d.png)

## ADVANCED SEARCH & FILTERING

Finding a specific advancement among thousands is now instant. The top navigation bar includes powerful tools to narrow down your search:

*   **Global Search Bar:** Search for advancements by Title, Description, or Category. **You can also combine `@modid` filters with free text** for pinpoint accuracy! > **Examples:** `@minecraft`, `@farmersdelight diamond`, `@create`
*   **Smart Dropdown Filtering:** Use the top Filter dropdown to easily switch between displaying _All_, _Partial_, _Completed_, _Uncompleted_, or _Challenges_ advancements.
*   **Dynamic Progress Updates:** When searching or filtering within a section, the progress bar updates in real-time to show your completion rate *only* for the matching advancements!

***

## ON-SCREEN PROGRESS TRACKING

Keep your current goals in sight at all times. Lucid Advancements automatically lists every incomplete advancement on a clean overlay, so you can monitor your progress without constantly reopening the menu.

*   **Automatic Display:** Every incomplete, visible advancement is shown by default — no manual setup required.
*   **Paged Overlay:** Up to four advancements are shown per page in the advancement tree's iteration order, reduced automatically to fit the overlay height; press `Ctrl` plus your configured advancements key (default `Ctrl + L`) to cycle pages, with the current page displayed as `x/y`.
*   **Overlay Toggle:** Press `Alt` plus your configured advancements key (default `Alt + L`) to hide or show the overlay.
*   **Quick Access:** Pressing the configured advancements key alone still opens or closes the advancements screen.

<div align="center">
  
![Overlay Tracking](https://cdn.modrinth.com/data/cached_images/f5d4915d3e47b6f9b7032437ce26780dbd777a64.png)

</div>

***

## UNIFIED NOTIFICATIONS & EFFECTS

Advancement completion and recipe unlocks now share the overlay style in centered top notifications, while active effects are displayed on the left in the same visual language.

*   **Unified Popups:** Advancement and recipe notifications appear at the top center with the same background, accent, icon, and text styling as the overlay. Each notification keeps its own timer and disappears independently; recipe notifications stay visible for one second after their icons finish cycling. Notifications render above container screens; other screens, including pause/ESC, draw over them.
*   **Effect Display:** Active effects are displayed in a left-side grid with matching cards, names including amplifier levels, and optional multi-column overflow. Column count depends on screen width and visible overlay elements; extra effects show a hidden-count hint. The effect display is drawn underneath open screens, like the right-side achievement overlay. Disabling the custom effect display restores vanilla effect rendering.
*   **Compatibility Note:** NeoForge-specific custom effect extensions are not adapted yet; effect visibility follows vanilla `showIcon()` rules.

***

## QUALITY OF LIFE, CONFIG & CUSTOMIZATION

*   **Hot-Reloadable Configuration:** Everything can be customized to your liking via the new `lucidadvancements.properties` config file. Best of all, it supports an **hot-reload system**—changes apply instantly without needing to restart the game!
*   **Performance Optimized:** Enjoy buttery-smooth UI navigation. The GUI rendering has been highly optimized with reduced GC allocations.
*   **Requirement Localization:** Native translations are actively being implemented for advancement requirements! Currently supports English (`en_us`), Brazilian Portuguese (`pt_br`), and Italian (`it_it` - WIP), making complex goals much easier to understand.
*   **Dynamic GUI Scale:** A dedicated dropdown allows you to adjust the interface scale on the fly to perfectly fit your monitor setup. Supports: `Auto`, `1` - `8`.
*   **Contextual Progress Bars:** Keep track of your overarching goals with a Global Progress bar at the bottom left, alongside specific **per-category progress bars** inside individual tabs.
*   **Intuitive Tooltips:** Hover over any advancement icon to view standard item tooltips (now with perfectly scaled mouse coordinates!).

<div align="center">
  
![Global Progress Bar](https://cdn.modrinth.com/data/cached_images/863752223aa5c85d005c605a76f75d77bcc33ca6.png)

</div>

***

**Lucid Advancements** is entirely **Client-Side**. It alters only the graphical interface and progress display, meaning it is perfectly safe to drop into any existing modpack or server. It reads standard vanilla and modded advancement data seamlessly without altering core mechanics.

## Special Thanks

A huge thank you to the community members who have helped shape and improve Lucid Advancements:

*   **[@VaporeonScripts](https://github.com/VaporeonScripts)** - For all the amazing suggestions, bug reports and support!

## Bug Reports & Feedback

If you encounter a bug, have a feature request, or want to contribute to the ongoing localization and cross-mod translation efforts, please open an issue in our **[Issue Tracker](https://github.com/Niix-Dan/Lucid-Advancements/issues)** using the provided templates.

<br>
<div align="center">

[![Discord Community](https://discordapp.com/api/guilds/1519007858778177749/widget.png?style=banner2)](https://discord.gg/vChttNgJ4v)

</div>