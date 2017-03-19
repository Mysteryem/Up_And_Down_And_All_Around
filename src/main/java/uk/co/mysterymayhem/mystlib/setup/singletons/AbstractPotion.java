package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Mysteryem on 15/03/2017.
 */
public abstract class AbstractPotion<POTION extends AbstractPotion<POTION>> extends Potion implements IModPotion<POTION> {
    private int iconIndex;

    public AbstractPotion(boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
    }

    public AbstractPotion(boolean isBadEffectIn, int liquidColorIn, int iconIndexX, int iconIndexY) {
        this(isBadEffectIn, liquidColorIn);
        this.setIconIndex(iconIndexX, iconIndexY);
    }

    public AbstractPotion(boolean isBadEffectIn, int liquidColorIn, int iconIndex) {
        this(isBadEffectIn, liquidColorIn);
        this.setIconIndex(iconIndex);
    }

    @Override
    public POTION setIconIndex(int p_76399_1_, int p_76399_2_) {
        this.iconIndex = p_76399_1_ + p_76399_2_ * 8;
        return this.getPotion();
    }

    @Override
    public int getStatusIconIndex() {
        return this.iconIndex;
    }

    @Override
    public POTION setPotionName(String nameIn) {
        super.setPotionName(nameIn);
        return this.getPotion();
    }

    @Override
    public POTION registerPotionAttributeModifier(IAttribute attribute, String uniqueId, double ammount, int operation) {
        super.registerPotionAttributeModifier(attribute, uniqueId, ammount, operation);
        return this.getPotion();
    }

    @Override
    public POTION setBeneficial() {
        super.setBeneficial();
        return this.getPotion();
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return this.getTextureResource() != null;
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return super.shouldRenderInvText(effect);
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        return this.getTextureResource() != null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        this.render(x + 6, y + 7, 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        this.render(x + 3, y + 3, alpha);
    }
}
