package org.example;

import com.example.dao.CalculationDAO;
import com.example.model.Calculation;
import com.example.util.CalcUtil;

import java.sql.SQLException;
import java.util.Scanner;

/** Runs from the console (no server needed). */
public class Main {

    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);
        CalculationDAO dao = new CalculationDAO();

        System.out.print("First number: ");
        double a = sc.nextDouble();

        System.out.print("Operator (+  -  *  /  %  ^  sqrt  abs  fact): ");
        String op = CalcUtil.fromSymbol(sc.next());
        if (op == null) {
            System.out.println("Unknown operator.");
            return;
        }

        Double b = null;
        if (CalcUtil.needsSecondNumber(op)) {
            System.out.print("Second number: ");
            b = sc.nextDouble();
        }

        CalcUtil.Result res = CalcUtil.calculate(op, a, b);
        dao.save(new Calculation(a, b, op, res.value, res.error));

        if (res.error != null) {
            System.out.println("Error: " + res.error + " -> saved!");
        } else {
            System.out.println(CalcUtil.describe(op, a, b) + " = " + CalcUtil.format(res.value) + " -> saved!");
        }
    }
}
