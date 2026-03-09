package kr.aka.mod.akaimage.network;

import kr.aka.mod.akaimage.AkaImageMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record ImageFramePayload(
    String action,
    BlockPos pos,
    String frameId,
    String imgAlias,
    float width,
    float height,
    String alias,
    String url
) implements CustomPacketPayload {

    public static final Type<ImageFramePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AkaImageMod.MODID, "image_frame_packet")
    );

    public static final StreamCodec<FriendlyByteBuf, ImageFramePayload> CODEC = new StreamCodec<>() {
        @Override
        public ImageFramePayload decode(FriendlyByteBuf buf) {
            return new ImageFramePayload(
                buf.readUtf(),
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readUtf(),
                buf.readUtf()
            );
        }

        @Override
        public void encode(FriendlyByteBuf buf, ImageFramePayload payload) {
            buf.writeUtf(payload.action());
            buf.writeBlockPos(payload.pos());
            buf.writeUtf(payload.frameId());
            buf.writeUtf(payload.imgAlias());
            buf.writeFloat(payload.width());
            buf.writeFloat(payload.height());
            buf.writeUtf(payload.alias());
            buf.writeUtf(payload.url());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}