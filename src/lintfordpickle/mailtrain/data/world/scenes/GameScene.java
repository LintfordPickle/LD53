package lintfordpickle.mailtrain.data.world.scenes;

import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;

public class GameScene {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private transient final GameWorldHeader mGameWorldHeader;
	private transient GameScene mActiveGameSceneHeader;

	private Track mTrack;
	private Props mProps;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public GameWorldHeader gameWorldHeader() {
		return mGameWorldHeader;
	}

	public GameScene activeSceneHeader() {
		return mActiveGameSceneHeader;
	}

	public Track track() {
		return mTrack;
	};

	public void track(Track pTrack) {
		mTrack = pTrack;
	};

	public Props props() {
		return mProps;
	};

	public void props(Props props) {
		mProps = props;
	};

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScene(GameWorldHeader pGameWorldHeader) {
		mGameWorldHeader = pGameWorldHeader;

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void LoadGameScene(SceneHeader sceneHeader) {

	}

}
