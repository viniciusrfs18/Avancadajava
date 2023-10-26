package Trabalho.Rotas;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import Trabalho.MobilityCompany.Company;
import Trabalho.Banco.AlphaBank;
import Trabalho.Driver.Driver;
import Trabalho.Driver.CriaMotorista;
import Trabalho.Posto.FuelStation;
import Trabalho.Rotas.Route;
import Trabalho.MobilityCompany.Company;
import Trabalho.Rotas.Start;
import it.polito.appeal.traci.SumoTraciConnection;
//usando o Scheduled
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EnvSimulator extends Thread {
    private SumoTraciConnection sumo;
	private static String host;
	private static int portaCompany;
	private static int portaAlphaBank;
	private static long acquisitionRate;
	private static int numDrivers;
	private static String rotasXML;
	private ScheduledExecutorService sumoExecutor;

    public EnvSimulator() {
		
		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		
		
		host = "localhost";
		portaCompany = 52341
		portaAlphaBank = 54321;
		acquisitionRate = 500;
		numDrivers = 100;
		rotasXML = "map/map.rou.alt.xml";// pegar por aqui
		sumoExecutor = Executors.newScheduledThreadPool(1);
		
	}

    public void run() {

		// Start e configurações inicias do SUMO
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

		try {

			sumo.runServer(12345);//porta sumo

			System.out.println("Servidor do SUMO conectado.");
			
			Thread.sleep(3000); 
			
			Start execSimulador = new Start(this.sumo, acquisitionRate);
			execSimulador.start();

			ServerSocket alphaBankServer = new ServerSocket(portaAlphaBank);
			AlphaBank alphaBank = new AlphaBank(alphaBankServer);
			alphaBank.start();

			Thread.sleep(2000); 

			FuelStation fuelStation = new FuelStation(portaAlphaBank, host);
			fuelStation.start();

			ServerSocket companyServer = new ServerSocket(portaCompany);
			ArrayList<Route> rotas = Route.criaRotas(rotasXML);
			Company company = new Company(companyServer, rotas, numDrivers,  portaAlphaBank, host);
			company.start();

			// Roda o metodo join em todos os Drivers, espera todos os drivers terminarem a execução
			ArrayList<Driver> drivers = CriaMotorista.criaDrivers(numDrivers, fuelStation, acquisitionRate, sumo, host, portaCompany, portaAlphaBank);
			
			AtualizaDriver(drivers);

			for(int i = 0; i < drivers.size(); i++) {
				drivers.get(i).start();
				Thread.sleep(500);
			}
			
			for(int i = 0; i < drivers.size(); i++) {
				drivers.get(i).join();
			}

			companyServer.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
		//Iniciamos um ScheduledExecutorService na construção do EnvSimulator.
		//	Dentro do método run, programamos o executor para avançar a simulação SUMO em intervalos regulares.
		//	No final da simulação (ou em caso de erros), encerramos o executor para liberar recursos.
		//
		//
		// Inicializa o executor para avançar a simulação SUMO em intervalos regulares.
        sumoExecutor.scheduleAtFixedRate(() -> {
            try {
                sumo.do_timestep();
            } catch (Exception e) {
                e.printStackTrace();
                sumoExecutor.shutdownNow(); // Encerra o executor em caso de exceção.
            }
        }, 0, acquisitionRate, TimeUnit.MILLISECONDS);      

        sumoExecutor.shutdown();
        try {
            if (!sumoExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                sumoExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            sumoExecutor.shutdownNow();
        }

        System.out.println("Encerrando o EnvSimulator");
    }

    
}
