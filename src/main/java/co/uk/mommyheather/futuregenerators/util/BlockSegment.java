package co.uk.mommyheather.futuregenerators.util;

import net.minecraft.core.Direction;

public enum BlockSegment {
   NORTH(Direction.NORTH, 0.0, 0.0, -1.0),
   SOUTH(Direction.SOUTH, 0.0, 0.0, 1.0),
   EAST(Direction.EAST, 1.0, 0.0, 0.0),
   WEST(Direction.WEST, -1.0, 0.0, 0.0),
   UP(Direction.UP, 0.0, 1.0, 0.0),
   DOWN(Direction.DOWN, 0.0, -1.0, 0.0),
   CENTRE(null, 0.0, 0.0, 0.0);

   private final Direction direction;
   private final double xOffset;
   private final double yOffset;
   private final double zOffset;

   BlockSegment(Direction direction, double xOffset, double yOffset, double zOffset) {
      this.direction = direction;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.zOffset = zOffset;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public double getxOffset() {
      return this.xOffset;
   }

   public double getyOffset() {
      return this.yOffset;
   }

   public double getzOffset() {
      return this.zOffset;
   }

   public double xWithMult(double in) {
      return this.xOffset * in;
   }

   public double yWithMult(double in) {
      return this.yOffset * in;
   }

   public double zWithMult(double in) {
      return this.zOffset * in;
   }
}
