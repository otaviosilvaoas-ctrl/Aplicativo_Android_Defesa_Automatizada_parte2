package com.example.autotarget;

/**
 * Implementa a Reconciliação de Dados para otimização.
 * Equação: y_hat = y - V * A^T * (A * V * A^T)^-1 * A * y
 */
public class DataReconciliation {

    /**
     * Realiza a reconciliação de dados.
     * @param y Vetor de medidas ruidosas.
     * @param V Matriz de covariância das medidas.
     * @param A Matriz de incidência (restrições).
     * @return Vetor reconciliado y_hat.
     */
    public static double[] reconcile(double[] y, double[][] V, double[][] A) {
        try {
            int m = A.length; // Número de restrições
            int n = y.length; // Número de variáveis

            if (m == 0 || n == 0) return y;

            double[][] AT = MatrixMath.transpose(A);

            // Termo 1: A * V
            double[][] AV = MatrixMath.multiply(A, V);

            // Termo 2: (A * V * A^T)
            double[][] AVAT = MatrixMath.multiply(AV, AT);

            // Termo 3: (A * V * A^T)^-1
            double[][] invAVAT = MatrixMath.invert(AVAT);

            // Termo 4: A * y (resíduo das restrições)
            double[] Ay = MatrixMath.multiply(A, y);

            // Termo 5: (A * V * A^T)^-1 * (A * y)
            double[] T5 = MatrixMath.multiply(invAVAT, Ay);

            // Termo 6: V * A^T * Termo 5
            double[][] VAT = MatrixMath.multiply(V, AT);
            double[] correction = MatrixMath.multiply(VAT, T5);

            // y_hat = y - correction
            double[] yHat = new double[n];
            for (int i = 0; i < n; i++) {
                yHat[i] = y[i] - correction[i];
            }

            return yHat;
        } catch (Exception e) {
            GerenciadorMetricas.log("RECONCILIACAO", "Erro matemático: " + e.getMessage());
            return y;
        }
    }
}
