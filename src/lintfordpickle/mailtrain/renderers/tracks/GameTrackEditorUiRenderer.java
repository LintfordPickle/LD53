package lintfordpickle.mailtrain.renderers.tracks;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.GameStateController;
import lintfordpickle.mailtrain.controllers.GameTrackEditorController;
import lintfordpickle.mailtrain.controllers.GameTrackEditorController.EditorMode;
import lintfordpickle.mailtrain.controllers.TrainController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import net.lintford.library.controllers.core.MouseCursorController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiIconToggleButton;
import net.lintford.library.screenmanager.entries.EntryInteractions;

public class GameTrackEditorUiRenderer extends UiWindow implements IInputProcessor, EntryInteractions {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Editor Controls Renderer";

	private static final int BUTTON_ID_PLACE_TRACK = 0;
	private static final int BUTTON_ID_PLACE_ROAD = 1;

	private static final int BUTTON_ID_PLACE_TRAIN = 10;

	private static final int BUTTON_ID_DEMOLISH = 99;

	private static final String CURSOR_NAME_DEMOLISH = "CURSOR_DEMOLISH";
	private static final String CURSOR_NAME_TRACK = "CURSOR_TRACK";
	private static final String CURSOR_NAME_ROAD = "CURSOR_ROAD";
	private static final String CURSOR_NAME_SIGNAL = "CURSOR_SIGNAL";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SpriteSheetDefinition mHudSpriteSheet;
	private GameStateController mGameStateController;
	private TrackController mTrackController;
	private TrainController mTrainController;
	private GameTrackEditorController mGameTrackEditorController;

	private MouseCursorController mMouseCursorController;

	private UiIconToggleButton mPlaceTrack;
	private UiIconToggleButton mPlaceRoad;
	private UiIconToggleButton mDemolishTrack;
	private UiIconToggleButton mPlaceTrain;

	private float mLeftMouseCooldownTimer;

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

	public boolean isCoolDownElapsed() {
		return mLeftMouseCooldownTimer <= 0.f;
	}

	public void resetCoolDownTimer() {
		mLeftMouseCooldownTimer = 200.f;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameTrackEditorUiRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mPlaceTrack = new UiIconToggleButton(this);
		mPlaceTrack.set(0, 0, 64, 64);
		mPlaceTrack.setClickListener(this, BUTTON_ID_PLACE_TRACK);

		mPlaceRoad = new UiIconToggleButton(this);
		mPlaceRoad.set(0, 0, 64, 64);
		mPlaceRoad.setClickListener(this, BUTTON_ID_PLACE_ROAD);

		mDemolishTrack = new UiIconToggleButton(this);
		mDemolishTrack.set(0, 0, 64, 64);
		mDemolishTrack.setClickListener(this, BUTTON_ID_DEMOLISH);

		mPlaceTrain = new UiIconToggleButton(this);
		mPlaceTrain.set(0, 0, 64, 64);
		mPlaceTrain.setClickListener(this, BUTTON_ID_PLACE_TRAIN);

		mComponents.add(mPlaceTrack);
		mComponents.add(mPlaceRoad);
		mComponents.add(mDemolishTrack);
		mComponents.add(mPlaceTrain);

		mIsOpen = true;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mGameStateController = (GameStateController) pCore.controllerManager().getControllerByNameRequired(GameStateController.CONTROLLER_NAME, entityGroupID());
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
		mGameTrackEditorController = (GameTrackEditorController) pCore.controllerManager().getControllerByNameRequired(GameTrackEditorController.CONTROLLER_NAME, entityGroupID());
		mMouseCursorController = (MouseCursorController) pCore.controllerManager().getControllerByNameRequired(MouseCursorController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mTrainController = (TrainController) pCore.controllerManager().getControllerByNameRequired(TrainController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mHudSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_HUD", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		mPlaceTrack.setTextureSource(mHudSpriteSheet, mHudSpriteSheet.getSpriteFrameIndexByName("TEXTUREICONTRACK"));
		mPlaceRoad.setTextureSource(mHudSpriteSheet, mHudSpriteSheet.getSpriteFrameIndexByName("TEXTUREICONROAD"));
		mDemolishTrack.setTextureSource(mHudSpriteSheet, mHudSpriteSheet.getSpriteFrameIndexByName("TEXTUREICONDEMOLISH"));
		mPlaceTrain.setTextureSource(mHudSpriteSheet, mHudSpriteSheet.getSpriteFrameIndexByName("TEXTUREICONTRAIN"));

		// Load Cursors
		mMouseCursorController.loadCursorFromFile("CURSOR_TRACK", "res/cursors/cursorTrack.png", 0, 0);
		mMouseCursorController.loadCursorFromFile("CURSOR_DEMOLISH", "res/cursors/cursorDemolish.png", 0, 0);
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (pCore.input().mouse().isMouseRightButtonDown()) {
			mPlaceTrack.isToggledOn(false);
			mPlaceRoad.isToggledOn(false);
			mDemolishTrack.isToggledOn(false);
			mPlaceTrain.isToggledOn(false);

			mGameTrackEditorController.setEditorMode(EditorMode.normal);
			mMouseCursorController.setCursor(MouseCursorController.DEFAULT_CURSOR_NAME);
		}
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		// Don't draw UI when game over
		if (mGameStateController.getHasLost() || mGameStateController.getHasWon())
			return;

		final var lHudRect = pCore.HUD().boundingRectangle();

		mPlaceTrack.set(lHudRect.left() + 32.f + 64.f * 0.f, lHudRect.bottom() - 16.f - 64.f, 64, 64);
		mPlaceRoad.set(lHudRect.left() + 32.f + 64.f * 1.f, lHudRect.bottom() - 16.f - 64.f, 64, 64);
		mDemolishTrack.set(lHudRect.left() + 32.f + 64.f * 2.f, lHudRect.bottom() - 16.f - 64.f, 64, 64);
		mPlaceTrain.set(lHudRect.left() + 32.f + 64.f * 3.f, lHudRect.bottom() - 16.f - 64.f, 64, 64);

		mPlaceTrack.update(pCore);
		mPlaceRoad.update(pCore);
		mDemolishTrack.update(pCore);
		mPlaceTrain.update(pCore);
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackController.isInitialized())
			return;

		mRendererManager.uiSpriteBatch().begin(pCore.HUD());
		mPlaceTrack.draw(pCore, mRendererManager.uiSpriteBatch(), mHudSpriteSheet, mRendererManager.uiTextFont(), -0.1f);
		mPlaceRoad.draw(pCore, mRendererManager.uiSpriteBatch(), mHudSpriteSheet, mRendererManager.uiTextFont(), -0.1f);
		mDemolishTrack.draw(pCore, mRendererManager.uiSpriteBatch(), mHudSpriteSheet, mRendererManager.uiTextFont(), -0.1f);
		mPlaceTrain.draw(pCore, mRendererManager.uiSpriteBatch(), mHudSpriteSheet, mRendererManager.uiTextFont(), -0.1f);
		mRendererManager.uiSpriteBatch().end();
	}

	// ---------------------------------------------

	@Override
	public void menuEntryOnClick(InputManager pInputState, int pEntryID) {
		mGameTrackEditorController.resetCoolDownTimer();
		switch (pEntryID) {
		case BUTTON_ID_PLACE_TRACK:
			mPlaceRoad.isToggledOn(false);
			mDemolishTrack.isToggledOn(false);

			if (mPlaceTrack.isToggledOn()) {
				mGameTrackEditorController.setEditorMode(EditorMode.place_track);
				mMouseCursorController.setCursor(CURSOR_NAME_TRACK);
			} else {
				mGameTrackEditorController.setEditorMode(EditorMode.normal);
				mMouseCursorController.setCursor(MouseCursorController.DEFAULT_CURSOR_NAME);
			}
			break;
		case BUTTON_ID_PLACE_ROAD:
			mDemolishTrack.isToggledOn(false);
			mPlaceTrack.isToggledOn(false);

			if (mPlaceRoad.isToggledOn()) {
				mGameTrackEditorController.setEditorMode(EditorMode.place_road);
				mMouseCursorController.setCursor(CURSOR_NAME_ROAD);
			} else {
				mGameTrackEditorController.setEditorMode(EditorMode.normal);
				mMouseCursorController.setCursor(MouseCursorController.DEFAULT_CURSOR_NAME);
			}

			break;
		case BUTTON_ID_DEMOLISH:
			mPlaceRoad.isToggledOn(false);
			mPlaceTrack.isToggledOn(false);

			if (mDemolishTrack.isToggledOn()) {
				mGameTrackEditorController.setEditorMode(EditorMode.demolish);
				mMouseCursorController.setCursor(CURSOR_NAME_DEMOLISH);
			} else {
				mGameTrackEditorController.setEditorMode(EditorMode.normal);
				mMouseCursorController.setCursor(MouseCursorController.DEFAULT_CURSOR_NAME);
			}
			break;
		case BUTTON_ID_PLACE_TRAIN:
			final var lSelectedTrackSegment = mGameTrackEditorController.selectedTrackSegment;
			if (lSelectedTrackSegment != null) {
				mTrainController.addNewTrain(lSelectedTrackSegment);
			}
			break;

		}
	}

}
