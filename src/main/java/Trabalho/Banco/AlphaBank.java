package Trabalho.Banco;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import Trabalho.Banco.Transferencias;


public class AlphaBank extends Thread {
    
    private ServerSocket serverAlphaBank; // Socket do Servidor (AlphaBank)
    private static ArrayList<Account> accounts; // ArrayList com todas as contas criadas
    private static ArrayList<Transferencias> records; 

    // Atributo de sincronização
    private Object sync;

    public AlphaBank(ServerSocket serverSocket) throws IOException {

        this.serverAlphaBank = serverSocket;
        accounts = new ArrayList<Account>();
        records = new ArrayList<Transferencias>();
        this.sync = new Object();
        createTransfSheet();
    }

    @Override
    public void run() {
    
        try {

            System.out.println("AlphaBank iniciado. Aguardando conexões...");
            
            while (true) {

                Socket clientSocket = serverAlphaBank.accept();

                AccountCommunication accountManipulator = new AccountCommunication(clientSocket, this);
                accountManipulator.start();
            
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }

    // Método para adicionar uma conta à lista de contas
    public static void addAccount(Account conta) {

        synchronized (AlphaBank.class) {

            if (accounts != null) {
                accounts.add(conta);
            } else {
                System.out.println("AlphaBank não iniciado.");
            }
        
        }

    }

    // Método para verificar se o login é bem-sucedido
    public boolean conect(String[] login) {

        String accountID = login[0];
        String password = login[1];

        for (Account account : accounts) {
            if (account.getAccountID().equals(accountID)) {
                if (account.getPassword().equals(password)) {
                    return true;
                }
            }
        }

        return false;

    }

    // Método para realizar uma transferência entre contas
    public boolean transferencia(String pagId, String recId, double amount) {
        Account payer = searchAccount(pagId);
        Account receiver = searchAccount(recId);
        
        synchronized (sync) {

            if (payer != null && receiver != null) {
                if (payer.getBalance() >= amount) {

                    payer.withdraw(amount); 
                    receiver.deposit(amount);
                    return true;
                
                } else {
                
                    System.out.println(payer + " Sem Saldo");
                
                }

            } else {
                
                System.out.println("ID nao encontrado");
            
            }
            return false;
        }

    }

    // Método para pesquisar uma conta por ID
    private static Account searchAccount(String accountID) {

        for (Account account : accounts) {
            if (account.getAccountID().equals(accountID)) {
                return account;
            }
        }
        return null;

    }

    // Método para adicionar registros de transferência
    public void addRecords(Transferencias registerPag) {

        registerPag.setTimestamp();
        registerPag.setAccountID(registerPag.getpayer());
        records.add(registerPag);

        Transferencias registerReceb = new Transferencias(registerPag.getpayer(), "Transferencia concluida", registerPag.getreceiver(), registerPag.getamount());
        registerReceb.setTimestamp();
        registerReceb.setAccountID(registerPag.getreceiver());
        records.add(registerReceb);
    }

    

    }

    //  exel
    private void createTransfSheet(){
        
        String nomeDoArquivo = "transacoes.xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {

            // Crie uma nova planilha (aba)
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("All");

            // Crie o cabeçalho na primeira linha
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Account ID");
            headerRow.createCell(1).setCellValue("payer");
            headerRow.createCell(2).setCellValue("operation");
            headerRow.createCell(3).setCellValue("receiver");
            headerRow.createCell(4).setCellValue("amount");

            // Salve a planilha com o cabeçalho
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


