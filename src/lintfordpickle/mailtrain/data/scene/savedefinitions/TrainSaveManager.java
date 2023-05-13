package lintfordpickle.mailtrain.data.scene.savedefinitions;

import java.io.Serializable;

import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSaveDefinition;

public class TrainSaveManager implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 8857512648781360683L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final RailTrackSaveDefinition track = new RailTrackSaveDefinition();

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void storeTrackDefinitions(RailTrackInstance track) {

	}
}
