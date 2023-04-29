package lintfordpickle.mailtrain.renderers.hud;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.GameStateController;
import lintfordpickle.mailtrain.controllers.TrainController;
import lintfordpickle.mailtrain.controllers.core.GameCameraMovementController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiIconButton;
import net.lintford.library.screenmanager.entries.EntryInteractions;

public class PlayerControlsRenderer extends UiWindow implements IInputProcessor, EntryInteractions {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Player Controls Renderer";

	private static final int BUTTON_ID_FOLLOW_CAMERA = 0;
	private static final int BUTTON_ID_STOP_TRAIN = 1;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SpriteSheetDefinition mHudSpriteSheet;
	private GameStateController mGameStateController;
	private GameCameraMovementController mCameraMovementController;
	private TrainController mTrainController;
	private TrackController mTrackController;

	private UiIconButton mFollowTrainToggleButton;
	private UiIconButton mStopTrainToggleButton;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 10;
	}

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

	public PlayerControlsRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mFollowTrainToggleButton = new UiIconButton(this);
		mFollowTrainToggleButton.set(0, 0, 64, 64);
		mFollowTrainToggleButton.setClickListener(this, BUTTON_ID_FOLLOW_CAMERA);

		mStopTrainToggleButton = new UiIconButton(this);
		mStopTrainToggleButton.set(0, 0, 64, 64);
		mStopTrainToggleButton.setClickListener(this, BUTTON_ID_STOP_TRAIN);

		mComponents.add(mStopTrainToggleButton);
		mComponents.add(mFollowTrainToggleButton);

		mIsOpen = true;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mGameStateController = (GameStateController) pCore.controllerManager().getControllerByNameRequired(GameStateController.CONTROLLER_NAME, entityGroupID());
		mCameraMovementController = (GameCameraMovementController) pCore.controllerManager().getControllerByNameRequired(GameCameraMovementController.CONTROLLER_NAME, entityGroupID());
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
		mTrainController = (TrainController) pCore.controllerManager().getControllerByNameRequired(TrainController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mHudSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_HUD", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		mFollowTrainToggleButton.setTextureSource(mHudSpriteSheet, mHudSpriteSheet.getSpriteFrameIndexByName("TEXTUREBUTTONFOLLOW"));
		mStopTrainToggleButton.setTextureSource(mHudSpriteSheet, mHudSpriteSheet.getSpriteFrameIndexByName("TEXTUREBUTTONSTOP"));
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		mFollowTrainToggleButton.handleInput(pCore);
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		// Don't draw UI when game over
		if (mGameStateController.getHasLost() || mGameStateController.getHasWon())
			return;

		final var lHudRect = pCore.HUD().boundingRectangle();

		mStopTrainToggleButton.set(lHudRect.right() - 64.f * 2 - 32, lHudRect.bottom() - 16.f - 64.f, 64, 64);
		mStopTrainToggleButton.update(pCore);
		mFollowTrainToggleButton.set(lHudRect.right() - 64.f - 15.f, lHudRect.bottom() - 16.f - 64.f, 64, 64);
		mFollowTrainToggleButton.update(pCore);
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackController.isInitialized())
			return;

		// Don't draw UI when game over
		if (mGameStateController.getHasLost() || mGameStateController.getHasWon())
			return;
		{
			mRendererManager.uiSpriteBatch().begin(pCore.HUD());
			mFollowTrainToggleButton.draw(pCore, mRendererManager.uiSpriteBatch(), mHudSpriteSheet, mRendererManager.uiTextFont(), -0.1f);
			mStopTrainToggleButton.draw(pCore, mRendererManager.uiSpriteBatch(), mHudSpriteSheet, mRendererManager.uiTextFont(), -0.1f);
			mRendererManager.uiSpriteBatch().end();
		}
	}

	@Override
	public void menuEntryOnClick(InputManager pInputState, int pEntryID) {
		if (pEntryID == BUTTON_ID_FOLLOW_CAMERA) {
			if (mCameraMovementController.isFollowingTrain()) {
				mCameraMovementController.setFollowTrain(null);

			} else {
				final var lMainTrain = mTrainController.mainTrain();
				mCameraMovementController.setFollowTrain(lMainTrain);

			}
		}
		if (pEntryID == BUTTON_ID_STOP_TRAIN) {
//			final var lMainTrain = mTrainController.mainTrain();
//			lMainTrain.targetSpeedInMetersPerSecond = 0.f;

		}
	}


}
