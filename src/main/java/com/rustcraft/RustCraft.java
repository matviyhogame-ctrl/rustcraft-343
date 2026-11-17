package com.rustcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class RustCraft implements ModInitializer {
    public static final String MOD_ID = "rustcraft";

    @Override
    public void onInitialize() {
        ModItems.init();
        ModBlocks.init();
        ModWorldgen.init();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ThirstManager.tick(server);
            Airdrop.tick(server);
            BuildingPlan.tick(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
            dispatcher.register(CommandManager.literal("rust")
                .then(CommandManager.literal("airdrop")
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                        Airdrop.drop(p.getServerWorld(), p.getBlockPos());
                        return 1;
                    }))
                .then(CommandManager.literal("plan")
                    .executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                        p.giveItemStack(new net.minecraft.item.ItemStack(ModItems.BUILDING_PLAN));
                        return 1;
                    }))
                .then(CommandManager.literal("thirst")
                    .executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                        ctx.getSource().sendFeedback(new TranslatableText("hud.rustcraft.thirst", (int) ThirstManager.get(p)), false);
                        return 1;
                    }))
            ));
    }
}
