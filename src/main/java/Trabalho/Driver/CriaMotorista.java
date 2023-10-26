package Trabalho.Driver;

import it.polito.appeal.traci.SumoTraciConnection;
import java.util.ArrayList;
import de.tudresden.sumo.objects.SumoColor;
import Trabalho.Posto.FuelStation;
import Trabalho.Car.Car;

public class CriaMotorista {

    // Variáveis estáticas para configuração dos drivers
    public static boolean on_off = false;               // Indica se o veículo está ligado ou desligado
    public static int fuelType = 2;                     // Tipo de combustível (2 - Gasolina)
    public static int fuelPreferential = 2;             // Tipo de combustível preferencial (2 - Gasolina)
    public static double fuelPrice = 5.89;              // Preço do combustível por litro
   // public static int personCapacity = 1;               // Capacidade máxima de passageiros no veículo
    public static int personNumber = 1;                 // Número atual de passageiros no veículo

    
    public static ArrayList<Driver> criaDrivers(int qtdDrivers, FuelStation fuelStation, long acquisitionRate, SumoTraciConnection sumo, String host, int portaCompanny, int portaAlphaBank) throws Exception {
        
        ArrayList<Driver> drivers = new ArrayList<>(); 
        SumoColor cor = new SumoColor(0, 255, 36, 0); //vermelho

        for (int i = 0; i < qtdDrivers; i++) {
            
            String driverID = "Driver" + (i + 1);
            String carID = "Car" + (i + 1);

            Car car = new Car(on_off, carID, cor, driverID, sumo, acquisitionRate, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber, host, portaCompanny);

            Driver driver = new Driver(driverID, car, acquisitionRate, fuelStation, portaAlphaBank, host);

            drivers.add(driver);
            
        }

        return drivers;
    }

}