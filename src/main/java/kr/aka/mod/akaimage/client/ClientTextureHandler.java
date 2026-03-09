package kr.aka.mod.akaimage.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTextureHandler {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public static ResourceLocation getTexture(String urlStr) {
        if (urlStr == null || urlStr.isEmpty()) return null;

        // 캐시에 있으면 반환
        if (TEXTURE_CACHE.containsKey(urlStr)) {
            return TEXTURE_CACHE.get(urlStr);
        }

        TEXTURE_CACHE.put(urlStr, null);

        EXECUTOR.submit(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.connect();

                if (conn.getResponseCode() / 100 != 2) {
                    throw new Exception("HTTP Error: " + conn.getResponseCode());
                }

                try (InputStream stream = conn.getInputStream()) {

                    byte[] data = stream.readAllBytes();

                    ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
                    buffer.put(data);
                    buffer.flip();

                    // NativeImage
                    NativeImage image = NativeImage.read(buffer);


                    Minecraft.getInstance().execute(() -> {
                        DynamicTexture dynamicTexture = new DynamicTexture(image);

                        String safeId = "img_" + Integer.toHexString(urlStr.hashCode());
                        ResourceLocation rl = Minecraft.getInstance().getTextureManager().register(safeId, dynamicTexture);
                        TEXTURE_CACHE.put(urlStr, rl);
                        // System.out.println("이미지 로딩 성공: " + urlStr); // 디버그용
                    });
                }
            } catch (Exception e) {

                System.err.println("§c[AkaImage] 이미지 로딩 실패: " + urlStr);
                System.err.println("§c원인: " + e.getMessage());
            }
        });

        return null;
    }
}