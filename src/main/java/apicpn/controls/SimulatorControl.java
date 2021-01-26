package apicpn.controls;

import java.io.File;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apicpn.models.DAO.CPNDAO;
import apicpn.models.simulation.BindingDescription;
import apicpn.models.simulation.SimulationMonitor;
import apicpn.models.simulation.SimulationRestrictor;
import apicpn.models.simulation.Simulator;

import java.io.FileInputStream;

import org.cpntools.accesscpn.engine.highlevel.instance.Binding;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(value="simulation")
public class SimulatorControl {
	
	Simulator simulator;
	
	//definir serviço de upload da rede na nuvem
	
	@GetMapping("/createsimulator")
	public SimulationMonitor createSimulator() throws Exception  {
		CPNDAO cpndao = new CPNDAO("PumpAccuV2.cpn"); 	// substituir pela leitura do diretório da rede 
														// no endereço da nuvem no qual a rede estará
														// após o upload
		simulator = new Simulator(cpndao.getFileCPN(), "");
		return simulator.getSimulationMonitor();
	}
	
	@PostMapping("/enabletransitions")
	public SimulationMonitor enableTransitions(@RequestBody SimulationRestrictor simulationRestrictor) throws Exception  {
		simulator.enableTransitions(simulationRestrictor.getStopTransitions(), simulationRestrictor.getExclusionTransitions());
		return simulator.getSimulationMonitor();
	}
	
	@PostMapping("/firetransitions")
	public SimulationMonitor fireTransitions(@RequestBody List<String> transitionsToFire) throws Exception  {
		simulator.fireTransitions(transitionsToFire);
		return simulator.getSimulationMonitor();
	}
	
	@PostMapping("/firebinding")
	public SimulationMonitor fireBinding(@RequestBody BindingDescription bindingDescription) throws Exception  {
		Binding b = simulator.getBinding(bindingDescription.getTransition(), bindingDescription.getBinding()); //exemplo: "Current_Value", "OkB = 0, b = 1"
		simulator.executeBinding(b);
		return simulator.getSimulationMonitor();
	}
	
	@PostMapping("/transitionisenabled")
	public boolean transitionIsEnabled(@RequestBody String transition) throws Exception  {
		return simulator.transitionIsEnabled(transition);
	}
		
	@GetMapping("/finish")
	public boolean finishSimulation() throws Exception 
	{
		return simulator.destroySimulator();
	}
		
	@GetMapping("/downloadreport")
	public ResponseEntity<Object> downloadSimulationReport() throws Exception 
	{
		String filename = "/home/tassio/0";
		File file = new File(filename);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
			
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition",
				String.format("attachment; filename=\"%s\"", file.getName()));
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers)
				.contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/txt")).body(resource);

		return responseEntity;
	}
}