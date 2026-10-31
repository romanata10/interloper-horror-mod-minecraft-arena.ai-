package net.anomidae.interloper.block;

import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.world.WhiteDoor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerPlayer;

/**
 * The white door. It has a lock, the lock wants the key, and behind it is the game
 * you were playing before all of this.
 */
public class WhiteDoorBlock extends Block {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private final boolean lock;

    public WhiteDoorBlock(Properties properties, boolean lock) {
        super(properties);
        this.lock = lock;
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, Boolean.FALSE));
    }

    public boolean isLock() {
        return this.lock;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (state.getValue(OPEN)) {
            serverPlayer.sendSystemMessage(Component.literal("the door is already open")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.WHITE_DOOR_KEY.get())) {
            serverPlayer.sendSystemMessage(Component.literal(this.lock
                            ? "the lock does not move. it wants a key"
                            : "the door is locked. white door, small key")
                    .withStyle(ChatFormatting.GRAY));
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.8F, 0.7F);
            return InteractionResult.CONSUME;
        }

        held.shrink(1);
        level.setBlock(pos, state.setValue(OPEN, true), 3);
        level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.9F);
        level.playSound(null, pos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 0.8F, 1.4F);
        WhiteDoor.open(serverPlayer);
        return InteractionResult.CONSUME;
    }
}
