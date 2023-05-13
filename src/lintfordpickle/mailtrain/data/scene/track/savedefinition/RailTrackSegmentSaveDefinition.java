package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RailTrackSegmentSaveDefinition implements Serializable {

	private static final long serialVersionUID = 621571731065042770L;

	// TrackEdge
	public int uid;

	public int nodeAUid;
	public float nodeAAngle;

	public int nodeBUid;
	public float nodeBAngle;

	public float control0X;
	public float control0Y;

	public float control1X;
	public float control1Y;

	// RailTrackSegment
	public float edgeLengthInMeters;

	public int edgeType;
	public int specialEdgeType;

	public float edgeAngle;

	public String segmentName;
	public String specialName;

	public final RailTrackSegmentSignalSaveDefinition signalsA = new RailTrackSegmentSignalSaveDefinition();
	public final RailTrackSegmentSignalSaveDefinition signalsB = new RailTrackSegmentSignalSaveDefinition();
	public int logicalUpdateCounter;

	public final List<Integer> allowedEdgeConnections = new ArrayList<>();
	
	// segment juntion
	public final TrackJunctionSaveDefinition trackJunction = new TrackJunctionSaveDefinition();

}
