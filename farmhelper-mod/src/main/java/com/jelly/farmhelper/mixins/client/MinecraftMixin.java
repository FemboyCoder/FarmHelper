package com.jelly.farmhelper.mixins.client;

import com.jelly.farmhelper.FarmHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    //R.I.P. Fastbreak 2022-2022
    // GUESS WHO IS BACK!

    @Shadow
    public GuiScreen currentScreen;

    @Shadow
    public GameSettings gameSettings;

    @Shadow
    public boolean inGameHasFocus;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    private Entity renderViewEntity;

    @Shadow
    public PlayerControllerMP playerController;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public EntityPlayerSP thePlayer;

    @Inject(method = "sendClickBlockToController", at = @At("RETURN"))
    private void sendClickBlockToController(CallbackInfo ci) {
        if (!FarmHelper.config.fastBreak) return;

        boolean shouldClick = this.currentScreen == null && this.gameSettings.keyBindAttack.isKeyDown() && this.inGameHasFocus;
        if (this.objectMouseOver != null && shouldClick)
            for (int i = 0; i < FarmHelper.config.fastBreakSpeed + 1; i++) {
                BlockPos clickedBlock = this.objectMouseOver.getBlockPos();
                this.objectMouseOver = this.renderViewEntity.rayTrace(this.playerController.getBlockReachDistance(), 1.0F);

                if (this.objectMouseOver == null ||
                    this.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
//                    this.theWorld.getBlockState(clickedBlock).getBlock().getMaterial() == Material.air
                ) break;

                BlockPos newBlock = this.objectMouseOver.getBlockPos();
                if (!newBlock.equals(clickedBlock) && this.theWorld.getBlockState(newBlock).getBlock() != Blocks.air) {
                    this.playerController.clickBlock(newBlock, this.objectMouseOver.sideHit);
                }
                if (i % 3 == 0) this.thePlayer.swingItem();
            }
    }
}

