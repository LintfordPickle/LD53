package lintfordpickle.mailtrain.data.scene.track;

public class TrackEdge {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public int uid;
	public transient boolean isSelected;

	public int nodeAUid;
	public float nodeAAngle;

	public int nodeBUid;
	public float nodeBAngle;

	public float control0X;
	public float control0Y;

	public float control1X;
	public float control1Y;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrackEdge() {

	}

	public TrackEdge(int pUid) {
		uid = pUid;
	}

	public TrackEdge(int pUid, int pNodeAUid, int pNodeBUid) {
		uid = pUid;

		nodeAUid = pNodeAUid;
		nodeBUid = pNodeBUid;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public float getNodeAngle(int pNodeUid) {
		if (pNodeUid == nodeAUid)
			return nodeAAngle;
		return nodeBAngle;
	}

	public void setNodesUids(int pNodeAUid, int pNodeBUid) {
		nodeAUid = pNodeAUid;
		nodeBUid = pNodeBUid;
	}
}
