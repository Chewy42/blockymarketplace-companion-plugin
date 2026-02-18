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

public class LinkCommand extends AbstractCommand {
    private final LinkCodeService linkCodeService;
    private final String webappUrl;

    public LinkCommand(LinkCodeService linkCodeService, String webappUrl) {
        super("link", "Link your game account to the marketplace website");
        this.linkCodeService = linkCodeService;
        this.webappUrl = webappUrl != null ? webappUrl : "https://marketplace.blockynetwork.com";
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
            String playerName = playerRef.getUsername();

            CompletableFuture.runAsync(() -> {
                try {
                    var account = linkCodeService.getLinkedAccount(playerUuid).join();

                    if (account != null) {
                        world.execute(() -> {
                            context.sendMessage(Message.raw("=== Account Already Linked ==="));
                            context.sendMessage(Message.raw("Your account is already linked to the marketplace."));
                            context.sendMessage(Message.raw("Use /linkstatus to see details."));
                            context.sendMessage(Message.raw("Use /unlink to disconnect your account."));
                        });
                        return;
                    }

                    var linkCode = linkCodeService.createLinkCode(playerUuid, playerName).join();

                    world.execute(() -> {
                        if (linkCode == null) {
                            context.sendMessage(Message.raw("Failed to generate link code. Please try again."));
                            return;
                        }

                        context.sendMessage(Message.raw("=== Account Linking ==="));
                        context.sendMessage(Message.raw("Your link code: " + linkCode.getCode()));
                        context.sendMessage(Message.raw(""));
                        context.sendMessage(Message.raw("Visit: " + webappUrl + "/link"));
                        context.sendMessage(Message.raw("Enter the code above to link your account."));
                        context.sendMessage(Message.raw(""));
                        context.sendMessage(Message.raw("This code expires in 5 minutes."));
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
