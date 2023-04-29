package lintfordpickle.mailtrain.data;

import net.lintford.library.core.entity.instances.IndexedPooledBaseData;

public class GameState extends IndexedPooledBaseData {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -4614085384213757212L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private int mCredits;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int credits() {
		return mCredits;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameState(int pPoolUid) {
		super(pPoolUid);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void startNewGame(int pStartingCredits) {
		mCredits = pStartingCredits;
	}

	public void addCredits(int pAddAmount) {
		if (pAddAmount < 0)
			return;
		mCredits += pAddAmount;
	}

	public void deductCredits(int pDeductAmount) {
		if (pDeductAmount > 0)
			return;
		mCredits -= pDeductAmount;
	}

}
