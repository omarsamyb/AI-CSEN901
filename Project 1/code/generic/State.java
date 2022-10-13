package code.generic;

public abstract class State {
	public abstract String toString();
	public abstract void setStateVisited();
	public abstract void setStateVisited2();
	public abstract boolean stateExists();
	public abstract boolean stateExists2();
	public abstract void resetState();
}
