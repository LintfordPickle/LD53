package lintfordpickle.mailtrain.data.scene.savedefinitions;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import lintfordpickle.mailtrain.data.scene.grid.HashGridSaveManager;

public class GameSceneSaveDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 7254811379004868429L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	@SerializedName(value = "SceneName")
	private String mTrackName;

	@SerializedName(value = "Track")
	private final TrackSaveManager mTrackManagerSaveManager = new TrackSaveManager();

	@SerializedName(value = "Trains")
	private final TrainSaveManager mTrainManagerSaveManager = new TrainSaveManager();

	@SerializedName(value = "Environment")
	private final EnvironmentSaveManager mEnvironmentManagerSaveManager = new EnvironmentSaveManager();

	@SerializedName(value = "HashGrid")
	private final HashGridSaveManager mHashGridSaveManager = new HashGridSaveManager();

	@SerializedName(value = "Triggers")
	private final TriggersSaveManager mTriggersSaveManager = new TriggersSaveManager();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public String trackName() {
		return mTrackName;
	}

	public void trackName(String trackName) {
		mTrackName = trackName;
	}

	public TrackSaveManager trackSaveManager() {
		return mTrackManagerSaveManager;
	}

	public TrainSaveManager trainSaveManager() {
		return mTrainManagerSaveManager;
	}

	public EnvironmentSaveManager environmentSaveManagerSaveDefinition() {
		return mEnvironmentManagerSaveManager;
	}

	public HashGridSaveManager hashGridSaveManager() {
		return mHashGridSaveManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameSceneSaveDefinition() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public boolean isSceneDefinitionValid() {
		// TODO: Unimplemented method
		return true;
	}

}
