package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock.SignalState;

public class RailTrackSignalBlockSaveDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -5044459261292305725L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public int uid;

	public SignalState signalState;
	public final List<Integer> signalSegmentIndices = new ArrayList<>();

}
