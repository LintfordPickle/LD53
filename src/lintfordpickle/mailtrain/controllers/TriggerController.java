package lintfordpickle.mailtrain.controllers;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;

public class TriggerController extends BaseController {

	public class Trigger {
		public int type;
		public int vari;
		public String vars;

		public boolean isFree;
		public boolean consumed;

		public Trigger() {
			reset();
		}

		public void reset() {
			type = -1;
			vari = -1;
			vars = null;
			consumed = false;
			isFree = true;
		}

	}

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Trigger Controller";

	private static final int MAX_TRIGGERS = 8;

	public static final int TRIGGER_TYPE_NEW_SCENE = 10;
	public static final int TRIGGER_TYPE_DIALOG = 20;

	public static final int TRIGGER_TYPE_GAME_WON = 30;
	public static final int TRIGGER_TYPE_GAME_LOST = 31;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private final List<Trigger> triggerQueuePool = new ArrayList<>();
	private final List<Trigger> triggerQueue = new ArrayList<>();

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TriggerController(ControllerManager controllerManager, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		for (int i = 0; i < MAX_TRIGGERS; i++) {
			triggerQueuePool.add(new Trigger());
		}

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void setTrigger(int triggerTypeUid, int int_arg, String string_arg) {
		final var trig = getNextFreeTrigger();

		trig.reset();

		trig.type = triggerTypeUid;
		trig.vari = int_arg;
		trig.vars = string_arg;

	}

	public Trigger getNextTrigger() {
		if (triggerQueue.size() > 0) {
			final var t = triggerQueue.remove(0);
			t.consumed = true;
			return t;
		}

		return null;
	}

	private Trigger getNextFreeTrigger() {
		final int MAX_TRIGGERS = 8;
		for (int i = 0; i < MAX_TRIGGERS; i++) {
			if (triggerQueuePool.get(i).isFree) {
				final var t = triggerQueuePool.get(i);

				if (t.isFree == false)
					continue;

				triggerQueue.add(t);

				t.isFree = false;
				return t;
			}
		}

		return null;
	}

	public void returnTrigger(Trigger t) {
		if (t == null)
			return;

		t.reset();

		if (triggerQueue.contains(t))
			triggerQueue.remove(t);
	}

}
