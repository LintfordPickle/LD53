package lintfordpickle.mailtrain.data.world.scenes;

import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.trains.TrainManager;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;

public class GameScene {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private transient final GameWorldHeader mGameWorldHeader;
	private Track mTrack;

	private TrainManager mTrainManager;
	private ScenePropList mWorldScenery;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public GameWorldHeader gameWorldHeader() {
		return mGameWorldHeader;
	}

	public Track track() {
		return mTrack;
	};

	public void track(Track pTrack) {
		mTrack = pTrack;
	};

	public ScenePropList worldScenery() {
		return mWorldScenery;
	};

	public void worldScenery(ScenePropList pWorldScenery) {
		mWorldScenery = pWorldScenery;
	};

	public TrainManager trainManager() {
		return mTrainManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScene(GameWorldHeader pGameWorldHeader) {
		mGameWorldHeader = pGameWorldHeader;
		mTrainManager = new TrainManager();
	}

}
