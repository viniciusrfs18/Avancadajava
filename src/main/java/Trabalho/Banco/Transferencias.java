package Trabalho.Banco;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import org.apache.poi.ss.usermodel.*;

public class Transferencias {
    private String accountID;
    private String payer;
    private String operation;
    private String receiver;
    private double amount;
    private Timestamp timestamp;

  
    public Transferencias(String accountID, String payer, String operation, String receiver, double amount, Timestamp timestamp) {
        this.accountID = accountID;
        this.payer = payer;
        this.operation = operation;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
        updateSheet();
    }

   
    public Transferencias(String payer, String operation, String receiver, double amount) {
        this("", payer, operation, receiver, amount, new Timestamp(System.currentTimeMillis()));
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getAccountID() {
        return accountID;
    }

    public String getPayer() {
        return payer;
    }

    public String getOperation() {
        return operation;
    }

    public String getReceiver() {
        return receiver;
    }

    public double getAmount() {
        return amount;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getDescricao() {
        if ("Pagamento".equals(operation)) {
            return payer + " transferiu R$" + amount + " para " + receiver;
        } else if ("Recebimento".equals(operation)) {
            return receiver + " recebeu R$" + amount + " de " + payer;
        }
        return "";
    }

    private void updateSheet() {
        String nomeDoArquivo = "banco.xlsx";

        try (FileInputStream inputStream = new FileInputStream(nomeDoArquivo);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
           
            Sheet sheet = workbook.getSheet("All");    
            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(getAccountID());
            newRow.createCell(1).setCellValue(getPayer());
            newRow.createCell(2).setCellValue(getOperation());
            newRow.createCell(3).setCellValue(getReceiver());
            newRow.createCell(4).setCellValue(getAmount());
            
            try (FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
                workbook.write(outputStream);
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
