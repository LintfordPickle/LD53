package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RailTrackNodeSaveDefinition implements Serializable {

	private static final long serialVersionUID = 1348315922969346261L;
	
	public List<Integer> connectedEdgeUids = new ArrayList<>();
	
	public int uid;
	
	public float x;
	public float y;

}
