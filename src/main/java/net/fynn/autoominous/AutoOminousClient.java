package net.fynn.autoominous;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

public class AutoOminousClient implements ClientModInitializer {
	public static final String MOD_ID = "autoominous";

	private static final int DRINK_TIMEOUT_TICKS = 80;
	private static final int COOLDOWN_TICKS = 20;

	private enum State { IDLE, DRINKING }

	private static KeyMapping toggleKey;

	private boolean enabled = false;
	private State state = State.IDLE;
	private int cooldown = 0;
	private int drinkTicks = 0;
	private int previousSlot = -1;
	private boolean warnedNoBottle = false;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(MOD_ID, "main")
		);

		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.autoominous.toggle",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_K,
			category
		));

		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(Minecraft mc) {
		LocalPlayer player = mc.player;

		while (toggleKey.consumeClick()) {
			enabled = !enabled;
			warnedNoBottle = false;
			if (!enabled) {
				finishDrinking(mc, player);
			}
			if (player != null) {
				player.sendSystemMessage(Component.translatable(
					enabled ? "message.autoominous.on" : "message.autoominous.off"));
			}
		}

		if (player == null || mc.gameMode == null) {
			state = State.IDLE;
			return;
		}
		if (!enabled) {
			return;
		}
		if (mc.currentScreen != null) {
			// Kein Trinken, solange ein Menue offen ist
			if (state == State.DRINKING) {
				finishDrinking(mc, player);
			}
			return;
		}

		switch (state) {
			case DRINKING -> tickDrinking(mc, player);
			case IDLE -> tickIdle(mc, player);
		}
	}

	private void tickIdle(Minecraft mc, LocalPlayer player) {
		if (cooldown > 0) {
			cooldown--;
			return;
		}

		// Nur trinken, wenn KEIN Ominous-Effekt aktiv ist
		if (player.hasEffect(MobEffects.BAD_OMEN) || player.hasEffect(MobEffects.RAID_OMEN)) {
			return;
		}
		if (player.isUsingItem()) {
			return;
		}

		int slot = -1;
		for (int i = 0; i < 9; i++) {
			if (player.getInventory().getItem(i).is(Items.OMINOUS_BOTTLE)) {
				slot = i;
				break;
			}
		}
		if (slot == -1) {
			if (!warnedNoBottle) {
				warnedNoBottle = true;
				player.sendSystemMessage(Component.translatable("message.autoominous.nobottle"));
			}
			return;
		}
		warnedNoBottle = false;

		previousSlot = player.getInventory().getSelectedSlot();
		player.getInventory().setSelectedSlot(slot);

		InteractionResult result = mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
		if (result.consumesAction()) {
			// Taste "gedrueckt halten", sonst bricht Minecraft das Trinken sofort ab
			mc.options.keyUse.setDown(true);
			state = State.DRINKING;
			drinkTicks = 0;
		} else {
			restoreSlot(player);
			cooldown = COOLDOWN_TICKS;
		}
	}

	private void tickDrinking(Minecraft mc, LocalPlayer player) {
		drinkTicks++;
		boolean done = !player.isUsingItem()
			|| player.hasEffect(MobEffects.BAD_OMEN)
			|| player.hasEffect(MobEffects.RAID_OMEN)
			|| drinkTicks > DRINK_TIMEOUT_TICKS;

		if (done) {
			finishDrinking(mc, player);
		} else {
			mc.options.keyUse.setDown(true);
		}
	}

	private void finishDrinking(Minecraft mc, LocalPlayer player) {
		if (state == State.DRINKING) {
			mc.options.keyUse.setDown(false);
			restoreSlot(player);
			cooldown = COOLDOWN_TICKS;
		}
		state = State.IDLE;
	}

	private void restoreSlot(LocalPlayer player) {
		if (player != null && previousSlot >= 0 && previousSlot < 9) {
			player.getInventory().setSelectedSlot(previousSlot);
		}
		previousSlot = -1;
	}
}
