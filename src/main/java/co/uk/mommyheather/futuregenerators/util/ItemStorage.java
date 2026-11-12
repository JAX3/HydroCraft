package co.uk.mommyheather.futuregenerators.util;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A machine's item slots, standing in for Forge's old {@code ItemStackHandler}: NeoForge's transfer-API handler
 * (hoppers, pipes and GUI slots go through it) with the old direct slot helpers for the machines' own logic.
 */
public class ItemStorage extends ItemStacksResourceHandler {
    private final BiPredicate<Integer, ItemStack> validator;
    private final IntPredicate canExtract;
    private final Runnable onChanged;

    public ItemStorage(int size, BiPredicate<Integer, ItemStack> validator, Runnable onChanged) {
        this(size, validator, slot -> true, onChanged);
    }

    public ItemStorage(int size, BiPredicate<Integer, ItemStack> validator, IntPredicate canExtract, Runnable onChanged) {
        super(size);
        this.validator = validator;
        this.canExtract = canExtract;
        this.onChanged = onChanged;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return !resource.isEmpty() && validator.test(index, resource.toStack());
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return canExtract.test(index) ? super.extract(index, resource, amount, transaction) : 0;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        onChanged.run();
    }

    public int getSlots() {
        return size();
    }

    /** The slot's contents. Don't modify the returned stack; use {@link #setStackInSlot}. */
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    /** Replaces a slot's contents without validation (machine-internal). */
    public void setStackInSlot(int slot, ItemStack stack) {
        ItemStack previous = stacks.get(slot);
        stacks.set(slot, stack);
        onContentsChanged(slot, previous);
    }

    /** Removes up to {@code amount} items from a slot right away (machine-internal) and returns them. */
    public ItemStack extractItem(int slot, int amount) {
        ItemStack current = stacks.get(slot);
        if (current.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        int taken = Math.min(amount, current.getCount());
        setStackInSlot(slot, current.getCount() > taken ? current.copyWithCount(current.getCount() - taken) : ItemStack.EMPTY);
        return current.copyWithCount(taken);
    }
}
