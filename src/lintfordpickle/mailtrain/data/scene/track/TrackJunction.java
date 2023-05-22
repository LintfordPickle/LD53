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
	public float signalLampWorldX = 0.f;
	public float signalLampWorldY = 0.f;

	// Clickable part
	public float signalBoxWorldX = 0.f;
	public float signalBoxWorldY = 0.f;

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

		signalLampWorldX = saveDef.lampOffsetX;
		signalLampWorldY = saveDef.lampOffsetY;

		signalBoxWorldX = saveDef.boxOffsetX;
		signalBoxWorldY = saveDef.boxOffsetY;
	}

	public void saveIntoDef(TrackJunctionSaveDefinition saveDef) {
		saveDef.isSignalActive = isSignalActive;
		saveDef.leftEdgeUid = leftEdgeUid;
		saveDef.rightEdgeUid = rightEdgeUid;
		saveDef.leftEnabled = leftEnabled;
		saveDef.signalNodeUid = signalNodeUid;

		saveDef.lampOffsetX = signalLampWorldX;
		saveDef.lampOffsetY = signalLampWorldY;

		saveDef.boxOffsetX = signalBoxWorldX;
		saveDef.boxOffsetY = signalBoxWorldY;
	}
}
