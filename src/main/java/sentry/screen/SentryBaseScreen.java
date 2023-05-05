package sentry.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import sentry.Sentry;
import sentry.container.SentryBaseMenu;


@OnlyIn(Dist.CLIENT)
public class SentryBaseScreen extends AbstractContainerScreen<SentryBaseMenu> implements MenuAccess<SentryBaseMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Sentry.MODID, "textures/gui/container/sentry_base.png");

    public SentryBaseScreen(SentryBaseMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 176;
        this.inventoryLabelY = 133 - 94;
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.getXSize()) / 2;
        int j = (this.height - this.getYSize()) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.getXSize(), this.getYSize());
    }
}
