package xyz.brassgoggledcoders.transport.api.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import xyz.brassgoggledcoders.transport.container.ModuleContainerProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IModularEntity extends IItemProvider {
    void openContainer(PlayerEntity playerEntity, INamedContainerProvider provider, Consumer<PacketBuffer> packetBufferConsumer);

    default boolean canInteractWith(PlayerEntity playerEntity) {
        return this.getSelf().isAlive() && playerEntity.getDistanceSq(this.getSelf()) < 64.0D;
    }

    @Nonnull
    default World getTheWorld() {
        return this.getSelf().world;
    }

    @Nonnull
    Entity getSelf();

    boolean canEquipComponent(Module<?> module);

    @Nonnull
    ITextComponent getCarrierDisplayName();

    ModuleCase getModuleCase();

    @Nullable
    default <T extends Module<T>, U extends ModuleInstance<T>> U getModuleInstance(ModuleType<T> moduleType) {
        Collection<? extends U> moduleInstances = this.getModuleInstances(moduleType);
        if (moduleInstances.isEmpty()) {
            return null;
        } else {
            return moduleInstances.stream()
                    .findFirst()
                    .orElse(null);
        }
    }

    @Nullable
    default <T extends Module<T>, U extends ModuleInstance<T>> U getModuleInstance(Supplier<ModuleType<T>> componentType) {
        return getModuleInstance(componentType.get());
    }

    @Nonnull
    default <T extends Module<T>, U extends ModuleInstance<T>> Collection<? extends U> getModuleInstances(ModuleType<T> moduleType) {
        return this.getModuleCase().getComponentInstances(moduleType);
    }

    @Nonnull
    default <T extends Module<T>, U extends ModuleInstance<T>> Collection<? extends U> getModuleInstances(Supplier<ModuleType<T>> componentType) {
        return getModuleInstances(componentType.get());
    }
}