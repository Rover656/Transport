package xyz.brassgoggledcoders.transport.api.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleInstance<MOD extends Module<MOD>>
        implements INBTSerializable<CompoundNBT>, ICapabilityProvider {
    private final MOD module;
    private final IModularEntity modularEntity;

    protected ModuleInstance(MOD module, IModularEntity modularEntity) {
        this.module = module;
        this.modularEntity = modularEntity;
    }

    public void tick() {

    }

    public ActionResultType applyInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        return ActionResultType.PASS;
    }

    public int getComparatorLevel() {
        return -1;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }

    public MOD getModule() {
        return module;
    }

    public IModularEntity getModularEntity() {
        return modularEntity;
    }

    public ITextComponent getDisplayName() {
        return this.getModule().getDisplayName();
    }
}