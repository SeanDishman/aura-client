package fmh.core.hud.client.render;

import fmh.core.hud.client.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import org.joml.Matrix4f;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class OceanEspModule extends Module {
    private static boolean dropdownOpen = false;
    private static double renderDistance = 64.0;
    private static boolean mobsDropdownOpen = false;
    private static boolean blocksDropdownOpen = false;

    private static Map<String, Boolean> mobSelection = new HashMap<>();
    private static Map<String, Boolean> blockSelection = new HashMap<>();

    // Performance optimization: cache found blocks and update periodically
    private Set<BlockPos> cachedSeaPickleBlocks = new HashSet<>();
    private long lastBlockScanTime = 0;
    private static final long BLOCK_SCAN_INTERVAL = 2000; // Scan every 2 seconds instead of every frame
    private BlockPos lastPlayerPos = null;
    private double lastScanDistance = 0;

    static {
        mobSelection.put("Tropical Fish", true);
        mobSelection.put("Pufferfish", true);
        mobSelection.put("Cod", true);
        mobSelection.put("Salmon", true);
        mobSelection.put("Frogs", true);
        mobSelection.put("Dolphins", true);
        mobSelection.put("Sea Turtles", true);

        blockSelection.put("Sea Pickles", true);
    }

    public OceanEspModule() {
        super("Ocean ESP", "Render");
        setEnabled(false);
    }

    @Override
    public void onEnable() {
        WorldRenderEvents.AFTER_ENTITIES.register(this::renderOceanEsp);
        // Clear cache when enabling
        cachedSeaPickleBlocks.clear();
        lastBlockScanTime = 0;
        lastPlayerPos = null;
    }

    @Override
    public void onDisable() {
        // Clear cache when disabling
        cachedSeaPickleBlocks.clear();
        lastPlayerPos = null;
    }

    private void renderOceanEsp(WorldRenderContext context) {
        if (!isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Vec3d playerPos = client.player.getPos();
        VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayer.getLines());
        MatrixStack matrixStack = context.matrixStack();

        // Render entities (this is already efficient)
        for (Entity entity : client.world.getEntities()) {
            if (entity.squaredDistanceTo(playerPos) <= renderDistance * renderDistance) {
                float[] color = getEntityColor(entity);
                if (color != null) {
                    Box box = entity.getBoundingBox();
                    Vec3d cameraPos = context.camera().getPos();
                    Box adjustedBox = box.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                    drawBox(matrixStack, vertexConsumer, adjustedBox, color[0], color[1], color[2], 1.0f);
                }
            }
        }

        // Render blocks with optimized scanning
        if (blockSelection.get("Sea Pickles")) {
            updateBlockCache(client, playerPos);
            renderCachedBlocks(context, matrixStack, vertexConsumer, playerPos);
        }
    }

    private void updateBlockCache(MinecraftClient client, Vec3d playerPos) {
        long currentTime = System.currentTimeMillis();
        BlockPos currentPlayerPos = client.player.getBlockPos();

        // Only update cache if enough time has passed OR player moved significantly OR render distance changed
        boolean shouldUpdate = false;

        if (currentTime - lastBlockScanTime > BLOCK_SCAN_INTERVAL) {
            shouldUpdate = true;
        } else if (lastPlayerPos == null ||
                lastPlayerPos.getSquaredDistance(currentPlayerPos) > 16) { // Player moved 4+ blocks
            shouldUpdate = true;
        } else if (Math.abs(lastScanDistance - renderDistance) > 5) { // Render distance changed significantly
            shouldUpdate = true;
        }

        if (!shouldUpdate) return;

        // Clear old blocks that are now out of range
        cachedSeaPickleBlocks.removeIf(pos ->
                playerPos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > renderDistance * renderDistance
        );

        // Scan for new blocks in a more efficient pattern
        scanForSeaPickles(client, currentPlayerPos);

        lastBlockScanTime = currentTime;
        lastPlayerPos = currentPlayerPos;
        lastScanDistance = renderDistance;
    }

    private void scanForSeaPickles(MinecraftClient client, BlockPos playerPos) {
        int range = (int) Math.ceil(renderDistance);

        // Scan all blocks in the radius without limiting per-frame count
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                // Limit Y scanning to reasonable ocean depths
                for (int y = -20; y <= 10; y++) {
                    BlockPos blockPos = playerPos.add(x, y, z);

                    // Skip if already in cache
                    if (cachedSeaPickleBlocks.contains(blockPos)) continue;

                    // Check distance
                    Vec3d playerPosVec = new Vec3d(playerPos.getX(), playerPos.getY(), playerPos.getZ());
                    if (playerPosVec.squaredDistanceTo(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= renderDistance * renderDistance) {
                        BlockState blockState = client.world.getBlockState(blockPos);

                        // Only detect waterlogged sea pickles
                        if (blockState.getBlock() == Blocks.SEA_PICKLE &&
                                blockState.contains(Properties.WATERLOGGED) &&
                                blockState.get(Properties.WATERLOGGED)) {
                            cachedSeaPickleBlocks.add(blockPos.toImmutable());
                        }
                    }
                }
            }
        }
    }

    private void renderCachedBlocks(WorldRenderContext context, MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d playerPos) {
        Vec3d cameraPos = context.camera().getPos();

        for (BlockPos blockPos : cachedSeaPickleBlocks) {
            // Double-check distance (blocks might be cached from when render distance was larger)
            if (playerPos.squaredDistanceTo(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= renderDistance * renderDistance) {
                Box blockBox = new Box(blockPos).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                drawBox(matrixStack, vertexConsumer, blockBox, 0.5f, 1.0f, 0.0f, 1.0f); // Lime green
            }
        }
    }

    private float[] getEntityColor(Entity entity) {
        if (entity instanceof TropicalFishEntity && mobSelection.get("Tropical Fish")) {
            return new float[]{0.0f, 0.4f, 1.0f}; // Royal Blue
        } else if (entity instanceof PufferfishEntity && mobSelection.get("Pufferfish")) {
            return new float[]{0.0f, 0.6f, 0.8f}; // Deep Sky Blue
        } else if (entity instanceof CodEntity && mobSelection.get("Cod")) {
            return new float[]{0.0f, 0.2f, 0.8f}; // Dark Blue
        } else if (entity instanceof SalmonEntity && mobSelection.get("Salmon")) {
            return new float[]{0.3f, 0.7f, 1.0f}; // Light Blue
        } else if (entity instanceof FrogEntity && mobSelection.get("Frogs")) {
            return new float[]{0.0f, 0.8f, 1.0f}; // Aqua
        } else if (entity instanceof DolphinEntity && mobSelection.get("Dolphins")) {
            return new float[]{0.1f, 0.5f, 0.9f}; // Dodger Blue
        } else if (entity instanceof TurtleEntity && mobSelection.get("Sea Turtles")) {
            return new float[]{0.2f, 0.4f, 0.6f}; // Steel Blue
        }
        return null;
    }

    private void drawBox(MatrixStack matrixStack, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Draw 12 edges of the box with normals
        // Bottom face
        vertexConsumer.vertex(matrix, minX, minY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, maxX, minY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, maxX, minY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, minX, minY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, minX, minY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, minX, minY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);

        // Top face
        vertexConsumer.vertex(matrix, minX, maxY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, minX, maxY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);

        // Vertical edges
        vertexConsumer.vertex(matrix, minX, minY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, minX, maxY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, maxX, minY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);

        vertexConsumer.vertex(matrix, minX, minY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 0, 1);
    }

    // Static methods for GUI interaction
    public static boolean isDropdownOpen() {
        return dropdownOpen;
    }

    public static void toggleDropdown() {
        dropdownOpen = !dropdownOpen;
    }

    public static double getRenderDistance() {
        return renderDistance;
    }

    public static void setRenderDistance(double distance) {
        renderDistance = Math.max(0.0, Math.min(distance, 300.0));
    }

    public static Map<String, Boolean> getMobSelection() {
        return mobSelection;
    }

    public static Map<String, Boolean> getBlockSelection() {
        return blockSelection;
    }

    public static void toggleMobSelection(String mobType) {
        mobSelection.put(mobType, !mobSelection.get(mobType));
    }

    public static void toggleBlockSelection(String blockType) {
        blockSelection.put(blockType, !blockSelection.get(blockType));
    }

    public static boolean isMobSelected(String mobType) {
        return mobSelection.getOrDefault(mobType, false);
    }

    public static boolean isBlockSelected(String blockType) {
        return blockSelection.getOrDefault(blockType, false);
    }

    public static boolean isMobsDropdownOpen() {
        return mobsDropdownOpen;
    }

    public static void toggleMobsDropdown() {
        mobsDropdownOpen = !mobsDropdownOpen;
    }

    public static boolean isBlocksDropdownOpen() {
        return blocksDropdownOpen;
    }

    public static void toggleBlocksDropdown() {
        blocksDropdownOpen = !blocksDropdownOpen;
    }
}