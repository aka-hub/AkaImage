package kr.aka.mod.akaimage;

import kr.aka.mod.akaimage.data.ImageDatabase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ModServerEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {

        ImageDatabase.init(event.getServer());
        AkaImageMod.LOGGER.info("[AkaImage] 데이터베이스 로드 완료!");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ImageDatabase.get().save();
        AkaImageMod.LOGGER.info("[AkaImage] 데이터베이스 저장 완료!");
    }
}