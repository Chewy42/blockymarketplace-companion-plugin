package com.blockymarketplace.command;

import com.blockymarketplace.service.LinkCodeService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnlinkCommand extends AbstractCommand {
    private final LinkCodeService linkCodeService;

    public UnlinkCommand(LinkCodeService linkCodeService) {
        super("unlink", "Unlink your game account from the marketplace website");
        this.linkCodeService = linkCodeService;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        if (linkCodeService == null) {
            context.sendMessage(Message.raw("Marketplace is not configured. Please contact the server administrator."));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> ref = context.senderAsPlayerRef();
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            UUID playerUuid = playerRef.getUuid();

            CompletableFuture.runAsync(() -> {
                try {
                    var account = linkCodeService.getLinkedAccount(playerUuid).join();

                    if (account == null) {
                        world.execute(() -> {
                            context.sendMessage(Message.raw("Your account is not linked."));
                        });
                        return;
                    }

                    var success = linkCodeService.unlinkAccount(playerUuid).join();

                    world.execute(() -> {
                        if (success) {
                            context.sendMessage(Message.raw("Your account has been unlinked from the marketplace."));
                            context.sendMessage(Message.raw("Use /link to link again."));
                        } else {
                            context.sendMessage(Message.raw("Failed to unlink account. Please try again."));
                        }
                    });
                } catch (Exception e) {
                    world.execute(() -> {
                        context.sendMessage(Message.raw("An error occurred. Please try again."));
                    });
                }
            });
        });

        return CompletableFuture.completedFuture(null);
    }
}
