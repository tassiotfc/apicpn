package apicpn.models.simulation;

import java.util.List;

public class SimulationMonitor {
	private List<String> enabledTransitions;
	private String lastTransitionFired;
	
	public List<String> getEnabledTransitions() {
		return enabledTransitions;
	}
	public void setEnabledTransitions(List<String> enabledTransitions) {
		this.enabledTransitions = enabledTransitions;
	}
	public String getLastTransitionFired() {
		return lastTransitionFired;
	}
	public void setLastTransitionFired(String lastTransitionFired) {
		this.lastTransitionFired = lastTransitionFired;
	}
}