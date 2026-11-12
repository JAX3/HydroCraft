package co.uk.mommyheather.futuregenerators.util;

import java.util.function.Predicate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

/**
 * A single fluid tank, standing in for Forge's old {@code FluidTank}. It is NeoForge's transfer-API handler (pipes,
 * buckets and other mods insert and extract through it inside transactions) with the old direct helpers on top for the
 * machines' own logic: {@link #fill}, {@link #drain}, {@link #getFluid} and so on.
 */
public class FluidTank extends FluidStacksResourceHandler {
    private final Predicate<FluidResource> validator;
    private final Runnable onChanged;

    public FluidTank(int capacity, Runnable onChanged) {
        this(capacity, fluid -> true, onChanged);
    }

    public FluidTank(int capacity, Predicate<FluidResource> validator, Runnable onChanged) {
        super(1, capacity);
        this.validator = validator;
        this.onChanged = onChanged;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return validator.test(resource);
    }

    @Override
    protected void onContentsChanged(int index, FluidStack previousContents) {
        onChanged.run();
    }

    public FluidStack getFluid() {
        FluidResource resource = getResource(0);
        return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(getAmountAsInt(0));
    }

    public FluidResource getFluidResource() {
        return getResource(0);
    }

    public int getFluidAmount() {
        return getAmountAsInt(0);
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return getFluidAmount() <= 0;
    }

    /** Applies a changed capacity, discarding fluid above a smaller one. */
    public void setCapacity(int capacity) {
        if (this.capacity == capacity) return;
        this.capacity = capacity;
        if (getFluidAmount() > capacity) set(0, getResource(0), capacity);
    }

    public void setFluid(FluidResource fluid, int amount) {
        set(0, amount > 0 ? fluid : FluidResource.EMPTY, Math.max(0, amount));
    }

    /** Adds up to {@code amount} of {@code fluid} right away (machine-internal). Returns the amount added. */
    public int fill(FluidResource fluid, int amount) {
        if (fluid.isEmpty() || amount <= 0 || !validator.test(fluid)) return 0;
        if (!isEmpty() && !getResource(0).equals(fluid)) return 0;
        int added = Math.min(amount, capacity - getFluidAmount());
        if (added > 0) set(0, fluid, getFluidAmount() + added);
        return added;
    }

    /** Removes up to {@code amount} right away (machine-internal). Returns the amount removed. */
    public int drain(int amount) {
        int removed = Math.max(0, Math.min(amount, getFluidAmount()));
        if (removed > 0) setFluid(getResource(0), getFluidAmount() - removed);
        return removed;
    }
}
