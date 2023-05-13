package lintfordpickle.mailtrain.data.scene.track;

import lintfordpickle.mailtrain.data.scene.track.savedefinition.TrackJunctionSaveDefinition;

// 2-Way junction (l/r)
public class TrackJunction {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public boolean isSignalActive; // not all nodes are walkable
	public int leftEdgeUid = -1;
	public int rightEdgeUid = -1;
	public int signalNodeUid = -1;
	public boolean leftEnabled;

	// Visual part
	public float signalLampOffsetX = 0.f;
	public float signalLampOffsetY = 0.f;

	// Clickable part
	public float signalBoxOffsetX = 0.f;
	public float signalBoxOffsetY = 0.f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public void toggleSignal() {
		leftEnabled = !leftEnabled;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackJunction() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void init(int pNodeUid, int pLeftEdgeUid, int pRightEdgeUid) {
		if (pLeftEdgeUid == -1 || pRightEdgeUid == -1) {
			reset();
			return;
		}

		isSignalActive = true;
		signalNodeUid = pNodeUid;
		leftEdgeUid = pLeftEdgeUid;
		rightEdgeUid = pRightEdgeUid;
	}

	public void reset() {
		isSignalActive = false;
		leftEdgeUid = -1;
		signalNodeUid = -1;
		rightEdgeUid = -1;
	}

	public void loadFromDef(TrackJunctionSaveDefinition saveDef) {
		isSignalActive = saveDef.isSignalActive;

		leftEdgeUid = saveDef.leftEdgeUid;
		rightEdgeUid = saveDef.rightEdgeUid;
		leftEnabled = saveDef.leftEnabled;
		signalNodeUid = saveDef.signalNodeUid;

		signalLampOffsetX = saveDef.lampOffsetX;
		signalLampOffsetY = saveDef.lampOffsetY;

		signalBoxOffsetX = saveDef.boxOffsetX;
		signalBoxOffsetY = saveDef.boxOffsetY;
	}

	public void saveIntoDef(TrackJunctionSaveDefinition saveDef) {
		saveDef.isSignalActive = isSignalActive;
		saveDef.leftEdgeUid = leftEdgeUid;
		saveDef.rightEdgeUid = rightEdgeUid;
		saveDef.leftEnabled = leftEnabled;
		saveDef.signalNodeUid = signalNodeUid;

		saveDef.lampOffsetX = signalLampOffsetX;
		saveDef.lampOffsetY = signalLampOffsetY;

		saveDef.boxOffsetX = signalBoxOffsetX;
		saveDef.boxOffsetY = signalBoxOffsetY;
	}
}
