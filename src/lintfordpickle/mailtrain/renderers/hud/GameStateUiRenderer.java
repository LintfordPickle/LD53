package lintfordpickle.mailtrain.renderers.hud;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.GameStateController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.UiWindow;

public class GameStateUiRenderer extends UiWindow implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "GameState UI Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameStateController mGameStateController;
	private SpriteSheetDefinition mHudSpritesheet;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return false;
	}

	float mLeftMouseCooldownTimer;

	public boolean isCoolDownElapsed() {
		return mLeftMouseCooldownTimer <= 0.f;
	}

	public void resetCoolDownTimer() {
		mLeftMouseCooldownTimer = 200.f;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameStateUiRenderer(RendererManager pRendererManager, int pEntityGroupUid) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupUid);

		mIsOpen = true;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		super.initialize(pCore);

		mGameStateController = (GameStateController) pCore.controllerManager().getControllerByNameRequired(GameStateController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mHudSpritesheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_HUD", ConstantsGame.GAME_RESOURCE_GROUP_ID);
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mGameStateController.isInitialized())
			return;

		// Don't draw UI when game over
		if (mGameStateController.getHasLost() || mGameStateController.getHasWon())
			return;

		final var lHudRect = pCore.HUD().boundingRectangle();

		final var lSpriteBatch = mRendererManager.uiSpriteBatch();
		{ // Deco
			lSpriteBatch.begin(pCore.HUD());
			final var lTrainFrontSprite = mHudSpritesheet.getSpriteFrame("TEXTURETRAINFRONT");

			// TODO : this IS null
			if (lTrainFrontSprite == null) {
				lSpriteBatch.end();
				return;
			}

			final float lScale = 2.f;
			final float lWidth = lTrainFrontSprite.width() * lScale;

			lSpriteBatch.draw(mHudSpritesheet, lTrainFrontSprite, -100.f - lWidth * .5f, lHudRect.top() + 15f, lTrainFrontSprite.width() * 2.f, lTrainFrontSprite.height() * 2.f, -0.1f, ColorConstants.WHITE);

			final var lTrainBackSprite = mHudSpritesheet.getSpriteFrame("TEXTURETRAINBACK");
			lSpriteBatch.draw(mHudSpritesheet, lTrainBackSprite, +100.f - lWidth * .5f, lHudRect.top() + 15f, lTrainBackSprite.width() * 2.f, lTrainBackSprite.height() * 2.f, -0.1f, ColorConstants.WHITE);
			lSpriteBatch.end();
		}
	}

}
