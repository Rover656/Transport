package xyz.brassgoggledcoders.transport.api.manager;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import xyz.brassgoggledcoders.transport.api.TransportCapabilities;
import xyz.brassgoggledcoders.transport.util.WorldUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class Worker implements IWorker {
    private final NonNullLazy<ItemStack> representativeLazy;
    private final ManagerType type;
    private LazyOptional<IManager> manager;
    private BlockPos managerPos;
    private UUID uniqueId;

    public Worker() {
        this(null);
    }

    public Worker(@Nullable ManagerType type) {
        this(type, () -> ItemStack.EMPTY);
    }

    public Worker(@Nullable ManagerType type, @Nonnull NonNullSupplier<ItemStack> representativeSupplier) {
        this.type = type;
        this.uniqueId = UUID.randomUUID();
        this.representativeLazy = NonNullLazy.of(representativeSupplier);
    }

    @Nonnull
    @Override
    public LazyOptional<IManager> getManager(IBlockReader blockReader) {
        if (this.getManagerPos() != null) {
            if (manager == null && WorldUtils.isBlockLoaded(blockReader, this.getManagerPos())) {
                TileEntity tileEntity = blockReader.getTileEntity(this.getManagerPos());
                if (tileEntity != null) {
                    this.manager = tileEntity.getCapability(TransportCapabilities.MANAGER);
                    if (this.manager.isPresent()) {
                        this.manager.addListener(this::invalidatedManager);
                    }
                }
            }
        }
        return manager != null ? manager : LazyOptional.empty();
    }

    @Override
    @Nullable
    public BlockPos getManagerPos() {
        return managerPos != null && managerPos != BlockPos.ZERO ? managerPos : null;
    }

    @Override
    public void setManagerPos(@Nullable BlockPos managerPos) {
        this.managerPos = managerPos;
    }

    @Override
    public boolean isValidManager(@Nonnull IManager manager) {
        return type == null || type == manager.getType();
    }

    @Override
    public boolean hasCustomRepresentative() {
        return !this.getCustomRepresentative().isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getCustomRepresentative() {
        return this.representativeLazy.get();
    }

    @Override
    @Nonnull
    public UUID getUniqueId() {
        return uniqueId;
    }

    private void invalidatedManager(LazyOptional<IManager> optional) {
        this.manager = null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (this.getManagerPos() != null) {
            nbt.putLong("managerPos", this.getManagerPos().toLong());
        }
        nbt.putUniqueId("uniqueId", this.uniqueId);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("managerPos")) {
            this.managerPos = BlockPos.fromLong(nbt.getLong("managerPos"));
        }
        this.uniqueId = nbt.getUniqueId("uniqueId");
    }
}