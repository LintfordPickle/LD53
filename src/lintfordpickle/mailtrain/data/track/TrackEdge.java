package lintfordpickle.mailtrain.data.track;

import net.lintford.library.core.entity.BaseInstanceData;

public class TrackEdge extends BaseInstanceData {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 6646831680311169604L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final int uid;
	public boolean isSelected;

	public int nodeAUid;
	public float nodeAAngle;

	public int nodeBUid;
	public float nodeBAngle;

	public float lControl0X;
	public float lControl0Y;

	public float lControl1X;
	public float lControl1Y;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

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
