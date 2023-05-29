package lintfordpickle.mailtrain.data.scene.track;

import lintfordpickle.mailtrain.data.MapEntity;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackNodeSaveDefinition;

public class RailTrackNode extends MapEntity {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final TrackSwitch trackSwitch;

	public transient boolean isSelected;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public void addSegmentToNode(RailTrackSegment segment) {
		trackSwitch.addSegmentToSwitch(segment);
	}

	public void removeSegmentByUid(int segmentUid) {
		trackSwitch.removeSegmentByUid(segmentUid);
	}

	public boolean getIsEndNode() {
		return trackSwitch.getIsEndNode();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public RailTrackNode(final int uid) {
		super(uid);

		trackSwitch = new TrackSwitch(this);
	}

	public RailTrackNode(final RailTrackNodeSaveDefinition saveDef) {
		this(saveDef.uid);

		this.x = saveDef.x;
		this.y = saveDef.y;

		trackSwitch.loadFromDef(saveDef.switchSaveDef);
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public void init(float worldX, float worldY) {
		x = worldX;
		y = worldY;

		trackSwitch.signalBoxWorldX = worldX;
		trackSwitch.signalBoxWorldY = worldY;
	}

	// ---------------------------------------------
	// IO Methods
	// ---------------------------------------------

	public void loadFromDef(RailTrackNodeSaveDefinition saveDef) {
		uid = saveDef.uid;
		x = saveDef.x;
		y = saveDef.y;

		trackSwitch.loadFromDef(saveDef.switchSaveDef);
	}

	public void saveIntoDef(RailTrackNodeSaveDefinition saveDef) {
		saveDef.uid = uid;
		saveDef.x = x;
		saveDef.y = y;

		trackSwitch.saveIntoDef(saveDef.switchSaveDef);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void finalizeAfterLoading(RailTrackInstance railTrackInstance) {
		trackSwitch.finalizeAfterLoading(railTrackInstance);
	}
}
