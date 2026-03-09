package kr.aka.mod.akaimage.client;

import kr.aka.mod.akaimage.menu.ImageFrameMenu;
import kr.aka.mod.akaimage.network.ImageFramePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ImageFrameScreen extends AbstractContainerScreen<ImageFrameMenu> {


    private static final int W         = 360;
    private static final int H         = 220;
    private static final int LEFT_X    = 8;
    private static final int RIGHT_X   = 186;
    private static final int PANEL_W   = 160;

    private static final int LIST_Y        = 28;
    private static final int LIST_ITEM_H   = 14;
    private static final int LIST_VISIBLE  = 6;

    private static final int LIST_BOTTOM   = LIST_Y + LIST_VISIBLE * LIST_ITEM_H;

    private static final int REG_Y         = LIST_BOTTOM + 16;


    private EditBox frameIdInput;
    private EditBox widthInput;
    private EditBox heightInput;
    private EditBox aliasInput;
    private EditBox urlInput;


    private List<String> aliasList   = new ArrayList<>();
    private int          scrollOffset = 0;
    private String       selectedAlias = "";


    public ImageFrameScreen(ImageFrameMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = W;
        this.imageHeight = H;
    }

    public void receiveAliasList(List<String> list) {
        this.aliasList = new ArrayList<>(list);
        if (!aliasList.contains(selectedAlias)) selectedAlias = "";
    }


    @Override
    protected void init() {
        super.init();


        frameIdInput = new EditBox(this.font, leftPos + LEFT_X, topPos + 28, 168, 18, Component.literal(""));
        frameIdInput.setValue(menu.blockEntity.getFrameId());
        frameIdInput.setMaxLength(64);
        addRenderableWidget(frameIdInput);

        widthInput = new EditBox(this.font, leftPos + LEFT_X, topPos + 80, 80, 18, Component.literal(""));
        widthInput.setValue(String.valueOf(menu.blockEntity.getWidth()));
        addRenderableWidget(widthInput);

        heightInput = new EditBox(this.font, leftPos + LEFT_X + 88, topPos + 80, 80, 18, Component.literal(""));
        heightInput.setValue(String.valueOf(menu.blockEntity.getHeight()));
        addRenderableWidget(heightInput);

        addRenderableWidget(Button.builder(Component.literal("적용"), btn -> applyFrame())
            .pos(leftPos + LEFT_X, topPos + 110).size(168, 20).build());



        int deleteBtnX = RIGHT_X + this.font.width("이미지 목록") + 6;
        addRenderableWidget(Button.builder(Component.literal("삭제"), btn -> deleteSelected())
            .pos(leftPos + deleteBtnX, topPos + 5).size(34, 12).build());

        //스크롤
        addRenderableWidget(Button.builder(Component.literal("▲"), btn -> {
            if (scrollOffset > 0) scrollOffset--;
        }).pos(leftPos + RIGHT_X + PANEL_W + 2, topPos + LIST_Y).size(12, 12).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), btn -> {
            if (scrollOffset < Math.max(0, aliasList.size() - LIST_VISIBLE)) scrollOffset++;
        }).pos(leftPos + RIGHT_X + PANEL_W + 2, topPos + LIST_Y + 14).size(12, 12).build());


        aliasInput = new EditBox(this.font, leftPos + RIGHT_X, topPos + REG_Y + 10, PANEL_W, 18, Component.literal(""));
        aliasInput.setMaxLength(64);
        addRenderableWidget(aliasInput);

        urlInput = new EditBox(this.font, leftPos + RIGHT_X, topPos + REG_Y + 40, PANEL_W, 18, Component.literal(""));
        urlInput.setMaxLength(512);
        addRenderableWidget(urlInput);


        int regBtnX = RIGHT_X + this.font.width("이미지 등록") + 6;
        addRenderableWidget(Button.builder(Component.literal("등록"), btn -> registerImage())
            .pos(leftPos + regBtnX, topPos + LIST_BOTTOM + 5).size(34, 12).build());
    }


    private void applyFrame() {
        try {
            String frameId = frameIdInput.getValue().trim();
            float  w       = Float.parseFloat(widthInput.getValue().trim());
            float  h       = Float.parseFloat(heightInput.getValue().trim());
            BlockPos pos   = menu.blockEntity.getBlockPos();
            PacketDistributor.sendToServer(new ImageFramePayload(
                "apply", pos, frameId, selectedAlias, w, h, "", ""
            ));
            this.onClose();
        } catch (Exception ignored) {}
    }

    private void registerImage() {
        String alias = aliasInput.getValue().trim();
        String url   = urlInput.getValue().trim();
        if (alias.isEmpty() || url.isEmpty()) return;
        PacketDistributor.sendToServer(new ImageFramePayload(
            "register", menu.blockEntity.getBlockPos(), "", "", 1f, 1f, alias, url
        ));
        if (!aliasList.contains(alias)) aliasList.add(alias);
        aliasInput.setValue("");
        urlInput.setValue("");
    }

    private void deleteSelected() {
        if (selectedAlias.isEmpty()) return;
        PacketDistributor.sendToServer(new ImageFramePayload(
            "delete", menu.blockEntity.getBlockPos(), "", "", 1f, 1f, selectedAlias, ""
        ));
        aliasList.remove(selectedAlias);
        selectedAlias = "";
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, aliasList.size() - LIST_VISIBLE)));
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < LIST_VISIBLE; i++) {
            int idx   = scrollOffset + i;
            if (idx >= aliasList.size()) break;
            int itemX = leftPos + RIGHT_X;
            int itemY = topPos  + LIST_Y + i * LIST_ITEM_H;
            if (mouseX >= itemX && mouseX < itemX + PANEL_W
                && mouseY >= itemY && mouseY < itemY + LIST_ITEM_H) {
                selectedAlias = aliasList.get(idx);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int lx = leftPos + RIGHT_X, ly = topPos + LIST_Y;
        if (mouseX >= lx && mouseX < lx + PANEL_W
            && mouseY >= ly && mouseY < ly + LIST_VISIBLE * LIST_ITEM_H) {
            scrollOffset = Math.max(0, Math.min(
                scrollOffset - (int) Math.signum(deltaY),
                Math.max(0, aliasList.size() - LIST_VISIBLE)
            ));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }



    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { applyFrame(); return true; }
        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }




    // ── 렌더링 ────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;

        // 전체 배경
        g.fill(x, y, x + W, y + H, 0xCC000000);
        // 외곽 테두리
        g.fill(x,       y,       x + W,     y + 1,     0xFFAAAAAA);
        g.fill(x,       y + H-1, x + W,     y + H,     0xFFAAAAAA);
        g.fill(x,       y,       x + 1,     y + H,     0xFFAAAAAA);
        g.fill(x + W-1, y,       x + W,     y + H,     0xFFAAAAAA);
        // 좌우 구분선
        g.fill(x + RIGHT_X - 4, y + 5, x + RIGHT_X - 3, y + H - 5, 0xFF666666);
        // 목록↔등록 구분선
        g.fill(x + RIGHT_X, y + LIST_BOTTOM + 2, x + RIGHT_X + PANEL_W, y + LIST_BOTTOM + 3, 0xFF555555);

        // 목록 배경
        g.fill(x + RIGHT_X, y + LIST_Y,
            x + RIGHT_X + PANEL_W, y + LIST_BOTTOM, 0x44FFFFFF);

        // 목록 아이템
        for (int i = 0; i < LIST_VISIBLE; i++) {
            int idx = scrollOffset + i;
            if (idx >= aliasList.size()) break;
            String  alias = aliasList.get(idx);
            int     iy    = y + LIST_Y + i * LIST_ITEM_H;
            boolean sel   = alias.equals(selectedAlias);
            boolean hov   = mouseX >= x + RIGHT_X && mouseX < x + RIGHT_X + PANEL_W
                && mouseY >= iy           && mouseY < iy + LIST_ITEM_H;

            if      (sel) g.fill(x + RIGHT_X, iy, x + RIGHT_X + PANEL_W, iy + LIST_ITEM_H, 0x88FFFF00);
            else if (hov) g.fill(x + RIGHT_X, iy, x + RIGHT_X + PANEL_W, iy + LIST_ITEM_H, 0x44FFFFFF);

            g.drawString(this.font, truncate(alias, PANEL_W - 6),
                x + RIGHT_X + 3, iy + 3, sel ? 0xFFFF00 : 0xFFFFFF, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // 왼쪽
        g.drawString(this.font, "액자 설정", LEFT_X,      8,  0xFFFFFF, false);
        g.drawString(this.font, "Frame ID",  LEFT_X,      19, 0xAAAAAA, false);
        g.drawString(this.font, "선택: " + (selectedAlias.isEmpty() ? "(없음)" : selectedAlias),
            LEFT_X, 55, selectedAlias.isEmpty() ? 0x777777 : 0xAAFF88, false);
        g.drawString(this.font, "너비", LEFT_X,      70, 0xAAAAAA, false);
        g.drawString(this.font, "높이", LEFT_X + 88, 70, 0xAAAAAA, false);

        // 오른쪽 위
        g.drawString(this.font, "이미지 목록", RIGHT_X, 8,  0xFFFFFF, false);
        g.drawString(this.font, "(클릭 선택)", RIGHT_X, 18, 0x777777, false);

        // 오른쪽 아래
        g.drawString(this.font, "이미지 등록",  RIGHT_X, LIST_BOTTOM + 5, 0xFFFFFF, false);
        g.drawString(this.font, "Alias", RIGHT_X, REG_Y,     0xAAAAAA, false);
        g.drawString(this.font, "URL",   RIGHT_X, REG_Y + 30, 0xAAAAAA, false);
    }

    private String truncate(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;
        String dot  = "...";
        int    dotW = this.font.width(dot);
        while (!text.isEmpty() && this.font.width(text) + dotW > maxWidth)
            text = text.substring(0, text.length() - 1);
        return text + dot;
    }
}