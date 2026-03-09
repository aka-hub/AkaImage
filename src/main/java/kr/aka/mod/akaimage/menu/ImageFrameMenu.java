package kr.aka.mod.akaimage.menu;

import kr.aka.mod.akaimage.AkaImageMod;
import kr.aka.mod.akaimage.block.ImageFrameBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ImageFrameMenu extends AbstractContainerMenu {
    public final ImageFrameBlockEntity blockEntity;

    public ImageFrameMenu(int containerId, Inventory inv, ImageFrameBlockEntity entity) {
        super(AkaImageMod.IMAGE_FRAME_MENU.get(), containerId);
        this.blockEntity = entity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }
}