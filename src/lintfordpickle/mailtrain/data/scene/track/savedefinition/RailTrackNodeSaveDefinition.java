package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;

public class RailTrackNodeSaveDefinition implements Serializable {

	private static final long serialVersionUID = 1348315922969346261L;
	
	public TrackSwitchSaveDefinition switchSaveDef = new TrackSwitchSaveDefinition();
	
	public int uid;
	
	public float x;
	public float y;

}
