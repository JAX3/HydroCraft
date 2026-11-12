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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jspecify.annotations.Nullable;

/**
 * Fluid pipe. Connects to other pipes and to anything that holds fluid. Right-click a connection with an empty hand to
 * switch that side between pushing and pulling; right-click the middle to raise the pipe's priority (sneak to lower it).
 */
public class BlockFluidPipe extends PipeBlock implements EntityBlock {
   public static final BooleanProperty PUMP_NORTH = BooleanProperty.create("pump_north");
   public static final BooleanProperty PUMP_EAST = BooleanProperty.create("pump_east");
   public static final BooleanProperty PUMP_SOUTH = BooleanProperty.create("pump_south");
   public static final BooleanProperty PUMP_WEST = BooleanProperty.create("pump_west");
   public static final BooleanProperty PUMP_UP = BooleanProperty.create("pump_up");
   public static final BooleanProperty PUMP_DOWN = BooleanProperty.create("pump_down");
   public static final Map<Direction, BooleanProperty> PUMP_BY_DIRECTION = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), map -> {
      map.put(Direction.NORTH, PUMP_NORTH);
      map.put(Direction.EAST, PUMP_EAST);
      map.put(Direction.SOUTH, PUMP_SOUTH);
      map.put(Direction.WEST, PUMP_WEST);
      map.put(Direction.UP, PUMP_UP);
      map.put(Direction.DOWN, PUMP_DOWN);
   }));
   public static final BooleanProperty HIDE_STOPPERS = BooleanProperty.create("hide_stoppers");

   public BlockFluidPipe(Properties properties) {
      super(0.15F, properties);
      BlockState state = this.stateDefinition.any().setValue(HIDE_STOPPERS, false);
      for (Direction direction : Direction.values()) {
         state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), false).setValue(PUMP_BY_DIRECTION.get(direction), false);
      }
      this.registerDefaultState(state);
   }

   @Override
   protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                    BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
      boolean flag = neighbourState.is(this) || this.isConnectable(level, pos, direction);
      state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), flag);
      return state.setValue(HIDE_STOPPERS, this.hideStoppers(state));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, PUMP_NORTH, PUMP_EAST, PUMP_SOUTH, PUMP_WEST, PUMP_UP, PUMP_DOWN, HIDE_STOPPERS);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
   }

   public BlockState getStateForPlacement(BlockGetter getter, BlockPos pos) {
      BlockState state = this.defaultBlockState();
      for (Direction direction : Direction.values()) {
         BlockState neighbour = getter.getBlockState(pos.relative(direction));
         state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), neighbour.is(this) || this.isConnectable(getter, pos, direction));
      }
      return state.setValue(HIDE_STOPPERS, this.hideStoppers(state));
   }

   /** Whether the block on {@code direction} holds fluid. */
   public boolean isConnectable(BlockGetter getter, BlockPos pos, Direction direction) {
      return getter instanceof Level level && level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(direction), direction.getOpposite()) != null;
   }

   /** Straight runs hide the end stoppers. */
   public boolean hideStoppers(BlockState state) {
      for (Direction direction : new Direction[]{Direction.NORTH, Direction.EAST, Direction.UP}) {
         if (state.getValue(PROPERTY_BY_DIRECTION.get(direction)) && state.getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileFluidPipe(pos, state);
   }

   @Override
   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.fluidPipe.get() ? TileFluidPipe::tick : null;
   }

   /** Works out which part of the pipe was clicked: the middle, or the connection on one side. */
   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult trace) {
      if (!level.isClientSide() && player.getMainHandItem().isEmpty() && level.getBlockEntity(pos) instanceof TileFluidPipe pipe) {
         HashMap<AABB, BlockSegment> segmentsMap = new HashMap<>();

         for (BlockSegment segment : BlockSegment.values()) {
            if (segment != BlockSegment.CENTRE) {
               BlockEntity be2 = level.getBlockEntity(pos.relative(segment.getDirection()));
               if (be2 == null || be2 instanceof TileFluidPipe) {
                  continue;
               }
            }

            segmentsMap.put(
               new AABB(pos).deflate(0.34, 0.34, 0.34).move(segment.xWithMult(0.34), segment.yWithMult(0.34), segment.zWithMult(0.34)), segment
            );
         }

         ArrayList<AABB> bbs = new ArrayList<>(segmentsMap.keySet());
         bbs.sort(Comparator.comparingDouble(box -> box.distanceToSqr(player.position())));

         for (AABB aabb : bbs) {
            if (aabb.contains(trace.getLocation())) {
               pipe.handleInteraction(segmentsMap.get(aabb), player.isCrouching(), player);
               return InteractionResult.SUCCESS;
            }
         }
      }

      return InteractionResult.PASS;
   }
}
