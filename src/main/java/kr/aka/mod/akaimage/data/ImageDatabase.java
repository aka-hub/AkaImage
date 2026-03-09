package kr.aka.mod.akaimage.data;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class ImageDatabase {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(BlockPos.class, new BlockPosAdapter())
        .create();
    private static final String FILE_NAME = "akaimage_db.json";

    private final Map<String, String> images = new HashMap<>();
    private final Map<String, BlockPos> frames = new HashMap<>();

    private static ImageDatabase instance;

    public static ImageDatabase get() {
        if (instance == null) instance = new ImageDatabase();
        return instance;
    }

    public static void init(net.minecraft.server.MinecraftServer server) {
        instance = new ImageDatabase();
        instance.load(server);
    }

    public void registerImage(String alias, String url) {
        images.put(alias, url);
        save();
    }

    public String getUrl(String alias) {
        return images.get(alias);
    }

    public Set<String> getAllAliases() {
        return Collections.unmodifiableSet(images.keySet());
    }

    public void deleteImage(String alias) {
        images.remove(alias);
        save();
    }

    public void registerFrame(String frameId, BlockPos pos) {
        frames.put(frameId, pos);
        save();
    }

    public BlockPos getFramePos(String frameId) {
        return frames.get(frameId);
    }

    public void save() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        Path path = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
        path.getParent().toFile().mkdirs();

        try (Writer writer = new FileWriter(path.toFile())) {
            DatabaseWrapper wrapper = new DatabaseWrapper(images, frames);
            GSON.toJson(wrapper, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
        File file = path.toFile();
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            DatabaseWrapper wrapper = GSON.fromJson(reader, DatabaseWrapper.class);
            if (wrapper != null) {
                this.images.clear();
                if (wrapper.images != null) this.images.putAll(wrapper.images);
                this.frames.clear();
                if (wrapper.frames != null) this.frames.putAll(wrapper.frames);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // BlockPos를 JSON xyz 형태로
    private static class BlockPosAdapter implements JsonSerializer<BlockPos>, JsonDeserializer<BlockPos> {
        @Override
        public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            obj.addProperty("z", src.getZ());
            return obj;
        }

        @Override
        public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());
        }
    }

    private static class DatabaseWrapper {
        Map<String, String> images;
        Map<String, BlockPos> frames;
        DatabaseWrapper(Map<String, String> images, Map<String, BlockPos> frames) {
            this.images = images;
            this.frames = frames;
        }
    }
}