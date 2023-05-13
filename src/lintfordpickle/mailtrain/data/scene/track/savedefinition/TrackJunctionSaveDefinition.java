package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;

public class TrackJunctionSaveDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 5430644653264474791L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public boolean isSignalActive; // TODO: rename variable (globally)

	public int leftEdgeUid;
	public int rightEdgeUid;
	public int signalNodeUid;
	public boolean leftEnabled;

	public float lampOffsetX;
	public float lampOffsetY;

	public float boxOffsetX;
	public float boxOffsetY;

}
