package Trabalho.Driver;

import Trabalho.Banco.ContaCorrente;
import Trabalho.Banco.AlphaBank;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DriverPayment {
    private ContaCorrente account; 
    private Socket socket; 
    private int alphaBankServerPort;
    private String alphaBankServerHost;
    private DataInputStream input; 
    private DataOutputStream output; 
    private long balanceInicial = 10000;

    public DriverPayment(int alphaBankServerPort, String alphaBankServerHost) {
        this.alphaBankServerPort = alphaBankServerPort;
        this.alphaBankServerHost = alphaBankServerHost;
    }

    public void connectToBank(String driverID) throws IOException {
        socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        this.account = new Account(driverID, 50); // Inicialização da conta com valor inicial.
        // AlphaBank.addAccount(account); // Assumindo que AlphaBank é uma classe estática. 
        account.start();
        System.out.println(driverID + " se conectou ao Servidor do AlphaBank!!");
    }

    private void fsPayment(Socket socket, double amount) {
        BotPayment bot = new BotPayment(socket, account.getAccountID(), account.getPassword(), "FuelStation", amount);
        bot.start();
    }

    public double PagaBomba(double litros, double balanceDisp, FuelStation Fluflu) { 
        double price = Flulu.getprice();
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

   
}

