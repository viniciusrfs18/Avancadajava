package io.sim.Transport.CarDriver;

import io.sim.Pagamentos.Account;
import io.sim.Pagamentos.AlphaBank;
import io.sim.Pagamentos.BotPayment;
import io.sim.Transport.Fuel.FuelStation;
import io.sim.Transport.Rotas.Rota;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Driver extends Thread {
    
    // Cliente de AlphaBank
    private Account account; // Representa a conta bancária do motorista
    private Socket socket; // Conexão de socket
    private int alphaBankServerPort; // Porta do servidor AlphaBank
    private String alphaBankServerHost; // Endereço do servidor AlphaBank
    private DataInputStream input; // Fluxo de entrada de dados
    private DataOutputStream output; // Fluxo de saída de dados

    // Atributos da Classe
    private String driverID; // ID único do motorista
    private Car car; // Representa o veículo associado ao motorista
    private long acquisitionRate; // Taxa de aquisição de dados
    private ArrayList<Rota> rotasDisp = new ArrayList<Rota>(); // Lista de rotas disponíveis
    private Rota rotaAtual; // A rota atual do motorista
    private ArrayList<Rota> finishedRoutes = new ArrayList<Rota>(); // Lista de rotas concluídas
    private boolean initRoute = false; // Indica se uma nova rota foi iniciada
    private long balanceInicial; // Saldo inicial da conta bancária
    private FuelStation fs; // Estação de abastecimento de combustível associada ao motorista

    // Construtor
    public Driver(String _driverID, Car _car, long _acquisitionRate, FuelStation _postoCombustivel, int _alphaBankServerPort, String _alphaBankServerHost) {
        this.driverID = _driverID;
        this.car = _car;
        this.acquisitionRate = _acquisitionRate;
        this.rotasDisp = new ArrayList<Rota>();
        rotaAtual = null;
        this.finishedRoutes = new ArrayList<Rota>();
        this.balanceInicial = 10000;
        this.alphaBankServerPort = _alphaBankServerPort;
        this.alphaBankServerHost = _alphaBankServerHost;
        this.fs = _postoCombustivel;
    }

    // Sobrescreve o método run da classe Thread
    @Override
    public void run() {
        try {
            System.out.println("Iniciando " + this.driverID);
            
            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            this.account = new Account(driverID, 50);
            AlphaBank.addAccount(account);
            account.start();
            
            System.out.println(driverID + " se conectou ao Servidor do AlphaBank!!");
            
            Thread threadCar = new Thread(this.car);
            threadCar.start();

            while(threadCar.isAlive()) {
                Thread.sleep(this.car.getAcquisitionRate());
                
                if(car.getCarStatus() == "finalizado") {
                    System.out.println(this.driverID + " rota " + this.rotasDisp.get(0).getID() + " finalizada");
                    finishedRoutes.add(rotaAtual);
                    initRoute = false;
                } else if((this.car.getCarStatus() == "rodando") && !initRoute) {
                    System.out.println(this.driverID + " rota "+ this.car.getRota().getID() +" iniciada");
                    rotaAtual = car.getRota();
                    initRoute = true; 
                }

                if (this.car.getNivelDoTanque() < 3){
                    double litros = (10 - this.car.getNivelDoTanque());
                    double qtdFuel = qtdToFuel(litros, this.account.getBalance());
                        
                    try {
                        System.out.println(driverID + " decidiu abastecer " + qtdFuel);
                            
                        this.car.stopToFuel();
                            
                        fs.fuelCar(this.car, qtdFuel);
                            
                        fsPayment(socket, (qtdFuel * fs.getprice()));
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //System.out.println(account.getAccountID() + " tem R$" + account.getBalance() + " de balance");
            }
            
            car.setFinalizado(true);  
            System.out.println("Encerrando " + driverID);
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Função utilizada para criar o BotPayment responsável por realizar o pagamento do Driver à FuelStation
    private void fsPayment(Socket socket, double amount){
        BotPayment bt = new BotPayment(socket, account.getAccountID(), account.getPassword(), "FuelStation", amount);
        bt.start();
    }

    public Car getCar(){
        return this.car;
    }

    // Método responsável por informar a quantidade de litros que o carro irá abastecer, com base no balance bancário do motorista, simulando a realidade.
    public double qtdToFuel(double litros, double balanceDisp) { 
        double price = fs.getprice();
        double priceTotal = litros * price;

        if (balanceDisp > priceTotal) {
            return litros;
        } else {
            double priceReduzindo = priceTotal;
            while (balanceDisp < priceReduzindo) {
                litros--;
                priceReduzindo = litros * price;
                
                if (litros <= 0) {
                    return 0;
                }
            }
            return litros;
        }
    }

    public String getDriverId() {
        return this.driverID;
    }

    public FuelStation getFuelStation() {
        return this.fs;
    }
}
