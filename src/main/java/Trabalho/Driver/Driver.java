package Trabalho.Driver;



import Trabalho.Rotas.Route;
import io.sim.DrivingData;

import java.util.ArrayList;

import org.json.JSONObject;

import Trabalho.Posto.FuelStation;
import Trabalho.Car.Car;


public class Driver extends Thread {

    private DriverPayment driverPayment; // Classe para lidar com pagamentos e operações bancárias.
    private String driverID;
    private Car car;
    private long acquisitionRoute;
    private ArrayList<Route> rotasDisp = new ArrayList<Route>();
    private Route rotaAtual;
    private ArrayList<Route> finishedRoutes = new ArrayList<Route>();
    private boolean initRoute = false;
    private FuelStation fs;

    public Driver(String _driverID, Car _car, long _acquisitionRoute, FuelStation _postoCombustivel, int _alphaBankServerPort, String _alphaBankServerHost) {
        this.driverID = _driverID;
        this.car = _car;
        this.acquisitionRoute = _acquisitionRoute;
        this.rotasDisp = new ArrayList<Route>();
        rotaAtual = null;
        this.finishedRoutes = new ArrayList<Route>();
        this.fs = _postoCombustivel;
        this.driverPayment = new DriverPayment(_alphaBankServerPort, _alphaBankServerHost); // Inicializa o DriverPayment.
    }

    @Override
    public void run() {
        try {
            System.out.println("Iniciando " + this.driverID);
            driverPayment.connectToBank(driverID);
            
            Thread threadCar = new Thread(this.car);
            threadCar.start();

            while (threadCar.isAlive()) {
                Thread.sleep(this.car.getAcquisitionRate());

                if (car.getCarStatus().equals("finalizado")) {
                    System.out.println(this.driverID + " rota " + this.rotasDisp.get(0).getID() + " finalizada");
                    finishedRoutes.add(rotaAtual);
                    initRoute = false;
                } else if (car.getCarStatus().equals("rodando") && !initRoute) {
                    System.out.println(this.driverID + " rota " + this.car.getRota().getID() + " iniciada");
                    rotaAtual = car.getRota();
                    initRoute = true;
                }

                if (this.car.getNivelDoTanque() < 3) {
                    double litros = (10 - this.car.getNivelDoTanque());
                    double qtdFuel = driverPayment.qtdToFuel(litros, driverPayment.getAccount().getBalance(), fs); // Usa DriverPayment para calcular.

                    try {
                        System.out.println(driverID + " decidiu abastecer " + qtdFuel);
                        this.car.stopToFuel();
                        fs.fuelCar(this.car, qtdFuel);
                        driverPayment.fsPayment(driverPayment.getSocket(), (qtdFuel * fs.getprice())); // Faz o pagamento usando DriverPayment.
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            car.setFinalizado(true);
            System.out.println("Encerrando " + driverID);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Métodos de getter/setter ou outros podem ser adicionados conforme necessário.
    public Car getCar() {
        return this.car;
    }

    public String getDriverId() {
        return this.driverID;
    }

    public FuelStation getFuelStation() {
        return this.fs;
    }
    private String routeJSON(Route rota) {
        JSONObject rotaJSON = new JSONObject();
        rotaJSON.put("ID da Rota", rota.getID());
        rotaJSON.put("Edges", rota.getEdges());
        return rotaJSON.toString();
    }

    // Extrai dados de condução a partir de uma representação JSON e retorna um objeto de DrivingData
    private DrivingData extractDrivingData(String drivingDataJSON) {
        JSONObject drivingDataJSONObj = new JSONObject(drivingDataJSON);
        String carID = drivingDataJSONObj.getString("Car ID");
        String driverID = drivingDataJSONObj.getString("Driver ID");
        String carStatus = drivingDataJSONObj.getString("Car Status");
        double latInicial = drivingDataJSONObj.getDouble("Latitude Inicial");
        double lonInicial = drivingDataJSONObj.getDouble("Longitude Inicial");
        double latAtual = drivingDataJSONObj.getDouble("Latitude Atual");
        double lonAtual = drivingDataJSONObj.getDouble("Longitude Atual");
        long timeStamp = drivingDataJSONObj.getLong("TimeStamp");
        double X_Position = drivingDataJSONObj.getDouble("X_Position");
        double Y_Position = drivingDataJSONObj.getDouble("Y_Position");
        String roadIDSUMO = drivingDataJSONObj.getString("RoadIDSUMO");
        String routeIDSUMO = drivingDataJSONObj.getString("RouteIDSUMO");
        double speed = drivingDataJSONObj.getDouble("Speed");
        double odometer = drivingDataJSONObj.getDouble("Odometer");
        double fuelConsumption = drivingDataJSONObj.getDouble("FuelConsumption");
        double averageFuelConsumption = drivingDataJSONObj.getDouble("AverageFuelConsumption");
        int fuelType = drivingDataJSONObj.getInt("FuelType");
        double fuelPrice = drivingDataJSONObj.getDouble("FuelPrice");
        double Co2Emission = drivingDataJSONObj.getDouble("Co2Emission");
        int personCapacity = drivingDataJSONObj
.getInt("PersonCapacity");
        int personNumber = drivingDataJSONObj.getInt("PersonNumber");

        DrivingData drivingData = new DrivingData(carID, driverID, carStatus, latInicial, lonInicial, latAtual, lonAtual, timeStamp, 
        X_Position, Y_Position, roadIDSUMO, routeIDSUMO, speed, odometer, fuelConsumption, averageFuelConsumption, 
        fuelType, fuelPrice, Co2Emission, HCEmission, personCapacity, personNumber);

		return drivingData;
	}
}

