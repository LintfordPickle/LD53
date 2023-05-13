package lintfordpickle.mailtrain.data.scene.trains;

import java.io.Serializable;

import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;

public class TrainFollowEdge implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -2338419397179933367L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public RailTrackSegment edge;
	public int targetNodeUid;
	public int logicalCounter;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrainFollowEdge() {

	}

	public TrainFollowEdge(RailTrackSegment pEdge, int pDestNodeUid, int pTicker) {
		edge = pEdge;
		targetNodeUid = pDestNodeUid;

		logicalCounter = pTicker;

	}

}
