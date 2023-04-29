package lintfordpickle.mailtrain.screens.game;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.GameStateController;
import lintfordpickle.mailtrain.controllers.GameTrackEditorController;
import lintfordpickle.mailtrain.controllers.TrainController;
import lintfordpickle.mailtrain.controllers.core.GameCameraMovementController;
import lintfordpickle.mailtrain.controllers.core.GameCameraZoomController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.controllers.tracks.TrackIOController;
import lintfordpickle.mailtrain.controllers.world.GameWorldController;
import lintfordpickle.mailtrain.controllers.world.WorldIOController;
import lintfordpickle.mailtrain.data.GameState;
import lintfordpickle.mailtrain.data.GameWorld;
import lintfordpickle.mailtrain.data.GameWorldHeader;
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
	private GameState mGameState;
	private GameWorld mGameWorld;
	private GameWorldHeader mGameWorldHeader;

	// Controllers
	private GameCameraMovementController mCameraMovementController;
	private GameCameraZoomController mCameraZooomController;
	private GameStateController mGameStateController;
	private GameWorldController mGameWorldController;
	private TrainController mTrainController;
	private TrackController mTrackController;

	// TODO: something not right here - why is the game instantiating editor controllers ?
	private GameTrackEditorController mGameTrackEditorController;

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

	public GameScreen(ScreenManager screenManager, GameWorldHeader gameWorldHeader) {
		super(screenManager);

		mShowBackgroundScreens = true;

		mGameWorldHeader = gameWorldHeader;
		mGameWorld = new GameWorld(mGameWorldHeader);
		mGameState = new GameState(0);

		var lTrackToLoad = TrackIOController.loadTrackFromFile(mGameWorldHeader.trackFilename());
		var lWorldScenery = WorldIOController.loadSceneryFromFile("res/scenery/sceneryTest.json" /* mTrackHeader.sceneryFilename() */);

		mGameWorld.track(lTrackToLoad);
		mGameWorld.worldScenery(lWorldScenery);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		final var lCore = screenManager().core();
		final var lControllerManager = lCore.controllerManager();

		createControllers(lControllerManager);
		initializeControllers(lCore);
		lControllerManager.initializeControllers(lCore);

		mGameWorldController.startNewGame();
		mGameState.startNewGame(300000); // 300000 ms = 5 mins

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
				final var lLoadingScreen = new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), mGameWorldHeader));
				screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, lLoadingScreen));
				return;
			}

			screenManager().addScreen(new PauseScreen(screenManager(), mGameWorldHeader));

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
//			if (mGameStateController.getHasWon()) {
//				screenManager().addScreen(new GameWonScreen(screenManager(), entityGroupUid()));
//				return;
//			}
//			
//			if (mGameStateController.getHasLost()) {
//				screenManager().addScreen(new GameLostScreen(screenManager(), entityGroupUid()));
//				return;
//			}
		}
	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		if (mCameraMovementController != null)
			Debug.debugManager().drawers().drawRectImmediate(pCore.gameCamera(), mCameraMovementController.playArea());
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected void createControllers(ControllerManager controllerManager) {
		mGameStateController = new GameStateController(controllerManager, mGameState, mGameWorldHeader, entityGroupUid());
		mGameWorldController = new GameWorldController(controllerManager, mGameWorld, entityGroupUid());
		mTrackController = new TrackController(controllerManager, mGameWorld, entityGroupUid());
		mGameTrackEditorController = new GameTrackEditorController(controllerManager, screenManager(), entityGroupUid());
		mTrainController = new TrainController(controllerManager, mGameWorld, entityGroupUid());

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

		mCameraMovementController.initialize(core);
		mCameraZooomController.initialize(core);

	}

	@Override
	protected void createRenderers(LintfordCore core) {
		mBackgroundRenderer = new GameBackgroundRenderer(rendererManager(), entityGroupUid());
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME) mGridRenderer = new GridRenderer(rendererManager(), entityGroupUid());
		mTrackRenderer = new TrackRenderer(rendererManager(), entityGroupUid());
		mTrackSignalRenderer = new TrackSignalRenderer(rendererManager(), entityGroupUid());
		mTrainRenderer = new TrainRenderer(rendererManager(), entityGroupUid());
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME) mTrackGhostRenderer = new TrackGhostRenderer(rendererManager(), entityGroupUid());

		mPlayerControlsRenderer = new PlayerControlsRenderer(rendererManager(), entityGroupUid());
		mTrainHudRenderer = new TrainHudRenderer(rendererManager(), entityGroupUid());
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME) mGameTrackEditorRenderer = new GameTrackEditorUiRenderer(rendererManager(), entityGroupUid());
		mGameStateUIRenderer = new GameStateUiRenderer(rendererManager(), entityGroupUid());
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME) mDebugTrackRenderer = new TrackDebugRenderer(rendererManager(), entityGroupUid());

	}

	@Override
	protected void initializeRenderers(LintfordCore core) {
		mBackgroundRenderer.initialize(core);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME) mGridRenderer.initialize(core);
		mTrackRenderer.initialize(core);
		mTrackSignalRenderer.initialize(core);
		mTrainRenderer.initialize(core);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME)mTrackGhostRenderer.initialize(core);

		mPlayerControlsRenderer.initialize(core);
		mTrainHudRenderer.initialize(core);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME)mGameTrackEditorRenderer.initialize(core);
		mGameStateUIRenderer.initialize(core);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME)mDebugTrackRenderer.initialize(core);

	}

	@Override
	protected void loadRendererResources(ResourceManager resourceManager) {
		mBackgroundRenderer.loadResources(resourceManager);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME) mGridRenderer.loadResources(resourceManager);
		mTrackRenderer.loadResources(resourceManager);
		mTrackSignalRenderer.loadResources(resourceManager);
		mTrainRenderer.loadResources(resourceManager);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME)mTrackGhostRenderer.loadResources(resourceManager);

		mPlayerControlsRenderer.loadResources(resourceManager);
		mTrainHudRenderer.loadResources(resourceManager);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME)mGameTrackEditorRenderer.loadResources(resourceManager);
		mGameStateUIRenderer.loadResources(resourceManager);
		if(ConstantsGame.DEBUG_EDITOR_IN_GAME)mDebugTrackRenderer.loadResources(resourceManager);

	}

}
