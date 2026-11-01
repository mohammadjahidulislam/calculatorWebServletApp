package com.example.model;

import java.sql.Timestamp;

/** One row of the calculation table. */
public class Calculation {

    private long id;
    private double operand1;
    private Double operand2;     // null for single-number operations
    private String operation;
    private Double result;       // null when there was an error
    private String errorMsg;     // null when the calculation succeeded
    private Timestamp createdAt;

    public Calculation() { }

    public Calculation(double operand1, Double operand2, String operation, Double result, String errorMsg) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operation = operation;
        this.result = result;
        this.errorMsg = errorMsg;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public double getOperand1() { return operand1; }
    public void setOperand1(double operand1) { this.operand1 = operand1; }

    public Double getOperand2() { return operand2; }
    public void setOperand2(Double operand2) { this.operand2 = operand2; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Double getResult() { return result; }
    public void setResult(Double result) { this.result = result; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
