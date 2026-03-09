package kr.aka.mod.akaimage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.aka.mod.akaimage.block.ImageFrameBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class ImageFrameRenderer implements BlockEntityRenderer<ImageFrameBlockEntity> {

    public ImageFrameRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ImageFrameBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String url = entity.getImageUrl();
        if (url == null || url.isEmpty()) return;

        ResourceLocation texture = ClientTextureHandler.getTexture(url);
        if (texture == null) return;


        net.minecraft.core.Direction facing = net.minecraft.core.Direction.SOUTH;
        if (entity.hasLevel()) {
            net.minecraft.world.level.block.state.BlockState state = entity.getBlockState();

            if (state.hasProperty(kr.aka.mod.akaimage.block.ImageFrameBlock.FACING)) {
                facing = state.getValue(kr.aka.mod.akaimage.block.ImageFrameBlock.FACING);
            }
        }

        poseStack.pushPose();


        poseStack.translate(0.5, 0.5, 0.5);

        //방향 무회전 앞에서 바로보이게
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-facing.toYRot()));

        // 앞으로 조금 튀어나오게
        poseStack.translate(-0.5f, -0.5f, 0.51f);

        // 크기 적용
        poseStack.scale(entity.getWidth(), entity.getHeight(), 1.0f);

        // 텍스처 그리기
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutout(texture));
        Matrix4f matrix = poseStack.last().pose();

        vertex(builder, matrix, 0f, 0f, 0, 0, 1, packedLight);
        vertex(builder, matrix, 1f, 0f, 0, 1, 1, packedLight);
        vertex(builder, matrix, 1f, 1f, 0, 1, 0, packedLight);
        vertex(builder, matrix, 0f, 1f, 0, 0, 0, packedLight);

        poseStack.popPose();
    }

    private void vertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float u, float v, int light) {
        builder.addVertex(matrix, x, y, z)
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0, 0, 1);
    }
}