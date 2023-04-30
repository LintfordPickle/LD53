package lintfordpickle.mailtrain.screens.game;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.GameStateController;
import lintfordpickle.mailtrain.controllers.GameTrackEditorController;
import lintfordpickle.mailtrain.controllers.TriggerController;
import lintfordpickle.mailtrain.controllers.core.GameCameraMovementController;
import lintfordpickle.mailtrain.controllers.core.GameCameraZoomController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.controllers.tracks.TrackIOController;
import lintfordpickle.mailtrain.controllers.trains.PlayerTrainController;
import lintfordpickle.mailtrain.controllers.trains.TrainController;
import lintfordpickle.mailtrain.controllers.world.GameWorldController;
import lintfordpickle.mailtrain.controllers.world.WorldIOController;
import lintfordpickle.mailtrain.data.GameState;
import lintfordpickle.mailtrain.data.trains.TrainManager;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.data.world.scenes.GameScene;
import lintfordpickle.mailtrain.data.world.scenes.SceneHeader;
import lintfordpickle.mailtrain.renderers.GridRenderer;
import lintfordpickle.mailtrain.renderers.TrackSignalRenderer;
import lintfordpickle.mailtrain.renderers.hud.GameStateUiRenderer;
import lintfordpickle.mailtrain.renderers.hud.PlayerControlsRenderer;
import lintfordpickle.mailtrain.renderers.hud.TrainHudRenderer;
import lintfordpickle.mailtrain.renderers.tracks.GameTrackEditorUiRenderer;
import lintfordpickle.mailtrain.renderers.tracks.TrackDebugRenderer;
import lintfordpickle.mailtrain.renderers.tracks.TrackGhostRenderer;
import lintfordpickle.mailtrain.renderers.tracks.TrackRenderer;
import lintfordpickle.mailtrain.renderers.trains.TrainRenderer;
import lintfordpickle.mailtrain.renderers.world.GameBackgroundRenderer;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.debug.DebugRendererTreeController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.AudioManager;
import net.lintford.library.core.audio.AudioSource;
import net.lintford.library.core.audio.data.AudioData;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class GameScreen extends BaseGameScreen {

	// Data
	private GameState mActiveGameState;
	private GameState mInitialGameState;

	private GameScene mActiveGameScene;

	private TrainManager mTrainManager;

	private GameWorldHeader mGameWorldHeader;
	private SceneHeader mSceneHeader;

	// Controllers
	private GameCameraMovementController mCameraMovementController;
	private GameCameraZoomController mCameraZooomController;
	private GameStateController mGameStateController;
	private GameWorldController mGameWorldController;
	private TrainController mTrainController;
	private PlayerTrainController mPlayerTrainController;
	private TrackController mTrackController;
	private GameTrackEditorController mGameTrackEditorController;
	private TriggerController mTriggerController;

	// Renderers
	private TrainHudRenderer mTrainHudRenderer;
	private TrainRenderer mTrainRenderer;
	private GameBackgroundRenderer mBackgroundRenderer;
	private TrackSignalRenderer mTrackSignalRenderer;
	private TrackGhostRenderer mTrackGhostRenderer;
	private TrackDebugRenderer mDebugTrackRenderer;

	// HUD
	private TrackRenderer mTrackRenderer;
	private PlayerControlsRenderer mPlayerControlsRenderer;
	private GridRenderer mGridRenderer;
	private GameStateUiRenderer mGameStateUIRenderer;
	private GameTrackEditorUiRenderer mGameTrackEditorRenderer;

	// TEMP ---

	private AudioSource mBackgroundAudioSource;
	private AudioData mAudioDataBufferBackground;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager screenManager, GameState persistentGameState, GameWorldHeader gameWorldHeader, SceneHeader sceneHeader) {
		super(screenManager);

		mShowBackgroundScreens = true;

		mGameWorldHeader = gameWorldHeader;
		mSceneHeader = sceneHeader;

		mActiveGameScene = new GameScene(mGameWorldHeader);
		mActiveGameState = persistentGameState;
		mInitialGameState = new GameState(mActiveGameState);

		mTrainManager = new TrainManager();

		final var lTrackFilename = gameWorldHeader.getSceneTrackHeaderFilepath(sceneHeader);
		var lTrackToLoad = TrackIOController.loadTrackFromFile(lTrackFilename);
		var lWorldScenery = WorldIOController.loadSceneryFromFile("res/scenery/sceneryTest.json" /* mTrackHeader.sceneryFilename() */);

		mActiveGameScene.track(lTrackToLoad);
		mActiveGameScene.props(lWorldScenery);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		final var lCore = screenManager().core();
		final var lControllerManager = lCore.controllerManager();

		lControllerManager.initializeControllers(lCore);

		mPlayerTrainController.addPlayerTrain(mSceneHeader.startEntryPointName());

		final int lStartingCredits = 1000;
		final int lStartingCrew = 10;
		final float lStartingFuel = 10;
		mActiveGameState.startNewGame(lStartingCredits, lStartingFuel, lStartingCrew);

		// Register the renders with the renderer tracker so they can be controlled through the debug menu (F1)
		if (Debug.debugManager().debugManagerEnabled() && ConstantsGame.IS_DEBUG_MODE) {
			DebugRendererTreeController lDebugRendererTreeController = (DebugRendererTreeController) lControllerManager.getControllerByName(DebugRendererTreeController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);

			lDebugRendererTreeController.trackRendererManager(rendererManager());
		}
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		// Load the SpriteSheet resources

		pResourceManager.audioManager().loadAudioFile("SOUND_BACKGROUND", "res/sounds/soundBackground.wav", false);

		mAudioDataBufferBackground = pResourceManager.audioManager().getAudioDataBufferByName("SOUND_BACKGROUND");
		mBackgroundAudioSource = pResourceManager.audioManager().getAudioSource(hashCode(), AudioManager.AUDIO_SOURCE_TYPE_SOUNDFX);

		// todo. font loading
		// pResourceManager.fontManager().load.loadNewFont("FONT_GAME_TEXT", "/res/fonts/Rajdhani-Bold.ttf", 28, true, true);

		mBackgroundAudioSource.setLooping(true);
		if (mAudioDataBufferBackground != null) {
			// mBackgroundAudioSource.play(mAudioDataBufferBackground.bufferID());
		}
	}

	@Override
	public void handleInput(LintfordCore core) {
		super.handleInput(core);

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE, this) || core.input().gamepads().isGamepadButtonDownTimed(GLFW.GLFW_GAMEPAD_BUTTON_START, this)) {
			if (ConstantsGame.ESCAPE_RESTART_MAIN_SCENE) {
				final var lLoadingScreen = new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), mActiveGameState, mGameWorldHeader, mSceneHeader));
				screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, lLoadingScreen));
				return;
			}

			screenManager().addScreen(new PauseScreen(screenManager(), mInitialGameState, mActiveGameState, mGameWorldHeader, mSceneHeader));

			return;
		}
	}

	@Override
	public void unloadResources() {
		super.unloadResources();
		if (mBackgroundAudioSource != null) {
			if (mBackgroundAudioSource.isPlaying()) {
				mBackgroundAudioSource.stop();
			}
		}
		mBackgroundAudioSource.unassign(hashCode());
		mBackgroundAudioSource = null;
	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);
		if (!pOtherScreenHasFocus && !pCoveredByOtherScreen) {

			final var lNextTrigger = mTriggerController.getNextTrigger();
			if (lNextTrigger != null) {
				switch (lNextTrigger.type) {
				case TriggerController.TRIGGER_TYPE_NEW_SCENE:
					final var lNextSceneEntryPoint = lNextTrigger.vars;

					final var targetParts = lNextSceneEntryPoint.split("\\.");
					if (targetParts == null || targetParts.length != 2) {
						Debug.debugManager().logger().e(getClass().getSimpleName(), "Could not resolve trigger to new scene");
						break;
					}

					final var lNextSceneHeader = mGameWorldHeader.getSceneByName(targetParts[0]);
					if (lNextSceneHeader == null) {
						break;
					}
					lNextSceneHeader.startEntryPointName(targetParts[1]);

					final var lLoadingScreen = new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), mInitialGameState, mGameWorldHeader, lNextSceneHeader));
					screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, lLoadingScreen));

					break;

				case TriggerController.TRIGGER_TYPE_GAME_LOST:
					// screenManager().addScreen(new GameLostScreen(screenManager(), entityGroupUid()));
					break;

				case TriggerController.TRIGGER_TYPE_GAME_WON:
					// screenManager().addScreen(new GameWonScreen(screenManager(), entityGroupUid()));
					break;

				case TriggerController.TRIGGER_TYPE_DIALOG:
					break;

				}

				lNextTrigger.consumed = true;
				mTriggerController.returnTrigger(lNextTrigger);

			}
		}
	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		if (mCameraMovementController != null)
			Debug.debugManager().drawers().drawRectImmediate(pCore.gameCamera(), mCameraMovementController.playArea());

		final var lHudBounds = pCore.HUD().boundingRectangle();

		final var lTitleFont = mRendererManager.uiTitleFont();
		lTitleFont.begin(pCore.HUD());
		lTitleFont.drawText("Scene: " + mSceneHeader.sceneName(), lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);
		lTitleFont.end();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected void createControllers(ControllerManager controllerManager) {
		mGameStateController = new GameStateController(controllerManager, mActiveGameState, mGameWorldHeader, entityGroupUid());
		mGameWorldController = new GameWorldController(controllerManager, mActiveGameScene, entityGroupUid());
		mTrackController = new TrackController(controllerManager, mActiveGameScene, entityGroupUid());
		mGameTrackEditorController = new GameTrackEditorController(controllerManager, screenManager(), entityGroupUid());
		mTrainController = new TrainController(controllerManager, mTrainManager, entityGroupUid());
		mPlayerTrainController = new PlayerTrainController(controllerManager, mTrainManager, entityGroupUid());
		mTriggerController = new TriggerController(controllerManager, entityGroupUid());

		mCameraMovementController = new GameCameraMovementController(controllerManager, mGameCamera, entityGroupUid());
		mCameraMovementController.setPlayArea(-1400, -1100, 2800, 2200);

		mCameraZooomController = new GameCameraZoomController(controllerManager, mGameCamera, entityGroupUid());
		mCameraZooomController.setZoomConstraints(400, 2400);

	}

	@Override
	protected void initializeControllers(LintfordCore core) {
		mGameStateController.initialize(core);
		mGameWorldController.initialize(core);
		mTrackController.initialize(core);
		mGameTrackEditorController.initialize(core);
		mTrainController.initialize(core);
		mPlayerTrainController.initialize(core);
		mTriggerController.initialize(core);

		mCameraMovementController.initialize(core);
		mCameraZooomController.initialize(core);

	}

	@Override
	protected void createRenderers(LintfordCore core) {
		mBackgroundRenderer = new GameBackgroundRenderer(rendererManager(), entityGroupUid());
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mGridRenderer = new GridRenderer(rendererManager(), entityGroupUid());
		mTrackRenderer = new TrackRenderer(rendererManager(), entityGroupUid());
		mTrackSignalRenderer = new TrackSignalRenderer(rendererManager(), entityGroupUid());
		mTrainRenderer = new TrainRenderer(rendererManager(), entityGroupUid());
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mTrackGhostRenderer = new TrackGhostRenderer(rendererManager(), entityGroupUid());

		mPlayerControlsRenderer = new PlayerControlsRenderer(rendererManager(), entityGroupUid());
		mTrainHudRenderer = new TrainHudRenderer(rendererManager(), entityGroupUid());
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mGameTrackEditorRenderer = new GameTrackEditorUiRenderer(rendererManager(), entityGroupUid());
		mGameStateUIRenderer = new GameStateUiRenderer(rendererManager(), entityGroupUid());
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mDebugTrackRenderer = new TrackDebugRenderer(rendererManager(), entityGroupUid());

	}

	@Override
	protected void initializeRenderers(LintfordCore core) {
		mBackgroundRenderer.initialize(core);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mGridRenderer.initialize(core);
		mTrackRenderer.initialize(core);
		mTrackSignalRenderer.initialize(core);
		mTrainRenderer.initialize(core);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mTrackGhostRenderer.initialize(core);

		mPlayerControlsRenderer.initialize(core);
		mTrainHudRenderer.initialize(core);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mGameTrackEditorRenderer.initialize(core);
		mGameStateUIRenderer.initialize(core);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mDebugTrackRenderer.initialize(core);

	}

	@Override
	protected void loadRendererResources(ResourceManager resourceManager) {
		mBackgroundRenderer.loadResources(resourceManager);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mGridRenderer.loadResources(resourceManager);
		mTrackRenderer.loadResources(resourceManager);
		mTrackSignalRenderer.loadResources(resourceManager);
		mTrainRenderer.loadResources(resourceManager);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mTrackGhostRenderer.loadResources(resourceManager);

		mPlayerControlsRenderer.loadResources(resourceManager);
		mTrainHudRenderer.loadResources(resourceManager);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mGameTrackEditorRenderer.loadResources(resourceManager);
		mGameStateUIRenderer.loadResources(resourceManager);
		if (ConstantsGame.DEBUG_EDITOR_IN_GAME)
			mDebugTrackRenderer.loadResources(resourceManager);

	}

}
