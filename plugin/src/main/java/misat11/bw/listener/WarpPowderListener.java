package misat11.bw.listener;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import misat11.bw.Main;
import misat11.bw.api.APIUtils;
import misat11.bw.api.Game;
import misat11.bw.api.GameStatus;
import misat11.bw.api.events.BedwarsApplyPropertyToBoughtItem;
import misat11.bw.game.GamePlayer;
import misat11.bw.special.WarpPowder;

import static misat11.bw.utils.I18n.i18n;

public class WarpPowderListener implements Listener {
	
	public static final String WARP_POWDER_PREFIX = "Module:WarpPowder:";

	@EventHandler
	public void onPowderItemRegister(BedwarsApplyPropertyToBoughtItem event) {
		if (event.getPropertyName().equalsIgnoreCase("warppowder")) {
			ItemStack stack = event.getStack();

			String warpPowderString = WARP_POWDER_PREFIX + event.getIntProperty("delay");

			APIUtils.hashIntoInvisibleString(stack, warpPowderString);
		}
	}

	@EventHandler
	public void onPlayerUseItem(PlayerInteractEvent event) {
		if (event.isCancelled() && event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		if (!Main.isPlayerInGame(event.getPlayer())) {
			return;
		}

		GamePlayer gPlayer = Main.getPlayerGameProfile(event.getPlayer());
		Game game = gPlayer.getGame();
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator) {
				if (event.getItem() != null) {
					ItemStack stack = event.getItem();
					String unhidden = APIUtils.unhashFromInvisibleStringStartsWith(stack, WARP_POWDER_PREFIX);
					if (unhidden != null) {
						event.setCancelled(true);
						int delay = Integer.parseInt(unhidden.split(":")[2]);
						WarpPowder powder = new WarpPowder(game, event.getPlayer(),
								game.getTeamOfPlayer(event.getPlayer()), stack, delay);

						WarpPowder originalPowder = (WarpPowder) game
								.getFirstActivedSpecialItemOfPlayer(event.getPlayer(), WarpPowder.class);

						if (originalPowder != null) {
							if (originalPowder.getStack().equals(powder.getStack())) {
								originalPowder.cancelTeleport(true, true, false);
							} else {
								event.getPlayer().sendMessage(i18n("specials_warp_powder_multiuse"));
							}
							return;
						}

						if (event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN)
								.getType() == Material.AIR) {
							return;
						}

						powder.runTask();
					}
				}
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();

		if (!Main.isPlayerInGame(player)) {
			return;
		}

		GamePlayer gPlayer = Main.getPlayerGameProfile(player);
		Game game = gPlayer.getGame();

		if (gPlayer.isSpectator) {
			return;
		}

		WarpPowder powder = (WarpPowder) game.getFirstActivedSpecialItemOfPlayer(player, WarpPowder.class);
		if (powder != null) {
			powder.cancelTeleport(true, true, false);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (!Main.isPlayerInGame(event.getPlayer())) {
			return;
		}

		GamePlayer gPlayer = Main.getPlayerGameProfile(event.getPlayer());
		Game game = gPlayer.getGame();

		if (gPlayer.isSpectator) {
			return;
		}

		WarpPowder powder = (WarpPowder) game.getFirstActivedSpecialItemOfPlayer(event.getPlayer(), WarpPowder.class);
		if (powder != null) {
			if (powder.getStack().equals(event.getItemDrop().getItemStack())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (!Main.isPlayerInGame(event.getPlayer())) {
			return;
		}

		GamePlayer gPlayer = Main.getPlayerGameProfile(event.getPlayer());
		Game game = gPlayer.getGame();

		if (gPlayer.isSpectator) {
			return;
		}

		WarpPowder powder = (WarpPowder) game.getFirstActivedSpecialItemOfPlayer(event.getPlayer(), WarpPowder.class);
		if (powder != null) {
			powder.cancelTeleport(true, true, false);
		}
	}
}
