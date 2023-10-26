package Trabalho.Posto;

import java.util.concurrent.Semaphore;

import Trabalho.Car.Car;

public class FuelBomba {

    private Semaphore pump; // Semáforo para controlar o acesso às bombas
    private double price;   // Preço por litro de gasolina

    public FuelBomba() {
        this.pump = new Semaphore(2); // Duas bombas disponíveis
        this.price = 5.87;             // Preço por litro de gasolina
    }

    // Método que retorna o preço por litro de gasolina
    public double getPrice() {
        return this.price;
    }

    // Método para abastecer o carro
    public void fuelCar(Car car, double litros) {
        try {
            pump.acquire();

            System.out.println(car.getIdCar() + " está abastecendo no Posto de Gasolina");
            Thread.sleep(120000); // Tempo de abastecimento de 2 minutos

            car.abastecido(litros);
            System.out.println(car.getIdCar() + " terminou de abastecer");

            pump.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
