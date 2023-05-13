package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RailTrackSegmentSignalSaveDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 7822196200663715066L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final List<Integer> signalSegmentUids = new ArrayList<>();
	public int destinationUid;

}
