package lintfordpickle.mailtrain.screens.editor;

import java.io.File;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.controllers.core.GameCameraMovementController;
import lintfordpickle.mailtrain.controllers.core.GameCameraZoomController;
import lintfordpickle.mailtrain.controllers.editor.EditorBrush;
import lintfordpickle.mailtrain.controllers.editor.EditorBrushController;
import lintfordpickle.mailtrain.controllers.editor.EditorHashGridController;
import lintfordpickle.mailtrain.controllers.editor.gui.EditorFileController;
import lintfordpickle.mailtrain.controllers.scene.GameSceneController;
import lintfordpickle.mailtrain.controllers.scene.SceneryController;
import lintfordpickle.mailtrain.data.scene.GameSceneHeader;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.renderers.EditorSignalRenderer;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.GridRenderer;
import lintfordpickle.mailtrain.renderers.SceneryRenderer;
import lintfordpickle.mailtrain.renderers.editor.EditorBrushRenderer;
import lintfordpickle.mailtrain.renderers.editor.EditorHashGridRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiDockedWindow;
import lintfordpickle.mailtrain.screens.MainMenu;
import lintfordpickle.mailtrain.screens.MenuBackgroundScreen;
import lintfordpickle.mailtrain.screens.dialogs.SaveTrackDialog;
import lintfordpickle.mailtrain.services.GameSceneHeaderIOService;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.GameRendererController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.debug.Debug;
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
	private GameWorldHeader mWorldHeader;
	private GameSceneHeader mSceneHeader;
	private GameSceneInstance mGameScene;
	private EditorBrush mEditorBrush;

	// Controllers
	private EditorHashGridController mEditorHashGridController;
	private EditorBrushController mEditorBrushController;
	private GameSceneController mGameSceneController;
	private GameCameraMovementController mCameraMovementController;
	private GameCameraZoomController mCameraZoomController;
	private TrackEditorController mTrackEditorController;
	private SceneryController mSceneryController;

	private EditorFileController mEditorFileController;

	// Renderers
	private UiDockedWindow mEditorGui;

	private EditorTrackRenderer mTrackEditorRenderer;
	private GridRenderer mGridRenderer;
	private EditorSignalRenderer mSignalBlockRenderer;
	private SceneryRenderer mSceneryRenderer;
	private SaveTrackDialog mSaveTrackDialog;

	private EditorHashGridRenderer mEditorHashGridRenderer;
	private EditorBrushRenderer mEditorBrushRenderer;

	// Clicky stuff
	protected ClickAction mClickAction;
	protected float mAnimationTimer;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackEditorScreen(ScreenManager screenManager, GameWorldHeader worldHeader, GameSceneHeader sceneHeader) {
		super(screenManager, "");

		mWorldHeader = worldHeader;
		mSceneHeader = sceneHeader;

		screenManager.contextHintManager().enabled(false);

		mGameScene = new GameSceneInstance(worldHeader);
		mEditorBrush = new EditorBrush();

		mESCBackEnabled = false;

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

		if (mSceneHeader.sceneFilename() != null) {
			mGameSceneController.loadGameScene(mSceneHeader.sceneFilename());
		} else {
			mGameSceneController.createEmptyScene();
		}

		initializeControllers(lCore);
		initializeRenderers(lCore);

		mGameScene.initializeScene();
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
			var lProposedFilename = "";

			if (mSceneHeader.sceneFilename() != null) {
				final var lSceneFilename = new File(mSceneHeader.sceneFilename());

				if (lSceneFilename.exists())
					lProposedFilename = lSceneFilename.getName();
			}

			mSaveTrackDialog.trackFilename(lProposedFilename);
			lScreenManager.addScreen(mSaveTrackDialog);
			return;
		}

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE, this)) {
			if (mEditorBrush.isActionSet()) {
				mEditorBrush.clearAction();
				return;
			}

			// otherwise check for changes to the map before exiting

			// otherwise, exit

			if (lScreenManager.getTopScreen() instanceof SaveTrackDialog) {
				lScreenManager.removeScreen(mSaveTrackDialog);
				return;
			}
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

	public void createControllers(ControllerManager controllerManager) {
		// Editor Controller
		mEditorHashGridController = new EditorHashGridController(controllerManager, entityGroupUid());
		mEditorBrushController = new EditorBrushController(controllerManager, mEditorBrush, entityGroupUid());
		mEditorFileController = new EditorFileController(controllerManager, entityGroupUid());

		// Scene Controllers

		mGameSceneController = new GameSceneController(controllerManager, mWorldHeader, mSceneHeader, mGameScene, entityGroupUid());
		mTrackEditorController = new TrackEditorController(controllerManager, mGameScene, entityGroupUid());
		mSceneryController = new SceneryController(controllerManager, null, entityGroupUid());

		mCameraMovementController = new GameCameraMovementController(controllerManager, mGameCamera, entityGroupUid());
		mCameraMovementController.setPlayArea(-1400, -1100, 2800, 2200);
		mCameraZoomController = new GameCameraZoomController(controllerManager, mGameCamera, entityGroupUid());
		mCameraZoomController.setZoomConstraints(200, 900);
	}

	public void initializeControllers(LintfordCore core) {
		mEditorHashGridController.initialize(core);
		mEditorBrushController.initialize(core);
		mTrackEditorController.initialize(core);
		mSceneryController.initialize(core);
		mGameSceneController.initialize(core);
		mCameraMovementController.initialize(core);
		mCameraZoomController.initialize(core);
		mEditorFileController.initialize(core);
	}

	public void createRenderers(LintfordCore core) {
		mTrackEditorRenderer = new EditorTrackRenderer(mRendererManager, entityGroupUid());
		mGridRenderer = new GridRenderer(mRendererManager, entityGroupUid());
		mSignalBlockRenderer = new EditorSignalRenderer(mRendererManager, entityGroupUid());
		mSceneryRenderer = new SceneryRenderer(mRendererManager, entityGroupUid());

		mEditorGui = new UiDockedWindow(mRendererManager, "Editor", entityGroupUid());
		mEditorHashGridRenderer = new EditorHashGridRenderer(mRendererManager, entityGroupUid());
		mEditorBrushRenderer = new EditorBrushRenderer(mRendererManager, entityGroupUid());
	}

	protected void initializeRenderers(LintfordCore core) {
		mTrackEditorRenderer.initialize(core);
		mEditorGui.initialize(core);
		mGridRenderer.initialize(core);
		mSignalBlockRenderer.initialize(core);
		mSceneryRenderer.initialize(core);
		mEditorHashGridRenderer.initialize(core);
		mEditorBrushRenderer.initialize(core);
	}

	protected void loadRendererResources(ResourceManager resourceManager) {
		mTrackEditorRenderer.loadResources(resourceManager);
		mEditorGui.loadResources(resourceManager);
		mGridRenderer.loadResources(resourceManager);
		mSignalBlockRenderer.loadResources(resourceManager);
		mSceneryRenderer.loadResources(resourceManager);
		mEditorHashGridRenderer.loadResources(resourceManager);
		mEditorBrushRenderer.loadResources(resourceManager);

	}

	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_DIALOG_SAVE_CONFIRM:
			saveSceneToFile(mSaveTrackDialog.trackFilename());
			mSaveTrackDialog.exitScreen();
			break;
		case BUTTON_DIALOG_SAVE_CANCEL:
			mSaveTrackDialog.exitScreen();
			break;

		}
	}

	private void saveSceneToFile(String sceneFilename) {
		var lTrackFilename = sceneFilename;

		if (lTrackFilename.indexOf('.') > 0)
			lTrackFilename = lTrackFilename.substring(0, lTrackFilename.lastIndexOf('.'));

		File lFileName = new File(lTrackFilename);

		final var lSceneDirectory = mWorldHeader.worldDirectory() + ConstantsGame.SCENES_REL_DIRECTORY;

		final var lFullHeaderFilename = lSceneDirectory + lFileName.getName() + GameSceneHeader.SCENE_FILE_EXTENSION;
		final var lFullFilename = lSceneDirectory + lFileName.getName() + GameSceneHeader.LEVEL_FILE_EXTENSION;

		Debug.debugManager().logger().i(getClass().getSimpleName(), "Saving track to: " + lFullFilename);

		mSceneHeader.sceneFilename(lFullFilename);

		GameSceneHeaderIOService.saveGameSceneHeader(mSceneHeader, lFullHeaderFilename);
		mGameSceneController.saveSceneToFile(lFullFilename);
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
