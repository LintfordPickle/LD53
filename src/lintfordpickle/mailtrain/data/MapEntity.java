package lintfordpickle.mailtrain.data;

import java.io.Serializable;

public class MapEntity implements Serializable {
	
	private static final long serialVersionUID = 1035552683821758772L;

	public final int uid;

	public float x;
	public float y;

	public MapEntity(final int pUid) {
		uid = pUid;
	}
}
