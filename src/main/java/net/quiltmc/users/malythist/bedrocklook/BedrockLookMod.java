package net.quiltmc.users.malythist.bedrocklook;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BedrockLookMod implements ModInitializer {

	public static final String MOD_ID = "bedrocklook";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static boolean enabled = false;

	@Override
	public void onInitialize(ModContainer mod) {
		registerCommands();
		registerTick();
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register(this::register);
	}

	private void register(
		CommandDispatcher<ServerCommandSource> dispatcher,
		CommandBuildContext context,
		CommandManager.RegistrationEnvironment env
	) {
		dispatcher.register(
			CommandManager.literal("bedrocklook")
				.then(CommandManager.literal("start")
					.executes(ctx -> {
						enabled = true;
						ctx.getSource().sendFeedback(
							() -> Text.literal("BedrockLook включен"),
							false
						);
						return 1;
					})
				)
				.then(CommandManager.literal("stop")
					.executes(ctx -> {
						enabled = false;
						ctx.getSource().sendFeedback(
							() -> Text.literal("BedrockLook выключен"),
							false
						);
						return 1;
					})
				)
		);
	}

	private void registerTick() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!enabled) return;

			for (ServerWorld world : server.getWorlds()) {
				for (ServerPlayerEntity player : world.getPlayers()) {
					processPlayerLook(player, world);
				}
			}
		});
	}

	private void processPlayerLook(ServerPlayerEntity player, ServerWorld world) {
		BlockHitResult hit = raycast(player, world);
		if (hit.getType() != HitResult.Type.BLOCK) return;

		BlockPos pos = hit.getBlockPos();

		if (world.getBlockState(pos).isOf(Blocks.END_PORTAL_FRAME)) return;
		if (world.getBlockState(pos).isOf(Blocks.BEDROCK)) return;

		world.setBlockState(pos, Blocks.BEDROCK.getDefaultState());
	}

	private BlockHitResult raycast(ServerPlayerEntity player, ServerWorld world) {
		return world.raycast(
			new RaycastContext(
				player.getEyePos(),
				player.getEyePos()
					.add(player.getRotationVec(1.0F).multiply(5.0D)),
				RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE,
				player
			)
		);
	}
}
