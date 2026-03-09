package kr.aka.mod.akaimage.network;

import kr.aka.mod.akaimage.AkaImageMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;


public record ImageListPayload(
    List<String> aliases
) implements CustomPacketPayload {

    public static final Type<ImageListPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AkaImageMod.MODID, "image_list_packet")
    );

    public static final StreamCodec<FriendlyByteBuf, ImageListPayload> CODEC = new StreamCodec<>() {
        @Override
        public ImageListPayload decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<String> list = new ArrayList<>();
            for (int i = 0; i < size; i++) list.add(buf.readUtf());
            return new ImageListPayload(list);
        }

        @Override
        public void encode(FriendlyByteBuf buf, ImageListPayload payload) {
            buf.writeVarInt(payload.aliases().size());
            for (String alias : payload.aliases()) buf.writeUtf(alias);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}