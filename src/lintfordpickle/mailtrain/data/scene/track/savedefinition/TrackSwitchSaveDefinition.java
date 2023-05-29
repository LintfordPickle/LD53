package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrackSwitchSaveDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 5430644653264474791L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public int mainSegmentLocalIndex;
	public List<Integer> connectedSegmentUids = new ArrayList<>();
	public int activeAuxiliarySegmentLocalIndex;

	public float boxOffsetX;
	public float boxOffsetY;

}
