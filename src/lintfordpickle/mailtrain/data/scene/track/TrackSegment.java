package lintfordpickle.mailtrain.data.scene.track;

public class TrackSegment {

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

	public TrackSegment() {

	}

	public TrackSegment(int uid) {
		this.uid = uid;
	}

	public TrackSegment(int uid, int nodeAUid, int nodeBUid) {
		this.uid = uid;

		this.nodeAUid = nodeAUid;
		this.nodeBUid = nodeBUid;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public float getNodeAngle(int nodeUid) {
		if (nodeUid == nodeAUid)
			return nodeAAngle;
		return nodeBAngle;
	}

	public void setNodesUids(int pNodeAUid, int nodeBUid) {
		nodeAUid = pNodeAUid;
		this.nodeBUid = nodeBUid;
	}
}
