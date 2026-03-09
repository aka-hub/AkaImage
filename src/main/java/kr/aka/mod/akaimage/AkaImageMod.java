package kr.aka.mod.akaimage;

import com.mojang.logging.LogUtils;
import kr.aka.mod.akaimage.block.ImageFrameBlock;
import kr.aka.mod.akaimage.block.ImageFrameBlockEntity;
import kr.aka.mod.akaimage.client.ImageFrameRenderer;
import kr.aka.mod.akaimage.client.ImageFrameScreen;
import kr.aka.mod.akaimage.command.ModCommands;
import kr.aka.mod.akaimage.data.ImageDatabase;
import kr.aka.mod.akaimage.menu.ImageFrameMenu;
import kr.aka.mod.akaimage.network.ImageFramePayload;
import kr.aka.mod.akaimage.network.ImageListPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod(AkaImageMod.MODID)
public class AkaImageMod {
    public static final String MODID = "akaimage";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);

    public static final Supplier<Block> IMAGE_FRAME_BLOCK = BLOCKS.register("image_frame",
        () -> new ImageFrameBlock(BlockBehaviour.Properties.of().strength(1.5f).noOcclusion()));
    public static final Supplier<Item> IMAGE_FRAME_ITEM = ITEMS.register("image_frame",
        () -> new BlockItem(IMAGE_FRAME_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<ImageFrameBlockEntity>> IMAGE_FRAME_BE = BLOCK_ENTITIES.register("image_frame_be",
        () -> BlockEntityType.Builder.of(ImageFrameBlockEntity::new, IMAGE_FRAME_BLOCK.get()).build(null));

    // 메뉴 등록
    public static final Supplier<MenuType<ImageFrameMenu>> IMAGE_FRAME_MENU = MENUS.register("image_frame_menu",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            net.minecraft.core.BlockPos pos = data.readBlockPos();
            return new ImageFrameMenu(windowId, inv,
                (ImageFrameBlockEntity) inv.player.level().getBlockEntity(pos));
        }));

    private static final ResourceKey<CreativeModeTab> CORE_TAB_KEY = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        ResourceLocation.fromNamespaceAndPath("akacore", "aka_qol_tab")
    );

    public AkaImageMod(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);

        modEventBus.addListener(this::addCreativeTab);
        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.register(ModServerEvents.class);
    }

    private void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CORE_TAB_KEY)) {
            event.accept(IMAGE_FRAME_ITEM.get());
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);


        registrar.playToServer(ImageFramePayload.TYPE, ImageFramePayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                Player player = context.player();
                if (player == null) return;

                if ("apply".equals(payload.action())) {

                    if (player.level().getBlockEntity(payload.pos()) instanceof ImageFrameBlockEntity frame) {
                        String imgAlias = payload.imgAlias();
                        String url = imgAlias.isEmpty() ? frame.getImageUrl()
                            : ImageDatabase.get().getUrl(imgAlias);
                        if (url == null) url = "";

                        frame.setData(payload.frameId(), url);
                        frame.setSize(payload.width(), payload.height());

                        if (!payload.frameId().isEmpty()) {
                            ImageDatabase.get().registerFrame(payload.frameId(), payload.pos());
                        }
                    }

                } else if ("register".equals(payload.action())) {
                    ImageDatabase.get().registerImage(payload.alias(), payload.url());
                    List<String> aliases = new ArrayList<>(ImageDatabase.get().getAllAliases());
                    PacketDistributor.sendToPlayer(
                        (net.minecraft.server.level.ServerPlayer) player,
                        new ImageListPayload(aliases)
                    );

                } else if ("delete".equals(payload.action())) {
                    ImageDatabase.get().deleteImage(payload.alias());
                    List<String> aliases = new ArrayList<>(ImageDatabase.get().getAllAliases());
                    PacketDistributor.sendToPlayer(
                        (net.minecraft.server.level.ServerPlayer) player,
                        new ImageListPayload(aliases)
                    );
                }
            });
        });


        registrar.playToClient(ImageListPayload.TYPE, ImageListPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.screen instanceof ImageFrameScreen screen) {
                    screen.receiveAliasList(payload.aliases());
                }
            });
        });
    }

    @SuppressWarnings("removal")
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(IMAGE_FRAME_BE.get(), ImageFrameRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(AkaImageMod.IMAGE_FRAME_MENU.get(), ImageFrameScreen::new);
        }
    }
}