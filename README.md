# BlockyMarketplace Companion Plugin

Official Java companion plugin for BlockyMarketplace.

This plugin links your Hytale server to BlockyMarketplace so players can purchase items, ranks, and perks through your store and receive deliveries in-game.

## Features

- Account linking (`/link`, `/linkstatus`, `/unlink`)
- In-game shop and purchase flows (`/shop`, `/buy`, `/purchases`)
- Delivery automation for items, ranks, commands, and currency
- Polar webhook support for payment events
- Merchant utility commands for linking products to in-game rewards

## Requirements

- Java 21
- Hytale server API jar (`HytaleServer.jar`)

## Setup

1. Place your Hytale server API jar at:
   - `libs/HytaleServer.jar`
2. Build the plugin:

```bash
./gradlew clean shadowJar
```

3. Copy the built jar from:
   - `build/libs/BlockyMarketplace-1.0-SNAPSHOT.jar`
4. Put it in your server's `mods/` directory.
5. Start once, then configure `plugins/BlockyMarketplace/config.json`.

## Configuration

Required values:

- `server.apiKey`
- `convex.url`

Optional values:

- `server.id`
- `convex.deploymentToken`
- `webhook.enabled`
- `webhook.port`
- `webhook.polarSecret`
- `webapp.url`

## Core Commands

- `/link`
- `/linkstatus`
- `/unlink`
- `/shop`
- `/buy <id>`
- `/purchases`
- `/redeem`
- `/redeemplayer <player>`

## Release Downloads

Use GitHub Releases for ready-to-use `.jar` files.

