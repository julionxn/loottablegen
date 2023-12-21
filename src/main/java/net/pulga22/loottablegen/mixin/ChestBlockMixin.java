package net.pulga22.loottablegen.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pulga22.loottablegen.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin extends AbstractChestBlock<ChestBlockEntity> {

	@Unique
	private static final BooleanProperty USED = BooleanProperty.of("used");

	protected ChestBlockMixin(Settings settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityTypeSupplier) {
		super(settings, blockEntityTypeSupplier);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(Settings settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, CallbackInfo ci) {
		setDefaultState(getStateManager().getDefaultState().with(USED, false));
	}

	@Inject(method = "appendProperties", at = @At("TAIL"))
	public void appendProps(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci){
		builder.add(USED);
	}

	@Inject(method = "onPlaced", at = @At("TAIL"))
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci){
		if (placer instanceof PlayerEntity player && !player.isCreative()){
			world.setBlockState(pos, state.with(USED, true), NOTIFY_NEIGHBORS);
		}
	}

	@Inject(method = "onUse", at = @At("TAIL"))
	public void injectInventory(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir){
		if (world.isClient) return;
		//Check if this chest is candidate to apply a loot table.
		Inventory inventory = ChestBlock.getInventory((ChestBlock)(Object) this, state, world, pos, true);
		if (state.get(USED) || inventory == null || !inventory.isEmpty()) return;
		//Try to apply a random loot table.
		Config.getInstance().getRandomLootTableFromPos(world, pos).ifPresent(lootTableId -> {
			LootableContainerBlockEntity.setLootTable(world, world.random, pos, lootTableId);
			world.setBlockState(pos, state.with(USED, true), NOTIFY_NEIGHBORS);
		});
	}

}