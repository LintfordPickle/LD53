package lintfordpickle.mailtrain.renderers.hud;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.trains.PlayerTrainController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.UiWindow;

public class TrainHudRenderer extends UiWindow {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Train Hud Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private PlayerTrainController mPlayerTrainController;

	private SpriteSheetDefinition mHudSpriteSheet;
	private SpriteSheetDefinition mTrainSpriteSheet;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 11;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainHudRenderer(RendererManager pRendererManager, int pEntityGroupUid) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupUid);

		mIsOpen = true;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		super.initialize(pCore);
		final var lControllerManager = pCore.controllerManager();

		mPlayerTrainController = (PlayerTrainController) lControllerManager.getControllerByNameRequired(PlayerTrainController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mHudSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_HUD", ConstantsGame.GAME_RESOURCE_GROUP_ID);
		mTrainSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_TRAINS", ConstantsGame.GAME_RESOURCE_GROUP_ID);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		final var lMouseHudPositionX = pCore.HUD().getMouseWorldSpaceX();
		final var lMouseHudPositionY = pCore.HUD().getMouseWorldSpaceY();
		if (mWindowArea.intersectsAA(lMouseHudPositionX, lMouseHudPositionY)) {
			if (pCore.input().mouse().isMouseLeftButtonDownTimed(this)) {
				handleClickOnHud(pCore, lMouseHudPositionX, lMouseHudPositionY);
				return true;

			}
		}
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		if (mMouseClickTimer >= 0) {
			mMouseClickTimer -= pCore.appTime().elapsedTimeMilli();

		}
		updateWindowPosition(pCore);
	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		final var lHudRect = pCore.HUD().boundingRectangle();

		final var lTextureBatch = mRendererManager.uiSpriteBatch();

		final var lWorldTexture = mHudSpriteSheet.texture();

		final var lHitchOpenRect = mHudSpriteSheet.getSpriteFrame("TEXTUREHITCHOPEN");
		final var lHitchClosedRect = mHudSpriteSheet.getSpriteFrame("TEXTUREHITCHCLOSED");
		final var lTrainCarRect = mHudSpriteSheet.getSpriteFrame("TEXTURETRAINCARHUD");

		float lTrainHudPositionX = mWindowArea.left() + 15f;
		float lTrainHudPositionY = mWindowArea.top() + 8f;

		lTextureBatch.begin(pCore.HUD());

		lTextureBatch.draw(lWorldTexture, 0, 0, 1, 1, lHudRect.left(), lHudRect.bottom() - 64f - 16f, lHudRect.width(), 64 + 16f, -0.1f, ColorConstants.WHITE);

		final var lPlayerLoc = mPlayerTrainController.playerLocomotive();

		final var lNumCarsInMainTrain = lPlayerLoc.getNumberOfCarsInTrain();
		for (int i = 0; i < lNumCarsInMainTrain; i++) {
			final var lTrainCar = lPlayerLoc.getCarByIndex(i);
			if (lTrainCar != lPlayerLoc.leadCar) {
				if (lTrainCar.frontHitch.isOpen)
					lTextureBatch.draw(lWorldTexture, lHitchOpenRect, lTrainHudPositionX, lTrainHudPositionY + 64 - 16, 8 * 2, 8 * 2, -0.1f, ColorConstants.WHITE);
				else
					lTextureBatch.draw(lWorldTexture, lHitchClosedRect, lTrainHudPositionX, lTrainHudPositionY + 64 - 16, 8 * 2, 8 * 2, -0.1f, ColorConstants.WHITE);
			}

			lTrainHudPositionX += 16f + 8f;
			if (lTrainCar.isLocomotive()) {
				lTextureBatch.draw(lWorldTexture, lTrainCarRect, lTrainHudPositionX, lTrainHudPositionY, 64 * 2, 32 * 2, -0.1f, ColorConstants.WHITE);
			} else {
				lTextureBatch.draw(lWorldTexture, lTrainCarRect, lTrainHudPositionX, lTrainHudPositionY, 64 * 2, 32 * 2, -0.1f, ColorConstants.WHITE);
			}
			lTrainHudPositionX += 64.f * 2f + 5.f;

			// Render the hitch status for the current train car
			lTextureBatch.draw(lWorldTexture, lHitchClosedRect, lTrainHudPositionX, lTrainHudPositionY + 64 - 16, 8 * 2, 8 * 2, -0.1f, ColorConstants.WHITE);

			lTrainHudPositionX += 32f;

		}
		lTextureBatch.end();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void handleClickOnHud(LintfordCore pCore, float pMouseHudX, float pMouseHudY) {
		float lTrainHudPositionX = mWindowArea.left() + 15f;
		float lTrainHudPositionY = mWindowArea.top() + 8f;

//		final var lMainTrain = mTrainController.mainTrain();
//		final var lNumCarsInMainTrain = lMainTrain.getNumberOfCarsInTrain();
//		for (int i = 0; i < lNumCarsInMainTrain; i++) {
//			final var lTrainCar = lMainTrain.getCarByIndex(i);
//
//			// TODO: Kind of car
//
//			// TODO: Status / Info of car (hp, crew, powered, weight ?)
//			if (pMouseHudX > lTrainHudPositionX && pMouseHudX < (lTrainHudPositionX + 16f) && pMouseHudY > lTrainHudPositionY + 64 - 16) {
//				System.out.println("Click on Hud front hitch " + i);
//
//			}
//			lTrainHudPositionX += 16f + 8f;
//			if (pMouseHudX > lTrainHudPositionX && pMouseHudX < (lTrainHudPositionX + 64.f * 2f + 5.f)) {
//				System.out.println("Click on Hud train " + i);
//
//				// TODO: Finish this off with hitches
//				if (i > 0)
//					mTrainController.unhitchTrainCar(lMainTrain, i);
//
//			}
//			lTrainHudPositionX += 64.f * 2f + 5.f;
//			if (pMouseHudX > lTrainHudPositionX && pMouseHudX < (lTrainHudPositionX + 16f) && pMouseHudY > lTrainHudPositionY + 64 - 16) {
//				System.out.println("Click on Hud rear hitch " + i);
//			}
//			lTrainHudPositionX += 32f;
//
//		}
	}

	@Override
	public void updateWindowPosition(LintfordCore pCore) {
		super.updateWindowPosition(pCore);

		final var lHudRect = pCore.HUD().boundingRectangle();

		mWindowArea.set(lHudRect.left(), lHudRect.bottom() - 64f - 16f, lHudRect.width(), 64f + 16);
	}

}
