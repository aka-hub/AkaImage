package kr.aka.mod.akaimage.block;

import kr.aka.mod.akaimage.AkaImageMod;
import kr.aka.mod.akaimage.menu.ImageFrameMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ImageFrameBlockEntity extends BlockEntity implements MenuProvider {
    private String frameId = "";
    private String imageUrl = "";

    private float width = 1.0f;
    private float height = 1.0f;

    public ImageFrameBlockEntity(BlockPos pos, BlockState state) {
        super(AkaImageMod.IMAGE_FRAME_BE.get(), pos, state);
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void setData(String frameId, String imageUrl) {
        this.frameId = frameId;
        this.imageUrl = imageUrl;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public String getImageUrl() { return imageUrl; }
    public String getFrameId() { return frameId; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("FrameId", frameId);
        tag.putString("ImageUrl", imageUrl);
        tag.putFloat("Width", width);
        tag.putFloat("Height", height);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.frameId = tag.getString("FrameId");
        this.imageUrl = tag.getString("ImageUrl");
        if (tag.contains("Width")) this.width = tag.getFloat("Width");
        if (tag.contains("Height")) this.height = tag.getFloat("Height");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        if (pkt.getTag() != null) {
            this.loadAdditional(pkt.getTag(), lookupProvider);
        }
    }


    @Override
    public Component getDisplayName() {
        return Component.literal("액자 설정");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ImageFrameMenu(containerId, inventory, this);
    }
}