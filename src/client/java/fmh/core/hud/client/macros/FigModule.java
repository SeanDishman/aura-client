package fmh.core.hud.client.macros;

import fmh.core.hud.client.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class FigModule extends Module {
    private boolean hasWarped = false;
    private boolean hasCheckedPosition = false;
    private boolean hasFoundAxe = false;
    private boolean isRotating = false;
    private boolean isWalking = false;
    private boolean hasReachedFirstTarget = false;
    private boolean hasReachedSecondTarget = false;
    private boolean hasReachedThirdTarget = false;
    private boolean hasReachedFourthTarget = false;
    private boolean hasReachedFifthTarget = false;
    private boolean hasReachedSixthTarget = false;
    private boolean hasReachedSeventhTarget = false;
    private int warpDelay = 0;
    private int axeSearchDelay = 0;
    private float currentYaw = 0;
    private float currentPitch = 0;
    private float targetYaw = 0;
    private float targetPitch = 0;
    private float rotationSpeed = 0.8f;
    private int rotationTicks = 0;
    private Vec3d lastPosition = null;
    private int stuckTicks = 0;
    private Random random = new Random();

    private static final int TARGET_X = -547;
    private static final int TARGET_Z = -22;
    private static final double FIRST_TARGET_X = -570.0;
    private static final double FIRST_TARGET_Z = -18.0;
    private static final double SECOND_TARGET_X = -580.0;
    private static final double SECOND_TARGET_Z = -18.0;
    private static final double THIRD_TARGET_X = -587.0;
    private static final double THIRD_TARGET_Z = -6.0;
    private static final double FOURTH_TARGET_X = -586.0;
    private static final double FOURTH_TARGET_Z = 18.0;
    private static final double FIFTH_TARGET_X = -580.0;
    private static final double FIFTH_TARGET_Z = 22.0;
    private static final double SIXTH_TARGET_X = -579.0;
    private static final double SIXTH_TARGET_Z = 45.0;
    private static final double SEVENTH_TARGET_X = -557.0;
    private static final double SEVENTH_TARGET_Z = 45.0;

    public FigModule() {
        super("Fig", "Macros");
        setEnabled(false);
    }

    private void calculateTargetRotation(MinecraftClient client) {
        Vec3d playerPos = client.player.getPos();
        double targetX, targetZ;

        if (!hasReachedFirstTarget) {
            targetX = FIRST_TARGET_X;
            targetZ = FIRST_TARGET_Z;
        } else if (!hasReachedSecondTarget) {
            targetX = SECOND_TARGET_X;
            targetZ = SECOND_TARGET_Z;
        } else if (!hasReachedThirdTarget) {
            targetX = THIRD_TARGET_X;
            targetZ = THIRD_TARGET_Z;
        } else if (!hasReachedFourthTarget) {
            targetX = FOURTH_TARGET_X;
            targetZ = FOURTH_TARGET_Z;
        } else if (!hasReachedFifthTarget) {
            targetX = FIFTH_TARGET_X;
            targetZ = FIFTH_TARGET_Z;
        } else if (!hasReachedSixthTarget) {
            targetX = SIXTH_TARGET_X;
            targetZ = SIXTH_TARGET_Z;
        } else {
            targetX = SEVENTH_TARGET_X;
            targetZ = SEVENTH_TARGET_Z;
        }

        double deltaX = targetX - playerPos.x;
        double deltaZ = targetZ - playerPos.z;

        targetYaw = (float) Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        targetPitch = 0.0f;

        currentYaw = client.player.getYaw();
        currentPitch = client.player.getPitch();
        rotationTicks = 0;

        float totalDistance = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw)) + Math.abs(targetPitch - currentPitch);

        if (totalDistance > 120f) {
            rotationSpeed = 2.8f;
        } else if (totalDistance > 80f) {
            rotationSpeed = 2.2f;
        } else if (totalDistance > 40f) {
            rotationSpeed = 1.6f;
        } else {
            rotationSpeed = 1.2f;
        }

        isRotating = true;

        String targetName = !hasReachedFirstTarget ? "first target" :
                (!hasReachedSecondTarget ? "second target" :
                        (!hasReachedThirdTarget ? "third target" :
                                (!hasReachedFourthTarget ? "fourth target" :
                                        (!hasReachedFifthTarget ? "fifth target" :
                                                (!hasReachedSixthTarget ? "sixth target" : "seventh target")))));
        client.player.sendMessage(
                Text.literal("Rotating towards " + targetName + " coordinates...")
                        .formatted(Formatting.YELLOW),
                false
        );
    }

    private void smoothRotateToTarget(MinecraftClient client) {
        rotationTicks++;

        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float yawNoise = (random.nextFloat() - 0.5f) * 0.002f;
        float pitchNoise = (random.nextFloat() - 0.5f) * 0.002f;

        if (rotationTicks % 8 == 0) {
            float totalDistanceRemaining = Math.abs(yawDiff) + Math.abs(pitchDiff);
            float baseSpeed;

            if (totalDistanceRemaining > 100f) {
                baseSpeed = 2.4f;
            } else if (totalDistanceRemaining > 60f) {
                baseSpeed = 1.8f;
            } else if (totalDistanceRemaining > 30f) {
                baseSpeed = 1.4f;
            } else {
                baseSpeed = 1.2f;
            }

            float variation = baseSpeed * 0.02f;
            rotationSpeed = baseSpeed + (random.nextFloat() - 0.5f) * variation;
        }

        if (Math.abs(yawDiff) < 5.0f && Math.abs(pitchDiff) < 5.0f) {
            currentYaw = targetYaw;
            currentPitch = targetPitch;
            isRotating = false;
            isWalking = true;
            client.player.sendMessage(
                    Text.literal("Rotation complete! Starting to walk to target...")
                            .formatted(Formatting.GREEN),
                    false
            );
        } else {
            float effectiveYawSpeed = rotationSpeed;
            float effectivePitchSpeed = rotationSpeed;

            float yawDistance = Math.abs(yawDiff);
            float pitchDistance = Math.abs(pitchDiff);

            if (yawDistance < 25.0f) {
                effectiveYawSpeed *= (0.25f + (yawDistance / 25.0f) * 0.75f);
            }
            if (pitchDistance < 25.0f) {
                effectivePitchSpeed *= (0.25f + (pitchDistance / 25.0f) * 0.75f);
            }

            float speedMultiplier;
            float totalDistanceRemaining = yawDistance + pitchDistance;
            if (totalDistanceRemaining > 80f) {
                speedMultiplier = 0.30f;
            } else if (totalDistanceRemaining > 40f) {
                speedMultiplier = 0.24f;
            } else {
                speedMultiplier = 0.20f;
            }

            currentYaw += (yawDiff * effectiveYawSpeed * speedMultiplier) + yawNoise;
            currentPitch += (pitchDiff * effectivePitchSpeed * speedMultiplier) + pitchNoise;
        }

        client.player.setYaw(currentYaw);
        client.player.setPitch(MathHelper.clamp(currentPitch, -90.0f, 90.0f));
    }

    private void walkToTarget(MinecraftClient client) {
        Vec3d playerPos = client.player.getPos();
        double targetX, targetZ;
        String targetName;

        if (!hasReachedFirstTarget) {
            targetX = FIRST_TARGET_X;
            targetZ = FIRST_TARGET_Z;
            targetName = "first target";
        } else if (!hasReachedSecondTarget) {
            targetX = SECOND_TARGET_X;
            targetZ = SECOND_TARGET_Z;
            targetName = "second target";
        } else if (!hasReachedThirdTarget) {
            targetX = THIRD_TARGET_X;
            targetZ = THIRD_TARGET_Z;
            targetName = "third target";
        } else if (!hasReachedFourthTarget) {
            targetX = FOURTH_TARGET_X;
            targetZ = FOURTH_TARGET_Z;
            targetName = "fourth target";
        } else if (!hasReachedFifthTarget) {
            targetX = FIFTH_TARGET_X;
            targetZ = FIFTH_TARGET_Z;
            targetName = "fifth target";
        } else if (!hasReachedSixthTarget) {
            targetX = SIXTH_TARGET_X;
            targetZ = SIXTH_TARGET_Z;
            targetName = "sixth target";
        } else {
            targetX = SEVENTH_TARGET_X;
            targetZ = SEVENTH_TARGET_Z;
            targetName = "seventh target";
        }

        double deltaX = targetX - playerPos.x;
        double deltaZ = targetZ - playerPos.z;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance < 1.0) {
            if (!hasReachedFirstTarget) {
                hasReachedFirstTarget = true;
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.player.sendMessage(
                        Text.literal("Reached first target! Stopping and rotating to second target...")
                                .formatted(Formatting.GREEN),
                        false
                );
                isWalking = false;
                calculateTargetRotation(client);
                return;
            } else if (!hasReachedSecondTarget) {
                hasReachedSecondTarget = true;
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.player.sendMessage(
                        Text.literal("Reached second target! Stopping and rotating to third target...")
                                .formatted(Formatting.GREEN),
                        false
                );
                isWalking = false;
                calculateTargetRotation(client);
                return;
            } else if (!hasReachedThirdTarget) {
                hasReachedThirdTarget = true;
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.player.sendMessage(
                        Text.literal("Reached third target! Stopping and rotating to fourth target...")
                                .formatted(Formatting.GREEN),
                        false
                );
                isWalking = false;
                calculateTargetRotation(client);
                return;
            } else if (!hasReachedFourthTarget) {
                hasReachedFourthTarget = true;
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.player.sendMessage(
                        Text.literal("Reached fourth target! Stopping and rotating to fifth target...")
                                .formatted(Formatting.GREEN),
                        false
                );
                isWalking = false;
                calculateTargetRotation(client);
                return;
            } else if (!hasReachedFifthTarget) {
                hasReachedFifthTarget = true;
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.player.sendMessage(
                        Text.literal("Reached fifth target! Stopping and rotating to sixth target...")
                                .formatted(Formatting.GREEN),
                        false
                );
                isWalking = false;
                calculateTargetRotation(client);
                return;
            } else if (!hasReachedSixthTarget) {
                hasReachedSixthTarget = true;
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.player.sendMessage(
                        Text.literal("Reached sixth target! Stopping and rotating to seventh target...")
                                .formatted(Formatting.GREEN),
                        false
                );
                isWalking = false;
                calculateTargetRotation(client);
                return;
            } else {
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                isWalking = false;
                hasReachedSeventhTarget = true;
                client.player.sendMessage(
                        Text.literal("Aura client: Pathfinding to trees is complete starting Fig Macro")
                                .formatted(Formatting.GREEN),
                        false
                );
                setEnabled(false);
                return;
            }
        }

        float targetWalkYaw = (float) Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        float currentPlayerYaw = client.player.getYaw();
        float yawDiff = MathHelper.wrapDegrees(targetWalkYaw - currentPlayerYaw);

        if (Math.abs(yawDiff) > 5.0f) {
            float adjustedYaw = currentPlayerYaw + (yawDiff * 0.1f);
            client.player.setYaw(adjustedYaw);
        }

        if (lastPosition == null) {
            lastPosition = playerPos;
            stuckTicks = 0;
        } else {
            double movementDistance = lastPosition.distanceTo(playerPos);

            if (movementDistance < 0.05) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
                lastPosition = playerPos;
            }

            if (stuckTicks > 10 && client.player.isOnGround()) {
                client.options.jumpKey.setPressed(true);
                if (stuckTicks % 20 == 0) {
                    client.player.sendMessage(
                            Text.literal("Player stuck, jumping...")
                                    .formatted(Formatting.AQUA),
                            false
                    );
                }
            } else {
                client.options.jumpKey.setPressed(false);
            }
        }

        client.options.forwardKey.setPressed(true);

        if (rotationTicks % 20 == 0) {
            client.player.sendMessage(
                    Text.literal("Walking to " + targetName + "... Distance: " + String.format("%.1f", distance))
                            .formatted(Formatting.YELLOW),
                    false
            );
        }
        rotationTicks++;
    }

    @Override
    public void onEnable() {
        hasWarped = false;
        hasCheckedPosition = false;
        hasFoundAxe = false;
        isRotating = false;
        isWalking = false;
        hasReachedFirstTarget = false;
        hasReachedSecondTarget = false;
        hasReachedThirdTarget = false;
        hasReachedFourthTarget = false;
        hasReachedFifthTarget = false;
        hasReachedSixthTarget = false;
        hasReachedSeventhTarget = false;
        warpDelay = 0;
        axeSearchDelay = 0;
        rotationTicks = 0;
        lastPosition = null;
        stuckTicks = 0;
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.options.forwardKey.setPressed(false);
            client.options.jumpKey.setPressed(false);
        }
        hasWarped = false;
        hasCheckedPosition = false;
        hasFoundAxe = false;
        isRotating = false;
        isWalking = false;
        hasReachedFirstTarget = false;
        hasReachedSecondTarget = false;
        hasReachedThirdTarget = false;
        hasReachedFourthTarget = false;
        hasReachedFifthTarget = false;
        hasReachedSixthTarget = false;
        hasReachedSeventhTarget = false;
        warpDelay = 0;
        axeSearchDelay = 0;
        rotationTicks = 0;
        lastPosition = null;
        stuckTicks = 0;
    }

    public void tick() {
        if (!isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (!hasWarped) {
            client.player.networkHandler.sendChatCommand("warp galatea");
            client.player.sendMessage(
                    Text.literal("Aura client: Warping to galatea...")
                            .formatted(Formatting.YELLOW),
                    false
            );
            hasWarped = true;
            warpDelay = 100;
        }

        if (hasWarped && !hasCheckedPosition) {
            if (warpDelay > 0) {
                warpDelay--;
                if (warpDelay % 20 == 0) {
                    client.player.sendMessage(
                            Text.literal("Waiting for warp... " + (warpDelay/20) + "s")
                                    .formatted(Formatting.YELLOW),
                            false
                    );
                }
                return;
            }

            client.player.sendMessage(
                    Text.literal("Checking position...")
                            .formatted(Formatting.YELLOW),
                    false
            );

            BlockPos playerPos = client.player.getBlockPos();
            int playerX = playerPos.getX();
            int playerZ = playerPos.getZ();

            if (playerX >= -550 && playerX <= -543 && playerZ >= -25 && playerZ <= -19) {
                client.player.sendMessage(
                        Text.literal("Aura client: Starting Fig Macro")
                                .formatted(Formatting.GREEN),
                        false
                );
                axeSearchDelay = 20 + random.nextInt(21);
            } else {
                client.player.sendMessage(
                        Text.literal("Aura client: Invalid Starting Position")
                                .formatted(Formatting.RED),
                        false
                );
                setEnabled(false);
            }

            hasCheckedPosition = true;
        }

        if (hasCheckedPosition && !hasFoundAxe && axeSearchDelay > 0) {
            axeSearchDelay--;
            if (axeSearchDelay == 0) {
                client.player.sendMessage(
                        Text.literal("Searching for axe...")
                                .formatted(Formatting.YELLOW),
                        false
                );
                searchForAxe(client);
            }
        }

        if (hasFoundAxe && !isRotating && !isWalking && !hasReachedFirstTarget && !hasReachedSecondTarget && !hasReachedThirdTarget && !hasReachedFourthTarget && !hasReachedFifthTarget && !hasReachedSixthTarget && !hasReachedSeventhTarget) {
            calculateTargetRotation(client);
        }

        if (isRotating) {
            smoothRotateToTarget(client);
        }

        if (isWalking) {
            walkToTarget(client);
        }
    }

    private void searchForAxe(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);

            if (!stack.isEmpty() && stack.getItem() instanceof AxeItem) {
                if (stack.getItem() == Items.GOLDEN_AXE || stack.getItem() == Items.STONE_AXE) {
                    client.player.getInventory().setSelectedSlot(i);
                    client.player.sendMessage(
                            Text.literal("Found and equipped " + stack.getItem().toString())
                                    .formatted(Formatting.GREEN),
                            false
                    );
                    hasFoundAxe = true;
                    return;
                }
            }
        }

        client.player.sendMessage(
                Text.literal("Aura client: Cannot Find Axe Aborting Script")
                        .formatted(Formatting.RED),
                false
        );
        setEnabled(false);
    }
}