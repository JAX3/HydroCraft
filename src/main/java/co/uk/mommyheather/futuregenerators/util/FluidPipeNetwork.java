package co.uk.mommyheather.futuregenerators.util;

import co.uk.mommyheather.futuregenerators.tile.TileFluidPipe;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluidPipeNetwork {
   public HashMap<Long, TileFluidPipe> pipes = new HashMap<>();
   private ArrayList<Long> changedPipes = new ArrayList<>();
   public long id;

   public FluidPipeNetwork(long id) {
      this.id = id;
   }

   public void addPipe(long pos, TileFluidPipe pipe) {
      if (this.pipes.containsKey(pos)) {
         throw new RuntimeException(String.format("Pipe with position {} added to a network that already had a pipe in that position!", pos));
      }

      this.pipes.put(pos, pipe);
      this.setChanged();
   }

   public void removePipe(long pos) {
      if (!this.pipes.containsKey(pos)) {
         throw new RuntimeException(String.format("Pipe with position {} attempted to remove from a network that had no pipe in that position!", pos));
      }

      this.pipes.remove(pos);
      this.setChanged();
   }

   public void mergeIntoSelf(FluidPipeNetwork... networks) {
      for (FluidPipeNetwork network : networks) {
         for (TileFluidPipe pipe : network.pipes.values()) {
            pipe.network = this;
         }

         this.pipes.putAll(network.pipes);
         network.pipes.clear();
      }

      this.setChanged();
   }

   public void rebuildNetwork() {
      ArrayList<Long> oldPositions = new ArrayList<>(this.pipes.keySet());
      BlockPos start = BlockPos.of(oldPositions.remove(0));
      TileFluidPipe startPipe = this.pipes.get(start.asLong());
      HashMap<Long, TileFluidPipe> tempPipes = new HashMap<>(this.pipes);
      this.pipes.clear();
      this.pipes.put(start.asLong(), startPipe);
      this.id = start.asLong();
      ArrayList<BlockPos> toSearch = new ArrayList<>();
      toSearch.add(start);

      while (!toSearch.isEmpty()) {
         for (int i = toSearch.size() - 1; i >= 0; i--) {
            BlockPos pos = toSearch.get(i);

            for (Direction direction : Direction.values()) {
               BlockPos pos2 = pos.relative(direction);
               BlockEntity be = startPipe.getLevel().getBlockEntity(pos2);
               if (be != null && be instanceof TileFluidPipe newPipe) {
                  oldPositions.remove(pos2.asLong());
                  newPipe.network = this;
                  this.pipes.put(pos2.asLong(), newPipe);
               }
            }

            toSearch.remove(i);
         }
      }

      this.setChanged();
      if (!oldPositions.isEmpty()) {
         while (!oldPositions.isEmpty()) {
            start = BlockPos.of(oldPositions.remove(0));
            startPipe = tempPipes.get(start.asLong());
            FluidPipeNetwork newNetwork = new FluidPipeNetwork(start.asLong());
            toSearch = new ArrayList<>();
            toSearch.add(start);

            while (!toSearch.isEmpty()) {
               for (int i = toSearch.size() - 1; i >= 0; i--) {
                  BlockPos pos = toSearch.get(i);

                  for (Direction direction : Direction.values()) {
                     BlockPos pos2 = pos.relative(direction);
                     BlockEntity be = startPipe.getLevel().getBlockEntity(pos2);
                     if (be != null && be instanceof TileFluidPipe newPipe) {
                        oldPositions.remove(pos2.asLong());
                        newPipe.network = newNetwork;
                        newNetwork.pipes.put(pos2.asLong(), newPipe);
                     }
                  }

                  toSearch.remove(i);
               }
            }

            newNetwork.setChanged();
         }
      }
   }

   public void setChanged() {
      this.changedPipes = new ArrayList<>(this.pipes.keySet());
   }

   public boolean hasChanged(BlockPos pos) {
      return this.changedPipes.remove(pos.asLong());
   }
}
