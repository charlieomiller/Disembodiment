package net.charl.disembodiment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.charl.disembodiment.block.entity.DematerializerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DematerializerRenderer implements BlockEntityRenderer<DematerializerBlockEntity> {
    private static final ResourceLocation ORB_TEXTURE =
            new ResourceLocation("disembodiment", "textures/entity/dematerializer_orb.png");

    public DematerializerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(DematerializerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {


        float periodInSecs = 2.0f;
        float yawDegreeSpinPerTick = 4.0f;

        long gameTime = be.getLevel() == null ? 0L : be.getLevel().getGameTime();
        float time = gameTime + partialTick;

        float baseY     = 12f / 16f + 0.15f;
        float bob       = 0.10f * Mth.sin((float)(time * (2 * Math.PI / (periodInSecs * 20.0))));
        float height    = baseY + bob;

        float spinDeg   = (time * yawDegreeSpinPerTick) % 360f;

        // if there is an active player bob and rotate at 2x speed
        if (be.hasAnyActivePlayers()) {
            bob *= 2.0f;
            spinDeg *= 2.0f;
        }

        pose.pushPose();
        pose.translate(0.5, height, 0.5);

        var mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();

        pose.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        pose.mulPose(Axis.YP.rotationDegrees(spinDeg));

        float scale = 0.50f;
        pose.scale(scale, scale, scale);

        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(ORB_TEXTURE));
        int light = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());

        float half = 0.5f;
        addVertex(vc, pose, light, -half, -half, 0, 0f, 0f);
        addVertex(vc, pose, light, half, -half, 0, 1f, 0f);
        addVertex(vc, pose, light, half, half, 0, 1f, 1f);
        addVertex(vc, pose, light, -half, half, 0, 0f, 1f);

        pose.popPose();





    }

    private static void addVertex(VertexConsumer vc, PoseStack pose, int light,
                                  float x, float y, float z, float u, float v) {
        var mat = pose.last().pose();
        var normal = pose.last().normal();
        vc.vertex(mat, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0, 1, 0)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(DematerializerBlockEntity be) {
        return true;
    }
}
