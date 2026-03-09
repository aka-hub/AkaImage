package kr.aka.mod.akaimage.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import kr.aka.mod.akaimage.block.ImageFrameBlockEntity;
import kr.aka.mod.akaimage.data.ImageDatabase;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("img")

            .then(Commands.literal("register")
                .then(Commands.argument("alias", StringArgumentType.word())
                    .then(Commands.argument("url", StringArgumentType.greedyString())
                        .executes(context -> {
                            String alias = StringArgumentType.getString(context, "alias");
                            String url = StringArgumentType.getString(context, "url");
                            ImageDatabase.get().registerImage(alias, url);
                            context.getSource().sendSuccess(() -> Component.literal("[AkaImage] 이미지 등록됨: " + alias), true);
                            return 1;
                        }))))


            .then(Commands.literal("bind")
                .then(Commands.argument("frameId", StringArgumentType.word())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();

                        BlockHitResult hit = getPlayerPOVHitResult(player.serverLevel(), player, 5.0f);

                        if (hit.getType() == HitResult.Type.BLOCK) {
                            BlockPos pos = hit.getBlockPos();
                            BlockEntity be = player.serverLevel().getBlockEntity(pos);

                            if (be instanceof ImageFrameBlockEntity frame) {
                                String frameId = StringArgumentType.getString(context, "frameId");
                                frame.setData(frameId, frame.getImageUrl());
                                ImageDatabase.get().registerFrame(frameId, pos);
                                context.getSource().sendSuccess(() -> Component.literal("[AkaImage] 이 액자는 이제 '" + frameId + "' 입니다."), true);
                                return 1;
                            }
                        }
                        context.getSource().sendFailure(Component.literal("[AkaImage] 액자를 바라보고 명령어를 입력해주세요."));
                        return 0;
                    })))


            .then(Commands.literal("show")
                .then(Commands.argument("frameId", StringArgumentType.word())
                    .then(Commands.argument("imgAlias", StringArgumentType.word())
                        .executes(context -> {
                            String frameId = StringArgumentType.getString(context, "frameId");
                            String imgAlias = StringArgumentType.getString(context, "imgAlias");
                            String url = ImageDatabase.get().getUrl(imgAlias);
                            BlockPos pos = ImageDatabase.get().getFramePos(frameId);

                            if (url == null) {
                                context.getSource().sendFailure(Component.literal("[AkaImage] 그런 이미지 이름은 없습니다."));
                                return 0;
                            }
                            if (pos == null) {
                                context.getSource().sendFailure(Component.literal("[AkaImage] 그런 액자 ID는 없습니다."));
                                return 0;
                            }

                            ServerLevel level = context.getSource().getLevel();
                            if (level.isLoaded(pos)) {
                                BlockEntity be = level.getBlockEntity(pos);
                                if (be instanceof ImageFrameBlockEntity frame) {
                                    frame.setData(frameId, url);
                                    context.getSource().sendSuccess(() -> Component.literal("[AkaImage] 화면 송출: " + imgAlias), true);
                                    return 1;
                                }
                            }
                            context.getSource().sendFailure(Component.literal("[AkaImage] 액자를 찾을 수 없습니다 (청크 로딩 안됨?)."));
                            return 0;
                        }))))


            .then(Commands.literal("size")
                .then(Commands.argument("frameId", StringArgumentType.word())
                    .then(Commands.argument("width", FloatArgumentType.floatArg(0.1f, 64.0f))
                        .then(Commands.argument("height", FloatArgumentType.floatArg(0.1f, 64.0f))
                            .executes(context -> {
                                String frameId = StringArgumentType.getString(context, "frameId");
                                float w = FloatArgumentType.getFloat(context, "width");
                                float h = FloatArgumentType.getFloat(context, "height");

                                BlockPos pos = ImageDatabase.get().getFramePos(frameId);
                                if (pos == null) {
                                    context.getSource().sendFailure(Component.literal("[AkaImage] 그런 액자 ID는 없습니다."));
                                    return 0;
                                }

                                ServerLevel level = context.getSource().getLevel();
                                if (level.isLoaded(pos)) {
                                    BlockEntity be = level.getBlockEntity(pos);
                                    if (be instanceof ImageFrameBlockEntity frame) {
                                        frame.setSize(w, h); // 엔티티의 setSize 호출
                                        context.getSource().sendSuccess(() -> Component.literal("[AkaImage] 크기 변경 완료: " + w + "x" + h), true);
                                        return 1;
                                    }
                                }
                                context.getSource().sendFailure(Component.literal("[AkaImage] 액자를 찾을 수 없습니다."));
                                return 0;
                            })))))
        );
    }

    private static BlockHitResult getPlayerPOVHitResult(ServerLevel level, ServerPlayer player, float range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(viewVec.x * range, viewVec.y * range, viewVec.z * range);
        return level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}