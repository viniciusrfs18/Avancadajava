package Trabalho.Posto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


import Trabalho.Banco.Account;
import Trabalho.Banco.AlphaBank;
import Trabalho.Car.Car;


public class FuelStation extends Thread {

    // Atributos para comunicação com o AlphaBank
    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost;
    private double price;
    private DataInputStream input;
    private DataOutputStream output;
    
    // Atributo para FuelBomba
    private FuelBomba bomba;

    public FuelStation(int _alphaBankServerPort, String _alphaBankServerHost) {
        this.alphaBankServerPort = _alphaBankServerPort;
        this.alphaBankServerHost = _alphaBankServerHost;
        this.bomba = new FuelBomba();
    }

    @Override
    public void run() {
        try {
            System.out.println("Fuel Station iniciando...");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            this.account = new Account("FuelStation", 0);
            AlphaBank.addAccount(account);
            account.start();

            while (true) {
                // Aqui você precisa implementar o loop principal da estação
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Encerrando a Fuel Station...");
    }
    
    public String getFSAccountID() {
        return this.account.getAccountID();
    }

    public void handleCarFueling(Car car, double litros) {
        this.bomba.fuelCar(car, litros);
    }
    public double getprice() {
        return this.price;
    }
}
