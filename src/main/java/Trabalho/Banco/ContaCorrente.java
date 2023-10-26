package Trabalho.Banco;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import org.json.JSONObject;

public class ContaCorrente {

    public static class Conta extends Thread {
        private String accountID;         
        private String password;          
        private double balance;           
        private ArrayList<Transferencias> transactionHistory;

        private Object sync;

        public Conta(String _accountID, double _balance) {
            this.accountID = _accountID;
            this.password = createPassword();
            this.balance = _balance;
            this.transactionHistory = new ArrayList<Transferencias>();
            this.sync = new Object();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (AlphaBank.pendingRecords() != 0) {
                        Thread.sleep(200);
                        Transferencias register = AlphaBank.getRecord(accountID); // nao funciona
                        if (register != null) {
                            transactionHistory.add(register);
                            System.out.println(register.getDescricao());
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public String getAccountID() {
            return accountID;
        }

        public String getPassword() {
            return password;
        }

        public double getBalance() {
            synchronized (sync) {
                return balance;
            }
        }

        public void setBalance(double _balance) {
            this.balance = _balance;
        }

        public void deposit(double amount) {
            synchronized (sync) {
                if (amount > 0) {
                    balance += amount;
                } else {
                    System.out.println("erro deposito ");
                }
            }
        }

        public void withdraw(double amount) {
            synchronized (sync) {
                if (amount > 0) {
                    if (balance >= amount) {
                        balance -= amount;
                    } else {
                        System.out.println("insuficiente ");
                    }
                } else {
                    System.out.println(" erro de retirada");
                }
            }
        }

        private String createPassword(){
            Random random = new Random();
            StringBuilder password = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int randomDigit = random.nextInt(10);
                password.append(randomDigit);
            }
            return password.toString();
        }
    }

    public static class ComunicacaoConta extends Thread {
        private Socket socket;
        private DataInputStream input;
        private DataOutputStream output;
        private AlphaBank alphaBank;

        public ComunicacaoConta(Socket _socket, AlphaBank _alphaBank) {
            this.socket = _socket;
            this.alphaBank = _alphaBank;
        }

        @Override
        public void run() {
            boolean aux = false;
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());

                while (!aux) {
                    String[] login = loginExtration(input.readUTF());

                    if (alphaBank.conect(login)) {
                        Transferencias tf = TransferenciasExtration(input.readUTF());
                        String operation = tf.getoperation();

                        if (operation.equals("Pagamento")) {
                            String receiverID = tf.getreceiver();
                            double amount = tf.getamount();

                            if (alphaBank.transferencia(login[0], receiverID, amount)) {
                                output.writeUTF(responseJSON(true));
                                alphaBank.addRecords(tf);
                            } else {
                                output.writeUTF(responseJSON(false));
                            }
                        } else if (operation.equals("Sair")) {
                            aux = true;
                        }
                    } else {
                        System.out.println("AB - Login malsucedido, verifique o ID e a senha: " + login[0]);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private String[] loginExtration(String loginJSON) {
            JSONObject json = new JSONObject(loginJSON);
            String[] login = new String[] { json.getString("payerID"), json.getString("payerPassword") };
            return login;
        }

        private Transferencias TransferenciasExtration(String TransferenciasJSON) {
            JSONObject TransferenciasJSONObj = new JSONObject(TransferenciasJSON);
            String payer = TransferenciasJSONObj.getString("payerID");
            String operation = TransferenciasJSONObj.getString("operation");
            String receiver = TransferenciasJSONObj.getString("receiverID");
            double amount = TransferenciasJSONObj.getDouble("amount");

            Transferencias tf = new Transferencias(payer, operation, receiver, amount);
            return tf;
        }

        private String responseJSON(boolean success) {
            JSONObject json = new JSONObject();
            json.put("response", success);
            return json.toString();
        }
    }
}
