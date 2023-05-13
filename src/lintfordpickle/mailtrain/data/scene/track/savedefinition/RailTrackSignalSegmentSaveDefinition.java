package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;

public class RailTrackSignalSegmentSaveDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -1844209045342751794L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public int uid;

	// references
	public int trackSegmentUid;
	public int signalBlockUid;

	public boolean isSignalHead;

	public int destinationNodeUid;
	public float startDistance;
	public float length;

}