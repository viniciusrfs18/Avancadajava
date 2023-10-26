package Trabalho.Car;

import de.tudresden.sumo.cmd.Vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import org.json.JSONObject;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import Trabalho.MobilityCompany.Company;
import Trabalho.Transporte.Transporte;
import io.sim.Transport.Fuel.SetFuelLevel;
import Trabalho.Rotas.Route;
import Trabalho.Driver.Driver;

/**Define os atributos que coracterizam um Carro.
 * Por meio de metodos get da classe Vehicle, 
 */
public class Car extends Vehicle implements Runnable {
	// atributos de cliente
    private Socket socket;
    private int companyServerPort;
    private String companyServerHost; 
	private DataInputStream input;
	private DataOutputStream output;
	
	private String idCar; // id do carro
	private SumoColor colorCar;
	private String driverID; // id do motorista
	private SumoTraciConnection sumo;
	private boolean on_off;
	private boolean finalizado; // chamado pelo Driver
	private long acquisitionRate; // taxa de aquisicao de dados dos sensores
	private int fuelType; 			// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; 	// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; 		// price in liters
	private double speed; 
	private Route rota;
	private double odometer;
	private double fuelTank;
    private double fuelConsumption;
    
	private String carStatus;
	private double latInicial;
	private double lonInicial;
	private double latAtual;
	private double lonAtual;
    
	
	private ArrayList<Driver> drivingRepport; // dados de conducao do veiculo
	private Transporte Trans;

	public Car(boolean _on_off, String _idCar, SumoColor _colorCar, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate,
            int _fuelType, int _fuelPreferential, double _fuelPrice, double _odometer, double _fuelConsumption, String _companyServerHost, 
            int _companyServerPort) throws Exception {
    // Inicialização do construtor da classe Car com diversos parâmetros.
    this.sumo = _sumo;  
    this.on_off = _on_off;
    this.idCar = _idCar;
    this.odometer = _odometer;
    this.fuelConsumption = _fuelConsumption;
    this.colorCar = _colorCar;
    this.driverID = _driverID;  
    this.acquisitionRate = _acquisitionRate;        
    this.companyServerPort = _companyServerPort;
    this.companyServerHost = _companyServerHost;
       
   
    if((_fuelType < 0) || (_fuelType > 4)) {
        this.fuelType = 4;
    } else {
        this.fuelType = _fuelType;
    }
    
    if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
        this.fuelPreferential = 4;
    } else {
        this.fuelPreferential = _fuelPreferential;
    }

    // Inicialização de diversos atributos.
    this.finalizado = false;
    this.fuelPrice = _fuelPrice;   // = 3,25;
    this.speed = setSpeed();
    this.rota = null;
    this.odometer = 0;
    this.fuelConsumption = setFuel();
    this.fuelTank = 10;
    this.carStatus = "aguardando";
    this.drivingRepport = new ArrayList<Drive>();

    // Inicialia DriverAgora 
    this.DriverAgora = new DrivingData(idCar, 
                                            driverID,
                                            "aguardando",   
                                            0, 0, 0, 
                                            0, 0, 0, 0, "", "", 
                                            0, 0, 0, 1, this.fuelType,
                                            this.fuelPrice, 0, 0);
}



	@Override
	public void run() {
		System.out.println(this.idCar + "Run");
		
		SetFuelLevel sf = new SetFuelLevel(this, (0.003*speed)); 
		sf.start();

		try {
			// Cria um socket para conectar ao servidor da empresa
			socket = new Socket(this.companyServerHost, this.companyServerPort);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		
			// Enquanto o veículo não estiver finalizado, ele continua sua execução
			while (!finalizado) {
				// Envia o estado atual do veículo para a empresa
				output.writeUTF(criarJSONDrivingData(DriverAgora));
				System.out.println(this.idCar + " aguardando rota");
				// Recebe a rota da empresa
				rota = extraiRota(input.readUTF());
		
				// Se a rota tiver ID igual a "-1," significa que não há mais rotas a receber
				if (rota.getID().equals("-1")) {
					System.out.println(this.idCar + " - Sem rotas a receber.");
					finalizado = true;
					break;
				}
		
				System.out.println(this.idCar + " leu " + rota.getID());
		
				// Inicia um serviço de transporte com a nova rota
				Trans = new Transporte(true, this.idCar, rota, this, this.sumo);
				Trans.start();
		
				// Obtém a aresta final da rota
				//String edgeFinal = this.getEdgeFinal();
				this.on_off = true;
		
				// Aguarda até que o veículo tenha partido (não esteja mais na aresta de partida)
				while (!Company.stillOnSUMO(this.idCar, this.sumo)) {
					Thread.sleep(this.acquisitionRate);
				}
		
				// Obtém a aresta atual do veículo
				String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));
		
				boolean initRoute = true;
		
				//
					// Verifica se a rota foi concluída
					//if (verificaRotaTerminada(edgeAtual, edgeFinal)) {
						System.out.println(this.idCar + " acabou a rota.");
						this.carStatus = "finalizado";
						//output.writeUTF(criarJSONDrivingData());
						this.on_off = false;
						break;
					}
		
					Thread.sleep(this.acquisitionRate);
		
					
		
						if (carStatus != "abastecendo") {
							this.carStatus = "rodando";
						}
		
						//output.writeUTF(criarJSONDrivingData());
		
						// Se o veículo estiver marcado como "finalizado," encerra a execução
						if (this.carStatus.equals("finalizado")) {
							this.on_off = false;
							break;
						} else {
							edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));
						}
					}
				}
				System.out.println(this.idCar + " off.");
		
				// Atualiza o estado do veículo com base na finalização
				if (!finalizado) {
					this.carStatus = "aguardando";
				}
		
				if (finalizado) {
					this.carStatus = "encerrado";
				}
			}
		
			// Encerra os canais e a conexão com a empresa
			System.out.println("Encerrando: " + idCar);
			input.close();
			output.close();
			socket.close();
			this.Trans.setTerminado(true);
		} catch (Exception e) {
			e.prinTranstackTrace();
		}		

		System.out.println(this.idCar + " encerrado.");
	}

	private void RefreshCars() {
		try {
			if (!this.getSumo().isClosed()) {
				
				SumoPosition2D sumoPosition2D;
				sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar));
				DriverAgora = new DrivingData(
					this.idCar, this.driverID, this.carStatus, this.latInicial, this.lonInicial,
					this.latAtual, this.lonAtual,
					
					System.currentTimeMillis(), sumoPosition2D.x, sumoPosition2D.y,
					(String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar)),
					(String) this.sumo.do_job_get(Vehicle.getRouteID(this.idCar)),
					(double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar)),
					(double) this.sumo.do_job_get(Vehicle.getDistance(this.idCar)),
	
					(double) this.sumo.do_job_get(Vehicle.getFuelConsumption(this.idCar)),
					1,				
					this.fuelType, this.fuelPrice,	
					(double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.idCar))		
                    );
	
				//this.Driver.add(AtualDriver); ERROR
				
				
				if (carStatus != "abastecendo") {
					this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));
					this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 0));
				}
	
			} else {
				this.on_off = false;
				System.out.println("Carr off");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public double getFuelConsumption(){
        return fuelConsumption;
    }
	public String getCarStatus() {
		return carStatus;
	}

	public Route getRota() {
		return rota;
	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public boolean getFinalizado() {
		return finalizado;
	}

	public void setFinalizado(boolean _finalizado) {
		this.finalizado = _finalizado;
	}

	public long getAcquisitionRate() {
		return this.acquisitionRate;
	}

	public void setAcquisitionRate(long _acquisitionRate) {
		this.acquisitionRate = _acquisitionRate;
	}

	public String getIdCar() {
		return this.idCar;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public void setFuelType(int _fuelType) {
		if((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	
	public double getFuelPrice() {
		return this.fuelPrice;
	}
	
	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	public void gastaCombustivel(double litros) {
		if (fuelTank >= litros) {
			fuelTank -= litros;
		} else {
			try {
				pararCarro();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Método que retorna o nível atual do tanque de combustível
	public double getNivelDoTanque() {
		return this.fuelTank;
	}

	// Método que simula o abastecimento do veículo
	public void abastecido(double litros) throws Exception{
		this.fuelTank += litros;
		carStatus = "rodando";
		voltarAndar();
	}

	// Método que retorna a cor do veículo
	public SumoColor getColorCar() {
		return this.colorCar;
	}

	// Método que retorna o tipo de combustível preferencial do veículo
	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	// Método para definir o tipo de combustível preferencial do veículo
	public void setFuelPreferential(int _fuelPreferential) {
		if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	
	public void StopCar() throws Exception{
		this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 0));//parar o veículo
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, 0));
	}
	
    
	public void voltarAndar() throws Exception {
		this.speed = setRandomSpeed();
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));// volta andar
	}

	
	public double setSpeed(){
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, 6));//  velocidade 
	}

    public double setfuelConsuption(){
		Random random = new Random();
		double range = 25 - 15;
		double scaled = random.nextDouble() * range;
		double generatedNumber = scaled + 15;
		return generatedNumber;
	}

	
	public double getSpeed() throws Exception{
		return (double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar)); //  velocidade atual do veículo
	}

	

	
	public void stopToFuel() throws Exception{
		carStatus = "abastecendo";// asbastendo
		StopCar();
	}


	

	// rota atual terminou ????
	private boolean StatusAndRota(String _edgeAtual, String _edgeFinal) throws Exception {
		SumoStringList lista = (SumoStringList) this.sumo.do_job_get(Vehicle.getIDList());

		if (!lista.contains(idCar) && (_edgeFinal.equals(_edgeAtual))) {
			return true;
		} else {
			return false;
		}
	}

	//##################
    //##################
    //##################
    //####JASON#########
    //##################
    //##################
    //##################
    //##################
    // extrair
    //criar
/* 
	
	private Rota extraiRota(String rotaJSON) {
		JSONObject rotaJSONObj = new JSONObject(rotaJSON);
		Rota rota = new Rota(rotaJSONObj.getString("ID da Rota"), rotaJSONObj.getString("Edges"));
		return rota;
	}

	
	private String criarJSONDrivingData(DrivingData drivingData) {
		JSONObject drivingDataJSON = new JSONObject();
		drivingDataJSON.put("Car ID", drivingData.getCarID());
		drivingDataJSON.put("Driver ID", drivingData.getDriverID());
		drivingDataJSON.put("Car Status", drivingData.getCarStatus());
		drivingDataJSON.put("Latitude Inicial", drivingData.getLatInicial());
		drivingDataJSON.put("Longitude Inicial", drivingData.getLonInicial());
		drivingDataJSON.put("Latitude Atual", drivingData.getLatAtual());
		drivingDataJSON.put("Longitude Atual", drivingData.getLonAtual());
		drivingDataJSON.put("TimeStamp", drivingData.getTimeStamp());
		drivingDataJSON.put("X_Position", drivingData.getX_Position());
		drivingDataJSON.put("Y_Position", drivingData.getY_Position());
		drivingDataJSON.put("RoadIDSUMO", drivingData.getRoadIDSUMO());
		drivingDataJSON.put("RouteIDSUMO", drivingData.getRouteIDSUMO());
		drivingDataJSON.put("Speed", drivingData.getSpeed());
		drivingDataJSON.put("Odometer", drivingData.getOdometer());
		drivingDataJSON.put("FuelConsumption", drivingData.getFuelConsumption());
		drivingDataJSON.put("AverageFuelConsumption", drivingData.getAverageFuelConsumption());
		drivingDataJSON.put("FuelType", drivingData.getFuelType());
		drivingDataJSON.put("FuelPrice", drivingData.getFuelPrice());
		drivingDataJSON.put("Co2Emission", drivingData.getCo2Emission());
		drivingDataJSON.put("HCEmission", drivingData.getHCEmission());
		drivingDataJSON.put("PersonCapacity", drivingData.getPersonCapacity());
		drivingDataJSON.put("PersonNumber", drivingData.getPersonNumber());
		return drivingDataJSON.toString();
	}
 */

