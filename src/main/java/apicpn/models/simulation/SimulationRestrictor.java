package apicpn.models.simulation;

import java.util.List;

public class SimulationRestrictor {
	private List<String> stopTransitions;
	private List<String> exclusionTransitions;
	private List<String> transitionsToFire;
	
	public List<String> getStopTransitions() {
		return stopTransitions;
	}
	public void setStopTransitions(List<String> stopTransitions) {
		this.stopTransitions = stopTransitions;
	}
	public List<String> getExclusionTransitions() {
		return exclusionTransitions;
	}
	public void setExclusionTransitions(List<String> exclusionTransitions) {
		this.exclusionTransitions = exclusionTransitions;
	}
	public List<String> getTransitionsToFire() {
		return transitionsToFire;
	}
	public void setTransitionsToFire(List<String> transitionsToFire) {
		this.transitionsToFire = transitionsToFire;
	}
}
