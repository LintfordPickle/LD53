package lintfordpickle.mailtrain.screens.editor;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.controllers.core.GameCameraMovementController;
import lintfordpickle.mailtrain.controllers.core.GameCameraZoomController;
import lintfordpickle.mailtrain.controllers.world.SceneryController;
import lintfordpickle.mailtrain.data.GameWorldHeader;
import lintfordpickle.mailtrain.renderers.EditorSignalRenderer;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.GridRenderer;
import lintfordpickle.mailtrain.renderers.SceneryRenderer;
import lintfordpickle.mailtrain.screens.MainMenu;
import lintfordpickle.mailtrain.screens.MenuBackgroundScreen;
import lintfordpickle.mailtrain.screens.dialogs.SaveTrackDialog;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.GameRendererController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.core.time.LogicialCounter;
import net.lintford.library.screenmanager.ClickAction;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.entries.EntryInteractions;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class TrackEditorScreen extends MenuScreen implements EntryInteractions {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final int BUTTON_DIALOG_SAVE_CONFIRM = 100;
	public static final int BUTTON_DIALOG_SAVE_CANCEL = 101;

	public static final float ANIMATION_TIMER_LENGTH = 130; // ms

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	protected ICamera mGameCamera;
	protected LogicialCounter mGameInputLogicalCounter;
	protected LogicialCounter mGameDrawLogicalCounter;
	protected ResourceManager mResourceManager;

	// Data
	private GameWorldHeader mTrackHeader;

	// Controllers
	private GameCameraMovementController mCameraMovementController;
	private GameCameraZoomController mCameraZoomController;
	private TrackEditorController mTrackEditorController;
	private SceneryController mSceneryController;

	// Renderers
	private EditorTrackRenderer mTrackEditorRenderer;
	private GridRenderer mGridRenderer;
	private EditorSignalRenderer mSignalBlockRenderer;
	private SceneryRenderer mSceneryRenderer;
	private SaveTrackDialog mSaveTrackDialog;

	// Clicky stuff
	protected ClickAction mClickAction;
	protected float mAnimationTimer;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackEditorScreen(ScreenManager pScreenManager, GameWorldHeader pTrackHeader) {
		super(pScreenManager, "");

		mTrackHeader = pTrackHeader;

		mSaveTrackDialog = new SaveTrackDialog(screenManager(), this);
		mSaveTrackDialog.confirmEntry().registerClickListener(this, BUTTON_DIALOG_SAVE_CONFIRM);
		mSaveTrackDialog.cancelEntry().registerClickListener(this, BUTTON_DIALOG_SAVE_CANCEL);

		mClickAction = new ClickAction();

		mGameInputLogicalCounter = new LogicialCounter();
		mGameDrawLogicalCounter = new LogicialCounter();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		final var lCore = screenManager().core();
		final var lControllerManager = lCore.controllerManager();

		new GameRendererController(lControllerManager, mRendererManager, entityGroupUid());
		mGameCamera = mScreenManager.core().setNewGameCamera(mGameCamera);

		createControllers(lControllerManager);
		createRenderers(lCore);

		initializeControllers(lCore);

		initializeRenderers(lCore);

		if (mTrackHeader.trackFilename() == null) {
			mTrackEditorController.setNewScene();
		} else {
			mTrackEditorController.loadTrackFromFile(mTrackHeader.trackFilename());
		}

		if (mTrackHeader.sceneryFilename() == null) {

		}
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		loadRendererResources(resourceManager);

		resourceManager.spriteSheetManager().loadSpriteSheet("res/spritesheets/spritesheetTracks.json", entityGroupUid());
	}

	@Override
	public void unloadResources() {
		super.unloadResources();
	}

	@Override
	public void handleInput(LintfordCore pCore) {
		if (mAnimationTimer > 0 || mClickAction.isConsumed())
			return; // don't handle input if 'animation' is playing

		final var lScreenManager = screenManager();

		super.handleInput(pCore);
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_F4, this)) {
			mSaveTrackDialog.trackFilename(mTrackHeader.trackFilename());
			lScreenManager.addScreen(mSaveTrackDialog);
			return;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE, this)) {
			lScreenManager.createLoadingScreen(new LoadingScreen(lScreenManager, false, new MenuBackgroundScreen(lScreenManager), new MainMenu(lScreenManager)));
			return;
		}
	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

		final var lDeltaTime = pCore.appTime().elapsedTimeMilli();

		// Handle dialog clicks
		if (mAnimationTimer > 0) {
			mAnimationTimer -= lDeltaTime;

		} else if (mClickAction.entryUid() != -1 && !mClickAction.isConsumed()) { // something was clicked
			handleOnClick();
			mClickAction.reset();
			return;

		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void createControllers(ControllerManager pControllerManager) {
		mCameraMovementController = new GameCameraMovementController(pControllerManager, mGameCamera, entityGroupUid());
		mCameraMovementController.setPlayArea(-1400, -1100, 2800, 2200);
		mCameraZoomController = new GameCameraZoomController(pControllerManager, mGameCamera, entityGroupUid());
		mCameraZoomController.setZoomConstraints(200, 900);

		mTrackEditorController = new TrackEditorController(pControllerManager, screenManager(), entityGroupUid());
		mSceneryController = new SceneryController(pControllerManager, null, entityGroupUid());
	}

	public void initializeControllers(LintfordCore pCore) {
		mTrackEditorController.initialize(pCore);
		mSceneryController.initialize(pCore);

		mCameraMovementController.initialize(pCore);
		mCameraZoomController.initialize(pCore);
	}

	public void createRenderers(LintfordCore pCore) {
		mTrackEditorRenderer = new EditorTrackRenderer(rendererManager(), entityGroupUid());
		mGridRenderer = new GridRenderer(rendererManager(), entityGroupUid());
		mSignalBlockRenderer = new EditorSignalRenderer(rendererManager(), entityGroupUid());
		mSceneryRenderer = new SceneryRenderer(rendererManager(), entityGroupUid());

	}

	protected void initializeRenderers(LintfordCore core) {
		mTrackEditorRenderer.initialize(core);
		mGridRenderer.initialize(core);
		mSignalBlockRenderer.initialize(core);
		mSceneryRenderer.initialize(core);

	}

	protected void loadRendererResources(ResourceManager resourceManager) {
		mTrackEditorRenderer.loadResources(resourceManager);
		mGridRenderer.loadResources(resourceManager);
		mSignalBlockRenderer.loadResources(resourceManager);
		mSceneryRenderer.loadResources(resourceManager);

	}

	private void saveTrackToFile(String pFilename) {
		String lTrackFilename = pFilename;
		if (!lTrackFilename.endsWith(GameWorldHeader.TRACK_FILE_EXTENSION)) {
			lTrackFilename = lTrackFilename + GameWorldHeader.TRACK_FILE_EXTENSION;
		}
		if (!lTrackFilename.startsWith(GameWorldHeader.TRACKS_DIRECTORY)) {
			lTrackFilename = GameWorldHeader.TRACKS_DIRECTORY + lTrackFilename;
		}
		System.out.println("Saving track to " + lTrackFilename);

		mTrackEditorController.saveTrack(lTrackFilename);
		// mSceneryController.saveSceneryScene(lSceneryFilename);
	}

	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_DIALOG_SAVE_CONFIRM:
			saveTrackToFile(mSaveTrackDialog.trackFilename());
			mSaveTrackDialog.exitScreen();
			break;
		case BUTTON_DIALOG_SAVE_CANCEL:
			mSaveTrackDialog.exitScreen();
			break;

		}
	}

	// ---------------------------------------------
	// EntryInteractions-Methods
	// ---------------------------------------------

	@Override
	public void menuEntryOnClick(InputManager pInputState, int pEntryID) {
		mClickAction.setNewClick(pEntryID);
		mAnimationTimer = ANIMATION_TIMER_LENGTH * 2f;

		// prevent the dialog from processing the menu entries (until the button press animation has finished)
		mSaveTrackDialog.menuEntryOnClick(null, ClickAction.ENTRY_UID_UNASSIGNED);
	}

}
