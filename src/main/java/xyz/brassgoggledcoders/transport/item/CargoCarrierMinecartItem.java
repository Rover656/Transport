package xyz.brassgoggledcoders.transport.item;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.brassgoggledcoders.transport.Transport;
import xyz.brassgoggledcoders.transport.api.TransportAPI;
import xyz.brassgoggledcoders.transport.api.TransportObjects;
import xyz.brassgoggledcoders.transport.api.cargo.Cargo;
import xyz.brassgoggledcoders.transport.api.cargo.CargoInstance;
import xyz.brassgoggledcoders.transport.content.TransportEntities;
import xyz.brassgoggledcoders.transport.entity.CargoCarrierMinecartEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.entity.item.minecart.AbstractMinecartEntity.Type.CHEST;

public class CargoCarrierMinecartItem extends MinecartItem {
    public CargoCarrierMinecartItem() {
        this(new Item.Properties()
                .containerItem(Items.MINECART)
                .group(Transport.ITEM_GROUP));
    }

    public CargoCarrierMinecartItem(Properties properties) {
        super(CHEST, properties);
        this.addPropertyOverride(new ResourceLocation(Transport.ID, "cargo"),
                (itemStack, world, livingEntity) -> Optional.ofNullable(getCargo(itemStack.getChildTag("cargo")))
                        .map(ForgeRegistryEntry::getRegistryName)
                        .map(TransportAPI.CARGO.get()::getID)
                        .map(id -> id / 1000F)
                        .orElse(0.000F));
    }

    public static ItemStack getCartStack(CargoInstance cargoInstance) {
        ItemStack itemStack = new ItemStack(TransportEntities.CARGO_MINECART_ITEM
                .map(Item::asItem)
                .orElse(Items.MINECART));
        itemStack.getOrCreateChildTag("cargo").putString("name", Objects.requireNonNull(
                cargoInstance.getModule().getRegistryName()).toString());
        return itemStack;
    }

    public static Cargo getCargo(@Nullable CompoundNBT cargo) {
        return Optional.ofNullable(cargo)
                .map(compoundNBT -> compoundNBT.getString("name"))
                .map(TransportAPI::getCargo)
                .orElse(null);
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getPos();
        BlockState blockstate = world.getBlockState(blockPos);
        if (!blockstate.isIn(BlockTags.RAILS)) {
            return ActionResultType.FAIL;
        } else {
            ItemStack itemStack = context.getItem();
            if (!world.isRemote) {
                RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockPos, null) : RailShape.NORTH_SOUTH;
                double d0 = 0.0D;
                if (railshape.isAscending()) {
                    d0 = 0.5D;
                }

                CargoCarrierMinecartEntity cargoCarrierMinecartEntity = new CargoCarrierMinecartEntity(world,
                        blockPos.getX() + 0.5D,
                        blockPos.getY() + 0.0625D + d0, blockPos.getZ() + 0.5D);
                if (itemStack.hasDisplayName()) {
                    cargoCarrierMinecartEntity.setCustomName(itemStack.getDisplayName());
                }

                CompoundNBT moduleNBT = itemStack.getChildTag("modules");
                if (moduleNBT != null) {
                    cargoCarrierMinecartEntity.getModuleCase().deserializeNBT(moduleNBT);
                }

                CompoundNBT cargoNBT = itemStack.getChildTag("cargo");
                if (cargoNBT != null && cargoNBT.contains("name")) {
                    Cargo cargo = TransportAPI.getCargo(cargoNBT.getString("name"));
                    if (cargo != null) {
                        cargoCarrierMinecartEntity.getModuleCase().addComponent(cargo);
                        if (cargoNBT.contains("instance")) {
                            for (CargoInstance cargoInstance : cargoCarrierMinecartEntity.<Cargo, CargoInstance>getModuleInstances(
                                    TransportObjects.CARGO_TYPE)) {
                                if (cargoInstance.getModule() == cargo) {
                                    cargoInstance.deserializeNBT(cargoNBT.getCompound("instance"));
                                    break;
                                }
                            }
                        }
                    }
                }

                world.addEntity(cargoCarrierMinecartEntity);
            }

            itemStack.shrink(1);
            return ActionResultType.SUCCESS;
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            TransportAPI.CARGO.get().getValues()
                    .stream()
                    .filter(cargo -> !cargo.isEmpty())
                    .map(Cargo::getRegistryName)
                    .filter(Objects::nonNull)
                    .map(ResourceLocation::toString)
                    .map(resourceLocation -> {
                        ItemStack itemStack = new ItemStack(this);
                        itemStack.getOrCreateChildTag("cargo").putString("name", resourceLocation);
                        return itemStack;
                    })
                    .forEach(items::add);
        }
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        return new TranslationTextComponent("text.transport.with",
                Items.MINECART.getDisplayName(stack),
                getCargo(stack.getChildTag("cargo")).getDisplayName());
    }
}
