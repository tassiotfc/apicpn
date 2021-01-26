package apicpn.models.simulation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.cpntools.accesscpn.engine.highlevel.HighLevelSimulator;
import org.cpntools.accesscpn.engine.highlevel.checker.Checker;
import org.cpntools.accesscpn.engine.highlevel.checker.ErrorInitializingSMLInterface;
import org.cpntools.accesscpn.engine.highlevel.instance.Binding;
import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.PlaceNode;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.accesscpn.model.importer.DOMParser;

public class Simulator {
	private HighLevelSimulator s;
	private SimulationMonitor simulationMonitor;
	
	public Simulator(String inputFileCPN, String reportDirectory) {
		try {
			simulationMonitor = new SimulationMonitor();
			PetriNet petriNet = DOMParser.parse(new URL("file://"+inputFileCPN));
			s = HighLevelSimulator.getHighLevelSimulator();
			s.setSimulationReportOptions(false, false, "");
			final Checker checker = new Checker(petriNet, null, s);
			try {
				checker.checkEntireModel(reportDirectory, reportDirectory);
				configureEnabledTransitions();
			} catch (final ErrorInitializingSMLInterface e) {
				// Ignore
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally {
			System.out.println("New simulator created!");
		}
	}
	
	public String getMarkingAndConvertToString(Instance<PlaceNode> pi) throws Exception{
		return s.getMarking(pi).toString();
	}
	
	public List<Instance<PlaceNode>> getAllPlaceInstances() throws Exception{
		return s.getAllPlaceInstances();
	}
	
	public int getTimeAndConvertToInt() throws Exception{
		return Integer.parseInt(s.getTime());
	}
	
	public void generateSimulationReport(String report) throws Exception {
		s.saveSimulationReport(report);
	}
			
	public String getPlaceMarking(String place) throws Exception{
		for(Instance<PlaceNode> pi: getAllPlaceInstances()) {
			if(pi.toString().contains(place)) {
				return s.getMarking(pi);
			}
		}
		return "empty";
	}
	
	public void enableTransitions(List<String> stopTransitions, List<String> exclusionTransitions) 
			throws Exception{
		if(!stopTransitions.isEmpty()){
			List<Instance<? extends Transition>> tisEnabled;
			while(true) {
				tisEnabled = getTransitionsEnabled();
				if(transitionsAreEnabled(tisEnabled, stopTransitions)) {
					break;
				}

				List<Instance<? extends Transition>> tisEnabledThatCanFire = 
						getTransitionsThatCanFire(tisEnabled, exclusionTransitions);
				
				List<Instance<Transition>> tisExecutaveis;
				Instance<? extends Transition> transition;
				if(!tisEnabledThatCanFire.isEmpty()) {
					tisExecutaveis = transitionsEnabledToExecutable(tisEnabledThatCanFire);
					transition = s.execute(tisExecutaveis);
				}
				else{
					transition = s.execute();
				}
				configureLastTransitionFired(transition);
			}
			configureEnabledTransitions();
		}
	}
	
	private List<Instance<? extends Transition>> getTransitionsEnabled() throws Exception {
		List<Instance<? extends Transition>> tisEnabled;
		tisEnabled = s.isEnabled(s.getAllTransitionInstances());
		if(tisEnabled.isEmpty()) {
			s.increaseTime();
		}
		tisEnabled = s.isEnabled(s.getAllTransitionInstances());
		return tisEnabled;
	}
	
	private boolean transitionsAreEnabled(List<Instance<? extends Transition>> tisEnabled, 
			List<String> stopTransitions) {
		int count = 0;
		for(String transition: stopTransitions) {
			for (Instance<? extends Transition> instance : tisEnabled) {
				if(instance.toString().contains(transition)) {
					count++;
				}
			}
		}
		if(count == stopTransitions.size()) {
			return true;
		}
		return false;
	}
	
	private List<Instance<? extends Transition>> getTransitionsThatCanFire(
			List<Instance<? extends Transition>> tisEnabled, 
			List<String> exclusionTransitions){
		
		boolean canFire;
		List<Instance<? extends Transition>> tisEnabledThatCanFire = new ArrayList<Instance<? extends Transition>>();
		for (Instance<? extends Transition> instance : tisEnabled) {
			canFire = true;
			for(String transition: exclusionTransitions) {
				if(instance.toString().contains(transition)) {
					canFire = false;
					break;
				}
			}
			if(canFire) {
				tisEnabledThatCanFire.add(instance);
			}
		}
		return tisEnabledThatCanFire;
	}
	
	@SuppressWarnings("unchecked")
	private List<Instance<Transition>> transitionsEnabledToExecutable
	(List<Instance<? extends Transition>> tisEnabled) throws Exception{
		List<Instance<Transition>> tisExecutable = new ArrayList<Instance<Transition>>();
		for (Instance<? extends Transition> instance : tisEnabled) {
			tisExecutable.add((Instance<Transition>) instance);
		}
		return tisExecutable;
	}
	
	public boolean fireTransitions(List<String> transitionsToFire) throws Exception{
		int count = 0;
		for (String transition : transitionsToFire) {
			List<Instance<? extends Transition>> tisEnabled = s.isEnabled(s.getAllTransitionInstances());
			for (Instance<? extends Transition> instance : tisEnabled) {
				if(instance.toString().contains(transition)) {
					s.execute(instance);
					configureEnabledTransitions();
					configureLastTransitionFired(instance);
					count++;
					break;
				}
			}
		}
		return transitionsToFire.size() == count ? true : false;
	}
	
	private void configureLastTransitionFired(Instance<? extends Transition> transition) {
		simulationMonitor.setLastTransitionFired(transition.toString());
	}
	
	public boolean destroySimulator() {
		if(s != null) {
			s.destroy();
			return true;
		}
		return false;
	}
	
	public SimulationMonitor getSimulationMonitor() {
		return simulationMonitor;
	}
	
	public Binding getBinding(String t, String binding) throws Exception{
		List<Instance<? extends Transition>> transitions = new ArrayList<Instance<? extends Transition>>();
		for (Instance<? extends Transition> instance : s.isEnabled(s.getAllTransitionInstances())) {
			if(instance.toString().contains(t)) {
				transitions.add(instance);
			}
		}
		for (Instance<? extends Transition> instance : transitions) {
			for (Binding b: s.getBindings(instance)) {
				if(b.toString().contains(binding)) {
					return b;
				}
			}
		}
		return null;
	}
	
	public void executeBinding(Binding b) throws Exception{
		s.execute(b);
		configureEnabledTransitions();
		configureLastTransitionFired(b);
	}
		
	private void configureEnabledTransitions() throws Exception{
		simulationMonitor.setEnabledTransitions(transitionsEnabledToString(
				s.isEnabled(s.getAllTransitionInstances())));
	}
	
	private List<String> transitionsEnabledToString(List<Instance<? extends Transition>> tis) 
			throws IOException, Exception{
		
		List<String> transicoes = new ArrayList<String>();
		for (Instance<? extends Transition> instance : tis) {
			transicoes.add(instance.toString());
		}
		return transicoes;
	}
	
	private void configureLastTransitionFired(Binding binding) {
		simulationMonitor.setLastTransitionFired(binding.toString());
	}
	
	public boolean transitionIsEnabled(String transition)
	{
		for(String transitionAux: simulationMonitor.getEnabledTransitions()) {
			if(transitionAux.contains(transition)) {
				return true;
			}
		}
		return false;
	}
}