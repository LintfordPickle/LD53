package lintfordpickle.mailtrain.data;

import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.trains.TrainManager;
import lintfordpickle.mailtrain.data.world.WorldScenery;

public class GameWorld {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private transient final GameWorldHeader mGameWorldHeader;
	private Track mTrack;

	private TrainManager mTrainManager;
	private WorldScenery mWorldScenery;

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

	public WorldScenery worldScenery() {
		return mWorldScenery;
	};

	public void worldScenery(WorldScenery pWorldScenery) {
		mWorldScenery = pWorldScenery;
	};

	public TrainManager trainManager() {
		return mTrainManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameWorld(GameWorldHeader pGameWorldHeader) {
		mGameWorldHeader = pGameWorldHeader;
		mTrainManager = new TrainManager();
	}

}
