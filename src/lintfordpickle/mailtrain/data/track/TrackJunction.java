package lintfordpickle.mailtrain.data.track;

public class TrackJunction {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public boolean isSignalActive; // not all nodes are walkable
	public int leftEdgeUid = -1;
	public int rightEdgeUid = -1;
	public int signalNodeUid = -1;
	public boolean leftEnabled;
	public float signalLampOffsetX = 0.f;
	public float signalLampOffsetY = 0.f;

	// TODO: Clickable part
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
}
