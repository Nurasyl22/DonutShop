# DonutShop Plugin

A simple but powerful GUI shop plugin for Spigot/Paper servers, fully managed via a configuration file and in-game commands. Inspired by the DonutSMP one

## Features

- **GUI-Based:** An easy-to-use shop interface.
- **Categories:** Organize items into customizable categories.
- **Admin Panel:** A graphical admin panel (`/adminshop`) for easily adding and removing shop items.
- **Amount Selection:** Players can choose exactly how many items they want to buy (for stackable items).
- **Reload Command:** Reload the configuration without a server restart.
- **Vault Integration:** Uses Vault for the economy.

---

## Showcase

### Main Menu (Category Overview)
![Main Menu](https://i.imgur.com/TTJbzCb.png)

### Category View
![Category View](https://i.imgur.com/gB2Rfii.png)

### Purchase View
![Purchase View](https://i.imgur.com/N8wYF6C.png)

---

## Commands & Permissions

| Command | Description | Permission |
| --- | --- | --- |
| `/shop` | Opens the main shop menu. | `donutshop.use` |
| `/shop reload` | Reloads the `config.yml`. | `OP` |
| `/adminshop` | Opens the admin panel for shop management. | `OP` |

---

## Installation

1. Download the `DonutShop-X.X.jar` into your `plugins` folder. (Found in the Target Folder)
2. Ensure that Vault and an economy plugin (e.g., EssentialsX) are installed.
3. Start the server. The `config.yml` will be created in the `plugins/DonutShop` folder.

---

## Configuration

The `config.yml` is divided into two main sections:

### `main-menu`
Here you define the categories that are displayed in the main shop window.

```yaml
main-menu:
  title: "Shop"
  size: 27
  items:
    '11':
      material: END_STONE
      name: 'End'
      lore: 'Click to view End Shop'
      category: end # This key links to the category below
```
