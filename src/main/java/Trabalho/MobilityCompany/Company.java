package Trabalho.MobilityCompany;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Trabalho.Car.CarRepport;
import Trabalho.Relatorio.Relatorio;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import Trabalho.Banco.Account;

import Trabalho.Banco.AlphaBank;
//import Trabalho.Banco.BotPayment;
import Trabalho.Car.Car;
import Trabalho.Rotas.Route;


public class Company extends Thread {
    // Atributos de Servidor
    private ServerSocket serverSocket;

    // Atributos para syncção
    private static Object sync;
    private static boolean rotasDisponiveis;
    private boolean CarUP;

    // Atributos da classe
    private ArrayList<Route> rotasDisp;
    private ArrayList<Route> routesInExec;
    private ArrayList<Route> finishedRoutes;
    private double price;
    private static int numDrivers;
    private static ArrayList<Driver> dd;

    // Atributos como cliente de AlphaBank
    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost; 
    private DataInputStream input;
    private DataOutputStream output;
    
    public Company(ServerSocket serverSocket, ArrayList<Route> Routes, int _numDrivers, int _alphaBankServerPort, String _alphaBankServerHost) {
        
        // Inicializa servidor
        this.serverSocket = serverSocket;

        // Inicializa atributos de syncção
        sync = new Object();
        rotasDisponiveis = true;
        this.CarUP = true;

        // Atributos da classe
        this.rotasDisp = Routes;
		//System.out.println("Rotas: "+ rotasDisp.size()+" rotas disponiveis");
        routesInExec = new ArrayList<Route>();
        finishedRoutes = new ArrayList<Route>();
        price = 3.25;
        numDrivers = _numDrivers;
        dd = new ArrayList<Driver>();

        // Atributos como cliente de AlphaBank
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;

    }

    @Override
    public void run() {
        try {
            System.out.println("Company iniciando...");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
            
            this.account = new Account("Company", 100000);
            AlphaBank.addAccount(account);
            account.start();
            
            System.out.println("Company se conectou ao Servido do AlphaBank!!");

            while (rotasDisponiveis) {
                
                if(!CarUP) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Verifica se ainda tem roas disponíveis
                if(rotasDisp.size() == 0 && routesInExec.size() == 0) {
                    System.out.println("Rotas terminadas");
                    rotasDisponiveis = false;
                }

                if(CarUP) {
                    boolean start = true;
                    for(int i = 0; i < numDrivers; i++) {
                       
                        Socket socket = serverSocket.accept();

                        // Cria uma thread para com de cada Car
                        //CarRepport cr = new CarRepport(socket, this);
                        cr.start();

                        if (start) {
                            Atualiza att = new atualiza(this);
                            att.start();
                            start = false;
                        }
                    }
                    CarUP = false;
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Encerrando a Company.");
    }

    // oha se tem rota vaga

    public static boolean routesAvaliable() {
        return rotasDisponiveis;
    }

    public boolean rotasDispVazio(){
        return (routesInExec.isEmpty());
    }

    
    
        
	

    // Libera uma rota para o cliente que a solicitou. Para isso, remove de "rotasDisp" e adiciona em "routesInExec"
    public Route execRoute() {
        synchronized (sync) {
            Route rota = rotasDisp.remove(0);
            routesInExec.add(rota);
            return rota;
        }
    }

    // Método responsável por adicionar a rota terminada ao ArrayList de Rotas Finalizadas
    public void endRoute(String _routeID) {
        synchronized (sync) {
            System.out.println("Arquivando rota: " + _routeID);
            int i = 0;
            while (!routesInExec.get(i).getID().equals(_routeID)) {
                i++;
            }
            finishedRoutes.add(routesInExec.remove(i));
        }
    }

    // Método responsável por criar o BotPayment que realizará o pagamento por 1Km percorrido ao motorista.
    public void oneKmPay(String driverID) throws IOException {
        BotPayment bt = new BotPayment(socket, "Company",  account.getPassword(), driverID, price);
        bt.start();
    }

    // Método para enviar informações de comunicação aos carros (Driver)
    // Synchronized para garantir o acesso seguro a partir de várias threads simultaneamente.
    public synchronized void sendCommunication(Driver com) {
        dd.add(com); // Adiciona o objeto com à lista dd para ser processado pelos carros.
    }

    // Verifica se existem relatórios (Driver) disponíveis.
    public boolean temReport() {
        return dd.isEmpty(); // Retorna true se a lista dd estiver vazia, caso contrário, retorna false.
    }
    
    // Método para pegar um relatório (Driver) da lista.
    public Driver pegacom() {
        return dd.remove(0); // Remove o primeiro relatório da lista e o retorna.
    }

    // Método para obter o preço por quilômetro utilizado para calcular os pagamentos aos motoristas.
    public double getprice() {
        return this.price; // Retorna o valor armazenado no atributo price.
    }

    // Classe interna BotPayment
    public class BotPayment extends Thread {
        private Socket socket;
        private String payerID;
        private String payerPassword;
        private String receiverID;
        private double amount;

        public BotPayment(Socket _socket, String _payerID, String _payerPassword, String _receiverID, double _amount) {
            this.socket = _socket;
            this.payerID = _payerID;
            this.payerPassword = _payerPassword;
            this.receiverID = _receiverID;
            this.amount = _amount;
        }

        @Override
        public void run() {
            try {
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream());

                String[] login = { payerID, payerPassword };
                output.writeUTF(loginJSON(login));

                TransferData td = new TransferData(payerID, "Pagamento", receiverID, amount);
                output.writeUTF(transferDataJSON(td));

                String response = input.readUTF();
                boolean success = extractResponse(response);

                if (success) {
                    System.out.println("Transferência bem-sucedida!");
                } else {
                    System.out.println("Transferência falhou.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String transferDataJSON(TransferData transferData) {
            JSONObject transferDataJSON = new JSONObject();
            transferDataJSON.put("payerID", transferData.getpayer());
            transferDataJSON.put("operation", transferData.getoperation());
            transferDataJSON.put("receiverID", transferData.getreceiver());
            transferDataJSON.put("amount", transferData.getamount());
            return transferDataJSON.toString();
        }

        private String loginJSON(String[] login) {
            JSONObject json = new JSONObject();
            json.put("payerID", login[0]);
            json.put("payerPassword", login[1]);
            return json.toString();
        }

        private boolean extractResponse(String responseJSON) {
            JSONObject response = new JSONObject(responseJSON);
            return response.getBoolean("response");
        }
    }
}