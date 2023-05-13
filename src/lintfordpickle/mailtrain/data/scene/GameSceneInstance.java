package lintfordpickle.mailtrain.data.scene;

import lintfordpickle.mailtrain.data.events.GameEventManager;
import lintfordpickle.mailtrain.data.scene.environment.EnvironmentManager;
import lintfordpickle.mailtrain.data.scene.grid.HashGridManager;
import lintfordpickle.mailtrain.data.scene.savedefinitions.GameSceneSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.TrackManager;
import lintfordpickle.mailtrain.data.scene.trains.TrainManager;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;

public class GameSceneInstance {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private transient final GameWorldHeader mGameWorldHeader;

	private TrackManager mTrackManager;
	private TrainManager mTrainManager;
	private EnvironmentManager mEnvironmentManager;
	private GameEventManager mGameEventManager;
	private HashGridManager mHashGridManager;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public GameWorldHeader gameWorldHeader() {
		return mGameWorldHeader;
	}

	public TrackManager trackManager() {
		return mTrackManager;
	}

	public TrainManager trainManager() {
		return mTrainManager;
	}

	public GameEventManager gameEventManager() {
		return mGameEventManager;
	}

	public HashGridManager hashGridManager() {
		return mHashGridManager;
	}

	public EnvironmentManager props() {
		return mEnvironmentManager;
	};

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameSceneInstance(GameWorldHeader pGameWorldHeader) {
		mGameWorldHeader = pGameWorldHeader;

		mTrackManager = new TrackManager();
		mTrainManager = new TrainManager();
		mGameEventManager = new GameEventManager();
		mHashGridManager = new HashGridManager();
		mEnvironmentManager = new EnvironmentManager();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	public void initializeScene() {
		mTrackManager.initializeManager();
		mTrainManager.initializeManager();
		mGameEventManager.initializeManager();
		mHashGridManager.initializeManager();
		mEnvironmentManager.initializeManager();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void createNewScene() {

	}

	public GameSceneSaveDefinition getSceneSaveDefinition() {
		final var lSceneDefinition = new GameSceneSaveDefinition();

		mTrackManager.storeInSceneDefinition(lSceneDefinition);
		mTrainManager.storeInSceneDefinition(lSceneDefinition);
		mGameEventManager.storeInSceneDefinition(lSceneDefinition);
		mHashGridManager.storeInSceneDefinition(lSceneDefinition);
		mEnvironmentManager.storeInSceneDefinition(lSceneDefinition);

		return lSceneDefinition;

	}

	public void loadGameScene(GameSceneSaveDefinition sceneDefinition) {
		mTrackManager.loadFromSceneDefinition(sceneDefinition);
		mTrainManager.loadFromSceneDefinition(sceneDefinition);
		mGameEventManager.loadFromSceneDefinition(sceneDefinition);
		mHashGridManager.loadFromSceneDefinition(sceneDefinition);
		mEnvironmentManager.loadFromSceneDefinition(sceneDefinition);
	}

	public void finalizeAfterLoading() {
		mTrackManager.finalizeAfterLoading(this);
		mTrainManager.finalizeAfterLoading(this);
		mGameEventManager.finalizeAfterLoading(this);
		mHashGridManager.finalizeAfterLoading(this);
		mEnvironmentManager.finalizeAfterLoading(this);
	}

}
