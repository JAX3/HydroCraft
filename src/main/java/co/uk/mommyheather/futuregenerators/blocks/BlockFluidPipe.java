package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.tile.TileFluidPipe;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import co.uk.mommyheather.futuregenerators.util.BlockSegment;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class BlockFluidPipe extends PipeBlock implements EntityBlock {
   public static final BooleanProperty PUMP_NORTH = BooleanProperty.create("pump_north");
   public static final BooleanProperty PUMP_EAST = BooleanProperty.create("pump_east");
   public static final BooleanProperty PUMP_SOUTH = BooleanProperty.create("pump_south");
   public static final BooleanProperty PUMP_WEST = BooleanProperty.create("pump_west");
   public static final BooleanProperty PUMP_UP = BooleanProperty.create("pump_up");
   public static final BooleanProperty PUMP_DOWN = BooleanProperty.create("pump_down");
   public static final Map<Direction, BooleanProperty> PUMP_BY_DIRECTION = ImmutableMap.copyOf(
      (Map)Util.make(Maps.newEnumMap(Direction.class), p_55164_ -> {
         p_55164_.put(Direction.NORTH, PUMP_NORTH);
         p_55164_.put(Direction.EAST, PUMP_EAST);
         p_55164_.put(Direction.SOUTH, PUMP_SOUTH);
         p_55164_.put(Direction.WEST, PUMP_WEST);
         p_55164_.put(Direction.UP, PUMP_UP);
         p_55164_.put(Direction.DOWN, PUMP_DOWN);
      })
   );
   public static final BooleanProperty HIDE_STOPPERS = BooleanProperty.create("hide_stoppers");

   public BlockFluidPipe(Properties p_55160_) {
      super(0.15F, p_55160_);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition
                                                   .any())
                                                .setValue(NORTH, false))
                                             .setValue(EAST, false))
                                          .setValue(SOUTH, false))
                                       .setValue(WEST, false))
                                    .setValue(UP, false))
                                 .setValue(DOWN, false))
                              .setValue(HIDE_STOPPERS, false))
                           .setValue(PUMP_NORTH, false))
                        .setValue(PUMP_EAST, false))
                     .setValue(PUMP_SOUTH, false))
                  .setValue(PUMP_WEST, false))
               .setValue(PUMP_UP, false))
            .setValue(PUMP_DOWN, false)
      );
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState other, LevelAccessor accessor, BlockPos pos, BlockPos otherPos) {
      boolean flag = other.is(this) || this.isConnectable(accessor, other, pos, direction);
      state = (BlockState)state.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), flag);
      return (BlockState)state.setValue(HIDE_STOPPERS, this.hideStoppers(state));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(
         new Property[]{
            NORTH, EAST, SOUTH, WEST, UP, DOWN, PUMP_NORTH, PUMP_EAST, PUMP_SOUTH, PUMP_WEST, PUMP_UP, PUMP_DOWN, HIDE_STOPPERS
         }
      );
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
   }

   public BlockState getStateForPlacement(BlockGetter getter, BlockPos pos) {
      BlockState blockstate = getter.getBlockState(pos.below());
      BlockState blockstate1 = getter.getBlockState(pos.above());
      BlockState blockstate2 = getter.getBlockState(pos.north());
      BlockState blockstate3 = getter.getBlockState(pos.east());
      BlockState blockstate4 = getter.getBlockState(pos.south());
      BlockState blockstate5 = getter.getBlockState(pos.west());
      BlockState state = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState()
                        .setValue(DOWN, blockstate.is(this) || this.isConnectable(getter, blockstate, pos, Direction.DOWN)))
                     .setValue(UP, blockstate1.is(this) || this.isConnectable(getter, blockstate1, pos, Direction.UP)))
                  .setValue(NORTH, blockstate2.is(this) || this.isConnectable(getter, blockstate2, pos, Direction.NORTH)))
               .setValue(EAST, blockstate3.is(this) || this.isConnectable(getter, blockstate3, pos, Direction.EAST)))
            .setValue(SOUTH, blockstate4.is(this) || this.isConnectable(getter, blockstate4, pos, Direction.SOUTH)))
         .setValue(WEST, blockstate5.is(this) || this.isConnectable(getter, blockstate5, pos, Direction.WEST));
      return (BlockState)state.setValue(HIDE_STOPPERS, this.hideStoppers(state));
   }

   public boolean isConnectable(BlockGetter getter, BlockState state, BlockPos pos, Direction direction) {
      BlockEntity be = getter.getBlockEntity(pos.offset(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
      return be != null ? be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent() : false;
   }

   public boolean hideStoppers(BlockState state) {
      for (Direction direction : new Direction[]{Direction.NORTH, Direction.EAST, Direction.UP}) {
         if ((Boolean)state.getValue((Property)PROPERTY_BY_DIRECTION.get(direction)) && (Boolean)state.getValue((Property)PROPERTY_BY_DIRECTION.get(direction.getOpposite()))) {
            return true;
         }
      }

      return false;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileFluidPipe(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.fluidPipe.get() ? TileFluidPipe::tick : null;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, final Player player, InteractionHand hand, BlockHitResult trace) {
      if (!level.isClientSide && player.getItemInHand(hand).isEmpty() && hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof TileFluidPipe pipe) {
         HashMap<AABB, BlockSegment> segmentsMap = new HashMap<>();

         for (BlockSegment segment : BlockSegment.values()) {
            if (segment != BlockSegment.CENTRE) {
               BlockEntity be2 = level.getBlockEntity(pos.offset(segment.getDirection().getNormal()));
               if (be2 == null || be2 instanceof TileFluidPipe) {
                  continue;
               }
            }

            segmentsMap.put(
               new AABB(pos).deflate(0.34, 0.34, 0.34).move(segment.xWithMult(0.34), segment.yWithMult(0.34), segment.zWithMult(0.34)), segment
            );
         }

         ArrayList<AABB> bbs = new ArrayList<>(segmentsMap.keySet());
         bbs.sort(new Comparator<AABB>() {
            public int compare(AABB o1, AABB o2) {
               return (int)((o1.distanceToSqr(player.position()) - o2.distanceToSqr(player.position())) * 100.0);
            }
         });

         for (AABB aabb : bbs) {
            if (aabb.contains(trace.getLocation())) {
               pipe.handleInteraction(segmentsMap.get(aabb), player.isCrouching(), player);
               return InteractionResult.SUCCESS;
            }
         }
      }

      return InteractionResult.PASS;
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean p_51542_) {
      if (!state.is(oldState.getBlock())) {
         TileFluidPipe pipe = (TileFluidPipe)level.getBlockEntity(pos);
         pipe.invalidateNetwork();
      }

      super.onRemove(state, level, pos, oldState, p_51542_);
   }
}
