package co.uk.mommyheather.futuregenerators.util;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * An FE buffer: NeoForge's {@link SimpleEnergyHandler} (what cables and other mods see), plus the direct helpers the
 * machines use on their own energy, which ignore the external insert and extract limits.
 */
public class FutureGeneratorsEnergyStorage extends SimpleEnergyHandler {
    private final Runnable onChanged;

    public FutureGeneratorsEnergyStorage(int capacity, Runnable onChanged) {
        this(capacity, Integer.MAX_VALUE, Integer.MAX_VALUE, onChanged);
    }

    public FutureGeneratorsEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = onChanged;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        onChanged.run();
    }

    public int getEnergyStored() {
        return energy;
    }

    public int getMaxEnergyStored() {
        return capacity;
    }

    /** Applies a changed capacity, discarding energy above a smaller one. */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
        if (energy > capacity) set(capacity);
    }

    public void setEnergy(int energy) {
        set(Math.max(0, Math.min(energy, capacity)));
    }

    /** Adds up to {@code amount} FE right away (machine-internal). Returns the amount added. */
    public int receiveEnergy(int amount) {
        int added = Math.max(0, Math.min(amount, capacity - energy));
        if (added > 0) set(energy + added);
        return added;
    }

    /** Removes up to {@code amount} FE right away (machine-internal). Returns the amount removed. */
    public int extractEnergy(int amount) {
        int removed = Math.max(0, Math.min(amount, energy));
        if (removed > 0) set(energy - removed);
        return removed;
    }
}
