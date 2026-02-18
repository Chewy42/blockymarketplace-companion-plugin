package com.blockymarketplace.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.concurrent.CompletableFuture;

public class ItemIdCommand extends AbstractCommand {
    public ItemIdCommand() {
        super("itemid", "Show the held item id for product setup");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> ref = context.senderAsPlayerRef();
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                context.sendMessage(Message.raw("Unable to access player inventory."));
                return;
            }

            Inventory inventory = player.getInventory();
            ItemStack itemStack = inventory.getItemInHand();
            if (itemStack == null || itemStack.isEmpty()) {
                context.sendMessage(Message.raw("Hold an item in your hand to get its item id."));
                return;
            }

            String itemId = itemStack.getItemId();
            int quantity = itemStack.getQuantity();

            context.sendMessage(Message.raw("Held item id: " + itemId));
            context.sendMessage(Message.raw("Delivery config: {\"itemId\":\"" + itemId + "\",\"quantity\":" + quantity + "}"));
        });

        return CompletableFuture.completedFuture(null);
    }
}
