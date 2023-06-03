package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;

public class RailTrackSegmentSignalSaveDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 7822196200663715066L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public int primarySignalSegmentUid;
	public int auxiliarySignalSegmentUid;
	
	public int destinationUid;

}
