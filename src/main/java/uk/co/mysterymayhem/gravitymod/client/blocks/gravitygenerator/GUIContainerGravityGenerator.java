package uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.ContainerGravityGenerator;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.MessageGravityGenerator;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.TileGravityGenerator;
import uk.co.mysterymayhem.gravitymod.common.packets.PacketHandler;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Mysteryem on 2017-01-08.
 */
@SideOnly(Side.CLIENT)
public class GUIContainerGravityGenerator extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(GravityMod.MOD_ID, "textures/gui/container/gravitygenerator.png");
    private static final int TEXT_LEFT_START = 8;
    private static final String directionLangPrefix = "mysttmtgravitymod.";
    private final int buttonLeftStart;
    private final ContainerGravityGenerator containerGravityGenerator;
    private final ArrayList<GuiLabel> labels = new ArrayList<>();
    private final int startingTileXWidth;
    private final int startingTileYHeight;
    private final int startingTileZWidth;
    private final int valueLeftStart;
    private int tileXWidth;
    private int tileYHeight;
    private int tileZWidth;

    public GUIContainerGravityGenerator(ContainerGravityGenerator inventorySlotsIn) {
        super(inventorySlotsIn);
        this.containerGravityGenerator = inventorySlotsIn;
        TileGravityGenerator tileGravityGenerator = this.containerGravityGenerator.getTileGravityGenerator();
        this.tileYHeight = tileGravityGenerator.getRelativeYHeight();
        this.tileXWidth = tileGravityGenerator.getRelativeXRadius() * 2 + 1;
        this.tileZWidth = tileGravityGenerator.getRelativeZRadius() * 2 + 1;
        this.startingTileYHeight = this.tileYHeight;
        this.startingTileXWidth = this.tileXWidth;
        this.startingTileZWidth = this.tileZWidth;

        this.ySize = 143;
        this.xSize = 176;

        EnumGravityDirection[] positiveDirections = {EnumGravityDirection.UP, EnumGravityDirection.EAST, EnumGravityDirection.SOUTH};
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        int directionTextMaxLength = 0;
        for (EnumGravityDirection direction : positiveDirections) {
            int stringWidth = fontRenderer.getStringWidth(
                    I18n.format(
                            "gui.gravitygenerator.widthtext",
                            I18n.format(directionLangPrefix + direction.getName()), I18n.format(directionLangPrefix + direction.getOpposite().getName())
                    )
            );
            if (stringWidth > directionTextMaxLength) {
                directionTextMaxLength = stringWidth;
            }
        }
        int relativeExtraTextMaxLength = Math.max(
                Math.max(fontRenderer.getStringWidth(" (X)"), fontRenderer.getStringWidth(" (Y)")),
                fontRenderer.getStringWidth(" (Z)"));

        int digitMaxLength = 0;
        for (int i = 0; i < 10; i++) {
            int digitWidth = fontRenderer.getStringWidth(String.valueOf(i));
            if (digitWidth > digitMaxLength) {
                digitMaxLength = digitWidth;
            }
        }

        int maxDisplayedValue = Math.max(TileGravityGenerator.MAX_HEIGHT, 2 * TileGravityGenerator.MAX_RADIUS + 1);
        int maxDigits = Integer.toString(maxDisplayedValue).length();
        int maxValueLength = digitMaxLength * maxDigits;

        final int textValueSpacer = 2;
        final int valueButtonSpacer = 2;

        this.valueLeftStart = TEXT_LEFT_START + directionTextMaxLength + relativeExtraTextMaxLength + textValueSpacer;
        this.buttonLeftStart = this.valueLeftStart + maxValueLength + valueButtonSpacer;
    }

    @Override
    public void initGui() {
        super.initGui();

//        labels.clear();
//
        FontRenderer fontRenderer = this.fontRendererObj;
        int buttonTopStart = fontRenderer.FONT_HEIGHT + 10;

        int buttonHeight = fontRenderer.FONT_HEIGHT;
        int firstButtonWidth = fontRenderer.getCharWidth('+') + 4;
        int secondButtonWidth = fontRenderer.getCharWidth('-') + 4;
        for (int i = 0; i < 6; i++) {
            boolean isFirstButton = i % 2 == 0;
            String text = isFirstButton ? "+" : "-";
            int buttonWidth = isFirstButton ? firstButtonWidth : secondButtonWidth;
            this.addButton(new GuiButton(i, this.guiLeft + this.buttonLeftStart + (i % 2) * (firstButtonWidth + 2), this.guiTop + buttonTopStart + (i / 2) * (buttonHeight + 4), buttonWidth, buttonHeight, text));
        }
//        GuiButton textButton = new GuiButton(0, this.guiLeft + buttonLeftStart, this.guiTop + 35, 20, 18, "+");
//
//        EnumGravityTier gravityTier = this.containerGravityGenerator.getTileGravityGenerator().getGravityTier();
//        String generatorLangKey;
//        switch (gravityTier) {
//            case WEAK:
//                generatorLangKey = "tile.gravitygenerator.weak.name";
//                break;
//            case NORMAL:
//                generatorLangKey = "tile.gravitygenerator.normal.name";
//                break;
//            default://case STRONG:
//                generatorLangKey = "tile.gravitygenerator.strong.name";
//                break;
//        }
//
//        int textLeft = this.guiLeft + 8;
//        int textTop = this.guiTop + 8;
//
//        String text = I18n.format(generatorLangKey);
//        int textWidth = fontRenderer.getStringWidth(text);
////        GuiLabel titleLabel = new GuiLabel(fontRenderer, 0, this.guiLeft + (this.xSize/2 - textWidth/2), textTop, textWidth, fontRenderer.FONT_HEIGHT, -1);
////        titleLabel.addLine(text);
////        titleLabel.setCentered();
//
//        text = "Relative X (#/#)";
//        textWidth = fontRenderer.getStringWidth(text);
//        textTop+=fontRenderer.FONT_HEIGHT + 8;
//        GuiLabel relativeXRadiusLabel = new GuiLabel(fontRenderer, 1, textLeft, textTop, textWidth, fontRenderer.FONT_HEIGHT, 0xF0F0F0);
//        relativeXRadiusLabel.addLine(text);
//
//        text = "Relative Z (#/#)";
//        textWidth = fontRenderer.getStringWidth(text);
//        textTop+=fontRenderer.FONT_HEIGHT + 4;
//        GuiLabel relativeZRadiusLabel = new GuiLabel(fontRenderer, 2, textLeft, textTop, textWidth, fontRenderer.FONT_HEIGHT, 0xF0F0F0);
//        relativeZRadiusLabel.addLine(text);
//
//        text = "Relative Y (#/#)";
//        textWidth = fontRenderer.getStringWidth(text);
//        textTop+=fontRenderer.FONT_HEIGHT + 4;
//        GuiLabel relativeYRadiusLabel = new GuiLabel(fontRenderer, 3, textLeft, textTop, textWidth, fontRenderer.FONT_HEIGHT, 0xF0F0F0);
//        relativeYRadiusLabel.addLine(text);

//        this.addButton(textButton);
//        this.labels.add(titleLabel);
//        this.labels.add(relativeXRadiusLabel);
//        this.labels.add(relativeZRadiusLabel);
//        this.labels.add(relativeYRadiusLabel);
//        this.addButton(new GuiButton(0, this.guiLeft, this.guiTop, 34, 22, "long text"));
    }

    @Override
    public void onGuiClosed() {
        boolean xChanged = this.startingTileXWidth != this.tileXWidth;
        boolean yChanged = this.startingTileYHeight != this.tileYHeight;
        boolean zChanged = this.startingTileZWidth != this.tileZWidth;
        if (xChanged || yChanged || zChanged) {
            PacketHandler.INSTANCE.sendToServer(new MessageGravityGenerator(this.containerGravityGenerator.getTileGravityGenerator(), TileGravityGenerator.widthToRadius(this.tileXWidth), this.tileYHeight, TileGravityGenerator.widthToRadius(this.tileZWidth), xChanged, yChanged, zChanged));
        }
        super.onGuiClosed();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.tileYHeight = TileGravityGenerator.clampHeight(this.tileYHeight + 1);
                break;
            case 1:
                this.tileYHeight = TileGravityGenerator.clampHeight(this.tileYHeight - 1);
                break;
            case 2:
                this.tileXWidth = TileGravityGenerator.clampWidth(this.tileXWidth + 2);
                break;
            case 3:
                this.tileXWidth = TileGravityGenerator.clampWidth(this.tileXWidth - 2);
                break;
            case 4:
                this.tileZWidth = TileGravityGenerator.clampWidth(this.tileZWidth + 2);
                break;
            case 5:
                this.tileZWidth = TileGravityGenerator.clampWidth(this.tileZWidth - 2);
                break;
            default:
                break;
        }
    }

//    @Override
//    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//        super.drawScreen(mouseX, mouseY, partialTicks);
//
//    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        Minecraft mc = Minecraft.getMinecraft();
        for (GuiLabel gui : this.labels) {
            gui.drawLabel(mc, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        TileGravityGenerator tileGravityGenerator = this.containerGravityGenerator.getTileGravityGenerator();
        EnumGravityTier gravityTier = tileGravityGenerator.getGravityTier();
        String generatorLangKey;
        switch (gravityTier) {
            case WEAK:
                generatorLangKey = "tile.gravitygenerator.weak.name";
                break;
            case NORMAL:
                generatorLangKey = "tile.gravitygenerator.normal.name";
                break;
            default://case STRONG:
                generatorLangKey = "tile.gravitygenerator.strong.name";
                break;
        }

        int textLeft = TEXT_LEFT_START;
        int textTop = 6;

        FontRenderer fontRenderer = this.fontRendererObj;
        int fontHeight = fontRenderer.FONT_HEIGHT;

        final int lengthsTextTop = 10 + fontHeight;

        String text = I18n.format(generatorLangKey);
        int textWidth = fontRenderer.getStringWidth(text);
        int colour = 0x404040;
        fontRenderer.drawString(text, this.xSize / 2 - textWidth / 2, textTop, colour);

        EnumFacing facing = tileGravityGenerator.getFacing();
        EnumGravityDirection direction = EnumGravityDirection.fromEnumFacing(facing.getOpposite());

        String directionLangPrefix = "mysttmtgravitymod.";

        EnumGravityDirection positive = direction.getRelativePositiveY();
        text = I18n.format("gui.gravitygenerator.heighttext", I18n.format(directionLangPrefix + positive.getName())) + " (Y)";
        int maxTextWidth = this.fontRendererObj.getStringWidth(text);
        textTop = lengthsTextTop;
        fontRenderer.drawString(text, textLeft, textTop, colour);

        positive = direction.getRelativePositiveX();
        EnumGravityDirection negative = positive.getOpposite();
        text = I18n.format("gui.gravitygenerator.widthtext", I18n.format(directionLangPrefix + positive.getName()), I18n.format(directionLangPrefix + negative.getName())) + " (X)";
        maxTextWidth = Math.max(maxTextWidth, this.fontRendererObj.getStringWidth(text));
        textTop += fontHeight + 4;
        fontRenderer.drawString(text, textLeft, textTop, colour);

        positive = direction.getRelativePositiveZ();
        negative = positive.getOpposite();
        text = I18n.format("gui.gravitygenerator.widthtext", I18n.format(directionLangPrefix + positive.getName()), I18n.format(directionLangPrefix + negative.getName())) + " (Z)";
        maxTextWidth = Math.max(maxTextWidth, this.fontRendererObj.getStringWidth(text));
        textTop += fontHeight + 4;
        fontRenderer.drawString(text, textLeft, textTop, colour);

        colour = 0xFFFFFF;
        textTop = lengthsTextTop;
        textLeft = this.valueLeftStart;
        text = "" + this.tileYHeight;
        int valueTextMaxWidth = fontRenderer.getStringWidth(text);
        fontRenderer.drawStringWithShadow(text, textLeft, textTop, colour);

        textTop += fontHeight + 4;
        text = "" + this.tileXWidth;
        valueTextMaxWidth = Math.max(valueTextMaxWidth, fontRenderer.getStringWidth(text));
        fontRenderer.drawStringWithShadow(text, textLeft, textTop, colour);

        textTop += fontHeight + 4;
        text = "" + this.tileZWidth;
        valueTextMaxWidth = Math.max(valueTextMaxWidth, fontRenderer.getStringWidth(text));
        fontRenderer.drawStringWithShadow(text, textLeft, textTop, colour);

//        this.fontRendererObj.drawString("Some text", 0, 14, 0x303030);
    }
}
