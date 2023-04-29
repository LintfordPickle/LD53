package lintfordpickle.mailtrain.screens.menu;

import org.lwjgl.opengl.GL11;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;

public class HelpScreen extends MenuScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Texture mHelpTexture;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public HelpScreen(ScreenManager screenManager) {
		super(screenManager, "");

		mShowContextualFooterBar = false;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mHelpTexture = resourceManager.textureManager().loadTexture("TEXTURE_MENU_HELP", "res/textures/textureHelpOverlay.png", entityGroupUid());
	}

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);

		GL11.glClearColor(0.06f, 0.18f, 0.11f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		final var lHudBoundingBox = core.HUD().boundingRectangle();
		final var lTextureBatch = rendererManager().uiSpriteBatch();

		lTextureBatch.begin(core.HUD());
		lTextureBatch.draw(mHelpTexture, 0, 0, 960, 540, lHudBoundingBox.left(), lHudBoundingBox.top(), lHudBoundingBox.width(), lHudBoundingBox.height(), -0.01f, ColorConstants.WHITE);
		lTextureBatch.end();
	}

	@Override
	protected void handleOnClick() {
		// TODO Auto-generated method stub

	}
}
